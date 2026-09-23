package com.samagan.car;

import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CarFakerDataAccessService implements CarDao {

    private static final int CAR_COUNT = 20;

    private final List<Car> cars;

    public CarFakerDataAccessService() {
        this.cars = generateCars();
    }

    @Override
    public List<Car> getCars() {
        return List.copyOf(cars);
    }

    @Override
    public Car findCarById(UUID carId) {
        if (carId == null) {
            return null;
        }

        return cars.stream()
                .filter(car -> car.getId().equals(carId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Car findCarByRegNumber(String regNum) {
        if (regNum == null || regNum.isBlank()) {
            return null;
        }

        return cars.stream()
                .filter(car -> car.getRegNumber().equals(regNum))
                .findFirst()
                .orElse(null);
    }

    private static List<Car> generateCars() {
        Faker faker = new Faker();
        Brand[] brands = Brand.values();
        List<Car> generated = new ArrayList<>(CAR_COUNT);

        for (int i = 0; i < CAR_COUNT; i++) {
            generated.add(new Car(
                    UUID.randomUUID(),
                    // JavaFaker has no car provider, so reg numbers are built by hand:
                    // two letters, two digits, three letters -> "AB12 CDE"
                    faker.bothify("??##-???").toUpperCase(),
                    BigDecimal.valueOf(faker.number().numberBetween(30, 150)),
                    brands[faker.random().nextInt(brands.length)],
                    faker.bool().bool()
            ));
        }

        return List.copyOf(generated);
    }
}
