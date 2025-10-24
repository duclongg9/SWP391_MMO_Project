package dao.admin;

import model.KycRequests;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageKycDAO {
    private final Connection con;

    // Mapping mặc định (đổi nếu schema khác)
    public static final int ROLE_BUYER   = 3;
    public static final int ROLE_SELLER  = 2;
    public static final int KYC_PENDING  = 1;
    public static final int KYC_APPROVED = 2; // Thành công
    public static final int KYC_REJECTED = 3; // Từ chối

    public ManageKycDAO(Connection con) {
        this.con = con;
    }

    /** Danh sách KYC */
    public List<KycRequests> getAllKyc() throws SQLException {
        List<KycRequests> list = new ArrayList<>();
        String sql = """
            SELECT
                s.id, s.user_id, s.status_id,
                s.front_image_url, s.back_image_url, s.selfie_image_url,
                s.id_number, s.created_at, s.reviewed_at, s.admin_feedback,
                u.name  AS user_name,
                u.email AS user_email,
                krs.status_name AS status_name
            FROM kyc_requests s
            LEFT JOIN users u ON u.id = s.user_id
            LEFT JOIN kyc_request_statuses krs ON krs.id = s.status_id
            ORDER BY s.created_at DESC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KycRequests k = new KycRequests(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("status_id"),
                        rs.getString("front_image_url"),
                        rs.getString("back_image_url"),
                        rs.getString("selfie_image_url"),
                        rs.getString("id_number"),
                        rs.getTimestamp("created_at") != null ? new java.util.Date(rs.getTimestamp("created_at").getTime()) : null,
                        rs.getTimestamp("reviewed_at") != null ? new java.util.Date(rs.getTimestamp("reviewed_at").getTime()) : null,
                        rs.getString("admin_feedback")
                );
                k.setUserName(rs.getString("user_name"));
                k.setUserEmail(rs.getString("user_email"));
                k.setStatusName(rs.getString("status_name"));
                list.add(k);
            }
        }
        return list;
    }

    /** Lấy 1 KYC */
    public KycRequests findById(int id) throws SQLException {
        String sql = """
            SELECT
                kr.id, kr.user_id, kr.status_id,
                kr.front_image_url, kr.back_image_url, kr.selfie_image_url,
                kr.id_number, kr.created_at, kr.reviewed_at, kr.admin_feedback,
                u.name  AS user_name,
                u.email AS user_email,
                krs.status_name AS status_name
            FROM kyc_requests kr
            LEFT JOIN users u ON u.id = kr.user_id
            LEFT JOIN kyc_request_statuses krs ON krs.id = kr.status_id
            WHERE kr.id = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                KycRequests k = new KycRequests(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("status_id"),
                        rs.getString("front_image_url"),
                        rs.getString("back_image_url"),
                        rs.getString("selfie_image_url"),
                        rs.getString("id_number"),
                        rs.getTimestamp("created_at") != null ? new java.util.Date(rs.getTimestamp("created_at").getTime()) : null,
                        rs.getTimestamp("reviewed_at") != null ? new java.util.Date(rs.getTimestamp("reviewed_at").getTime()) : null,
                        rs.getString("admin_feedback")
                );
                k.setUserName(rs.getString("user_name"));
                k.setUserEmail(rs.getString("user_email"));
                k.setStatusName(rs.getString("status_name"));
                return k;
            }
        }
    }

    /** Lấy user_id bằng kyc_id */
    public Integer getUserIdByKycId(int kycId) throws SQLException {
        String sql = "SELECT user_id FROM kyc_requests WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, kycId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    /**
     * Transaction:
     *  - Luôn cập nhật trạng thái KYC (approved/rejected)
     *  - Nếu approve và promoteRoleId != null: NÂNG users.role_id (chỉ khi user đang là BUYER)
     */
    public int updateKycAndMaybePromoteUser(int kycId, int newStatusId,
                                            Integer promoteRoleId, String feedback)
            throws SQLException {

        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            // 1) Update trạng thái KYC
            int kRows;
            try (PreparedStatement ps = con.prepareStatement("""
            UPDATE kyc_requests
               SET status_id = ?, admin_feedback = ?, reviewed_at = NOW()
             WHERE id = ?
        """)) {
                ps.setInt(1, newStatusId);
                ps.setString(2, feedback);
                ps.setInt(3, kycId);
                kRows = ps.executeUpdate();
            }
            if (kRows == 0) { con.rollback(); return 0; }

            // 2) Approve thì set role = Seller (2) cho user của bản KYC này (không ràng buộc role hiện tại)
            if (promoteRoleId != null && newStatusId == KYC_APPROVED) {
                try (PreparedStatement ps = con.prepareStatement("""
                UPDATE users u
                JOIN kyc_requests kr ON kr.user_id = u.id
                   SET u.role_id = ?, u.updated_at = NOW()
                 WHERE kr.id = ?
            """)) {
                    ps.setInt(1, promoteRoleId);  // 2
                    ps.setInt(2, kycId);
                    ps.executeUpdate();
                }
            }

            con.commit();
            return kRows;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

}
