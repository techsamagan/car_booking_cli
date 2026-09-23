package com.samagan.car;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CarArrayDataAccessServiceTest {

    private static final UUID TESLA_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private CarArrayDataAccessService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CarArrayDataAccessService();
    }

    @Test
    @DisplayName("getCars returns every seeded car")
    void itShouldGetAllCars() {
        // When
        List<Car> cars = underTest.getCars();

        // Then
        assertThat(cars)
                .hasSize(3)
                .extracting(Car::getBrand)
                .containsExactly(Brand.TESLA, Brand.AUDI, Brand.MERCEDES);
    }

    @Test
    @DisplayName("findCarById returns the matching car")
    void itShouldFindCarById() {
        // When
        Car car = underTest.findCarById(TESLA_ID);

        // Then
        assertThat(car).isNotNull();
        assertThat(car.getId()).isEqualTo(TESLA_ID);
        assertThat(car.getBrand()).isEqualTo(Brand.TESLA);
        assertThat(car.isElectric()).isTrue();
    }

    @Test
    @DisplayName("findCarById returns null for an unknown id and for null")
    void itShouldReturnNullWhenCarNotFound() {
        assertThat(underTest.findCarById(UUID.randomUUID())).isNull();
        assertThat(underTest.findCarById(null)).isNull();
    }

    @Test
    @DisplayName("findCarByRegNumber returns the matching car")
    void itShouldFindCarByRegNumber() {
        // When
        Car car = underTest.findCarByRegNumber("5678");

        // Then
        assertThat(car).isNotNull();
        assertThat(car.getBrand()).isEqualTo(Brand.AUDI);
    }

    @Test
    @DisplayName("findCarByRegNumber returns null for an unknown, blank or null reg number")
    void itShouldReturnNullWhenRegNumberNotFound() {
        assertThat(underTest.findCarByRegNumber("does-not-exist")).isNull();
        assertThat(underTest.findCarByRegNumber("  ")).isNull();
        assertThat(underTest.findCarByRegNumber(null)).isNull();
    }
}
