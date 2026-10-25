package com.carTracker.carTracker.maintenanceRecord;

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
public class MaintenanceRecord {
    public static final String MILEAGE_UPDATE_TYPE = "Aktualizacja przebiegu";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private LocalDate date;
    private Integer mileage;
    private String type;
    @Column(length = 2000)
    private String description;
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

}
