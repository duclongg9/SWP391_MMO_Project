package admin;

import dao.ManageDisputeDAO;
import dao.connect.DBConnect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Disputes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@WebServlet(name = "AdminDisputesServlet",
        urlPatterns = {"/admin/disputes", "/admin/disputes/status"})
public class AdminDisputesServlet extends AbstractAdminServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleListDisputes(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/disputes/status")) {
            handleDisputeStatus(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    private void handleListDisputes(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q         = clean(req.getParameter("q"));
        String status    = clean(req.getParameter("status"));     // all | ...
        String issueType = clean(req.getParameter("issueType"));  // all | ...
        String from      = clean(req.getParameter("from"));
        String to        = clean(req.getParameter("to"));

        LocalDate fromD = tryParseDate(from);
        LocalDate toD   = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt   = (toD   == null) ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        final int DEFAULT_SIZE = 10;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) size = DEFAULT_SIZE;

        try (Connection con = DBConnect.getConnection()) {
            ManageDisputeDAO dao = new ManageDisputeDAO(con);

            List<Disputes> list = dao.search(
                    q,
                    (status == null || "all".equalsIgnoreCase(status)) ? null : status,
                    (issueType == null || "all".equalsIgnoreCase(issueType)) ? null : issueType,
                    fromAt, toAt
            );

            list = list.stream()
                    .sorted(Comparator.comparing(
                            d -> d.getCreatedAt() == null
                                    ? new java.util.Date(0)
                                    : d.getCreatedAt(),
                            Comparator.reverseOrder()))
                    .toList();

            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx   = Math.min(total, fromIdx + size);
            List<Disputes> pageList = list.subList(fromIdx, toIdx);

            req.setAttribute("disputes", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page",  page);
            req.setAttribute("pg_size",  size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast",  page >= pages);
            req.setAttribute("pg_single",  pages <= 1);

        } catch (Exception e) {
            throw new ServletException("Lỗi tải khiếu nại: " + e.getMessage(), e);
        }

        req.setAttribute("q",         q == null ? "" : q);
        req.setAttribute("status",    status == null ? "all" : status);
        req.setAttribute("issueType", issueType == null ? "all" : issueType);
        req.setAttribute("from",      fromD == null ? "" : fromD.toString());
        req.setAttribute("to",        toD   == null ? "" : toD.toString());

        req.setAttribute("pageTitle", "Quản lý khiếu nại");
        req.setAttribute("active",    "disputes");
        req.setAttribute("content",   "/WEB-INF/views/Admin/pages/disputes.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    private void handleDisputeStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idStr   = safe(req.getParameter("id"));
        String action  = safe(req.getParameter("action"));          // inreview | accept | reject
        String note    = safe(req.getParameter("resolution_note"));

        if (idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Lỗi: ID khiếu nại không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/disputes");
            return;
        }
        int id = Integer.parseInt(idStr);

        if (action == null ||
                !(action.equalsIgnoreCase("inreview")
                        || action.equalsIgnoreCase("accept")
                        || action.equalsIgnoreCase("reject"))) {
            req.getSession().setAttribute("flash", "Lỗi: Hành động không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/disputes");
            return;
        }

        if ("reject".equalsIgnoreCase(action) &&
                (note == null || note.trim().isEmpty())) {
            req.getSession().setAttribute("flash",
                    "Lỗi: Vui lòng nhập ghi chú khi Reject.");
            resp.sendRedirect(req.getContextPath() + "/admin/disputes");
            return;
        }

        String newStatus = switch (action.toLowerCase()) {
            case "inreview" -> "InReview";
            case "accept"   -> "ResolvedWithoutRefund";
            case "reject"   -> "Closed";
            default -> null;
        };
        if (newStatus == null) {
            req.getSession().setAttribute("flash", "Lỗi: Hành động không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/disputes");
            return;
        }

        Integer adminId = (Integer) req.getSession().getAttribute("userId");

        try (Connection con = DBConnect.getConnection()) {
            String sql = """
                    UPDATE disputes
                    SET status = ?,
                        resolution_note = ?,
                        resolved_by_admin_id = ?,
                        updated_at = NOW()
                    WHERE id = ?
                    """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                ps.setString(2, note);
                ps.setObject(3, adminId, java.sql.Types.INTEGER);
                ps.setInt(4, id);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    req.getSession().setAttribute("flash",
                            "Cập nhật khiếu nại #" + id + " thành công.");
                } else {
                    req.getSession().setAttribute("flash",
                            "Không tìm thấy khiếu nại #" + id + ".");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash",
                    "Lỗi xử lý khiếu nại: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/disputes");
    }
}
