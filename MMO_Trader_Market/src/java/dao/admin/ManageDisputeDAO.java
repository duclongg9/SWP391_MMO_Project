package dao.admin;

import model.Dispute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageDisputeDAO {

    private final Connection con;

    public ManageDisputeDAO(Connection con) {
        this.con = con;
    }

    /* Map 1 row -> Dispute (dựa trên alias ở dưới) */
    private Dispute map(ResultSet rs) throws SQLException {
        Dispute d = new Dispute();

        d.setId(rs.getInt("id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setOrderReferenceCode(rs.getString("order_reference_code"));

        d.setReporterId(rs.getInt("reporter_id"));
        d.setReporterName(rs.getString("reporter_name"));
        d.setReporterEmail(rs.getString("reporter_email"));

        int rba = rs.getInt("resolved_by_admin_id");
        if (!rs.wasNull()) {
            d.setResolvedByAdminId(rba);
        }
        d.setResolvedByAdminName(rs.getString("resolved_by_admin_name"));

        d.setIssueType(rs.getString("issue_type"));
        d.setCustomIssueTitle(rs.getString("custom_issue_title"));
        d.setReason(rs.getString("reason"));

        d.setOrderSnapshotJson(rs.getString("order_snapshot_json"));

        d.setStatus(rs.getString("status"));

        Timestamp ep = rs.getTimestamp("escrow_paused_at");
        if (ep != null) d.setEscrowPausedAt(new java.util.Date(ep.getTime()));

        Timestamp er = rs.getTimestamp("escrow_resolved_at");
        if (er != null) d.setEscrowResolvedAt(new java.util.Date(er.getTime()));

        d.setResolutionNote(rs.getString("resolution_note"));

        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (cAt != null) d.setCreatedAt(new java.util.Date(cAt.getTime()));
        if (uAt != null) d.setUpdatedAt(new java.util.Date(uAt.getTime()));

        return d;
    }

    /** Search có filter (dùng cho trang /admin/disputes) */
    public List<Dispute> search(String q,
                                String status,
                                String issueType,
                                Timestamp fromAt,
                                Timestamp toAt) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT
                d.*,
                u.name  AS reporter_name,
                u.email AS reporter_email,
                a.name  AS resolved_by_admin_name
            FROM mmo_schema.disputes d
            LEFT JOIN mmo_schema.users u ON d.reporter_id = u.id
            LEFT JOIN mmo_schema.users a ON d.resolved_by_admin_id = a.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" AND (d.order_reference_code LIKE ? OR u.email LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }

        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            sql.append(" AND d.status = ?");
            params.add(status);
        }

        if (issueType != null && !issueType.isBlank() && !"all".equalsIgnoreCase(issueType)) {
            sql.append(" AND d.issue_type = ?");
            params.add(issueType);
        }

        if (fromAt != null) {
            sql.append(" AND d.created_at >= ?");
            params.add(fromAt);
        }
        if (toAt != null) {
            sql.append(" AND d.created_at <= ?");
            params.add(toAt);
        }

        sql.append(" ORDER BY d.created_at DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Dispute> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    /** Lấy toàn bộ (nếu cần) */
    public List<Dispute> getAll() throws SQLException {
        String sql = """
            SELECT
                d.*,
                u.name  AS reporter_name,
                u.email AS reporter_email,
                a.name  AS resolved_by_admin_name
            FROM mmo_schema.disputes d
            LEFT JOIN mmo_schema.users u ON d.reporter_id = u.id
            LEFT JOIN mmo_schema.users a ON d.resolved_by_admin_id = a.id
            ORDER BY d.created_at DESC
            """;

        List<Dispute> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Cập nhật trạng thái + note khi admin xử lý */
    public int updateStatus(int id,
                            String newStatus,
                            String resolutionNote,
                            Integer adminId,
                            boolean setEscrowResolvedTime) throws SQLException {

        String sql = """
            UPDATE mmo_schema.disputes
            SET status = ?,
                resolution_note = ?,
                resolved_by_admin_id = ?,
                updated_at = NOW()
            """ + (setEscrowResolvedTime ? ", escrow_resolved_at = NOW()" : "") + """
            WHERE id = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, resolutionNote);
            if (adminId == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, adminId);
            }
            ps.setInt(4, id);
            return ps.executeUpdate();
        }
    }
}
