package service;

import dao.seller.SellerOrderDAO;
import dao.seller.SellerProductDAO;
import dao.seller.SellerShopDAO;
import model.Orders;
import model.Products;
import model.Shops;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service tổng hợp cho các chức năng của Seller.
 * Xử lý logic nghiệp vụ liên quan đến shop, sản phẩm và đơn hàng của seller.
 * 
 * @version 1.0
 * @author AI Assistant
 */
public class SellerService {

    private final SellerShopDAO shopDAO = new SellerShopDAO();
    private final SellerProductDAO productDAO = new SellerProductDAO();
    private final SellerOrderDAO orderDAO = new SellerOrderDAO();

    // ==================== SHOP MANAGEMENT ====================

    /**
     * Lấy thông tin shop của seller.
     *
     * @param sellerId ID của seller
     * @return Optional chứa shop nếu có
     */
    public Optional<Shops> getSellerShop(int sellerId) {
        return shopDAO.findByOwnerId(sellerId);
    }

    /**
     * Kiểm tra seller đã có shop chưa.
     *
     * @param sellerId ID của seller
     * @return true nếu đã có shop
     */
    public boolean hasShop(int sellerId) {
        return shopDAO.hasShop(sellerId);
    }

    /**
     * Tạo shop mới cho seller.
     *
     * @param sellerId    ID của seller
     * @param name        tên shop
     * @param description mô tả shop
     * @return ID của shop vừa tạo, hoặc -1 nếu thất bại
     */
    public int createShop(int sellerId, String name, String description) {
        // Kiểm tra xem seller đã có shop chưa
        if (hasShop(sellerId)) {
            throw new IllegalStateException("Seller đã có shop rồi");
        }

        // Validate
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên shop không được để trống");
        }

        Shops shop = new Shops();
        shop.setOwnerId(sellerId);
        shop.setName(name.trim());
        shop.setDescription(description != null ? description.trim() : "");
        shop.setStatus("Pending"); // Shop mới cần admin duyệt

