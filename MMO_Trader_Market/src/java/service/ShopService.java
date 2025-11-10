package service;

import dao.order.OrderDAO;
import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import model.ShopStatsView;
import model.Shops;
import model.statistics.BestSellerProduct;
import model.statistics.QuarterRevenue;
import java.util.regex.Pattern;

/**
 * Service xử lý logic nghiệp vụ liên quan đến quản lý shop cho seller.
 * Bao gồm: tạo, cập nhật, ẩn/khôi phục shop, và liệt kê shop kèm thống kê.
 */
public class ShopService {

        private static final Pattern BASIC_TEXT_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s.,-]+$");
        private static final int NAME_MIN_LENGTH = 8;
        private static final int NAME_MAX_LENGTH = 50;
        private static final int DESCRIPTION_MIN_LENGTH = 8;
        private static final int DESCRIPTION_MAX_LENGTH = 50;

        private final ShopDAO shopDAO = new ShopDAO();
        private final ProductDAO productDAO = new ProductDAO();
        private final OrderDAO orderDAO = new OrderDAO();

	/**
	 * Tạo shop mới cho seller.
         * Kiểm tra giới hạn tối đa 5 shop, validate tên shop (8-50 ký tự, không chỉ khoảng trắng),
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
		// Chuẩn hóa và validate tên shop
                String normalizedName = normalize(name);
                ensureValidText(normalizedName, "Tên shop", NAME_MIN_LENGTH, NAME_MAX_LENGTH);
                String desc = normalize(description);
                ensureValidText(desc, "Mô tả shop", DESCRIPTION_MIN_LENGTH, DESCRIPTION_MAX_LENGTH);
                // Gọi DAO để tạo shop (status = 'Active', created_at = NOW())
                shopDAO.create(ownerId, normalizedName, desc);
        }

	/**
	 * Cập nhật tên và mô tả của shop.
	 * Validate tên giống như tạo mới, và kiểm tra quyền owner trước khi cập nhật.
	 *
	 * @param id ID của shop cần cập nhật
	 * @param ownerId ID của seller (để xác thực quyền)
	 * @param name Tên mới (sẽ được trim và validate)
	 * @param description Mô tả mới (tùy chọn, có thể null)
	 * @throws BusinessException nếu tên không hợp lệ hoặc không tìm thấy shop/không có quyền
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
	public void updateShop(int id, int ownerId, String name, String description) throws BusinessException, SQLException {
		// Chuẩn hóa và validate tên shop (giống như tạo mới)
                String normalizedName = normalize(name);
                ensureValidText(normalizedName, "Tên shop", NAME_MIN_LENGTH, NAME_MAX_LENGTH);
                String desc = normalize(description);
                ensureValidText(desc, "Mô tả shop", DESCRIPTION_MIN_LENGTH, DESCRIPTION_MAX_LENGTH);
                // Cập nhật shop (DAO sẽ kiểm tra owner_id trong WHERE clause)
                boolean ok = shopDAO.update(id, ownerId, normalizedName, desc);
                if (!ok) {
                        throw new BusinessException("Không tìm thấy shop hoặc bạn không có quyền.");
                }
	}

	/**
	 * Ẩn shop bằng cách đổi trạng thái sang 'Suspended'.
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
                productDAO.updateStatusByShop(id, "Unlisted", null);
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
                productDAO.updateStatusByShop(id, "Available", "Unlisted");
        }

	/**
	 * Lấy danh sách shop của seller kèm thống kê (số sản phẩm, lượng bán, tồn kho).
	 * Hỗ trợ sắp xếp theo lượng bán, ngày tạo, hoặc tên.
	 *
	 * @param ownerId ID của seller
	 * @param sortBy Cách sắp xếp: 'sales_desc', 'created_desc', 'name_asc', hoặc null (mặc định sales_desc)
	 * @return Danh sách ShopStatsView chứa thông tin shop và thống kê
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public List<ShopStatsView> listMyShops(int ownerId, String sortBy, String keyword) throws SQLException {
                String sanitizedKeyword = keyword == null ? null : keyword.trim();
                return shopDAO.findByOwnerWithStats(ownerId, sortBy, sanitizedKeyword);
        }

	/**
	 * Tìm shop theo ID và owner, dùng để kiểm tra quyền trước khi cho phép chỉnh sửa.
	 *
	 * @param id ID của shop
	 * @param ownerId ID của seller (để xác thực quyền)
	 * @return Optional chứa Shops nếu tìm thấy và thuộc về owner, Optional.empty() nếu không
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public Optional<Shops> findByIdAndOwner(int id, int ownerId) throws SQLException {
                return shopDAO.findByIdAndOwner(id, ownerId);
        }

        /**
         * Lấy chi tiết shop kèm thống kê cơ bản theo ID và owner.
         *
         * @param shopId  ID shop
         * @param ownerId ID chủ sở hữu
         * @return {@link Optional} chứa {@link ShopStatsView} nếu tìm thấy
         * @throws SQLException nếu truy vấn lỗi
         */
        public Optional<ShopStatsView> findDetailWithStats(int shopId, int ownerId) throws SQLException {
                return shopDAO.findDetailByIdAndOwner(shopId, ownerId);
        }

        /**
         * Lấy thống kê doanh thu theo quý cho shop trong các mốc 3/6/12 tháng.
         *
         * @param shopId      ID shop
         * @param rangeMonths số tháng cần thống kê (3,6,12)
         * @return danh sách doanh thu từng quý (luôn đủ số phần tử tương ứng)
         */
        public List<QuarterRevenue> getQuarterlyRevenue(int shopId, int rangeMonths) {
                int sanitizedMonths = switch (rangeMonths) {
                        case 6 -> 6;
                        case 12 -> 12;
                        default -> 3;
                };
                int quarters = sanitizedMonths / 3;

                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                int monthInQuarter = cal.get(java.util.Calendar.MONTH) % 3;
                cal.add(java.util.Calendar.MONTH, -monthInQuarter);

                java.util.Calendar start = (java.util.Calendar) cal.clone();
                start.add(java.util.Calendar.MONTH, -3 * (quarters - 1));

                java.sql.Timestamp startTimestamp = new java.sql.Timestamp(start.getTimeInMillis());
                List<QuarterRevenue> raw = orderDAO.getQuarterlyRevenue(shopId, startTimestamp);
                java.util.Map<String, QuarterRevenue> mapped = new java.util.LinkedHashMap<>();
                for (QuarterRevenue item : raw) {
                        String key = item.getYear() + "-" + item.getQuarter();
                        mapped.put(key, item);
                }

                List<QuarterRevenue> result = new java.util.ArrayList<>();
                java.util.Calendar cursor = (java.util.Calendar) start.clone();
                for (int i = 0; i < quarters; i++) {
                        int year = cursor.get(java.util.Calendar.YEAR);
                        int quarter = (cursor.get(java.util.Calendar.MONTH) / 3) + 1;
                        String key = year + "-" + quarter;
                        QuarterRevenue existing = mapped.get(key);
                        if (existing == null) {
                                existing = new QuarterRevenue(year, quarter, java.math.BigDecimal.ZERO);
                        }
                        result.add(existing);
                        cursor.add(java.util.Calendar.MONTH, 3);
                }
                return result;
        }

        /**
         * Tìm sản phẩm bán chạy nhất của shop.
         *
         * @param shopId ID shop
         * @return {@link Optional} chứa {@link BestSellerProduct} nếu có
         */
        public Optional<BestSellerProduct> findBestSellerProduct(int shopId) {
                return orderDAO.findBestSellingProduct(shopId);
        }

        /**
         * Chuẩn hoá chuỗi đầu vào bằng cách trim và thay thế nhiều khoảng trắng liên tiếp bằng một khoảng trắng đơn.
         *
         * @param value giá trị cần chuẩn hoá
         * @return chuỗi đã được chuẩn hoá (không null)
         */
        private String normalize(String value) {
                if (value == null) {
                        return "";
                }
                return value.trim().replaceAll("\\s+", " ");
        }

        /**
         * Kiểm tra chuỗi nhập liệu của shop theo yêu cầu nghiệp vụ của seller.
         * Ràng buộc: không rỗng, độ dài trong khoảng cho phép, không chứa ký tự đặc biệt.
         *
         * @param value chuỗi cần kiểm tra (đã normalize)
         * @param fieldLabel tên trường để hiển thị lỗi cho người dùng
         * @param minLength độ dài tối thiểu
         * @param maxLength độ dài tối đa
         * @throws BusinessException nếu dữ liệu không hợp lệ
         */
        private void ensureValidText(String value, String fieldLabel, int minLength, int maxLength) throws BusinessException {
                if (value == null || value.isBlank()) {
                        throw new BusinessException(fieldLabel + " không được để trống.");
                }
                int length = value.length();
                if (length < minLength) {
                        throw new BusinessException(fieldLabel + " phải có tối thiểu " + minLength + " ký tự.");
                }
                if (length > maxLength) {
                        throw new BusinessException(fieldLabel + " không được vượt quá " + maxLength + " ký tự.");
                }
                if (!BASIC_TEXT_PATTERN.matcher(value).matches()) {
                        throw new BusinessException(fieldLabel + " chỉ được chứa chữ cái, chữ số, khoảng trắng và các ký tự . , -.");
                }
        }
}


