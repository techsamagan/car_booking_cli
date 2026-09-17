package com.samagan.car;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CarService {

    private final CarDao carDao;

    public CarService(CarDao carDao) {
        if (carDao == null) {
            throw new IllegalArgumentException("CarDao cannot be null");
        }
        this.carDao = carDao;
    }

    public Car getCarByRegNumber(String regNum) {
        if (regNum == null || regNum.isBlank()) {
            return null;
        }
        return carDao.findCarByRegNumber(regNum);
    }

    public Car getCarById(UUID id) {
        if (id == null) {
            return null;
        }
        return carDao.findCarById(id);
    }

    public List<Car> getAllCars() {
        return carDao.getCars();
    }

    public List<Car> getAllElectricCars() {
        List<Car> all = carDao.getCars();
        List<Car> electricCars = new ArrayList<>();

        for (Car car : all) {
            if (car != null && car.isElectric()) {
                electricCars.add(car);
            }
        }

        return electricCars;
    }
}