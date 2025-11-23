package admin;

import dao.admin.CashDAO;
import dao.connect.DBConnect;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.CashTxn;
import model.TransactionType;
import model.Wallets;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Comparator;
import java.util.List;

@WebServlet(name = "AdminCashsServlet",
        urlPatterns = {"/admin/cashs", "/admin/cashs/status"})
@MultipartConfig
public class AdminCashsServlet extends AbstractAdminServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleListCashs(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/cashs/status")) {
            handleCashsStatus(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    // ===== GET: list + filter + sort + paging =====
    private void handleListCashs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Tham số khớp với JSP
        String type   = clean(req.getParameter("type"));   // all | Deposit | Withdrawal
        String q      = clean(req.getParameter("q"));      // userName (search)
        String status = clean(req.getParameter("status")); // all | Pending | Completed | Rejected
        String sort   = clean(req.getParameter("sort"));   // date_asc|date_desc|status_asc|status_desc
        String fromS  = clean(req.getParameter("from"));   // yyyy-MM-dd
        String toS    = clean(req.getParameter("to"));     // yyyy-MM-dd

        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) size = DEFAULT_SIZE;

        // Parse khoảng ngày (createdAt). toS được +1 ngày để inclusive
        java.util.Date fromDate = null, toDate = null;
        try {
            if (fromS != null && !fromS.isBlank()) {
                java.time.LocalDate d = java.time.LocalDate.parse(fromS);
                fromDate = java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            if (toS != null && !toS.isBlank()) {
                java.time.LocalDate d = java.time.LocalDate.parse(toS).plusDays(1); // inclusive
                toDate = java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception ignored) {}

        try (Connection con = DBConnect.getConnection()) {
            CashDAO dao = new CashDAO(con);
            List<CashTxn> txns = dao.listAll(); // tụi mình sẽ lọc/sort trên memory, sau có thể chuyển về SQL

            // Lọc loại
            if (type != null && !"all".equalsIgnoreCase(type)) {
                String t = type;
                txns = txns.stream().filter(x -> t.equalsIgnoreCase(x.getType())).toList();
            }

            // Lọc tên
            if (q != null && !q.isBlank()) {
                String qLower = q.toLowerCase();
                txns = txns.stream()
                        .filter(x -> x.getUserName() != null && x.getUserName().toLowerCase().contains(qLower))
                        .toList();
            }

            // Lọc trạng thái
            if (status != null && !"all".equalsIgnoreCase(status)) {
                String s = status;
                txns = txns.stream().filter(x -> s.equalsIgnoreCase(x.getStatus())).toList();
            }

            // Lọc theo khoảng ngày (createdAt)
            if (fromDate != null) {
                java.util.Date fd = fromDate;
                txns = txns.stream()
                        .filter(x -> x.getCreatedAt() != null && !x.getCreatedAt().before(fd))
                        .toList();
            }
            if (toDate != null) {
                java.util.Date td = toDate; // exclusive (đã +1 ngày)
                txns = txns.stream()
                        .filter(x -> x.getCreatedAt() != null && x.getCreatedAt().before(td))
                        .toList();
            }

            // Sắp xếp
            String effSort = (sort == null || sort.isBlank()) ? "date_desc" : sort;
            Comparator<CashTxn> byDate = Comparator.comparing(
                    x -> x.getCreatedAt() == null ? new java.util.Date(0) : x.getCreatedAt());
            Comparator<CashTxn> byStatus = Comparator.comparing(
                    x -> x.getStatus() == null ? "" : x.getStatus(), String.CASE_INSENSITIVE_ORDER);

            switch (effSort) {
                case "date_asc"    -> txns = txns.stream().sorted(byDate).toList();
                case "status_asc"  -> txns = txns.stream().sorted(byStatus.thenComparing(byDate.reversed())).toList();
                case "status_desc" -> txns = txns.stream().sorted(byStatus.reversed().thenComparing(byDate.reversed())).toList();
                default /* date_desc */ -> txns = txns.stream().sorted(byDate.reversed()).toList();
            }

            // Paging
            int total = txns.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int from = Math.max(0, (page - 1) * size);
            int to   = Math.min(total, from + size);
            List<CashTxn> pageList = txns.subList(from, to);

            // Data cho JSP
            req.setAttribute("txList", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page",  page);
            req.setAttribute("pg_size",  size);
            // pg_pages/pg_isFirst/pg_isLast/pg_single là optional với JSP hiện tại, có cũng không sao
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast",  page >= pages);
            req.setAttribute("pg_single",  pages <= 1);

        } catch (Exception e) {
            throw new ServletException("Lỗi tải giao dịch: " + e.getMessage(), e);
        }

        // Trả lại filter state đúng tên để form giữ giá trị
        req.setAttribute("f_type",   type   == null ? "all"      : type);
        req.setAttribute("f_q",      q      == null ? ""         : q);
        req.setAttribute("f_status", status == null ? "all"      : status);
        req.setAttribute("sort",     (sort == null || sort.isBlank()) ? "date_desc" : sort);
        req.setAttribute("from",     fromS == null ? ""          : fromS);
        req.setAttribute("to",       toS   == null ? ""          : toS);

        // Layout
        req.setAttribute("pageTitle", "Nạp / Rút");
        req.setAttribute("active", "cashs");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/cashs.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    // ===== POST: xử lý trạng thái (giữ nguyên cấu trúc, chỉ chỉnh rất nhỏ về flash/message nếu cần) =====
    private void handleCashsStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");

        String idStr   = safe(req.getParameter("id"));
        String action  = safe(req.getParameter("action"));   // accept | reject
        String note    = safe(req.getParameter("note"));
        String txTypeP = safe(req.getParameter("txType"));   // Deposit | Withdrawal

        if (idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Lỗi: ID giao dịch không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/cashs");
            return;
        }
        if (!"accept".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action)) {
            req.getSession().setAttribute("flash", "Lỗi: Hành động không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/cashs");
            return;
        }
        if (txTypeP == null ||
                !(txTypeP.equalsIgnoreCase("Deposit") || txTypeP.equalsIgnoreCase("Withdrawal"))) {
            req.getSession().setAttribute("flash", "Lỗi: Loại giao dịch không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/cashs");
            return;
        }

        int txId = Integer.parseInt(idStr);
        boolean requireNote =
                "reject".equalsIgnoreCase(action)
                        || ("accept".equalsIgnoreCase(action) && "Deposit".equalsIgnoreCase(txTypeP));

        if (requireNote && (note == null || note.isBlank())) {
            req.setAttribute("cashErrorId", txId);
            req.setAttribute("cashErrorMessage",
                    "Vui lòng nhập ghi chú cho hành động này (bắt buộc).");
            handleListCashs(req, resp);
            return;
        }

        String adminProofUrl = null;
        try {
            String ct = req.getContentType();
            if (ct != null && ct.toLowerCase().startsWith("multipart/")) {
                Part part = req.getPart("proof_file");
                if (part != null && part.getSize() > 0) {
                    String original = part.getSubmittedFileName();
                    String cleanName = java.nio.file.Paths.get(
                            original == null ? "" : original).getFileName().toString();
                    String fileName = System.currentTimeMillis() + "_" + cleanName;
                    String uploadDir = req.getServletContext()
                            .getRealPath("/uploads/withdrawals");
                    java.nio.file.Files.createDirectories(java.nio.file.Path.of(uploadDir));
                    java.nio.file.Path dest = java.nio.file.Path.of(uploadDir, fileName);
                    part.write(dest.toString());
                    adminProofUrl = req.getContextPath()
                            + "/uploads/withdrawals/" + fileName;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try (Connection con = DBConnect.getConnection()) {
            con.setAutoCommit(false);

            CashDAO dao = new CashDAO(con);
            CashTxn tx = dao.findById(txId, txTypeP);
            if (tx == null) {
                con.rollback();
                req.getSession().setAttribute("flash",
                        "Lỗi: Không tìm thấy giao dịch #" + txId);
                resp.sendRedirect(req.getContextPath() + "/admin/cashs");
                return;
            }

            String type   = tx.getType();
            String status = tx.getStatus();
            int userId    = tx.getUserId();
            BigDecimal amount = tx.getAmount();

            if (!txTypeP.equalsIgnoreCase(type)) {
                con.rollback();
                req.getSession().setAttribute("flash",
                        "Lỗi: Loại giao dịch không khớp dữ liệu.");
                resp.sendRedirect(req.getContextPath() + "/admin/cashs");
                return;
            }

            if (!"Pending".equalsIgnoreCase(status)) {
                con.rollback();
                req.getSession().setAttribute("flash",
                        "Giao dịch #" + txId + " đã được xử lý trước đó.");
                resp.sendRedirect(req.getContextPath() + "/admin/cashs");
                return;
            }

            // Accept Withdrawal cần proof
            if ("accept".equalsIgnoreCase(action) && "Withdrawal".equalsIgnoreCase(type)) {
                String existingProof = tx.getAdminProofUrl();
                if (adminProofUrl == null || adminProofUrl.isBlank()) {
                    adminProofUrl = existingProof;
                }
                if (adminProofUrl == null || adminProofUrl.isBlank()) {
                    con.rollback();
                    req.setAttribute("cashErrorId", txId);
                    req.setAttribute("cashErrorMessage",
                            "Vui lòng upload ảnh chuyển khoản của admin trước khi duyệt rút tiền.");
                    handleListCashs(req, resp);
                    return;
                }
            }

            int rowsTx = 0;
            int rowsWal = 0;

            if ("accept".equalsIgnoreCase(action)) {
                if ("Deposit".equalsIgnoreCase(type)) {
                    rowsWal = dao.updateWalletPlus(userId, amount);
                    rowsTx  = dao.updateDepositStatus(txId, "Completed", note);
                    req.getSession().setAttribute("flash",
                            (rowsWal > 0 && rowsTx > 0)
                                    ? "Duyệt nạp tiền #" + txId + " thành công."
                                    : "Không có thay đổi khi duyệt nạp.");
                }else { // Withdrawal
                    rowsTx = dao.updateWithdrawalStatus(txId, "Completed", note, adminProofUrl);

                    if (rowsTx > 0) {
                        try {
                            // DAO ví & transaction
                            WalletsDAO wdao = new WalletsDAO();
                            WalletTransactionDAO wtdao = new WalletTransactionDAO();

                            // 1) Lấy ví của user
                            Wallets wallet = wdao.getUserWallet(userId);
                            int walletId = wallet.getId();

                            // 2) Lấy số dư hiện tại của ví
                            //    (đây là balance sau giao dịch, dùng để log)
                            BigDecimal balanceAfter = wdao.getWalletBalanceByUserId(userId);

                            // 3) Tính số dư trước giao dịch để ghi log
                            BigDecimal balanceBefore = balanceAfter.add(amount);

                            // 4) Số tiền log trong wallet_transactions
                            //    bạn đang để Withdrawal là số âm, nên:
                            BigDecimal txnAmount = amount.negate();

                            // 5) Ghi 1 bản ghi sang wallet_transactions
                            wtdao.insertTransaction(
                                    con,                      // dùng luôn connection hiện tại
                                    walletId,
                                    txId,                     // related_entity_id = id bên cash/withdraw
                                    TransactionType.WITHDRAWAL,
                                    txnAmount,
                                    balanceBefore,
                                    balanceAfter,
                                    "Rút tiền #" + txId      // note
                            );
                        } catch (Exception ex) {
                            // log lỗi, nhưng không rollback việc duyệt rút
                            ex.printStackTrace();
                        }
                    }

                    req.getSession().setAttribute("flash",
                            rowsTx > 0
                                    ? "Duyệt rút tiền #" + txId + " thành công."
                                    : "Không có thay đổi khi duyệt rút.");
                }
            } else { // reject
                if ("Deposit".equalsIgnoreCase(type)) {
                    rowsTx = dao.updateDepositStatus(txId, "Rejected", note);
                    req.getSession().setAttribute("flash",
                            rowsTx > 0
                                    ? "Đã từ chối nạp tiền #" + txId + "."
                                    : "Không có thay đổi khi từ chối nạp.");
                } else { // Withdrawal
                    BigDecimal fee = new BigDecimal("10000");
                    BigDecimal refund = amount.subtract(fee);
                    if (refund.compareTo(BigDecimal.ZERO) < 0) {
                        refund = BigDecimal.ZERO;
                    }
                    rowsWal = dao.updateWalletPlus(userId, refund);
                    rowsTx  = dao.updateWithdrawalStatus(txId, "Rejected", note, adminProofUrl);
                    if (rowsWal > 0 && rowsTx > 0) {
                        req.getSession().setAttribute("flash",
                                "Đã từ chối rút tiền #" + txId +
                                        " và hoàn " + refund.toPlainString() +
                                        " (đã trừ phí 10.000).");
                    } else {
                        req.getSession().setAttribute("flash",
                                "Không có thay đổi khi từ chối rút tiền #" + txId + ".");
                    }
                }
            }

            if (rowsTx > 0 || rowsWal > 0) {
                con.commit();
            } else {
                con.rollback();
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash",
                    "Lỗi hệ thống: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/cashs");
    }

}
