package com.carTracker.carTracker.util;

import com.carTracker.carTracker.reminder.ReminderStatus;
import com.carTracker.carTracker.reminder.ServiceReminder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Formats {

    private static final Locale PL = Locale.of("pl", "PL");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String NONE = "–";

    private Formats() {
    }

    public static String km(Integer km) {
        return km == null ? NONE : String.format(PL, "%,d km", km);
    }

    public static String kmDifference(int km) {
        return String.format(PL, "%+,d km", km);
    }

    public static String date(LocalDate date) {
        return date == null ? NONE : DATE.format(date);
    }

    public static String average(Double kmPerDay) {
        return kmPerDay == null ? NONE : String.format(PL, "%.1f km/dzień (≈ %,d km/miesiąc)", kmPerDay, Math.round(kmPerDay * 30));
    }

    public static String days(Long days) {
        if (days == null) {
            return NONE;
        }
        if (days < 0) {
            return -days + " dni temu";
        }
        if (days == 0) {
            return "dziś";
        }
        if (days == 1) {
            return "jutro";
        }
        return "za " + days + " dni";
    }

    public static String interval(ServiceReminder reminder) {
        List<String> parts = new ArrayList<>();
        if (reminder.getIntervalKm() != null) {
            parts.add(km(reminder.getIntervalKm()));
        }
        if (reminder.getIntervalMonths() != null) {
            parts.add(reminder.getIntervalMonths() + " mies.");
        }
        return parts.isEmpty() ? NONE : "co " + String.join(" / ", parts);
    }

    public static String lastDone(ServiceReminder reminder) {
        return date(reminder.getLastDoneDate()) + ", " + km(reminder.getLastDoneMileage());
    }

    public static String due(ReminderStatus status) {
        StringBuilder text = new StringBuilder();
        if (status.status() == ReminderStatus.Status.OVERDUE) {
            text.append("po terminie");
            if (status.kmLeft() != null && status.kmLeft() <= 0) {
                text.append(" (").append(km(-status.kmLeft())).append(" ponad)");
            } else if (status.daysLeft() != null) {
                text.append(" (").append(days(status.daysLeft())).append(")");
            }
            return text.toString();
        }
        text.append(days(status.daysLeft()));
        if (status.kmLeft() != null) {
            text.append(" (zostało ").append(km(status.kmLeft())).append(")");
        }
        return text.toString();
    }
}
