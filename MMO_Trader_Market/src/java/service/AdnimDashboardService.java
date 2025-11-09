/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.order.OrderDAO;
import dao.shop.ShopDAO;
import dao.user.DepositeRequestDAO;
import dao.user.WithdrawRequestDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author D E L L
 */
public class AdnimDashboardService {

    DepositeRequestDAO drdao = new DepositeRequestDAO();
    WithdrawRequestDAO wddao = new WithdrawRequestDAO();
    ShopDAO sdao = new ShopDAO();
    OrderDAO odao = new OrderDAO();

    //hàm lấy tổng tiền nạp
    public BigDecimal totalDeposite(int month, int year) throws SQLException {
        //vadidate
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ! (1–12)");
        }
        if (year < 2000 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Năm không hợp lệ hoặc vượt quá hiện tại!");
        }
        return drdao.getTotalDepositByMonth(month, year);
    }

    //hàm lấy tổng tiền rút
    public BigDecimal totalWithdraw(int month, int year) throws SQLException {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ! (1–12)");
        }
        if (year < 2000 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Năm không hợp lệ hoặc vượt quá hiện tại!");
        }
        return wddao.getTotalWithdrawByMonth(month, year);
    }

    //hàm lấy tổng số cửa hàng đang hoạt động
    public int totalShop() {
        return sdao.getTotalActiveShops();
    }

    //hàm lấy tổng số đơn theo tháng
    public int totalOrder(int month, int year) {
        // Validate cơ bản
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ (1–12)");
        }
        if (year < 2000 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Năm không hợp lệ hoặc vượt quá hiện tại");
        }
        
        return odao.gettotalOrderByMonth(month, year);
    }
    
    //Hàm lấy năm có chứa dữ liệu
    public List<Integer> getAvailableYears() throws SQLException{
        return drdao.getAvailableYears();
    }
}
