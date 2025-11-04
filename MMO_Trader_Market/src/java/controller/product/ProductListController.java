package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.product.PagedResult;
import model.view.product.ProductSubtypeOption;
import model.view.product.ProductSummaryView;
import model.view.product.ProductTypeOption;
import service.ProductService;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Điều phối luồng "Danh sách sản phẩm" trên marketplace công khai.
 * <p>
 * - Liệt kê sản phẩm theo từng loại chính với phân trang phía server. - Hỗ trợ
 * lọc theo nhiều phân loại con của cùng một loại sản phẩm. - Chuẩn hóa tham số
 * đầu vào để đảm bảo trải nghiệm duyệt sản phẩm mượt mà.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends BaseController {

    private static final long serialVersionUID = 1L;
    // Trang mặc định khi không có tham số page.
    private static final int DEFAULT_PAGE = 1;
    // Kích thước trang mặc định khi người dùng không chọn.
    private static final int DEFAULT_SIZE = 5;
    // Các tùy chọn số bản ghi mỗi trang được phép hiển thị.
    private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(5, 10, 20, 40);

    // Dịch vụ sản phẩm để truy vấn danh sách theo bộ lọc và phân trang.
    private final ProductService productService = new ProductService();

    // Xử lý yêu cầu GET hiển thị danh sách sản phẩm với các bộ lọc.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ProductTypeOption> typeOptions = productService.getTypeOptions(); //Lấy danh sách type khả dụng
        String requestedType = request.getParameter("type"); //Lấy type từ query, normalize về code hợp lệ
        String normalizedType = productService.normalizeTypeCode(requestedType);
        boolean hasRawType = requestedType != null && !requestedType.trim().isEmpty();
        if (normalizedType == null && hasRawType) {
            Optional<String> fallbackType = typeOptions.stream()
                    .map(ProductTypeOption::getCode)
                    .findFirst();
            if (fallbackType.isPresent()) {
                // Nếu không truyền type hợp lệ thì chuyển sang loại đầu tiên.
                response.sendRedirect(request.getContextPath() + "/products?type=" + fallbackType.get());
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
//Lấy trang (mặc định 1) và kích thước trang (mặc định 5).
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE); 
        int sizeCandidate = parsePositiveIntOrDefault(resolveSizeParam(request), DEFAULT_SIZE);
        int size = PAGE_SIZE_OPTIONS.contains(sizeCandidate) ? sizeCandidate : DEFAULT_SIZE;
//Chuẩn hoá keyword: trim, rỗng ⇒ coi như không có.
        String rawKeyword = request.getParameter("keyword");
        String normalizedKeyword = rawKeyword == null ? null : rawKeyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
            normalizedKeyword = null;
        }
//Lấy danh sách subtype được chọn (có thể nhiều), chuẩn hoá theo type hiện tại.
        List<String> subtypeFilters = productService.normalizeSubtypeCodes(normalizedType,
                request.getParameterValues("subtype"));
        Set<String> selectedSubtypeSet = new LinkedHashSet<>(subtypeFilters);
//Gọi service lấy kết quả trang (items, page, size, total, totalPages).
        PagedResult<ProductSummaryView> result = productService.browseByType(normalizedType, subtypeFilters,
                normalizedKeyword, page, size);
        List<ProductSubtypeOption> subtypeOptions = normalizedType == null
                ? List.of()
                : productService.getSubtypeOptions(normalizedType);
        String currentTypeLabel = normalizedType == null
                ? "Tất cả sản phẩm"
                : typeOptions.stream()
                        .filter(option -> option.getCode().equals(normalizedType))
                        .map(ProductTypeOption::getLabel)
                        .findFirst()
                        .orElse(productService.getTypeLabel(normalizedType));

        request.setAttribute("pageTitle", currentTypeLabel + " - Sản phẩm");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("pageSize", result.getSize());
        request.setAttribute("pageSizeOptions", PAGE_SIZE_OPTIONS);
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("selectedType", normalizedType == null ? "" : normalizedType);
        request.setAttribute("selectedSubtypes", selectedSubtypeSet);
        request.setAttribute("keyword", normalizedKeyword == null ? "" : normalizedKeyword);
        request.setAttribute("typeOptions", typeOptions);
        request.setAttribute("subtypeOptions", subtypeOptions);
        request.setAttribute("currentTypeLabel", currentTypeLabel);

        forward(request, response, "product/list");
    }

    // Phân tích số nguyên dương, trả về mặc định nếu đầu vào không hợp lệ.
    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    // Ưu tiên tham số pageSize nếu có, nếu không thì lấy size (tương thích cũ).
    private String resolveSizeParam(HttpServletRequest request) {
        String pageSizeParam = request.getParameter("pageSize");
        if (pageSizeParam != null && !pageSizeParam.isBlank()) {
            return pageSizeParam;
        }
        return request.getParameter("size");
    }
}
