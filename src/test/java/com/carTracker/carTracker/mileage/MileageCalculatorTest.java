package com.carTracker.carTracker.mileage;

import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MileageCalculatorTest {

    static MaintenanceRecord reading(LocalDate date, Integer mileage) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setDate(date);
        record.setMileage(mileage);
        record.setType(MaintenanceRecord.MILEAGE_UPDATE_TYPE);
        return record;
    }

    @Test
    void averageFromFirstAndLastReading() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        MileageStats stats = MileageCalculator.calculate(List.of(
                reading(start.plusDays(10), 10_500),
                reading(start, 10_000),
                reading(start.plusDays(5), null),
                reading(start.plusDays(20), 11_000)));

        assertEquals(50.0, stats.averageKmPerDay());
        assertEquals(11_000, stats.lastMileage());
        assertEquals(start.plusDays(20), stats.lastReadingDate());
    }

    @Test
    void noAverageWithSingleReading() {
        LocalDate day = LocalDate.of(2026, 1, 1);
        MileageStats stats = MileageCalculator.calculate(List.of(reading(day, 10_000)));

        assertNull(stats.averageKmPerDay());
        assertEquals(10_000, stats.estimatedMileageAt(day.plusDays(30)));
    }

    @Test
    void estimatesMileageFromAverage() {
        MileageStats stats = new MileageStats(40.0, 20_000, LocalDate.of(2026, 3, 1));

        assertEquals(20_400, stats.estimatedMileageAt(LocalDate.of(2026, 3, 11)));
        assertNull(stats.estimatedMileageAt(LocalDate.of(2026, 2, 1)));
    }
}
