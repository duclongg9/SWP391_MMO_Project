package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Shops;
import model.SystemConfigs;
import model.view.ConversationMessageView;
import model.view.CustomerProfileView;
import model.view.MarketplaceSummary;
import model.view.product.ProductSummaryView;
import service.HomepageService;

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

    private final HomepageService homepageService = new HomepageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Chợ tài khoản MMO - Trang chủ");
        request.setAttribute("bodyClass", "layout layout--landing");
//        request.setAttribute("headerTitle", "MMO Trader Market");
//        request.setAttribute("headerSubtitle", "Nền tảng demo mua bán tài khoản an toàn");

        populateHomepageData(request);

        forward(request, response, "product/home");
    }

    private void populateHomepageData(HttpServletRequest request) {
        MarketplaceSummary summary = homepageService.loadMarketplaceSummary();
        request.setAttribute("summary", summary);

        List<ProductSummaryView> featuredProducts = homepageService.loadFeaturedProducts();
        request.setAttribute("featuredProducts", featuredProducts);

        List<Shops> shops = homepageService.loadActiveShops();
        request.setAttribute("shops", shops);
        request.setAttribute("shopIcons", buildShopIconMap());

        request.setAttribute("productCategories", homepageService.loadProductCategories());

        CustomerProfileView profile = homepageService.loadHighlightedBuyer();
        request.setAttribute("customerProfile", profile);

        List<ConversationMessageView> messages = homepageService.loadRecentMessages();
        request.setAttribute("recentMessages", messages);

        List<SystemConfigs> systemNotes = homepageService.loadSystemNotes();
        request.setAttribute("systemNotes", systemNotes);

        request.setAttribute("faqs", buildFaqEntries());
    }

    private Map<String, String> buildShopIconMap() {
        Map<String, String> icons = new HashMap<>();
        icons.put("Active", "🛍️");
        icons.put("Pending", "⏳");
        icons.put("Suspended", "⚠️");
        return icons;
    }

    private List<Map<String, String>> buildFaqEntries() {
        List<Map<String, String>> faqs = new ArrayList<>();

        faqs.add(createEntry("Làm sao để mua tài khoản an toàn?",
                "Hãy kiểm tra trạng thái duyệt và chỉ thanh toán qua kênh được hỗ trợ trong hệ thống."));
        faqs.add(createEntry("Tôi có thể yêu cầu hoàn tiền không?",
                "Người mua có thể mở khiếu nại trong vòng 24 giờ sau khi nhận tài khoản."));
        faqs.add(createEntry("Ví điện tử hoạt động như thế nào?",
                "Ví cho phép nạp tiền nhanh, thanh toán tức thì và theo dõi lịch sử giao dịch."));
        faqs.add(createEntry("Seller cần chuẩn bị gì để đăng bán?",
                "Hãy xác minh danh tính KYC và cung cấp mô tả chi tiết cho từng sản phẩm."));

        return faqs;
    }

    private Map<String, String> createEntry(String title, String description) {
        Map<String, String> entry = new HashMap<>();
        entry.put("title", title);
        entry.put("description", description);
        return entry;
    }
}
