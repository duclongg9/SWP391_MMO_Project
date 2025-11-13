package dao.user;

import model.CashTxn;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserCashDAO {

    private final Connection con;

    public UserCashDAO(Connection con) {
        this.con = con;
    }

    private CashTxn mapCashTxn(ResultSet rs) throws SQLException {
        CashTxn t = new CashTxn();
        t.setType(rs.getString("type"));
        t.setId(rs.getInt("id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setStatus(rs.getString("status"));

        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp pAt = rs.getTimestamp("processed_at");   // = updated_at của bạn

        if (cAt != null) t.setCreatedAt(new java.util.Date(cAt.getTime()));
        if (pAt != null) t.setProcessedAt(new java.util.Date(pAt.getTime())); // dùng như updatedAt

        t.setAdminNote(rs.getString("admin_note"));
        t.setAdminProofUrl(rs.getString("admin_url"));               // đúng getter/setter bạn đang dùng
        t.setBankAccountInfo(rs.getString("bank_account_info_image"));
        return t;
    }

    /** Lấy tất cả giao dịch của user (không filter) */
    public List<CashTxn> listUserTransactions(int userId) throws SQLException {
        String sql = """
        SELECT * FROM (
            SELECT
                'Deposit' AS type,
                dr.id,
                dr.user_id,
                dr.amount,
                dr.status,
                dr.created_at,
                NULL        AS processed_at,            -- deposit không có processed_at
                dr.admin_note,
                NULL        AS admin_url,               -- deposit không có URL chứng từ
                dr.qr_content AS bank_account_info_image
            FROM mmo_schema.deposit_requests dr
            WHERE dr.user_id = ?

            UNION ALL

            SELECT
                'Withdrawal' AS type,
                wr.id,
                wr.user_id,
                wr.amount,
                wr.status,
                wr.created_at,
                wr.processed_at AS processed_at,
                wr.admin_note,
                wr.admin_proof_url AS admin_url,        -- ĐÚNG TÊN CỘT
                wr.bank_account_info AS bank_account_info_image
            FROM mmo_schema.withdrawal_requests wr
            WHERE wr.user_id = ?
        ) x
        ORDER BY x.created_at DESC
        """;

        List<CashTxn> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCashTxn(rs));
            }
        }
        return list;
    }


    /** Lấy danh sách có filter */
    public List<CashTxn> listUserTransactionsFiltered(
            int userId, String type, String status,
            BigDecimal minAmount, BigDecimal maxAmount,
            Date from, Date to, String sort) throws SQLException {

        StringBuilder sql = new StringBuilder("""
        SELECT * FROM (
            SELECT
                'Deposit' AS type,
                dr.id,
                dr.user_id,
                dr.amount,
                dr.status,
                dr.created_at,
                NULL        AS processed_at,
                dr.admin_note,
                NULL        AS admin_url,
                dr.qr_content AS bank_account_info_image
            FROM mmo_schema.deposit_requests dr
            WHERE dr.user_id = ?

            UNION ALL

            SELECT
                'Withdrawal' AS type,
                wr.id,
                wr.user_id,
                wr.amount,
                wr.status,
                wr.created_at,
                wr.processed_at AS processed_at,
                wr.admin_note,
                wr.admin_proof_url AS admin_url,
                wr.bank_account_info AS bank_account_info_image
            FROM mmo_schema.withdrawal_requests wr
            WHERE wr.user_id = ?
        ) x
        WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);

        if (type != null && !"all".equalsIgnoreCase(type)) {
            sql.append(" AND x.type = ?");
            params.add(type);
        }
        if (status != null && !"all".equalsIgnoreCase(status)) {
            sql.append(" AND x.status = ?");
            params.add(status);
        }
        if (minAmount != null) { sql.append(" AND x.amount >= ?"); params.add(minAmount); }
        if (maxAmount != null) { sql.append(" AND x.amount <= ?"); params.add(maxAmount); }
        if (from != null)      { sql.append(" AND x.created_at >= ?"); params.add(from); }
        if (to != null)        { sql.append(" AND x.created_at <= ?"); params.add(to); }

        switch (sort != null ? sort : "date_desc") {
            case "date_asc"    -> sql.append(" ORDER BY x.created_at ASC");
            case "amount_asc"  -> sql.append(" ORDER BY x.amount ASC");
            case "amount_desc" -> sql.append(" ORDER BY x.amount DESC");
            default            -> sql.append(" ORDER BY x.created_at DESC");
        }

        List<CashTxn> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCashTxn(rs));
            }
        }
        return list;
    }

}
