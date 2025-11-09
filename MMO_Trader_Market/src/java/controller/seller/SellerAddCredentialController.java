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
import model.product.ProductVariantOption;
import service.util.ProductVariantUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller để thêm sản phẩm (credential) vào kho.
 * Mỗi khi thêm một sản phẩm, số lượng tồn kho sẽ tự động tăng lên 1.
 */
@WebServlet(name = "SellerAddCredentialController", urlPatterns = {"/seller/products/add-credential"})
public class SellerAddCredentialController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    
    private final ProductDAO productDAO = new ProductDAO();
    private final ShopDAO shopDAO = new ShopDAO();
    private final CredentialDAO credentialDAO = new CredentialDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        String productIdStr = request.getParameter("productId");
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        int productId;
        try {
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
            request.setAttribute("errorMessage", "Bạn chưa có cửa hàng.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
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
            session.setAttribute("errorMessage", "Bạn không có quyền thêm sản phẩm cho sản phẩm này.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        // Parse variants từ product để hiển thị trong dropdown
        List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
            product.getVariantSchema(), 
            product.getVariantsJson()
        );
        
        request.setAttribute("product", product);
        request.setAttribute("variants", variants);
        request.setAttribute("pageTitle", "Thêm sản phẩm - " + product.getName());
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/add-credential");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        String productIdStr = request.getParameter("productId");
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Kiểm tra shop
        Shops shop = shopDAO.findByOwnerId(userId);
        if (shop == null || !"Active".equals(shop.getStatus())) {
            request.setAttribute("errorMessage", "Cửa hàng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        // Lấy sản phẩm hiện tại
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            session.setAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        Products product = productOpt.get();
        if (!product.getShopId().equals(shop.getId())) {
            session.setAttribute("errorMessage", "Bạn không có quyền thêm sản phẩm cho sản phẩm này.");
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
            
            // Kiểm tra nếu cần validate email format
            boolean requiresEmailFormat = false;
            if ("EMAIL".equalsIgnoreCase(productType)) {
                // Tất cả sản phẩm EMAIL đều cần email format
                requiresEmailFormat = true;
            } else if ("SOCIAL".equalsIgnoreCase(productType) && "FACEBOOK".equalsIgnoreCase(productSubtype)) {
                // Facebook cần email format
                requiresEmailFormat = true;
            }
            
            if (requiresEmailFormat && !isValidEmail(username.trim())) {
                errors.add("Tên đăng nhập phải là địa chỉ email hợp lệ (ví dụ: example@gmail.com, user@yahoo.com, name@outlook.com, v.v.)");
            }
        }
        
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("product", product);
            request.setAttribute("username", username);
            request.setAttribute("password", password);
            request.setAttribute("variantCode", variantCode);
            doGet(request, response);
            return;
        }
        
        // Thêm credential và tăng inventory
        // Sử dụng transaction từ CredentialDAO vì nó đã có sẵn transaction handling
        // Sau đó tăng inventory riêng
        boolean credentialAdded = credentialDAO.addRealCredential(productId, username.trim(), password.trim(), variantCode);
        
        if (credentialAdded) {
            // Tăng inventory_count sau khi thêm credential thành công
            boolean inventoryUpdated = productDAO.incrementInventoryCount(productId);
            
            if (inventoryUpdated) {
                session.setAttribute("successMessage", "Đã thêm sản phẩm thành công! Số lượng tồn kho đã tăng lên 1.");
                response.sendRedirect(request.getContextPath() + "/seller/inventory");
            } else {
                // Nếu không tăng được inventory, credential đã được thêm nhưng inventory không tăng
                // Có thể log lỗi này để xử lý sau
                session.setAttribute("errorMessage", "Đã thêm sản phẩm nhưng không thể cập nhật số lượng tồn kho. Vui lòng liên hệ admin.");
                response.sendRedirect(request.getContextPath() + "/seller/inventory");
            }
        } else {
            session.setAttribute("errorMessage", "Không thể thêm sản phẩm. Vui lòng thử lại.");
            request.setAttribute("product", product);
            request.setAttribute("username", username);
            request.setAttribute("password", password);
            request.setAttribute("variantCode", variantCode);
            doGet(request, response);
        }
    }
    
    /**
     * Kiểm tra định dạng email hợp lệ.
     * 
     * @param email địa chỉ email cần kiểm tra
     * @return true nếu email hợp lệ
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Pattern đơn giản để validate email format
        // Email phải có dạng: text@domain.extension
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
}

