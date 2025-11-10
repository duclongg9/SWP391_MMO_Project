package controller.seller;

import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Products;
import model.Shops;
import units.FileUploadUtil;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Trang tạo sản phẩm mới cho người bán.
 */
@WebServlet(name = "SellerCreateProductController", urlPatterns = {"/seller/products/create"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB per file
    maxRequestSize = 1024 * 1024 * 50     // 50MB total (cho phép upload nhiều ảnh)
)
public class SellerCreateProductController extends SellerBaseController {

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
        prepareLayout(request);

        try {
            Shops shop = resolveShopForOwner(userId, request.getParameter("shopId"));
            if (shop == null) {
                request.setAttribute("errorMessage", "Bạn chưa có cửa hàng hợp lệ. Vui lòng tạo shop trước khi đăng sản phẩm.");
                forward(request, response, "seller/create-product");
                return;
            }
            if (!"Active".equals(shop.getStatus())) {
                request.setAttribute("errorMessage", "Cửa hàng của bạn đang tạm ngừng hoạt động.");
                forward(request, response, "seller/create-product");
                return;
            }

            request.setAttribute("shop", shop);
            request.setAttribute("selectedShopId", shop.getId());
            forward(request, response, "seller/create-product");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        prepareLayout(request);

        Shops shop;
        try {
            shop = resolveShopForOwner(userId, request.getParameter("shopId"));
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if (shop == null || !"Active".equals(shop.getStatus())) {
            request.setAttribute("errorMessage", "Cửa hàng không hợp lệ hoặc đang bị tạm dừng.");
            request.setAttribute("shop", shop);
            forward(request, response, "seller/create-product");
            return;
        }

        // Lấy dữ liệu từ form
        String productName = request.getParameter("productName");
        String productType = request.getParameter("productType");
        String productSubtype = request.getParameter("productSubtype");
        String shortDescription = request.getParameter("shortDescription");
        String description = request.getParameter("description");
        String priceStr = request.getParameter("price");
        String variantsJsonStr = request.getParameter("variantsJson");
        String variantIndicesStr = request.getParameter("variantIndices");
        
        // Validate
        List<String> errors = new ArrayList<>();
        
        // Xử lý variants với ảnh
        String primaryImageUrl = null;
        List<String> galleryImages = new ArrayList<>();
        List<Map<String, Object>> variants = new ArrayList<>();
        
        if (variantsJsonStr != null && !variantsJsonStr.trim().isEmpty()) {
            try {
                String applicationPath = request.getServletContext().getRealPath("");
                Collection<Part> parts = request.getParts();
                
                // Parse variants metadata
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> variantsMetadata = gson.fromJson(variantsJsonStr, listType);
                
                // Parse variant indices để biết variant nào có bao nhiêu ảnh
                Map<Integer, Integer> variantImageCounts = new HashMap<>();
                if (variantIndicesStr != null && !variantIndicesStr.trim().isEmpty()) {
                    String[] indices = variantIndicesStr.split(",");
                    for (String indexStr : indices) {
                        String[] parts2 = indexStr.split(":");
                        if (parts2.length == 2) {
                            int variantIndex = Integer.parseInt(parts2[0]);
                            int imageCount = Integer.parseInt(parts2[1]);
                            variantImageCounts.put(variantIndex, imageCount);
                        }
                    }
                }
                
                // Process variant images
                for (int i = 0; i < variantsMetadata.size(); i++) {
                    Map<String, Object> variantMeta = variantsMetadata.get(i);
                    List<String> variantImages = new ArrayList<>();
                    
                    // Get image count for this variant
                    int imageCount = variantImageCounts.getOrDefault(i, 0);
                    
                    // Process images for this variant
                    for (int j = 0; j < imageCount; j++) {
                        String partName = "variantImages_" + i + "_" + j;
                        for (Part part : parts) {
                            if (partName.equals(part.getName()) && part.getSize() > 0) {
                                String imageUrl = FileUploadUtil.saveFile(part, applicationPath);
                                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                                    variantImages.add(imageUrl);
                                }
                            }
                        }
                    }
                    
                    // Validate: mỗi variant phải có ít nhất 1 ảnh, tối đa 3 ảnh
                    if (variantImages.isEmpty()) {
                        errors.add("Biến thể \"" + variantMeta.get("name") + "\" phải có ít nhất 1 ảnh");
                    } else if (variantImages.size() > 3) {
                        errors.add("Biến thể \"" + variantMeta.get("name") + "\" chỉ được tối đa 3 ảnh");
                    } else {
                        // Add images to variant
                        variantMeta.put("images", variantImages);
                        variantMeta.put("image_url", variantImages.get(0)); // Ảnh đầu tiên làm ảnh chính
                        variants.add(variantMeta);
                        
                        // Add to gallery (ảnh đầu tiên của variant đầu tiên làm primary)
                        if (primaryImageUrl == null && i == 0 && !variantImages.isEmpty()) {
                            primaryImageUrl = variantImages.get(0);
                        }
                        galleryImages.addAll(variantImages);
                    }
                }
                
                if (variants.isEmpty()) {
                    errors.add("Vui lòng thêm ít nhất một biến thể sản phẩm với ảnh");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                errors.add("Lỗi xử lý biến thể: " + e.getMessage());
            }
        } else {
            errors.add("Vui lòng thêm ít nhất một biến thể sản phẩm");
        }
        
        if (productName == null || productName.trim().isEmpty()) {
            errors.add("Tên sản phẩm không được để trống");
        }
        
        if (productType == null || productType.trim().isEmpty()) {
            errors.add("Vui lòng chọn loại sản phẩm");
        }
        
        // Price sẽ là giá thấp nhất của variants
        BigDecimal price = BigDecimal.ZERO;
        if (!variants.isEmpty()) {
            price = new BigDecimal(variants.get(0).get("price").toString());
            for (Map<String, Object> variant : variants) {
                BigDecimal variantPrice = new BigDecimal(variant.get("price").toString());
                if (variantPrice.compareTo(price) < 0) {
                    price = variantPrice;
                }
            }
        }
        
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("productName", productName);
            request.setAttribute("productType", productType);
            request.setAttribute("productSubtype", productSubtype);
            request.setAttribute("shortDescription", shortDescription);
            request.setAttribute("description", description);
            request.setAttribute("price", priceStr);
            request.setAttribute("primaryImageUrl", primaryImageUrl);
            request.setAttribute("shop", shop);
            request.setAttribute("selectedShopId", shop.getId());
            doGet(request, response);
            return;
        }
        
        // Tạo sản phẩm với inventory = 0 (sẽ thêm sau bằng cách thêm sản phẩm)
        Products product = new Products();
        product.setShopId(shop.getId());
        product.setProductType(productType);
        product.setProductSubtype(productSubtype);
        product.setName(productName);
        product.setShortDescription(shortDescription);
        product.setDescription(description);
        product.setPrice(price);
        product.setInventoryCount(0); // Mặc định 0, sẽ tăng khi thêm sản phẩm
        product.setPrimaryImageUrl(primaryImageUrl);
        
        // Lưu gallery_json từ danh sách ảnh (tất cả ảnh từ tất cả variants)
        if (!galleryImages.isEmpty()) {
            Gson gson = new Gson();
            String galleryJson = gson.toJson(galleryImages);
            product.setGalleryJson(galleryJson);
        }
        
        // Lưu variants_json
        if (!variants.isEmpty()) {
            Gson gson = new Gson();
            String variantsJson = gson.toJson(variants);
            product.setVariantsJson(variantsJson);
            product.setVariantSchema("custom"); // Đánh dấu là có variants
        } else {
            product.setVariantsJson("[]");
            product.setVariantSchema("none");
        }
        
        product.setStatus("Available"); // Đăng thẳng lên shop
        
        boolean success = productDAO.createProduct(product);
        
        if (success) {
            session.setAttribute("successMessage", "Đã đăng sản phẩm thành công!");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
        } else {
            request.setAttribute("errorMessage", "Không thể đăng sản phẩm. Vui lòng thử lại.");
            request.setAttribute("shop", shop);
            request.setAttribute("selectedShopId", shop.getId());
            doGet(request, response);
        }
    }

    /**
     * Chuẩn hoá các thuộc tính layout được tái sử dụng giữa GET và POST.
     *
     * @param request HttpServletRequest hiện tại
     */
    private void prepareLayout(HttpServletRequest request) {
        request.setAttribute("pageTitle", "Đăng sản phẩm mới - Quản lý cửa hàng");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
    }

    /**
     * Tìm shop thuộc sở hữu của seller dựa trên tham số truyền vào. Nếu không chỉ định, trả về shop đầu tiên.
     *
     * @param ownerId      mã người dùng sở hữu shop
     * @param shopIdParam  tham số shopId từ request (có thể null)
     * @return đối tượng Shops nếu tồn tại, null nếu không tìm thấy
     * @throws SQLException nếu xảy ra lỗi khi truy vấn cơ sở dữ liệu
     */
    private Shops resolveShopForOwner(Integer ownerId, String shopIdParam) throws SQLException {
        if (ownerId == null) {
            return null;
        }
        if (shopIdParam != null && !shopIdParam.isBlank()) {
            try {
                int shopId = Integer.parseInt(shopIdParam);
                Optional<Shops> shopOpt = shopDAO.findByIdAndOwner(shopId, ownerId);
                if (shopOpt.isPresent()) {
                    return shopOpt.get();
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return shopDAO.findByOwnerId(ownerId);
    }
}
