package dao.seller;

import dao.BaseDAO;
import model.Orders;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO cho seller xem đơn hàng bán ra từ shop của mình.
 * 
 * @version 1.0
 * @author AI Assistant
 */
public class SellerOrderDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SellerOrderDAO.class.getName());

    /**
     * Lấy danh sách đơn hàng theo shop ID với phân trang.
     *
     * @param shopId ID của shop
     * @param status trạng thái đơn hàng (null = tất cả)
     * @param limit  số lượng đơn hàng
     * @param offset vị trí bắt đầu
     * @return danh sách đơn hàng
     */
    public List<Orders> findByShopId(int shopId, String status, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
            "SELECT o.id, o.buyer_id, o.product_id, o.payment_transaction_id, "
            + "o.total_amount, o.status, o.idempotency_key, o.hold_until, "
            + "o.created_at, o.updated_at "
            + "FROM orders o "
            + "JOIN products p ON o.product_id = p.id "
            + "WHERE p.shop_id = ?");
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND o.status = ?");
        }
        
        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            statement.setInt(paramIndex++, shopId);
            
            if (status != null && !status.trim().isEmpty()) {
                statement.setString(paramIndex++, status);
            }
            
            statement.setInt(paramIndex++, limit);
            statement.setInt(paramIndex, offset);
            
            List<Orders> orders = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
            return orders;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải đơn hàng theo shop", ex);
            return List.of();
        }
    }

    /**
     * Đếm tổng số đơn hàng theo shop.
     *
     * @param shopId ID của shop
     * @param status trạng thái đơn hàng (null = tất cả)
     * @return tổng số đơn hàng
     */
    public long countByShopId(int shopId, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM orders o "
            + "JOIN products p ON o.product_id = p.id "
            + "WHERE p.shop_id = ?");
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND o.status = ?");
        }
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            statement.setInt(paramIndex++, shopId);
            
            if (status != null && !status.trim().isEmpty()) {
                statement.setString(paramIndex, status);
            }
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn hàng theo shop", ex);
        }
        return 0;
    }

    /**
     * Thống kê đơn hàng theo trạng thái.
     *
     * @param shopId ID của shop
     * @return Map<status, count>
     */
    public Map<String, Long> countByStatus(int shopId) {
        String sql = "SELECT o.status, COUNT(*) AS total "
                + "FROM orders o "
                + "JOIN products p ON o.product_id = p.id "
                + "WHERE p.shop_id = ? "
                + "GROUP BY o.status";
        
        Map<String, Long> result = new HashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("status"), rs.getLong("total"));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê đơn hàng theo trạng thái", ex);
        }
        return result;
    }

    /**
     * Tính tổng doanh thu theo shop (chỉ đơn hàng thành công).
     *
     * @param shopId ID của shop
     * @return tổng doanh thu
     */
    public java.math.BigDecimal calculateRevenue(int shopId) {
        String sql = "SELECT SUM(o.total_amount) AS revenue "
                + "FROM orders o "
                + "JOIN products p ON o.product_id = p.id "
                + "WHERE p.shop_id = ? AND o.status = 'Completed'";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("revenue");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tính doanh thu", ex);
        }
        return java.math.BigDecimal.ZERO;
    }

    /**
     * Ánh xạ ResultSet sang đối tượng Orders.
     */
    private Orders mapRow(ResultSet rs) throws SQLException {
        Orders order = new Orders();
        order.setId(rs.getInt("id"));
        order.setBuyerId(rs.getInt("buyer_id"));
        order.setProductId(rs.getInt("product_id"));
        
        Integer paymentTxId = (Integer) rs.getObject("payment_transaction_id");
        order.setPaymentTransactionId(paymentTxId);
        
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setIdempotencyKey(rs.getString("idempotency_key"));
        
        Timestamp holdUntil = rs.getTimestamp("hold_until");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        
        if (holdUntil != null) {
            order.setHoldUntil(new java.util.Date(holdUntil.getTime()));
        }
        if (createdAt != null) {
            order.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        if (updatedAt != null) {
            order.setUpdatedAt(new java.util.Date(updatedAt.getTime()));
        }
        
        return order;
    }
}
