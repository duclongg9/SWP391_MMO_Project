package units;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Utility class để xử lý upload file.
 */
public class FileUploadUtil {

    private static final String DEFAULT_UPLOAD_DIR = "assets/images/products";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
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
        return saveFile(filePart, uploadPath, DEFAULT_UPLOAD_DIR);
    }

    /**
     * Lưu file upload vào thư mục tùy chỉnh và trả về đường dẫn relative.
     *
     * @param filePart Part từ multipart form
     * @param uploadPath đường dẫn thực tế trên server (từ ServletContext.getRealPath)
     * @param targetRelativeDir thư mục con relative trong ứng dụng web để lưu file
     * @return đường dẫn relative của file (để lưu vào DB)
     * @throws IOException nếu có lỗi khi lưu file
     */
    public static String saveFile(Part filePart, String uploadPath, String targetRelativeDir) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        
        // Kiểm tra kích thước file
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File quá lớn. Kích thước tối đa: 5MB");
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
        String sanitizedRelativeDir = sanitizeRelativeDir(targetRelativeDir);
        String fullUploadPath = uploadPath + File.separator + sanitizedRelativeDir;
        File uploadDir = new File(fullUploadPath);

        // Tạo thư mục nếu chưa có
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Lưu file
        String filePath = fullUploadPath + File.separator + uniqueFileName;
        filePart.write(filePath);

        // Trả về đường dẫn relative (dùng / thay vì File.separator cho web path)
        String relativePath = sanitizedRelativeDir.replace(File.separatorChar, '/') + "/" + uniqueFileName;
        relativePath = relativePath.replace("\\", "/");
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        // Loại bỏ trường hợp nhiều dấu '/' liên tiếp khi ghép chuỗi.
        while (relativePath.contains("//")) {
            relativePath = relativePath.replace("//", "/");
        }
        return relativePath;
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
            String normalized = oldFilePath.replace('\', '/');
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            normalized = normalized.replace("//", "/");
            String fullPath = applicationPath + File.separator + normalized.replace("/", File.separator);
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

    /**
     * Chuẩn hóa đường dẫn relative để tránh truyền ký tự ../ gây truy cập ngoài thư mục cho phép.
     */
    private static String sanitizeRelativeDir(String targetRelativeDir) {
        String normalized = targetRelativeDir == null ? DEFAULT_UPLOAD_DIR : targetRelativeDir.trim();
        if (normalized.isEmpty()) {
            normalized = DEFAULT_UPLOAD_DIR;
        }
        normalized = normalized.replace('\\', '/');
        while (normalized.contains("..")) {
            normalized = normalized.replace("..", "");
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        normalized = normalized.replace('/', File.separatorChar);
        if (normalized.isEmpty()) {
            normalized = DEFAULT_UPLOAD_DIR;
        }
        return normalized;
    }
}

