package com.carTracker.carTracker.mileage;

import java.util.List;

public record MileageCheck(
        Integer predictedMileage,
        Integer enteredMileage,
        Double previousAverage,
        Double newAverage,
        List<String> updatedReminders
) {
}
