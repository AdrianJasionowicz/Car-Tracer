package com.carTracker.carTracker.mileage;

import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public final class MileageCalculator {

    private MileageCalculator() {
    }

    public static MileageStats calculate(List<MaintenanceRecord> records) {
        List<MaintenanceRecord> readings = records.stream()
                .filter(r -> r.getMileage() != null && r.getDate() != null)
                .sorted(Comparator.comparing(MaintenanceRecord::getDate).thenComparing(MaintenanceRecord::getMileage))
                .toList();
        if (readings.isEmpty()) {
            return new MileageStats(null, null, null);
        }
        MaintenanceRecord first = readings.getFirst();
        MaintenanceRecord last = readings.getLast();
        long days = ChronoUnit.DAYS.between(first.getDate(), last.getDate());
        Double average = days > 0 ? (double) (last.getMileage() - first.getMileage()) / days : null;
        return new MileageStats(average, last.getMileage(), last.getDate());
    }
}
