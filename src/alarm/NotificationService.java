package alarm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles notification delivery when alarms fire.
 * In a real application this could send OS notifications, emails, etc.
 * Here it prints clearly formatted console banners.
 */
public class NotificationService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int notificationCount = 0;

    /**
     * Deliver notifications for every alarm in the supplied list.
     */
    public void notify(List<Alarm> firedAlarms) {
        if (firedAlarms == null || firedAlarms.isEmpty()) return;
        for (Alarm alarm : firedAlarms) {
            deliver(alarm);
        }
    }

    /**
     * Deliver a notification for a single alarm.
     */
    public void deliver(Alarm alarm) {
        notificationCount++;
        String now = LocalDateTime.now().format(FMT);
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║           🔔  ALARM TRIGGERED!  🔔            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf( "║  ID      : %-33s║%n", alarm.getId());
        System.out.printf( "║  Label   : %-33s║%n", alarm.getLabel());
        System.out.printf( "║  Fired   : %-33s║%n", now);
        System.out.printf( "║  Repeat  : %-33s║%n", alarm.getRepeatMode());
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    /** Sends a snooze confirmation message. */
    public void notifySnooze(Alarm alarm, int minutes) {
        System.out.printf("💤  Alarm '%s' (%s) snoozed for %d minute(s). New time: %s%n",
                alarm.getLabel(),
                alarm.getId(),
                minutes,
                alarm.getAlarmTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public int getNotificationCount() { return notificationCount; }
}
