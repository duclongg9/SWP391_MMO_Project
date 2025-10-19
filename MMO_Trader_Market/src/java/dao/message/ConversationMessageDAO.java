package dao.message;

import dao.BaseDAO;
import model.view.ConversationMessageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConversationMessageDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ConversationMessageDAO.class.getName());

    public List<ConversationMessageView> findLatest(int limit) {
        final String sql = "SELECT m.content, m.created_at, u.name AS sender_name, "
                + "COALESCE(p1.name, p2.name) AS product_name "
                + "FROM messages m "
                + "JOIN users u ON u.id = m.sender_id "
                + "JOIN conversations c ON c.id = m.conversation_id "
                + "LEFT JOIN orders o ON o.id = c.related_order_id "
                + "LEFT JOIN products p1 ON p1.id = o.product_id "
                + "LEFT JOIN products p2 ON p2.id = c.related_product_id "
                + "ORDER BY m.created_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            List<ConversationMessageView> messages = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
            return messages;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải hội thoại gần nhất", ex);
            return List.of();
        }
    }

    private ConversationMessageView mapRow(ResultSet rs) throws SQLException {
        String senderName = rs.getString("sender_name");
        String content = rs.getString("content");
        String productName = rs.getString("product_name");
        Timestamp created = rs.getTimestamp("created_at");
        LocalDateTime createdAt = created == null ? LocalDateTime.now() : created.toLocalDateTime();
        return new ConversationMessageView(senderName, content, productName, createdAt);
    }
}

