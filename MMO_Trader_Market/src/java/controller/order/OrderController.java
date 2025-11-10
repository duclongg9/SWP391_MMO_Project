package controller.order;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.DisputeAttachment;
import model.Disputes;
import model.Orders;
import model.Products;
import model.WalletTransactions;
import model.view.OrderDetailView;
import model.view.OrderWalletEvent;
import service.DisputeService;
import service.OrderService;
import units.FileUploadUtil;
import units.IdObfuscator;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 
 * Servlet điều phối toàn bộ luồng mua sản phẩm của người mua từ lúc gửi yêu cầu
 * "Mua ngay" tới khi người dùng truy cập lịch sử đơn và xem dữ liệu bàn
 * giao.
 * 
 * Controller này chịu trách nhiệm:
 * 
 * Chuẩn hóa và xác thực tham số HTTP trước khi ủy quyền cho tầng dịch vụ xử
 * lý nghiệp vụ.
 * Định tuyến tới đúng trang JSP, truyền dữ liệu view model (OrderRow,
 * OrderDetailView)
 * Gắn kết với hàng đợi xử lý bất đồng bộ thông qua
 * {@link service.OrderService#placeOrderPending}
 * 
 *
 * @author longpdhe171902
 */
@MultipartConfig(
        fileSizeThreshold = 1 * 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 30 * 1024 * 1024
)
@WebServlet(name = "OrderController", urlPatterns = {
    "/order/buy-now",
    "/orders",
    "/orders/my",
    "/orders/detail/*",
    "/orders/unlock",
    "/orders/report"
})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int ROLE_SELLER = 2;
    private static final int ROLE_BUYER = 3;
    private static final String DISPUTE_UPLOAD_DIR = "assets/uploads/disputes";
    private static final int MAX_EVIDENCE_FILES = 5;
    /**
     * Dung lượng tối đa cho mỗi file ảnh bằng chứng (đơn vị MB).
     */
    private static final int MAX_EVIDENCE_FILE_SIZE_MB = 5;
    /**
     * Tổng dung lượng tối đa của toàn bộ minh chứng trong một lần gửi (đơn vị MB).
     */
    private static final int MAX_EVIDENCE_TOTAL_SIZE_MB = 30;

    private final OrderService orderService = new OrderService();
    private final DisputeService disputeService = new DisputeService();

    /**
     * Xử lý các yêu cầu POST. Ở thời điểm hiện tại chỉ có một entry point duy
     * nhất là /order/buy-now. Dòng chảy cụ thể:
     * 
     * Đọc {@code servletPath} để xác định hành động.
     * Nếu là "buy-now" thì chuyển cho
     * {@link #handleBuyNow(HttpServletRequest, HttpServletResponse)}.
     * Nếu không khớp, trả về HTTP 405 để thông báo phương thức không được
     * hỗ trợ.
     * 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        // Phân nhánh theo đường dẫn cụ thể để gọi đúng handler nghiệp vụ.
        if ("/order/buy-now".equals(path)) {
            handleBuyNow(request, response);
            return;
        } else if ("/orders/unlock".equals(path)) {
            handleUnlockCredentials(request, response);
            return;
        } else if ("/orders/report".equals(path)) {
            handleReportOrder(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Xử lý các yêu cầu GET cho ba đường dẫn:
     * 
     * /orders: chuyển hướng 302 tới trang lịch sử cá nhân để
     * tái sử dụng logic phân trang.
     * /orders/my: tải danh sách đơn có lọc, gán vào request
     * attribute để JSP dựng bảng.
     * /orders/detail/&lt;token&gt;: hiển thị chi tiết kèm
     * credential nếu đã bàn giao.
     * 
     * Nếu đường dẫn không khớp sẽ phản hồi HTTP 404.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/orders" ->
                // Điều hướng để tái sử dụng trang "Đơn của tôi".
                redirectToMyOrders(request, response);
            case "/orders/my" ->
                // Tải dữ liệu danh sách đơn và forward tới JSP.
                showMyOrders(request, response);
            case "/orders/detail" -> {
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && pathInfo.endsWith("/wallet-events")) {
                    // API JSON cho phép front-end polling tiến độ ví.
                    handleWalletEventsApi(request, response);
                } else {
                    // Trường hợp còn lại: render trang chi tiết đơn hàng.
                    showOrderDetail(request, response);
                }
            }
            default ->
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Chuyển hướng người dùng tới trang danh sách đơn cá nhân.
     */
    private void redirectToMyOrders(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String target = request.getContextPath() + "/orders/my";
        response.sendRedirect(target);
    }

    /**
     * Tiếp nhận yêu cầu mua ngay từ trang chi tiết sản phẩm. Hàm này xử lý toàn
     * bộ phần đầu luồng cho tới khi đơn được đưa vào hàng đợi:
     * 
     * Kiểm tra người dùng đăng nhập và có vai trò buyer/seller để được phép
     * mua.
     * Đọc các tham số {@code productId}, {@code qty}, {@code variantCode}
     * do form gửi lên.
     * Chuẩn hóa khóa idempotent {@code idemKey} (nếu không gửi thì sinh
     * ngẫu nhiên) để chống double-submit.
     * Ủy quyền cho
     * {@link OrderService#placeOrderPending(int, int, int, String, String)}.
     * Nếu thành công, redirect sang trang chi tiết đơn; 
     * nếu lỗi nghiệp vụ
     * -> HTTP 400/409.
     * 
     */
    private void handleBuyNow(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        // Bảo vệ route: chỉ cho phép buyer/seller đăng nhập tiếp tục.
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        int productId = decodeIdentifier(request.getParameter("productId"));
        int quantity = parsePositiveInt(request.getParameter("qty"));
        String variantCode = normalize(request.getParameter("variantCode"));
        // Nếu dữ liệu đầu vào thiếu -> trả lỗi cho client.
        if (userId == null || productId <= 0 || quantity <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String idemKeyParam = Optional.ofNullable(request.getParameter("idemKey"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(UUID.randomUUID().toString());
        try {
            // Ủy quyền xuống OrderService để tạo đơn và đưa vào hàng đợi xử lý.
            int orderId = orderService.placeOrderPending(userId, productId, quantity, variantCode, idemKeyParam);
            String token = IdObfuscator.encode(orderId);
            String redirectUrl = request.getContextPath() + "/orders/detail/" + token + "?processing=1";
            response.sendRedirect(redirectUrl);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            if (!redirectBackWithError(request, response, session, productId, ex.getMessage())) {
                response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
            }
        }
    }

    private boolean redirectBackWithError(HttpServletRequest request, HttpServletResponse response,
                                          HttpSession session, int productId, String message) throws IOException {
        if (session == null || productId <= 0) {
            return false;
        }
        String resolvedMessage = (message == null || message.isBlank())
                ? "Không thể xử lý giao dịch. Vui lòng thử lại sau."
                : message;
        session.setAttribute("purchaseError", resolvedMessage);
        String target = request.getContextPath() + "/product/detail/" + IdObfuscator.encode(productId);
        response.sendRedirect(target);
        return true;
    }

    /**
     * Hiển thị danh sách đơn hàng của người mua kèm phân trang và lọc trạng
     * thái. Tầng controller chịu trách nhiệm thu thập tham số và chuyển dữ liệu
     * xuống JSP:
     * 
     * Lấy trạng thái filter, số trang, kích thước trang từ query
     * string.
     * Gọi {@link OrderService#getMyOrders(int, String, int, int)} để truy
     * vấn DB qua DAO.
     * Đổ danh sách {@code OrderRow} và meta phân trang vào request
     * attribute "items", "total", ...
     * Forward tới view /WEB-INF/views/order/my.jsp để dựng
     * giao diện.
     * 
     */
    private void showMyOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String rawCodeParam = request.getParameter("code");
        String statusParam = normalize(request.getParameter("status"));
        String productParam = normalize(request.getParameter("product"));
        LocalDate today = LocalDate.now();
        LocalDate fromDate = normalizeDate(request.getParameter("fromDate"), today);
        LocalDate toDate = normalizeDate(request.getParameter("toDate"), today);
        // Nếu người dùng nhập khoảng ngày ngược, tự động điều chỉnh để tránh lỗi truy vấn.
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            fromDate = toDate;
        }
        Integer orderIdFilter = extractOrderId(rawCodeParam);
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_PAGE_SIZE);

        // Gọi service lấy danh sách đơn theo filter + phân trang.
        var result = orderService.getMyOrders(userId, statusParam, orderIdFilter, productParam, fromDate, toDate, page, size);
        Map<String, String> statusLabels = orderService.getStatusLabels();

        // Đổ dữ liệu và metadata xuống request để JSP render bảng.
        request.setAttribute("items", result.getItems());
        request.setAttribute("total", result.getTotalItems());
        request.setAttribute("page", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("size", result.getPageSize());
        request.setAttribute("status", statusParam == null ? "" : statusParam);
        request.setAttribute("orderCode", rawCodeParam == null ? "" : rawCodeParam.trim());
        request.setAttribute("productName", productParam == null ? "" : productParam);
        request.setAttribute("fromDate", fromDate == null ? "" : fromDate.toString());
        request.setAttribute("toDate", toDate == null ? "" : toDate.toString());
        request.setAttribute("todayIso", today.toString());
        request.setAttribute("statusLabels", statusLabels);
        request.setAttribute("statusOptions", statusLabels);

        forward(request, response, "order/my");
    }

    /**
     * Hiển thị chi tiết một đơn hàng cụ thể nếu thuộc sở hữu người dùng. Sau
     * khi qua bước kiểm tra quyền truy cập, controller sẽ:
     * 
     * Đọc {@code id} của đơn từ query string và validate.
     * Gọi {@link OrderService#getDetail(int, int)} để load đơn, sản phẩm và
     * credential.
     * Đưa các đối tượng domain vào request attribute cho JSP:
     * {@code order}, {@code product}, {@code credentials}.
     * Tính sẵn nhãn trạng thái tiếng Việt thông qua
     * {@link OrderService#getStatusLabel(String)}.
     * 
     */
    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String token = extractTokenFromPath(request);
        if (token == null) {
            int legacyId = parsePositiveInt(request.getParameter("id"));
            if (legacyId > 0) {
                // Hỗ trợ đường dẫn cũ ?id= bằng cách chuyển sang URL mới dạng token.
                String redirectUrl = buildOrderDetailRedirect(request, legacyId);
                response.sendRedirect(redirectUrl);
                return;
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int orderId;
        try {
            orderId = IdObfuscator.decode(token);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Optional<OrderDetailView> detailOpt = orderService.getDetail(orderId, userId);
        if (detailOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String unlockSuccess = null;
        String unlockError = null;
        List<String> reportErrors = List.of();
        Map<String, String> reportFormValues = Map.of();
        String reportSuccessMessage = null;
        if (session != null) {
            // Lấy flash message sau khi người dùng mở khóa credential.
            unlockSuccess = (String) session.getAttribute("orderUnlockSuccess");
            unlockError = (String) session.getAttribute("orderUnlockError");
            session.removeAttribute("orderUnlockSuccess");
            session.removeAttribute("orderUnlockError");
            @SuppressWarnings("unchecked")
            List<String> storedErrors = (List<String>) session.getAttribute("orderReportErrors");
            if (storedErrors != null) {
                reportErrors = storedErrors;
            }
            @SuppressWarnings("unchecked")
            Map<String, String> storedForm = (Map<String, String>) session.getAttribute("orderReportFormValues");
            if (storedForm != null) {
                reportFormValues = storedForm;
            }
            reportSuccessMessage = (String) session.getAttribute("orderReportSuccess");
            session.removeAttribute("orderReportErrors");
            session.removeAttribute("orderReportFormValues");
            session.removeAttribute("orderReportSuccess");
        }
        boolean unlocked = orderService.hasUnlockedCredentials(orderId, userId);
        boolean includeCredentials = unlocked || unlockSuccess != null;
        // Nếu đã được phép xem credential thì nạp lại detail với flag load credential.
        OrderDetailView detail = includeCredentials
                ? orderService.getDetail(orderId, userId, true).orElse(detailOpt.get())
                : detailOpt.get();
        Orders order = detail.order();
        Products product = detail.product();
        List<String> credentials = detail.credentials();
        Optional<OrderService.EscrowReleaseResult> escrowReleaseResult = orderService.releaseEscrowIfExpired(order);
        Optional<WalletTransactions> paymentTxOpt = orderService.getPaymentTransactionForOrder(order);
        if (paymentTxOpt.isPresent()) {
            WalletTransactions paymentTx = paymentTxOpt.get();
            request.setAttribute("paymentTransaction", paymentTx);
            request.setAttribute("paymentTransactionTypeLabel",
                    orderService.getTransactionTypeLabel(paymentTx.getTransactionTypeEnum()));
            BigDecimal amount = paymentTx.getAmount();
            if (amount != null) {
                request.setAttribute("paymentTransactionAmountAbs", amount.abs());
            }
        }

        request.setAttribute("order", order);
        request.setAttribute("product", product);
        request.setAttribute("credentials", credentials);
        request.setAttribute("statusLabel", orderService.getStatusLabel(order.getStatus()));
        boolean showProcessingModal = "1".equals(request.getParameter("processing"));
        request.setAttribute("showProcessingModal", showProcessingModal);
        request.setAttribute("credentialsUnlocked", includeCredentials);
        request.setAttribute("unlockSuccessMessage", unlockSuccess);
        request.setAttribute("unlockErrorMessage", unlockError);
        request.setAttribute("unlockJustConfirmed", unlockSuccess != null);
        request.setAttribute("orderToken", IdObfuscator.encode(orderId));
        request.setAttribute("orderReportErrors", reportErrors);
        request.setAttribute("orderReportFormValues", reportFormValues);
        request.setAttribute("orderReportSuccess", reportSuccessMessage);
        request.setAttribute("escrowAutoRelease", escrowReleaseResult.orElse(null));

        Optional<Disputes> disputeOpt = disputeService.findByOrderId(order.getId());
        List<DisputeAttachment> disputeAttachments = disputeOpt.map(dispute -> disputeService.getAttachments(dispute.getId()))
                .orElse(List.of());
        request.setAttribute("existingDispute", disputeOpt.orElse(null));
        request.setAttribute("existingDisputeAttachments", disputeAttachments);
        request.setAttribute("reportIssueOptions", disputeService.getIssueTypeLabels());
        boolean canReport = disputeOpt.isEmpty() && orderService.canReportOrder(order);
        request.setAttribute("canReportOrder", canReport);
        request.setAttribute("maxEvidenceFiles", MAX_EVIDENCE_FILES);
        request.setAttribute("maxEvidenceFileSizeMb", MAX_EVIDENCE_FILE_SIZE_MB);
        request.setAttribute("maxEvidenceTotalSizeMb", MAX_EVIDENCE_TOTAL_SIZE_MB);

        forward(request, response, "order/detail");
    }

    /**
     * Phản hồi JSON mô tả các sự kiện ví của đơn hàng để giao diện tải bằng AJAX.
     */
    private void handleWalletEventsApi(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.contains("/wallet-events")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String token = extractTokenFromPath(request);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int orderId;
        try {
            orderId = IdObfuscator.decode(token);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Optional<OrderDetailView> detailOpt = orderService.getDetail(orderId, userId);
        if (detailOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Orders order = detailOpt.get().order();
        orderService.releaseEscrowIfExpired(order);
        Optional<WalletTransactions> paymentTxOpt = orderService.getPaymentTransactionForOrder(order);
        // Xây dựng danh sách sự kiện ví và trả về JSON để front-end polling.
        List<OrderWalletEvent> events = orderService.buildWalletTimeline(order, paymentTxOpt);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(buildWalletEventsPayload(events));
    }

    private String buildWalletEventsPayload(List<OrderWalletEvent> events) {
        StringBuilder builder = new StringBuilder();
        builder.append('{').append("\"events\":[");
        for (int i = 0; i < events.size(); i++) {
            OrderWalletEvent event = events.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{');
            appendJsonField(builder, "sequence", Integer.toString(i + 1), false);
            appendJsonField(builder, "code", escapeJson(event.getCode()), true);
            appendJsonField(builder, "title", escapeJson(event.getTitle()), true);
            appendJsonField(builder, "description", escapeJson(event.getDescription()), true);
            appendJsonField(builder, "occurredAt", formatIso(event.getOccurredAt()), true);
            appendJsonField(builder, "amount", formatDecimal(event.getAmount()), true);
            appendJsonField(builder, "balanceAfter", formatDecimal(event.getBalanceAfter()), true);
            appendJsonField(builder, "reference", escapeJson(event.getReference()), true);
            appendJsonField(builder, "primary", Boolean.toString(event.isPrimary()), false);
            trimTrailingComma(builder);
            builder.append('}');
        }
        builder.append(']');
        builder.append('}');
        return builder.toString();
    }

    private void appendJsonField(StringBuilder builder, String name, String value, boolean quote) {
        if (value == null) {
            return;
        }
        builder.append('"').append(name).append('"').append(':');
        if (quote) {
            builder.append('"').append(value).append('"');
        } else {
            builder.append(value);
        }
        builder.append(',');
    }

    private void trimTrailingComma(StringBuilder builder) {
        int length = builder.length();
        if (length > 0 && builder.charAt(length - 1) == ',') {
            builder.setLength(length - 1);
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder escaped = new StringBuilder(input.length() + 8);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private String formatIso(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return java.time.ZonedDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * Xác nhận mở khóa thông tin bàn giao.
     */
    private void handleUnlockCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String token = normalize(request.getParameter("orderToken"));
        int orderId = decodeIdentifier(token);
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String clientIp = resolveClientIp(request);
        try {
            OrderService.CredentialUnlockResult result = orderService.unlockCredentials(orderId, userId, clientIp);
            String message = result.firstView()
                    ? "Thông tin bàn giao đã được mở khóa và ghi nhận lượt xem đầu tiên."
                    : "Bạn đã mở khóa thông tin bàn giao trước đó, hệ thống cập nhật thời gian truy cập mới.";
            session.setAttribute("orderUnlockSuccess", message);
            String canonicalToken = IdObfuscator.encode(orderId);
            String redirectUrl = request.getContextPath() + "/orders/detail/" + canonicalToken + "?unlocked=1";
            response.sendRedirect(redirectUrl);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalStateException ex) {
            session.setAttribute("orderUnlockError", ex.getMessage());
            String canonicalToken = IdObfuscator.encode(orderId);
            String redirectUrl = request.getContextPath() + "/orders/detail/" + canonicalToken;
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Xử lý form báo cáo đơn hàng và lưu dispute mới.
     */
    private void handleReportOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String token = normalize(request.getParameter("orderToken"));
        int orderId = decodeIdentifier(token);
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Optional<OrderDetailView> detailOpt = orderService.getDetail(orderId, userId);
        if (detailOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Orders order = detailOpt.get().order();
        List<String> errors = new ArrayList<>();
        Map<String, String> formValues = new HashMap<>();

        String issueType = normalize(request.getParameter("issueType"));
        if (!disputeService.isValidIssueType(issueType)) {
            errors.add("Vui lòng chọn loại vấn đề cần báo cáo.");
        }
        String customIssueTitle = normalize(request.getParameter("customIssueTitle"));
        if ("OTHER".equals(issueType) && (customIssueTitle == null || customIssueTitle.isBlank())) {
            errors.add("Vui lòng mô tả ngắn gọn tiêu đề khi chọn loại Khác.");
        }
        String descriptionRaw = request.getParameter("description");
        String description = descriptionRaw == null ? null : descriptionRaw.trim();
        if (description == null || description.length() < 20) {
            errors.add("Nội dung báo cáo cần tối thiểu 20 ký tự để mô tả vấn đề rõ ràng.");
        }

        if (!orderService.canReportOrder(order)) {
            errors.add("Đơn hàng đã hết thời gian escrow hoặc đang được xử lý khiếu nại.");
        }
        Optional<Disputes> existingDispute = disputeService.findByOrderId(orderId);
        if (existingDispute.isPresent()) {
            errors.add("Đơn hàng này đã có báo cáo đang xử lý.");
        }

        List<Part> evidenceParts = new ArrayList<>();
        boolean uploadLimitExceeded = false;
        try {
            for (Part part : request.getParts()) {
                if (part != null && "evidenceImages".equals(part.getName()) && part.getSize() > 0) {
                    evidenceParts.add(part);
                }
            }
        } catch (IllegalStateException ex) {
            uploadLimitExceeded = true;
            errors.add(String.format("Dung lượng upload vượt giới hạn (tối đa %dMB mỗi ảnh, tổng cộng %dMB cho toàn bộ yêu cầu).",
                    MAX_EVIDENCE_FILE_SIZE_MB, MAX_EVIDENCE_TOTAL_SIZE_MB));
        }
        if (evidenceParts.isEmpty() && !uploadLimitExceeded) {
            errors.add("Vui lòng đính kèm ít nhất một ảnh bằng chứng chụp trước khi mở khóa tài khoản.");
        }
        if (evidenceParts.size() > MAX_EVIDENCE_FILES) {
            errors.add("Chỉ được phép tải lên tối đa " + MAX_EVIDENCE_FILES + " ảnh bằng chứng.");
        }

        formValues.put("issueType", issueType == null ? "" : issueType);
        formValues.put("customIssueTitle", customIssueTitle == null ? "" : customIssueTitle);
        formValues.put("description", description == null ? "" : description);

        String redirectUrl = request.getContextPath() + "/orders/detail/" + IdObfuscator.encode(orderId) + "#order-report";
        if (!errors.isEmpty()) {
            session.setAttribute("orderReportErrors", errors);
            session.setAttribute("orderReportFormValues", formValues);
            response.sendRedirect(redirectUrl);
            return;
        }

        String applicationPath = request.getServletContext().getRealPath("");
        List<String> savedFiles = new ArrayList<>();
        List<DisputeAttachment> attachments = new ArrayList<>();
        try {
            for (Part part : evidenceParts) {
                String storedPath = FileUploadUtil.saveFile(part, applicationPath, DISPUTE_UPLOAD_DIR);
                if (storedPath != null) {
                    savedFiles.add(storedPath);
                    attachments.add(new DisputeAttachment(null, storedPath));
                }
            }
        } catch (IOException ex) {
            for (String saved : savedFiles) {
                FileUploadUtil.deleteFile(saved, applicationPath);
            }
            errors.add("Không thể lưu ảnh bằng chứng: " + ex.getMessage());
            session.setAttribute("orderReportErrors", errors);
            session.setAttribute("orderReportFormValues", formValues);
            response.sendRedirect(redirectUrl);
            return;
        }

        try {
            disputeService.reportOrder(order, userId, issueType,
                    "OTHER".equals(issueType) ? customIssueTitle : null, description, attachments);
            session.setAttribute("orderReportSuccess", "Đã gửi báo cáo tới đội ngũ hỗ trợ. Chúng tôi sẽ liên hệ trong thời gian sớm nhất.");
        } catch (IllegalStateException ex) {
            for (String saved : savedFiles) {
                FileUploadUtil.deleteFile(saved, applicationPath);
            }
            errors.add(ex.getMessage());
            session.setAttribute("orderReportErrors", errors);
            session.setAttribute("orderReportFormValues", formValues);
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Kiểm tra quyền truy cập của phiên người dùng (buyer hoặc seller).
     */
    private boolean isBuyerOrSeller(HttpSession session) {
        if (session == null) {
            return false;
        }
        Integer role = (Integer) session.getAttribute("userRole");
        return role != null && (ROLE_BUYER == role || ROLE_SELLER == role);
    }

    /**
     * Chuyển đổi chuỗi sang số nguyên dương, trả về -1 nếu không hợp lệ.
     */
    private int parsePositiveInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Chuyển chuỗi sang số nguyên dương, trả về giá trị mặc định khi không hợp
     * lệ.
     */
    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        int parsed = parsePositiveInt(value);
        return parsed > 0 ? parsed : defaultValue;
    }

    /**
     * Chuẩn hóa chuỗi: cắt khoảng trắng và trả về {@code null} nếu rỗng.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer extractOrderId(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String digits = normalized.startsWith("#") ? normalized.substring(1) : normalized;
        int parsed = parsePositiveInt(digits);
        return parsed > 0 ? parsed : null;
    }

    private LocalDate normalizeDate(String value, LocalDate today) {
        LocalDate parsed = parseDate(value);
        if (parsed == null) {
            return null;
        }
        return parsed.isAfter(today) ? today : parsed;
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String extractTokenFromPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }
        String token = pathInfo.charAt(0) == '/' ? pathInfo.substring(1) : pathInfo;
        int slashIndex = token.indexOf('/');
        if (slashIndex >= 0) {
            token = token.substring(0, slashIndex);
        }
        return token.isBlank() ? null : token;
    }

    private String buildOrderDetailRedirect(HttpServletRequest request, int orderId) {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/orders/detail/")
                .append(IdObfuscator.encode(orderId));
        List<String> queryParts = new ArrayList<>();
        request.getParameterMap().forEach((key, values) -> {
            if ("id".equals(key) || values == null) {
                return;
            }
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                queryParts.add(encodedKey + "=" + encodedValue);
            }
        });
        if (!queryParts.isEmpty()) {
            url.append('?').append(String.join("&", queryParts));
        }
        return url.toString();
    }

    private int decodeIdentifier(String value) {
        if (value == null) {
            return -1;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return -1;
        }
        try {
            return IdObfuscator.decode(normalized);
        } catch (IllegalArgumentException ex) {
            return parsePositiveInt(normalized);
        }
    }

    /**
     * Ưu tiên đọc IP thực tế từ header proxy (X-Forwarded-For) trước khi
     * fallback về địa chỉ kết nối.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex >= 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}
