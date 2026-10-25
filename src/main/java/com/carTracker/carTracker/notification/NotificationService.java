package com.carTracker.carTracker.notification;

import com.carTracker.carTracker.car.Car;
import com.carTracker.carTracker.reminder.ReminderService;
import com.carTracker.carTracker.reminder.ReminderStatus;
import com.carTracker.carTracker.ui.AppIcon;
import com.carTracker.carTracker.util.Formats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final long FIRST_CHECK_DELAY_SECONDS = 5;
    private static final long CHECK_INTERVAL_HOURS = 4;
    private static final int MAX_LINES = 2;

    private final ReminderService reminderService;
    private final Set<String> alreadyNotified = ConcurrentHashMap.newKeySet();
    private TrayIcon trayIcon;
    private ScheduledExecutorService scheduler;
    private boolean hiddenHintShown;

    public NotificationService(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    public boolean install(Runnable onOpen, Runnable onExit) {
        if (!SystemTray.isSupported()) {
            return false;
        }
        PopupMenu menu = new PopupMenu();
        MenuItem open = new MenuItem("Otwórz CarTracker");
        open.addActionListener(e -> onOpen.run());
        MenuItem check = new MenuItem("Sprawdź przypomnienia");
        check.addActionListener(e -> checkReminders(true));
        MenuItem exit = new MenuItem("Zamknij");
        exit.addActionListener(e -> onExit.run());
        menu.add(open);
        menu.add(check);
        menu.addSeparator();
        menu.add(exit);

        SystemTray tray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(AppIcon.awtImage(tray.getTrayIconSize().width), "CarTracker", menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> onOpen.run());
        try {
            tray.add(trayIcon);
            return true;
        } catch (AWTException e) {
            log.warn("Nie udało się dodać ikony do zasobnika", e);
            trayIcon = null;
            return false;
        }
    }

    public void startScheduledChecks() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "reminder-check");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> checkReminders(false),
                FIRST_CHECK_DELAY_SECONDS, TimeUnit.HOURS.toSeconds(CHECK_INTERVAL_HOURS), TimeUnit.SECONDS);
    }

    public void checkReminders(boolean manual) {
        try {
            List<ReminderStatus> due = reminderService.getStatusesRequiringAttention();
            String today = LocalDate.now().toString();
            List<ReminderStatus> toShow = manual
                    ? due
                    : due.stream().filter(s -> alreadyNotified.add(s.reminder().getId() + "@" + today)).toList();
            if (toShow.isEmpty()) {
                if (manual) {
                    show("CarTracker", "Brak zbliżających się terminów.", TrayIcon.MessageType.INFO);
                }
                return;
            }
            String text = toShow.stream().limit(MAX_LINES).map(this::describe).collect(Collectors.joining("\n"));
            if (toShow.size() > MAX_LINES) {
                text += "\n…i " + (toShow.size() - MAX_LINES) + " więcej – kliknij, aby otworzyć";
            }
            show("CarTracker – " + countText(toShow.size()), text, TrayIcon.MessageType.WARNING);
        } catch (RuntimeException e) {
            log.error("Błąd podczas sprawdzania przypomnień", e);
        }
    }

    public void showHiddenHintOnce() {
        if (!hiddenHintShown) {
            hiddenHintShown = true;
            show("CarTracker działa w tle",
                    "Przypomnienia będą sprawdzane dalej. Zamknij aplikację z menu ikony w zasobniku.",
                    TrayIcon.MessageType.INFO);
        }
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    private void show(String title, String text, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, text, type);
        }
    }

    private static String countText(int count) {
        if (count == 1) {
            return "1 przypomnienie";
        }
        int lastDigit = count % 10;
        int lastTwoDigits = count % 100;
        boolean few = lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14);
        return count + (few ? " przypomnienia" : " przypomnień");
    }

    private String describe(ReminderStatus status) {
        Car car = status.reminder().getCar();
        return car.getBrand() + " " + car.getModel() + ": " + status.reminder().getName() + " – " + Formats.due(status);
    }
}
