package com.samagan.car;

import java.util.UUID;

public class CarService {

    private final CarDao carDao;

    public CarService(CarDao carDao) {
        this.carDao = carDao;
    }

    public Car getCarByRegNumber(String regNum) {
        return carDao.findCarByRegNumber(regNum);
    }

    public Car getCarById(UUID id) {
        return carDao.findCarById(id);
    }

    public Car[] getAllCars() {
        return carDao.getCars();
    }

    public Car[] getAllElectricCars() {
        Car[] all = carDao.getCars();

        int count = 0;

        for (Car car : all) {
            if (car != null && car.isElectric()) {
                count++;
            }
        }

        Car[] result = new Car[count];
        int index = 0;

        for (Car car : all) {
            if (car != null && car.isElectric()) {
                result[index++] = car;
            }
        }

        return result;
    }
}