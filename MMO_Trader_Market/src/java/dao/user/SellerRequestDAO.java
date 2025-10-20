package dao.user;

import dao.BaseDAO;
import model.SellerRequest;
import model.Users;
import model.view.SellerRequestView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO cho quản lý seller requests
 */
public class SellerRequestDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SellerRequestDAO.class.getName());

    /**
     * Tạo seller request mới (backward compatibility)
     */
    public SellerRequest createSellerRequest(int userId, String businessName, String businessDescription,
                                           String experience, String contactInfo) throws SQLException {
        // Tạo dummy KYC data để tương thích với schema mới
        return createKycRequest(userId, 
                "Chưa cung cấp", // dummy full name
                new java.util.Date(90, 0, 1), // dummy date of birth (1990-01-01)
                "000000000", // dummy ID number
                null, null, null, // no images
                businessName, businessDescription, experience, contactInfo);
    }
    
    /**
     * Tạo KYC request mới
     */
    public SellerRequest createKycRequest(int userId, String fullName, java.util.Date dateOfBirth, String idNumber,
                                         String frontIdImagePath, String backIdImagePath, String selfieImagePath,
                                         String businessName, String businessDescription, String experience, 
                                         String contactInfo) throws SQLException {
        final String sql = "INSERT INTO seller_requests (user_id, full_name, date_of_birth, id_number, " +
                "front_id_image_path, back_id_image_path, selfie_image_path, business_name, business_description, " +
                "experience, contact_info, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending', NOW())";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, userId);
            statement.setString(2, fullName);
            statement.setDate(3, new java.sql.Date(dateOfBirth.getTime()));
            statement.setString(4, idNumber);
            statement.setString(5, frontIdImagePath);
            statement.setString(6, backIdImagePath);
            statement.setString(7, selfieImagePath);
            statement.setString(8, businessName);
            statement.setString(9, businessDescription);
            statement.setString(10, experience);
            statement.setString(11, contactInfo);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo KYC request thất bại");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    SellerRequest request = new SellerRequest();
                    request.setId(generatedKeys.getInt(1));
                    request.setUserId(userId);
                    request.setFullName(fullName);
                    request.setDateOfBirth(dateOfBirth);
                    request.setIdNumber(idNumber);
                    request.setFrontIdImagePath(frontIdImagePath);
                    request.setBackIdImagePath(backIdImagePath);
                    request.setSelfieImagePath(selfieImagePath);
                    request.setBusinessName(businessName);
                    request.setBusinessDescription(businessDescription);
                    request.setExperience(experience);
                    request.setContactInfo(contactInfo);
                    request.setStatus("Pending");
                    request.setCreatedAt(new java.util.Date());
                    return request;
                }
                throw new SQLException("Tạo KYC request thất bại, không lấy được ID");
            }
        }
    }

    /**
     * Tìm seller request của user
     */
    public Optional<SellerRequest> findByUserId(int userId) {
        final String sql = "SELECT id, user_id, full_name, date_of_birth, id_number, " +
                "front_id_image_path, back_id_image_path, selfie_image_path, " +
                "business_name, business_description, experience, contact_info, " +
                "status, rejection_reason, reviewed_by, created_at, reviewed_at " +
                "FROM seller_requests WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm seller request theo user ID", ex);
        }
        
        return Optional.empty();
    }

    /**
     * Tìm seller request theo ID
     */
    public Optional<SellerRequest> findById(int requestId) {
        final String sql = "SELECT id, user_id, full_name, date_of_birth, id_number, " +
                "front_id_image_path, back_id_image_path, selfie_image_path, " +
                "business_name, business_description, experience, contact_info, " +
                "status, rejection_reason, reviewed_by, created_at, reviewed_at " +
                "FROM seller_requests WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, requestId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm seller request theo ID", ex);
        }
        
        return Optional.empty();
    }

    /**
     * Lấy danh sách seller requests với phân trang
     */
    public List<SellerRequestView> findAllWithPagination(String status, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT sr.id, sr.user_id, sr.full_name, sr.date_of_birth, sr.id_number, " +
                "sr.front_id_image_path, sr.back_id_image_path, sr.selfie_image_path, " +
                "sr.business_name, sr.business_description, sr.experience, sr.contact_info, " +
                "sr.status, sr.rejection_reason, sr.reviewed_by, sr.created_at, sr.reviewed_at, " +
                "u.email, u.name, u.role_id, u.status as user_status " +
                "FROM seller_requests sr " +
                "JOIN users u ON u.id = sr.user_id ");
        
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            sql.append("WHERE sr.status = ? ");
            params.add(status);
        }
        
        sql.append("ORDER BY sr.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        List<SellerRequestView> results = new ArrayList<>();
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    SellerRequest request = mapRow(rs);
                    Users user = mapUserRow(rs);
                    results.add(new SellerRequestView(request, user));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách seller requests", ex);
        }
        
        return results;
    }

    /**
     * Đếm tổng số seller requests
     */
    public long countRequests(String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM seller_requests ");
        
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            sql.append("WHERE status = ?");
        }
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                statement.setString(1, status);
            }
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm seller requests", ex);
        }
        
        return 0;
    }

    /**
     * Duyệt seller request (approve)
     */
    public boolean approveRequest(int requestId, int adminId) {
        final String sql = "UPDATE seller_requests SET status = 'Approved', reviewed_by = ?, " +
                "reviewed_at = NOW() WHERE id = ? AND status = 'Pending'";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, adminId);
            statement.setInt(2, requestId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể approve seller request", ex);
            return false;
        }
    }

    /**
     * Từ chối seller request (reject)
     */
    public boolean rejectRequest(int requestId, int adminId, String rejectionReason) {
        final String sql = "UPDATE seller_requests SET status = 'Rejected', reviewed_by = ?, " +
                "rejection_reason = ?, reviewed_at = NOW() WHERE id = ? AND status = 'Pending'";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, adminId);
            statement.setString(2, rejectionReason);
            statement.setInt(3, requestId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể reject seller request", ex);
            return false;
        }
    }

    private SellerRequest mapRow(ResultSet rs) throws SQLException {
        SellerRequest request = new SellerRequest();
        request.setId(rs.getInt("id"));
        request.setUserId(rs.getInt("user_id"));
        
        // KYC information
        request.setFullName(rs.getString("full_name"));
        java.sql.Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            request.setDateOfBirth(new java.util.Date(dateOfBirth.getTime()));
        }
        request.setIdNumber(rs.getString("id_number"));
        request.setFrontIdImagePath(rs.getString("front_id_image_path"));
        request.setBackIdImagePath(rs.getString("back_id_image_path"));
        request.setSelfieImagePath(rs.getString("selfie_image_path"));
        
        // Business information
        request.setBusinessName(rs.getString("business_name"));
        request.setBusinessDescription(rs.getString("business_description"));
        request.setExperience(rs.getString("experience"));
        request.setContactInfo(rs.getString("contact_info"));
        
        // Status and review information
        request.setStatus(rs.getString("status"));
        request.setRejectionReason(rs.getString("rejection_reason"));
        
        Integer reviewedBy = rs.getObject("reviewed_by") == null ? null : rs.getInt("reviewed_by");
        request.setReviewedBy(reviewedBy);
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            request.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        if (reviewedAt != null) {
            request.setReviewedAt(new java.util.Date(reviewedAt.getTime()));
        }
        
        return request;
    }

    private Users mapUserRow(ResultSet rs) throws SQLException {
        Users user = new Users();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setName(rs.getString("name"));
        user.setRoleId(rs.getInt("role_id"));
        user.setStatus(rs.getBoolean("user_status"));
        return user;
    }
}
