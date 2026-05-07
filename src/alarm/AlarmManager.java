package alarm;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a collection of {@link Alarm} objects.
 * Provides CRUD operations and a live-check method used by the scheduler.
 */
public class AlarmManager {

    private final Map<String, Alarm> alarms = new LinkedHashMap<>();
    private int idCounter = 1;

    // ── ID generation ──────────────────────────────────────────────────────────

    public String generateId() {
        return String.format("ALM-%03d", idCounter++);
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    /**
     * Adds a new alarm.
     *
     * @return the newly created {@link Alarm}
     */
    public Alarm addAlarm(String label, LocalDateTime alarmTime, Alarm.RepeatMode repeatMode) {
        String id = generateId();
        Alarm alarm = new Alarm(id, label, alarmTime, repeatMode);
        alarms.put(id, alarm);
        System.out.printf("✅  Alarm '%s' (%s) added successfully.%n", label, id);
        return alarm;
    }

    /** Convenience overload — no repeat. */
    public Alarm addAlarm(String label, LocalDateTime alarmTime) {
        return addAlarm(label, alarmTime, Alarm.RepeatMode.NONE);
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    public Optional<Alarm> getAlarm(String id) {
        return Optional.ofNullable(alarms.get(id));
    }

    /** Returns an unmodifiable snapshot of all alarms. */
    public List<Alarm> getAllAlarms() {
        return Collections.unmodifiableList(new ArrayList<>(alarms.values()));
    }

    /** Returns only alarms that are currently active. */
    public List<Alarm> getActiveAlarms() {
        return alarms.values().stream()
                .filter(Alarm::isActive)
                .collect(Collectors.toList());
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    /**
     * Updates an existing alarm's label, time, and/or repeat mode.
     * Pass {@code null} to leave a field unchanged.
     */
    public boolean updateAlarm(String id,
                               String newLabel,
                               LocalDateTime newTime,
                               Alarm.RepeatMode newRepeat) {
        Alarm alarm = alarms.get(id);
        if (alarm == null) {
            System.out.printf("❌  Alarm '%s' not found.%n", id);
            return false;
        }
        if (newLabel  != null) alarm.setLabel(newLabel);
        if (newTime   != null) alarm.setAlarmTime(newTime);
        if (newRepeat != null) alarm.setRepeatMode(newRepeat);
        System.out.printf("✏️   Alarm '%s' updated.%n", id);
        return true;
    }

    /** Enable or disable an alarm without deleting it. */
    public boolean toggleAlarm(String id, boolean active) {
        Alarm alarm = alarms.get(id);
        if (alarm == null) {
            System.out.printf("❌  Alarm '%s' not found.%n", id);
            return false;
        }
        alarm.setActive(active);
        System.out.printf("%s  Alarm '%s' %s.%n",
                active ? "🔔" : "🔕", id, active ? "enabled" : "disabled");
        return true;
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    public boolean deleteAlarm(String id) {
        Alarm removed = alarms.remove(id);
        if (removed == null) {
            System.out.printf("❌  Alarm '%s' not found.%n", id);
            return false;
        }
        System.out.printf("🗑️   Alarm '%s' (%s) deleted.%n", removed.getLabel(), id);
        return true;
    }

    /** Remove all alarms. */
    public void clearAll() {
        alarms.clear();
        System.out.println("🗑️   All alarms cleared.");
    }

    // ── Trigger check ──────────────────────────────────────────────────────────

    /**
     * Called periodically by the scheduler.
     * Returns all alarms whose time has arrived and have not yet been triggered.
     */
    public List<Alarm> checkAndTrigger(LocalDateTime now) {
        List<Alarm> fired = new ArrayList<>();
        for (Alarm alarm : alarms.values()) {
            if (alarm.isActive()
                    && !alarm.isTriggered()
                    && !now.isBefore(alarm.getAlarmTime())) {
                alarm.setTriggered(true);
                fired.add(alarm);
            }
        }
        return fired;
    }

    /**
     * After a notification is delivered, advance repeating alarms to their
     * next scheduled time.
     */
    public void advanceRepeating(List<Alarm> fired) {
        for (Alarm alarm : fired) {
            alarm.scheduleNext();
        }
    }

    // ── Display ────────────────────────────────────────────────────────────────

    public void displayAllAlarms() {
        if (alarms.isEmpty()) {
            System.out.println("📭  No alarms set.");
            return;
        }
        System.out.println("\n══════════════  ALL ALARMS  ══════════════");
        alarms.values().forEach(System.out::println);
        System.out.println("══════════════════════════════════════════\n");
    }

    public void displayActiveAlarms() {
        List<Alarm> active = getActiveAlarms();
        if (active.isEmpty()) {
            System.out.println("📭  No active alarms.");
            return;
        }
        System.out.println("\n══════════════  ACTIVE ALARMS  ══════════════");
        active.forEach(System.out::println);
        System.out.printf("Total active: %d%n", active.size());
        System.out.println("═════════════════════════════════════════════\n");
    }

    public int getTotalCount()  { return alarms.size(); }
    public int getActiveCount() { return (int) alarms.values().stream().filter(Alarm::isActive).count(); }
}
