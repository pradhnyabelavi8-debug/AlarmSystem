
# ⏰ Java Alarm Setting System

A fully featured, console-based Alarm Setting System built in pure Java (no external libraries).

---

## Project Structure

```
AlarmSystem/
├── src/
│   └── alarm/
│       ├── Alarm.java               # Domain model
│       ├── AlarmManager.java        # CRUD + trigger logic
│       ├── NotificationService.java # Notification delivery
│       ├── AlarmScheduler.java      # Background polling thread
│       ├── AlarmCLI.java            # Interactive CLI menu
│       ├── Main.java                # Entry point (CLI + demo)
│       └── AlarmManagerTest.java    # Unit tests (no framework needed)
├── build.sh                         # Compile helper
└── README.md
```

---

## Classes

| Class | Responsibility |
|---|---|
| `Alarm` | Data model — id, label, time, repeat mode, state |
| `AlarmManager` | Add / update / delete alarms; fire-check logic |
| `NotificationService` | Print notifications and snooze messages |
| `AlarmScheduler` | ScheduledExecutorService polling every second |
| `AlarmCLI` | Scanner-driven interactive menu |
| `Main` | Wires everything together; demo mode |
| `AlarmManagerTest` | Zero-dependency unit tests |

---

## Requirements

* Java 17 or later (uses sealed-class `switch` expressions)

---

## Build & Run

### 1. Compile

```bash
chmod +x build.sh
./build.sh
```

Or manually:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

### 2. Interactive CLI

```bash
java -cp out alarm.Main
```

Menu options:
1. Add Alarm
2. Update Alarm
3. Delete Alarm
4. Enable / Disable Alarm
5. Snooze Alarm
6. View All Alarms
7. View Active Alarms
8. Statistics
0. Exit

### 3. Automated Demo (no keyboard input)

```bash
java -cp out alarm.Main demo
```

Adds 5 alarms that fire within seconds, demonstrating notifications, snooze, update, and delete.

### 4. Unit Tests

```bash
java -cp out alarm.AlarmManagerTest
```

---

## Features

- **Set alarms** with a label, date/time, and optional repeat mode
- **Repeat modes**: NONE, DAILY, WEEKLY, WEEKDAYS, WEEKENDS
- **Update** label, time, and repeat mode independently
- **Delete** or **enable/disable** any alarm
- **Snooze** — pushes the alarm forward by N minutes
- **Live scheduler** — daemon thread polls every second; fires and auto-advances repeating alarms
- **Notifications** — formatted banner printed to console when an alarm fires
- **Statistics** — total/active count and notification count
- **Input validation** — blank ID/label and null time throw `IllegalArgumentException`

---

## Sample Output

```
╔══════════════════════════════════════════════╗
║           🔔  ALARM TRIGGERED!  🔔            ║
╠══════════════════════════════════════════════╣
║  ID      : ALM-001                           ║
║  Label   : Morning Coffee ☕                  ║
║  Fired   : 2025-05-07 08:00:03               ║
║  Repeat  : DAILY                             ║
╚══════════════════════════════════════════════╝
```

# AlarmSystem
Develop a Java-based Alarm Setting System that allows users to set, update, and delete alarms, triggers notifications at the specified time, and displays the list of active alarms using appropriate classes, constructors, and methods.
>>>>>>> 422e651189c2ca9a267c990cc9a4f3d45230d0d3
