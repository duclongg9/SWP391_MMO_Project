package dao.product;

import dao.BaseDAO;
import model.Products;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides read-only access to marketplace products.
 *
 * <p>The real project will connect to the database, however for the purpose of
 * testing the checkout flow we keep an in-memory catalogue that is seeded on
 * application start.</p>
 */
public class ProductDAO extends BaseDAO {

    private static final List<Products> SAMPLE_PRODUCTS = new ArrayList<>();

    static {
        seedSampleProducts();
    }

    private static void seedSampleProducts() {
        if (!SAMPLE_PRODUCTS.isEmpty()) {
            return;
        }
        Date now = Date.from(Instant.now());
        SAMPLE_PRODUCTS.add(new Products(
                1001,
                10,
                "Gmail Business 50GB",
                new BigDecimal("250000"),
                12,
                "APPROVED",
                Date.from(Instant.now().minus(14, ChronoUnit.DAYS)),
                now,
                "Tài khoản Gmail doanh nghiệp dung lượng 50GB kèm hướng dẫn đổi mật khẩu."
        ));
        SAMPLE_PRODUCTS.add(new Products(
                1002,
                11,
                "Spotify Premium 12 tháng",
                new BigDecimal("185000"),
                30,
                "APPROVED",
                Date.from(Instant.now().minus(5, ChronoUnit.DAYS)),
                now,
                "Gia hạn Spotify Premium tài khoản chính chủ, bảo hành 30 ngày."
        ));
        SAMPLE_PRODUCTS.add(new Products(
                1003,
                12,
                "Netflix UHD 1 năm",
                new BigDecimal("650000"),
                8,
                "DISPUTED",
                Date.from(Instant.now().minus(20, ChronoUnit.DAYS)),
                Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
                "Tài khoản Netflix gói Ultra HD, hỗ trợ đăng nhập 4 thiết bị."
        ));
        SAMPLE_PRODUCTS.add(new Products(
                1004,
                13,
                "Windows 11 Pro key",
                new BigDecimal("390000"),
                50,
                "PENDING",
                Date.from(Instant.now().minus(3, ChronoUnit.DAYS)),
                now,
                "Key bản quyền Windows 11 Pro, kích hoạt online trọn đời."
        ));
    }

    /**
     * Returns all products sorted by the latest update time.
     */
    public List<Products> findAll() {
        return SAMPLE_PRODUCTS.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Returns products to be displayed on the homepage dashboard.
     */
    public List<Products> findHighlighted() {
        List<Products> sorted = findAll();
        return sorted.subList(0, Math.min(3, sorted.size()));
    }

    /**
     * Finds a product by id from the sample catalogue.
     */
    public Optional<Products> findById(int id) {
        return SAMPLE_PRODUCTS.stream()
                .filter(product -> product.getId() != null && product.getId() == id)
                .findFirst();
    }

    /**
     * Searches products by keyword across name and description.
     * @param keyword optional keyword provided by the user
     * @return filtered product list sorted by latest update time
     */
    public List<Products> search(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return findAll().stream()
                .filter(product -> normalized.isEmpty() || matchesKeyword(product, normalized))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(Products product, String normalizedKeyword) {
        String name = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
        String description = product.getDescription() == null ? "" : product.getDescription().toLowerCase(Locale.ROOT);
        return name.contains(normalizedKeyword) || description.contains(normalizedKeyword);
    }
}
