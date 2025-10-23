package dao.admin;

import model.KycRequests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageKycDAO {
    private Connection con;
    public ManageKycDAO(Connection con) {
        this.con = con;
    }
    public List<KycRequests> getAllKyc() throws SQLException {
        List<KycRequests> list = new ArrayList<>();
        String sql =
                "SELECT s.id, s.user_id, s.status_id, s.front_image_url, s.back_image_url, " +
                        "s.selfie_image_url, s.id_number, s.created_at, s.reviewed_at, s.admin_feedback, " +
                        "u.name AS user_name, krs.status_name AS status_name " +
                        "FROM kyc_requests s " +
                        "LEFT JOIN users u ON u.id = s.user_id " +
                        "LEFT JOIN kyc_request_statuses krs ON krs.id = s.status_id " +
                        "ORDER BY s.created_at DESC";

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
                k.setStatusName(rs.getString("status_name"));
                list.add(k);
            }
        }
        return list;
    }

    public int updateKycStatus(int kycId, int statusId, String adminFeedback) throws SQLException {
        String sql = """
            UPDATE kyc_requests
            SET status_id = ?, admin_feedback = ?, reviewed_at = NOW()
            WHERE id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            ps.setString(2, adminFeedback);
            ps.setInt(3, kycId);
            return ps.executeUpdate();
        }
    }
    public KycRequests findById(int id) throws SQLException {
        String sql =
                "SELECT kr.id, kr.user_id, kr.status_id, kr.front_image_url, kr.back_image_url, " +
                        "kr.selfie_image_url, kr.id_number, kr.created_at, kr.reviewed_at, kr.admin_feedback, " +
                        "u.name AS user_name, krs.status_name AS status_name " +
                        "FROM kyc_requests kr " +
                        "LEFT JOIN users u ON u.id = kr.user_id " +
                        "LEFT JOIN kyc_request_statuses krs ON krs.id = kr.status_id " +
                        "WHERE kr.id = ?";

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
                k.setStatusName(rs.getString("status_name"));
                return k;
            }
        }
    }
}
