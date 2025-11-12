/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vnpay.common;

import com.google.gson.JsonObject;
import dao.user.DepositeRequestDAO;
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
import service.WalletService;

/**
 *
 * @author CTT VNPAY
 */
public class vnpayRefund extends HttpServlet {

    DepositeRequestDAO dtdao = new DepositeRequestDAO();
    WalletsDAO wdao = new WalletsDAO();

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

        boolean paidOk = "00".equals(respCode) && "00".equals(txnStatus) && hashOk && "P8MN1RS8".equals(tmn);

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
            // mark deposit completed + tăng ví
            int userId = 0;
            try {
                userId = dtdao.getUserIdBydeposit(depositId);
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                dtdao.UpdateDepositStatus(depositId, 2); // Completed
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                wdao.increaseBalance(userId, amountVnd);
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
            req.setAttribute("transResult", true);
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

        //Command: refund
        String vnp_RequestId = Config.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = Config.vnp_TmnCode;
        String vnp_TransactionType = req.getParameter("trantype");
        String vnp_TxnRef = req.getParameter("order_id");
        long amount = Integer.parseInt(req.getParameter("amount")) * 100;
        String vnp_Amount = String.valueOf(amount);
        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
        String vnp_TransactionNo = ""; //Assuming value of the parameter "vnp_TransactionNo" does not exist on your system.
        String vnp_TransactionDate = req.getParameter("trans_date");
        String vnp_CreateBy = req.getParameter("user");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        String vnp_IpAddr = Config.getIpAddress(req);

        JsonObject vnp_Params = new JsonObject();

        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
        vnp_Params.addProperty("vnp_Version", vnp_Version);
        vnp_Params.addProperty("vnp_Command", vnp_Command);
        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);

        if (vnp_TransactionNo != null && !vnp_TransactionNo.isEmpty()) {
            vnp_Params.addProperty("vnp_TransactionNo", "{get value of vnp_TransactionNo}");
        }

        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransactionDate);
        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

        String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hash_Data.toString());

        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

        URL url = new URL(Config.vnp_ApiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(vnp_Params.toString());
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("nSending 'POST' request to URL : " + url);
        System.out.println("Post Data : " + vnp_Params);
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        //Ghi bản ghi để đối chiếu
        // 1) Xác định deposit_request_id từ vnp_TxnRef
        int depositId = Integer.parseInt(vnp_TxnRef);
        int user = 0;
        try {
            user = dtdao.getUserIdBydeposit(depositId);
        } catch (SQLException ex) {
            Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
        }

        com.google.gson.JsonObject resJson;
        try {
            // Parse JSON trả về từ VNPay (nếu không phải JSON thì lưu raw)

            try {
                resJson = com.google.gson.JsonParser.parseString(response.toString()).getAsJsonObject();
            } catch (Exception ignore) {
                resJson = new com.google.gson.JsonObject();
                resJson.addProperty("raw", response.toString());
                System.out.println("chạy đến đây");
            }

            // Suy ra status tổng quát: "success" khi vnp_ResponseCode == "00", ngược lại "failed"
            final String respCode = resJson.has("vnp_ResponseCode")
                    ? resJson.get("vnp_ResponseCode").getAsString()
                    : null;
            final String status = "00".equals(respCode) ? "success" : "failed";

            // Build JSON audit (link_data) đầy đủ ngữ cảnh
            com.google.gson.JsonObject linkData = new com.google.gson.JsonObject();
            linkData.addProperty("direction", "refund");         // loại thao tác
            linkData.addProperty("source", "vnpayRefund");       // servlet/handler nguồn
            linkData.addProperty("remote_ip", vnp_IpAddr);       // IP phía client khi gọi
            linkData.addProperty("http_status", responseCode);   // mã HTTP từ VNPay
            linkData.add("request", vnp_Params);                // payload đã gửi đi
            linkData.add("response", resJson);                   // payload nhận về

            // Một số trường tiện tra soát (nếu có trong response)
            if (respCode != null) {
                linkData.addProperty("vnp_ResponseCode", respCode);
            }
            if (resJson.has("vnp_Message")) {
                linkData.addProperty("vnp_Message", resJson.get("vnp_Message").getAsString());
            }
            if (resJson.has("vnp_TransactionNo")) {
                linkData.addProperty("vnp_TransactionNo", resJson.get("vnp_TransactionNo").getAsString());
            }
            if (resJson.has("vnp_PayDate")) {
                linkData.addProperty("vnp_PayDate", resJson.get("vnp_PayDate").getAsString());
            }

            // Ghi DB qua DAO
            dao.vnpay.vnpayTransactionDAO txDao = new dao.vnpay.vnpayTransactionDAO();
            int affected = txDao.createVnpayTransaction(depositId, linkData.toString(), status);
            System.out.println("[VNPayRefund][AUDIT] rows=" + affected
                    + " depositId=" + depositId + " status=" + status);

        } catch (NumberFormatException nf) {
            System.err.println("[VNPayRefund][AUDIT] depositId không hợp lệ: " + depositId);
        } catch (Exception e) {
            System.err.println("[VNPayRefund][AUDIT][ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        //Cập nhật trạng thái cho bản ghi deposit
        try {
            resJson = com.google.gson.JsonParser.parseString(response.toString()).getAsJsonObject();
        } catch (Exception ignore) {
            resJson = new com.google.gson.JsonObject();
            resJson.addProperty("raw", response.toString());
        }
        BigDecimal amountVnd = new BigDecimal(req.getParameter("amount"));
        boolean transactionSuccess = false;
        if ("00".equals(resJson.has("vnp_ResponseCode") ? resJson.get("vnp_ResponseCode").getAsString() : null)) {
            try {
                int results = dtdao.UpdateDepositStatus(depositId, 2);
                wdao.increaseBalance(user, amountVnd);
                transactionSuccess = true;
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                int results = dtdao.UpdateDepositStatus(depositId, 3);
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        in.close();
        System.out.println(response.toString());

        req.getSession().setAttribute("transResult", transactionSuccess);
        resp.sendRedirect(req.getContextPath() + "/vnpayRefund");
    }

}
