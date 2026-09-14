package com.samagan.car;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CarArrayDataAccessService implements CarDao {

    private static final List<Car> CARS = List.of(
            new Car(
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                    "1234",
                    new BigDecimal("89.00"),
                    Brand.TESLA,
                    true
            ),
            new Car(
                    UUID.fromString("223e4567-e89b-12d3-a456-426614174001"),
                    "5678",
                    new BigDecimal("55.00"),
                    Brand.AUDI,
                    false
            ),
            new Car(
                    UUID.fromString("323e4567-e89b-12d3-a456-426614174002"),
                    "9012",
                    new BigDecimal("60.00"),
                    Brand.MERCEDES,
                    false
            )
    );

    @Override
    public List<Car> getCars() {
        return CARS;
    }

    @Override
    public Car findCarById(UUID carId) {
        if (carId == null) {
            return null;
        }

        for (Car car : CARS) {
            if (carId.equals(car.getId())) {
                return car;
            }
        }

        return null;
    }

    @Override
    public Car findCarByRegNumber(String regNum) {
        if (regNum == null || regNum.isBlank()) {
            return null;
        }

        for (Car car : CARS) {
            if (regNum.equalsIgnoreCase(car.getRegNumber())) {
                return car;
            }
        }

        return null;
    }
}