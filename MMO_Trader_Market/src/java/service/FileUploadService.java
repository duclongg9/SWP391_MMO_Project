package service;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service xử lý upload và quản lý file
 */
public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads";
    private static final String KYC_DIR = "kyc";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    private static final String[] ALLOWED_MIME_TYPES = {"image/jpeg", "image/png"};

    /**
     * Upload file KYC (CCCD hoặc selfie)
     */
    public String uploadKycImage(Part filePart, int userId, String imageType) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file
        validateImageFile(filePart);

        // Tạo thư mục nếu chưa có
        String uploadPath = createUploadDirectory(userId);

        // Tạo tên file unique
        String originalFileName = getFileName(filePart);
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = imageType + "_" + UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = Paths.get(uploadPath, newFileName);
        Files.copy(filePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về relative path để lưu vào database
        return KYC_DIR + "/" + userId + "/" + newFileName;
    }

    /**
     * Xóa file KYC cũ khi upload file mới
     */
    public void deleteKycImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(UPLOAD_DIR, imagePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error nhưng không throw exception
            System.err.println("Không thể xóa file: " + imagePath + " - " + e.getMessage());
        }
    }

    /**
     * Kiểm tra file có tồn tại không
     */
    public boolean fileExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        
        Path filePath = Paths.get(UPLOAD_DIR, imagePath);
        return Files.exists(filePath);
    }

    /**
     * Lấy đường dẫn tuyệt đối của file
     */
    public String getAbsolutePath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        
        Path filePath = Paths.get(UPLOAD_DIR, imagePath);
        return filePath.toAbsolutePath().toString();
    }

    /**
     * Lấy URL để hiển thị ảnh (cho web)
     */
    public String getImageUrl(String imagePath, String contextPath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        
        return contextPath + "/uploads/" + imagePath;
    }

    private void validateImageFile(Part filePart) throws IOException {
        // Kiểm tra kích thước file
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa là 5MB");
        }

        // Kiểm tra MIME type
        String contentType = filePart.getContentType();
        if (contentType == null || !isAllowedMimeType(contentType)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (.jpg, .jpeg, .png)");
        }

        // Kiểm tra extension
        String fileName = getFileName(filePart);
        if (fileName == null || !isAllowedExtension(fileName)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (.jpg, .jpeg, .png)");
        }
    }

    private String createUploadDirectory(int userId) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR, KYC_DIR, String.valueOf(userId));
        Files.createDirectories(uploadPath);
        return uploadPath.toString();
    }

    private String getFileName(Part filePart) {
        String contentDisposition = filePart.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String content : contentDisposition.split(";")) {
                if (content.trim().startsWith("filename")) {
                    return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    private boolean isAllowedMimeType(String mimeType) {
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedExtension(String fileName) {
        String extension = getFileExtension(fileName);
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
