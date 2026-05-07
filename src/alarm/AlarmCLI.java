package alarm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive command-line interface for the Alarm Setting System.
 */
public class AlarmCLI {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AlarmManager        manager;
    private final NotificationService notificationService;
    private final AlarmScheduler      scheduler;
    private final Scanner             scanner = new Scanner(System.in);

    public AlarmCLI(AlarmManager manager,
                    NotificationService notificationService,
                    AlarmScheduler scheduler) {
        this.manager             = manager;
        this.notificationService = notificationService;
        this.scheduler           = scheduler;
    }

    public void run() {
        scheduler.start();
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Enter choice: ").trim();
            switch (choice) {
                case "1"  -> addAlarm();
                case "2"  -> updateAlarm();
                case "3"  -> deleteAlarm();
                case "4"  -> toggleAlarm();
                case "5"  -> snoozeAlarm();
                case "6"  -> manager.displayAllAlarms();
                case "7"  -> manager.displayActiveAlarms();
                case "8"  -> printStats();
                case "0"  -> running = false;
                default   -> System.out.println("⚠️   Invalid option. Please try again.");
            }
        }

        scheduler.stop();
        System.out.println("\n👋  Goodbye! Alarm System shut down.");
    }

    // ── Menu actions ───────────────────────────────────────────────────────────

    private void addAlarm() {
        System.out.println("\n── Add New Alarm ──");
        String label = readLine("Label: ");
        LocalDateTime time = readDateTime("Date & Time (yyyy-MM-dd HH:mm): ");
        if (time == null) return;
        Alarm.RepeatMode repeat = readRepeatMode();
        manager.addAlarm(label, time, repeat);
    }

    private void updateAlarm() {
        System.out.println("\n── Update Alarm ──");
        String id = readLine("Alarm ID to update: ").toUpperCase();
        Optional<Alarm> opt = manager.getAlarm(id);
        if (opt.isEmpty()) { System.out.println("❌  Alarm not found."); return; }

        Alarm alarm = opt.get();
        System.out.println("Current details:\n" + alarm);

        String newLabel = readLine("New label (blank = keep '" + alarm.getLabel() + "'): ");
        LocalDateTime newTime = null;
        String timeStr = readLine("New time yyyy-MM-dd HH:mm (blank = keep): ");
        if (!timeStr.isBlank()) {
            try { newTime = LocalDateTime.parse(timeStr, FMT); }
            catch (DateTimeParseException e) { System.out.println("⚠️   Invalid date — keeping original."); }
        }
        System.out.println("New repeat (blank = keep " + alarm.getRepeatMode() + "):");
        String repeatStr = readLine("  Repeat options — NONE/DAILY/WEEKLY/WEEKDAYS/WEEKENDS: ").toUpperCase();
        Alarm.RepeatMode newRepeat = null;
        if (!repeatStr.isBlank()) {
            try { newRepeat = Alarm.RepeatMode.valueOf(repeatStr); }
            catch (IllegalArgumentException e) { System.out.println("⚠️   Invalid repeat mode — keeping original."); }
        }

        manager.updateAlarm(id,
                newLabel.isBlank() ? null : newLabel,
                newTime,
                newRepeat);
    }

    private void deleteAlarm() {
        System.out.println("\n── Delete Alarm ──");
        String id = readLine("Alarm ID to delete: ").toUpperCase();
        manager.deleteAlarm(id);
    }

    private void toggleAlarm() {
        System.out.println("\n── Enable / Disable Alarm ──");
        String id = readLine("Alarm ID: ").toUpperCase();
        String state = readLine("Enable or disable? (e/d): ").toLowerCase();
        if (state.equals("e") || state.equals("enable")) {
            manager.toggleAlarm(id, true);
        } else if (state.equals("d") || state.equals("disable")) {
            manager.toggleAlarm(id, false);
        } else {
            System.out.println("⚠️   Invalid input. Use 'e' or 'd'.");
        }
    }

    private void snoozeAlarm() {
        System.out.println("\n── Snooze Alarm ──");
        String id = readLine("Alarm ID to snooze: ").toUpperCase();
        Optional<Alarm> opt = manager.getAlarm(id);
        if (opt.isEmpty()) { System.out.println("❌  Alarm not found."); return; }
        String minsStr = readLine("Snooze for how many minutes? (default 5): ").trim();
        int minutes = 5;
        try { if (!minsStr.isBlank()) minutes = Integer.parseInt(minsStr); }
        catch (NumberFormatException e) { System.out.println("⚠️   Invalid number — using 5 minutes."); }

        Alarm alarm = opt.get();
        alarm.setAlarmTime(alarm.getAlarmTime().plusMinutes(minutes));
        alarm.setTriggered(false);
        alarm.setActive(true);
        notificationService.notifySnooze(alarm, minutes);
    }

    private void printStats() {
        System.out.println("\n── Statistics ──");
        System.out.printf("  Total alarms    : %d%n", manager.getTotalCount());
        System.out.printf("  Active alarms   : %d%n", manager.getActiveCount());
        System.out.printf("  Notifications   : %d%n", notificationService.getNotificationCount());
        System.out.printf("  Scheduler       : %s%n", scheduler.isRunning() ? "Running" : "Stopped");
        System.out.println();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : "";
    }

    private LocalDateTime readDateTime(String prompt) {
        String input = readLine(prompt).trim();
        try {
            return LocalDateTime.parse(input, FMT);
        } catch (DateTimeParseException e) {
            System.out.println("❌  Invalid date/time format. Use: yyyy-MM-dd HH:mm");
            return null;
        }
    }

    private Alarm.RepeatMode readRepeatMode() {
        System.out.println("  Repeat options: NONE | DAILY | WEEKLY | WEEKDAYS | WEEKENDS");
        String input = readLine("  Repeat (default NONE): ").toUpperCase().trim();
        try {
            return input.isBlank() ? Alarm.RepeatMode.NONE : Alarm.RepeatMode.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️   Invalid repeat mode — defaulting to NONE.");
            return Alarm.RepeatMode.NONE;
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        ⏰  JAVA ALARM SETTING SYSTEM      ║");
        System.out.println("║          Set. Update. Delete. Ring.       ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("┌─────────────── MENU ───────────────┐");
        System.out.println("│  1. Add Alarm                       │");
        System.out.println("│  2. Update Alarm                    │");
        System.out.println("│  3. Delete Alarm                    │");
        System.out.println("│  4. Enable / Disable Alarm          │");
        System.out.println("│  5. Snooze Alarm                    │");
        System.out.println("│  6. View All Alarms                 │");
        System.out.println("│  7. View Active Alarms              │");
        System.out.println("│  8. Statistics                      │");
        System.out.println("│  0. Exit                            │");
        System.out.println("└─────────────────────────────────────┘");
    }
}
