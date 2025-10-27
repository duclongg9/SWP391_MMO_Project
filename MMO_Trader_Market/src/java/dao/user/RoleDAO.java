/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
import model.Roles;

import javax.management.relation.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author D E L L
 */
public class RoleDAO {

    private static final String COL_ID = "id";
    private static final String COL_ROLE = "name";

    /*Hàm lấy role theo role id */
    public Roles getRoleById(int id) throws SQLException {
        String sql = """
                     SELECT * FROM roles
                     WHERE id = ?
                     LIMIT 1
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Roles role = new Roles();
                    role.setId(rs.getInt(COL_ID));
                    role.setName(rs.getString(COL_ROLE));
                    return role;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }

    /**
     * Tìm kiếm mã định danh (ID) của một vai trò dựa trên tên duy nhất của nó.
     */
    public Integer getRoleIdByName(String roleName) {
        String sql = """
                     SELECT id FROM roles
                     WHERE name = ?
                     LIMIT 1
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(COL_ID);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }
}
