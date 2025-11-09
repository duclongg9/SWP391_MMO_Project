package service;

import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import model.ShopStatsView;
import model.Shops;

/**
 * Service xử lý logic nghiệp vụ liên quan đến quản lý shop cho seller.
 * Bao gồm: tạo, cập nhật, ẩn/khôi phục shop, và liệt kê shop kèm thống kê.
 */
public class ShopService {

        private static final int MIN_LENGTH = 20;
        private static final int MAX_NAME_LENGTH = 255;
        private static final int MAX_DESCRIPTION_LENGTH = 1000;
        private static final Pattern ALLOWED_TEXT_PATTERN = Pattern.compile("^[\\p{L}\\p{Nd}\\s]+$", Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
        private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

        private final ShopDAO shopDAO = new ShopDAO();
        private final ProductDAO productDAO = new ProductDAO();

        /**
         * Tạo shop mới cho seller.
         * Kiểm tra giới hạn tối đa 5 shop, validate tên/mô tả (tối thiểu 20 ký tự, chỉ cho phép chữ, số, khoảng trắng)
         * và tự động set status = 'Active', created_at = NOW().
	 *
	 * @param ownerId ID của seller (chủ sở hữu shop)
	 * @param name Tên shop (sẽ được trim và validate)
	 * @param description Mô tả shop (tùy chọn, có thể null)
	 * @throws BusinessException nếu vượt quá 5 shop hoặc tên không hợp lệ
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
	public void createShop(int ownerId, String name, String description) throws BusinessException, SQLException {
		// Kiểm tra giới hạn tối đa 5 shop
		if (shopDAO.countByOwner(ownerId) >= 5) {
			throw new BusinessException("Bạn chỉ được tạo tối đa 5 shop.");
		}
                String normalizedName = normalizeText(name);
                validateShopText(normalizedName, "Tên shop", MAX_NAME_LENGTH, true);

                String normalizedDescription = normalizeText(description);
                validateShopText(normalizedDescription, "Mô tả shop", MAX_DESCRIPTION_LENGTH, true);

                shopDAO.create(ownerId, normalizedName, normalizedDescription);
	}

        /**
         * Cập nhật tên và mô tả của shop.
         * Validate dữ liệu giống như tạo mới (tối thiểu 20 ký tự, không chứa ký tự đặc biệt)
         * và kiểm tra quyền owner trước khi cập nhật.
	 *
	 * @param id ID của shop cần cập nhật
	 * @param ownerId ID của seller (để xác thực quyền)
	 * @param name Tên mới (sẽ được trim và validate)
	 * @param description Mô tả mới (tùy chọn, có thể null)
	 * @throws BusinessException nếu tên không hợp lệ hoặc không tìm thấy shop/không có quyền
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
	public void updateShop(int id, int ownerId, String name, String description) throws BusinessException, SQLException {
                String normalizedName = normalizeText(name);
                validateShopText(normalizedName, "Tên shop", MAX_NAME_LENGTH, true);

                String normalizedDescription = normalizeText(description);
                validateShopText(normalizedDescription, "Mô tả shop", MAX_DESCRIPTION_LENGTH, true);

                boolean ok = shopDAO.update(id, ownerId, normalizedName, normalizedDescription);
                if (!ok) {
                        throw new BusinessException("Không tìm thấy shop hoặc bạn không có quyền.");
                }
	}

        /**
         * Ẩn shop bằng cách đổi trạng thái sang 'Suspended'.
         * Đồng thời chuyển toàn bộ sản phẩm thuộc shop sang trạng thái "Unlisted" để ngừng bán.
         * Chỉ cho phép nếu shop thuộc về owner.
	 *
	 * @param id ID của shop cần ẩn
	 * @param ownerId ID của seller (để xác thực quyền)
	 * @throws BusinessException nếu không tìm thấy shop hoặc không có quyền
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
	public void hideShop(int id, int ownerId) throws BusinessException, SQLException {
		// Đổi trạng thái sang 'Suspended' (ngừng hoạt động)
                boolean ok = shopDAO.setStatus(id, ownerId, "Suspended");
                if (!ok) {
                        throw new BusinessException("Không tìm thấy shop hoặc bạn không có quyền.");
                }
                productDAO.updateStatusByShop(id, "Unlisted");
	}

	/**
	 * Khôi phục shop bằng cách đổi trạng thái sang 'Active'.
	 * Chỉ cho phép nếu shop thuộc về owner.
	 *
	 * @param id ID của shop cần khôi phục
	 * @param ownerId ID của seller (để xác thực quyền)
	 * @throws BusinessException nếu không tìm thấy shop hoặc không có quyền
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
	public void restoreShop(int id, int ownerId) throws BusinessException, SQLException {
		// Đổi trạng thái sang 'Active' (hoạt động trở lại)
		boolean ok = shopDAO.setStatus(id, ownerId, "Active");
		if (!ok) {
			throw new BusinessException("Không tìm thấy shop hoặc bạn không có quyền.");
		}
	}

        /**
         * Lấy danh sách shop của seller kèm thống kê (số sản phẩm, lượng bán, tồn kho).
         * Hỗ trợ sắp xếp theo lượng bán, ngày tạo, hoặc tên, đồng thời cho phép tìm theo tên shop.
         *
         * @param ownerId ID của seller
         * @param sortBy Cách sắp xếp: 'sales_desc', 'created_desc', 'name_asc', hoặc null (mặc định sales_desc)
         * @param searchKeyword Từ khóa tìm kiếm theo tên shop (có thể null)
         * @return Danh sách ShopStatsView chứa thông tin shop và thống kê
         * @throws SQLException nếu có lỗi khi truy vấn database
         */
        public List<ShopStatsView> listMyShops(int ownerId, String sortBy, String searchKeyword) throws SQLException {
                return shopDAO.findByOwnerWithStats(ownerId, sortBy, searchKeyword);
        }

	/**
	 * Tìm shop theo ID và owner, dùng để kiểm tra quyền trước khi cho phép chỉnh sửa.
	 *
	 * @param id ID của shop
	 * @param ownerId ID của seller (để xác thực quyền)
	 * @return Optional chứa Shops nếu tìm thấy và thuộc về owner, Optional.empty() nếu không
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public java.util.Optional<Shops> findByIdAndOwner(int id, int ownerId) throws SQLException {
                return shopDAO.findByIdAndOwner(id, ownerId);
        }

        private String normalizeText(String value) {
                if (value == null) {
                        return "";
                }
                String trimmed = value.trim();
                return WHITESPACE_PATTERN.matcher(trimmed).replaceAll(" ");
        }

        private void validateShopText(String text, String fieldLabel, int maxLength, boolean required) throws BusinessException {
                if (text == null || text.isBlank()) {
                        if (required) {
                                throw new BusinessException(fieldLabel + " không được để trống và phải có tối thiểu " + MIN_LENGTH + " ký tự.");
                        }
                        return;
                }
                if (text.length() < MIN_LENGTH) {
                        throw new BusinessException(fieldLabel + " phải có ít nhất " + MIN_LENGTH + " ký tự.");
                }
                if (text.length() > maxLength) {
                        throw new BusinessException(fieldLabel + " không được vượt quá " + maxLength + " ký tự.");
                }
                if (!ALLOWED_TEXT_PATTERN.matcher(text).matches()) {
                        throw new BusinessException(fieldLabel + " chỉ được chứa chữ cái, chữ số và khoảng trắng.");
                }
        }
}


