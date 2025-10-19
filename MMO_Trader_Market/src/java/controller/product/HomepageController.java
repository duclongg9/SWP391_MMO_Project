package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles requests for the public facing homepage where visitors discover
 * available account products.
 */
@WebServlet(name = "HomepageController", urlPatterns = {"/home"})
public class HomepageController extends BaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Chợ tài khoản MMO - Trang chủ");
        request.setAttribute("bodyClass", "layout layout--landing");
        request.setAttribute("headerTitle", "MMO Trader Market");
        request.setAttribute("headerSubtitle", "Nền tảng demo mua bán tài khoản an toàn");

        String contextPath = request.getContextPath();
        List<Map<String, String>> navItems = new ArrayList<>();

        Map<String, String> loginLink = new HashMap<>();
        loginLink.put("href", contextPath + "/login.jsp");
        loginLink.put("label", "Đăng nhập");
        loginLink.put("modifier", "button button--primary");
        navItems.add(loginLink);

        Map<String, String> productLink = new HashMap<>();
        productLink.put("href", contextPath + "/products");
        productLink.put("label", "Quản lý sản phẩm");
        navItems.add(productLink);

        Map<String, String> guideLink = new HashMap<>();
        guideLink.put("href", contextPath + "/styleguide");
        guideLink.put("label", "Styleguide");
        navItems.add(guideLink);

        request.setAttribute("navItems", navItems);

        forward(request, response, "product/home");
    }
}
