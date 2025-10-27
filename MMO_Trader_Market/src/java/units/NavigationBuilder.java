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
    private static final String ICON_CLASS = "menu__item--icon";
    private static final String BUTTON_CLASS = "menu__item--button";

    private static final ProductService PRODUCT_SERVICE = new ProductService();

    private NavigationBuilder() {
    }

    public static List<Map<String, Object>> build(HttpServletRequest request) {
        List<Map<String, Object>> items = new ArrayList<>();
        String contextPath = request.getContextPath();
        String currentPath = resolveCurrentPath(request);

        HttpSession session = request.getSession(false);
        Integer roleId = session == null ? null : (Integer) session.getAttribute("userRole");

        if (isAdminRole(roleId)) {
            items.add(createNavItem(contextPath + RoleHomeResolver.ADMIN_HOME, "Dashboard",
                    isActive(currentPath, RoleHomeResolver.ADMIN_HOME)));
            items.add(createNavItem(contextPath + "/orders", "Quản lý đơn hàng",
                    isActive(currentPath, "/orders")));
        }

        addBaseItems(items, request, contextPath, currentPath);

        if (roleId == null) {
            if (shouldDisplayAuthCta(currentPath)) {
                items.add(createIconItem(contextPath + "/auth", "👤", "Đăng nhập", true,
                        isActive(currentPath, "/auth")));
            }
            return items;
        }

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

    private static Map<String, Object> createIconItem(String href, String icon, String text,
            boolean highlight, boolean active) {
        String extraClasses = ICON_CLASS + (highlight ? " " + BUTTON_CLASS : "");
        Map<String, Object> item = createNavItem(href, text, active, extraClasses);
        item.put("icon", icon);
        item.put("srText", text);
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

    private static boolean isActive(String currentPath, String path) {
        if (path == null || path.isEmpty()) {
            return currentPath == null || currentPath.isEmpty() || "/".equals(currentPath);
        }
        return currentPath.equals(path) || currentPath.startsWith(path + "/");
    }

    private static boolean shouldDisplayAuthCta(String currentPath) {
        if (currentPath == null) {
            return true;
        }

        return !currentPath.equals("/auth")
                && !currentPath.startsWith("/auth/")
                && !currentPath.equals("/register")
                && !currentPath.startsWith("/register/")
                && !currentPath.equals("/forgot-password")
                && !currentPath.startsWith("/forgot-password/")
                && !currentPath.equals("/reset-password")
                && !currentPath.startsWith("/reset-password/");
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
}
