package admin;

import dao.admin.ManageUserDAO;
import dao.connect.DBConnect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Users;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@WebServlet(
        name = "AdminUsersServlet",
        urlPatterns = {"/admin/users", "/admin/users/status"}
)
public class AdminUsersServlet extends AbstractAdminServlet {

    // ============ ROUTER ============

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleListUsers(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String uri = req.getRequestURI();

        if (uri.endsWith("/users/status")) {
            handleUpdateStatus(req, resp);
        }  else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ============ GET: LIST + SEARCH ============

    private void handleListUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q    = clean(req.getParameter("q"));
        String role = clean(req.getParameter("role"));  // all | buyer | seller
        String from = clean(req.getParameter("from"));
        String to   = clean(req.getParameter("to"));

        // role = all/null -> bỏ filter role
        if (role != null && ("all".equalsIgnoreCase(role) || role.isBlank())) {
            role = null;
        }

        LocalDate fromD = tryParseDate(from);
        LocalDate toD   = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt   = (toD == null)   ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) size = DEFAULT_SIZE;

        try (Connection con = DBConnect.getConnection()) {
            ManageUserDAO dao = new ManageUserDAO(con);

            // lấy list theo filter (đã sửa DAO cho đúng)
            java.util.List<Users> list = dao.searchUsers(q, role, fromAt, toAt);

            // bỏ ADMIN trước khi sort & paging
            list = list.stream()
                    .filter(u -> {
                        String rn = u.getRoleName();
                        return rn == null || !rn.equalsIgnoreCase("ADMIN");
                    })
                    .toList();

            // sort mới -> cũ theo created_at
            list = list.stream()
                    .sorted(Comparator.comparing(
                                            (Users u) -> u.getCreatedAt() == null
                                                    ? new java.util.Date(0)
                                                    : u.getCreatedAt()
                                    )
                                    .reversed()
                    )
                    .toList();

            // paging
            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx   = Math.min(total, fromIdx + size);
            java.util.List<Users> pageList = list.subList(fromIdx, toIdx);

            // set attribute xuống JSP
            req.setAttribute("userList", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page",  page);
            req.setAttribute("pg_size",  size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast",  page >= pages);
            req.setAttribute("pg_single",  pages <= 1);

        } catch (Exception e) {
            throw new ServletException("Lỗi tải người dùng: " + e.getMessage(), e);
        }

        // giữ filter cho JSP
        req.setAttribute("q",    q == null ? "" : q);
        req.setAttribute("role", role == null ? "all" : role);
        req.setAttribute("from", fromD == null ? "" : fromD.toString());
        req.setAttribute("to",   toD   == null ? "" : toD.toString());

        req.setAttribute("pageTitle", "Quản lý người dùng");
        req.setAttribute("active", "users");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/users.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }


    // ============ POST: BAN / UNBAN ============

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        String action = safe(req.getParameter("action")); // ban | unban
        String idStr  = safe(req.getParameter("id"));

        if (action == null || idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Tham số không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        int userId = Integer.parseInt(idStr);
        int newStatus = "unban".equalsIgnoreCase(action) ? 1 : 0;

        try (Connection con = DBConnect.getConnection()) {
            ManageUserDAO dao = new ManageUserDAO(con);
            int rows = dao.updateStatus(userId, newStatus);

            if (rows > 0) {
                String msg = "unban".equalsIgnoreCase(action)
                        ? "Mở khóa người dùng thành công"
                        : "Khóa người dùng thành công";
                req.getSession().setAttribute("flash", msg);
            } else {
                req.getSession().setAttribute("flash",
                        "User không tồn tại hoặc không thể cập nhật.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}
