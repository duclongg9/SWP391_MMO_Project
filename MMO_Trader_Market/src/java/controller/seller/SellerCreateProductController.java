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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Trang tạo sản phẩm mới cho người bán.
 */
@WebServlet(name = "SellerCreateProductController", urlPatterns = {"/seller/products/create"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 15     // 15MB
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
        String inventoryStr = request.getParameter("inventory");
        
        // Validate
        List<String> errors = new ArrayList<>();
        
        // Xử lý upload ảnh
        String primaryImageUrl = null;
        try {
            Part filePart = request.getPart("productImage");
            if (filePart != null && filePart.getSize() > 0) {
                String applicationPath = request.getServletContext().getRealPath("");
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
            request.setAttribute("productName", productName);
            request.setAttribute("productType", productType);
            request.setAttribute("productSubtype", productSubtype);
            request.setAttribute("shortDescription", shortDescription);
            request.setAttribute("description", description);
            request.setAttribute("price", priceStr);
            request.setAttribute("inventory", inventoryStr);
            request.setAttribute("primaryImageUrl", primaryImageUrl);
            request.setAttribute("shop", shop);
            request.setAttribute("selectedShopId", shop.getId());
            doGet(request, response);
            return;
        }
        
        // Tạo sản phẩm
        Products product = new Products();
        product.setShopId(shop.getId());
        product.setProductType(productType);
        product.setProductSubtype(productSubtype);
        product.setName(productName);
        product.setShortDescription(shortDescription);
        product.setDescription(description);
        product.setPrice(price);
        product.setInventoryCount(inventory);
        product.setPrimaryImageUrl(primaryImageUrl);
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
