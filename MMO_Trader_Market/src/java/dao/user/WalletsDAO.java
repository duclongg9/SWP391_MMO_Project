package dao.user;

import dao.connect.DBConnect;
import model.Wallets;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WalletsDAO {

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
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
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
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletsDAO.class.getName()).log(Level.SEVERE, "Lỗi DB", e);
        }
        return null;
    }

    public Wallets ensureUserWallet(int userId) {
        Wallets existing = getUserWallet(userId);
        if (existing != null) {
            return existing;
        }

        String sql = """
                INSERT INTO wallets (user_id)
                VALUES (?)
                """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException dup) {
            // Ví đã được tạo bởi một luồng khác, bỏ qua và truy vấn lại.
        } catch (SQLException e) {
            Logger.getLogger(WalletsDAO.class.getName()).log(Level.SEVERE, "Lỗi DB", e);
            return null;
        }
        return getUserWallet(userId);
    }

    public Wallets lockWalletForUpdate(Connection connection, int userId) throws SQLException {
        // Sử dụng SELECT ... FOR UPDATE để khóa bản ghi ví trong suốt giao dịch.
        final String sql = "SELECT * FROM wallets WHERE user_id = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateBalance(Connection connection, int walletId, BigDecimal newBalance) throws SQLException {
        // Cập nhật số dư và mốc thời gian chỉnh sửa để phản ánh giao dịch mới nhất.
        final String sql = "UPDATE wallets SET balance = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, walletId);
            return ps.executeUpdate() > 0;
        }
    }

    private Wallets mapRow(ResultSet rs) throws SQLException {
        Wallets wallet = new Wallets();
        wallet.setId(rs.getInt(COL_ID));
        wallet.setUserId(rs.getInt(COL_USER_ID));
        wallet.setBalance(rs.getBigDecimal(COL_BALANCE));
        boolean statusValue = rs.getBoolean(COL_STATUS);
        wallet.setStatus(rs.wasNull() ? null : statusValue);
        Timestamp created = rs.getTimestamp(COL_CREATED_AT);
        if (created != null) {
            wallet.setCreatedAt(new Date(created.getTime()));
        }
        Timestamp updated = rs.getTimestamp(COL_UPDATED_AT);
        if (updated != null) {
            wallet.setUpdatedAt(new Date(updated.getTime()));
        }
        return wallet;
    }

    //Hàm lấy ra số dư trong ví
    public BigDecimal getWalletBalanceByUserId(int userId) {
        BigDecimal amount = null;
        String sql = """
                     SELECT balance FROM mmo_schema.wallets
                     WHERE user_id = ?
                     LIMIT 1
                     """;
        try(Connection con = DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) amount = rs.getBigDecimal(1);
            }
            return amount;
        } catch (SQLException e) {
            Logger.getLogger(WalletsDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }
    
    //Hàm trừ tiền trong ví
    public boolean decreaseBalance(int userId , BigDecimal balance) throws SQLException{
        BigDecimal presentBalance = getWalletBalanceByUserId(userId);
        boolean results = updateBalance(DBConnect.getConnection(), userId, (presentBalance.subtract(balance)));
        return results;
    }
    
    //Hàm cộng tiền trong ví
    public boolean increaseBalance(int userId , BigDecimal balance) throws SQLException{
        BigDecimal presentBalance = getWalletBalanceByUserId(userId);
        boolean results = updateBalance(DBConnect.getConnection(), userId, (presentBalance.add(balance)));
        return results;
    }
    
    public int createWallet(int userId) throws SQLException{
        String sql = """
                     INSERT INTO mmo_schema.wallets (user_id, status)
                     VALUES (?, 1);
                     """;
        try(Connection con = DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }
}
