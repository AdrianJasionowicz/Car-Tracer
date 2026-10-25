package com.carTracker.carTracker.maintenanceRecord;

import com.carTracker.carTracker.car.Car;
import com.carTracker.carTracker.car.CarRepository;
import com.carTracker.carTracker.mileage.MileageCalculator;
import com.carTracker.carTracker.mileage.MileageCheck;
import com.carTracker.carTracker.mileage.MileageStats;
import com.carTracker.carTracker.reminder.ServiceReminder;
import com.carTracker.carTracker.reminder.ServiceReminderRepository;
import com.carTracker.carTracker.util.Formats;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaintenanceRecordService implements MaintenanceRecordInterface {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CarRepository carRepository;
    private final ServiceReminderRepository serviceReminderRepository;

    public MaintenanceRecordService(MaintenanceRecordRepository maintenanceRecordRepository, CarRepository carRepository,
                                    ServiceReminderRepository serviceReminderRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.carRepository = carRepository;
        this.serviceReminderRepository = serviceReminderRepository;
    }

    @Override
    public MaintenanceRecord getMaintenanceRecord(Long id) {
        return maintenanceRecordRepository.findById(id).orElseThrow();
    }

    @Transactional
    @Override
    public MileageCheck addMaintenanceRecord(Long carId, MaintenanceRecord record) {
        Car car = carRepository.findById(carId).orElseThrow();
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCarIdOrderByDateDescMileageDesc(carId);
        validateMileage(records, record);

        MileageStats before = MileageCalculator.calculate(records);
        Integer predicted = record.getMileage() != null ? before.estimatedMileageAt(record.getDate()) : null;

        record.setCar(car);
        maintenanceRecordRepository.save(record);
        List<MaintenanceRecord> allRecords = new ArrayList<>(records);
        allRecords.add(record);
        MileageStats after = MileageCalculator.calculate(allRecords);
        car.setMileage(after.lastMileage());

        List<String> updatedReminders = updateReminders(carId, record, after);
        return new MileageCheck(predicted, record.getMileage(), before.averageKmPerDay(), after.averageKmPerDay(), updatedReminders);
    }

    @Transactional
    @Override
    public void deleteMaintenanceRecord(Long id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id).orElseThrow();
        Car car = record.getCar();
        maintenanceRecordRepository.delete(record);
        List<MaintenanceRecord> remaining = maintenanceRecordRepository.findByCarIdOrderByDateDescMileageDesc(car.getId());
        remaining.removeIf(r -> r.getId() == id);
        car.setMileage(MileageCalculator.calculate(remaining).lastMileage());
    }

    @Override
    public List<MaintenanceRecord> getAllMaintenanceRecordsToCar(Long carId) {
        return maintenanceRecordRepository.findByCarIdOrderByDateDescMileageDesc(carId);
    }

    @Override
    public MileageStats getMileageStats(Long carId) {
        return MileageCalculator.calculate(maintenanceRecordRepository.findByCarIdOrderByDateDescMileageDesc(carId));
    }

    private void validateMileage(List<MaintenanceRecord> records, MaintenanceRecord record) {
        if (record.getMileage() == null) {
            return;
        }
        for (MaintenanceRecord existing : records) {
            if (existing.getMileage() == null) {
                continue;
            }
            boolean earlierOrSameDay = !existing.getDate().isAfter(record.getDate());
            if (earlierOrSameDay && existing.getMileage() > record.getMileage()) {
                throw new IllegalArgumentException(String.format("Przebieg %s jest mniejszy niż wcześniejszy odczyt %s z dnia %s.",
                        Formats.km(record.getMileage()), Formats.km(existing.getMileage()), Formats.date(existing.getDate())));
            }
            if (!earlierOrSameDay && existing.getMileage() < record.getMileage()) {
                throw new IllegalArgumentException(String.format("Przebieg %s jest większy niż późniejszy odczyt %s z dnia %s.",
                        Formats.km(record.getMileage()), Formats.km(existing.getMileage()), Formats.date(existing.getDate())));
            }
        }
    }

    private List<String> updateReminders(Long carId, MaintenanceRecord record, MileageStats stats) {
        List<String> updated = new ArrayList<>();
        for (ServiceReminder reminder : serviceReminderRepository.findByCarId(carId)) {
            boolean sameType = reminder.getName().trim().equalsIgnoreCase(record.getType().trim());
            boolean newer = reminder.getLastDoneDate() == null || !record.getDate().isBefore(reminder.getLastDoneDate());
            if (sameType && newer) {
                reminder.setLastDoneDate(record.getDate());
                Integer mileage = record.getMileage() != null ? record.getMileage() : stats.estimatedMileageAt(record.getDate());
                reminder.setLastDoneMileage(mileage);
                updated.add(reminder.getName());
            }
        }
        return updated;
    }
}
