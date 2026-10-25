package com.carTracker.carTracker.reminder;

import com.carTracker.carTracker.mileage.MileageStats;
import com.carTracker.carTracker.reminder.ReminderStatus.Status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class ReminderCalculator {

    private ReminderCalculator() {
    }

    public static ReminderStatus calculate(ServiceReminder reminder, MileageStats stats, LocalDate today) {
        Integer dueMileage = reminder.getIntervalKm() != null && reminder.getLastDoneMileage() != null
                ? reminder.getLastDoneMileage() + reminder.getIntervalKm()
                : null;
        Integer currentMileage = stats.estimatedMileageAt(today);
        Integer kmLeft = dueMileage != null && currentMileage != null ? dueMileage - currentMileage : null;

        LocalDate dueByKm = null;
        if (dueMileage != null && stats.averageKmPerDay() != null && stats.averageKmPerDay() > 0) {
            long daysFromLastReading = (long) Math.ceil((dueMileage - stats.lastMileage()) / stats.averageKmPerDay());
            dueByKm = stats.lastReadingDate().plusDays(daysFromLastReading);
        }
        LocalDate dueByTime = reminder.getIntervalMonths() != null && reminder.getLastDoneDate() != null
                ? reminder.getLastDoneDate().plusMonths(reminder.getIntervalMonths())
                : null;

        LocalDate dueDate = earliest(dueByKm, dueByTime);
        Long daysLeft = dueDate != null ? ChronoUnit.DAYS.between(today, dueDate) : null;

        Status status;
        if (kmLeft == null && daysLeft == null) {
            status = Status.UNKNOWN;
        } else if ((kmLeft != null && kmLeft <= 0) || (daysLeft != null && daysLeft < 0)) {
            status = Status.OVERDUE;
        } else if ((daysLeft != null && daysLeft <= reminder.notifyDaysBeforeOrDefault())
                || (kmLeft != null && dueByKm == null && kmLeft <= reminder.getIntervalKm() / 10)) {
            status = Status.SOON;
        } else {
            status = Status.OK;
        }
        return new ReminderStatus(reminder, dueMileage, kmLeft, dueDate, daysLeft, status);
    }

    private static LocalDate earliest(LocalDate a, LocalDate b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }
}
