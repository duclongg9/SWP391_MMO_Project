package service;

import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import model.PaginatedResult;
import model.Products;
import model.Shops;
import model.Users;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service layer cho quản lý sản phẩm của seller
 */
public class ProductManagementService {

    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_.()]{3,100}$");
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final BigDecimal MIN_PRICE = new BigDecimal("1000"); // 1,000 VND
    private static final BigDecimal MAX_PRICE = new BigDecimal("100000000"); // 100 triệu VND

    private final ProductDAO productDAO;
    private final ShopDAO shopDAO;

    public ProductManagementService() {
        this.productDAO = new ProductDAO();
        this.shopDAO = new ShopDAO();
    }

    public ProductManagementService(ProductDAO productDAO, ShopDAO shopDAO) {
        this.productDAO = productDAO;
        this.shopDAO = shopDAO;
    }

    /**
     * Tạo sản phẩm mới
     */
    public Products createProduct(Users seller, String name, String description, 
                                BigDecimal price, Integer inventoryCount) {
        validateSeller(seller);
        validateProductInput(name, description, price, inventoryCount);
        
        Shops myShop = getActiveShop(seller);
        
        try {
            return productDAO.createProduct(myShop.getId(), name.trim(), 
                    description.trim(), price, inventoryCount);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo sản phẩm lúc này. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Lấy danh sách sản phẩm của shop với phân trang
     */
    public PaginatedResult<Products> getMyProducts(Users seller, int page, int pageSize) {
        validateSeller(seller);
        
        Shops myShop = getActiveShop(seller);
        
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(Math.min(pageSize, 50), 5); // Giới hạn 5-50 items per page
        
        long totalItems = productDAO.countByShopId(myShop.getId());
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;
        
        List<Products> products = productDAO.findByShopId(myShop.getId(), safeSize, offset);
        
        return new PaginatedResult<>(products, currentPage, totalPages, 
                safeSize, Math.toIntExact(totalItems));
    }

    /**
     * Cập nhật sản phẩm
     */
    public boolean updateMyProduct(Users seller, int productId, String name, String description, 
                                 BigDecimal price, Integer inventoryCount) {
        validateSeller(seller);
        validateProductInput(name, description, price, inventoryCount);
        
        Shops myShop = getActiveShop(seller);
        
        // Kiểm tra sản phẩm thuộc về shop của seller
        Products existingProduct = productDAO.findById(productId).orElse(null);
        if (existingProduct == null || !existingProduct.getShopId().equals(myShop.getId())) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc không thuộc gian hàng của bạn");
        }

        return productDAO.updateProduct(productId, name.trim(), 
                description.trim(), price, inventoryCount);
    }

    /**
     * Xóa sản phẩm
     */
    public boolean deleteMyProduct(Users seller, int productId) {
        validateSeller(seller);
        
        Shops myShop = getActiveShop(seller);
        
        // Kiểm tra sản phẩm thuộc về shop của seller
        Products existingProduct = productDAO.findById(productId).orElse(null);
        if (existingProduct == null || !existingProduct.getShopId().equals(myShop.getId())) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc không thuộc gian hàng của bạn");
        }

        return productDAO.deleteProduct(productId, myShop.getId());
    }

    /**
     * Lấy chi tiết sản phẩm của mình
     */
    public Products getMyProduct(Users seller, int productId) {
        validateSeller(seller);
        
        Shops myShop = getActiveShop(seller);
        
        Products product = productDAO.findById(productId).orElse(null);
        if (product == null || !product.getShopId().equals(myShop.getId())) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc không thuộc gian hàng của bạn");
        }
        
        return product;
    }

    private void validateSeller(Users seller) {
        if (seller == null) {
            throw new IllegalArgumentException("Thông tin seller không hợp lệ");
        }
        if (seller.getRoleId() == null || seller.getRoleId() != 2) {
            throw new IllegalArgumentException("Chỉ seller mới có thể quản lý sản phẩm");
        }
        if (Boolean.FALSE.equals(seller.getStatus())) {
            throw new IllegalStateException("Tài khoản seller đang bị khóa");
        }
    }

    private Shops getActiveShop(Users seller) {
        Shops shop = shopDAO.findByOwnerId(seller.getId());
        if (shop == null) {
            throw new IllegalStateException("Bạn chưa có gian hàng. Vui lòng tạo gian hàng trước khi đăng sản phẩm");
        }
        if (!"Active".equals(shop.getStatus())) {
            throw new IllegalStateException("Gian hàng của bạn chưa được kích hoạt. Vui lòng chờ admin duyệt");
        }
        return shop;
    }

    private void validateProductInput(String name, String description, BigDecimal price, Integer inventoryCount) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm");
        }
        
        String trimmedName = name.trim();
        if (!PRODUCT_NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new IllegalArgumentException("Tên sản phẩm phải từ 3-100 ký tự, chỉ chứa chữ, số và ký tự đặc biệt: - _ . ( )");
        }

        // Validate description
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mô tả sản phẩm");
        }
        
        String trimmedDesc = description.trim();
        if (trimmedDesc.length() < MIN_DESCRIPTION_LENGTH || trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Mô tả sản phẩm phải từ " + MIN_DESCRIPTION_LENGTH + 
                    " đến " + MAX_DESCRIPTION_LENGTH + " ký tự");
        }

        // Validate price
        if (price == null) {
            throw new IllegalArgumentException("Vui lòng nhập giá sản phẩm");
        }
        
        if (price.compareTo(MIN_PRICE) < 0 || price.compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải từ " + 
                    MIN_PRICE.toPlainString() + " đến " + MAX_PRICE.toPlainString() + " VND");
        }

        // Validate inventory (optional)
        if (inventoryCount != null && inventoryCount < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }
        if (inventoryCount != null && inventoryCount > 99999) {
            throw new IllegalArgumentException("Số lượng tồn kho không được vượt quá 99,999");
        }
    }
}
