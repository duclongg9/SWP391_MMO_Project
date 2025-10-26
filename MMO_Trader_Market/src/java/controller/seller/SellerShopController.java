package controller.seller;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Shops;
import service.SellerService;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller quản lý thông tin shop của seller.
 * 
 * @version 1.0
 * @author AI Assistant
 */
@WebServlet(name = "SellerShopController", urlPatterns = {
    "/seller/shop/create",
    "/seller/shop/edit",
    "/seller/shop/view"
})
public class SellerShopController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int ROLE_SELLER = 2;

    private final SellerService sellerService = new SellerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (!isSellerSession(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        Integer sellerId = (Integer) session.getAttribute("userId");
        if (sellerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String path = request.getServletPath();
        switch (path) {
            case "/seller/shop/create" -> showCreateForm(request, response, sellerId);
            case "/seller/shop/edit" -> showEditForm(request, response, sellerId);
            case "/seller/shop/view" -> showShopInfo(request, response, sellerId);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (!isSellerSession(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        Integer sellerId = (Integer) session.getAttribute("userId");
        if (sellerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String path = request.getServletPath();
        switch (path) {
            case "/seller/shop/create" -> handleCreate(request, response, sellerId);
            case "/seller/shop/edit" -> handleEdit(request, response, sellerId);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Hiển thị form tạo shop.
     */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, int sellerId)
            throws ServletException, IOException {
        
        // Kiểm tra xem đã có shop chưa
        if (sellerService.hasShop(sellerId)) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/view");
            return;
        }

        request.setAttribute("pageTitle", "Tạo Shop Mới");
        forward(request, response, "seller/shop-create");
    }

    /**
     * Xử lý tạo shop mới.
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response, int sellerId)
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String description = request.getParameter("description");

        try {
            int shopId = sellerService.createShop(sellerId, name, description);
            if (shopId > 0) {
                request.getSession().setAttribute("successMessage", 
                    "Tạo shop thành công! Shop của bạn đang chờ admin phê duyệt.");
                response.sendRedirect(request.getContextPath() + "/seller/dashboard");
            } else {
                request.setAttribute("errorMessage", "Không thể tạo shop. Vui lòng thử lại.");
                request.setAttribute("name", name);
                request.setAttribute("description", description);
                showCreateForm(request, response, sellerId);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("name", name);
            request.setAttribute("description", description);
            showCreateForm(request, response, sellerId);
        }
    }

    /**
     * Hiển thị form chỉnh sửa shop.
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, int sellerId)
            throws ServletException, IOException {
        
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        request.setAttribute("shop", shopOpt.get());
        request.setAttribute("pageTitle", "Chỉnh Sửa Shop");
        forward(request, response, "seller/shop-edit");
    }

    /**
     * Xử lý cập nhật shop.
     */
    private void handleEdit(HttpServletRequest request, HttpServletResponse response, int sellerId)
            throws ServletException, IOException {
        
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        Shops shop = shopOpt.get();
        String name = request.getParameter("name");
        String description = request.getParameter("description");

        try {
            boolean success = sellerService.updateShop(shop.getId(), sellerId, name, description);
            if (success) {
                request.getSession().setAttribute("successMessage", "Cập nhật shop thành công!");
                response.sendRedirect(request.getContextPath() + "/seller/shop/view");
            } else {
                request.setAttribute("errorMessage", "Không thể cập nhật shop. Vui lòng thử lại.");
                request.setAttribute("shop", shop);
                showEditForm(request, response, sellerId);
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("shop", shop);
            showEditForm(request, response, sellerId);
        }
    }

    /**
     * Hiển thị thông tin shop.
     */
    private void showShopInfo(HttpServletRequest request, HttpServletResponse response, int sellerId)
            throws ServletException, IOException {
        
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        request.setAttribute("shop", shopOpt.get());
        request.setAttribute("pageTitle", "Thông Tin Shop");
        forward(request, response, "seller/shop-view");
    }

    /**
     * Kiểm tra session có phải của seller không.
     */
    private boolean isSellerSession(HttpSession session) {
        if (session == null) {
            return false;
        }
        Integer role = (Integer) session.getAttribute("userRole");
        return role != null && ROLE_SELLER == role;
    }
}
