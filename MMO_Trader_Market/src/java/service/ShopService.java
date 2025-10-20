package service;

import dao.shop.ShopDAO;
import model.Shops;
import model.Users;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Service layer cho quản lý shop và seller operations
 */
public class ShopService {

    private static final Pattern SHOP_NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_.]{3,50}$");
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final ShopDAO shopDAO;

    public ShopService() {
        this.shopDAO = new ShopDAO();
    }

    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
    }

    /**
     * Tạo gian hàng mới cho seller
     */
    public Shops createShop(Users seller, String shopName, String description) {
        validateSeller(seller);
        validateShopInput(shopName, description);
        
        // Kiểm tra seller đã có shop chưa
        Shops existingShop = shopDAO.findByOwnerId(seller.getId());
        if (existingShop != null) {
            throw new IllegalStateException("Bạn đã có gian hàng. Mỗi seller chỉ được tạo 1 gian hàng.");
        }

        try {
            return shopDAO.createShop(seller.getId(), shopName.trim(), description.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo gian hàng lúc này. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Lấy thông tin shop của seller
     */
    public Shops getMyShop(Users seller) {
        validateSeller(seller);
        return shopDAO.findByOwnerId(seller.getId());
    }

    /**
     * Cập nhật thông tin shop
     */
    public boolean updateMyShop(Users seller, String shopName, String description) {
        validateSeller(seller);
        validateShopInput(shopName, description);

        Shops myShop = shopDAO.findByOwnerId(seller.getId());
        if (myShop == null) {
            throw new IllegalStateException("Bạn chưa có gian hàng để cập nhật");
        }

        return shopDAO.updateShop(myShop.getId(), shopName.trim(), description.trim());
    }

    /**
     * Kiểm tra seller có thể tạo shop không
     */
    public boolean canCreateShop(Users user) {
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        // Role 2 = Seller
        if (user.getRoleId() != 2) {
            return false;
        }
        // Chưa có shop
        return shopDAO.findByOwnerId(user.getId()) == null;
    }

    /**
     * Kiểm tra seller có shop đang hoạt động không
     */
    public boolean hasActiveShop(Users seller) {
        if (seller == null || seller.getRoleId() == null || seller.getRoleId() != 2) {
            return false;
        }
        Shops shop = shopDAO.findByOwnerId(seller.getId());
        return shop != null && "Active".equals(shop.getStatus());
    }

    /**
     * Lấy shop theo ID (cho public view)
     */
    public Shops getShopById(int shopId) {
        return shopDAO.findById(shopId);
    }

    private void validateSeller(Users seller) {
        if (seller == null) {
            throw new IllegalArgumentException("Thông tin seller không hợp lệ");
        }
        if (seller.getRoleId() == null || seller.getRoleId() != 2) {
            throw new IllegalArgumentException("Chỉ seller mới có thể thực hiện thao tác này");
        }
        if (Boolean.FALSE.equals(seller.getStatus())) {
            throw new IllegalStateException("Tài khoản seller đang bị khóa");
        }
    }

    private void validateShopInput(String shopName, String description) {
        if (shopName == null || shopName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên gian hàng");
        }

        String trimmedName = shopName.trim();
        if (!SHOP_NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new IllegalArgumentException("Tên gian hàng phải từ 3-50 ký tự, chỉ chứa chữ, số và ký tự đặc biệt: - _ .");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mô tả gian hàng");
        }

        String trimmedDesc = description.trim();
        if (trimmedDesc.length() < MIN_DESCRIPTION_LENGTH || trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Mô tả gian hàng phải từ " + MIN_DESCRIPTION_LENGTH + 
                    " đến " + MAX_DESCRIPTION_LENGTH + " ký tự");
        }
    }
}
