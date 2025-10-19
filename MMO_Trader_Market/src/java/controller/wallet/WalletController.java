package controller.wallet;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hiển thị ví điện tử dành cho người mua.
 */
@WebServlet(name = "WalletController", urlPatterns = {"/wallet"})
public class WalletController extends BaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Ví điện tử - MMO Trader Market");
        request.setAttribute("bodyClass", "layout");
//        request.setAttribute("headerTitle", "Ví điện tử");
//        request.setAttribute("headerSubtitle", "Theo dõi số dư và giao dịch gần đây");
        request.setAttribute("headerModifier", "layout__header--split");

        request.setAttribute("walletBalance", new BigDecimal("1250000"));
        request.setAttribute("walletHold", new BigDecimal("250000"));
        request.setAttribute("walletCurrency", "VND");
        request.setAttribute("walletStatus", "Đang hoạt động");
        request.setAttribute("walletUpdatedAt", "12/05/2024 09:30");

        request.setAttribute("transactions", buildTransactions());
        request.setAttribute("shortcuts", buildShortcuts(request.getContextPath()));

        forward(request, response, "wallet/overview");
    }

    private List<Map<String, String>> buildTransactions() {
        List<Map<String, String>> transactions = new ArrayList<>();

        transactions.add(createTransaction("Nạp tiền Momo", "+500.000 đ", "Hoàn tất", "12/05/2024 09:30"));
        transactions.add(createTransaction("Thanh toán đơn #1024", "-320.000 đ", "Hoàn tất", "08/05/2024 21:10"));
        transactions.add(createTransaction("Hoàn tiền đơn #1008", "+320.000 đ", "Đã hoàn", "02/05/2024 14:20"));
        transactions.add(createTransaction("Rút về ngân hàng", "-1.000.000 đ", "Đang xử lý", "28/04/2024 08:45"));

        return transactions;
    }

    private List<Map<String, String>> buildShortcuts(String contextPath) {
        List<Map<String, String>> shortcuts = new ArrayList<>();

        shortcuts.add(createShortcut(contextPath + "/orders", "Đơn đã mua", "Theo dõi lịch sử giao dịch mua", "🧾"));
        shortcuts.add(createShortcut(contextPath + "/products", "Sản phẩm", "Khám phá thêm sản phẩm mới", "🛒"));
        shortcuts.add(createShortcut(contextPath + "/profile", "Tài khoản", "Cập nhật thông tin bảo mật", "🔐"));

        return shortcuts;
    }

    private Map<String, String> createTransaction(String title, String amount, String status, String time) {
        Map<String, String> transaction = new HashMap<>();
        transaction.put("title", title);
        transaction.put("amount", amount);
        transaction.put("status", status);
        transaction.put("time", time);
        return transaction;
    }

    private Map<String, String> createShortcut(String href, String title, String description, String icon) {
        Map<String, String> shortcut = new HashMap<>();
        shortcut.put("href", href);
        shortcut.put("title", title);
        shortcut.put("description", description);
        shortcut.put("icon", icon);
        return shortcut;
    }
}
