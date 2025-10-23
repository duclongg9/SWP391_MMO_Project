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
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
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

