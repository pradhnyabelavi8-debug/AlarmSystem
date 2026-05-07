package alarm;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight unit-test runner (no external frameworks required).
 * Run:  java alarm.AlarmManagerTest
 */
public class AlarmManagerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("══════════  AlarmManager Unit Tests  ══════════\n");

        testAddAlarm();
        testGetAlarm();
        testUpdateAlarm();
        testToggleAlarm();
        testDeleteAlarm();
        testCheckAndTrigger();
        testRepeatAdvance();
        testSnoozeViaSetTime();
        testInvalidAlarm();

        System.out.printf("%n════════════════════════════════════════════════%n");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("════════════════════════════════════════════════");
        if (failed > 0) System.exit(1);
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    static void testAddAlarm() {
        AlarmManager m = new AlarmManager();
        Alarm a = m.addAlarm("Wake up", LocalDateTime.now().plusHours(1));
        assertEqual("add: total count", 1, m.getTotalCount());
        assertNotNull("add: alarm not null", a);
        assertCondition("add: alarm active by default", a.isActive());
        assertCondition("add: alarm ID starts with ALM", a.getId().startsWith("ALM"));
    }

    static void testGetAlarm() {
        AlarmManager m = new AlarmManager();
        Alarm a = m.addAlarm("Lunch", LocalDateTime.now().plusHours(2));
        assertCondition("get: present", m.getAlarm(a.getId()).isPresent());
        assertCondition("get: absent",  m.getAlarm("FAKE-000").isEmpty());
    }

    static void testUpdateAlarm() {
        AlarmManager m = new AlarmManager();
        Alarm a = m.addAlarm("Old label", LocalDateTime.now().plusHours(1));
        LocalDateTime newTime = LocalDateTime.now().plusHours(3);
        boolean ok = m.updateAlarm(a.getId(), "New label", newTime, Alarm.RepeatMode.DAILY);
        assertCondition("update: returns true", ok);
        assertEqual("update: label changed", "New label", a.getLabel());
        assertEqual("update: time changed", newTime, a.getAlarmTime());
        assertEqual("update: repeat changed", Alarm.RepeatMode.DAILY, a.getRepeatMode());
        assertCondition("update: non-existent id returns false",
                !m.updateAlarm("FAKE", null, null, null));
    }

    static void testToggleAlarm() {
        AlarmManager m = new AlarmManager();
        Alarm a = m.addAlarm("Toggle me", LocalDateTime.now().plusHours(1));
        m.toggleAlarm(a.getId(), false);
        assertCondition("toggle: disabled", !a.isActive());
        m.toggleAlarm(a.getId(), true);
        assertCondition("toggle: re-enabled", a.isActive());
    }

    static void testDeleteAlarm() {
        AlarmManager m = new AlarmManager();
        Alarm a = m.addAlarm("Delete me", LocalDateTime.now().plusHours(1));
        boolean deleted = m.deleteAlarm(a.getId());
        assertCondition("delete: returns true", deleted);
        assertEqual("delete: total 0", 0, m.getTotalCount());
        assertCondition("delete: fake id returns false", !m.deleteAlarm("FAKE"));
    }

    static void testCheckAndTrigger() {
        AlarmManager m = new AlarmManager();
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        Alarm a = m.addAlarm("Past alarm", past);
        List<Alarm> fired = m.checkAndTrigger(LocalDateTime.now());
        assertEqual("trigger: 1 alarm fired", 1, fired.size());
        assertCondition("trigger: alarm marked triggered", a.isTriggered());
        // Second check — should NOT re-fire
        List<Alarm> fired2 = m.checkAndTrigger(LocalDateTime.now());
        assertEqual("trigger: no double-fire", 0, fired2.size());
    }

    static void testRepeatAdvance() {
        AlarmManager m = new AlarmManager();
        LocalDateTime t = LocalDateTime.now().minusMinutes(1);
        Alarm a = m.addAlarm("Daily", t, Alarm.RepeatMode.DAILY);
        List<Alarm> fired = m.checkAndTrigger(LocalDateTime.now());
        m.advanceRepeating(fired);
        assertCondition("repeat: time advanced", a.getAlarmTime().isAfter(LocalDateTime.now()));
        assertCondition("repeat: still active",  a.isActive());
        assertCondition("repeat: not triggered", !a.isTriggered());
    }

    static void testSnoozeViaSetTime() {
        AlarmManager m = new AlarmManager();
        Alarm a = m.addAlarm("Snooze me", LocalDateTime.now().minusMinutes(1));
        m.checkAndTrigger(LocalDateTime.now());
        int snoozeMinutes = 10;
        a.setAlarmTime(LocalDateTime.now().plusMinutes(snoozeMinutes));
        a.setTriggered(false);
        assertCondition("snooze: time in future", a.getAlarmTime().isAfter(LocalDateTime.now()));
        assertCondition("snooze: not triggered",  !a.isTriggered());
    }

    static void testInvalidAlarm() {
        boolean caught = false;
        try { new Alarm("", "label", LocalDateTime.now()); }
        catch (IllegalArgumentException e) { caught = true; }
        assertCondition("validation: blank ID throws", caught);

        caught = false;
        try { new Alarm("A1", "", LocalDateTime.now()); }
        catch (IllegalArgumentException e) { caught = true; }
        assertCondition("validation: blank label throws", caught);

        caught = false;
        try { new Alarm("A1", "label", null); }
        catch (IllegalArgumentException e) { caught = true; }
        assertCondition("validation: null time throws", caught);
    }

    // ── Assertion helpers ──────────────────────────────────────────────────────

    static void assertCondition(String name, boolean condition) {
        if (condition) { pass(name); } else { fail(name, "expected true but was false"); }
    }

    static void assertNotNull(String name, Object obj) {
        if (obj != null) { pass(name); } else { fail(name, "expected non-null"); }
    }

    static void assertEqual(String name, Object expected, Object actual) {
        if (expected.equals(actual)) { pass(name); }
        else { fail(name, "expected [" + expected + "] but got [" + actual + "]"); }
    }

    static void pass(String name) {
        System.out.printf("  ✅  PASS  %s%n", name);
        passed++;
    }

    static void fail(String name, String reason) {
        System.out.printf("  ❌  FAIL  %s — %s%n", name, reason);
        failed++;
    }
}
