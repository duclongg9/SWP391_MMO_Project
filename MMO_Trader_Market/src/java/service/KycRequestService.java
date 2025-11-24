/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conf.AppConfig;
import dao.user.KYCRequestDAO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import utils.ImageUtils;

/**
 *
 * @author D E L L
 */
public class KycRequestService {

    private static final String RELATIVE_UPLOAD_DIR = normalizeRelativeDir(AppConfig.get("upload.kyc.relative"));
    private static final String ABSOLUTE_UPLOAD_DIR = AppConfig.get("upload.kyc.absolute");
    private static final KYCRequestDAO kdao = new KYCRequestDAO();


    public boolean handleKycRequest(ServletContext context, int userId, String idNumber,
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

        Path uploadDir = resolveUploadDir(context);
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
    /**
     * Builds an absolute upload directory using configuration values, with a
     * fallback to the servlet context's real path when the absolute path is
     * not provided. Always returns a normalized path to ensure compatibility
     * across operating systems.
     *
     * @param context current {@link ServletContext} from the controller
     * @return normalized {@link Path} pointing to the upload directory
     */
    private static Path resolveUploadDir(ServletContext context) {
        String configuredAbsolute = normalizePath(ABSOLUTE_UPLOAD_DIR);

        if (configuredAbsolute != null && !configuredAbsolute.isBlank()) {
            Path configuredPath = Paths.get(configuredAbsolute);
            if (configuredPath.isAbsolute()) {
                return configuredPath;
            }
        }

        String realPath = context.getRealPath(RELATIVE_UPLOAD_DIR);
        if (realPath == null || realPath.isBlank()) {
            return Paths.get(RELATIVE_UPLOAD_DIR).toAbsolutePath();
        }
        return Paths.get(realPath).toAbsolutePath().normalize();
    }

    /**
     * Normalizes relative directory values to use forward slashes and ensures
     * a leading slash for predictable URL generation and filesystem access.
     *
     * @param path configured relative path
     * @return normalized relative path beginning with '/'
     */
    private static String normalizeRelativeDir(String path) {
        String normalized = normalizePath(path);
        if (normalized == null || normalized.isBlank()) {
            normalized = "/assets/images/kyc";
        }
        normalized = normalized.replaceAll("^/+", "/");
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    /**
     * Normalizes path separators to forward slashes for consistency.
     *
     * @param path input path string
     * @return normalized path or {@code null} when the input is blank
     */
    private static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replace('\\', '/');
    }
   
}
