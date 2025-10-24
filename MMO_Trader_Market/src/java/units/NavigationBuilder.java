package units;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.view.product.ProductTypeOption;
import service.ProductService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Builds a consistent navigation menu based on the authenticated user role.
 */
public final class NavigationBuilder {

    private static final String ACTIVE_CLASS = "menu__item--active";
    private static final String ICON_CLASS = "menu__item--icon";
    private static final String BUTTON_CLASS = "menu__item--button";
    private static final String DROPDOWN_TYPE = "productDropdown";

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

        addBaseItems(items, contextPath, currentPath, request);

        if (roleId == null) {
            if (shouldDisplayAuthCta(currentPath)) {
                items.add(createIconItem(contextPath + "/auth", "👤", "Đăng nhập", true,
                        isActive(currentPath, "/auth")));
            }
            return items;
        }

        if (isBuyerRole(roleId)) {
            items.add(createNavItem(contextPath + "/wallet", "Ví của tôi",
                    isActive(currentPath, "/wallet")));
            items.add(createNavItem(contextPath + "/orders", "Đơn hàng",
                    isActive(currentPath, "/orders")));
            items.add(createIconItem(contextPath + "/profile", "👤", "Tài khoản", false,
                    isActive(currentPath, "/profile")));
        } else {
            items.add(createIconItem(contextPath + "/profile", "👤", "Quản trị viên", false,
                    isActive(currentPath, "/profile")));
        }

        items.add(createLogoutItem(contextPath));
        return items;
    }

    private static void addBaseItems(List<Map<String, Object>> items, String contextPath,
            String currentPath, HttpServletRequest request) {
        items.add(createProductDropdownItem(contextPath, request));
        items.add(createNavItem(contextPath + "/home#faq", "FAQ", false));
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

    private static Map<String, Object> createLogoutItem(String contextPath) {
        Map<String, Object> item = createNavItem(contextPath + "/auth?action=logout", "Đăng xuất", false);
        String existingModifier = item.get("modifier");
        StringBuilder modifierBuilder = new StringBuilder();
        if (existingModifier != null && !existingModifier.isBlank()) {
            modifierBuilder.append(existingModifier.trim()).append(' ');
        }
        modifierBuilder.append("menu__item--danger menu__item--logout");
        item.put("modifier", modifierBuilder.toString());
        return item;
    }

    private static Map<String, Object> createProductDropdownItem(String contextPath,
            HttpServletRequest request) {
        Map<String, Object> item = new HashMap<>();
        item.put("type", DROPDOWN_TYPE);
        item.put("label", "Sản phẩm");
        item.put("modifier", "menu__item--dropdown");
        item.put("href", contextPath + "/products");
        item.put("options", PRODUCT_SERVICE.getTypeOptions());

        Object selectedAttribute = request.getAttribute("selectedType");
        String requestedType = request.getParameter("type");
        String selectedValue = null;
        if (selectedAttribute instanceof String attribute) {
            selectedValue = attribute;
        } else if (selectedAttribute != null) {
            selectedValue = selectedAttribute.toString();
        } else if (requestedType != null) {
            selectedValue = requestedType;
        }
        if (selectedValue != null) {
            String normalized = selectedValue.trim().toUpperCase(Locale.ROOT);
            Optional<ProductTypeOption> option = PRODUCT_SERVICE.findTypeOption(normalized);
            option.ifPresent(opt -> item.put("currentType", opt.getCode()));
        }
        return item;
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

    private static boolean isBuyerRole(Integer roleId) {
        return roleId != null && roleId == 3;
    }

    private static boolean isAdminRole(Integer roleId) {
        return roleId != null && roleId == 1;
    }
}
