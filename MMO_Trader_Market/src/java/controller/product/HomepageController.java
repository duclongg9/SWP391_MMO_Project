package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Products;
import service.ProductService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final ProductService productService = new ProductService();

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
        request.setAttribute("categories", buildCategories());
        request.setAttribute("featuredProducts", loadFeaturedProducts());
        request.setAttribute("customerProfile", buildCustomerProfile());
        request.setAttribute("reviews", buildReviews());
        request.setAttribute("buyerTips", buildBuyerTips());
    }

    private List<Map<String, String>> buildCategories() {
        List<Map<String, String>> categories = new ArrayList<>();
        categories.add(createCategory("🎮", "Tài khoản game", "Nick game phổ biến, bảo hành rõ ràng"));
        categories.add(createCategory("📧", "Email doanh nghiệp", "Tên miền riêng cho doanh nghiệp vừa và nhỏ"));
        categories.add(createCategory("💼", "Phần mềm bản quyền", "Các gói Office, Windows chính hãng"));
        categories.add(createCategory("🛡️", "Bảo mật", "Công cụ bảo vệ tài khoản, 2FA"));
        return categories;
    }

    private Map<String, String> createCategory(String icon, String name, String description) {
        Map<String, String> category = new HashMap<>();
        category.put("icon", icon);
        category.put("name", name);
        category.put("description", description);
        return category;
    }

    private List<Products> loadFeaturedProducts() {
        return productService.homepageHighlights();
    }

    private Map<String, Object> buildCustomerProfile() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("displayName", "Nguyễn Minh Trí");
        profile.put("membershipLevel", "Thành viên hạng Platinum");
        profile.put("joinDate", LocalDate.now().minusYears(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        profile.put("successfulOrders", 128);
        profile.put("satisfactionScore", 4.9);
        return profile;
    }

    private List<Map<String, String>> buildReviews() {
        List<Map<String, String>> reviews = new ArrayList<>();
        reviews.add(createReview("Trần Anh", "5", "Giao key Netflix trong 2 phút, support nhiệt tình", "Netflix UHD 1 năm"));
        reviews.add(createReview("Lê Phương", "4.5", "Tài khoản Spotify dùng ổn định, giá hợp lý", "Spotify Premium 12 tháng"));
        reviews.add(createReview("Phạm Duy", "4.8", "Key Windows kích hoạt thành công ngay", "Windows 11 Pro key"));
        return reviews;
    }

    private Map<String, String> createReview(String reviewer, String rating, String comment, String productName) {
        Map<String, String> review = new HashMap<>();
        review.put("reviewerName", reviewer);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("productName", productName);
        return review;
    }

    private List<String> buildBuyerTips() {
        List<String> tips = new ArrayList<>();
        tips.add("Luôn đổi mật khẩu sau khi nhận tài khoản từ người bán");
        tips.add("Kích hoạt xác thực hai lớp ngay khi có thể");
        tips.add("Liên hệ hỗ trợ trong 24h nếu có vấn đề phát sinh");
        return tips;
    }
}
