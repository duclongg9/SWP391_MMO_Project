/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.order.OrderDAO;
import dao.shop.ShopDAO;
import dao.user.DepositeRequestDAO;
import dao.user.UserDAO;
import dao.user.WithdrawRequestDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author D E L L
 */
public class AdnimDashboardService {
    
    UserDAO udao = new UserDAO();
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
    public int totalActiveShop() {
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
    
    public int totalUser(int month, int year) throws SQLException {
        // Validate cơ bản
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ (1–12)");
        }
        if (year < 2000 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Năm không hợp lệ hoặc vượt quá hiện tại");
        }

        return udao.getTotalUserByMonth(month, year);
    }

    //Hàm lấy năm có chứa dữ liệu
    public List<Integer> getAvailableYears() throws SQLException {
        return drdao.getAvailableYears();
    }

    // Hàm lấy tổng số tiền rút mỗi tháng
    public BigDecimal[] arrayWithdrawByMonth(int year) throws SQLException {
        BigDecimal[] arr = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            arr[i] = wddao.getTotalWithdrawByMonth(i + 1, year); // Lưu ý tháng bắt đầu từ 1
        }
        return arr;
    }
    
    //hàm lấy số lượng order mỗi tháng trong năm
    public int[] arrayOrderByMonth(int year) throws SQLException {
        int[] arr = new int[12];
        for (int i = 0; i < 12; i++) {
            arr[i] = odao.gettotalOrderByMonth(i + 1, year); // Lưu ý tháng bắt đầu từ 1
        }
        return arr;
    }

// Hàm lấy tổng tiền nạp mỗi tháng
    public BigDecimal[] arrayDepositByMonth(int year) throws SQLException {
        BigDecimal[] arr = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            arr[i] = drdao.getTotalDepositByMonth(i + 1, year);
        }
        return arr;
    }
    
    //Hàm lấy tổng số các cửa hàng theo trạng thái
    public int totalPendingShop(){
        return sdao.getTotalPendingShops();
    }
    
    public int totalSuspendedShop(){
        return sdao.getTotalSuspendedShops();
    }
    
    //hàm lấy số lượng user theo tháng
     public int[] arrayUserByMonth(int year) throws SQLException {
        int[] arr = new int[12];
        for (int i = 0; i < 12; i++) {
            arr[i] = udao.getTotalUserByMonth(i + 1, year); // Lưu ý tháng bắt đầu từ 1
        }
        return arr;
    }
     
     public int totalActiveUser() throws SQLException{
         return udao.getTotalActive();
     }
}
