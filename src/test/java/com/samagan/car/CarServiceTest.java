package com.samagan.car;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarDao carDao;

    private CarService underTest;

    private Car tesla;
    private Car toyota;

    @BeforeEach
    void setUp() {
        underTest = new CarService(carDao);
        tesla = new Car(UUID.randomUUID(), "EV-001", new BigDecimal("120.00"), Brand.TESLA, true);
        toyota = new Car(UUID.randomUUID(), "TY-002", new BigDecimal("45.00"), Brand.TOYOTA, false);
    }

    @Test
    @DisplayName("getAllCars delegates to the DAO")
    void itShouldGetAllCars() {
        // Given
        when(carDao.getCars()).thenReturn(List.of(tesla, toyota));

        // When / Then
        assertThat(underTest.getAllCars()).containsExactly(tesla, toyota);
        verify(carDao).getCars();
    }

    @Test
    @DisplayName("getCarById returns the car the DAO finds")
    void itShouldGetCarById() {
        // Given
        when(carDao.findCarById(tesla.getId())).thenReturn(tesla);

        // When / Then
        assertThat(underTest.getCarById(tesla.getId())).isEqualTo(tesla);
    }

    @Test
    @DisplayName("getCarById returns null for a null id without hitting the DAO")
    void itShouldReturnNullForNullId() {
        assertThat(underTest.getCarById(null)).isNull();
        verifyNoInteractions(carDao);
    }

    @Test
    @DisplayName("getCarByRegNumber returns the car the DAO finds")
    void itShouldGetCarByRegNumber() {
        // Given
        when(carDao.findCarByRegNumber("EV-001")).thenReturn(tesla);

        // When / Then
        assertThat(underTest.getCarByRegNumber("EV-001")).isEqualTo(tesla);
    }

    @Test
    @DisplayName("getCarByRegNumber returns null for a blank reg number without hitting the DAO")
    void itShouldReturnNullForBlankRegNumber() {
        assertThat(underTest.getCarByRegNumber("  ")).isNull();
        verifyNoInteractions(carDao);
    }

    @Test
    @DisplayName("getAllElectricCars returns only the electric ones")
    void itShouldGetOnlyElectricCars() {
        // Given
        when(carDao.getCars()).thenReturn(List.of(tesla, toyota));

        // When / Then
        assertThat(underTest.getAllElectricCars()).containsExactly(tesla);
    }

    @Test
    @DisplayName("constructor rejects a null CarDao")
    void itShouldRejectNullDao() {
        assertThatThrownBy(() -> new CarService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CarDao cannot be null");
    }
}
