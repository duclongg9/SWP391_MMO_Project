package controller.seller;

import dao.order.CredentialDAO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller để sửa credential (tài khoản/key) trong kho.
 */
@WebServlet(name = "SellerEditCredentialController", urlPatterns = {"/seller/products/edit-credential"})
public class SellerEditCredentialController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    
    private final ProductDAO productDAO = new ProductDAO();
    private final ShopDAO shopDAO = new ShopDAO();
    private final CredentialDAO credentialDAO = new CredentialDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        String credentialIdStr = request.getParameter("credentialId");
        String productIdStr = request.getParameter("productId");
        
        if (credentialIdStr == null || credentialIdStr.trim().isEmpty() ||
            productIdStr == null || productIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        int credentialId;
        int productId;
        try {
            credentialId = Integer.parseInt(credentialIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Kiểm tra shop
        Shops shop = shopDAO.findByOwnerId(userId);
        if (shop == null) {
            session.setAttribute("errorMessage", "Bạn chưa có cửa hàng.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory/view?productId=" + productId);
            return;
        }
        
        // Lấy sản phẩm
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            session.setAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        Products product = productOpt.get();
        
        // Kiểm tra sản phẩm có thuộc shop của seller không
        if (!product.getShopId().equals(shop.getId())) {
            session.setAttribute("errorMessage", "Bạn không có quyền sửa credential của sản phẩm này.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        // Lấy dữ liệu từ form
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String variantCode = request.getParameter("variantCode"); // Có thể null
        
        // Validate
        List<String> errors = new ArrayList<>();
        
        if (username == null || username.trim().isEmpty()) {
            errors.add("Tên đăng nhập không được để trống");
        }
        
        if (password == null || password.trim().isEmpty()) {
            errors.add("Mật khẩu không được để trống");
        }
        
        // Validate định dạng email nếu là sản phẩm EMAIL hoặc FACEBOOK
        if (username != null && !username.trim().isEmpty()) {
            String productType = product.getProductType();
            String productSubtype = product.getProductSubtype();
            
            boolean requiresEmailFormat = false;
            if ("EMAIL".equalsIgnoreCase(productType)) {
                requiresEmailFormat = true;
            } else if ("SOCIAL".equalsIgnoreCase(productType) && "FACEBOOK".equalsIgnoreCase(productSubtype)) {
                requiresEmailFormat = true;
            }
            
            if (requiresEmailFormat && !isValidEmail(username.trim())) {
                errors.add("Tên đăng nhập phải là địa chỉ email hợp lệ (ví dụ: example@gmail.com, user@yahoo.com, name@outlook.com, v.v.)");
            }
        }
        
        if (!errors.isEmpty()) {
            session.setAttribute("errorMessage", String.join(", ", errors));
            response.sendRedirect(request.getContextPath() + "/seller/inventory/view?productId=" + productId);
            return;
        }
        
        // Cập nhật credential (bao gồm variant code)
        boolean success = credentialDAO.updateCredential(credentialId, username.trim(), password.trim(), variantCode);
        
        if (success) {
            session.setAttribute("successMessage", "Đã cập nhật sản phẩm thành công!");
        } else {
            session.setAttribute("errorMessage", "Không thể cập nhật sản phẩm. Có thể credential đã được bán hoặc không tồn tại.");
        }
        
        response.sendRedirect(request.getContextPath() + "/seller/inventory/view?productId=" + productId);
    }
    
    /**
     * Kiểm tra định dạng email hợp lệ.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
}

