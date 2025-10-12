package dao.user;

import dao.BaseDAO;
import dao.connect.DBConnect;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Role;
import java.sql.Timestamp;

/**
 * DAO responsible for user authentication. Replace the in-memory collection by
 * a JDBC implementation when integrating with the real database tables.
 */
public class UserDAO extends BaseDAO {

    /*Khai báo các model cần xử dụng liên quan*/
    RoleDAO rdao = new RoleDAO();

    /*Phần mapping database với các biến tự tạo trong java*/
    private static final String COL_ID = "id";
    private static final String COL_FULL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_ROLE = "role_id";
    private static final String COL_HASH_PASSWORD = "hashed_password";
    private static final String COL_GOOGLE_ID = "google_id";
    private static final String COL_AVATA = "avatar_url";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";
    
    /*Hàm lấy user theo userId*/
    public User getUserByUserId(int id) throws SQLException {
        String sql = """
                    SELECT * FROM  users
                    WHERE id = ? AND status = 1
                    LIMIT 1
                    """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                Role role = new Role();
                user.setId(rs.getInt(COL_ID));
                user.setUsername(rs.getString(COL_FULL_NAME));
                user.setEmail(rs.getString(COL_EMAIL));
                user.setRole(rdao.getRoleById(rs.getInt(COL_ROLE)));
                user.setHashPassword(rs.getString(COL_HASH_PASSWORD));
                user.setGoogleId(rs.getString(COL_GOOGLE_ID));
                user.setAvataUrl(rs.getString(COL_AVATA));
                user.setStatus(rs.getInt(COL_STATUS));

                //Lấy thời gian TimeStamp ở DB --> Instant trong java
                java.sql.Timestamp c = rs.getTimestamp(COL_CREATED_AT);
                java.sql.Timestamp u = rs.getTimestamp(COL_UPDATED_AT);
                user.setCreatedAt(c != null ? c.toInstant() : null);
                user.setUpdatedAt(u != null ? u.toInstant() : null);

                return user;
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }
    
    public int updateUserProfileBasic(int id,String name) throws SQLException{
        final String sql = """
                           Update users
                           Set name = ?, updated_at = CURRENT_TIMESTAMP
                           WHERE id = ? AND status = 1
                           """;
        try(Connection con = DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }
    
    public int updateUserPassword(int id,String hashedPassword) throws SQLException{
        final String sql = """
                           Update users
                           Set hashed_password = ?, updated_at = CURRENT_TIMESTAMP
                           WHERE id = ? AND status = 1
                           """;
        try(Connection con = DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }
    
   
}
