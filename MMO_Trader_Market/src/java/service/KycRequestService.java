package service;

import conf.AppConfig;
import dao.user.KYCRequestDAO;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import utils.ImageUtils;

/**
 * Lớp xử lý yêu cầu KYC
 */
public class KycRequestService {

    // Đường dẫn tương đối và tuyệt đối của thư mục lưu trữ ảnh KYC
    private static final String RELATIVE_UPLOAD_DIR = "/assets/images/kyc"; // Sử dụng dấu '/' thay vì '\'
    private static final String ABSOLUTE_UPLOAD_DIR = "E:/SWP391_MMO_Project/MMO_Trader_Market/web/assets/images/kyc";
   
    private static final KYCRequestDAO kdao = new KYCRequestDAO();

    /**
     * Xử lý yêu cầu KYC và lưu ảnh lên server
     * @param userId ID người dùng
     * @param idNumber Số giấy tờ của người dùng
     * @param front Ảnh mặt trước
     * @param back Ảnh mặt sau
     * @param selfie Ảnh selfie
     * @return true nếu xử lý thành công, false nếu có lỗi
     * @throws IOException Khi có lỗi trong quá trình lưu ảnh hoặc xử lý yêu cầu
     */
    public boolean handleKycRequest(int userId, String idNumber,
                                    Part front, Part back, Part selfie) throws IOException {

        // Kiểm tra loại MIME của ảnh (chỉ chấp nhận JPG, PNG, WebP)
        if (!ImageUtils.isAllowedImage(front.getContentType())
                || !ImageUtils.isAllowedImage(back.getContentType())
                || !ImageUtils.isAllowedImage(selfie.getContentType())) {
            throw new IOException("Chỉ chấp nhận ảnh JPG, PNG, hoặc WebP.");
        }

        // Kiểm tra người dùng đã gửi yêu cầu KYC chưa
        if (kdao.checkKycRequest(userId) > 0) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu rồi, vui lòng chờ duyệt.");
        }

        Path uploadDir = Paths.get(ABSOLUTE_UPLOAD_DIR);
        Files.createDirectories(uploadDir); // Tạo thư mục nếu chưa tồn tại

        // Tạo tên file duy nhất cho từng ảnh (mặt trước, mặt sau, selfie)
        String frontFileName = "kyc_front_" + UUID.randomUUID() + ImageUtils.extFromMime(front.getContentType());
        String backFileName = "kyc_back_" + UUID.randomUUID() + ImageUtils.extFromMime(back.getContentType());
        String selfieFileName = "kyc_selfie_" + UUID.randomUUID() + ImageUtils.extFromMime(selfie.getContentType());

        // Đường dẫn tuyệt đối của các file ảnh
        Path frontPath = uploadDir.resolve(frontFileName);
        Path backPath = uploadDir.resolve(backFileName);
        Path selfiePath = uploadDir.resolve(selfieFileName);

        // Ghi ảnh vào server
        try {
            front.write(frontPath.toString());
            back.write(backPath.toString());
            selfie.write(selfiePath.toString());
        } catch (Exception e) {
throw new IOException("Không thể lưu file ảnh lên máy chủ.", e);
        }

        // Lưu đường dẫn ảnh vào cơ sở dữ liệu
        String frontRel = RELATIVE_UPLOAD_DIR + "/" + frontFileName;
        String backRel = RELATIVE_UPLOAD_DIR + "/" + backFileName;
        String selfieRel = RELATIVE_UPLOAD_DIR + "/" + selfieFileName;

        // Thay thế dấu '\' bằng dấu '/' để đảm bảo đường dẫn chính xác trên web
        frontRel = frontRel.replace("\\", "/");
        backRel = backRel.replace("\\", "/");
        selfieRel = selfieRel.replace("\\", "/");

        // Ghi yêu cầu KYC vào cơ sở dữ liệu
        int rows = kdao.createdKycRequest(userId, frontRel, backRel, selfieRel, idNumber);
        if (rows <= 0) {
            // Nếu không thành công, xóa các file đã lưu
            Files.deleteIfExists(frontPath);
            Files.deleteIfExists(backPath);
            Files.deleteIfExists(selfiePath);
            throw new IOException("Tạo yêu cầu KYC thất bại khi ghi cơ sở dữ liệu.");
        }

        return true;
    }

    public static void main(String[] args) {
        // In ra giá trị cấu hình tải lên KYC (có thể dùng cho mục đích kiểm tra)
        System.out.println(AppConfig.get("upload.kyc.relative"));
    }
}
