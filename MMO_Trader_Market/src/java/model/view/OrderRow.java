package model.view;



import java.math.BigDecimal;

import java.util.Date;

import java.util.Objects;



/**
 * View model dạng rút gọn cho từng dòng hiển thị tại bảng lịch sử đơn hàng.
 * <p>Được dựng từ truy vấn {@link dao.order.OrderDAO#findByBuyerPaged} để truyền thẳng tới
 * JSP {@code order/my.jsp} mà không cần convert thêm.</p>
 */
public final class OrderRow {



    private final int id;

    private final String productName;

    private final BigDecimal totalAmount;

    private final String status;

    private final Date createdAt;



    public OrderRow(int id, String productName, BigDecimal totalAmount, String status, Date createdAt) {

        this.id = id;

        this.productName = productName;

        this.totalAmount = totalAmount;

        this.status = status;

        this.createdAt = createdAt;

    }



    public int getId() {

        return id;

    }



    public String getProductName() {

        return productName;

    }



    public BigDecimal getTotalAmount() {

        return totalAmount;

    }



    public String getStatus() {

        return status;

    }



    public Date getCreatedAt() {

        return createdAt;

    }



    @Override

    public String toString() {

        return "OrderRow{" +

                "id=" + id +

                ", productName='" + productName + '\'' +

                ", totalAmount=" + totalAmount +

                ", status='" + status + '\'' +

                ", createdAt=" + createdAt +

                '}';

    }



    @Override

    public boolean equals(Object o) {

        if (this == o) {

            return true;

        }

        if (!(o instanceof OrderRow)) {

            return false;

        }

        OrderRow orderRow = (OrderRow) o;

        return id == orderRow.id

                && Objects.equals(productName, orderRow.productName)

                && Objects.equals(totalAmount, orderRow.totalAmount)

                && Objects.equals(status, orderRow.status)

                && Objects.equals(createdAt, orderRow.createdAt);

    }



    @Override

    public int hashCode() {

        return Objects.hash(id, productName, totalAmount, status, createdAt);

    }

}
