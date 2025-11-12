/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vnpay.common;

import com.google.gson.JsonObject;
import dao.user.DepositeRequestDAO;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.TransactionType;
import service.WalletService;

/**
 *
 * @author CTT VNPAY
 */
public class vnpayRefund extends HttpServlet {

    DepositeRequestDAO dtdao = new DepositeRequestDAO();
    WalletsDAO wdao = new WalletsDAO();
    WalletTransactionDAO wtdao = new WalletTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, String[]> raw = req.getParameterMap();
        Map<String, String> params = new TreeMap<>();
        for (var e : raw.entrySet()) {
            if (e.getValue() != null && e.getValue().length > 0) {
                params.put(e.getKey(), e.getValue()[0]);
            }
        }

        String secureHash = params.remove("vnp_SecureHash"); // bỏ ra để build data
        // build data = key=value&key=value... (đã sort)
        StringBuilder data = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
            var en = it.next();
            data.append(en.getKey()).append("=")
                    .append(java.net.URLEncoder.encode(en.getValue(), java.nio.charset.StandardCharsets.UTF_8));
            if (it.hasNext()) {
                data.append("&");
            }
        }
        String calc = Config.hmacSHA512(Config.secretKey, data.toString());
        boolean hashOk = calc.equalsIgnoreCase(secureHash);

        String respCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");
        String tmn = params.get("vnp_TmnCode");
        String txnRef = params.get("vnp_TxnRef");
        String amountStr = params.get("vnp_Amount"); // đơn vị x100

        boolean paidOk = "00".equals(respCode)
        && "00".equals(txnStatus)
        && hashOk
        && Config.vnp_TmnCode.equals(tmn);

        // map deposit id
        int depositId = Integer.parseInt(txnRef);
        BigDecimal amountVnd = new BigDecimal(amountStr).divide(new BigDecimal(100)); // về VND

        // === Audit JSON link_data ===
        com.google.gson.JsonObject link = new com.google.gson.JsonObject();
        link.addProperty("direction", "return");
        link.addProperty("source", "VnpayReturnServlet");
        link.addProperty("hash_verified", hashOk);
        link.addProperty("vnp_ResponseCode", respCode);
        link.addProperty("vnp_TransactionStatus", txnStatus);
        link.addProperty("vnp_TmnCode", tmn);
        link.addProperty("vnp_TxnRef", txnRef);
        link.addProperty("vnp_Amount", amountStr);

        // Ghi audit + cập nhật DB
        dao.vnpay.vnpayTransactionDAO txDao = new dao.vnpay.vnpayTransactionDAO();
        try {
            txDao.createVnpayTransaction(depositId, link.toString(), paidOk ? "success" : "failed");
        } catch (SQLException ex) {
            Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (paidOk) {
            try {
                int userId = dtdao.getUserIdBydeposit(depositId);

                //Lấy before TRƯỚC khi + tiền
                BigDecimal before = wdao.getWalletBalanceByUserId(userId);

                //Cộng tiền
                wdao.increaseBalance(userId, amountVnd);

                //Tính after
                BigDecimal after = before.add(amountVnd);

                // 4) Lấy walletId để log correct
                int walletId = wdao.getUserWallet(userId).getId();

                // 5) Ghi transaction log 
                wtdao.insertDepositWalletTransaction(walletId, amountVnd, before, after);

                // 6) Cập nhật trạng thái deposit
                dtdao.UpdateDepositStatus(depositId, 2); // Completed

                req.setAttribute("transResult", true);
                
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
                req.setAttribute("transResult", null);
                return;
            }
        } else {
            try {
                dtdao.UpdateDepositStatus(depositId, 3); // Failed
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
            req.setAttribute("transResult", false);
        }

        req.getRequestDispatcher("/WEB-INF/views/wallet/paymentResult.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

}
