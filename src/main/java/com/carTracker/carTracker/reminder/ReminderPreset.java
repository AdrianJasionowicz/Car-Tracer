package com.carTracker.carTracker.reminder;

import java.util.List;
import java.util.Optional;

public record ReminderPreset(String name, Integer intervalKm, Integer intervalMonths) {

    public static final List<ReminderPreset> PRESETS = List.of(
            new ReminderPreset("Wymiana oleju", 15_000, 12),
            new ReminderPreset("Filtr powietrza", 15_000, 12),
            new ReminderPreset("Filtr kabinowy", 15_000, 12),
            new ReminderPreset("Klocki hamulcowe", 30_000, null),
            new ReminderPreset("Tarcze hamulcowe", 60_000, null),
            new ReminderPreset("Płyn hamulcowy", null, 24),
            new ReminderPreset("Rozrząd", 90_000, 60),
            new ReminderPreset("Świece zapłonowe", 30_000, null),
            new ReminderPreset("Przegląd techniczny", null, 12),
            new ReminderPreset("Ubezpieczenie OC", null, 12),
            new ReminderPreset("Wymiana opon", null, 6)
    );

    public static Optional<ReminderPreset> find(String name) {
        return PRESETS.stream().filter(p -> p.name().equalsIgnoreCase(name)).findFirst();
    }
}
