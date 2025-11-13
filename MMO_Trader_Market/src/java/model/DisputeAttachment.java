package model;

import java.util.Date;

/**
 * Đại diện cho ảnh bằng chứng mà người mua tải lên khi báo cáo đơn hàng.
 */
public class DisputeAttachment {

    private Integer id;
    private Integer disputeId;
    private String filePath;
    private Date createdAt;

    public DisputeAttachment() {
    }

    public DisputeAttachment(Integer disputeId, String filePath) {
        this.disputeId = disputeId;
        this.filePath = filePath;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(Integer disputeId) {
        this.disputeId = disputeId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Chuẩn hóa đường dẫn để hiển thị trên web. Đảm bảo đường dẫn luôn bắt đầu
     * bằng dấu {@code '/'} và sử dụng dấu gạch chéo xuôi giúp trình duyệt đọc
     * đúng resource.
     *
     * @return đường dẫn ảnh sẵn sàng dùng cho thuộc tính {@code src}; trả về
     * {@code null} nếu giá trị rỗng.
     */
    public String getWebPath() {
        if (filePath == null) {
            return null;
        }
        String normalized = filePath.trim().replace('\\', '/');
        if (normalized.isEmpty()) {
            return null;
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        return normalized;
    }
}
