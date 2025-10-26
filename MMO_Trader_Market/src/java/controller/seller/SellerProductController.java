package controller.seller;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Products;
import model.Shops;
import service.SellerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller quản lý sản phẩm của seller.
 * 
 * @version 1.0
 * @author AI Assistant
 */
@WebServlet(name = "SellerProductController", urlPatterns = {
    "/seller/products",
    "/seller/products/create",
    "/seller/products/edit",
    "/seller/products/delete",
    "/seller/products/status"
})
public class SellerProductController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int ROLE_SELLER = 2;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SellerService sellerService = new SellerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (!isSellerSession(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        Integer sellerId = (Integer) session.getAttribute("userId");
        if (sellerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Kiểm tra shop
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        int shopId = shopOpt.get().getId();
        String path = request.getServletPath();

        switch (path) {
            case "/seller/products" -> listProducts(request, response, shopId);
            case "/seller/products/create" -> showCreateForm(request, response, shopId);
            case "/seller/products/edit" -> showEditForm(request, response, shopId);
            case "/seller/products/delete" -> handleDelete(request, response, shopId);
            case "/seller/products/status" -> handleStatusChange(request, response, shopId);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (!isSellerSession(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        Integer sellerId = (Integer) session.getAttribute("userId");
        if (sellerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Kiểm tra shop
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        int shopId = shopOpt.get().getId();
        String path = request.getServletPath();

        switch (path) {
            case "/seller/products/create" -> handleCreate(request, response, shopId);
            case "/seller/products/edit" -> handleEdit(request, response, shopId);
            case "/seller/products/delete" -> handleDelete(request, response, shopId);
            case "/seller/products/status" -> handleStatusChange(request, response, shopId);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Hiển thị danh sách sản phẩm.
     */
    private void listProducts(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws ServletException, IOException {
        
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_PAGE_SIZE);
        String keyword = request.getParameter("keyword");

        List<Products> products;
        long totalProducts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = sellerService.searchProducts(shopId, keyword, page, size);
            totalProducts = sellerService.countProducts(shopId); // Simplified
        } else {
            products = sellerService.getProducts(shopId, page, size);
            totalProducts = sellerService.countProducts(shopId);
        }

        int totalPages = (int) Math.ceil((double) totalProducts / size);

        request.setAttribute("products", products);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("pageTitle", "Quản Lý Sản Phẩm");

        forward(request, response, "seller/products");
    }

    /**
     * Hiển thị form tạo sản phẩm.
     */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws ServletException, IOException {
        
        request.setAttribute("shopId", shopId);
        request.setAttribute("pageTitle", "Thêm Sản Phẩm Mới");
        forward(request, response, "seller/product-create");
    }

    /**
     * Xử lý tạo sản phẩm.
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws ServletException, IOException {
        
        Products product = buildProductFromRequest(request);
        try {
            int productId = sellerService.createProduct(product, shopId);

            if (productId > 0) {
                request.getSession().setAttribute("successMessage", "Thêm sản phẩm thành công!");
                response.sendRedirect(request.getContextPath() + "/seller/products");
            } else {
                request.setAttribute("errorMessage", "Không thể thêm sản phẩm. Vui lòng thử lại. (Product ID returned: " + productId + ")");
                request.setAttribute("product", product); // Giữ lại dữ liệu đã nhập
                showCreateForm(request, response, shopId);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            request.setAttribute("errorMessage", "Lỗi: " + e.getMessage());
            request.setAttribute("product", product); // Giữ lại dữ liệu đã nhập
            showCreateForm(request, response, shopId);
        } catch (Exception e) {
            // Log unexpected errors
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi không mong muốn: " + e.getMessage());
            request.setAttribute("product", product);
            showCreateForm(request, response, shopId);
        }
    }

    /**
     * Hiển thị form chỉnh sửa sản phẩm.
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws ServletException, IOException {
        
        int productId = parsePositiveInt(request.getParameter("id"));
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Optional<Products> productOpt = sellerService.getProduct(productId, shopId);
        if (productOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("product", productOpt.get());
        request.setAttribute("shopId", shopId);
        request.setAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
        forward(request, response, "seller/product-edit");
    }

    /**
     * Xử lý cập nhật sản phẩm.
     */
    private void handleEdit(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws ServletException, IOException {
        
        int productId = parsePositiveInt(request.getParameter("id"));
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Products product = buildProductFromRequest(request);
        try {
            boolean success = sellerService.updateProduct(product, productId, shopId);

            if (success) {
                request.getSession().setAttribute("successMessage", "Cập nhật sản phẩm thành công!");
                response.sendRedirect(request.getContextPath() + "/seller/products");
            } else {
                request.setAttribute("errorMessage", "Không thể cập nhật sản phẩm. Vui lòng thử lại.");
                request.setAttribute("product", product); // Giữ lại dữ liệu đã nhập
                showEditForm(request, response, shopId);
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("product", product); // Giữ lại dữ liệu đã nhập
            showEditForm(request, response, shopId);
        }
    }

    /**
     * Xử lý xóa sản phẩm.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws IOException {
        
        int productId = parsePositiveInt(request.getParameter("id"));
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean success = sellerService.deleteProduct(productId, shopId);
        if (success) {
            request.getSession().setAttribute("successMessage", "Xóa sản phẩm thành công!");
        } else {
            request.getSession().setAttribute("errorMessage", "Không thể xóa sản phẩm.");
        }

        response.sendRedirect(request.getContextPath() + "/seller/products");
    }

    /**
     * Xử lý thay đổi trạng thái sản phẩm.
     */
    private void handleStatusChange(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws IOException {
        
        int productId = parsePositiveInt(request.getParameter("id"));
        String status = request.getParameter("status");

        if (productId <= 0 || status == null || status.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean success = sellerService.updateProductStatus(productId, shopId, status);
            if (success) {
                request.getSession().setAttribute("successMessage", "Cập nhật trạng thái thành công!");
            } else {
                request.getSession().setAttribute("errorMessage", "Không thể cập nhật trạng thái.");
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/seller/products");
    }

    /**
     * Xây dựng đối tượng Product từ request.
     */
    private Products buildProductFromRequest(HttpServletRequest request) {
        Products product = new Products();
        
        product.setName(request.getParameter("name"));
        product.setProductType(request.getParameter("productType"));
        product.setProductSubtype(emptyToNull(request.getParameter("productSubtype")));
        product.setShortDescription(emptyToNull(request.getParameter("shortDescription")));
        product.setDescription(emptyToNull(request.getParameter("description")));
        
        String priceStr = request.getParameter("price");
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            product.setPrice(new BigDecimal(priceStr));
        }
        
        String inventoryStr = request.getParameter("inventoryCount");
        if (inventoryStr != null && !inventoryStr.trim().isEmpty()) {
            product.setInventoryCount(Integer.parseInt(inventoryStr));
        }
        
        product.setPrimaryImageUrl(emptyToNull(request.getParameter("primaryImageUrl")));
        product.setGalleryJson(emptyToNull(request.getParameter("galleryJson")));
        product.setVariantSchema(emptyToNull(request.getParameter("variantSchema")));
        product.setVariantsJson(emptyToNull(request.getParameter("variantsJson")));
        product.setStatus(request.getParameter("status"));
        
        return product;
    }
    
    /**
     * Convert empty string to null
     */
    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Parse integer từ string, trả về -1 nếu không hợp lệ.
     */
    private int parsePositiveInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Parse integer từ string, trả về defaultValue nếu không hợp lệ.
     */
    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        int parsed = parsePositiveInt(value);
        return parsed > 0 ? parsed : defaultValue;
    }

    /**
     * Kiểm tra session có phải của seller không.
     */
    private boolean isSellerSession(HttpSession session) {
        if (session == null) {
            return false;
        }
        Integer role = (Integer) session.getAttribute("userRole");
        return role != null && ROLE_SELLER == role;
    }
}
