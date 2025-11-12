package controller.seller;

import dao.admin.ManageDisputeDAO;
import dao.connect.DBConnect;
import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Disputes;
import model.Products;
import model.Shops;

import java.io.IOException;
import java.sql.Connection;
import java.util.Comparator;
import java.util.List;

/**
 * Màn hình tổng hợp khiếu nại dành cho người bán để theo dõi trạng thái tranh
 * chấp của các đơn thuộc cửa hàng.
 */
@WebServlet(name = "SellerDisputesController", urlPatterns = {"/seller/disputes"})
public class SellerDisputesController extends SellerBaseController {

    private static final long serialVersionUID = 1L;

    private final ShopDAO shopDAO = new ShopDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        Shops shop = shopDAO.findByOwnerId(userId);
        if (shop == null) {
            request.setAttribute("errorMessage", "Bạn chưa có cửa hàng để theo dõi khiếu nại.");
            forward(request, response, "seller/disputes");
            return;
        }

        String status = normalize(request.getParameter("status"));
        String issueType = normalize(request.getParameter("issueType"));
        String query = trimToNull(request.getParameter("q"));
        Integer productId = parseIntOrNull(request.getParameter("productId"));

        int page = parsePositiveInt(request.getParameter("page"), 1);
        int size = parsePositiveInt(request.getParameter("size"), 10);

        List<Disputes> disputes;
        try (Connection connection = DBConnect.getConnection()) {
            ManageDisputeDAO dao = new ManageDisputeDAO(connection);
            disputes = dao.search(query,
                    status == null || "all".equalsIgnoreCase(status) ? null : status,
                    issueType == null || "all".equalsIgnoreCase(issueType) ? null : issueType,
                    null,
                    null,
                    userId,
                    shop.getId(),
                    productId);
        } catch (Exception ex) {
            throw new ServletException("Không thể tải danh sách khiếu nại", ex);
        }

        disputes = disputes.stream()
                .sorted(Comparator.comparing(Disputes::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int total = disputes.size();
        int pages = (int) Math.ceil(total / (double) size);
        if (pages <= 0) {
            pages = 1;
        }
        if (page > pages) {
            page = pages;
        }
        int fromIdx = Math.max(0, (page - 1) * size);
        int toIdx = Math.min(total, fromIdx + size);
        List<Disputes> paged = disputes.subList(fromIdx, toIdx);

        List<Products> products = productDAO.findByShopId(shop.getId(), null, 200, 0);

        request.setAttribute("shop", shop);
        request.setAttribute("disputes", paged);
        request.setAttribute("pg_total", total);
        request.setAttribute("pg_page", page);
        request.setAttribute("pg_size", size);
        request.setAttribute("pg_pages", pages);
        request.setAttribute("pg_isFirst", page <= 1);
        request.setAttribute("pg_isLast", page >= pages);
        request.setAttribute("pg_single", pages <= 1);

        request.setAttribute("status", status == null ? "all" : status);
        request.setAttribute("issueType", issueType == null ? "all" : issueType);
        request.setAttribute("query", query == null ? "" : query);
        request.setAttribute("selectedProductId", productId);
        request.setAttribute("products", products);

        request.setAttribute("pageTitle", "Khiếu nại từ người mua");
        forward(request, response, "seller/disputes");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
