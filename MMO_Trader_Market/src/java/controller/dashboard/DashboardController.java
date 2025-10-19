package controller.dashboard;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.DashboardService;
import service.dto.DashboardOverview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller hiển thị bảng điều khiển với dữ liệu thực tế từ cơ sở dữ liệu.
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard"})
public class DashboardController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer ownerId = resolveOwnerId(request);
        if (ownerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        DashboardOverview overview = dashboardService.getOverview(ownerId);
        prepareLayout(request);

        request.setAttribute("totalProducts", overview.totalProducts());
        request.setAttribute("pendingOrders", overview.pendingOrders());
        request.setAttribute("monthlyRevenue", overview.monthlyRevenue());
        request.setAttribute("featuredProducts", overview.featuredProducts());

        forward(request, response, "dashboard/index");
    }

    private Integer resolveOwnerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId != null) {
            return ownerId;
        }
        return (Integer) session.getAttribute("userId");
    }

    private void prepareLayout(HttpServletRequest request) {
        request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerTitle", "Bảng điều khiển");
        request.setAttribute("headerSubtitle", "Tổng quan nhanh về thị trường của bạn");
        request.setAttribute("headerModifier", "layout__header--split");
        request.setAttribute("navItems", buildNavigation(request.getContextPath()));
    }

    private List<Map<String, String>> buildNavigation(String contextPath) {
        List<Map<String, String>> items = new ArrayList<>();
        items.add(createNavItem(contextPath + "/products", "Danh sách sản phẩm", null));
        items.add(createNavItem(contextPath + "/styleguide", "Thư viện giao diện", null));
        items.add(createNavItem(contextPath + "/auth?action=logout", "Đăng xuất", "menu__item--danger"));
        return items;
    }

    private Map<String, String> createNavItem(String href, String label, String modifier) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("href", href);
        item.put("label", label);
        if (modifier != null && !modifier.isBlank()) {
            item.put("modifier", modifier);
        }
        return item;
    }
}
