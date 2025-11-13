package listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import service.OrderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lập lịch kiểm tra định kỳ để giải ngân escrow cho các đơn hàng đã hết hạn.
 */
@WebListener
public class EscrowAutoReleaseScheduler implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(EscrowAutoReleaseScheduler.class.getName());
    private static final long INITIAL_DELAY_SECONDS = 60L;
    private static final long INTERVAL_SECONDS = 300L;

    private ScheduledExecutorService executorService;
    private OrderService orderService;

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        orderService = new OrderService();
        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "escrow-auto-release");
                thread.setDaemon(true);
                return thread;
            }
        });
        executorService.scheduleWithFixedDelay(this::runAutoRelease,
                INITIAL_DELAY_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    /**
     * Tác vụ nền thực hiện auto-release escrow và ghi log khi có thay đổi.
     */
    private void runAutoRelease() {
        try {
            int released = orderService.releaseExpiredEscrows();
            if (released > 0) {
                LOGGER.log(Level.INFO, "Đã giải ngân escrow tự động cho {0} đơn hàng", released);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Không thể chạy tác vụ giải ngân escrow tự động", ex);
        }
    }
}
