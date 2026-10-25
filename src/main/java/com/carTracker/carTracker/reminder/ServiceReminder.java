package com.carTracker.carTracker.reminder;

import com.carTracker.carTracker.car.Car;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceReminder {
    public static final int DEFAULT_NOTIFY_DAYS_BEFORE = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private Integer intervalKm;
    private Integer intervalMonths;
    private LocalDate lastDoneDate;
    private Integer lastDoneMileage;
    private Integer notifyDaysBefore;
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    public int notifyDaysBeforeOrDefault() {
        return notifyDaysBefore != null ? notifyDaysBefore : DEFAULT_NOTIFY_DAYS_BEFORE;
    }
}
