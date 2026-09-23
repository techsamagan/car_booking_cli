package com.samagan.car;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The generated data is random, so this only checks the shape of what JavaFaker
 * produces and that the lookups work against it.
 */
class CarFakerDataAccessServiceTest {

    private CarFakerDataAccessService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CarFakerDataAccessService();
    }

    @Test
    @DisplayName("getCars generates 20 cars with plausible values")
    void itShouldGenerateTwentyCars() {
        // When
        List<Car> cars = underTest.getCars();

        // Then
        assertThat(cars).hasSize(20);
        assertThat(cars).allSatisfy(car -> {
            assertThat(car.getId()).isNotNull();
            assertThat(car.getRegNumber()).matches("[A-Z0-9]{2}\\d{2}-[A-Z0-9]{3}");
            assertThat(car.getBrand()).isIn((Object[]) Brand.values());
            assertThat(car.getRentalPricePerDay())
                    .isBetween(BigDecimal.valueOf(30), BigDecimal.valueOf(150));
        });
        assertThat(cars).extracting(Car::getId).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("findCarById and findCarByRegNumber find a generated car")
    void itShouldFindGeneratedCar() {
        // Given
        Car expected = underTest.getCars().get(0);

        // When / Then
        assertThat(underTest.findCarById(expected.getId())).isEqualTo(expected);
        assertThat(underTest.findCarByRegNumber(expected.getRegNumber())).isEqualTo(expected);
        assertThat(underTest.findCarById(UUID.randomUUID())).isNull();
        assertThat(underTest.findCarByRegNumber("nope")).isNull();
    }
}
