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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarDao carDao;

    private CarService carService;

    private Car tesla;
    private Car toyota;

    @BeforeEach
    void setUp() {
        carService = new CarService(carDao);
        tesla = new Car(UUID.randomUUID(), "EV-001", new BigDecimal("120.00"), Brand.TESLA, true);
        toyota = new Car(UUID.randomUUID(), "TY-002", new BigDecimal("45.00"), Brand.TOYOTA, false);
    }

    @Test
    @DisplayName("getAllElectricCars returns only the electric ones")
    void getAllElectricCarsFiltersByElectric() {
        when(carDao.getCars()).thenReturn(List.of(tesla, toyota));

        assertThat(carService.getAllElectricCars()).containsExactly(tesla);
    }

    @Test
    @DisplayName("getCarById returns null for a null id without hitting the DAO")
    void getCarByIdReturnsNullForNullId() {
        assertThat(carService.getCarById(null)).isNull();
    }

    @Test
    @DisplayName("constructor rejects a null CarDao")
    void constructorRejectsNullDao() {
        assertThatThrownBy(() -> new CarService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CarDao cannot be null");
    }
}
