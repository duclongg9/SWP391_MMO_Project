package units;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gửi email giao dịch ở chế độ nền để các request HTTP có thể trả về ngay.
 */
public final class AsyncEmailSender {

    private static final Logger LOGGER = Logger.getLogger(AsyncEmailSender.class.getName());
    private static final ExecutorService EMAIL_EXECUTOR =
            Executors.newFixedThreadPool(2, new DaemonThreadFactory());

    private AsyncEmailSender() {
        // Lớp tiện ích (utility) — không cho tạo instance
    }

    /**
     * Đưa tác vụ gửi email vào thread pool nền.
     *
     * @param toEmail địa chỉ email người nhận
     * @param subject tiêu đề email
     * @param body    nội dung email
     */
    public static void send(String toEmail, String subject, String body) {
        String normalizedRecipient = Objects.requireNonNull(toEmail, "Cần truyền email người nhận").trim();
        EMAIL_EXECUTOR.submit(() -> {
            try {
                SendMail.sendMail(normalizedRecipient, subject, body);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Gửi email bất đồng bộ tới " + normalizedRecipient + " thất bại", e);
            }
        });
    }

    /**
     * ThreadFactory tuỳ biến để các luồng nền không chặn việc tắt ứng dụng.
     */
    private static final class DaemonThreadFactory implements ThreadFactory {

        private int threadIndex = 0;

        @Override
        public synchronized Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "async-email-sender-" + (++threadIndex));
            thread.setDaemon(true);
            return thread;
        }
    }
}
