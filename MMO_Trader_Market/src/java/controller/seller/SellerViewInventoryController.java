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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller để xem chi tiết hàng tồn kho của sản phẩm.
 * Hiển thị số lượng tồn kho theo từng biến thể và danh sách credentials (tài khoản/key) trong kho.
 */
@WebServlet(name = "SellerViewInventoryController", urlPatterns = {"/seller/inventory/view"})
public class SellerViewInventoryController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    
    private final ProductDAO productDAO = new ProductDAO();
    private final ShopDAO shopDAO = new ShopDAO();
    private final CredentialDAO credentialDAO = new CredentialDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
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
                session.setAttribute("errorMessage", "Bạn không có quyền xem tồn kho của sản phẩm này.");
                response.sendRedirect(request.getContextPath() + "/seller/inventory");
                return;
            }
            
            // Parse variants từ product
            List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
                product.getVariantSchema(), 
                product.getVariantsJson()
            );
            
            // Đảm bảo variants không null
            if (variants == null) {
                variants = new ArrayList<>();
            }
            
            // Tạo map để lưu thông tin tồn kho theo từng variant
            Map<String, VariantInventoryInfo> variantInventoryMap = new HashMap<>();
            
            // Nếu sản phẩm có variants
            if (variants != null && !variants.isEmpty()) {
                for (ProductVariantOption variant : variants) {
                    String variantCode = variant.getVariantCode();
                    String normalizedCode = ProductVariantUtils.normalizeCode(variantCode);
                    
                    // Lấy số lượng tồn kho từ variant
                    Integer inventoryCount = variant.getInventoryCount();
                    if (inventoryCount == null) {
                        inventoryCount = 0;
                    }
                    
                    // Lấy credentials của variant này (chỉ lấy chưa bán)
                    List<CredentialDAO.CredentialInfo> credentials = credentialDAO.findAllCredentials(
                        productId, 
                        variantCode, 
                        false // chỉ lấy chưa bán
                    );
                    
                    // Lấy số lượng thực tế từ database
                    CredentialDAO.CredentialAvailability availability = credentialDAO.fetchAvailability(
                        productId, 
                        variantCode
                    );
                    
                    variantInventoryMap.put(
                        normalizedCode != null ? normalizedCode : "",
                        new VariantInventoryInfo(
                            variant,
                            inventoryCount,
                            availability.available(),
                            credentials
                        )
                    );
                }
            } else {
                // Sản phẩm không có variants - lấy tồn kho tổng
                Integer inventoryCount = product.getInventoryCount();
                if (inventoryCount == null) {
                    inventoryCount = 0;
                }
                
                // Lấy credentials (không có variant)
                List<CredentialDAO.CredentialInfo> credentials = credentialDAO.findAllCredentials(
                    productId, 
                    null, 
                    false // chỉ lấy chưa bán
                );
                
                // Lấy số lượng thực tế từ database
                CredentialDAO.CredentialAvailability availability = credentialDAO.fetchAvailability(productId);
                
                variantInventoryMap.put(
                    "",
                    new VariantInventoryInfo(
                        null,
                        inventoryCount,
                        availability.available(),
                        credentials
                    )
                );
            }
            
            request.setAttribute("product", product);
            request.setAttribute("variants", variants);
            request.setAttribute("variantInventoryMap", variantInventoryMap);
            request.setAttribute("pageTitle", "Hàng tồn kho - " + product.getName());
            request.setAttribute("bodyClass", "layout");
            request.setAttribute("headerModifier", "layout__header--split");
            forward(request, response, "seller/view-inventory");
        } catch (Exception e) {
            // Log lỗi và hiển thị thông báo lỗi
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi khi tải dữ liệu tồn kho: " + e.getMessage());
            // Đảm bảo các attribute cần thiết luôn được set
            request.setAttribute("variants", new ArrayList<>());
            request.setAttribute("variantInventoryMap", new HashMap<>());
            request.setAttribute("pageTitle", "Lỗi");
            request.setAttribute("bodyClass", "layout");
            request.setAttribute("headerModifier", "layout__header--split");
            forward(request, response, "seller/view-inventory");
        }
    }
    
    /**
     * Class để lưu thông tin tồn kho của một variant.
     */
    public static class VariantInventoryInfo {
        private final ProductVariantOption variant;
        private final Integer configuredInventory; // Tồn kho được cấu hình
        private final Integer actualInventory; // Tồn kho thực tế từ database
        private final List<CredentialDAO.CredentialInfo> credentials;
        
        public VariantInventoryInfo(ProductVariantOption variant, Integer configuredInventory, 
                                   Integer actualInventory, List<CredentialDAO.CredentialInfo> credentials) {
            this.variant = variant;
            this.configuredInventory = configuredInventory;
            this.actualInventory = actualInventory;
            this.credentials = credentials != null ? credentials : new ArrayList<>();
        }
        
        public ProductVariantOption getVariant() {
            return variant;
        }
        
        public Integer getConfiguredInventory() {
            return configuredInventory;
        }
        
        public Integer getActualInventory() {
            return actualInventory;
        }
        
        public List<CredentialDAO.CredentialInfo> getCredentials() {
            return credentials;
        }
    }
}

