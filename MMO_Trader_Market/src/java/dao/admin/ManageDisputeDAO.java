package dao.admin;

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
            Timestamp to,
            Integer sellerId,
            Integer shopId,
            Integer productId) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT d.*, "
                + "       u.name  AS reporter_name, "
                + "       u.email AS reporter_email, "
                + "       o.quantity AS order_quantity, "
                + "       o.status   AS order_status, "
                + "       p.id       AS product_id, "
                + "       p.name     AS product_name, "
                + "       s.id       AS shop_id, "
                + "       s.name     AS shop_name "
                + "FROM disputes d "
                + "JOIN orders o   ON d.order_id = o.id "
                + "JOIN products p ON o.product_id = p.id "
                + "JOIN shops s    ON p.shop_id = s.id "
                + "LEFT JOIN users u ON d.reporter_id = u.id "
                + "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Tìm kiếm theo từ khóa: mã đơn / email / tên user
        if (q != null && !q.isBlank()) {
            sql.append(" AND (d.order_reference_code LIKE ? "
                    + "      OR u.email LIKE ? "
                    + "      OR u.name LIKE ? "
                    + "      OR p.name LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
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

        if (sellerId != null) {
            sql.append(" AND s.owner_id = ? ");
            params.add(sellerId);
        }

        if (shopId != null) {
            sql.append(" AND s.id = ? ");
            params.add(shopId);
        }

        if (productId != null) {
            sql.append(" AND p.id = ? ");
            params.add(productId);
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
                Set<String> columnLabels = extractColumnLabels(rs);
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
                        if (columnLabels.contains("order_snapshot_json")) {
                            d.setOrderSnapshotJson(rs.getString("order_snapshot_json"));
                        }

                        // từ LEFT JOIN users
                        d.setReporterName(rs.getString("reporter_name"));
                        d.setReporterEmail(rs.getString("reporter_email"));

                        if (columnLabels.contains("order_quantity")) {
                            d.setOrderQuantity((Integer) rs.getObject("order_quantity"));
                        }
                        if (columnLabels.contains("order_status")) {
                            d.setOrderStatus(rs.getString("order_status"));
                        }
                        if (columnLabels.contains("product_id")) {
                            d.setProductId((Integer) rs.getObject("product_id"));
                        }
                        if (columnLabels.contains("product_name")) {
                            d.setProductName(rs.getString("product_name"));
                        }
                        if (columnLabels.contains("shop_id")) {
                            d.setShopId((Integer) rs.getObject("shop_id"));
                        }
                        if (columnLabels.contains("shop_name")) {
                            d.setShopName(rs.getString("shop_name"));
                        }

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
        if (disputes == null || disputes.isEmpty()) {
            return;
        }

        Map<Integer, Disputes> byId = new HashMap<>();
        StringBuilder in = new StringBuilder();

        for (Disputes d : disputes) {
            Integer id = d.getId();
            if (id != null) {
                byId.put(id, d);
                if (in.length() > 0) {
                    in.append(',');
                }
                in.append(id);
            }
        }

        if (byId.isEmpty()) {
            return;
        }

        String sql
                = "SELECT id, dispute_id, file_path, created_at "
                + "FROM dispute_attachments "
                + "WHERE dispute_id IN (" + in + ") "
                + "ORDER BY created_at ASC";

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int disputeId = rs.getInt("dispute_id");
                Disputes d = byId.get(disputeId);
                if (d == null) {
                    continue;
                }

                DisputeAttachment att = new DisputeAttachment();
                att.setId(rs.getInt("id"));
                att.setDisputeId(disputeId);
                att.setFilePath(rs.getString("file_path"));
                att.setCreatedAt(rs.getTimestamp("created_at"));

                d.addAttachment(att);
            }
        }
    }

    /**
     * Thu thập danh sách tên cột (column label) của ResultSet hiện tại nhằm
     * tránh lỗi khi truy cập những cột có thể không tồn tại trên một số phiên
     * bản cơ sở dữ liệu.
     *
     * @param rs ResultSet cần lấy metadata.
     * @return Tập hợp tên cột viết thường để tiện tra cứu.
     * @throws SQLException nếu không lấy được metadata của ResultSet.
     */
    private Set<String> extractColumnLabels(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Set<String> labels = new HashSet<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            labels.add(metaData.getColumnLabel(i).toLowerCase());
        }
        return labels;
    }
}