        return shopDAO.create(shop);
    }

    /**
     * Cập nhật thông tin shop.
     *
     * @param shopId      ID của shop
     * @param sellerId    ID của seller
     * @param name        tên shop mới
     * @param description mô tả mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateShop(int shopId, int sellerId, String name, String description) {
        // Validate
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên shop không được để trống");
        }

        // Kiểm tra quyền sở hữu
        Optional<Shops> shopOpt = shopDAO.findByIdAndOwnerId(shopId, sellerId);
        if (shopOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy shop hoặc không có quyền truy cập");
        }

        Shops shop = shopOpt.get();
        shop.setName(name.trim());
        shop.setDescription(description != null ? description.trim() : "");

        return shopDAO.update(shop);
    }

    // ==================== PRODUCT MANAGEMENT ====================

    /**
     * Lấy danh sách sản phẩm của shop với phân trang.
     *
     * @param shopId ID của shop
     * @param page   trang hiện tại (bắt đầu từ 1)
     * @param size   số lượng sản phẩm mỗi trang
     * @return danh sách sản phẩm
     */
    public List<Products> getProducts(int shopId, int page, int size) {
        int offset = (page - 1) * size;
        return productDAO.findByShopId(shopId, size, offset);
    }

    /**
     * Đếm tổng số sản phẩm của shop.
     *
     * @param shopId ID của shop
     * @return tổng số sản phẩm
     */
    public long countProducts(int shopId) {
        return productDAO.countByShopId(shopId);
    }

    /**
     * Lấy chi tiết sản phẩm (chỉ sản phẩm của shop).
     *
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @return Optional chứa sản phẩm
     */
    public Optional<Products> getProduct(int productId, int shopId) {
        return productDAO.findByIdAndShopId(productId, shopId);
    }

    /**
     * Tạo sản phẩm mới.
     *
     * @param product sản phẩm cần tạo
     * @param shopId  ID của shop
     * @return ID của sản phẩm vừa tạo
     */
    public int createProduct(Products product, int shopId) {
        // Validate
        validateProduct(product);

        // Kiểm tra shop có active không
        // TODO: Uncomment this check in production
        // if (!shopDAO.isActive(shopId)) {
        //     throw new IllegalStateException("Shop chưa được kích hoạt, không thể thêm sản phẩm");
        // }

        product.setShopId(shopId);
        product.setSoldCount(0);

        return productDAO.create(product);
    }

    /**
     * Cập nhật sản phẩm.
     *
     * @param product   sản phẩm cần cập nhật
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @return true nếu cập nhật thành công
     */
    public boolean updateProduct(Products product, int productId, int shopId) {
        // Validate
        validateProduct(product);

        // Kiểm tra quyền sở hữu
        Optional<Products> existingProduct = productDAO.findByIdAndShopId(productId, shopId);
        if (existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc không có quyền truy cập");
        }

        product.setId(productId);
        product.setShopId(shopId);

        return productDAO.update(product);
    }

    /**
     * Xóa sản phẩm (soft delete).
     *
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @return true nếu xóa thành công
     */
    public boolean deleteProduct(int productId, int shopId) {
        return productDAO.delete(productId, shopId);
    }

    /**
     * Cập nhật trạng thái sản phẩm.
     *
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @param status    trạng thái mới (Available, Unavailable)
     * @return true nếu cập nhật thành công
     */
    public boolean updateProductStatus(int productId, int shopId, String status) {
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }
        return productDAO.updateStatus(productId, shopId, status);
    }

    /**
     * Cập nhật số lượng tồn kho.
     *
     * @param productId      ID của sản phẩm
     * @param shopId         ID của shop
     * @param inventoryCount số lượng tồn kho mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateInventory(int productId, int shopId, int inventoryCount) {
        if (inventoryCount < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }
        return productDAO.updateInventory(productId, shopId, inventoryCount);
    }

    /**
     * Thống kê sản phẩm theo trạng thái.
     *
     * @param shopId ID của shop
     * @return mảng [Available, Unavailable, Deleted]
     */
    public long[] getProductStatsByStatus(int shopId) {
        return productDAO.countByStatus(shopId);
    }

    /**
     * Tìm kiếm sản phẩm trong shop.
     *
     * @param shopId  ID của shop
     * @param keyword từ khóa tìm kiếm
     * @param page    trang hiện tại
     * @param size    số lượng mỗi trang
     * @return danh sách sản phẩm
     */
    public List<Products> searchProducts(int shopId, String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getProducts(shopId, page, size);
        }
        int offset = (page - 1) * size;
        return productDAO.searchInShop(shopId, keyword.trim(), size, offset);
    }

    // ==================== ORDER MANAGEMENT ====================

    /**
     * Lấy danh sách đơn hàng của shop.
     *
     * @param shopId ID của shop
     * @param status trạng thái đơn hàng (null = tất cả)
     * @param page   trang hiện tại
     * @param size   số lượng mỗi trang
     * @return danh sách đơn hàng
     */
    public List<Orders> getOrders(int shopId, String status, int page, int size) {
        int offset = (page - 1) * size;
        return orderDAO.findByShopId(shopId, status, size, offset);
    }

    /**
     * Đếm tổng số đơn hàng của shop.
     *
     * @param shopId ID của shop
     * @param status trạng thái đơn hàng (null = tất cả)
     * @return tổng số đơn hàng
     */
    public long countOrders(int shopId, String status) {
        return orderDAO.countByShopId(shopId, status);
    }

    /**
     * Thống kê đơn hàng theo trạng thái.
     *
     * @param shopId ID của shop
     * @return Map<status, count>
     */
    public Map<String, Long> getOrderStatsByStatus(int shopId) {
        return orderDAO.countByStatus(shopId);
    }

    /**
     * Tính tổng doanh thu của shop.
     *
     * @param shopId ID của shop
     * @return tổng doanh thu
     */
    public BigDecimal getRevenue(int shopId) {
        BigDecimal revenue = orderDAO.calculateRevenue(shopId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // ==================== DASHBOARD DATA ====================

    /**
     * Lấy dữ liệu tổng quan cho dashboard.
     *
     * @param shopId ID của shop
     * @return Map chứa các thống kê
     */
    public Map<String, Object> getDashboardStats(int shopId) {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        // Thống kê sản phẩm
        long totalProducts = productDAO.countByShopId(shopId);
        long[] productStats = productDAO.countByStatus(shopId);
        stats.put("totalProducts", totalProducts);
        stats.put("availableProducts", productStats[0]);
        stats.put("unavailableProducts", productStats[1]);
        
        // Thống kê đơn hàng
        long totalOrders = orderDAO.countByShopId(shopId, null);
        Map<String, Long> orderStats = orderDAO.countByStatus(shopId);
        stats.put("totalOrders", totalOrders);
        stats.put("ordersByStatus", orderStats);
        
        // Doanh thu
        BigDecimal revenue = orderDAO.calculateRevenue(shopId);
        stats.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        
        return stats;
    }

    // ==================== VALIDATION ====================

    private void validateProduct(Products product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }

        if (product.getInventoryCount() == null || product.getInventoryCount() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không hợp lệ");
        }

        if (product.getProductType() == null || product.getProductType().trim().isEmpty()) {
            throw new IllegalArgumentException("Loại sản phẩm không được để trống");
        }
    }

    private boolean isValidStatus(String status) {
        // Các giá trị ENUM hợp lệ từ database: Available, OutOfStock, Unlisted
        return "Available".equals(status) || "OutOfStock".equals(status) || "Unlisted".equals(status);
    }
}
