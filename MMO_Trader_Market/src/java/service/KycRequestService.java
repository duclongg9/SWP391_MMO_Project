/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conf.AppConfig;
import dao.user.KYCRequestDAO;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import utils.ImageUtils;

/**
 *
 * @author D E L L
 */
public class KycRequestService {

    private static final String RELATIVE_UPLOAD_DIR = "/assets/images/kyc";
    private static final String ABSOLUTE_UPLOAD_DIR = "D:/Chuyen_nganh/ky5/SWP391-2/SWP391_server/SWP391_MMO_Project/MMO_Trader_Market/web/assets/images/kyc";
    private static final KYCRequestDAO kdao = new KYCRequestDAO();
    
//    private static final String RELATIVE_UPLOAD_DIR = "/assets/images/kyc";
//    private static final String ABSOLUTE_UPLOAD_DIR = "D:/Chuyen_nganh/ky5/SWP391-2/SWP391_server/SWP391_MMO_Project/MMO_Trader_Market/web/assets/images/kyc";
            

    public boolean handleKycRequest(int userId, String idNumber,
            Part front, Part back, Part selfie) throws IOException {

        // Validate MIME
        if (!ImageUtils.isAllowedImage(front.getContentType())
                || !ImageUtils.isAllowedImage(back.getContentType())
                || !ImageUtils.isAllowedImage(selfie.getContentType())) {
            throw new IOException("Chỉ chấp nhận ảnh JPG, PNG, hoặc WebP.");
        }

        // Kiểm tra người dùng đã gửi KYC chưa 
        if (kdao.checkKycRequest(userId) > 0) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu rồi, vui lòng chờ duyệt.");
        }

        Path uploadDir = Paths.get(ABSOLUTE_UPLOAD_DIR);
        Files.createDirectories(uploadDir);

        // Tạo tên file duy nhất
        String frontFileName = "kyc_front_" + UUID.randomUUID() + ImageUtils.extFromMime(front.getContentType());
        String backFileName = "kyc_back_" + UUID.randomUUID() + ImageUtils.extFromMime(back.getContentType());
        String selfieFileName = "kyc_selfie_" + UUID.randomUUID() + ImageUtils.extFromMime(selfie.getContentType());

        Path frontPath = uploadDir.resolve(frontFileName);
        Path backPath = uploadDir.resolve(backFileName);
        Path selfiePath = uploadDir.resolve(selfieFileName);

        // Ghi file
        try {
            front.write(frontPath.toString());
            back.write(backPath.toString());
            selfie.write(selfiePath.toString());
        } catch (Exception e) {
            throw new IOException("Không thể lưu file ảnh lên máy chủ.", e);
        }

        // Lưu DB
        String frontRel = RELATIVE_UPLOAD_DIR + "/" + frontFileName;
        String backRel = RELATIVE_UPLOAD_DIR + "/" + backFileName;
        String selfieRel = RELATIVE_UPLOAD_DIR + "/" + selfieFileName;

        int rows = kdao.createdKycRequest(userId, frontRel, backRel, selfieRel, idNumber);
        if (rows <= 0) {
            Files.deleteIfExists(frontPath);
            Files.deleteIfExists(backPath);
            Files.deleteIfExists(selfiePath);
            throw new IOException("Tạo yêu cầu KYC thất bại khi ghi cơ sở dữ liệu.");
        }

        return true;
    }
    public static void main(String[] args) {
        System.out.println(AppConfig.get("upload.kyc.relative"));
    }
   
}
