package service;

import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import model.Shops;
import model.view.ShopListItem;
import service.BusinessException;
import service.dto.ShopAction;
import service.dto.ShopFilters;

/**
 * Xử lý nghiệp vụ quản lý shop cho seller: lọc danh sách, tạo/sửa thông tin và
 * thay đổi trạng thái kèm cascade sản phẩm.
 */
public class ShopService {

    private static final Pattern SHOP_NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N} ]{3,60}$");
    private static final int MIN_DESCRIPTION_LENGTH = 20;
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_SUSPENDED = "Suspended";
    private static final String PRODUCT_STATUS_UNLISTED = "UNLISTED";

    private final ShopDAO shopDAO = new ShopDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Lấy danh sách shop của owner với bộ lọc đã chuẩn hóa.
     *
     * @param ownerId id seller
     * @param filters bộ lọc (có thể null)
     * @return danh sách shop đã sắp xếp theo updated_at giảm dần
     * @throws SQLException lỗi truy vấn
     */
    public List<ShopListItem> listByOwner(long ownerId, ShopFilters filters)
            throws SQLException {
        ShopFilters sanitized = sanitizeFilters(filters);
        return shopDAO.findByOwnerWithFilters(ownerId, sanitized);
    }

    /**
     * Tạo shop mới sau khi chuẩn hóa tên/mô tả và kiểm tra trùng.
     */
    public Shops createShop(long ownerId, String rawName, String rawDescription)
            throws SQLException, BusinessException {
        String normalizedName = normalizeName(rawName);
        validateName(normalizedName);
        if (shopDAO.existsNameInOwner(ownerId, normalizedName, null)) {
            throw new BusinessException("SHOP_NAME_DUPLICATED");
        }
        String description = normalizeDescription(rawDescription);
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new BusinessException("DESCRIPTION_TOO_SHORT");
        }
        return shopDAO.insert(ownerId, normalizedName, description, STATUS_PENDING);
    }

    /**
     * Cập nhật thông tin shop. Nếu tên hoặc mô tả trống/không hợp lệ sẽ ném lỗi.
     */
    public Shops updateShop(long ownerId, long shopId, String rawName, String rawDescription)
            throws SQLException, BusinessException {
        Optional<Shops> existingOpt = shopDAO.findByIdAndOwner(shopId, ownerId);
        if (existingOpt.isEmpty()) {
            throw new BusinessException("FORBIDDEN");
        }
        String normalizedName = normalizeName(rawName);
        validateName(normalizedName);
        if (shopDAO.existsNameInOwner(ownerId, normalizedName, shopId)) {
            throw new BusinessException("SHOP_NAME_DUPLICATED");
        }
        String description = normalizeDescription(rawDescription);
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new BusinessException("DESCRIPTION_TOO_SHORT");
        }
        boolean updated = shopDAO.update(shopId, ownerId, normalizedName, description);
        if (!updated) {
            throw new BusinessException("FORBIDDEN");
        }
        Shops shop = existingOpt.get();
        shop.setName(normalizedName);
        shop.setDescription(description);
        shop.setUpdatedAt(new java.util.Date());
        return shop;
    }

    /**
     * Thay đổi trạng thái shop và cascade sản phẩm nếu cần.
     */
    public void changeStatus(long ownerId, long shopId, ShopAction action)
            throws SQLException, BusinessException {
        Optional<Shops> existingOpt = shopDAO.findByIdAndOwner(shopId, ownerId);
        if (existingOpt.isEmpty()) {
            throw new BusinessException("FORBIDDEN");
        }
        try (Connection connection = shopDAO.openConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                boolean success;
                if (action == ShopAction.HIDE) {
                    success = shopDAO.updateStatus(connection, shopId, ownerId, STATUS_SUSPENDED);
                    if (!success) {
                        throw new BusinessException("FORBIDDEN");
                    }
                    productDAO.updateStatusByShop(connection, shopId, PRODUCT_STATUS_UNLISTED);
                } else if (action == ShopAction.UNHIDE) {
                    success = shopDAO.updateStatus(connection, shopId, ownerId, STATUS_ACTIVE);
                    if (!success) {
                        throw new BusinessException("FORBIDDEN");
                    }
                } else {
                    throw new BusinessException("UNSUPPORTED_ACTION");
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof BusinessException) {
                    throw (BusinessException) ex;
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Unexpected error when changing shop status", ex);
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public Optional<Shops> findByIdAndOwner(long shopId, long ownerId) throws SQLException {
        return shopDAO.findByIdAndOwner(shopId, ownerId);
    }

    /**
     * Chuẩn hóa bộ lọc danh sách shop, đảm bảo từ khóa rỗng được chuyển thành {@code null}.
     *
     * @param filters bộ lọc đầu vào từ controller
     * @return bộ lọc đã chuẩn hóa, luôn không null
     */
    private ShopFilters sanitizeFilters(ShopFilters filters) {
        if (filters == null) {
            return ShopFilters.builder().build();
        }
        String keyword = filters.getKeyword();
        keyword = keyword == null ? null : keyword.trim();
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }
        return ShopFilters.builder()
                .keyword(keyword)
                .build();
    }

    private String normalizeName(String rawName) throws BusinessException {
        if (rawName == null) {
            throw new BusinessException("SHOP_NAME_INVALID");
        }
        String collapsed = rawName.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new BusinessException("SHOP_NAME_INVALID");
        }
        return collapsed;
    }

    private void validateName(String normalizedName) throws BusinessException {
        if (!SHOP_NAME_PATTERN.matcher(normalizedName).matches()) {
            throw new BusinessException("SHOP_NAME_INVALID");
        }
    }

    private String normalizeDescription(String rawDescription) {
        return rawDescription == null ? "" : rawDescription.trim();
    }
}
