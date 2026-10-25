package com.carTracker.carTracker.car;

import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceImplementationTest {

    @InjectMocks
     CarServiceImplementation carServiceImplementation;
    @Mock
     CarRepository carRepository;
    @Mock
    CarMapper carMapper;
    @Mock
    MaintenanceRecordRepository maintenanceRecordRepository;

    @Test
    void addCar() {
        CarView carView = new CarView(null,"TestBrand","V1",5,"Super Tester",null);
        Car car = new Car();
        car.setId(1L);
        car.setBrand("TestBrand");
        car.setModel("V1");
        car.setMileage(5);
        car.setOwner("Super Tester");
        when(carMapper.carViewToCar(carView)).thenReturn(car);
        when(carMapper.carToCarView(car)).thenReturn(carView);
        CarView result = carServiceImplementation.addCar(carView);
        verify(carMapper).carViewToCar(carView);
        verify(carRepository).save(car);
        verify(maintenanceRecordRepository).save(org.mockito.ArgumentMatchers.argThat(
                (MaintenanceRecord r) -> r.getMileage() == 5 && r.getCar() == car));
        verify(carMapper).carToCarView(car);
        assertEquals(carView, result);
    }

    @Test
    void getCar() {
        Car car = new Car();
        car.setId(1L);
        car.setBrand("TestBrand");
        CarView carView = new CarView(1L,"TestBrand","V1",5,"Super Tester",null);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.carToCarView(car)).thenReturn(carView);

        CarView result = carServiceImplementation.getCar(1L);
        assertEquals(carView, result);
    }

    @Test
    void getCars() {
        Car car = new Car();
        car.setId(1L);
        car.setBrand("TestBrand");
        car.setModel("V1");
        car.setMileage(5);
        car.setOwner("Super Tester");
        Car car2 = new Car();
        car2.setId(2L);
        car2.setBrand("TestBrand2");
        car2.setModel("V2");
        car2.setMileage(5);
        car2.setOwner("Super Tester2");
        List<Car> carsList = new ArrayList<>();
        carsList.add(car);
        carsList.add(car2);

        CarView carView = new CarView(1L,"TestBrand","V1",5,"Super Tester",null);
        CarView carView2 = new CarView(2L,"TestBrand2","V2",5,"Super Tester2",null);

        List<CarView>  carViews = new ArrayList<>();
        carViews.add(carView);
        carViews.add(carView2);
        when(carMapper.carToCarView(car)).thenReturn(carView);
        when(carMapper.carToCarView(car2)).thenReturn(carView2);
        when(carRepository.findAll()).thenReturn(carsList);
        List<CarView> result =  carServiceImplementation.getCars();
        assertEquals(carViews, result);
    }

    @Test
    void deleteCar() {
        Car car = new Car();
        car.setId(1L);
        carServiceImplementation.deleteCar(car.getId());
        verify(carRepository).deleteById(car.getId());
    }
}