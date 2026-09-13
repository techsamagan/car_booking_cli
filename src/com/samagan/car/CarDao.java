package com.samagan.car;

import java.math.BigDecimal;
import java.util.UUID;

public class CarDao {

    private static final Car[] cars;

    static {
        cars = new Car[]{
                new Car(UUID.fromString("1a2b3c4d-0001-4000-8000-000000000001"), "1234", new BigDecimal("89.00"), Brand.TESLA, true),
                new Car(UUID.fromString("2b3c4d5e-0002-4000-8000-000000000002"), "5678", new BigDecimal("55.00"), Brand.AUDI, false),
                new Car(UUID.fromString("3c4d5e6f-0003-4000-8000-000000000003"), "9012", new BigDecimal("70.00"), Brand.MERCEDES, false),
                new Car(UUID.fromString("4d5e6f7a-0004-4000-8000-000000000004"), "3456", new BigDecimal("45.00"), Brand.TOYOTA, false),
                new Car(UUID.fromString("5e6f7a8b-0005-4000-8000-000000000005"), "7890", new BigDecimal("95.00"), Brand.TESLA, true)
        };
    }

    public Car getCarByRegNumber(String regNum){
        for(Car car: cars){
            if(car.getRegNumber().equals(regNum)){
                return car;
            }
        }
        return null;
    }

    public Car getCarById(UUID id){
        for(Car car: cars){
            if (car.getId().equals(id)){
                return car;
            }
        }
        return null;
    }

    public Car[] getAllCars(){
        return cars;
    }


}
