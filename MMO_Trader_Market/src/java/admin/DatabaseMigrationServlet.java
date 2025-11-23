package admin;

import dao.connect.DBConnect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet tạm thời để chạy database migration.
 * Truy cập: http://localhost:8080/MMO_Trader_Market/admin/migrate
 * 
 * Sửa các lỗi:
 * 1. Tạo view product_sales_view
 * 2. Thêm các cột thiếu vào bảng products
 * 
 * SAU KHI CHẠY XONG HÃY XÓA FILE NÀY ĐỂ BẢO MẬT!
 */
@WebServlet(name = "DatabaseMigrationServlet", urlPatterns = {"/admin/migrate"})
public class DatabaseMigrationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Database Migration</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }");
        out.println(".container { background: white; padding: 30px; border-radius: 8px; max-width: 900px; }");
        out.println(".success { color: green; background: #d4edda; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println(".error { color: red; background: #f8d7da; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println(".info { color: #004085; background: #cce5ff; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println(".warning { color: #856404; background: #fff3cd; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println("pre { background: #f8f9fa; padding: 15px; border-radius: 4px; overflow-x: auto; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>🔧 Database Migration - Fix All Errors</h1>");
        
        try (Connection conn = DBConnect.getConnection()) {
            
            // ============================================
            // 1. TẠO VIEW product_sales_view
            // ============================================
            out.println("<h2>1️⃣ Tạo view product_sales_view:</h2>");
            try (Statement stmt = conn.createStatement()) {
                // Drop view nếu đã tồn tại
                try {
                    stmt.executeUpdate("DROP VIEW IF EXISTS product_sales_view");
                } catch (Exception e) {
                    // Ignore if view doesn't exist
                }
                
                // Tạo view mới
                String createViewSql = "CREATE VIEW product_sales_view AS " +
                    "SELECT product_id, SUM(quantity) AS sold_count " +
                    "FROM orders WHERE status = 'Completed' GROUP BY product_id";
                
                stmt.executeUpdate(createViewSql);
                out.println("<div class='success'>✅ View product_sales_view đã được tạo thành công!</div>");
                out.println("<pre>" + createViewSql + "</pre>");
            } catch (Exception e) {
                out.println("<div class='error'>❌ Lỗi khi tạo view: " + e.getMessage() + "</div>");
            }
            
            // ============================================
            // 2. KIỂM TRA VÀ THÊM CÁC CỘT THIẾU
            // ============================================
            out.println("<h2>2️⃣ Kiểm tra và thêm các cột thiếu vào bảng products:</h2>");
            
            // Danh sách các cột cần kiểm tra
            String[] columns = {
                "product_type:ENUM('EMAIL', 'SOCIAL', 'GAME', 'SOFTWARE', 'OTHER') NOT NULL DEFAULT 'OTHER':shop_id",
                "product_subtype:VARCHAR(100) NULL:product_type",
                "short_description:VARCHAR(500) NULL:name",
                "primary_image_url:VARCHAR(500) NULL:price",
                "gallery_json:TEXT NULL:primary_image_url",
                "variant_schema:VARCHAR(50) NULL DEFAULT 'none':status",
                "variants_json:TEXT NULL:variant_schema"
            };
            
            for (String colDef : columns) {
                String[] parts = colDef.split(":");
                String colName = parts[0];
                String colType = parts[1];
                String afterCol = parts.length > 2 ? parts[2] : null;
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                         "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.COLUMNS " +
                         "WHERE TABLE_SCHEMA = 'mmo_schema' AND TABLE_NAME = 'products' AND COLUMN_NAME = '" + colName + "'")) {
                    
                    rs.next();
                    int exists = rs.getInt("cnt");
                    
                    if (exists == 0) {
                        // Cột chưa tồn tại, thêm mới
                        String alterSql = "ALTER TABLE products ADD COLUMN " + colName + " " + colType;
                        if (afterCol != null) {
                            alterSql += " AFTER " + afterCol;
                        }
                        
                        try (Statement alterStmt = conn.createStatement()) {
                            alterStmt.executeUpdate(alterSql);
                            out.println("<div class='success'>✅ Đã thêm cột: " + colName + "</div>");
                        }
                    } else {
                        // Cột đã tồn tại, kiểm tra xem có cần update không
                        if (colName.equals("product_type")) {
                            // Kiểm tra xem ENUM có 'OTHER' chưa
                            try (Statement checkStmt = conn.createStatement();
                                 ResultSet checkRs = checkStmt.executeQuery(
                                     "SHOW COLUMNS FROM products WHERE Field='product_type'")) {
                                
                                if (checkRs.next()) {
                                    String currentType = checkRs.getString("Type");
                                    if (!currentType.contains("'OTHER'")) {
                                        String updateSql = "ALTER TABLE products MODIFY COLUMN product_type " + colType;
                                        try (Statement updateStmt = conn.createStatement()) {
                                            updateStmt.executeUpdate(updateSql);
                                            out.println("<div class='success'>✅ Đã cập nhật cột: " + colName + " (thêm 'OTHER')</div>");
                                        }
                                    } else {
                                        out.println("<div class='info'>ℹ️ Cột " + colName + " đã tồn tại và đã có 'OTHER'</div>");
                                    }
                                }
                            }
                        } else if (colName.equals("variant_schema")) {
                            // Đảm bảo default value
                            try (Statement updateStmt = conn.createStatement()) {
                                updateStmt.executeUpdate("ALTER TABLE products MODIFY COLUMN variant_schema " + colType);
                                out.println("<div class='info'>ℹ️ Đã cập nhật default value cho: " + colName + "</div>");
                            }
                        } else {
                            out.println("<div class='info'>ℹ️ Cột " + colName + " đã tồn tại</div>");
                        }
                    }
                } catch (Exception e) {
                    out.println("<div class='error'>❌ Lỗi khi xử lý cột " + colName + ": " + e.getMessage() + "</div>");
                }
            }
            
            // ============================================
            // 3. KIỂM TRA KẾT QUẢ
            // ============================================
            out.println("<h2>3️⃣ Kiểm tra kết quả:</h2>");
            
            // Hiển thị cấu trúc bảng
            out.println("<h3>Cấu trúc bảng products:</h3>");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("DESCRIBE products")) {
                
                out.println("<pre>");
                out.println(String.format("%-20s %-30s %-10s %-10s %-10s", "Field", "Type", "Null", "Key", "Default"));
                out.println("--------------------------------------------------------------------------------");
                while (rs.next()) {
                    out.println(String.format("%-20s %-30s %-10s %-10s %-10s",
                        rs.getString("Field"),
                        rs.getString("Type"),
                        rs.getString("Null"),
                        rs.getString("Key"),
                        rs.getString("Default") != null ? rs.getString("Default") : "NULL"));
                }
                out.println("</pre>");
            }
            
            // Kiểm tra view
            out.println("<h3>Kiểm tra view product_sales_view:</h3>");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW CREATE VIEW product_sales_view")) {
                
                if (rs.next()) {
                    out.println("<div class='success'>✅ View product_sales_view đã tồn tại</div>");
                    out.println("<pre>" + rs.getString("Create View") + "</pre>");
                }
            } catch (Exception e) {
                out.println("<div class='error'>❌ View chưa được tạo: " + e.getMessage() + "</div>");
            }
            
            out.println("<div class='success' style='margin-top: 30px; font-size: 18px;'>");
            out.println("🎉 <strong>MIGRATION HOÀN TẤT!</strong><br>");
            out.println("Bạn có thể đóng trang này và thử lại ứng dụng.");
            out.println("</div>");
            
        } catch (Exception e) {
            out.println("<div class='error'>");
            out.println("<h3>❌ Lỗi kết nối database:</h3>");
            out.println("<pre>" + e.getMessage() + "</pre>");
            e.printStackTrace(out);
            out.println("</div>");
        }
        
        out.println("<hr>");
        out.println("<div class='warning'>");
        out.println("<p><strong>⚠️ LƯU Ý BẢO MẬT:</strong> Sau khi chạy migration xong, hãy XÓA file <code>DatabaseMigrationServlet.java</code> này để tránh lỗ hổng bảo mật!</p>");
        out.println("</div>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}

