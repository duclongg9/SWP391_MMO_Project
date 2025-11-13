package dao.system;

import dao.BaseDAO;
import model.SystemConfigs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO lấy cấu hình hệ thống dùng để hiển thị thông báo trên trang chủ.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class SystemConfigDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SystemConfigDAO.class.getName());

    /**
     * Lấy toàn bộ cấu hình hệ thống theo thứ tự hiển thị.
     *
     * @return danh sách cấu hình
     */
    public List<SystemConfigs> findAll() {
        final String sql = "SELECT id, config_key, config_value, description, created_at, updated_at "
                + "FROM system_configs ORDER BY id ASC";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            List<SystemConfigs> configs = new ArrayList<>();
            while (rs.next()) {
                configs.add(mapRow(rs));
            }
            return configs;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải cấu hình hệ thống", ex);
            return List.of();
        }
    }

    /**
     * Tìm kiếm giá trị cấu hình theo {@code config_key}.
     *
     * @param key khóa cấu hình cần tra cứu
     * @return {@link Optional} chứa giá trị nếu tìm thấy
     */
    public Optional<String> findValueByKey(String key) {
        final String sql = "SELECT config_value FROM system_configs WHERE config_key = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("config_value"));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tra cứu cấu hình hệ thống", ex);
        }
        return Optional.empty();
    }

    /**
     * Cập nhật (hoặc tạo mới) giá trị cấu hình tương ứng {@code config_key}.
     *
     * <p>Phương thức ưu tiên câu lệnh {@code UPDATE}. Nếu không có bản ghi nào bị
     * ảnh hưởng (không tồn tại khóa) thì fallback sang {@code INSERT}. Điều này
     * cho phép trang quản trị thêm cấu hình mới mà không cần thao tác tay trên
     * cơ sở dữ liệu.</p>
     *
     * @param key   khóa cấu hình cần cập nhật
     * @param value giá trị mới cần lưu
     * @return {@code true} nếu cập nhật/khởi tạo thành công
     */
    public boolean upsertValueByKey(String key, String value) {
        final String updateSql = "UPDATE system_configs SET config_value = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?";
        try (Connection connection = getConnection(); PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            updateStmt.setString(1, value);
            updateStmt.setString(2, key);
            int affected = updateStmt.executeUpdate();
            if (affected > 0) {
                return true;
            }
            final String insertSql = "INSERT INTO system_configs (config_key, config_value, created_at, updated_at) "
                    + "VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setString(1, key);
                insertStmt.setString(2, value);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật cấu hình hệ thống", ex);
            return false;
        }
    }

    /**
     * Ánh xạ dữ liệu cấu hình sang đối tượng {@link SystemConfigs}.
     */
    private SystemConfigs mapRow(ResultSet rs) throws SQLException {
        SystemConfigs config = new SystemConfigs();
        config.setId(rs.getInt("id"));
        config.setConfigKey(rs.getString("config_key"));
        config.setConfigValue(rs.getString("config_value"));
        config.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) {
            config.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        if (updatedAt != null) {
            config.setUpdatedAt(new java.util.Date(updatedAt.getTime()));
        }
        return config;
    }
}
