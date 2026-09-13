package com.samagan.car;

import java.util.UUID;

public class CarService {
    private final CarDao carDao = new CarDao();

    public Car getCarByRegNumber(String regNum){

        return carDao.getCarByRegNumber(regNum);
    }

    public Car getCarById(UUID id){

        return carDao.getCarById(id);
    }

    public Car[] getAllCars(){
        return carDao.getAllCars();
    }

    public Car[] getAllElectricCars() {
        Car[] all = carDao.getAllCars();
        int count = 0;

        // 1. Count how many are electric
        for (Car car : all) {
            if (car != null && car.isElectric()) {
                count++;
            }
        }

        // 2. Populate result using a separate pointer
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
