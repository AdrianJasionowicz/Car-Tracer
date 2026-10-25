package com.carTracker.carTracker.reminder;

import java.time.LocalDate;

public record ReminderStatus(
        ServiceReminder reminder,
        Integer dueMileage,
        Integer kmLeft,
        LocalDate dueDate,
        Long daysLeft,
        Status status
) {
    public enum Status {
        OVERDUE("Po terminie"),
        SOON("Wkrótce"),
        OK("OK"),
        UNKNOWN("Brak danych");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public boolean requiresAttention() {
        return status == Status.OVERDUE || status == Status.SOON;
    }
}
