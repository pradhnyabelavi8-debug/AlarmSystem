package alarm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Runs a background daemon thread that polls the {@link AlarmManager}
 * every second and fires notifications via {@link NotificationService}.
 */
public class AlarmScheduler {

    private final AlarmManager        manager;
    private final NotificationService notificationService;
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "AlarmScheduler");
                t.setDaemon(true);
                return t;
            });

    private ScheduledFuture<?> scheduledTask;
    private boolean running = false;

    public AlarmScheduler(AlarmManager manager, NotificationService notificationService) {
        this.manager             = manager;
        this.notificationService = notificationService;
    }

    /** Start polling every {@code intervalSeconds} seconds. */
    public void start(long intervalSeconds) {
        if (running) {
            System.out.println("⚠️   Scheduler is already running.");
            return;
        }
        scheduledTask = executor.scheduleAtFixedRate(this::tick, 0, intervalSeconds, TimeUnit.SECONDS);
        running = true;
        System.out.printf("⏱️   Alarm scheduler started (polling every %ds).%n", intervalSeconds);
    }

    /** Start with a default 1-second polling interval. */
    public void start() { start(1); }

    /** Stop polling. */
    public void stop() {
        if (!running) return;
        if (scheduledTask != null) scheduledTask.cancel(false);
        executor.shutdown();
        running = false;
        System.out.println("⏹️   Alarm scheduler stopped.");
    }

    public boolean isRunning() { return running; }

    // ── Internal tick ──────────────────────────────────────────────────────────

    private void tick() {
        LocalDateTime now   = LocalDateTime.now();
        List<Alarm>   fired = manager.checkAndTrigger(now);
        if (!fired.isEmpty()) {
            notificationService.notify(fired);
            manager.advanceRepeating(fired);
        }
    }
}
