package units;

import model.Users;

public final class RoleHomeResolver {

    public static final String ADMIN_HOME = "/admin";
    private static final String SELLER_HOME = "/products";
    private static final String BUYER_HOME = "/home";

    private RoleHomeResolver() {
    }

    public static String resolve(Users user) {
        Integer roleId = user == null ? null : user.getRoleId();
        if (roleId == null) {
            return ADMIN_HOME;
        }
        switch (roleId) {
            case 1:
                return ADMIN_HOME;
            case 2:
                return SELLER_HOME;
            case 3:
                return BUYER_HOME;
            default:
                return ADMIN_HOME;
        }
    }
}
