package com.carTracker.carTracker.car;

import java.util.List;

public interface CarService {

    CarView addCar(CarView car);
    CarView updateCar(CarView car);
    CarView getCar(Long id);
    List<CarView> getCars();
    void deleteCar(Long id);


}
