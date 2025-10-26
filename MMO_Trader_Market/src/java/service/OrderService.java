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
 * Business logic for buyer orders: creating pending orders and reading history.
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
    public OrderService() {
        this(new OrderDAO(), new ProductDAO(), new CredentialDAO(), new WalletsDAO(),
                new WalletTransactionDAO(), InMemoryOrderQueue.getInstance());
    }

    public OrderService(OrderDAO orderDAO, ProductDAO productDAO, CredentialDAO credentialDAO,
            OrderQueueProducer queueProducer) {
        this(orderDAO, productDAO, credentialDAO, new WalletsDAO(), new WalletTransactionDAO(), queueProducer);
    }

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

    public int placeOrderPending(int userId, int productId, int quantity) {
        return placeOrderPending(userId, productId, quantity, null, UUID.randomUUID().toString());
    }

    public int placeOrderPending(int userId, int productId, int quantity, String idempotencyKey) {
        return placeOrderPending(userId, productId, quantity, null, idempotencyKey);
    }

    public int placeOrderPending(int userId, int productId, int quantity, String variantCode, String idempotencyKey) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0");
        }
        String trimmedKey = Objects.requireNonNullElse(idempotencyKey, "").trim();
        if (trimmedKey.isEmpty()) {
            trimmedKey = UUID.randomUUID().toString();
        }
        String normalizedVariant = ProductVariantUtils.normalizeCode(variantCode);
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

        CredentialDAO.CredentialAvailability credentialAvailability;
        if (resolvedVariantCode != null) {
            credentialAvailability = credentialDAO.fetchAvailability(productId, resolvedVariantCode);
            if (credentialAvailability.total() > 0 && credentialAvailability.available() < quantity) {
                throw new IllegalStateException("Biến thể sản phẩm tạm thời hết mã bàn giao, vui lòng thử lại sau.");
            }
            if (credentialAvailability.total() == 0) {
                CredentialDAO.CredentialAvailability overall = credentialDAO.fetchAvailability(productId);
                if (overall.total() > 0) {
                    throw new IllegalStateException("Biến thể sản phẩm tạm thời hết mã bàn giao, vui lòng thử lại sau.");
                }
            }
        } else {
            credentialAvailability = credentialDAO.fetchAvailability(productId);
            if (credentialAvailability.total() > 0 && credentialAvailability.available() < quantity) {
                throw new IllegalStateException("Sản phẩm tạm thời hết mã bàn giao, vui lòng thử lại sau.");
            }
        }

        // Kiểm tra nhanh số dư ví trước khi tạo đơn, việc trừ tiền thực tế vẫn diễn ra trong worker.
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
        int orderId = orderDAO.createPending(userId, productId, quantity, unitPrice, total, resolvedVariantCode, trimmedKey);
        queueProducer.publish(orderId, trimmedKey, productId, quantity, resolvedVariantCode);
        return orderId;
    }

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

    public Optional<OrderDetailView> getDetail(int orderId, int userId) {
        return orderDAO.findByIdForUser(orderId, userId)
                .map(detail -> {
                    List<String> credentials = List.of();
                    if ("Completed".equalsIgnoreCase(detail.order().getStatus())) {
                        credentials = credentialDAO.findPlainCredentialsByOrder(orderId);
                    }
                    return new OrderDetailView(detail.order(), detail.product(), credentials);
                });
    }

    public Map<String, String> getStatusLabels() {
        return STATUS_LABELS.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        e -> e.getKey().toDatabaseValue(), Map.Entry::getValue));
    }

    public String getStatusLabel(String status) {
        OrderStatus orderStatus = OrderStatus.fromDatabaseValue(status);
        if (orderStatus == null) {
            return "Không xác định";
        }
        return STATUS_LABELS.getOrDefault(orderStatus, "Không xác định");
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = Character.toUpperCase(status.charAt(0)) + status.substring(1).toLowerCase();
        return ALLOWED_STATUSES.contains(normalized) ? normalized : null;
    }

    private boolean isOrderActive(String status) {
        OrderStatus orderStatus = OrderStatus.fromDatabaseValue(status);
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.PROCESSING;
    }

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
}
