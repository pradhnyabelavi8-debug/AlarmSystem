package alarm;


import java.time.LocalDateTime;

/**
 * Entry point for the Java Alarm Setting System.
 *
 * Run modes
 * ---------
 *  java alarm.Main          →  interactive CLI menu
 *  java alarm.Main demo     →  automated demo (no keyboard input needed)
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        AlarmManager        manager  = new AlarmManager();
        NotificationService notifSvc = new NotificationService();
        AlarmScheduler      sched    = new AlarmScheduler(manager, notifSvc);

        boolean demoMode = args.length > 0 && args[0].equalsIgnoreCase("demo");

        if (demoMode) {
            runDemo(manager, notifSvc, sched);
        } else {
            AlarmCLI cli = new AlarmCLI(manager, notifSvc, sched);
            cli.run();
        }
    }

    // ── Demo ───────────────────────────────────────────────────────────────────

    private static void runDemo(AlarmManager manager,
                                NotificationService notifSvc,
                                AlarmScheduler sched) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  ⏰  ALARM SYSTEM — AUTOMATED DEMO  ⏰    ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        // ── 1. Add alarms ──────────────────────────────────────────────────────
        System.out.println("═══ 1. ADDING ALARMS ═══");
        LocalDateTime now = LocalDateTime.now();

        Alarm a1 = manager.addAlarm("Morning Coffee",   now.plusSeconds(3),  Alarm.RepeatMode.DAILY);
        Alarm a2 = manager.addAlarm("Team Stand-up",    now.plusSeconds(6),  Alarm.RepeatMode.WEEKDAYS);
        Alarm a3 = manager.addAlarm("Weekend Jog",      now.plusSeconds(9),  Alarm.RepeatMode.WEEKENDS);
        Alarm a4 = manager.addAlarm("Take Medicine",    now.plusSeconds(12), Alarm.RepeatMode.NONE);
        Alarm a5 = manager.addAlarm("Disabled Example", now.plusSeconds(2),  Alarm.RepeatMode.NONE);

        // ── 2. Display all ─────────────────────────────────────────────────────
        System.out.println("\n═══ 2. ALL ALARMS (after adding) ═══");
        manager.displayAllAlarms();

        // ── 3. Update an alarm ────────────────────────────────────────────────
        System.out.println("═══ 3. UPDATING ALARM ═══");
        manager.updateAlarm(a1.getId(), "Morning Coffee ☕", now.plusSeconds(3), Alarm.RepeatMode.DAILY);

        // ── 4. Disable one alarm ───────────────────────────────────────────────
        System.out.println("\n═══ 4. DISABLING ALARM ═══");
        manager.toggleAlarm(a5.getId(), false);

        // ── 5. Display active only ─────────────────────────────────────────────
        System.out.println("\n═══ 5. ACTIVE ALARMS ═══");
        manager.displayActiveAlarms();

        // ── 6. Start scheduler and let alarms fire ────────────────────────────
        System.out.println("═══ 6. STARTING SCHEDULER — watch for notifications! ═══");
        sched.start(1);

        // Wait long enough for the first three alarms to fire
        Thread.sleep(14_000);

        // ── 7. Snooze an alarm ────────────────────────────────────────────────
        System.out.println("═══ 7. SNOOZING ALARM ═══");
        a2.setTriggered(false);
        a2.setActive(true);
        a2.setAlarmTime(now.plusSeconds(16));
        notifSvc.notifySnooze(a2, 1);   // 1-second snooze for demo brevity

        Thread.sleep(3_000);

        // ── 8. Delete an alarm ────────────────────────────────────────────────
        System.out.println("\n═══ 8. DELETING ALARM ═══");
        manager.deleteAlarm(a4.getId());

        // ── 9. Final state ────────────────────────────────────────────────────
        System.out.println("\n═══ 9. FINAL STATE ═══");
        manager.displayAllAlarms();

        // ── 10. Stats ─────────────────────────────────────────────────────────
        System.out.println("═══ 10. STATISTICS ═══");
        System.out.printf("  Total alarms    : %d%n", manager.getTotalCount());
        System.out.printf("  Active alarms   : %d%n", manager.getActiveCount());
        System.out.printf("  Notifications   : %d%n", notifSvc.getNotificationCount());

        sched.stop();
        System.out.println("\n✅  Demo complete.");
    }
}
