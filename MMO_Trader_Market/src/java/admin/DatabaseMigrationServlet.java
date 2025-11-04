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
        out.println(".container { background: white; padding: 30px; border-radius: 8px; max-width: 800px; }");
        out.println(".success { color: green; background: #d4edda; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println(".error { color: red; background: #f8d7da; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println(".info { color: #004085; background: #cce5ff; padding: 15px; border-radius: 4px; margin: 10px 0; }");
        out.println("pre { background: #f8f9fa; padding: 15px; border-radius: 4px; overflow-x: auto; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>🔧 Database Migration - Thêm 'OTHER' vào ENUM</h1>");
        
        try (Connection conn = new DBConnect().getConnection()) {
            
            // Kiểm tra schema hiện tại
            out.println("<h2>1️⃣ Kiểm tra schema hiện tại:</h2>");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM products WHERE Field='product_type'")) {
                
                if (rs.next()) {
                    String type = rs.getString("Type");
                    out.println("<pre>Column: product_type\nType: " + type + "</pre>");
                    
                    if (type.contains("'OTHER'")) {
                        out.println("<div class='success'>✅ ENUM đã có 'OTHER'. Không cần migration!</div>");
                    } else {
                        out.println("<div class='info'>⚠️ ENUM chưa có 'OTHER'. Cần chạy migration.</div>");
                        
                        // Chạy migration
                        out.println("<h2>2️⃣ Đang chạy migration...</h2>");
                        String sql = "ALTER TABLE products MODIFY COLUMN product_type " +
                                    "ENUM('EMAIL', 'SOCIAL', 'GAME', 'SOFTWARE', 'OTHER') NOT NULL";
                        
                        try (Statement migrateStmt = conn.createStatement()) {
                            migrateStmt.executeUpdate(sql);
                            out.println("<div class='success'>✅ Migration thành công!</div>");
                            out.println("<pre>" + sql + "</pre>");
                        }
                        
                        // Kiểm tra lại
                        out.println("<h2>3️⃣ Kiểm tra lại sau migration:</h2>");
                        try (Statement checkStmt = conn.createStatement();
                             ResultSet checkRs = checkStmt.executeQuery("SHOW COLUMNS FROM products WHERE Field='product_type'")) {
                            
                            if (checkRs.next()) {
                                String newType = checkRs.getString("Type");
                                out.println("<pre>Column: product_type\nType: " + newType + "</pre>");
                                
                                if (newType.contains("'OTHER'")) {
                                    out.println("<div class='success'>🎉 HOÀN THÀNH! Bạn có thể đóng trang này và thử tạo sản phẩm loại 'Khác'.</div>");
                                }
                            }
                        }
                    }
                }
            }
            
            // Kiểm tra product_subtype
            out.println("<h2>4️⃣ Kiểm tra product_subtype:</h2>");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM products WHERE Field='product_subtype'")) {
                
                if (rs.next()) {
                    String type = rs.getString("Type");
                    out.println("<pre>Column: product_subtype\nType: " + type + "</pre>");
                    
                    if (type.contains("'OTHER'")) {
                        out.println("<div class='success'>✅ product_subtype đã có 'OTHER'</div>");
                    } else {
                        out.println("<div class='info'>⚠️ product_subtype chưa có 'OTHER'</div>");
                    }
                }
            }
            
        } catch (Exception e) {
            out.println("<div class='error'>");
            out.println("<h3>❌ Lỗi:</h3>");
            out.println("<pre>" + e.getMessage() + "</pre>");
            e.printStackTrace(out);
            out.println("</div>");
        }
        
        out.println("<hr>");
        out.println("<p><strong>⚠️ LƯU Ý BẢO MẬT:</strong> Sau khi chạy migration xong, hãy XÓA file <code>DatabaseMigrationServlet.java</code> này để tránh lỗ hổng bảo mật!</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}

