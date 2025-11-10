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
import model.TransactionType;
import model.product.ProductVariantOption;
import model.view.OrderDetailView;
import model.view.OrderRow;
import model.view.OrderWalletEvent;
import model.view.PurchasePreviewResult;
import model.WalletTransactions;
import model.Wallets;
import queue.OrderQueueProducer;
import queue.memory.InMemoryOrderQueue;
import service.util.ProductVariantUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Duration;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * <p>
 * Dịch vụ nghiệp vụ cho luồng mua hàng của người mua: kiểm tra tồn kho, xác
 * minh số dư ví, phát hành khóa idempotent, khởi tạo đơn chờ xử lý, phát thông
 * điệp vào hàng đợi và cung cấp dữ liệu cho các trang JSP lịch sử/chi tiết.</p>
 * <p>
 * Mỗi phương thức trong lớp này đều mô tả rõ ràng luồng dữ liệu đi/đến các lớp
 * DAO tương ứng (OrderDAO, WalletsDAO, CredentialDAO) giúp người đọc nắm được
 * cách dữ liệu di chuyển từ DB tới view.</p>
 *
 * @author longpdhe171902
 */
public class OrderService {

    // Danh sách trạng thái hợp lệ cho bộ lọc đơn hàng.
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "Pending", "Processing", "Completed", "Failed", "Refunded", "Disputed"
    );
    // Bảng ánh xạ trạng thái sang nhãn tiếng Việt.
    private static final Map<OrderStatus, String> STATUS_LABELS = buildStatusLabels();
    private static final Map<TransactionType, String> TRANSACTION_TYPE_LABELS = buildTransactionTypeLabels();

    // DAO quản lý đơn hàng.
    private final OrderDAO orderDAO;
    // DAO sản phẩm phục vụ kiểm tra tồn kho.
    private final ProductDAO productDAO;
    // DAO mã bàn giao.
    private final CredentialDAO credentialDAO;
    // DAO ví người dùng.
    private final WalletsDAO walletsDAO;
    // DAO giao dịch ví để ghi nhận biến động số dư.
    private final WalletTransactionDAO walletTransactionDAO;
    // Bộ phát sự kiện hàng đợi xử lý đơn hàng.
    private final OrderQueueProducer queueProducer;

    /**
     * Khởi tạo dịch vụ với toàn bộ DAO mặc định, dùng cho môi trường sản xuất.
     */
    public OrderService() {
        this(new OrderDAO(), new ProductDAO(), new CredentialDAO(), new WalletsDAO(),
                new WalletTransactionDAO(), InMemoryOrderQueue.getInstance());
    }

    /**
     * Khởi tạo dịch vụ khi cần truyền sản phẩm và hàng đợi tùy chỉnh (phục vụ
     * test).
     *
     * @param orderDAO DAO quản lý bảng {@code orders}
     * @param productDAO DAO truy vấn sản phẩm
     * @param credentialDAO DAO kiểm tra mã bàn giao
     * @param queueProducer bộ phát sự kiện sang hàng đợi xử lý bất đồng bộ
     */
    public OrderService(OrderDAO orderDAO, ProductDAO productDAO, CredentialDAO credentialDAO,
            OrderQueueProducer queueProducer) {
        this(orderDAO, productDAO, credentialDAO, new WalletsDAO(), new WalletTransactionDAO(), queueProducer);
    }

    /**
     * Khởi tạo dịch vụ với toàn bộ thành phần cần thiết.
     *
     * @param orderDAO DAO quản lý đơn hàng
     * @param productDAO DAO quản lý sản phẩm
     * @param credentialDAO DAO kiểm tra mã bàn giao
     * @param walletsDAO DAO ví người dùng
     * @param walletTransactionDAO DAO giao dịch ví
     * @param queueProducer bộ phát sự kiện hàng đợi nội bộ
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
     * @param userId mã người mua
     * @param productId mã sản phẩm
     * @param quantity số lượng cần mua
     * @return mã đơn hàng được tạo
     */
    public int placeOrderPending(int userId, int productId, int quantity) {
        return placeOrderPending(userId, productId, quantity, null, UUID.randomUUID().toString());
    }

    /**
     * Đặt đơn chờ xử lý với khóa idempotency do phía client truyền lên.
     *
     * @param userId mã người mua
     * @param productId mã sản phẩm
     * @param quantity số lượng cần mua
     * @param idempotencyKey khóa chống tạo trùng
     * @return mã đơn hàng được tạo hoặc đơn có sẵn
     */
    public int placeOrderPending(int userId, int productId, int quantity, String idempotencyKey) {
        return placeOrderPending(userId, productId, quantity, null, idempotencyKey);
    }

    /**
     * Đặt đơn chờ xử lý, kiểm tra tồn kho và số dư ví trước khi đẩy vào hàng
     * đợi. Thuật toán tổng quát:
     * <ol>
     * <li>Chuẩn hóa khóa idempotent và biến thể sản phẩm để tránh dữ liệu
     * bẩn.</li>
     * <li>Kiểm tra xem khóa đã tồn tại chưa: nếu là đơn đang hoạt động -> trả
     * về id để người dùng tiếp tục. Nếu khóa thuộc tài khoản khác hoặc đơn cũ
     * đã hoàn tất -> phát sinh khóa mới.</li>
     * <li>Tải sản phẩm và biến thể phù hợp từ DB (thông qua {@link ProductDAO})
     * để xác định tồn kho và giá.</li>
     * <li>Đối chiếu số lượng credential khả dụng bằng {@link CredentialDAO}
     * nhằm đảm bảo có đủ dữ liệu bàn giao.</li>
     * <li>Đảm bảo ví người dùng đã tồn tại, hoạt động và đủ số dư bằng
     * {@link WalletsDAO}.</li>
     * <li>Tạo bản ghi đơn hàng Pending, sau đó publish thông điệp vào hàng đợi
     * nội bộ để worker tiếp tục xử lý.</li>
     * </ol>
     * Hàng đợi tiếp tục luồng tiền bằng {@link queue.memory.AsyncOrderWorker}
     * (trừ tiền, trừ tồn kho, giao credential).
     *
     * @param userId mã người mua
     * @param productId mã sản phẩm
     * @param quantity số lượng đặt mua
     * @param variantCode mã biến thể (có thể null, dùng để xác định SKU con nếu
     * sản phẩm có nhiều lựa chọn)
     * @param idempotencyKey khóa chống tạo trùng gửi từ client (ví dụ theo
     * session/local storage)
     * @return mã đơn hàng vừa tạo hoặc đơn đang xử lý tương ứng khóa
     */
    public int placeOrderPending(int userId, int productId, int quantity, String variantCode, String idempotencyKey) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0");
        }
        String trimmedKey = Objects.requireNonNullElse(idempotencyKey, "").trim();
        if (trimmedKey.isEmpty()) {
            // Nếu client không truyền khóa, sinh ngẫu nhiên để đảm bảo tránh double-submit.
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
                // Trường hợp người mua lặp lại request nhưng đơn vẫn đang xử lý -> trả về id hiện tại.
                return order.getId();
            }
            // Đơn hàng cũ đã kết thúc vòng đời, cấp một khóa mới để tạo giao dịch mới.
            trimmedKey = UUID.randomUUID().toString();
        }
        // 2. Nạp thông tin sản phẩm kèm tồn kho tại thời điểm hiện tại.
        Products product = productDAO.findAvailableById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không khả dụng hoặc tồn kho không đủ."));
        Optional<Integer> ownerIdOpt = productDAO.findShopOwnerIdByProductId(productId);
        if (ownerIdOpt.filter(ownerId -> ownerId != null && ownerId == userId).isPresent()) {
            throw new IllegalStateException("Bạn không thể mua sản phẩm trên gian hàng của mình.");
        }
        Integer productInventory = product.getInventoryCount();
        List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
                product.getVariantSchema(), product.getVariantsJson());
        // 3. Sau khi đã có dữ liệu sản phẩm, chuẩn bị các thuộc tính phục vụ tính toán đơn giá/tồn kho.
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
        String normalizedVariantForStorage = ProductVariantUtils.normalizeCode(resolvedVariantCode);

        // 3. Kiểm tra tồn kho credential trước khi cho phép đặt mua.
        CredentialDAO.CredentialAvailability credentialAvailability = normalizedVariantForStorage != null
                ? credentialDAO.fetchAvailability(productId, normalizedVariantForStorage)
                : credentialDAO.fetchAvailability(productId);
        if (credentialAvailability.available() < quantity) {
            if (resolvedVariantCode != null) {
                throw new IllegalStateException("Biến thể sản phẩm tạm thời hết mã bàn giao, vui lòng thử lại sau.");
            }
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
        int orderId = orderDAO.createPending(userId, productId, quantity, unitPrice, total,
                normalizedVariantForStorage, trimmedKey);
        queueProducer.publish(orderId, trimmedKey, productId, quantity, normalizedVariantForStorage);
        return orderId;
    }

    /**
     * Lấy danh sách đơn hàng của người mua kèm phân trang và các bộ lọc nâng
     * cao. Phương thức này là cầu nối từ Controller -> DAO:
     * <ol>
     * <li>Chuẩn hóa tham số trạng thái theo enum {@link OrderStatus}.</li>
     * <li>Tính tổng bản ghi bằng
     * {@link OrderDAO#countByBuyer(int, String, Integer, String, LocalDate, LocalDate)}.</li>
     * <li>Tính toán trang hiện tại, offset -> gọi
     * {@link OrderDAO#findByBuyerPaged} với đầy đủ filter để lấy danh sách rút
     * gọn.</li>
     * <li>Trả về {@link PaginatedResult} chứa dữ liệu và meta để JSP dựng
     * bảng.</li>
     * </ol>
     *
     * @param userId mã người mua
     * @param status trạng thái cần lọc (chuỗi database)
     * @param orderId mã đơn cần tìm (có thể {@code null})
     * @param productName tên sản phẩm cần tìm (có thể {@code null})
     * @param fromDate ngày tạo bắt đầu (có thể {@code null})
     * @param toDate ngày tạo kết thúc (có thể {@code null})
     * @param page trang hiện tại (>=1)
     * @param size số bản ghi mỗi trang (>0)
     * @return đối tượng phân trang chứa danh sách đơn rút gọn
     */
    public PaginatedResult<OrderRow> getMyOrders(int userId, String status, Integer orderId, String productName,
            LocalDate fromDate, LocalDate toDate, int page, int size) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedProduct = productName == null ? null : productName.trim();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        // Đếm tổng số bản ghi phù hợp filter để tính toán phân trang.
        long totalItemsLong = orderDAO.countByBuyer(userId, normalizedStatus, orderId, normalizedProduct, fromDate, toDate);
        int totalPages = totalItemsLong == 0 ? 1 : (int) Math.ceil((double) totalItemsLong / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;
        List<OrderRow> rows = orderDAO.findByBuyerPaged(userId, normalizedStatus, orderId, normalizedProduct, fromDate,
                toDate, safeSize, offset);
        int totalItems = Math.toIntExact(Math.min(totalItemsLong, Integer.MAX_VALUE));
        return new PaginatedResult<>(rows, currentPage, totalPages, safeSize, totalItems);
    }

    /**
     * Lấy chi tiết đơn hàng kèm thông tin bàn giao khi người mua truy cập.
     * <p>
     * Luồng dữ liệu:</p>
     * <ol>
     * <li>{@link OrderDAO#findByIdForUser} join bảng {@code orders} +
     * {@code products} để dựng {@link OrderDetailView}.</li>
     * <li>Nếu trạng thái đã Completed thì truy vấn credential plaintext để show
     * cho khách.</li>
     * <li>Trả về Optional để controller render hoặc trả lỗi 404.</li>
     * </ol>
     *
     * @param orderId mã đơn hàng
     * @param userId mã người mua sở hữu
     * @return {@link Optional} chứa thông tin chi tiết nếu tìm thấy
     */
    public Optional<OrderDetailView> getDetail(int orderId, int userId) {
        return getDetail(orderId, userId, false);
    }

    /**
     * Lấy chi tiết đơn hàng và tùy chọn tải plaintext credential nếu người mua
     * đã xác nhận mở khóa.
     *
     * @param orderId mã đơn hàng
     * @param userId người sở hữu đơn
     * @param includeCredentials {@code true} nếu đã có log mở khóa và cần trả
     * plaintext về cho JSP
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
     * Kiểm tra và tự động giải ngân escrow cho đơn hàng nếu đã hết hạn và không có khiếu nại.
     *
     * @param order bản ghi đơn hàng cần kiểm tra
     * @return {@link Optional} chứa kết quả giải ngân nếu phát sinh
     */
    public Optional<EscrowReleaseResult> releaseEscrowIfExpired(Orders order) {
        if (order == null || order.getId() == null || order.getProductId() == null) {
            return Optional.empty();
        }
        Date now = new Date();
        if (!isEscrowReleaseEligible(order, now)) {
            return Optional.empty();
        }
        BigDecimal payoutAmount = order.getTotalAmount();
        if (payoutAmount == null || payoutAmount.signum() <= 0) {
            return Optional.empty();
        }
        Optional<Integer> ownerIdOpt = productDAO.findShopOwnerIdByProductId(order.getProductId());
        if (ownerIdOpt.isEmpty()) {
            return Optional.empty();
        }
        int sellerId = ownerIdOpt.get();
        if (walletsDAO.ensureUserWallet(sellerId) == null) {
            throw new IllegalStateException("Không thể khởi tạo ví cho người bán trước khi giải ngân");
        }
        Timestamp releasedAtTs = new Timestamp(now.getTime());
        Timestamp scheduledRelease = order.getEscrowReleaseAt() == null ? null
                : new Timestamp(order.getEscrowReleaseAt().getTime());
        try (Connection connection = orderDAO.openConnection()) {
            connection.setAutoCommit(false);
            try {
                Wallets lockedWallet = walletsDAO.lockWalletForUpdate(connection, sellerId);
                if (lockedWallet == null) {
                    rollbackQuietly(connection);
                    throw new IllegalStateException("Không thể khóa ví người bán để giải ngân");
                }
                BigDecimal currentBalance = lockedWallet.getBalance() == null
                        ? BigDecimal.ZERO : lockedWallet.getBalance();
                BigDecimal newBalance = currentBalance.add(payoutAmount);
                if (!walletsDAO.updateBalance(connection, lockedWallet.getId(), newBalance)) {
                    rollbackQuietly(connection);
                    throw new IllegalStateException("Không thể cập nhật số dư ví người bán");
                }
                int payoutTxId = walletTransactionDAO.insertTransaction(connection, lockedWallet.getId(), order.getId(),
                        TransactionType.PAYOUT, payoutAmount, currentBalance, newBalance,
                        String.format("Giải ngân đơn #%d sau khi hết thời gian escrow", order.getId()));
                if (!orderDAO.markEscrowReleased(connection, order.getId(), releasedAtTs)) {
                    rollbackQuietly(connection);
                    return Optional.empty();
                }
                orderDAO.insertEscrowReleasedEvent(connection, order.getId(), scheduledRelease, releasedAtTs,
                        "{\"reason\":\"Auto release after escrow timeout\"}");
                connection.commit();
                Date releasedAt = new Date(releasedAtTs.getTime());
                order.setEscrowStatus("Released");
                order.setEscrowRemainingSeconds(0);
                order.setEscrowReleasedAtActual(releasedAt);
                order.setUpdatedAt(releasedAt);
                return Optional.of(new EscrowReleaseResult(order.getId(), payoutAmount, newBalance, payoutTxId, releasedAt));
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể giải ngân escrow cho đơn hàng", ex);
        }
    }

    /**
     * Kiểm tra người dùng đã từng mở khóa thông tin bàn giao của đơn hay chưa.
     */
    public boolean hasUnlockedCredentials(int orderId, int userId) {
        return credentialDAO.hasViewLog(orderId, userId);
    }

    /**
     * Kiểm tra đơn hàng có đủ điều kiện mở form báo cáo hay không.
     */
    public boolean canReportOrder(Orders order) {
        if (order == null) {
            return false;
        }
        if (!"Completed".equalsIgnoreCase(order.getStatus())) {
            return false;
        }
        String escrowStatus = order.getEscrowStatus();
        if (escrowStatus == null || !"Scheduled".equalsIgnoreCase(escrowStatus)) {
            return false;
        }
        Date now = new Date();
        Date releaseAt = order.getEscrowReleaseAt();
        if (releaseAt != null) {
            return releaseAt.after(now);
        }
        Integer remainingSeconds = order.getEscrowRemainingSeconds();
        if (remainingSeconds != null && remainingSeconds > 0) {
            return true;
        }
        Integer holdSeconds = order.getEscrowHoldSeconds();
        return holdSeconds != null && holdSeconds > 0;
    }

    /**
     * Ghi nhận hành động mở khóa credential và chỉ cho phép hiển thị plaintext
     * khi đơn đã hoàn thành.
     * <p>
     * Luồng xử lý:</p>
     * <ol>
     * <li>Kiểm tra quyền sở hữu đơn hàng thông qua
     * {@link OrderDAO#findByIdForUser(int, int)}.</li>
     * <li>Đảm bảo trạng thái đơn là Completed và đã có credential được worker
     * gán.</li>
     * <li>Insert log vào bảng {@code credential_view_logs} trước khi trả về kết
     * quả.</li>
     * </ol>
     * Nếu bất kỳ bước nào thất bại (ví dụ log không ghi được) phương thức sẽ
     * ném {@link IllegalStateException} để controller thông báo lỗi thay vì
     * hiển thị thông tin nhạy cảm.
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
     * Trả về map trạng thái -> nhãn tiếng Việt phục vụ hiển thị. Controller và
     * JSP dùng map này để tránh hard-code chuỗi trên view.
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
     * Lấy thông tin giao dịch thanh toán gắn với đơn hàng (nếu có).
     *
     * @param order bản ghi đơn hàng cần tra cứu
     * @return {@link Optional} chứa giao dịch nếu tìm thấy và thuộc về người mua
     */
    public Optional<WalletTransactions> getPaymentTransactionForOrder(Orders order) {
        if (order == null) {
            return Optional.empty();
        }
        
        Integer buyerId = order.getBuyerId();
        if (buyerId == null) {
            return Optional.empty();
        }
        Integer txId = order.getPaymentTransactionId();
        if (txId != null) {
            return walletTransactionDAO.findByIdForUser(txId, buyerId);
        }
        Integer orderId = order.getId();
        if (orderId == null) {
            return Optional.empty();
        }
        Optional<WalletTransactions> fallback = walletTransactionDAO.findPurchaseByOrderForUser(orderId, buyerId);
        if (fallback.isPresent()) {
            WalletTransactions transaction = fallback.get();
            Integer resolvedId = transaction.getId();
            if (resolvedId != null && orderDAO.assignPaymentTransaction(orderId, resolvedId)) {
                order.setPaymentTransactionId(resolvedId);
            }
        }
        return fallback;
    }

    /**
     * Xây dựng timeline mô tả các bước thao tác ví cho đơn hàng.
     *
     * @param order bản ghi đơn cần hiển thị
     * @param paymentTxOpt giao dịch ví thực tế (nếu đã ghi nhận)
     * @return danh sách sự kiện theo thứ tự thời gian
     */
    public List<OrderWalletEvent> buildWalletTimeline(Orders order, Optional<WalletTransactions> paymentTxOpt) {
        if (order == null) {
            return Collections.emptyList();
        }
        List<OrderWalletEvent> events = new ArrayList<>();
        Date createdAt = copyDate(order.getCreatedAt());
        Integer orderId = order.getId();
        // Bỏ qua sự kiện hàng đợi để giao diện không hiển thị bước này nhưng vẫn giữ nguyên luồng API.
        Date walletStepTime = copyDate(order.getUpdatedAt());
        if (walletStepTime == null) {
            walletStepTime = createdAt;
        }
        StringBuilder walletDesc = new StringBuilder("Worker khóa ví người mua, kiểm tra trạng thái hoạt động và số dư trước khi trừ tiền.");
        BigDecimal totalAmount = order.getTotalAmount();
        if (totalAmount != null) {
            walletDesc.append(' ').append("Tổng tiền cần thanh toán: ")
                    .append(formatCurrency(totalAmount)).append(" đ.");
        }
        events.add(new OrderWalletEvent(
                "WALLET_VALIDATED",
                "Kiểm tra số dư ví",
                walletDesc.toString(),
                walletStepTime,
                totalAmount,
                null,
                buildSyntheticReference("V", orderId, 1),
                false));

        if (paymentTxOpt.isPresent()) {
            WalletTransactions tx = paymentTxOpt.get();
            Date transactionTime = copyDate(tx.getCreatedAt());
            if (transactionTime == null) {
                transactionTime = walletStepTime;
            }
            TransactionType type = tx.getTransactionTypeEnum();
            String typeLabel = getTransactionTypeLabel(type);
            String normalizedTypeLabel = typeLabel == null
                    ? "thanh toán"
                    : typeLabel.toLowerCase(Locale.ROOT);
            String desc = "Ghi nhận giao dịch " + normalizedTypeLabel + " và cập nhật số dư ví.";
            events.add(new OrderWalletEvent(
                    "PAYMENT_CAPTURED",
                    "Trừ tiền ví",
                    desc,
                    transactionTime,
                    tx.getAmount(),
                    tx.getBalanceAfter(),
                    tx.getId() == null ? null : "#" + tx.getId(),
                    true));
        } else {
            events.add(new OrderWalletEvent(
                    "PAYMENT_PENDING",
                    "Chờ ghi nhận giao dịch",
                    "Worker đang xử lý các bước trừ tiền và sẽ cập nhật mã giao dịch ngay khi hoàn tất.",
                    walletStepTime,
                    null,
                    null,
                    buildSyntheticReference("P", orderId, events.size() + 1),
                    true));
        }

        String status = order.getStatus();
        if (status != null && !status.isBlank()) {
            Date statusTime = copyDate(order.getUpdatedAt());
            String statusLabel = getStatusLabel(status);
            String desc = "Trạng thái đơn hàng hiện tại: " + statusLabel + ".";
            events.add(new OrderWalletEvent(
                    "ORDER_STATUS",
                    "Cập nhật trạng thái đơn",
                    desc,
                    statusTime,
                    null,
                    null,
                    buildSyntheticReference("S", orderId, events.size() + 1),
                    false));
        }

        String escrowStatus = order.getEscrowStatus();
        if ("Scheduled".equalsIgnoreCase(escrowStatus)) {
            Date scheduledAt = copyDate(order.getUpdatedAt());
            if (scheduledAt == null) {
                scheduledAt = copyDate(order.getCreatedAt());
            }
            Date releaseAt = copyDate(order.getEscrowReleaseAt());
            String releaseLabel = releaseAt == null ? "theo lịch cấu hình" : formatDateTime(releaseAt);
            String holdDescription = describeEscrowHold(order.getEscrowHoldSeconds());
            String desc = String.format(
                    "Hệ thống đang giữ tiền escrow %s và sẽ tự động giải ngân vào %s nếu không phát sinh khiếu nại.",
                    holdDescription,
                    releaseLabel);
            events.add(new OrderWalletEvent(
                    "ESCROW_SCHEDULED",
                    "Giữ tiền escrow",
                    desc,
                    scheduledAt,
                    null,
                    null,
                    buildSyntheticReference("ES", orderId, events.size() + 1),
                    false));
        } else if ("Released".equalsIgnoreCase(escrowStatus)) {
            Date payoutTime = copyDate(order.getEscrowReleasedAtActual());
            if (payoutTime == null) {
                payoutTime = copyDate(order.getEscrowReleaseAt());
            }
            String desc = "Hệ thống đã giải ngân tiền cho người bán sau khi hết thời gian escrow.";
            events.add(new OrderWalletEvent(
                    "ESCROW_RELEASED",
                    "Giải ngân cho người bán",
                    desc,
                    payoutTime,
                    order.getTotalAmount(),
                    null,
                    buildSyntheticReference("E", orderId, events.size() + 1),
                    false));
        }

        return events;
    }

    /**
     * Kiểm tra nhanh điều kiện mua hàng trước khi gửi yêu cầu tạo đơn.
     *
     * @param userId mã người mua (có thể {@code null} nếu chưa đăng nhập)
     * @param productId mã sản phẩm cần mua
     * @param quantity số lượng đặt mua
     * @param variantCode mã biến thể (có thể {@code null})
     * @return kết quả đánh giá tổng hợp
     */
    public PurchasePreviewResult previewPurchase(Integer userId, int productId, int quantity, String variantCode) {
        List<String> blockers = new ArrayList<>();
        if (quantity <= 0) {
            blockers.add("Số lượng phải lớn hơn 0.");
            return new PurchasePreviewResult(false, false, false, false, false, false, false, false, false,
                    0, 0, null, null, null, List.copyOf(blockers));
        }

        Optional<Products> productOpt = productDAO.findAvailableById(productId);
        if (productOpt.isEmpty()) {
            blockers.add("Sản phẩm không khả dụng hoặc đã bị ẩn.");
            return new PurchasePreviewResult(false, false, false, false, false, false, false, false, false,
                    0, 0, null, null, null, List.copyOf(blockers));
        }

        Products product = productOpt.get();
        String normalizedVariant = ProductVariantUtils.normalizeCode(variantCode);
        List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(product.getVariantSchema(),
                product.getVariantsJson());
        Optional<ProductVariantOption> variantOpt = ProductVariantUtils.findVariant(variants, normalizedVariant);
        ProductVariantOption selectedVariant = variantOpt.orElse(null);

        boolean variantRequired = ProductVariantUtils.hasVariants(product.getVariantSchema());
        boolean variantValid = !variantRequired;
        if (normalizedVariant != null) {
            if (selectedVariant == null || !selectedVariant.isAvailable()) {
                blockers.add("Biến thể sản phẩm không khả dụng.");
            } else {
                variantValid = true;
            }
        } else if (variantRequired) {
            blockers.add("Vui lòng chọn biến thể sản phẩm.");
        } else {
            variantValid = true;
        }

        int availableInventory;
        if (selectedVariant != null && selectedVariant.getInventoryCount() != null) {
            availableInventory = Math.max(selectedVariant.getInventoryCount(), 0);
        } else if (product.getInventoryCount() != null) {
            availableInventory = Math.max(product.getInventoryCount(), 0);
        } else {
            availableInventory = 0;
        }
        boolean hasInventory = availableInventory >= quantity;
        if (!hasInventory && variantValid) {
            blockers.add("Tồn kho hiện tại không đủ đáp ứng số lượng yêu cầu.");
        }

        CredentialDAO.CredentialAvailability credentialAvailability = normalizedVariant == null
                ? credentialDAO.fetchAvailability(productId)
                : credentialDAO.fetchAvailability(productId, normalizedVariant);
        int availableCredentials = Math.max(credentialAvailability.available(), 0);
        boolean hasCredentials = availableCredentials >= quantity;
        if (!hasCredentials && variantValid) {
            blockers.add("Kho mã bàn giao chưa sẵn sàng đủ số lượng.");
        }

        BigDecimal unitPrice = ProductVariantUtils.resolveUnitPrice(product, variantOpt);
        BigDecimal totalPrice = unitPrice == null ? null : unitPrice.multiply(BigDecimal.valueOf(quantity));

        boolean walletExists = false;
        boolean walletActive = false;
        boolean walletHasBalance = false;
        BigDecimal walletBalance = null;
        if (userId == null) {
            blockers.add("Vui lòng đăng nhập để kiểm tra số dư ví.");
        } else if (userId > 0) {
            Wallets wallet = walletsDAO.ensureUserWallet(userId);
            if (wallet != null) {
                walletExists = true;
                walletActive = !Boolean.FALSE.equals(wallet.getStatus());
                walletBalance = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
                if (!walletActive) {
                    blockers.add("Ví của bạn đang bị khóa, vui lòng liên hệ hỗ trợ.");
                } else if (totalPrice != null && walletBalance.compareTo(totalPrice) < 0) {
                    blockers.add("Số dư ví không đủ để thanh toán đơn hàng.");
                } else if (totalPrice != null) {
                    walletHasBalance = true;
                }
            } else {
                blockers.add("Không thể truy vấn ví của tài khoản.");
            }
        }

        boolean ok = variantValid && hasInventory && hasCredentials;
        boolean canPurchase = ok && walletHasBalance;
        return new PurchasePreviewResult(
                ok,
                canPurchase,
                true,
                variantValid,
                hasInventory,
                hasCredentials,
                walletExists,
                walletActive,
                walletHasBalance,
                availableInventory,
                availableCredentials,
                unitPrice,
                totalPrice,
                walletBalance,
                List.copyOf(blockers));
    }

    /**
     * Trả về nhãn tiếng Việt cho loại giao dịch ví.
     */
    public String getTransactionTypeLabel(TransactionType type) {
        if (type == null) {
            return "Không xác định";
        }
        return TRANSACTION_TYPE_LABELS.getOrDefault(type, type.getDbValue());
    }

    /**
     * Chuẩn hóa trạng thái đầu vào (đầu chữ hoa, phần còn lại chữ thường) và
     * kiểm tra hợp lệ.
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

    private static Date copyDate(Date input) {
        return input == null ? null : new Date(input.getTime());
    }

    /**
     * Định dạng thời gian theo mẫu {@code dd/MM/yyyy HH:mm} phục vụ hiển thị timeline.
     */
    private static String formatDateTime(Date date) {
        if (date == null) {
            return "theo lịch cấu hình";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }

    /**
     * Chuyển đổi tổng thời gian giữ tiền escrow thành câu tiếng Việt dễ đọc.
     */
    private static String describeEscrowHold(Integer holdSeconds) {
        if (holdSeconds == null || holdSeconds <= 0) {
            return "theo cấu hình hiện hành";
        }
        Duration duration = Duration.ofSeconds(holdSeconds);
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + " ngày");
        }
        if (hours > 0) {
            parts.add(hours + " giờ");
        }
        if (minutes > 0) {
            parts.add(minutes + " phút");
        }
        if (parts.isEmpty()) {
            parts.add("dưới 1 phút");
        }
        return "trong " + String.join(" ", parts);
    }

    private static String buildSyntheticReference(String prefix, Integer orderId, int step) {
        if (prefix == null || orderId == null) {
            return null;
        }
        int normalized = Math.max(orderId, 1);
        String base = Integer.toString(normalized, 36).toUpperCase();
        return prefix + '-' + base + '-' + step;
    }

    private static String formatCurrency(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    private static Map<TransactionType, String> buildTransactionTypeLabels() {
        Map<TransactionType, String> labels = new EnumMap<>(TransactionType.class);
        labels.put(TransactionType.PURCHASE, "Thanh toán đơn hàng");
        labels.put(TransactionType.DEPOSIT, "Nạp tiền vào ví");
        labels.put(TransactionType.WITHDRAWAL, "Rút tiền");
        labels.put(TransactionType.REFUND, "Hoàn tiền");
        labels.put(TransactionType.FEE, "Phí giao dịch");
        labels.put(TransactionType.PAYOUT, "Chi trả");
        return labels;
    }

    /**
     * Kết quả thao tác mở khóa credential, trả về cờ {@link #firstView()} để
     * hiển thị thông điệp phù hợp.
     */
    public record CredentialUnlockResult(boolean firstView) {

    }

    /**
     * Thông tin trả về sau khi giải ngân escrow thành công.
     *
     * @param orderId mã đơn hàng
     * @param amount số tiền giải ngân
     * @param sellerBalanceAfter số dư ví người bán sau khi cộng tiền
     * @param walletTransactionId mã giao dịch ví được ghi nhận
     * @param releasedAt thời điểm giải ngân thực tế
     */
    public record EscrowReleaseResult(int orderId, BigDecimal amount, BigDecimal sellerBalanceAfter,
            int walletTransactionId, Date releasedAt) {

    }

    private boolean isEscrowReleaseEligible(Orders order, Date now) {
        if (!"Completed".equalsIgnoreCase(order.getStatus())) {
            return false;
        }
        if (!"Scheduled".equalsIgnoreCase(order.getEscrowStatus())) {
            return false;
        }
        Date releaseAt = order.getEscrowReleaseAt();
        if (releaseAt != null) {
            return !releaseAt.after(now);
        }
        Integer remainingSeconds = order.getEscrowRemainingSeconds();
        return remainingSeconds != null && remainingSeconds <= 0;
    }

    /**
     * Thực hiện rollback transaction và bỏ qua lỗi phát sinh trong quá trình rollback.
     */
    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ex) {
            // Bỏ qua lỗi rollback để không che khuất ngoại lệ chính.
        }
    }
}
