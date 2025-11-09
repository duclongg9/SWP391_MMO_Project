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
import model.product.ProductVariantOption;
import service.util.ProductVariantUtils;
import units.FileUploadUtil;

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
 * Controller chỉnh sửa sản phẩm.
 */
@WebServlet(name = "SellerEditProductController", urlPatterns = {"/seller/products/edit"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB per file
    maxRequestSize = 1024 * 1024 * 50     // 50MB total (cho phép upload nhiều ảnh)
)
public class SellerEditProductController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    
    private final ProductDAO productDAO = new ProductDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        String productIdStr = request.getParameter("id");
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        int productId = Integer.parseInt(productIdStr);
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
            session.setAttribute("errorMessage", "Bạn không có quyền chỉnh sửa sản phẩm này.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
            return;
        }
        
        // Parse variants từ product để hiển thị trong JSP
        List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
            product.getVariantSchema(), 
            product.getVariantsJson()
        );
        
        request.setAttribute("product", product);
        request.setAttribute("variants", variants);
        request.setAttribute("shop", shop);
        request.setAttribute("pageTitle", "Chỉnh sửa sản phẩm - " + product.getName());
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/edit-product");
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
        
        int productId = Integer.parseInt(productIdStr);
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
        
        Products existingProduct = productOpt.get();
        if (!existingProduct.getShopId().equals(shop.getId())) {
            session.setAttribute("errorMessage", "Bạn không có quyền chỉnh sửa sản phẩm này.");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
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
                
                // Parse existing variants từ product
                List<ProductVariantOption> existingVariants = ProductVariantUtils.parseVariants(
                    existingProduct.getVariantSchema(), 
                    existingProduct.getVariantsJson()
                );
                
                // Create map of existing variants by variant_code
                Map<String, ProductVariantOption> existingVariantsMap = new HashMap<>();
                for (ProductVariantOption existingVariant : existingVariants) {
                    existingVariantsMap.put(existingVariant.getVariantCode(), existingVariant);
                }
                
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
                    String variantCode = (String) variantMeta.get("variant_code");
                    List<String> variantImages = new ArrayList<>();
                    
                    // Get existing images for this variant
                    ProductVariantOption existingVariant = existingVariantsMap.get(variantCode);
                    if (existingVariant != null && existingVariant.getImages() != null) {
                        variantImages.addAll(existingVariant.getImages());
                    }
                    
                    // Remove deleted existing images
                    @SuppressWarnings("unchecked")
                    List<String> deletedExistingImages = (List<String>) variantMeta.get("deleted_existing_images");
                    if (deletedExistingImages != null && !deletedExistingImages.isEmpty()) {
                        variantImages.removeAll(deletedExistingImages);
                    }
                    
                    // Get image count for this variant
                    int imageCount = variantImageCounts.getOrDefault(i, 0);
                    
                    // Process new images for this variant
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
        
        if (productSubtype == null || productSubtype.trim().isEmpty()) {
            errors.add("Vui lòng chọn phân loại chi tiết");
        }
        
        // Price sẽ là giá thấp nhất của variants
        BigDecimal price = existingProduct.getPrice();
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
            request.setAttribute("product", existingProduct);
            request.setAttribute("shop", shop);
            doGet(request, response);
            return;
        }
        
        // Cập nhật sản phẩm (giữ nguyên inventory_count, không cho phép sửa ở đây)
        existingProduct.setProductType(productType);
        existingProduct.setProductSubtype(productSubtype);
        existingProduct.setName(productName);
        existingProduct.setShortDescription(shortDescription);
        existingProduct.setDescription(description);
        existingProduct.setPrice(price);
        existingProduct.setPrimaryImageUrl(primaryImageUrl);
        // Không cập nhật inventoryCount ở đây, chỉ tăng khi thêm sản phẩm
        
        // Lưu gallery_json từ tất cả ảnh của tất cả variants
        Gson gson = new Gson();
        String galleryJson;
        if (!galleryImages.isEmpty()) {
            galleryJson = gson.toJson(galleryImages);
        } else {
            galleryJson = "[]"; // JSON array rỗng nếu không có ảnh
        }
        existingProduct.setGalleryJson(galleryJson);
        
        // Lưu variants_json
        if (!variants.isEmpty()) {
            String variantsJson = gson.toJson(variants);
            existingProduct.setVariantsJson(variantsJson);
            existingProduct.setVariantSchema("custom"); // Đánh dấu là có variants
        } else {
            existingProduct.setVariantsJson("[]");
            existingProduct.setVariantSchema("none");
        }
        
        // Debug: Log thông tin cập nhật
        System.out.println("Updating product ID: " + existingProduct.getId());
        System.out.println("Primary image URL: " + primaryImageUrl);
        System.out.println("Gallery JSON: " + galleryJson);
        System.out.println("Gallery size: " + galleryImages.size());
        System.out.println("Variants count: " + variants.size());
        
        boolean success = productDAO.updateProduct(existingProduct);
        
        if (success) {
            session.setAttribute("successMessage", "Đã cập nhật sản phẩm thành công!");
            response.sendRedirect(request.getContextPath() + "/seller/inventory");
        } else {
            request.setAttribute("errorMessage", "Không thể cập nhật sản phẩm. Vui lòng thử lại.");
            request.setAttribute("product", existingProduct);
            request.setAttribute("shop", shop);
            doGet(request, response);
        }
    }
}

