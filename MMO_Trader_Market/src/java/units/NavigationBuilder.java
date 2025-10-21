package units;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

    private NavigationBuilder() {
    }

    public static List<Map<String, String>> build(HttpServletRequest request) {
        List<Map<String, String>> items = new ArrayList<>();
        String contextPath = request.getContextPath();
        String currentPath = resolveCurrentPath(request);

        HttpSession session = request.getSession(false);
        Integer roleId = session == null ? null : (Integer) session.getAttribute("userRole");

        if (isAdminRole(roleId)) {
            items.add(createNavItem(contextPath + "/dashboard", "Dashboard",
                    isActive(currentPath, "/dashboard")));
            items.add(createNavItem(contextPath + "/orders", "Quản lý đơn hàng",
                    isActive(currentPath, "/orders")));
        }

        addBaseItems(items, contextPath, currentPath);

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

    private static void addBaseItems(List<Map<String, String>> items, String contextPath, String currentPath) {
        items.add(createNavItem(contextPath + "/home#product-types", "Loại sản phẩm", false));
        items.add(createNavItem(contextPath + "/home#faq", "FAQ", false));
    }

    private static Map<String, String> createNavItem(String href, String text, boolean active) {
        return createNavItem(href, text, active, null);
    }

    private static Map<String, String> createNavItem(String href, String text, boolean active, String extraClasses) {
        Map<String, String> item = new HashMap<>();
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

    private static Map<String, String> createIconItem(String href, String icon, String text,
            boolean highlight, boolean active) {
        String extraClasses = ICON_CLASS + (highlight ? " " + BUTTON_CLASS : "");
        Map<String, String> item = createNavItem(href, text, active, extraClasses);
        item.put("icon", icon);
        item.put("srText", text);
        return item;
    }

    private static Map<String, String> createLogoutItem(String contextPath) {
        Map<String, String> item = createNavItem(contextPath + "/auth?action=logout", "Đăng xuất", false);
        String existingModifier = item.get("modifier");
        StringBuilder modifierBuilder = new StringBuilder();
        if (existingModifier != null && !existingModifier.isBlank()) {
            modifierBuilder.append(existingModifier.trim()).append(' ');
        }
        modifierBuilder.append("menu__item--danger menu__item--logout");
        item.put("modifier", modifierBuilder.toString());
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
        return roleId != null && roleId != 3;
    }
}
