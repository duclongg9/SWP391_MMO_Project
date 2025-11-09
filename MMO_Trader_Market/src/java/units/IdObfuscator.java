package units;

import java.util.Locale;

/**
mã hoá ID
 */
public final class IdObfuscator {

    private static final int SHIFT_BITS = 17;
    private static final long LOWER_MASK = (1L << SHIFT_BITS) - 1;
    private static final long SECRET_MASK = 0x5A6C3E9BD4A7L;

    private IdObfuscator() {
        // Utility class
    }

    /**
     Ép id (int dương) sang long.

Dịch trái SHIFT_BITS=17 bit (tức nhân 2^17).

XOR với SECRET_MASK (một hằng bí mật trong code).

Chuyển kết quả sang base36 (0-9 + a-z) và viết hoa → ra TOKEN.
     */
    public static String encode(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Identifier must be a positive integer");
        }
        long obfuscated = (((long) id) << SHIFT_BITS) ^ SECRET_MASK;
        return Long.toUnsignedString(obfuscated, 36).toUpperCase(Locale.ROOT);
    }

    /**
     Chuẩn hoá chuỗi, parse base36 về long.

XOR ngược với SECRET_MASK.

Kiểm tra phần LOWER_MASK (17 bit thấp) phải bằng 0 (đảm bảo đúng định dạng).

Dịch phải 17 bit để lấy lại id.

Kiểm tra id dương và trong khoảng int.
     */
    public static int decode(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Identifier token must not be null");
        }
        String normalized = token.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Identifier token must not be empty");
        }
        long obfuscated;
        try {
            obfuscated = Long.parseUnsignedLong(normalized.toLowerCase(Locale.ROOT), 36);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Identifier token is invalid", ex);
        }
        long combined = obfuscated ^ SECRET_MASK;
        if ((combined & LOWER_MASK) != 0) {
            throw new IllegalArgumentException("Identifier token is invalid");
        }
        long id = combined >>> SHIFT_BITS;
        if (id <= 0 || id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Identifier token is invalid");
        }
        return (int) id;
    }
}
