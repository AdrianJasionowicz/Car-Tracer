package com.carTracker.carTracker.car;

public record CarView(
        Long id,
        String brand,
        String model,
        Integer mileage,
        String owner,
        String description
) {
}
