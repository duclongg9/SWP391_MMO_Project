package dao.user;

import dao.connect.DBConnect;
import model.Wallets;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
// import model.Users;  // KHÔNG cần
// import model.User;   // KHÔNG cần

public class WalletsDAO {

    // Nếu chỉ lưu userId (Integer) trong Wallets thì không cần UserDAO ở đây
    // private final UserDAO udao = new UserDAO();

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_BALANCE = "balance";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";

    public Wallets getWalletById(int id) {
        String sql = """
                SELECT * FROM wallets
                WHERE id = ?
                LIMIT 1
                """;
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Wallets wallet = new Wallets();
                    wallet.setId(rs.getInt(COL_ID));
                    wallet.setUserId(rs.getInt(COL_USER_ID));                // Integer
                    wallet.setBalance(rs.getBigDecimal(COL_BALANCE));        // BigDecimal
                    wallet.setStatus(rs.getBoolean(COL_STATUS));             // Boolean

                    Timestamp c = rs.getTimestamp(COL_CREATED_AT);           // java.sql.Timestamp extends java.util.Date
                    Timestamp u = rs.getTimestamp(COL_UPDATED_AT);
                    wallet.setCreatedAt(c);
                    wallet.setUpdatedAt(u);

                    return wallet;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletsDAO.class.getName()).log(Level.SEVERE, "Lỗi DB", e);
        }
        return null;
    }

    public Wallets getUserWallet(int userId) {
        String sql = """
                SELECT * FROM wallets
                WHERE user_id = ?
                LIMIT 1
                """;
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Wallets wallet = new Wallets();
                    wallet.setId(rs.getInt(COL_ID));
                    wallet.setUserId(rs.getInt(COL_USER_ID));                // Integer
                    wallet.setBalance(rs.getBigDecimal(COL_BALANCE));        // BigDecimal
                    wallet.setStatus(rs.getBoolean(COL_STATUS));             // Boolean

                    Timestamp c = rs.getTimestamp(COL_CREATED_AT);
                    Timestamp u = rs.getTimestamp(COL_UPDATED_AT);
wallet.setCreatedAt(c);
                    wallet.setUpdatedAt(u);

                    return wallet;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletsDAO.class.getName()).log(Level.SEVERE, "Lỗi DB", e);
        }
        return null;
    }
}