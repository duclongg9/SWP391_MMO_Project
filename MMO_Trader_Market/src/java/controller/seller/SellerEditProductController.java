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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller chỉnh sửa sản phẩm.
 */
@WebServlet(name = "SellerEditProductController", urlPatterns = {"/seller/products/edit"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 15     // 15MB
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
        
        request.setAttribute("product", product);
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
        String inventoryStr = request.getParameter("inventory");
        
        // Validate
        List<String> errors = new ArrayList<>();
        
        // Xử lý upload ảnh mới (nếu có)
        String primaryImageUrl = existingProduct.getPrimaryImageUrl(); // Giữ ảnh cũ
        try {
            Part filePart = request.getPart("productImage");
            if (filePart != null && filePart.getSize() > 0) {
                String applicationPath = request.getServletContext().getRealPath("");
                // Xóa ảnh cũ nếu có
                if (primaryImageUrl != null && !primaryImageUrl.trim().isEmpty()) {
                    FileUploadUtil.deleteFile(primaryImageUrl, applicationPath);
                }
                // Lưu ảnh mới
                primaryImageUrl = FileUploadUtil.saveFile(filePart, applicationPath);
            }
        } catch (Exception e) {
            errors.add("Lỗi upload ảnh: " + e.getMessage());
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
        
        BigDecimal price = null;
        if (priceStr == null || priceStr.trim().isEmpty()) {
            errors.add("Giá bán không được để trống");
        } else {
            try {
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Giá bán phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                errors.add("Giá bán không hợp lệ");
            }
        }
        
        Integer inventory = 0;
        if (inventoryStr != null && !inventoryStr.trim().isEmpty()) {
            try {
                inventory = Integer.parseInt(inventoryStr);
                if (inventory < 0) {
                    errors.add("Số lượng không được âm");
                }
            } catch (NumberFormatException e) {
                errors.add("Số lượng không hợp lệ");
            }
        }
        
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("product", existingProduct);
            request.setAttribute("shop", shop);
            doGet(request, response);
            return;
        }
        
        // Cập nhật sản phẩm
        existingProduct.setProductType(productType);
        existingProduct.setProductSubtype(productSubtype);
        existingProduct.setName(productName);
        existingProduct.setShortDescription(shortDescription);
        existingProduct.setDescription(description);
        existingProduct.setPrice(price);
        existingProduct.setInventoryCount(inventory);
        existingProduct.setPrimaryImageUrl(primaryImageUrl);
        
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

