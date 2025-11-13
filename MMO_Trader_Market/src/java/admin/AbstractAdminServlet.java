package admin;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public abstract class AbstractAdminServlet extends HttpServlet {

    protected static final DateTimeFormatter FLEX_DMY = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = (Integer) req.getSession().getAttribute("userId");
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }
        super.service(req, resp);
    }

    // ==== Helpers ====

    protected static String clean(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    protected static String safe(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    protected static LocalDate tryParseDate(String s) {
        String v = clean(s);
        if (v == null) return null;
        try {
            return LocalDate.parse(v, FLEX_DMY);
        } catch (Exception e) {
            return null;
        }
    }

    protected static int parseIntOrDefault(String s, int dft) {
        try {
            return Integer.parseInt(safe(s));
        } catch (Exception e) {
            return dft;
        }
    }

    protected static int ceilDiv(int total, int size) {
        return Math.max(1, (int) Math.ceil(total / (double) Math.max(1, size)));
    }

    protected static int clampPage(int page, int pages) {
        if (page < 1) return 1;
        if (page > pages) return pages;
        return page;
    }

    protected static String normalizeKycImageUrl(HttpServletRequest req, String raw) {
        if (raw == null || raw.isBlank()) return raw;
        String url = raw.replaceFirst("^https?://[^/]+", "")
                .replaceFirst("^/(SWP391_MMO_Project|MMO_Trader_Market)", "");
        if (!url.startsWith("/")) url = "/" + url;
        return req.getContextPath() + url;
    }
}
