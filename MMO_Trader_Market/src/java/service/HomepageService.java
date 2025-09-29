package service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import model.CustomerProfile;
import model.Product;
import model.ProductCategory;
import model.Review;

/**
 * Provides demo data for the public homepage without requiring a database.
 */
public class HomepageService {

    private final ProductService productService = new ProductService();

    /**
     * Returns sample categories used to render the navigation menu.
     */
    public List<ProductCategory> getCategories() {
        return Arrays.asList(
                new ProductCategory("mail", "Mail doanh nghiệp", "Email theo domain, full quyền quản trị", "📧"),
                new ProductCategory("software", "Phần mềm bản quyền", "Tài khoản học tập, làm việc uy tín", "🛠"),
                new ProductCategory("game", "Tài khoản game", "Nhiều hạng mức rank, skin hiếm", "🎮"),
                new ProductCategory("stream", "Giải trí & Stream", "Netflix, Spotify, Fshare, v.v.", "🎬"),
                new ProductCategory("other", "Khác", "Key bản quyền, gift card và hơn thế nữa", "🪙")
        );
    }

    /**
     * Uses the product service to display highlighted products on the homepage.
     */
    public List<Product> getFeaturedProducts() {
        return productService.getHighlightedProducts();
    }

    /**
     * Returns a fictional customer profile to highlight marketplace credibility.
     */
    public CustomerProfile getSampleCustomerProfile() {
        return new CustomerProfile(
                "Bùi Minh Khôi",
                "Thành viên Kim cương",
                LocalDate.of(2021, 3, 12),
                186,
                4.9
        );
    }

    /**
     * Provides curated customer reviews for the landing page.
     */
    public List<Review> getSampleReviews() {
        return Arrays.asList(
                new Review("Linh Trần", 5, "Mua acc Liên Quân full tướng, giao dịch an toàn và nhanh chóng.", "Acc game B"),
                new Review("Đức Hoàng", 4, "Giao key Microsoft 365 trong 5 phút, hỗ trợ kích hoạt tận tình.", "Phần mềm bản quyền"),
                new Review("Mai Ngọc", 5, "Mail EDU giá tốt, hạn sử dụng dài, đúng mô tả.", "Mail doanh nghiệp")
        );
    }

    /**
     * Returns suggested safety tips for new buyers.
     */
    public List<String> getBuyerSafetyTips() {
        return Arrays.asList(
                "Luôn kiểm tra lịch sử giao dịch và đánh giá của người bán.",
                "Sử dụng kênh thanh toán được hỗ trợ để đảm bảo quyền lợi.",
                "Đổi mật khẩu ngay sau khi nhận tài khoản để bảo mật.",
                "Liên hệ hỗ trợ 24/7 nếu cần xác minh thông tin hoặc kháng nghị."
        );
    }
}
