package units;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Utility class để xử lý upload file.
 */
public class FileUploadUtil {
    
    private static final String UPLOAD_DIR = "assets/images/products";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    
    /**
     * Lưu file upload và trả về đường dẫn relative.
     *
     * @param filePart Part từ multipart form
     * @param uploadPath đường dẫn thực tế trên server (từ ServletContext.getRealPath)
     * @return đường dẫn relative của file (để lưu vào DB)
     * @throws IOException nếu có lỗi khi lưu file
     */
    public static String saveFile(Part filePart, String uploadPath) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        
        // Kiểm tra kích thước file
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File quá lớn. Kích thước tối đa: 10MB");
        }
        
        // Lấy tên file gốc
        String fileName = getFileName(filePart);
        String fileExtension = getFileExtension(fileName);
        
        // Kiểm tra extension hợp lệ
        if (!isAllowedExtension(fileExtension)) {
            throw new IOException("Định dạng file không hợp lệ. Chỉ chấp nhận: JPG, PNG, GIF, WEBP");
        }
        
        // Tạo tên file unique
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Tạo đường dẫn đầy đủ
        String fullUploadPath = uploadPath + File.separator + UPLOAD_DIR;
        File uploadDir = new File(fullUploadPath);
        
        // Tạo thư mục nếu chưa có
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Lưu file
        String filePath = fullUploadPath + File.separator + uniqueFileName;
        filePart.write(filePath);
        
        // Trả về đường dẫn relative (dùng / thay vì File.separator cho web path)
        return UPLOAD_DIR + "/" + uniqueFileName;
    }
    
    /**
     * Xóa file cũ khi upload file mới.
     *
     * @param oldFilePath đường dẫn relative của file cũ
     * @param applicationPath đường dẫn thực tế của application
     */
    public static void deleteFile(String oldFilePath, String applicationPath) {
        if (oldFilePath == null || oldFilePath.trim().isEmpty()) {
            return;
        }
        
        try {
            String fullPath = applicationPath + File.separator + oldFilePath.replace("/", File.separator);
            File file = new File(fullPath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Exception e) {
            // Log error nhưng không throw exception
            System.err.println("Không thể xóa file cũ: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tên file từ Part header.
     */
    private static String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return "unknown";
        }
        
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                String filename = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                // Lấy tên file cuối cùng (tránh path traversal)
                return new File(filename).getName();
            }
        }
        return "unknown";
    }
    
    /**
     * Lấy extension của file.
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot).toLowerCase();
        }
        return "";
    }
    
    /**
     * Kiểm tra extension có hợp lệ không.
     */
    private static boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}

