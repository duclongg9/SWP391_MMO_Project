package dao.support;

import dao.BaseDAO;
import model.DisputeAttachment;
import model.Disputes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tác với bảng {@code disputes} và {@code dispute_attachments}.
 */
public class DisputeDAO extends BaseDAO {

    /**
     * Lấy dispute theo {@code order_id} nếu tồn tại.
     */
    public Optional<Disputes> findByOrderId(int orderId) {
        final String sql = "SELECT id, order_id, order_reference_code, reporter_id, resolved_by_admin_id, issue_type, "
                + "custom_issue_title, reason, status, escrow_paused_at, escrow_remaining_seconds, resolved_at, resolution_note, "
                + "created_at, updated_at FROM disputes WHERE order_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDispute(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải thông tin báo cáo đơn hàng", ex);
        }
        return Optional.empty();
    }

    /**
     * Tìm kiếm dispute theo ID.
     *
     * @param disputeId mã khiếu nại cần tìm
     * @return {@link Optional} chứa bản ghi nếu tồn tại
     */
    public Optional<Disputes> findById(int disputeId) {
        try (Connection connection = getConnection()) {
            return findById(connection, disputeId, false);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải thông tin khiếu nại", ex);
        }
    }

    /**
     * Tìm kiếm dispute theo ID đồng thời khóa bản ghi để cập nhật trong transaction.
     *
     * @param connection kết nối hiện hành
     * @param disputeId  mã khiếu nại
     * @return {@link Optional} chứa bản ghi nếu tồn tại
     * @throws SQLException khi truy vấn lỗi
     */
    public Optional<Disputes> findByIdForUpdate(Connection connection, int disputeId) throws SQLException {
        return findById(connection, disputeId, true);
    }

    /**
     * Lấy danh sách ảnh bằng chứng gắn với dispute.
     */
    public List<DisputeAttachment> findAttachments(int disputeId) {
        final String sql = "SELECT id, dispute_id, file_path, created_at FROM dispute_attachments WHERE dispute_id = ?"
                + " ORDER BY id";
        List<DisputeAttachment> attachments = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, disputeId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapAttachment(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải ảnh bằng chứng", ex);
        }
        return attachments;
    }

    /**
     * Cập nhật trạng thái và ghi chú xử lý cho dispute.
     *
     * @param connection kết nối đang sử dụng
     * @param disputeId  mã khiếu nại cần cập nhật
     * @param status     trạng thái mới
     * @param adminId    admin xử lý
     * @param note       ghi chú xử lý
     * @param resolvedAt thời điểm hoàn tất (có thể null)
     * @return {@code true} nếu có bản ghi được cập nhật
     * @throws SQLException nếu thao tác SQL thất bại
     */
    public boolean updateStatus(Connection connection, int disputeId, String status, Integer adminId, String note,
            Timestamp resolvedAt) throws SQLException {
        final String sql = "UPDATE disputes SET status = ?, resolved_by_admin_id = ?, resolution_note = ?, resolved_at = ?, "
                + "updated_at = NOW() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            if (adminId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, adminId);
            }
            if (note == null || note.isBlank()) {
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(3, note);
            }
            if (resolvedAt == null) {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(4, resolvedAt);
            }
            statement.setInt(5, disputeId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Thêm dispute mới trong transaction sẵn có.
     */
    public int insert(Disputes dispute, Connection connection) throws SQLException {
        final String sql = "INSERT INTO disputes (order_id, order_reference_code, reporter_id, resolved_by_admin_id, issue_type,"
                + " custom_issue_title, reason, status, escrow_paused_at, escrow_remaining_seconds, resolved_at, resolution_note,"
                + " created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, dispute.getOrderId());
            statement.setString(2, dispute.getOrderReferenceCode());
            statement.setInt(3, dispute.getReporterId());
            if (dispute.getResolvedByAdminId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, dispute.getResolvedByAdminId());
            }
            statement.setString(5, dispute.getIssueType());
            if (dispute.getCustomIssueTitle() == null) {
                statement.setNull(6, java.sql.Types.VARCHAR);
            } else {
                statement.setString(6, dispute.getCustomIssueTitle());
            }
            statement.setString(7, dispute.getReason());
            statement.setString(8, dispute.getStatus());
            if (dispute.getEscrowPausedAt() == null) {
                statement.setNull(9, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(9, new Timestamp(dispute.getEscrowPausedAt().getTime()));
            }
            if (dispute.getEscrowRemainingSeconds() == null) {
                statement.setNull(10, java.sql.Types.INTEGER);
            } else {
                statement.setInt(10, dispute.getEscrowRemainingSeconds());
            }
            if (dispute.getResolvedAt() == null) {
                statement.setNull(11, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(11, new Timestamp(dispute.getResolvedAt().getTime()));
            }
            if (dispute.getResolutionNote() == null) {
                statement.setNull(12, java.sql.Types.VARCHAR);
            } else {
                statement.setString(12, dispute.getResolutionNote());
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Không thể tạo dispute mới");
    }

    /**
     * Thêm danh sách ảnh bằng chứng vào dispute trong transaction hiện tại.
     */
    public void insertAttachments(int disputeId, List<DisputeAttachment> attachments, Connection connection) throws SQLException {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        final String sql = "INSERT INTO dispute_attachments (dispute_id, file_path, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (DisputeAttachment attachment : attachments) {
                statement.setInt(1, disputeId);
                statement.setString(2, attachment.getFilePath());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private Disputes mapDispute(ResultSet rs) throws SQLException {
        Disputes dispute = new Disputes();
        dispute.setId(rs.getInt("id"));
        dispute.setOrderId(rs.getInt("order_id"));
        dispute.setOrderReferenceCode(rs.getString("order_reference_code"));
        dispute.setReporterId(rs.getInt("reporter_id"));
        int resolvedBy = rs.getInt("resolved_by_admin_id");
        if (!rs.wasNull()) {
            dispute.setResolvedByAdminId(resolvedBy);
        }
        dispute.setIssueType(rs.getString("issue_type"));
        dispute.setCustomIssueTitle(rs.getString("custom_issue_title"));
        dispute.setReason(rs.getString("reason"));
        dispute.setStatus(rs.getString("status"));
        Timestamp pausedAt = rs.getTimestamp("escrow_paused_at");
        if (pausedAt != null) {
            dispute.setEscrowPausedAt(new Date(pausedAt.getTime()));
        }
        int remaining = rs.getInt("escrow_remaining_seconds");
        if (!rs.wasNull()) {
            dispute.setEscrowRemainingSeconds(remaining);
        }
        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) {
            dispute.setResolvedAt(new Date(resolvedAt.getTime()));
        }
        dispute.setResolutionNote(rs.getString("resolution_note"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            dispute.setCreatedAt(new Date(created.getTime()));
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            dispute.setUpdatedAt(new Date(updated.getTime()));
        }
        return dispute;
    }

    private DisputeAttachment mapAttachment(ResultSet rs) throws SQLException {
        DisputeAttachment attachment = new DisputeAttachment();
        attachment.setId(rs.getInt("id"));
        attachment.setDisputeId(rs.getInt("dispute_id"));
        attachment.setFilePath(rs.getString("file_path"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            attachment.setCreatedAt(new Date(created.getTime()));
        }
        return attachment;
    }

    private Optional<Disputes> findById(Connection connection, int disputeId, boolean forUpdate) throws SQLException {
        String sql = "SELECT id, order_id, order_reference_code, reporter_id, resolved_by_admin_id, issue_type, "
                + "custom_issue_title, reason, status, escrow_paused_at, escrow_remaining_seconds, resolved_at, resolution_note, "
                + "created_at, updated_at FROM disputes WHERE id = ?";
        if (forUpdate) {
            sql += " FOR UPDATE";
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, disputeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDispute(rs));
                }
            }
        }
        return Optional.empty();
    }
}
