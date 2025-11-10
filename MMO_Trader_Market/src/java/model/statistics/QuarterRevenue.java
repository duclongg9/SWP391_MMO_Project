package model.statistics;

import java.math.BigDecimal;

/**
 * Đại diện cho tổng doanh thu của một cửa hàng theo từng quý.
 * <p>
 * View-model này được xây dựng để phục vụ màn hình chi tiết shop của seller,
 * nơi yêu cầu hiển thị doanh thu gom theo quý trong các mốc 3 - 6 - 12 tháng.
 * </p>
 */
public class QuarterRevenue {

    private final int year;

    private final int quarter;

    private BigDecimal revenue;

    /**
     * Khởi tạo đối tượng chứa thông tin doanh thu theo quý.
     *
     * @param year    năm của quý (ví dụ 2024)
     * @param quarter số thứ tự quý (1-4)
     * @param revenue tổng doanh thu của quý
     */
    public QuarterRevenue(int year, int quarter, BigDecimal revenue) {
        this.year = year;
        this.quarter = quarter;
        this.revenue = revenue;
    }

    public int getYear() {
        return year;
    }

    public int getQuarter() {
        return quarter;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    /**
     * @return Nhãn hiển thị dạng {@code Qx yyyy}.
     */
    public String getLabel() {
        return "Q" + quarter + ' ' + year;
    }
}

