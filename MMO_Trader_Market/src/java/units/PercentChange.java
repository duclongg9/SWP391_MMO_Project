/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package units;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author D E L L
 */
public class PercentChange {
    public static BigDecimal calculatePercentChange(BigDecimal current, BigDecimal lastMonth) {
        if (lastMonth == null || lastMonth.compareTo(BigDecimal.ZERO) == 0) {
            // Trường hợp tháng trước không có dữ liệu, mặc định tăng 100%
            return BigDecimal.valueOf(100);
        } else {
            return current.subtract(lastMonth)
                          .divide(lastMonth, 4, RoundingMode.HALF_UP) // chia 4 số thập phân
                          .multiply(BigDecimal.valueOf(100)); // nhân 100
        }
    }
}
