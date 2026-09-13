package com.samagan.car;

import java.util.UUID;

public interface CarDao {

    Car[] getCars();

    Car findCarById(UUID carId);

    Car findCarByRegNumber(String regNum);
}