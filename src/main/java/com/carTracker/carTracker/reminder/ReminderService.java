package com.carTracker.carTracker.reminder;

import com.carTracker.carTracker.car.CarRepository;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecordRepository;
import com.carTracker.carTracker.mileage.MileageCalculator;
import com.carTracker.carTracker.mileage.MileageStats;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Comparator<ReminderStatus> BY_URGENCY = Comparator
            .comparing(ReminderStatus::status)
            .thenComparing(ReminderStatus::dueDate, Comparator.nullsLast(Comparator.naturalOrder()));

    private final ServiceReminderRepository serviceReminderRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CarRepository carRepository;

    public ReminderService(ServiceReminderRepository serviceReminderRepository,
                           MaintenanceRecordRepository maintenanceRecordRepository, CarRepository carRepository) {
        this.serviceReminderRepository = serviceReminderRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.carRepository = carRepository;
    }

    public List<ReminderStatus> getStatuses(Long carId) {
        MileageStats stats = statsFor(carId);
        LocalDate today = LocalDate.now();
        return serviceReminderRepository.findByCarId(carId).stream()
                .map(r -> ReminderCalculator.calculate(r, stats, today))
                .sorted(BY_URGENCY)
                .toList();
    }

    public List<ReminderStatus> getStatusesRequiringAttention() {
        Map<Long, List<ServiceReminder>> byCar = serviceReminderRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getCar().getId()));
        LocalDate today = LocalDate.now();
        return byCar.entrySet().stream()
                .flatMap(entry -> {
                    MileageStats stats = statsFor(entry.getKey());
                    return entry.getValue().stream().map(r -> ReminderCalculator.calculate(r, stats, today));
                })
                .filter(ReminderStatus::requiresAttention)
                .sorted(BY_URGENCY)
                .toList();
    }

    @Transactional
    public ServiceReminder save(Long carId, ServiceReminder input) {
        ServiceReminder reminder = input.getId() != 0
                ? serviceReminderRepository.findById(input.getId()).orElseThrow()
                : new ServiceReminder();
        reminder.setCar(carRepository.findById(carId).orElseThrow());
        reminder.setName(input.getName().trim());
        reminder.setIntervalKm(input.getIntervalKm());
        reminder.setIntervalMonths(input.getIntervalMonths());
        reminder.setNotifyDaysBefore(input.getNotifyDaysBefore());
        reminder.setLastDoneDate(input.getLastDoneDate());
        reminder.setLastDoneMileage(input.getLastDoneMileage());
        fillMissingLastDone(carId, reminder);
        return serviceReminderRepository.save(reminder);
    }

    @Transactional
    public void delete(Long id) {
        serviceReminderRepository.deleteById(id);
    }

    private void fillMissingLastDone(Long carId, ServiceReminder reminder) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCarIdOrderByDateDescMileageDesc(carId);
        MileageStats stats = MileageCalculator.calculate(records);
        if (reminder.getLastDoneDate() == null && reminder.getLastDoneMileage() == null) {
            records.stream()
                    .filter(r -> r.getType() != null && r.getType().trim().equalsIgnoreCase(reminder.getName()))
                    .findFirst()
                    .ifPresent(r -> {
                        reminder.setLastDoneDate(r.getDate());
                        reminder.setLastDoneMileage(r.getMileage());
                    });
        }
        if (reminder.getLastDoneDate() == null) {
            reminder.setLastDoneDate(LocalDate.now());
        }
        if (reminder.getLastDoneMileage() == null) {
            reminder.setLastDoneMileage(stats.estimatedMileageAt(reminder.getLastDoneDate()));
        }
    }

    private MileageStats statsFor(Long carId) {
        return MileageCalculator.calculate(maintenanceRecordRepository.findByCarIdOrderByDateDescMileageDesc(carId));
    }
}
