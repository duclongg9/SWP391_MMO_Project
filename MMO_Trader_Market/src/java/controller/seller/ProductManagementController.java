package controller.seller;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.PaginatedResult;
import model.Products;
import model.Users;
import service.ProductManagementService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho quản lý sản phẩm của seller
 */
@WebServlet(name = "ProductManagementController", urlPatterns = {"/seller/products"})
public class ProductManagementController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProductManagementController.class.getName());
    
    private final ProductManagementService productService = new ProductManagementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        if (currentUser.getRoleId() == null || currentUser.getRoleId() != 2) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ seller mới có thể truy cập trang này");
            return;
        }

        String action = request.getParameter("action");
        
        try {
            switch (action != null ? action : "list") {
                case "create" -> showCreateProductForm(request, response, currentUser);
                case "edit" -> showEditProductForm(request, response, currentUser);
                case "delete" -> handleDeleteProduct(request, response, currentUser);
                default -> showProductList(request, response, currentUser);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            showProductList(request, response, currentUser);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong ProductManagementController", e);
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
            showProductList(request, response, currentUser);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        if (currentUser.getRoleId() == null || currentUser.getRoleId() != 2) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ seller mới có thể thực hiện thao tác này");
            return;
        }

        String action = request.getParameter("action");
        
        try {
            switch (action != null ? action : "") {
                case "create" -> handleCreateProduct(request, response, currentUser);
                case "update" -> handleUpdateProduct(request, response, currentUser);
                default -> {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
                    return;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            if ("create".equals(action)) {
                preserveFormData(request);
                showCreateProductForm(request, response, currentUser);
            } else if ("update".equals(action)) {
                preserveFormData(request);
                showEditProductForm(request, response, currentUser);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong ProductManagementController POST", e);
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
            showProductList(request, response, currentUser);
        }
    }

    private void showProductList(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        // Lấy tham số phân trang
        int page = getIntParameter(request, "page", 1);
        int pageSize = getIntParameter(request, "size", 10);
        
        PaginatedResult<Products> result = productService.getMyProducts(seller, page, pageSize);
        
        request.setAttribute("products", result.getItems());
        request.setAttribute("pagination", result);
        request.setAttribute("pageTitle", "Quản lý sản phẩm");
        
        // Kiểm tra thông báo thành công từ session
        HttpSession session = request.getSession();
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        
        forward(request, response, "seller/product-list");
    }

    private void showCreateProductForm(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        request.setAttribute("pageTitle", "Đăng sản phẩm mới");
        forward(request, response, "seller/create-product");
    }

    private void showEditProductForm(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        int productId = getIntParameter(request, "id", 0);
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ");
            return;
        }
        
        Products product = productService.getMyProduct(seller, productId);
        
        request.setAttribute("product", product);
        request.setAttribute("pageTitle", "Chỉnh sửa sản phẩm: " + product.getName());
        forward(request, response, "seller/edit-product");
    }

    private void handleCreateProduct(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        BigDecimal price = parsePrice(request.getParameter("price"));
        Integer inventoryCount = parseInventoryCount(request.getParameter("inventoryCount"));

        Products newProduct = productService.createProduct(seller, name, description, price, inventoryCount);
        
        // Thành công - chuyển hướng với thông báo
        HttpSession session = request.getSession();
        session.setAttribute("successMessage", "Đăng sản phẩm thành công! Sản phẩm đang chờ admin duyệt.");
        response.sendRedirect(request.getContextPath() + "/seller/products");
    }

    private void handleUpdateProduct(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        int productId = getIntParameter(request, "productId", 0);
        if (productId <= 0) {
            throw new IllegalArgumentException("ID sản phẩm không hợp lệ");
        }
        
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        BigDecimal price = parsePrice(request.getParameter("price"));
        Integer inventoryCount = parseInventoryCount(request.getParameter("inventoryCount"));

        boolean updated = productService.updateMyProduct(seller, productId, name, description, price, inventoryCount);
        
        if (updated) {
            HttpSession session = request.getSession();
            session.setAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            response.sendRedirect(request.getContextPath() + "/seller/products");
        } else {
            request.setAttribute("error", "Không thể cập nhật sản phẩm");
            showEditProductForm(request, response, seller);
        }
    }

    private void handleDeleteProduct(HttpServletRequest request, HttpServletResponse response, Users seller)
            throws ServletException, IOException {
        
        int productId = getIntParameter(request, "id", 0);
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ");
            return;
        }
        
        boolean deleted = productService.deleteMyProduct(seller, productId);
        
        HttpSession session = request.getSession();
        if (deleted) {
            session.setAttribute("successMessage", "Xóa sản phẩm thành công!");
        } else {
            session.setAttribute("errorMessage", "Không thể xóa sản phẩm");
        }
        
        response.sendRedirect(request.getContextPath() + "/seller/products");
    }

    private Users getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Users) session.getAttribute("currentUser");
    }

    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập giá sản phẩm");
        }
        
        try {
            // Loại bỏ dấu phẩy và khoảng trắng
            String cleanPrice = priceStr.trim().replaceAll("[,\\s]", "");
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá sản phẩm không hợp lệ");
        }
    }

    private Integer parseInventoryCount(String inventoryStr) {
        if (inventoryStr == null || inventoryStr.trim().isEmpty()) {
            return null; // Cho phép null (unlimited inventory)
        }
        
        try {
            return Integer.parseInt(inventoryStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng tồn kho không hợp lệ");
        }
    }

    private void preserveFormData(HttpServletRequest request) {
        request.setAttribute("name", request.getParameter("name"));
        request.setAttribute("description", request.getParameter("description"));
        request.setAttribute("price", request.getParameter("price"));
        request.setAttribute("inventoryCount", request.getParameter("inventoryCount"));
    }
}
