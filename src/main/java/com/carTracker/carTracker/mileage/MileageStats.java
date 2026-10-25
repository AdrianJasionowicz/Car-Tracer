package com.carTracker.carTracker.mileage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record MileageStats(Double averageKmPerDay, Integer lastMileage, LocalDate lastReadingDate) {

    public Integer estimatedMileageAt(LocalDate date) {
        if (lastMileage == null || date.isBefore(lastReadingDate)) {
            return null;
        }
        if (averageKmPerDay == null) {
            return lastMileage;
        }
        long days = ChronoUnit.DAYS.between(lastReadingDate, date);
        return lastMileage + (int) Math.round(averageKmPerDay * days);
    }
}
