package admin;

import dao.system.SystemConfigDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "AdminSystemsServlet",
        urlPatterns = {"/admin/systems", "/admin/systems/escrow"})
public class AdminSystemsServlet extends AbstractAdminServlet {

    private static final long DEFAULT_ESCROW_HOLD_SECONDS = 72L * 3600L;
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleSystems(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/systems/escrow")) {
            handleUpdateEscrowConfig(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    private void handleSystems(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object flash = session.getAttribute("flash");
            if (flash != null) {
                req.setAttribute("flash", flash);
                session.removeAttribute("flash");
            }
            Object flashError = session.getAttribute("flashError");
            if (flashError != null) {
                req.setAttribute("flashError", flashError);
                session.removeAttribute("flashError");
            }
        }

        long escrowHoldSeconds = DEFAULT_ESCROW_HOLD_SECONDS;
        String configValue = systemConfigDAO.findValueByKey("escrow.hold.default.seconds")
                .orElse(null);
        if (configValue != null && !configValue.trim().isEmpty()) {
            try {
                long parsed = Long.parseLong(configValue.trim());
                if (parsed > 0) escrowHoldSeconds = parsed;
            } catch (NumberFormatException ignore) {}
        }

        int hours = (int) (escrowHoldSeconds / 3600L);
        long remainder = escrowHoldSeconds % 3600L;
        int minutes = (int) (remainder / 60L);
        int seconds = (int) (remainder % 60L);

        req.setAttribute("escrowHoldHoursPart", hours);
        req.setAttribute("escrowHoldMinutesPart", minutes);
        req.setAttribute("escrowHoldSecondsPart", seconds);
        req.setAttribute("escrowHoldTotalSeconds", escrowHoldSeconds);
        req.setAttribute("escrowHoldDurationLabel",
                formatEscrowDuration(hours, minutes, seconds));

        req.setAttribute("pageTitle", "Cấu hình hệ thống");
        req.setAttribute("active", "systems");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/systems.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    private void handleUpdateEscrowConfig(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        String hoursParam = safe(req.getParameter("escrowHoldHours"));
        String minutesParam = safe(req.getParameter("escrowHoldMinutes"));
        String secondsParam = safe(req.getParameter("escrowHoldSeconds"));
        HttpSession session = req.getSession();

        int hours;
        int minutes;
        int seconds;

        try {
            hours = hoursParam == null ? 0 : Integer.parseInt(hoursParam);
            minutes = minutesParam == null ? 0 : Integer.parseInt(minutesParam);
            seconds = secondsParam == null ? 0 : Integer.parseInt(secondsParam);
        } catch (NumberFormatException ex) {
            session.setAttribute("flashError",
                    "Vui lòng nhập giá trị số hợp lệ cho giờ, phút và giây.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }

        if (hours < 0 || hours > 720) {
            session.setAttribute("flashError", "Giá trị giờ phải nằm trong khoảng 0 - 720.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }
        if (minutes < 0 || minutes >= 60) {
            session.setAttribute("flashError", "Giá trị phút phải nằm trong khoảng 0 - 59.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }
        if (seconds < 0 || seconds >= 60) {
            session.setAttribute("flashError", "Giá trị giây phải nằm trong khoảng 0 - 59.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }

        long totalSeconds = hours * 3600L + minutes * 60L + seconds;
        long maxSeconds = 720L * 3600L;
        if (totalSeconds <= 0) {
            session.setAttribute("flashError",
                    "Thời gian escrow phải lớn hơn 0 giây.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }
        if (totalSeconds > maxSeconds) {
            session.setAttribute("flashError",
                    "Thời gian escrow không được vượt quá 720 giờ.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }

        boolean updated = systemConfigDAO.upsertValueByKey(
                "escrow.hold.default.seconds",
                Long.toString(totalSeconds)
        );

        if (updated) {
            session.setAttribute("flash",
                    "Đã cập nhật thời gian giữ tiền escrow thành "
                            + formatEscrowDuration(hours, minutes, seconds) + ".");
        } else {
            session.setAttribute("flashError",
                    "Không thể lưu cấu hình thời gian escrow. Vui lòng thử lại sau.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/systems");
    }

    private String formatEscrowDuration(int hours, int minutes, int seconds) {
        StringBuilder label = new StringBuilder();
        if (hours > 0) label.append(hours).append(" giờ");
        if (minutes > 0) {
            if (label.length() > 0) label.append(' ');
            label.append(minutes).append(" phút");
        }
        if (seconds > 0 || label.length() == 0) {
            if (label.length() > 0) label.append(' ');
            label.append(seconds).append(" giây");
        }
        return label.toString();
    }
}
