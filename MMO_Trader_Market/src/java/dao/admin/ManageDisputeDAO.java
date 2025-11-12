package dao;

import model.Disputes;
import model.DisputeAttachment;

import java.sql.*;
import java.util.*;

public class ManageDisputeDAO {

    private final Connection con;

    public ManageDisputeDAO(Connection con) {
        this.con = con;
    }

    public List<Disputes> search(String q,
                                 String status,
                                 String issueType,
                                 Timestamp from,
                                 Timestamp to) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT d.*, " +
                        "       u.name  AS reporter_name, " +
                        "       u.email AS reporter_email " +
                        "FROM disputes d " +
                        "LEFT JOIN users u ON d.reporter_id = u.id " +
                        "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Tìm kiếm theo từ khóa: mã đơn / email / tên user
        if (q != null && !q.isBlank()) {
            sql.append(" AND (d.order_reference_code LIKE ? " +
                    "      OR u.email LIKE ? " +
                    "      OR u.name LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // Filter status (bỏ qua nếu all/null)
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            sql.append(" AND d.status = ? ");
            params.add(status);
        }

        // Filter issueType (bỏ qua nếu all/null)
        if (issueType != null && !issueType.isBlank() && !"all".equalsIgnoreCase(issueType)) {
            sql.append(" AND d.issue_type = ? ");
            params.add(issueType);
        }

        // Date range
        if (from != null) {
            sql.append(" AND d.created_at >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND d.created_at <= ? ");
            params.add(to);
        }

        sql.append(" ORDER BY d.created_at DESC ");

        Map<Integer, Disputes> map = new LinkedHashMap<>();

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Disputes d = map.get(id);
                    if (d == null) {
                        d = new Disputes();
                        d.setId(id);
                        d.setOrderId((Integer) rs.getObject("order_id"));
                        d.setOrderReferenceCode(rs.getString("order_reference_code"));
                        d.setReporterId((Integer) rs.getObject("reporter_id"));
                        d.setResolvedByAdminId((Integer) rs.getObject("resolved_by_admin_id"));
                        d.setIssueType(rs.getString("issue_type"));
                        d.setCustomIssueTitle(rs.getString("custom_issue_title"));
                        d.setReason(rs.getString("reason"));
                        d.setStatus(rs.getString("status"));

                        // map đúng tên cột
                        d.setEscrowPausedAt(rs.getTimestamp("escrow_paused_at"));
                        d.setResolutionNote(rs.getString("resolution_note"));
                        d.setCreatedAt(rs.getTimestamp("created_at"));
                        d.setUpdatedAt(rs.getTimestamp("updated_at"));
                        d.setOrderSnapshotJson(rs.getString("order_snapshot_json"));

                        // từ LEFT JOIN users
                        d.setReporterName(rs.getString("reporter_name"));
                        d.setReporterEmail(rs.getString("reporter_email"));

                        map.put(id, d);
                    }
                }
            }
        }

        if (map.isEmpty()) {
            return new ArrayList<>();
        }

        // load ảnh đính kèm cho tất cả disputes
        loadAttachmentsFor(new ArrayList<>(map.values()));

        return new ArrayList<>(map.values());
    }

    private void loadAttachmentsFor(List<Disputes> disputes) throws SQLException {
        if (disputes == null || disputes.isEmpty()) return;

        Map<Integer, Disputes> byId = new HashMap<>();
        StringBuilder in = new StringBuilder();

        for (Disputes d : disputes) {
            Integer id = d.getId();
            if (id != null) {
                byId.put(id, d);
                if (in.length() > 0) in.append(',');
                in.append(id);
            }
        }

        if (byId.isEmpty()) return;

        String sql =
                "SELECT id, dispute_id, file_url, created_at " +
                        "FROM dispute_attachments " +
                        "WHERE dispute_id IN (" + in + ") " +
                        "ORDER BY created_at ASC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int disputeId = rs.getInt("dispute_id");
                Disputes d = byId.get(disputeId);
                if (d == null) continue;

                DisputeAttachment att = new DisputeAttachment();
                att.setId(rs.getInt("id"));
                att.setDisputeId(disputeId);
                att.setFilePath(rs.getString("file_url"));
                att.setCreatedAt(rs.getTimestamp("created_at"));

                d.addAttachment(att);
            }
        }
    }
}
