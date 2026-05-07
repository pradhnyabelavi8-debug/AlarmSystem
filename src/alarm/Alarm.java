package alarm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single Alarm with a unique ID, label, scheduled time,
 * repeat setting, and active/triggered state.
 */
public class Alarm {

    public enum RepeatMode {
        NONE, DAILY, WEEKLY, WEEKDAYS, WEEKENDS
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String id;
    private String label;
    private LocalDateTime alarmTime;
    private RepeatMode repeatMode;
    private boolean active;
    private boolean triggered;

    /**
     * Full constructor.
     */
    public Alarm(String id, String label, LocalDateTime alarmTime, RepeatMode repeatMode) {
        if (id == null || id.isBlank())   throw new IllegalArgumentException("ID cannot be blank.");
        if (label == null || label.isBlank()) throw new IllegalArgumentException("Label cannot be blank.");
        if (alarmTime == null)            throw new IllegalArgumentException("Alarm time cannot be null.");
        this.id         = id;
        this.label      = label;
        this.alarmTime  = alarmTime;
        this.repeatMode = (repeatMode != null) ? repeatMode : RepeatMode.NONE;
        this.active     = true;
        this.triggered  = false;
    }

    /** Convenience constructor with no-repeat and active by default. */
    public Alarm(String id, String label, LocalDateTime alarmTime) {
        this(id, label, alarmTime, RepeatMode.NONE);
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String        getId()         { return id; }
    public String        getLabel()      { return label; }
    public LocalDateTime getAlarmTime()  { return alarmTime; }
    public RepeatMode    getRepeatMode() { return repeatMode; }
    public boolean       isActive()      { return active; }
    public boolean       isTriggered()   { return triggered; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setLabel(String label) {
        if (label == null || label.isBlank()) throw new IllegalArgumentException("Label cannot be blank.");
        this.label = label;
    }

    public void setAlarmTime(LocalDateTime alarmTime) {
        if (alarmTime == null) throw new IllegalArgumentException("Alarm time cannot be null.");
        this.alarmTime  = alarmTime;
        this.triggered  = false;   // reset triggered state when time changes
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = (repeatMode != null) ? repeatMode : RepeatMode.NONE;
    }

    public void setActive(boolean active) { this.active = active; }
    public void setTriggered(boolean triggered) { this.triggered = triggered; }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Advance a repeating alarm to its next fire time after it triggers. */
    public void scheduleNext() {
        switch (repeatMode) {
            case DAILY    -> alarmTime = alarmTime.plusDays(1);
            case WEEKLY   -> alarmTime = alarmTime.plusWeeks(1);
            case WEEKDAYS -> {
                alarmTime = alarmTime.plusDays(1);
                while (isWeekend(alarmTime)) alarmTime = alarmTime.plusDays(1);
            }
            case WEEKENDS -> {
                alarmTime = alarmTime.plusDays(1);
                while (!isWeekend(alarmTime)) alarmTime = alarmTime.plusDays(1);
            }
            default -> active = false;   // NONE — deactivate after first trigger
        }
        triggered = false;
    }

    private boolean isWeekend(LocalDateTime dt) {
        return switch (dt.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return String.format(
            "┌─ [%s] %s%n" +
            "│  Time   : %s%n" +
            "│  Repeat : %-10s  Active: %s  Triggered: %s%n" +
            "└─────────────────────────────────────",
            id, label,
            alarmTime.format(FORMATTER),
            repeatMode, active, triggered
        );
    }
}
