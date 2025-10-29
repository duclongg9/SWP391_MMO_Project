/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author D E L L
 */
public class ImageUtils {

    private ImageUtils() {
    }

    public static boolean isAllowedImage(String mime) {
        return mime != null && (mime.equalsIgnoreCase("image/jpeg")
                || mime.equalsIgnoreCase("image/png")
                || mime.equalsIgnoreCase("image/webp"));
    }

    public static String extFromMime(String mime) {
        if ("image/png".equalsIgnoreCase(mime)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(mime)) {
            return ".webp";
        }
        return ".jpg";
    }
}
