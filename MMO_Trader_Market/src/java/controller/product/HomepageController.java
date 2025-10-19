package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Products;
import model.Shops;
import model.SystemConfigs;
import model.view.ConversationMessageView;
import model.view.CustomerProfileView;
import model.view.MarketplaceSummary;
import service.HomepageService;

import java.io.IOException;
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

    private final HomepageService homepageService = new HomepageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Chợ tài khoản MMO - Trang chủ");
        request.setAttribute("bodyClass", "layout layout--landing");
        request.setAttribute("headerTitle", "MMO Trader Market");
        request.setAttribute("headerSubtitle", "Nền tảng demo mua bán tài khoản an toàn");

        populateNavigation(request);
        populateHomepageData(request);

        forward(request, response, "product/home");
    }

    private void populateNavigation(HttpServletRequest request) {
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

        Map<String, String> orderLink = new HashMap<>();
        orderLink.put("href", contextPath + "/orders");
        orderLink.put("label", "Đơn đã mua");
        navItems.add(orderLink);

        Map<String, String> guideLink = new HashMap<>();
        guideLink.put("href", contextPath + "/styleguide");
        guideLink.put("label", "Styleguide");
        navItems.add(guideLink);

        request.setAttribute("navItems", navItems);
    }

    private void populateHomepageData(HttpServletRequest request) {
        MarketplaceSummary summary = homepageService.loadMarketplaceSummary();
        request.setAttribute("summary", summary);

        List<Products> featuredProducts = homepageService.loadFeaturedProducts();
        request.setAttribute("featuredProducts", featuredProducts);

        List<Shops> shops = homepageService.loadActiveShops();
        request.setAttribute("shops", shops);
        request.setAttribute("shopIcons", buildShopIconMap());

        CustomerProfileView profile = homepageService.loadHighlightedBuyer();
        request.setAttribute("customerProfile", profile);

        List<ConversationMessageView> messages = homepageService.loadRecentMessages();
        request.setAttribute("recentMessages", messages);

        List<SystemConfigs> systemNotes = homepageService.loadSystemNotes();
        request.setAttribute("systemNotes", systemNotes);
    }

    private Map<String, String> buildShopIconMap() {
        Map<String, String> icons = new HashMap<>();
        icons.put("Active", "🛍️");
        icons.put("Pending", "⏳");
        icons.put("Suspended", "⚠️");
        return icons;
    }
}
