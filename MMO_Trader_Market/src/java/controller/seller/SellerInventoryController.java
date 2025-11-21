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
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
        
        // Kiểm tra userId
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        // Xử lý flash messages
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
        
        // Lấy shopId từ tham số hoặc lấy shop đầu tiên
        String shopIdParam = request.getParameter("shopId");
        Shops shop = null;
        
        if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
            try {
                int shopId = Integer.parseInt(shopIdParam.trim());
                Optional<Shops> shopOpt = shopDAO.findByIdAndOwner(shopId, userId);
                if (shopOpt.isPresent()) {
                    shop = shopOpt.get();
                } else {
                    // shopId được chỉ định nhưng không tìm thấy hoặc không thuộc về owner
                    request.setAttribute("errorMessage", "Không tìm thấy cửa hàng hoặc bạn không có quyền truy cập.");
                    forward(request, response, "seller/inventory");
                    return;
                }
            } catch (NumberFormatException e) {
                // shopId không hợp lệ
                request.setAttribute("errorMessage", "Mã cửa hàng không hợp lệ.");
                forward(request, response, "seller/inventory");
                return;
            } catch (SQLException e) {
                throw new ServletException("Lỗi khi truy vấn cửa hàng", e);
            }
        } else {
            // Nếu không có shopId, lấy shop đầu tiên (tương thích ngược)
            shop = shopDAO.findByOwnerId(userId);
        }
        
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
        // Đảm bảo chỉ lấy sản phẩm của shop hiện tại
        List<Products> products = productDAO.findByShopId(shop.getId(), keyword, pageSize, offset);
        
        request.setAttribute("shop", shop);
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("shopId", shop.getId()); // Thêm shopId để giữ lại khi redirect
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
        
        // Kiểm tra action hợp lệ
        if (action == null || (!"stop".equals(action) && !"resume".equals(action))) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        // Kiểm tra productId
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        // Parse productId với xử lý lỗi
        int productId;
        try {
            productId = Integer.parseInt(productIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Kiểm tra userId
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        // Lấy shopId từ tham số (để redirect sau khi xử lý)
        String shopIdParamPost = request.getParameter("shopId");
        
        // Lấy thông tin sản phẩm để kiểm tra quyền sở hữu
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            session.setAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        Products product = productOpt.get();
        
        // Kiểm tra xem shop của sản phẩm có thuộc về user không
        Optional<Shops> shopOpt;
        try {
            shopOpt = shopDAO.findByIdAndOwner(product.getShopId(), userId);
        } catch (SQLException e) {
            throw new ServletException("Lỗi khi kiểm tra quyền sở hữu", e);
        }
        
        if (shopOpt.isEmpty()) {
            session.setAttribute("errorMessage", "Bạn không có quyền thay đổi trạng thái sản phẩm này.");
            // Nếu shopIdParamPost có, redirect về đó, nếu không về default
            String redirect = request.getContextPath() + "/seller/inventory";
            if (shopIdParamPost != null && !shopIdParamPost.trim().isEmpty()) {
                redirect += "?shopId=" + shopIdParamPost;
            }
            response.sendRedirect(redirect);
            return;
        }
        
        // Thực hiện cập nhật trạng thái
        boolean success = false;
        if ("stop".equals(action)) {
            // Ngừng bán - chuyển sang Unlisted
            success = productDAO.updateStatus(productId, "Unlisted");
            if (success) {
                session.setAttribute("successMessage", "Đã ngừng bán sản phẩm");
            } else {
                session.setAttribute("errorMessage", "Không thể ngừng bán sản phẩm");
            }
        } else if ("resume".equals(action)) {
            // Mở bán lại - chuyển sang Available
            success = productDAO.updateStatus(productId, "Available");
            if (success) {
                session.setAttribute("successMessage", "Đã mở bán lại sản phẩm");
            } else {
                session.setAttribute("errorMessage", "Không thể mở bán lại sản phẩm");
            }
        }
        
        // Lấy tham số page, keyword và shopId để giữ lại vị trí hiện tại
        String pageParam = request.getParameter("page");
        String keywordParam = request.getParameter("keyword");
        // Sử dụng shopId của sản phẩm để redirect về đúng shop
        String shopIdToRedirect = String.valueOf(product.getShopId());
        
        // Xây dựng URL redirect với tham số page, keyword và shopId
        StringBuilder redirectUrl = new StringBuilder(request.getContextPath() + "/seller/inventory");
        boolean hasParams = false;
        
        if (shopIdToRedirect != null && !shopIdToRedirect.trim().isEmpty()) {
            redirectUrl.append("?shopId=").append(shopIdToRedirect);
            hasParams = true;
        }
        
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            if (hasParams) {
                redirectUrl.append("&page=").append(pageParam);
            } else {
                redirectUrl.append("?page=").append(pageParam);
                hasParams = true;
            }
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
