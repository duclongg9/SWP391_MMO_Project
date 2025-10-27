package dao.admin;

import model.CashTxn;

import java.sql.*;
import java.util.*;

public class CashDAO {

    private final Connection con;

    public CashDAO(Connection con) {
        this.con = con;
    }

    /**
     * Lấy giao dịch nạp+rút, kèm tên user
     */
    public List<CashTxn> listAll() throws SQLException {

        String sql = """
            SELECT * FROM (
                SELECT
                    'Deposit'  AS type,
                    dr.id, dr.user_id,
                    u.name     AS user_name,
                    dr.amount, dr.status, dr.created_at,
                    NULL       AS processed_at,
                    -- fields riêng
                    NULL       AS bank_account_info,
                    NULL       AS admin_proof_url,
                    dr.qr_content,
                    dr.idempotency_key,
                    dr.admin_note
                FROM mmo_schema.deposit_requests dr
                LEFT JOIN mmo_schema.users u ON u.id = dr.user_id

                UNION ALL

                SELECT
                    'Withdrawal' AS type,
                    wr.id, wr.user_id,
                    u.name       AS user_name,
                    wr.amount, wr.status, wr.created_at,
                    wr.processed_at,
                    -- fields riêng
                    wr.bank_account_info,
                    wr.admin_proof_url,
                    NULL        AS qr_content,
                    NULL        AS idempotency_key,
                    NULL        AS admin_note
                FROM mmo_schema.withdrawal_requests wr
                LEFT JOIN mmo_schema.users u ON u.id = wr.user_id
            ) x
            ORDER BY x.created_at DESC
        """;

        List<CashTxn> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CashTxn t = new CashTxn();
                t.setType(rs.getString("type"));                 // Deposit | Withdrawal
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setUserName(rs.getString("user_name"));        // <= tên user
                t.setAmount(rs.getBigDecimal("amount"));
                t.setStatus(rs.getString("status"));
                Timestamp cAt = rs.getTimestamp("created_at");
                Timestamp pAt = rs.getTimestamp("processed_at");
                t.setCreatedAt(cAt == null ? null : new java.util.Date(cAt.getTime()));
                t.setProcessedAt(pAt == null ? null : new java.util.Date(pAt.getTime()));

                t.setBankAccountInfo(rs.getString("bank_account_info"));
                t.setAdminProofUrl(rs.getString("admin_proof_url"));
                t.setQrContent(rs.getString("qr_content"));
                t.setIdempotencyKey(rs.getString("idempotency_key"));
                t.setAdminNote(rs.getString("admin_note"));
                list.add(t);
            }
        }
        System.out.println("tx count = " + list.size());
        return list;
    }
}
