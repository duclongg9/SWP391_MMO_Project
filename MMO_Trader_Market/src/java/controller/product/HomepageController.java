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
 * Điều phối luồng "Trang chủ" dành cho khách truy cập.
 * <p>
 * - Hiển thị sản phẩm nổi bật, danh mục chính và thông tin tổng quan thị
 * trường. - Cung cấp dữ liệu shop, FAQ và thông điệp giúp người mới nắm được
 * bức tranh chung. - Chuẩn bị bộ lọc mặc định để người dùng bắt đầu hành trình
 * tìm sản phẩm.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "HomepageController", urlPatterns = {"/home"})
public class HomepageController extends BaseController {

    // Mã phiên bản phục vụ tuần tự hóa servlet.
    private static final long serialVersionUID = 1L;

    // Dịch vụ tổng hợp dữ liệu cho trang chủ (sản phẩm, shop, thông báo...).
    private final HomepageService homepageService = new HomepageService();

    // Đón yêu cầu GET tới trang chủ và chuẩn bị dữ liệu hiển thị.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Chợ tài khoản MMO - Trang chủ");
        request.setAttribute("bodyClass", "layout layout--landing");

        populateHomepageData(request);

        request.setAttribute("query", "");
        request.setAttribute("selectedType", "");
        request.setAttribute("selectedSubtype", "");

        forward(request, response, "product/home");
    }

    // Tải toàn bộ dữ liệu cần thiết cho trang chủ và gán vào request.
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
        request.setAttribute("typeOptions", homepageService.loadFilterTypeOptions());
    }

    // Xây dựng bộ ánh xạ trạng thái shop sang biểu tượng hiển thị nhanh.
    private Map<String, String> buildShopIconMap() {
        Map<String, String> icons = new HashMap<>();
        icons.put("Active", "🛍️");
        icons.put("Pending", "⏳");
        icons.put("Suspended", "⚠️");
        return icons;
    }

    // Chuẩn bị danh sách câu hỏi thường gặp hiển thị trên trang chủ.
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

    // Tạo một phần tử FAQ bao gồm tiêu đề và mô tả.
    private Map<String, String> createEntry(String title, String description) {
        Map<String, String> entry = new HashMap<>();
        entry.put("title", title);
        entry.put("description", description);
        return entry;
    }
}
