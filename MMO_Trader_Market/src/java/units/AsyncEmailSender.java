package units;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dispatches transactional emails in the background so HTTP requests can return immediately.
 */
public final class AsyncEmailSender {

    private static final Logger LOGGER = Logger.getLogger(AsyncEmailSender.class.getName());
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newFixedThreadPool(2, new DaemonThreadFactory());

    private AsyncEmailSender() {
        // Utility class
    }

    /**
     * Submit an email-sending task to the background executor.
     *
     * @param toEmail recipient email address
     * @param subject email subject line
     * @param body    email body content
     */
    public static void send(String toEmail, String subject, String body) {
        String normalizedRecipient = Objects.requireNonNull(toEmail, "Recipient email is required").trim();
        EMAIL_EXECUTOR.submit(() -> {
            try {
                SendMail.sendMail(normalizedRecipient, subject, body);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to send email asynchronously to " + normalizedRecipient, e);
            }
        });
    }

    /**
     * Custom thread factory to ensure background threads do not block application shutdown.
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
