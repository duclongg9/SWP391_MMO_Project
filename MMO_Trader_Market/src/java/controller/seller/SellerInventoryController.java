package controller.seller;

import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Products;
import model.Shops;

import java.io.IOException;
import java.util.List;

/**
 * Trang cập nhật kho hàng dành cho người bán.
 */
@WebServlet(name = "SellerInventoryController", urlPatterns = {"/seller/inventory"})
public class SellerInventoryController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    
    private final ProductDAO productDAO = new ProductDAO();
    private final ShopDAO shopDAO = new ShopDAO();
    
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Xử lý flash messages
        if (session != null) {
            Object success = session.getAttribute("sellerInventoryFlashSuccess");
            if (success instanceof String) {
                request.setAttribute("flashSuccess", success);
                session.removeAttribute("sellerInventoryFlashSuccess");
            }
            Object error = session.getAttribute("sellerInventoryFlashError");
            if (error instanceof String) {
                request.setAttribute("flashError", error);
                session.removeAttribute("sellerInventoryFlashError");
            }
            Object successMsg = session.getAttribute("successMessage");
            if (successMsg instanceof String) {
                request.setAttribute("successMessage", successMsg);
                session.removeAttribute("successMessage");
            }
            Object errorMsg = session.getAttribute("errorMessage");
            if (errorMsg instanceof String) {
                request.setAttribute("errorMessage", errorMsg);
                session.removeAttribute("errorMessage");
            }
        }
        
        // Lấy shop của seller
        Shops shop = shopDAO.findByOwnerId(userId);
        if (shop == null) {
            request.setAttribute("errorMessage", "Bạn chưa có cửa hàng.");
            forward(request, response, "seller/inventory");
            return;
        }
        
        // Lấy tham số tìm kiếm và phân trang
        String keyword = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        
        int page = parsePage(request.getParameter("page"));
        int pageSize = parsePageSize(request.getParameter("size"));
        
        // Đếm tổng sản phẩm (có hoặc không có keyword)
        int totalProducts = productDAO.countByShopId(shop.getId(), keyword);
        
        // Tính tổng số trang
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;
        
        // Tính offset
        int offset = (page - 1) * pageSize;
        
        // Lấy danh sách sản phẩm (có phân trang và tìm kiếm)
        List<Products> products = productDAO.findByShopId(shop.getId(), keyword, pageSize, offset);
        
        request.setAttribute("shop", shop);
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("pageTitle", "Quản lý kho hàng - " + shop.getName());
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/inventory");
    }
    
    private int parsePage(String pageStr) {
        if (pageStr == null || pageStr.trim().isEmpty()) {
            return DEFAULT_PAGE;
        }
        try {
            int page = Integer.parseInt(pageStr.trim());
            return page < 1 ? DEFAULT_PAGE : page;
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE;
        }
    }
    
    private int parsePageSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int size = Integer.parseInt(sizeStr.trim());
            return size < 1 ? DEFAULT_PAGE_SIZE : size;
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE_SIZE;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        String action = request.getParameter("action");
        String productIdStr = request.getParameter("productId");
        
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        int productId = Integer.parseInt(productIdStr);
        HttpSession session = request.getSession();
        
        // Lấy tham số page và keyword để giữ lại vị trí hiện tại
        String pageParam = request.getParameter("page");
        String keywordParam = request.getParameter("keyword");
        
        if ("stop".equals(action)) {
            // Ngừng bán - chuyển sang Unlisted
            boolean success = productDAO.updateStatus(productId, "Unlisted");
            if (success) {
                session.setAttribute("successMessage", "Đã ngừng bán sản phẩm");
            } else {
                session.setAttribute("errorMessage", "Không thể ngừng bán sản phẩm");
            }
        } else if ("resume".equals(action)) {
            // Mở bán lại - chuyển sang Available
            boolean success = productDAO.updateStatus(productId, "Available");
            if (success) {
                session.setAttribute("successMessage", "Đã mở bán lại sản phẩm");
            } else {
                session.setAttribute("errorMessage", "Không thể mở bán lại sản phẩm");
            }
        }
        
        // Xây dựng URL redirect với tham số page và keyword
        StringBuilder redirectUrl = new StringBuilder(request.getContextPath() + "/seller/inventory");
        boolean hasParams = false;
        
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            redirectUrl.append("?page=").append(pageParam);
            hasParams = true;
        }
        
        if (keywordParam != null && !keywordParam.trim().isEmpty()) {
            if (hasParams) {
                redirectUrl.append("&keyword=");
            } else {
                redirectUrl.append("?keyword=");
                hasParams = true;
            }
            try {
                redirectUrl.append(java.net.URLEncoder.encode(keywordParam, "UTF-8"));
            } catch (java.io.UnsupportedEncodingException e) {
                redirectUrl.append(keywordParam);
            }
        }
        
        response.sendRedirect(redirectUrl.toString());
    }
}
