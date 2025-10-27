package service;

import dao.order.CredentialDAO;
import dao.order.OrderDAO;
import dao.product.ProductDAO;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import model.OrderStatus;
import model.Orders;
import model.PaginatedResult;
import model.Products;
import model.product.ProductVariantOption;
import model.view.OrderDetailView;
import model.view.OrderRow;
import model.Wallets;
import queue.OrderQueueProducer;
import queue.memory.InMemoryOrderQueue;
import service.util.ProductVariantUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Dịch vụ nghiệp vụ cho luồng mua hàng của người mua: kiểm tra tồn kho, xác minh số dư ví,
 * phát hành khóa idempotent, khởi tạo đơn chờ xử lý, phát thông điệp vào hàng đợi và cung cấp
 * dữ liệu cho các trang JSP lịch sử/chi tiết.</p>
 * <p>Mỗi phương thức trong lớp này đều mô tả rõ ràng luồng dữ liệu đi/đến các lớp DAO tương ứng
 * (OrderDAO, WalletsDAO, CredentialDAO) giúp người đọc nắm được cách dữ liệu di chuyển từ DB tới view.</p>
 *
 * @author longpdhe171902
 */
public class OrderService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "Pending", "Processing", "Completed", "Failed", "Refunded", "Disputed"
    );
    private static final Map<OrderStatus, String> STATUS_LABELS = buildStatusLabels();

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CredentialDAO credentialDAO;
    private final WalletsDAO walletsDAO;
    private final WalletTransactionDAO walletTransactionDAO;
    private final OrderQueueProducer queueProducer;
    /**
     * Khởi tạo dịch vụ với toàn bộ DAO mặc định, dùng cho môi trường sản xuất.
     */
    public OrderService() {
        this(new OrderDAO(), new ProductDAO(), new CredentialDAO(), new WalletsDAO(),
                new WalletTransactionDAO(), InMemoryOrderQueue.getInstance());
    }

    /**
     * Khởi tạo dịch vụ khi cần truyền sản phẩm và hàng đợi tùy chỉnh (phục vụ test).
     *
     * @param orderDAO        DAO quản lý bảng {@code orders}
     * @param productDAO      DAO truy vấn sản phẩm
     * @param credentialDAO   DAO kiểm tra mã bàn giao
     * @param queueProducer   bộ phát sự kiện sang hàng đợi xử lý bất đồng bộ
     */
    public OrderService(OrderDAO orderDAO, ProductDAO productDAO, CredentialDAO credentialDAO,
            OrderQueueProducer queueProducer) {
        this(orderDAO, productDAO, credentialDAO, new WalletsDAO(), new WalletTransactionDAO(), queueProducer);
    }

    /**
     * Khởi tạo dịch vụ với toàn bộ thành phần cần thiết.
     *
     * @param orderDAO              DAO quản lý đơn hàng
     * @param productDAO            DAO quản lý sản phẩm
     * @param credentialDAO         DAO kiểm tra mã bàn giao
     * @param walletsDAO            DAO ví người dùng
     * @param walletTransactionDAO  DAO giao dịch ví
     * @param queueProducer         bộ phát sự kiện hàng đợi nội bộ
     */
    public OrderService(OrderDAO orderDAO, ProductDAO productDAO, CredentialDAO credentialDAO,
            WalletsDAO walletsDAO, WalletTransactionDAO walletTransactionDAO, OrderQueueProducer queueProducer) {
        this.orderDAO = Objects.requireNonNull(orderDAO, "orderDAO");
        this.productDAO = Objects.requireNonNull(productDAO, "productDAO");
        this.credentialDAO = Objects.requireNonNull(credentialDAO, "credentialDAO");
        this.walletsDAO = Objects.requireNonNull(walletsDAO, "walletsDAO");
        this.walletTransactionDAO = Objects.requireNonNull(walletTransactionDAO, "walletTransactionDAO");
        this.queueProducer = Objects.requireNonNull(queueProducer, "queueProducer");
        // Đảm bảo worker bất đồng bộ được cấu hình với đầy đủ DAO khi service được khởi tạo.
        InMemoryOrderQueue.ensureWorkerInitialized(orderDAO, productDAO, credentialDAO, walletsDAO, walletTransactionDAO);
    }

    /**
     * Đặt đơn chờ xử lý với khóa idempotency ngẫu nhiên.
     *
     * @param userId    mã người mua
     * @param productId mã sản phẩm
     * @param quantity  số lượng cần mua
     * @return mã đơn hàng được tạo
     */
    public int placeOrderPending(int userId, int productId, int quantity) {
        return placeOrderPending(userId, productId, quantity, null, UUID.randomUUID().toString());
    }

    /**
     * Đặt đơn chờ xử lý với khóa idempotency do phía client truyền lên.
     *
     * @param userId         mã người mua
     * @param productId      mã sản phẩm
     * @param quantity       số lượng cần mua
     * @param idempotencyKey khóa chống tạo trùng
     * @return mã đơn hàng được tạo hoặc đơn có sẵn
     */
    public int placeOrderPending(int userId, int productId, int quantity, String idempotencyKey) {
        return placeOrderPending(userId, productId, quantity, null, idempotencyKey);
    }

    /**
     * Đặt đơn chờ xử lý, kiểm tra tồn kho và số dư ví trước khi đẩy vào hàng đợi. Thuật toán tổng quát:
     * <ol>
     *     <li>Chuẩn hóa khóa idempotent và biến thể sản phẩm để tránh dữ liệu bẩn.</li>
     *     <li>Kiểm tra xem khóa đã tồn tại chưa: nếu là đơn đang hoạt động -> trả về id để người dùng tiếp tục.
     *     Nếu khóa thuộc tài khoản khác hoặc đơn cũ đã hoàn tất -> phát sinh khóa mới.</li>
     *     <li>Tải sản phẩm và biến thể phù hợp từ DB (thông qua {@link ProductDAO}) để xác định tồn kho và giá.</li>
     *     <li>Đối chiếu số lượng credential khả dụng bằng {@link CredentialDAO} nhằm đảm bảo có đủ dữ liệu bàn giao.</li>
     *     <li>Đảm bảo ví người dùng đã tồn tại, hoạt động và đủ số dư bằng {@link WalletsDAO}.</li>
     *     <li>Tạo bản ghi đơn hàng Pending, sau đó publish thông điệp vào hàng đợi nội bộ để worker tiếp tục xử lý.</li>
     * </ol>
     * Hàng đợi tiếp tục luồng tiền bằng {@link queue.memory.AsyncOrderWorker} (trừ tiền, trừ tồn kho, giao credential).
     *
     * @param userId         mã người mua
     * @param productId      mã sản phẩm
     * @param quantity       số lượng đặt mua
     * @param variantCode    mã biến thể (có thể null, dùng để xác định SKU con nếu sản phẩm có nhiều lựa chọn)
     * @param idempotencyKey khóa chống tạo trùng gửi từ client (ví dụ theo session/local storage)
     * @return mã đơn hàng vừa tạo hoặc đơn đang xử lý tương ứng khóa
     */
    public int placeOrderPending(int userId, int productId, int quantity, String variantCode, String idempotencyKey) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0");
        }
        String trimmedKey = Objects.requireNonNullElse(idempotencyKey, "").trim();
        if (trimmedKey.isEmpty()) {
            trimmedKey = UUID.randomUUID().toString();
        }
        String normalizedVariant = ProductVariantUtils.normalizeCode(variantCode);
        // 1. Kiểm tra khóa idempotent đã từng được sử dụng chưa để ngăn người dùng lặp giao dịch.
        Optional<Orders> existing = orderDAO.findByIdemKey(trimmedKey);
        if (existing.isPresent()) {
            Orders order = existing.get();
            if (!Objects.equals(order.getBuyerId(), userId)) {
                throw new IllegalStateException("Khóa idempotency đã được sử dụng bởi tài khoản khác.");
            }
            if (isOrderActive(order.getStatus())) {
                return order.getId();
            }
            // Đơn hàng cũ đã kết thúc vòng đời, cấp một khóa mới để tạo giao dịch mới.
            trimmedKey = UUID.randomUUID().toString();
        }
        // 2. Nạp thông tin sản phẩm kèm tồn kho tại thời điểm hiện tại.
        Products product = productDAO.findAvailableById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không khả dụng hoặc tồn kho không đủ."));
        Integer productInventory = product.getInventoryCount();
        List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
                product.getVariantSchema(), product.getVariantsJson());
        Optional<ProductVariantOption> variantOpt = ProductVariantUtils.findVariant(variants, normalizedVariant);
        ProductVariantOption selectedVariant = variantOpt.orElse(null);
        if (normalizedVariant != null) {
            if (selectedVariant == null) {
                throw new IllegalArgumentException("Biến thể sản phẩm không khả dụng.");
            }
            if (!selectedVariant.isAvailable()) {
                throw new IllegalArgumentException("Biến thể sản phẩm không khả dụng.");
            }
            Integer variantInventory = selectedVariant.getInventoryCount();
            if (variantInventory == null || variantInventory < quantity) {
                throw new IllegalArgumentException("Biến thể sản phẩm không đủ tồn kho.");
            }
            if (productInventory != null && productInventory < quantity) {
                throw new IllegalStateException("Tồn kho sản phẩm không đồng bộ với biến thể, vui lòng liên hệ hỗ trợ.");
            }
        } else if (ProductVariantUtils.hasVariants(product.getVariantSchema())) {
            throw new IllegalArgumentException("Vui lòng chọn biến thể sản phẩm trước khi đặt mua.");
        } else if (productInventory == null || productInventory < quantity) {
            throw new IllegalArgumentException("Sản phẩm không khả dụng hoặc tồn kho không đủ.");
        }
        BigDecimal unitPrice = ProductVariantUtils.resolveUnitPrice(product, variantOpt);
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

        String resolvedVariantCode = selectedVariant == null ? null : selectedVariant.getVariantCode();
        if (resolvedVariantCode != null) {
            resolvedVariantCode = resolvedVariantCode.trim();
            if (resolvedVariantCode.isEmpty()) {
                resolvedVariantCode = null;
            }
        }

        // 3. Đối chiếu và tự động bổ sung credential theo biến thể nhằm đảm bảo khi worker chạy sẽ có dữ liệu bàn giao.
        CredentialDAO.CredentialAvailability credentialAvailability = credentialDAO
                .ensureAvailabilityForOrder(productId, resolvedVariantCode, quantity);
        if (resolvedVariantCode != null) {
            if (credentialAvailability.available() < quantity) {
                throw new IllegalStateException("Biến thể sản phẩm tạm thời hết mã bàn giao, vui lòng thử lại sau.");
            }
        } else if (credentialAvailability.available() < quantity) {
            throw new IllegalStateException("Sản phẩm tạm thời hết mã bàn giao, vui lòng thử lại sau.");
        }

        // Kiểm tra nhanh số dư ví trước khi tạo đơn, việc trừ tiền thực tế vẫn diễn ra trong worker.
        // 4. Kiểm tra ví: nếu ví bị khóa hoặc không đủ tiền thì báo lỗi ngay cho người mua.
        Wallets wallet = walletsDAO.ensureUserWallet(userId);
        if (wallet == null) {
            throw new IllegalStateException("Không thể khởi tạo ví cho tài khoản.");
        }
        if (Boolean.FALSE.equals(wallet.getStatus())) {
            throw new IllegalStateException("Ví không khả dụng, vui lòng liên hệ hỗ trợ.");
        }
        BigDecimal balance = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
        if (balance.compareTo(total) < 0) {
            throw new IllegalStateException("Ví không đủ số dư để thanh toán đơn hàng.");
        }
        // 5. Tạo bản ghi đơn hàng Pending và đẩy vào hàng đợi bất đồng bộ.
        int orderId = orderDAO.createPending(userId, productId, quantity, unitPrice, total, resolvedVariantCode, trimmedKey);
        queueProducer.publish(orderId, trimmedKey, productId, quantity, resolvedVariantCode);
        return orderId;
    }

    /**
     * Lấy danh sách đơn hàng của người mua kèm phân trang và lọc trạng thái.
     * Phương thức này là cầu nối từ Controller -> DAO:
     * <ol>
     *     <li>Chuẩn hóa tham số trạng thái theo enum {@link OrderStatus}.</li>
     *     <li>Tính tổng bản ghi bằng {@link OrderDAO#countByBuyer(int, String)}.</li>
     *     <li>Tính toán trang hiện tại, offset -> gọi {@link OrderDAO#findByBuyerPaged} để lấy danh sách gọn.</li>
     *     <li>Trả về {@link PaginatedResult} chứa dữ liệu và meta để JSP dựng bảng.</li>
     * </ol>
     *
     * @param userId mã người mua
     * @param status trạng thái cần lọc (chuỗi database)
     * @param page   trang hiện tại (>=1)
     * @param size   số bản ghi mỗi trang (>0)
     * @return đối tượng phân trang chứa danh sách đơn rút gọn
     */
    public PaginatedResult<OrderRow> getMyOrders(int userId, String status, int page, int size) {
        String normalizedStatus = normalizeStatus(status);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        long totalItemsLong = orderDAO.countByBuyer(userId, normalizedStatus);
        int totalPages = totalItemsLong == 0 ? 1 : (int) Math.ceil((double) totalItemsLong / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;
        List<OrderRow> rows = orderDAO.findByBuyerPaged(userId, normalizedStatus, safeSize, offset);
        int totalItems = Math.toIntExact(Math.min(totalItemsLong, Integer.MAX_VALUE));
        return new PaginatedResult<>(rows, currentPage, totalPages, safeSize, totalItems);
    }

    /**
     * Lấy chi tiết đơn hàng kèm thông tin bàn giao khi người mua truy cập.
     * <p>Luồng dữ liệu:</p>
     * <ol>
     *     <li>{@link OrderDAO#findByIdForUser} join bảng {@code orders} + {@code products} để dựng {@link OrderDetailView}.</li>
     *     <li>Nếu trạng thái đã Completed thì truy vấn credential plaintext để show cho khách.</li>
     *     <li>Trả về Optional để controller render hoặc trả lỗi 404.</li>
     * </ol>
     *
     * @param orderId mã đơn hàng
     * @param userId  mã người mua sở hữu
     * @return {@link Optional} chứa thông tin chi tiết nếu tìm thấy
     */
    public Optional<OrderDetailView> getDetail(int orderId, int userId) {
        return getDetail(orderId, userId, false);
    }

    /**
     * Lấy chi tiết đơn hàng và tùy chọn tải plaintext credential nếu người mua đã xác nhận mở khóa.
     *
     * @param orderId            mã đơn hàng
     * @param userId             người sở hữu đơn
     * @param includeCredentials {@code true} nếu đã có log mở khóa và cần trả plaintext về cho JSP
     * @return {@link Optional} chứa thông tin chi tiết nếu tìm thấy
     */
    public Optional<OrderDetailView> getDetail(int orderId, int userId, boolean includeCredentials) {
        return orderDAO.findByIdForUser(orderId, userId)
                .map(detail -> {
                    List<String> credentials = List.of();
                    if (includeCredentials && "Completed".equalsIgnoreCase(detail.order().getStatus())) {
                        credentials = credentialDAO.findPlainCredentialsByOrder(orderId);
                    }
                    return new OrderDetailView(detail.order(), detail.product(), credentials);
                });
    }

    /**
     * Kiểm tra người dùng đã từng mở khóa thông tin bàn giao của đơn hay chưa.
     */
    public boolean hasUnlockedCredentials(int orderId, int userId) {
        return credentialDAO.hasViewLog(orderId, userId);
    }

    /**
     * Ghi nhận hành động mở khóa credential và chỉ cho phép hiển thị plaintext khi đơn đã hoàn thành.
     * <p>Luồng xử lý:</p>
     * <ol>
     *     <li>Kiểm tra quyền sở hữu đơn hàng thông qua {@link OrderDAO#findByIdForUser(int, int)}.</li>
     *     <li>Đảm bảo trạng thái đơn là Completed và đã có credential được worker gán.</li>
     *     <li>Insert log vào bảng {@code credential_view_logs} trước khi trả về kết quả.</li>
     * </ol>
     * Nếu bất kỳ bước nào thất bại (ví dụ log không ghi được) phương thức sẽ ném {@link IllegalStateException}
     * để controller thông báo lỗi thay vì hiển thị thông tin nhạy cảm.
     */
    public CredentialUnlockResult unlockCredentials(int orderId, int userId, String viewerIp) {
        OrderDetailView detail = orderDAO.findByIdForUser(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng hoặc không thuộc sở hữu của bạn."));
        if (!"Completed".equalsIgnoreCase(detail.order().getStatus())) {
            throw new IllegalStateException("Đơn hàng chưa hoàn thành, chưa thể mở khóa thông tin bàn giao.");
        }
        List<String> credentials = credentialDAO.findPlainCredentialsByOrder(orderId);
        if (credentials.isEmpty()) {
            throw new IllegalStateException("Đơn hàng chưa có dữ liệu bàn giao để hiển thị.");
        }
        boolean alreadyLogged = credentialDAO.hasViewLog(orderId, userId);
        credentialDAO.logCredentialView(orderId, detail.order().getProductId(), userId, detail.order().getVariantCode(), viewerIp);
        return new CredentialUnlockResult(!alreadyLogged);
    }

    /**
     * Trả về map trạng thái -> nhãn tiếng Việt phục vụ hiển thị.
     * Controller và JSP dùng map này để tránh hard-code chuỗi trên view.
     *
     * @return map không chỉnh sửa được, khóa là mã trạng thái database
     */
    public Map<String, String> getStatusLabels() {
        return STATUS_LABELS.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        e -> e.getKey().toDatabaseValue(), Map.Entry::getValue));
    }

    /**
     * Lấy nhãn trạng thái theo chuỗi trong cơ sở dữ liệu.
     *
     * @param status giá trị trạng thái database
     * @return nhãn tiếng Việt tương ứng hoặc "Không xác định"
     */
    public String getStatusLabel(String status) {
        OrderStatus orderStatus = OrderStatus.fromDatabaseValue(status);
        if (orderStatus == null) {
            return "Không xác định";
        }
        return STATUS_LABELS.getOrDefault(orderStatus, "Không xác định");
    }

    /**
     * Chuẩn hóa trạng thái đầu vào (đầu chữ hoa, phần còn lại chữ thường) và kiểm tra hợp lệ.
     */
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = Character.toUpperCase(status.charAt(0)) + status.substring(1).toLowerCase();
        return ALLOWED_STATUSES.contains(normalized) ? normalized : null;
    }

    /**
     * Kiểm tra trạng thái đơn còn đang trong vòng đời xử lý hay không.
     */
    private boolean isOrderActive(String status) {
        OrderStatus orderStatus = OrderStatus.fromDatabaseValue(status);
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.PROCESSING;
    }

    /**
     * Sinh map nhãn trạng thái cố định.
     */
    private static Map<OrderStatus, String> buildStatusLabels() {
        Map<OrderStatus, String> labels = new EnumMap<>(OrderStatus.class);
        labels.put(OrderStatus.PENDING, "Đang xử lý");
        labels.put(OrderStatus.PROCESSING, "Đang xử lý");
        labels.put(OrderStatus.COMPLETED, "Hoàn thành");
        labels.put(OrderStatus.FAILED, "Thất bại");
        labels.put(OrderStatus.REFUNDED, "Đã hoàn tiền");
        labels.put(OrderStatus.DISPUTED, "Khiếu nại");
        return labels;
    }

    /**
     * Kết quả thao tác mở khóa credential, trả về cờ {@link #firstView()} để hiển thị thông điệp phù hợp.
     */
    public record CredentialUnlockResult(boolean firstView) {
    }
}
