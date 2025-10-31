package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final long serialVersionUID = 1L;
    private final HomepageService homepageService = new HomepageService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Chợ tài khoản MMO - Trang chủ");
        request.setAttribute("bodyClass", "layout layout--landing");

        request.setAttribute("query", ""); //Set các filter mặc định
        request.setAttribute("selectedType", "");
        request.setAttribute("selectedSubtype", "");
        request.setAttribute("typeOptions", homepageService.loadFilterTypeOptions());
        request.setAttribute("faqs", buildFaqEntries());

        forward(request, response, "product/home");
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
