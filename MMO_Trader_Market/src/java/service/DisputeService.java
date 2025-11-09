package service;

import dao.order.OrderDAO;
import dao.support.DisputeDAO;
import model.DisputeAttachment;
import model.Disputes;
import model.Orders;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Dịch vụ xử lý các nghiệp vụ báo cáo đơn hàng và quản lý dispute.
 */
public class DisputeService {

    private static final Map<String, String> ISSUE_TYPE_LABELS = buildIssueTypeLabels();

    private final DisputeDAO disputeDAO;
    private final OrderDAO orderDAO;

    public DisputeService() {
        this(new DisputeDAO(), new OrderDAO());
    }

    public DisputeService(DisputeDAO disputeDAO, OrderDAO orderDAO) {
        this.disputeDAO = Objects.requireNonNull(disputeDAO, "disputeDAO");
        this.orderDAO = Objects.requireNonNull(orderDAO, "orderDAO");
    }

    public Map<String, String> getIssueTypeLabels() {
        return Collections.unmodifiableMap(ISSUE_TYPE_LABELS);
    }

    public boolean isValidIssueType(String code) {
        return code != null && ISSUE_TYPE_LABELS.containsKey(code);
    }

    public String getIssueTypeLabel(String code) {
        return ISSUE_TYPE_LABELS.getOrDefault(code, code);
    }

    public Optional<Disputes> findByOrderId(int orderId) {
        return disputeDAO.findByOrderId(orderId);
    }

    public List<DisputeAttachment> getAttachments(int disputeId) {
        return disputeDAO.findAttachments(disputeId);
    }

    /**
     * Tạo báo cáo đơn hàng mới, đóng băng escrow và lưu ảnh bằng chứng trong một transaction.
     */
    public Disputes reportOrder(Orders order, int reporterId, String issueType, String customIssueTitle,
            String reason, List<DisputeAttachment> attachments) {
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không hợp lệ");
        }
        if (!"Completed".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể báo cáo những đơn đã hoàn tất");
        }
        if (!"Scheduled".equalsIgnoreCase(order.getEscrowStatus())) {
            throw new IllegalStateException("Đơn hàng hiện không nằm trong giai đoạn escrow để tạo báo cáo");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int remainingSeconds = computeRemainingEscrowSeconds(order, now);
        Disputes dispute = new Disputes();
        dispute.setOrderId(order.getId());
        dispute.setOrderReferenceCode(buildOrderReferenceCode(order.getId()));
        dispute.setReporterId(reporterId);
        dispute.setIssueType(issueType);
        dispute.setCustomIssueTitle(customIssueTitle);
        dispute.setReason(reason);
        dispute.setStatus("Open");
        dispute.setEscrowPausedAt(new Date(now.getTime()));
        dispute.setEscrowRemainingSeconds(remainingSeconds);

        List<DisputeAttachment> safeAttachments = attachments == null
                ? List.of()
                : new ArrayList<>(attachments);

        try (Connection connection = orderDAO.openConnection()) {
            connection.setAutoCommit(false);
            int disputeId = disputeDAO.insert(dispute, connection);
            dispute.setId(disputeId);
            disputeDAO.insertAttachments(disputeId, safeAttachments, connection);
            boolean updated = orderDAO.pauseEscrowForDispute(order.getId(), order.getBuyerId(), now, remainingSeconds,
                    order.getEscrowReleaseAt(), connection);
            if (!updated) {
                connection.rollback();
                throw new IllegalStateException("Không thể cập nhật trạng thái escrow của đơn hàng");
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo báo cáo đơn hàng", ex);
        }
        return dispute;
    }

    private static Map<String, String> buildIssueTypeLabels() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ACCOUNT_NOT_WORKING", "Lỗi tài khoản không sử dụng được");
        map.put("ACCOUNT_DUPLICATED", "Tài khoản trùng");
        map.put("ACCOUNT_EXPIRED", "Tài khoản hết hạn");
        map.put("ACCOUNT_MISSING", "Gửi thiếu tài khoản");
        map.put("OTHER", "Khác");
        return map;
    }

    private int computeRemainingEscrowSeconds(Orders order, Timestamp now) {
        Date releaseAt = order.getEscrowReleaseAt();
        if (releaseAt != null) {
            long diff = releaseAt.getTime() - now.getTime();
            if (diff > 0) {
                long seconds = diff / 1000L;
                return seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) seconds;
            }
            return 0;
        }
        Integer storedRemaining = order.getEscrowRemainingSeconds();
        if (storedRemaining != null && storedRemaining > 0) {
            return storedRemaining;
        }
        Integer holdSeconds = order.getEscrowHoldSeconds();
        if (holdSeconds != null && holdSeconds > 0) {
            return holdSeconds;
        }
        Date holdUntil = order.getHoldUntil();
        if (holdUntil != null) {
            long diff = holdUntil.getTime() - now.getTime();
            if (diff > 0) {
                long seconds = diff / 1000L;
                return seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) seconds;
            }
        }
        return 0;
    }

    private String buildOrderReferenceCode(int orderId) {
        return String.format("ORD-%06d", orderId);
    }
}
