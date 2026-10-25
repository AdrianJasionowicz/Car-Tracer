package com.carTracker.carTracker.car;

import org.springframework.stereotype.Component;

@Component
public class CarMapper {

    public CarView carToCarView(Car car) {
        return new CarView(car.getId(), car.getBrand(), car.getModel(), car.getMileage(), car.getOwner(), car.getDescription());
    }

    public Car carViewToCar(CarView carView) {
        Car car = new Car();
        car.setBrand(carView.brand());
        car.setModel(carView.model());
        car.setMileage(carView.mileage());
        car.setOwner(carView.owner());
        car.setDescription(carView.description());
        return car;
    }
}
