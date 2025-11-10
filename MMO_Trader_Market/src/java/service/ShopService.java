package service;

import dao.shop.ShopDAO;
import java.sql.SQLException;
import java.util.List;
import model.ShopStatsView;
import model.Shops;

/**
 * Service xử lý logic nghiệp vụ liên quan đến quản lý shop cho seller.
 * Bao gồm: tạo, cập nhật, ẩn/khôi phục shop, và liệt kê shop kèm thống kê.
 */
public class ShopService {

	private final ShopDAO shopDAO = new ShopDAO();

	/**
	 * Tạo shop mới cho seller.
	 * Kiểm tra giới hạn tối đa 5 shop, validate tên shop (3-255 ký tự, không chỉ khoảng trắng),
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
		String normalizedName = name == null ? "" : name.trim();
		if (normalizedName.isEmpty() || normalizedName.length() < 3 || normalizedName.length() > 255) {
			throw new BusinessException("Tên shop phải từ 3 đến 255 ký tự và không được chỉ chứa khoảng trắng.");
		}
		// Chuẩn hóa mô tả (trim nếu không null)
		String desc = description == null ? null : description.trim();
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
		String normalizedName = name == null ? "" : name.trim();
		if (normalizedName.isEmpty() || normalizedName.length() < 3 || normalizedName.length() > 255) {
			throw new BusinessException("Tên shop phải từ 3 đến 255 ký tự và không được chỉ chứa khoảng trắng.");
		}
		String desc = description == null ? null : description.trim();
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
	 * Hỗ trợ sắp xếp theo lượng bán, ngày tạo, hoặc tên.
	 *
	 * @param ownerId ID của seller
	 * @param sortBy Cách sắp xếp: 'sales_desc', 'created_desc', 'name_asc', hoặc null (mặc định sales_desc)
	 * @return Danh sách ShopStatsView chứa thông tin shop và thống kê
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
	public List<ShopStatsView> listMyShops(int ownerId, String sortBy) throws SQLException {
		return shopDAO.findByOwnerWithStats(ownerId, sortBy);
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
}


