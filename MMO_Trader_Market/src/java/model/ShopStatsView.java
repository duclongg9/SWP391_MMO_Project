package model;

import java.sql.Timestamp;

/**
 * DTO (Data Transfer Object) chứa thông tin shop kèm các thống kê đã được aggregate.
 * Dùng để hiển thị danh sách shop của seller với các số liệu: số sản phẩm, lượng bán, tồn kho.
 * Không phải entity từ database, mà là view object được tạo từ query JOIN.
 */
public class ShopStatsView {
	private int id;
	private String name;
	private String description;
	private String status;
        private Timestamp createdAt;
        private Timestamp updatedAt;
	private int productCount;
	private int totalSold;
	private int totalInventory;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

	public int getProductCount() { return productCount; }
	public void setProductCount(int productCount) { this.productCount = productCount; }

	public int getTotalSold() { return totalSold; }
	public void setTotalSold(int totalSold) { this.totalSold = totalSold; }

	public int getTotalInventory() { return totalInventory; }
	public void setTotalInventory(int totalInventory) { this.totalInventory = totalInventory; }
}


