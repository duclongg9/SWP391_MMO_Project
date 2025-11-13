package admin;

import dao.admin.ManageKycDAO;
import dao.connect.DBConnect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.KycRequests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@WebServlet(name = "AdminKycsServlet",
        urlPatterns = {"/admin/kycs", "/admin/kycs/status"})
public class AdminKycsServlet extends AbstractAdminServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleListKycs(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/kycs/status")) {
            handleKycStatus(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    // ===== GET: list KYC =====

    private void handleListKycs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q      = clean(req.getParameter("q"));
        String status = clean(req.getParameter("status"));   // all | 1 | 2 | 3
        String from   = clean(req.getParameter("from"));
        String to     = clean(req.getParameter("to"));
        String sort   = clean(req.getParameter("sort"));     // date_desc | date_asc | status_asc | status_desc
        if (sort == null || sort.isBlank()) sort = "date_desc";

        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) size = DEFAULT_SIZE;

        LocalDate fromD = tryParseDate(from);
        LocalDate toD   = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt   = (toD == null)   ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        Integer statusId = null;
        if (status != null && !"all".equalsIgnoreCase(status)) {
            try { statusId = Integer.valueOf(status); } catch (Exception ignore) {}
        }

        try (Connection con = DBConnect.getConnection()) {
            ManageKycDAO dao = new ManageKycDAO(con);
            List<KycRequests> list = dao.getAllKycRequests();

            // filter tên
            if (q != null && !q.isEmpty()) {
                String qLower = q.toLowerCase();
                list = list.stream()
                        .filter(k -> k.getUserName() != null &&
                                k.getUserName().toLowerCase().contains(qLower))
                        .toList();
            }

            // filter status
            if (statusId != null) {
                int s = statusId;
                list = list.stream().filter(k -> k.getStatusId() == s).toList();
            }

            // filter ngày
            if (fromAt != null || toAt != null) {
                long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                long toMs   = (toAt   == null ? Long.MAX_VALUE : toAt.getTime());
                list = list.stream().filter(k -> {
                    java.util.Date d = k.getCreatedAt();
                    long t = (d == null ? Long.MIN_VALUE : d.getTime());
                    return t >= fromMs && t <= toMs;
                }).toList();
            }

            // sort
            Comparator<KycRequests> byDate = Comparator.comparing(
                    k -> k.getCreatedAt() == null ? new java.util.Date(0) : k.getCreatedAt());
            Comparator<KycRequests> byStatus = Comparator.comparing(k -> {
                Integer v = k.getStatusId();
                return v == null ? 9 : switch (v) {
                    case 1 -> 1; // Pending
                    case 2 -> 2; // Approved
                    case 3 -> 3; // Rejected
                    default -> 9;
                };
            });

            list = switch (sort) {
                case "date_asc"    -> list.stream().sorted(byDate).toList();
                case "status_asc"  -> list.stream().sorted(byStatus).toList();
                case "status_desc" -> list.stream().sorted(byStatus.reversed()).toList();
                default            -> list.stream().sorted(byDate.reversed()).toList();
            };

            // paging
            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx   = Math.min(total, fromIdx + size);
            List<KycRequests> pageList = list.subList(fromIdx, toIdx);

            // chuẩn hóa URL ảnh
            for (KycRequests k : pageList) {
                k.setFrontImageUrl( normalizeKycImageUrl(req, k.getFrontImageUrl()) );
                k.setBackImageUrl(  normalizeKycImageUrl(req, k.getBackImageUrl())  );
                k.setSelfieImageUrl(normalizeKycImageUrl(req, k.getSelfieImageUrl()));
            }

            boolean isFirst    = (page <= 1);
            boolean isLast     = (page >= pages);
            boolean singlePage = (pages <= 1);

            req.setAttribute("kycList",   pageList);
            req.setAttribute("pg_total",  total);
            req.setAttribute("pg_page",   page);
            req.setAttribute("pg_size",   size);
            req.setAttribute("pg_pages",  pages);
            req.setAttribute("pg_isFirst",isFirst);
            req.setAttribute("pg_isLast", isLast);
            req.setAttribute("pg_single", singlePage);

        } catch (Exception e) {
            throw new ServletException("Lỗi khi tải danh sách KYC: " + e.getMessage(), e);
        }

        // giữ filter
        req.setAttribute("q",      q == null ? "" : q);
        req.setAttribute("status", status == null ? "all" : status);
        req.setAttribute("from",   fromD == null ? "" : fromD.toString());
        req.setAttribute("to",     toD   == null ? "" : toD.toString());
        req.setAttribute("sort",   sort);

        req.setAttribute("pageTitle", "Quản lý KYC");
        req.setAttribute("active",    "kycs");
        req.setAttribute("content",   "/WEB-INF/views/Admin/pages/kycs.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    // ===== POST: approve / reject =====

    private void handleKycStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");

        String action   = safe(req.getParameter("action"));   // approve | reject
        String idStr    = safe(req.getParameter("id"));
        String feedback = safe(req.getParameter("feedback"));

        if (idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Lỗi: ID không hợp lệ");
            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
            return;
        }
        if (!"approve".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action)) {
            req.getSession().setAttribute("flash", "Lỗi: Hành động không hợp lệ");
            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
            return;
        }

        int kycId = Integer.parseInt(idStr);

        // reject mà thiếu feedback -> show lỗi inline
        if ("reject".equalsIgnoreCase(action) && (feedback == null || feedback.isBlank())) {
            req.setAttribute("kycErrorId", kycId);
            req.setAttribute("kycErrorMessage", "Vui lòng nhập lí do từ chối KYC.");
            handleListKycs(req, resp);
            return;
        }

        try (Connection con = DBConnect.getConnection()) {
            ManageKycDAO dao = new ManageKycDAO(con);
            int rows;

            if ("approve".equalsIgnoreCase(action)) {
                rows = dao.approveKycAndPromote(kycId, feedback);
                req.getSession().setAttribute("flash",
                        rows > 0 ? "Duyệt KYC thành công!" : "Không có thay đổi");
            } else {
                rows = dao.rejectKyc(kycId, feedback);
                req.getSession().setAttribute("flash",
                        rows > 0 ? "Từ chối KYC thành công!" : "Không có thay đổi");
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/kycs");
    }
}
