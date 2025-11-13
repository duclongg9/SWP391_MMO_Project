package dao.admin;

import model.CashTxn;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CashDAO {
    private final Connection con;

    public CashDAO(Connection con) {
        this.con = con;
        // --- CHỈNH MÚI GIỜ CHO PHIÊN LÀM VIỆC (UTC+7) ---
        // Đảm bảo mọi NOW(), TIMESTAMP trả/nhận theo giờ Việt Nam.
        try (Statement st = this.con.createStatement()) {
            // Cách 1: set trực tiếp offset
            st.execute("SET time_zone = '+07:00'");
            // (Tuỳ chọn) Cách 2: nếu server đã có timezone name
            // st.execute("SET time_zone = 'Asia/Ho_Chi_Minh'");
        } catch (SQLException ignored) {
            // Không phá vỡ luồng nếu DB không cho phép set; vẫn chạy bình thường
        }
    }

    /* Map ResultSet -> CashTxn (giữ như cũ) */
    private CashTxn mapCashTxn(ResultSet rs) throws SQLException {
        CashTxn t = new CashTxn();

        t.setType(rs.getString("type"));
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setUserName(rs.getString("user_name"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setStatus(rs.getString("status"));

        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp pAt = null;
        try { pAt = rs.getTimestamp("processed_at"); } catch (SQLException ignored) {}
        if (cAt != null) t.setCreatedAt(new java.util.Date(cAt.getTime()));
        if (pAt != null) t.setProcessedAt(new java.util.Date(pAt.getTime()));

        t.setQrContent(rs.getString("qr_content"));
        t.setBankAccountInfo(rs.getString("bank_account_info"));
        t.setAdminProofUrl(rs.getString("admin_proof_url"));
        t.setIdempotencyKey(rs.getString("idempotency_key"));
        t.setAdminNote(rs.getString("admin_note"));

        return t;
    }

    /** Dùng cho trang list /admin/cashs */
    public List<CashTxn> listAll() throws SQLException {
        String sql = """
        SELECT * FROM (
            -- DEPOSIT
            SELECT
                'Deposit'  AS type,
                dr.id,
                dr.user_id,
                u.name              AS user_name,
                dr.amount,
                dr.status,
                dr.created_at,
                /* alias để mapper đọc được */
                dr.expires_at       AS processed_at,
                NULL                AS bank_account_info,
                NULL                AS admin_proof_url,
                dr.qr_content       AS qr_content,
                dr.idempotency_key,
                dr.admin_note       AS admin_note
            FROM mmo_schema.deposit_requests dr
            LEFT JOIN mmo_schema.users u ON u.id = dr.user_id

            UNION ALL

            -- WITHDRAWAL
            SELECT
                'Withdrawal'        AS type,
                wr.id,
                wr.user_id,
                u.name              AS user_name,
                wr.amount,
                wr.status,
                wr.created_at,
                wr.processed_at     AS processed_at,
                wr.bank_account_info,
                wr.admin_proof_url,
                wr.bank_account_info AS qr_content,
                NULL                AS idempotency_key,
                wr.admin_note       AS admin_note
            FROM mmo_schema.withdrawal_requests wr
            LEFT JOIN mmo_schema.users u ON u.id = wr.user_id
        ) x
        ORDER BY x.created_at DESC
        """;

        List<CashTxn> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCashTxn(rs));
            }
        }
        return list;
    }

    /** Lấy 1 tx dùng cho handleCashsStatus (tự detect Deposit/Withdrawal) */
    public CashTxn findById(int txId, String txType) throws SQLException {
        if ("Deposit".equalsIgnoreCase(txType)) {
            String sql = """
    SELECT
        'Deposit'       AS type,
        dr.id,
        dr.user_id,
        u.name          AS user_name,
        dr.amount,
        dr.status,
        dr.created_at,
        dr.expires_at   AS processed_at,  -- giữ nguyên alias theo code hiện tại
        dr.qr_content   AS qr_content,
        NULL            AS bank_account_info,
        NULL            AS admin_proof_url,
        dr.idempotency_key,
        dr.admin_note   AS admin_note
    FROM mmo_schema.deposit_requests dr
    LEFT JOIN mmo_schema.users u ON u.id = dr.user_id
    WHERE dr.id = ?
    """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, txId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapCashTxn(rs);
                }
            }
        } else if ("Withdrawal".equalsIgnoreCase(txType)) {
            String sql = """
            SELECT
                'Withdrawal'        AS type,
                wr.id,
                wr.user_id,
                u.name              AS user_name,
                wr.amount,
                wr.status,
                wr.created_at,
                wr.processed_at     AS processed_at,
                NULL                AS qr_content,
                wr.bank_account_info,
                wr.admin_proof_url,
                NULL                AS idempotency_key,
                wr.admin_note       AS admin_note
            FROM mmo_schema.withdrawal_requests wr
            LEFT JOIN mmo_schema.users u ON u.id = wr.user_id
            WHERE wr.id = ?
            """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, txId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapCashTxn(rs);
                }
            }
        }
        return null;
    }

    /* ============ UPDATE STATUS THEO LOẠI ============ */

    public int updateDepositStatus(int id, String status, String note) throws SQLException {
        String sql = """
        UPDATE mmo_schema.deposit_requests
        SET status = ?, admin_note = ?
        WHERE id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    public int updateWithdrawalStatus(int txId, String status, String note, String proofUrl) throws SQLException {
        String sql = """
        UPDATE mmo_schema.withdrawal_requests
        SET status = ?,
            admin_note = ?,
            -- Giờ phiên đã là +07:00, dùng NOW() là đúng giờ VN
            processed_at = NOW(),
            admin_proof_url = COALESCE(?, admin_proof_url)
        WHERE id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setString(3, proofUrl);   // nếu null -> giữ nguyên
            ps.setInt(4, txId);
            return ps.executeUpdate();
        }
    }

    /* ============ WALLET (bảng mmo_schema.wallets) ============ */

    // + tiền ví
    public int updateWalletPlus(int userId, BigDecimal amount) throws SQLException {
        String sql = """
            UPDATE mmo_schema.wallets
            SET balance = balance + ?, updated_at = NOW()
            WHERE user_id = ? AND status = 1
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    public int updateWalletMinus(int userId, BigDecimal amount) throws SQLException {
        String sql = """
            UPDATE mmo_schema.wallets
            SET balance = balance - ?, updated_at = NOW()
            WHERE user_id = ? AND status = 1
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }
}
