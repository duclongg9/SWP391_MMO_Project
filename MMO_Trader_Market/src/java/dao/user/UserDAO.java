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
    
    /*Phần mapping database với các biến tự tạo trong java*/
    private static final String COL_ID = "id";
    private static final String COL_FULL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_ROLE = "role_id";
    private static final String COL_HASH_PASSWORD = "hashed_password";
    private static final String COL_GOOGLE_ID = "google_id";
    private static final String COL_AVATA = "avata_url";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";

   public User getUserByUserId(int id){
       String sql = """
                    SELECT * FROM  users
                    WHERE id = ? AND status = 1
                    LIMIT 1
                    """;
       
   } 
}
