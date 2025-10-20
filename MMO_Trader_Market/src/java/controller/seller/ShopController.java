package controller.seller;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Shops;
import model.Users;
import service.ShopService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho quản lý gian hàng của seller
 */
@WebServlet(name = "ShopController", urlPatterns = {"/seller/shop"})
public class ShopController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ShopController.class.getName());
    
    private final ShopService shopService = new ShopService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        if (currentUser.getRoleId() == null || currentUser.getRoleId() != 2) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ seller mới có thể truy cập trang này");
            return;
        }

        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            showCreateShopForm(request, response, currentUser);
        } else {
            showMyShop(request, response, currentUser);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        if (currentUser.getRoleId() == null || currentUser.getRoleId() != 2) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ seller mới có thể thực hiện thao tác này");
            return;
        }

        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            handleCreateShop(request, response, currentUser);
        } else if ("update".equals(action)) {
            handleUpdateShop(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
        }
    }

    private void showCreateShopForm(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        if (!shopService.canCreateShop(seller)) {
            request.setAttribute("error", "Bạn đã có gian hàng hoặc không đủ điều kiện tạo gian hàng");
            showMyShop(request, response, seller);
            return;
        }

        request.setAttribute("pageTitle", "Tạo gian hàng mới");
        forward(request, response, "seller/create-shop");
    }

    private void showMyShop(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        Shops myShop = shopService.getMyShop(seller);
        
        if (myShop == null) {
            // Seller chưa có shop, chuyển hướng đến form tạo shop
            request.setAttribute("pageTitle", "Tạo gian hàng mới");
            request.setAttribute("canCreateShop", true);
            forward(request, response, "seller/create-shop");
            return;
        }

        request.setAttribute("shop", myShop);
        request.setAttribute("pageTitle", "Quản lý gian hàng: " + myShop.getName());
        
        // Thêm thông tin trạng thái
        String statusMessage = getShopStatusMessage(myShop.getStatus());
        request.setAttribute("statusMessage", statusMessage);
        
        forward(request, response, "seller/my-shop");
    }

    private void handleCreateShop(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        String shopName = request.getParameter("shopName");
        String description = request.getParameter("description");

        try {
            Shops newShop = shopService.createShop(seller, shopName, description);
            
            // Thành công - chuyển hướng với thông báo
            HttpSession session = request.getSession();
            session.setAttribute("successMessage", "Tạo gian hàng thành công! Vui lòng chờ admin duyệt để kích hoạt gian hàng.");
            response.sendRedirect(request.getContextPath() + "/seller/shop");
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Lỗi validation hoặc business logic
            request.setAttribute("error", e.getMessage());
            request.setAttribute("shopName", shopName);
            request.setAttribute("description", description);
            request.setAttribute("pageTitle", "Tạo gian hàng mới");
            forward(request, response, "seller/create-shop");
            
        } catch (RuntimeException e) {
            // Lỗi hệ thống
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo shop cho seller ID: " + seller.getId(), e);
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
            request.setAttribute("shopName", shopName);
            request.setAttribute("description", description);
            request.setAttribute("pageTitle", "Tạo gian hàng mới");
            forward(request, response, "seller/create-shop");
        }
    }

    private void handleUpdateShop(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        String shopName = request.getParameter("shopName");
        String description = request.getParameter("description");

        try {
            boolean updated = shopService.updateMyShop(seller, shopName, description);
            
            if (updated) {
                HttpSession session = request.getSession();
                session.setAttribute("successMessage", "Cập nhật thông tin gian hàng thành công!");
            } else {
                request.setAttribute("error", "Không thể cập nhật thông tin gian hàng");
            }
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật shop cho seller ID: " + seller.getId(), e);
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
        }
        
        // Quay lại trang quản lý shop
        showMyShop(request, response, seller);
    }

    private Users getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Users) session.getAttribute("currentUser");
    }

    private String getShopStatusMessage(String status) {
        if (status == null) {
            return "Trạng thái không xác định";
        }
        
        return switch (status) {
            case "Active" -> "Gian hàng đang hoạt động bình thường";
            case "Pending" -> "Gian hàng đang chờ admin duyệt";
            case "Suspended" -> "Gian hàng bị tạm khóa";
            default -> "Trạng thái: " + status;
        };
    }
}
