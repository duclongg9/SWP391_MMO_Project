package admin;

import dao.admin.ManageShopDAO;
import dao.connect.DBConnect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Shops;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@WebServlet(name = "AdminShopsServlet",
        urlPatterns = {"/admin/shops", "/admin/shops/status"})
public class AdminShopsServlet extends AbstractAdminServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleListShops(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/shops/status")) {
            handleShopStatus(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    private void handleListShops(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q      = clean(req.getParameter("q"));
        String status = clean(req.getParameter("status"));
        String from   = clean(req.getParameter("from"));
        String to     = clean(req.getParameter("to"));
        String sort   = clean(req.getParameter("sort")); // date_desc | date_asc | status_asc | status_desc
        if (sort == null || sort.isBlank()) sort = "date_desc";

        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) size = DEFAULT_SIZE;

        LocalDate fromD = tryParseDate(from);
        LocalDate toD   = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt   = (toD == null)   ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        try (Connection con = DBConnect.getConnection()) {
            ManageShopDAO dao = new ManageShopDAO(con);
            List<Shops> list = dao.getAllShops();

            if (q != null && !q.isEmpty()) {
                String qLower = q.toLowerCase();
                list = list.stream()
                        .filter(s ->
                                (s.getName() != null && s.getName().toLowerCase().contains(qLower)) ||
                                        (s.getOwnerName() != null && s.getOwnerName().toLowerCase().contains(qLower))
                        )
                        .toList();
            }

            if (status != null && !"all".equalsIgnoreCase(status)) {
                String st = status;
                list = list.stream()
                        .filter(s -> s.getStatus() != null &&
                                s.getStatus().equalsIgnoreCase(st))
                        .toList();
            }

            if (fromAt != null || toAt != null) {
                long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                long toMs   = (toAt   == null ? Long.MAX_VALUE : toAt.getTime());
                list = list.stream().filter(s -> {
                    java.util.Date d = s.getCreatedAt();
                    long t = (d == null ? Long.MIN_VALUE : d.getTime());
                    return t >= fromMs && t <= toMs;
                }).toList();
            }

            Comparator<Shops> byDate = Comparator.comparing(
                    s -> s.getCreatedAt() == null ? new java.util.Date(0) : s.getCreatedAt());

            Map<String,Integer> rank = new HashMap<>();
            rank.put("Pending", 1);
            rank.put("Active", 2);
            rank.put("Rejected", 3);
            rank.put("Banned", 4);

            Comparator<Shops> byStatus = Comparator.comparing(s -> {
                String st = s.getStatus() == null ? "" : s.getStatus();
                return rank.getOrDefault(st, 9);
            });

            list = switch (sort) {
                case "date_asc"    -> list.stream().sorted(byDate).toList();
                case "status_asc"  -> list.stream().sorted(byStatus).toList();
                case "status_desc" -> list.stream().sorted(byStatus.reversed()).toList();
                default            -> list.stream().sorted(byDate.reversed()).toList();
            };

            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx   = Math.min(total, fromIdx + size);
            List<Shops> pageList = list.subList(fromIdx, toIdx);

            req.setAttribute("shopList", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page",  page);
            req.setAttribute("pg_size",  size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast",  page >= pages);
            req.setAttribute("pg_single",  pages <= 1);

        } catch (Exception e) {
            throw new ServletException("Lỗi tải cửa hàng: " + e.getMessage(), e);
        }

        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("status", status == null ? "all" : status);
        req.setAttribute("from", fromD == null ? "" : fromD.toString());
        req.setAttribute("to",   toD   == null ? "" : toD.toString());
        req.setAttribute("sort", sort);

        req.setAttribute("pageTitle", "Quản lý cửa hàng");
        req.setAttribute("active", "shops");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/shops.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    private void handleShopStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        String idStr     = safe(req.getParameter("id"));
        String action    = safe(req.getParameter("action"));     // ban | unban
        String adminNote = safe(req.getParameter("admin_note"));

        if (idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Lỗi: ID cửa hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/shops");
            return;
        }
        if (action == null ||
                !(action.equalsIgnoreCase("ban") || action.equalsIgnoreCase("unban"))) {
            req.getSession().setAttribute("flash", "Lỗi: Hành động không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/shops");
            return;
        }

        int shopId = Integer.parseInt(idStr);

        try (Connection con = DBConnect.getConnection()) {
            ManageShopDAO dao = new ManageShopDAO(con);
            Shops shop = dao.findById(shopId);
            if (shop == null) {
                req.getSession().setAttribute("flash", "Lỗi: Không tìm thấy cửa hàng #" + shopId);
                resp.sendRedirect(req.getContextPath() + "/admin/shops");
                return;
            }

            String currentStatus = shop.getStatus() == null ? "" : shop.getStatus();

            if ("ban".equalsIgnoreCase(action)) {
                if (!"Active".equalsIgnoreCase(currentStatus)) {
                    req.getSession().setAttribute("flash",
                            "Chỉ có thể ban cửa hàng đang ở trạng thái Active.");
                    resp.sendRedirect(req.getContextPath() + "/admin/shops");
                    return;
                }
                if (adminNote == null || adminNote.isBlank()) {
                    req.getSession().setAttribute("flash",
                            "Lỗi: Vui lòng nhập Admin note khi ban cửa hàng.");
                    resp.sendRedirect(req.getContextPath() + "/admin/shops");
                    return;
                }
                boolean ok = dao.updateStatusAndNote(shopId, "Suspended", adminNote.trim());
                req.getSession().setAttribute("flash",
                        ok ? "Đã ban cửa hàng #" + shopId + " thành công."
                                : "Không có thay đổi khi ban cửa hàng #" + shopId + ".");
            } else { // unban
                if (!"Suspended".equalsIgnoreCase(currentStatus)) {
                    req.getSession().setAttribute("flash",
                            "Chỉ có thể unban cửa hàng đang ở trạng thái Suspended.");
                    resp.sendRedirect(req.getContextPath() + "/admin/shops");
                    return;
                }
                String newNote = (adminNote != null && !adminNote.isBlank())
                        ? adminNote.trim()
                        : shop.getAdminNote();
                boolean ok = dao.updateStatusAndNote(shopId, "Active", newNote);
                req.getSession().setAttribute("flash",
                        ok ? "Đã unban cửa hàng #" + shopId + " (chuyển về Active)."
                                : "Không có thay đổi khi unban cửa hàng #" + shopId + ".");
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash",
                    "Lỗi hệ thống: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/shops");
    }
}
