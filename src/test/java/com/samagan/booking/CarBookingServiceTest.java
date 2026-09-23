package com.samagan.booking;

import com.samagan.car.Brand;
import com.samagan.car.Car;
import com.samagan.car.CarService;
import com.samagan.user.User;
import com.samagan.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarBookingServiceTest {

    @Mock
    private CarBookingDao carBookingDao;

    @Mock
    private UserService userService;

    @Mock
    private CarService carService;

    @Captor
    private ArgumentCaptor<CarBooking> bookingCaptor;

    private CarBookingService underTest;

    private User user;
    private Car car;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        underTest = new CarBookingService(carBookingDao, userService, carService);

        user = new User(UUID.randomUUID(), "James");
        car = new Car(UUID.randomUUID(), "1234", new BigDecimal("89.00"), Brand.TESLA, true);

        // bookCar rejects start dates in the past, so the window has to be in the future
        startDate = LocalDate.now().plusDays(1);
        endDate = startDate.plusDays(2);
    }

    private CarBooking bookingFor(User bookingUser, Car bookingCar, BookingStatus status) {
        return new CarBooking(
                UUID.randomUUID(),
                bookingUser,
                bookingCar,
                startDate,
                endDate,
                new BigDecimal("178.00"),
                status,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("bookCar saves an ACTIVE booking priced per day")
    void itShouldBookCar() {
        // Given
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(car.getId())).thenReturn(car);
        when(carBookingDao.getBookings()).thenReturn(List.of());

        // When
        CarBooking result = underTest.bookCar(user.getId(), car.getId(), startDate, endDate);

        // Then
        verify(carBookingDao).saveBooking(bookingCaptor.capture());
        CarBooking saved = bookingCaptor.getValue();

        assertThat(saved).isEqualTo(result);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getCar()).isEqualTo(car);
        assertThat(saved.getStartDate()).isEqualTo(startDate);
        assertThat(saved.getEndDate()).isEqualTo(endDate);
        assertThat(saved.getBookingStatus()).isEqualTo(BookingStatus.ACTIVE);
        // 2 days at 89.00
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("178.00"));
    }

    @Test
    @DisplayName("bookCar charges a minimum of one day when start and end are the same")
    void itShouldChargeMinimumOneDay() {
        // Given
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(car.getId())).thenReturn(car);
        when(carBookingDao.getBookings()).thenReturn(List.of());

        // When
        CarBooking result = underTest.bookCar(user.getId(), car.getId(), startDate, startDate);

        // Then
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("89.00"));
    }

    @Test
    @DisplayName("bookCar throws when the user does not exist")
    void itShouldThrowWhenUserNotFound() {
        // Given
        UUID unknownUserId = UUID.randomUUID();
        when(userService.getUserById(unknownUserId)).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> underTest.bookCar(unknownUserId, car.getId(), startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(unknownUserId.toString());

        verify(carBookingDao, never()).saveBooking(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("bookCar throws when the car does not exist")
    void itShouldThrowWhenCarNotFound() {
        // Given
        UUID unknownCarId = UUID.randomUUID();
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(unknownCarId)).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> underTest.bookCar(user.getId(), unknownCarId, startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(unknownCarId.toString());
    }

    @Test
    @DisplayName("bookCar rejects a start date in the past")
    void itShouldRejectPastStartDate() {
        // Given
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(car.getId())).thenReturn(car);

        // When / Then
        assertThatThrownBy(() -> underTest.bookCar(
                user.getId(), car.getId(), LocalDate.now().minusDays(1), endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date cannot be in the past.");
    }

    @Test
    @DisplayName("bookCar rejects an end date before the start date")
    void itShouldRejectEndDateBeforeStartDate() {
        // Given
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(car.getId())).thenReturn(car);

        // When / Then
        assertThatThrownBy(() -> underTest.bookCar(
                user.getId(), car.getId(), endDate, startDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date cannot be before start date.");
    }

    @Test
    @DisplayName("bookCar throws when the car already has an ACTIVE booking")
    void itShouldThrowWhenCarAlreadyBooked() {
        // Given
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(car.getId())).thenReturn(car);
        when(carBookingDao.getBookings()).thenReturn(List.of(bookingFor(user, car, BookingStatus.ACTIVE)));

        // When / Then
        assertThatThrownBy(() -> underTest.bookCar(user.getId(), car.getId(), startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("bookCar allows a car whose only booking was CANCELLED")
    void itShouldAllowBookingCancelledCar() {
        // Given
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(carService.getCarById(car.getId())).thenReturn(car);
        when(carBookingDao.getBookings()).thenReturn(List.of(bookingFor(user, car, BookingStatus.CANCELLED)));

        // When / Then
        assertThat(underTest.bookCar(user.getId(), car.getId(), startDate, endDate)).isNotNull();
        verify(carBookingDao).saveBooking(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("deleteBooking delegates to the DAO")
    void itShouldDeleteBooking() {
        // Given
        UUID bookingId = UUID.randomUUID();

        // When
        underTest.deleteBooking(bookingId);

        // Then
        verify(carBookingDao).deleteBooking(bookingId);
    }

    @Test
    @DisplayName("deleteBooking rejects a null id")
    void itShouldRejectNullBookingId() {
        assertThatThrownBy(() -> underTest.deleteBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking ID cannot be null.");

        verify(carBookingDao, never()).deleteBooking(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("getAllBookings delegates to the DAO")
    void itShouldGetAllBookings() {
        // Given
        List<CarBooking> bookings = List.of(bookingFor(user, car, BookingStatus.ACTIVE));
        when(carBookingDao.getBookings()).thenReturn(bookings);

        // When / Then
        assertThat(underTest.getAllBookings()).isEqualTo(bookings);
    }

    @Test
    @DisplayName("getUserBookings returns only that user's bookings")
    void itShouldGetUserBookings() {
        // Given
        User otherUser = new User(UUID.randomUUID(), "Jamila");
        CarBooking mine = bookingFor(user, car, BookingStatus.ACTIVE);
        CarBooking theirs = bookingFor(otherUser, car, BookingStatus.ACTIVE);
        when(carBookingDao.getBookings()).thenReturn(List.of(mine, theirs));

        // When / Then
        assertThat(underTest.getUserBookings(user.getId())).containsExactly(mine);
    }

    @Test
    @DisplayName("getUserBookings returns an empty list for a null id")
    void itShouldReturnEmptyForNullUserId() {
        assertThat(underTest.getUserBookings(null)).isEmpty();
    }

    @Test
    @DisplayName("getAvailableCars excludes cars with an ACTIVE booking")
    void itShouldGetAvailableCars() {
        // Given
        Car freeCar = new Car(UUID.randomUUID(), "5678", new BigDecimal("55.00"), Brand.AUDI, false);
        when(carBookingDao.getBookings()).thenReturn(List.of(bookingFor(user, car, BookingStatus.ACTIVE)));

        // When / Then
        assertThat(underTest.getAvailableCars(List.of(car, freeCar))).containsExactly(freeCar);
    }

    @Test
    @DisplayName("getAvailableCars returns an empty list when given null")
    void itShouldReturnEmptyForNullCarList() {
        assertThat(underTest.getAvailableCars(null)).isEmpty();
    }

    @Test
    @DisplayName("constructor rejects null dependencies")
    void itShouldRejectNullDependencies() {
        assertThatThrownBy(() -> new CarBookingService(null, userService, carService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CarBookingDao cannot be null.");

        assertThatThrownBy(() -> new CarBookingService(carBookingDao, null, carService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserService cannot be null.");

        assertThatThrownBy(() -> new CarBookingService(carBookingDao, userService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CarService cannot be null.");
    }
}
