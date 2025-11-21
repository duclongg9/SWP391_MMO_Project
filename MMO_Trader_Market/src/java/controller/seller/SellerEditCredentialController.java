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
import java.sql.SQLException;
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
        
        // Lấy sản phẩm
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            session.setAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            String shopIdParam = request.getParameter("shopId");
            String redirectUrl = request.getContextPath() + "/seller/inventory";
            if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
                redirectUrl += "?shopId=" + shopIdParam;
            }
            response.sendRedirect(redirectUrl);
            return;
        }
        
        Products product = productOpt.get();
        
        // Kiểm tra quyền sở hữu: shop của sản phẩm phải thuộc về user
        Optional<Shops> shopOpt;
        try {
            shopOpt = shopDAO.findByIdAndOwner(product.getShopId(), userId);
        } catch (java.sql.SQLException e) {
             throw new ServletException("Lỗi khi kiểm tra quyền sở hữu", e);
        }
        
        if (shopOpt.isEmpty()) {
            session.setAttribute("errorMessage", "Bạn không có quyền sửa credential của sản phẩm này.");
            String shopIdParam = request.getParameter("shopId");
            String redirectUrl = request.getContextPath() + "/seller/inventory";
            if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
                redirectUrl += "?shopId=" + shopIdParam;
            }
            response.sendRedirect(redirectUrl);
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
            String shopIdParam = request.getParameter("shopId");
            String redirectUrl = request.getContextPath() + "/seller/inventory/view?productId=" + productId;
            if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
                redirectUrl += "&shopId=" + shopIdParam;
            }
            response.sendRedirect(redirectUrl);
            return;
        }
        
        // Cập nhật credential (bao gồm variant code)
        boolean success = credentialDAO.updateCredential(credentialId, username.trim(), password.trim(), variantCode);
        
        if (success) {
            session.setAttribute("successMessage", "Đã cập nhật sản phẩm thành công!");
        } else {
            session.setAttribute("errorMessage", "Không thể cập nhật sản phẩm. Có thể credential đã được bán hoặc không tồn tại.");
        }
        
        // Redirect về view inventory với shopId
        String shopIdParam = request.getParameter("shopId");
        String redirectUrl = request.getContextPath() + "/seller/inventory/view?productId=" + productId;
        if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
            redirectUrl += "&shopId=" + shopIdParam;
        } else {
            // Nếu không có shopId từ request, lấy từ product
            redirectUrl += "&shopId=" + product.getShopId();
        }
        response.sendRedirect(redirectUrl);
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

