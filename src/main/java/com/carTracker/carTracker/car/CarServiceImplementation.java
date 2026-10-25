package com.carTracker.carTracker.car;

import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarServiceImplementation implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public CarServiceImplementation(CarRepository carRepository, CarMapper carMapper,
                                    MaintenanceRecordRepository maintenanceRecordRepository) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    @Transactional
    @Override
    public CarView addCar(CarView carView) {
        Car car = carMapper.carViewToCar(carView);
        carRepository.save(car);
        if (car.getMileage() != null) {
            MaintenanceRecord initialReading = new MaintenanceRecord();
            initialReading.setCar(car);
            initialReading.setDate(LocalDate.now());
            initialReading.setMileage(car.getMileage());
            initialReading.setType(MaintenanceRecord.MILEAGE_UPDATE_TYPE);
            initialReading.setDescription("Przebieg początkowy");
            maintenanceRecordRepository.save(initialReading);
        }
        return carMapper.carToCarView(car);
    }

    @Transactional
    @Override
    public CarView updateCar(CarView carView) {
        Car car = carRepository.findById(carView.id()).orElseThrow();
        car.setBrand(carView.brand());
        car.setModel(carView.model());
        car.setOwner(carView.owner());
        car.setDescription(carView.description());
        return carMapper.carToCarView(car);
    }

    @Override
    public CarView getCar(Long id) {
        return carMapper.carToCarView(carRepository.findById(id).orElseThrow());
    }

    @Override
    public List<CarView> getCars() {
        List<Car> carsList = carRepository.findAll();
        return carsList.stream()
                .map(carMapper::carToCarView)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }
}

