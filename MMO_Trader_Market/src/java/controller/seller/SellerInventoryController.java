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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Lấy shop của seller
        Shops shop = shopDAO.findByOwnerId(userId);
        if (shop == null) {
            request.setAttribute("errorMessage", "Bạn chưa có cửa hàng.");
            forward(request, response, "seller/inventory");
            return;
        }
        
        // Lấy danh sách sản phẩm
        List<Products> products = productDAO.findByShopId(shop.getId());
        
        // Log để debug
        System.out.println("DEBUG - SellerInventory: userId=" + userId + ", shopId=" + shop.getId() + ", products found=" + products.size());
        
        request.setAttribute("shop", shop);
        request.setAttribute("products", products);
        request.setAttribute("pageTitle", "Quản lý kho hàng - " + shop.getName());
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/inventory");
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
        
        response.sendRedirect(request.getContextPath() + "/seller/inventory");
    }
}
