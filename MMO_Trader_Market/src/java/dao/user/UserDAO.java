package dao.user;

import dao.BaseDAO;
import model.Role;
import model.User;
import java.util.Arrays;
import java.util.List;

/**
 * DAO responsible for user authentication. Replace the in-memory collection by
 * a JDBC implementation when integrating with the real database tables.
 */
public class UserDAO extends BaseDAO {

    private static final List<User> USERS = Arrays.asList(
            new User(1, "admin", "admin@mmo.local", Role.ADMIN, "admin123"),
            new User(2, "seller", "seller@mmo.local", Role.SELLER, "seller123"),
            new User(3, "buyer", "buyer@mmo.local", Role.BUYER, "buyer123")
    );

    public User findByCredentials(String username, String password) {
        return USERS.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username)
                        && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }
}
