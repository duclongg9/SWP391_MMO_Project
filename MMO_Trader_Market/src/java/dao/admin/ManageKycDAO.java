package dao.admin;

import model.KycRequests;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageKycDAO {
    private final Connection con;

    public ManageKycDAO(Connection con) {
        this.con = con;
    }

    /** Lấy toàn bộ danh sách KYC (JOIN users bằng kr.id = u.id) */
    public List<KycRequests> getAllKycRequests() throws SQLException {
        List<KycRequests> list = new ArrayList<>();
        String sql = """
        SELECT
            kr.id,                                  -- id KYC (== id user)
            kr.user_id,                             -- ở DB này bạn dùng như role_id
            kr.status_id,
            kr.front_image_url, kr.back_image_url, kr.selfie_image_url,
            kr.id_number,
            kr.created_at, kr.reviewed_at, kr.admin_feedback,
            u.name  AS user_name,
            u.email AS user_email,
            krs.status_name AS status_name
        FROM mmo_schema.kyc_requests kr
        LEFT JOIN mmo_schema.users u  ON u.id = kr.id        -- << id user == id KYC
        LEFT JOIN mmo_schema.kyc_request_statuses krs ON krs.id = kr.status_id
        ORDER BY kr.created_at DESC
    """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int i = 0; // debug
            while (rs.next()) {
                KycRequests k = new KycRequests(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("status_id"),
                        rs.getString("front_image_url"),
                        rs.getString("back_image_url"),
                        rs.getString("selfie_image_url"),
                        rs.getString("id_number"),
                        rs.getTimestamp("created_at") == null ? null
                                : new java.util.Date(rs.getTimestamp("created_at").getTime()),
                        rs.getTimestamp("reviewed_at") == null ? null
                                : new java.util.Date(rs.getTimestamp("reviewed_at").getTime()),
                        rs.getString("admin_feedback")
                );
                k.setUserName(rs.getString("user_name"));
                k.setUserEmail(rs.getString("user_email"));
                k.setStatusName(rs.getString("status_name"));
                list.add(k);

                // ---- DEBUG: in 3 dòng đầu tiên ra console để chắc chắn dữ liệu đúng
                if (i++ < 3) {
                    System.out.printf("[KYC] id=%d, userName=%s, email=%s, idNumber=%s, status=%d%n",
                            rs.getInt("id"), rs.getString("user_name"),
                            rs.getString("user_email"), rs.getString("id_number"),
                            rs.getInt("status_id"));
                }
            }
        }
        return list;
    }



    /** Lấy 1 KYC theo id (JOIN users bằng kr.id = u.id) */
    public KycRequests findById(int id) throws SQLException {
        String sql = """
            SELECT
                kr.id, kr.user_id, kr.status_id,
                kr.front_image_url, kr.back_image_url, kr.selfie_image_url,
                kr.id_number, kr.created_at, kr.reviewed_at, kr.admin_feedback,
                u.name  AS user_name,
                u.email AS user_email,
                krs.status_name AS status_name
            FROM mmo_schema.kyc_requests kr
            LEFT JOIN mmo_schema.users u  ON u.id = kr.id        -- << ID KYC == ID USER
            LEFT JOIN mmo_schema.kyc_request_statuses krs ON krs.id = kr.status_id
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
                        rs.getTimestamp("created_at") == null ? null
                                : new java.util.Date(rs.getTimestamp("created_at").getTime()),
                        rs.getTimestamp("reviewed_at") == null ? null
                                : new java.util.Date(rs.getTimestamp("reviewed_at").getTime()),
                        rs.getString("admin_feedback")
                );
                k.setUserName(rs.getString("user_name"));
                k.setUserEmail(rs.getString("user_email"));
                k.setStatusName(rs.getString("status_name"));
                return k;
            }
        }
    }

    /** Trả về ID user ứng với 1 KYC: ở DB này user_id = role_id, id user chính là id KYC */
    public Integer getUserIdByKycId(int kycId) throws SQLException {
        // user.id == kyc_requests.id
        String sql = "SELECT id FROM mmo_schema.kyc_requests WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, kycId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    /** Approve + promote (KHÔNG đụng user_id) */
    public int approveKycAndPromote(int kycId, String feedback) throws SQLException {
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            // 1) KYC: status 1 -> 2 + user_id 3 -> 2
            int updatedKyc;
            try (PreparedStatement p = con.prepareStatement(
                    "UPDATE mmo_schema.kyc_requests " +
                            "SET status_id = 2, user_id = 2, reviewed_at = NOW(), admin_feedback = ? " +
                            "WHERE id = ? AND status_id = 1"
            )) {
                p.setString(1, feedback);
                p.setInt(2, kycId);
                updatedKyc = p.executeUpdate();
            }

            // 2) User (id == kycId): role 3 -> 2
            int updatedUser = 0;
            if (updatedKyc > 0) {
                try (PreparedStatement p = con.prepareStatement(
                        "UPDATE mmo_schema.users " +
                                "SET role_id = 2, updated_at = NOW() " +
                                "WHERE id = ? AND role_id = 3"
                )) {
                    p.setInt(1, kycId);
                    updatedUser = p.executeUpdate();
                }
            }

            con.commit();
            return updatedKyc + updatedUser;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

    /** Reject KYC */
    public int rejectKyc(int kycId, String feedback) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE mmo_schema.kyc_requests " +
                        "SET status_id=3, reviewed_at=NOW(), admin_feedback=? " +
                        "WHERE id=? AND status_id=1")) {
            ps.setString(1, feedback);
            ps.setInt(2, kycId);
            return ps.executeUpdate();
        }
    }
}
