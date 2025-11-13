package units;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import model.view.product.ProductTypeOption;
import service.ProductService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a consistent navigation menu based on the authenticated user role.
 */
public final class NavigationBuilder {

    private static final String ACTIVE_CLASS = "menu__item--active";
    private static final ProductService PRODUCT_SERVICE = new ProductService();

    private NavigationBuilder() {
    }

    public static List<Map<String, Object>> build(HttpServletRequest request) {
        List<Map<String, Object>> items = new ArrayList<>();
        String contextPath = request.getContextPath();
        String currentPath = resolveCurrentPath(request);

        HttpSession session = request.getSession(false);
        Integer roleId = session == null ? null : (Integer) session.getAttribute("userRole");

        if (isSellerRole(roleId)) {
            Map<String, Object> sellerDropdown = buildSellerDropdown(contextPath, currentPath);
            if (sellerDropdown != null) {
                items.add(sellerDropdown);
            }
        }

        addBaseItems(items, request, contextPath, currentPath);

        return items;
    }

    private static void addBaseItems(List<Map<String, Object>> items, HttpServletRequest request,
            String contextPath, String currentPath) {
        Map<String, Object> productItem = buildProductDropdown(request, contextPath, currentPath);
        if (productItem != null) {
            items.add(productItem);
        } else {
            items.add(createNavItem(contextPath + "/products", "Sản phẩm", isActive(currentPath, "/products")));
        }
        items.add(createNavItem(contextPath + "/faq", "FAQ", isActive(currentPath, "/faq")));
    }

    private static Map<String, Object> createNavItem(String href, String text, boolean active) {
        return createNavItem(href, text, active, null);
    }

    private static Map<String, Object> createNavItem(String href, String text, boolean active, String extraClasses) {
        Map<String, Object> item = new HashMap<>();
        item.put("href", href);
        item.put("text", text);
        item.put("label", text);
        if (active) {
            item.put("modifier", ACTIVE_CLASS + (extraClasses == null ? "" : " " + extraClasses));
        } else if (extraClasses != null && !extraClasses.isBlank()) {
            item.put("modifier", extraClasses.trim());
        }
        return item;
    }

    private static Map<String, Object> buildProductDropdown(HttpServletRequest request, String contextPath,
            String currentPath) {
        List<ProductTypeOption> typeOptions = PRODUCT_SERVICE.getTypeOptions();
        if (typeOptions.isEmpty()) {
            return null;
        }
        String requestedType = request.getParameter("type");
        String normalizedType = PRODUCT_SERVICE.normalizeTypeCode(requestedType);
        if (normalizedType == null) {
            normalizedType = typeOptions.get(0).getCode();
        }
        boolean active = isActive(currentPath, "/products");
        String fallbackHref = contextPath + "/products?type=" + typeOptions.get(0).getCode();
        String selectedLabel = typeOptions.get(0).getLabel();

        List<Map<String, Object>> children = new ArrayList<>();
        for (ProductTypeOption option : typeOptions) {
            Map<String, Object> child = new HashMap<>();
            child.put("href", contextPath + "/products?type=" + option.getCode());
            child.put("text", option.getLabel());
            child.put("label", option.getLabel());
            child.put("code", option.getCode());
            boolean childActive = active && option.getCode().equalsIgnoreCase(normalizedType);
            child.put("active", childActive);
            children.add(child);
            if (option.getCode().equalsIgnoreCase(normalizedType)) {
                fallbackHref = contextPath + "/products?type=" + option.getCode();
                selectedLabel = option.getLabel();
            }
        }

        Map<String, Object> dropdown = createNavItem(fallbackHref, "Sản phẩm", active);
        dropdown.put("dropdown", true);
        dropdown.put("children", children);
        dropdown.put("selectedLabel", selectedLabel);
        return dropdown;
    }

    private static Map<String, Object> buildSellerDropdown(String contextPath, String currentPath) {
        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> dashboardItem = new HashMap<>();
        dashboardItem.put("href", contextPath + "/dashboard");
        dashboardItem.put("text", "Dashboard");
        dashboardItem.put("label", "Dashboard");
        boolean dashboardActive = isActive(currentPath, "/dashboard");
        dashboardItem.put("active", dashboardActive);
        children.add(dashboardItem);


        Map<String, Object> inventoryItem = new HashMap<>();
        inventoryItem.put("href", contextPath + "/seller/inventory");
        inventoryItem.put("text", "Danh sách shop");
        inventoryItem.put("label", "Danh sách shop");
        boolean inventoryActive = isActive(currentPath, "/seller/inventory");
        inventoryItem.put("active", inventoryActive);
        children.add(inventoryItem);

        Map<String, Object> disputesItem = new HashMap<>();
        disputesItem.put("href", contextPath + "/seller/disputes");
        disputesItem.put("text", "Khiếu nại");
        disputesItem.put("label", "Khiếu nại");
        boolean disputesActive = isActive(currentPath, "/seller/disputes");
        disputesItem.put("active", disputesActive);
        children.add(disputesItem);


        Map<String, Object> incomeItem = new HashMap<>();
        incomeItem.put("href", contextPath + "/seller/income");
        incomeItem.put("text", "Thu nhập");
        incomeItem.put("label", "Thu nhập");
        boolean incomeActive = isActive(currentPath, "/seller/income");
        incomeItem.put("active", incomeActive);
        children.add(incomeItem);

        boolean active = dashboardActive || inventoryActive || disputesActive || incomeActive;
        Map<String, Object> dropdown = createNavItem(contextPath + "/dashboard", "Quản lý cửa hàng", active);
        dropdown.put("dropdown", true);
        dropdown.put("children", children);
        return dropdown;
    }

    private static boolean isActive(String currentPath, String path) {
        if (path == null || path.isEmpty()) {
            return currentPath == null || currentPath.isEmpty() || "/".equals(currentPath);
        }
        return currentPath.equals(path) || currentPath.startsWith(path + "/");
    }

    private static String resolveCurrentPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private static boolean isAdminRole(Integer roleId) {
        return roleId != null && roleId == 1;
    }

    private static boolean isSellerRole(Integer roleId) {
        return roleId != null && roleId == 2;
    }
}
