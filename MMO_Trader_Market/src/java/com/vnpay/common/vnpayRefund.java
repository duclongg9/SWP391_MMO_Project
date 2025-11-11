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
         Integer user = (Integer) req.getSession().getAttribute("userId");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }
        
        Object flash = req.getSession().getAttribute("transResult");
        if (flash != null) {
            req.setAttribute("transResult", flash);
            req.getSession().removeAttribute("transResult");
        }
        req.getRequestDispatcher("/WEB-INF/views/wallet/paymentResult.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Integer user = (Integer) req.getSession().getAttribute("userId");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }
        
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
        in.close();
        System.out.println(response.toString());
        
         //Ghi bản ghi để đối chiếu
         // 1) Xác định deposit_request_id từ vnp_TxnRef
        int depositId = Integer.parseInt(vnp_TxnRef);
       
        try {

            // 2) Gói request + response thành JSON audit
            com.google.gson.JsonObject linkData = new com.google.gson.JsonObject();
            linkData.addProperty("direction", "refund");            // phân biệt loại thao tác
            linkData.addProperty("source", "vnpayRefund");          // servlet nguồn
            linkData.addProperty("remote_ip", vnp_IpAddr);
            linkData.addProperty("http_status", responseCode);
            linkData.add("request", vnp_Params);                    // payload đã gửi

            com.google.gson.JsonObject resJson;
            try {
                resJson = com.google.gson.JsonParser
                        .parseString(response.toString())
                        .getAsJsonObject();
            } catch (Exception ignore) {
                // response không phải JSON (hoặc 4xx/5xx text) -> lưu raw
                resJson = new com.google.gson.JsonObject();
                resJson.addProperty("raw", response.toString());
            }
            linkData.add("response", resJson);                      // payload nhận về

            // 3) Suy ra status: 00 = success, còn lại failed (tuỳ bạn muốn mapping chi tiết hơn)
            String status = (resJson.has("vnp_ResponseCode")
                    && "00".equals(resJson.get("vnp_ResponseCode").getAsString()))
                    ? "success" : "failed";

            // 4) Ghi vào bảng qua DAO (đã viết trước đó)
            dao.vnpay.vnpayTransactionDAO txDao = new dao.vnpay.vnpayTransactionDAO();
            int affected = txDao.createVnpayTransaction(depositId, linkData.toString(), status);
            System.out.println("[VNPayRefund] audit rows affected = " + affected);

        } catch (NumberFormatException nf) {
            // vnp_TxnRef không phải số -> nếu bạn map theo kiểu khác, thay thế đoạn parse ở trên
            System.err.println("[VNPayRefund] Cannot parse vnp_TxnRef to deposit_id: " + vnp_TxnRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        
         //Cập nhật trạng thái cho bản ghi deposit
         
        BigDecimal amounts = BigDecimal.valueOf(amount);
        boolean transactionSuccess = false;
        if("00".equals(req.getParameter("vnp_TransactionStatus"))){
            try {
                int results = dtdao.UpdateDepositStatus(depositId, 2);
                wdao.increaseBalance(user,amounts);
                transactionSuccess = true;
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else {
            try {
                int results = dtdao.UpdateDepositStatus(depositId, 3);
            } catch (SQLException ex) {
                Logger.getLogger(vnpayRefund.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        req.getSession().setAttribute("transResult", transactionSuccess);
        resp.sendRedirect(req.getContextPath()+"/vnpayRefund");
    }
    
    
   
}
