package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servlet để serve uploaded files (ảnh KYC, etc.)
 */
@WebServlet(name = "FileServlet", urlPatterns = {"/uploads/*"})
public class FileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = "uploads";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Remove leading slash
        String filePath = pathInfo.substring(1);
        
        // Security check - prevent directory traversal
        if (filePath.contains("..") || filePath.contains("//") || filePath.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid file path");
            return;
        }
        
        // Construct full file path
        Path fullPath = Paths.get(UPLOAD_DIR, filePath);
        File file = fullPath.toFile();
        
        // Check if file exists and is a file (not directory)
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }
        
        // Security check - ensure file is within upload directory
        try {
            Path canonicalUploadDir = Paths.get(UPLOAD_DIR).toRealPath();
            Path canonicalFilePath = file.toPath().toRealPath();
            
            if (!canonicalFilePath.startsWith(canonicalUploadDir)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to verify file path");
            return;
        }
        
        // Determine content type
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = getContentTypeFromExtension(filePath);
        }
        
        // Set response headers
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());
        
        // Add cache headers for images
        if (contentType != null && contentType.startsWith("image/")) {
            response.setHeader("Cache-Control", "public, max-age=3600"); // Cache for 1 hour
            response.setDateHeader("Expires", System.currentTimeMillis() + 3600000L);
        }
        
        // Stream file content
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            
            os.flush();
        } catch (IOException e) {
            // Client probably closed connection
            // Log error but don't throw exception
            System.err.println("Error serving file: " + e.getMessage());
        }
    }
    
    /**
     * Get content type based on file extension
     */
    private String getContentTypeFromExtension(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            case ".pdf":
                return "application/pdf";
            case ".txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
}
