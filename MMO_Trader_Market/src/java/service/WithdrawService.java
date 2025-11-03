/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.user.WithdrawRequestDAO;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.UUID;

/**
 *
 * @author D E L L
 */
public class WithdrawService {
    
    WithdrawRequestDAO wrdao = new WithdrawRequestDAO();

    public static String buildUrl(String bankIdOrBin, String accountNo,
            long amountVnd, String addInfo, String accountName) {
        if (bankIdOrBin == null || bankIdOrBin.isBlank()) {
            throw new IllegalArgumentException("Ngân hàng không được để trống");
        }
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("Số tài khoản không được để trống");
        }
        if (amountVnd < 100000) {
            throw new IllegalArgumentException("Số tiền chuyển phải lớn hơn hoặc bằng 100k ");
        }

        // Chuẩn hóa chuỗi: loại bỏ khoảng trắng dư, null thì thay bằng chuỗi rỗng
        addInfo = (addInfo == null) ? "" : addInfo.trim();
        accountName = (accountName == null) ? "" : accountName.trim().replaceAll("\\s+", " ").toUpperCase();
        

        
        String base = "https://img.vietqr.io/image/%s-%s-compact2.png";
        String url = String.format(base, bankIdOrBin, accountNo);

        //Mã hóa tham số để an toàn khi đưa lên URL
        String qAmount = "amount=" + amountVnd;
        String qInfo = "addInfo=" + enc(addInfo);
        String qName = "accountName=" + accountName;

        //Ghép toàn bộ URL hoàn chỉnh
        return url + "?" + qAmount + "&" + qInfo + "&" + qName;
    }

    /**
     * Tải ảnh PNG từ URL về đĩa
     */
    public static String downloadPng(String url, Path folder) throws Exception {
        //Tạo thư mục nếu chưa có
        Files.createDirectories(folder);

        // Sinh tên file ngẫu nhiên chuẩn UUID
        String fileName = UUID.randomUUID().toString() + ".png";
        Path saveTo = folder.resolve(fileName);

        //Tạo HttpClient
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        //Tạo request tải file
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        //Gửi và ghi thẳng vào file
        HttpResponse<Path> resp = client.send(req, HttpResponse.BodyHandlers.ofFile(saveTo));

        int code = resp.statusCode();
        if (code / 100 != 2) {
            Files.deleteIfExists(saveTo);
            throw new IllegalStateException("Download QR failed, HTTP " + code);
        }

        return fileName;
    }

    private static String enc(String s) {
        if (s == null) {
            return "";
        }
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
    
    //Tạo Withdraw Request
    public int createWithdrawRequest(int userId, long amount, String bankAccountInfo){
        if (amount < 100000) {
            throw new IllegalArgumentException("Số tiền chuyển phải lớn hơn hoặc bằng 100k ");
        }
        int results =  wrdao.createWithDrawRequest(userId, amount, bankAccountInfo);
        if(results < 0 ){
            throw new IllegalStateException("Tạo yêu cầu thất bại");
        }
        return results;
    }
}
