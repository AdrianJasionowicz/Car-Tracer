package com.carTracker.carTracker.reminder;

import com.carTracker.carTracker.mileage.MileageStats;
import com.carTracker.carTracker.reminder.ReminderStatus.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReminderCalculatorTest {

    static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    static ServiceReminder reminder(Integer intervalKm, Integer intervalMonths, LocalDate lastDate, Integer lastMileage) {
        ServiceReminder reminder = new ServiceReminder();
        reminder.setName("Wymiana oleju");
        reminder.setIntervalKm(intervalKm);
        reminder.setIntervalMonths(intervalMonths);
        reminder.setLastDoneDate(lastDate);
        reminder.setLastDoneMileage(lastMileage);
        return reminder;
    }

    @Test
    void kmIntervalIsConvertedToDaysUsingAverage() {
        MileageStats stats = new MileageStats(50.0, 20_000, TODAY);
        ReminderStatus status = ReminderCalculator.calculate(reminder(15_000, null, TODAY.minusYears(1), 10_000), stats, TODAY);

        assertEquals(25_000, status.dueMileage());
        assertEquals(5_000, status.kmLeft());
        assertEquals(TODAY.plusDays(100), status.dueDate());
        assertEquals(100L, status.daysLeft());
        assertEquals(Status.OK, status.status());
    }

    @Test
    void earlierOfKmAndTimeWins() {
        MileageStats stats = new MileageStats(10.0, 20_000, TODAY);
        ReminderStatus status = ReminderCalculator.calculate(
                reminder(15_000, 12, LocalDate.of(2025, 7, 1), 15_000), stats, TODAY);

        assertEquals(LocalDate.of(2026, 7, 1), status.dueDate());
        assertEquals(Status.OK, status.status());
    }

    @Test
    void soonWithinNotifyWindow() {
        MileageStats stats = new MileageStats(50.0, 24_500, TODAY);
        ReminderStatus status = ReminderCalculator.calculate(reminder(15_000, null, TODAY.minusYears(1), 10_000), stats, TODAY);

        assertEquals(10L, status.daysLeft());
        assertEquals(Status.SOON, status.status());
    }

    @Test
    void overdueWhenKmExceeded() {
        MileageStats stats = new MileageStats(50.0, 25_200, TODAY);
        ReminderStatus status = ReminderCalculator.calculate(reminder(15_000, null, TODAY.minusYears(1), 10_000), stats, TODAY);

        assertEquals(-200, status.kmLeft());
        assertEquals(Status.OVERDUE, status.status());
    }

    @Test
    void estimatesCurrentMileageSinceLastReading() {
        MileageStats stats = new MileageStats(50.0, 20_000, TODAY.minusDays(20));
        ReminderStatus status = ReminderCalculator.calculate(reminder(15_000, null, TODAY.minusYears(1), 10_000), stats, TODAY);

        assertEquals(4_000, status.kmLeft());
        assertEquals(80L, status.daysLeft());
    }

    @Test
    void unknownWithoutData() {
        MileageStats stats = new MileageStats(null, null, null);
        ReminderStatus status = ReminderCalculator.calculate(reminder(15_000, null, null, null), stats, TODAY);

        assertEquals(Status.UNKNOWN, status.status());
    }
}
