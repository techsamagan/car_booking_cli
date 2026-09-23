package com.samagan.booking;

import com.samagan.car.Car;
import com.samagan.car.CarArrayDataAccessService;
import com.samagan.car.CarDao;
import com.samagan.user.User;
import com.samagan.user.UserArrayDataAccessService;
import com.samagan.user.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarBookingFileDataAccessServiceTest {

    // @TempDir gives each test its own directory, so the tests never interfere
    // with each other and nothing is written into the project.
    @TempDir
    Path tempDir;

    private CarBookingFileDataAccessService underTest;

    // parseLine resolves the user and car through the injected DAOs, so bookings
    // have to be built from IDs those DAOs actually know about.
    private final UserDao userDao = new UserArrayDataAccessService();
    private final CarDao carDao = new CarArrayDataAccessService();

    private User user;
    private Car car;

    @BeforeEach
    void setUp() {
        String filePath = tempDir.resolve("bookings.csv").toString();
        underTest = new CarBookingFileDataAccessService(filePath, userDao, carDao);

        user = userDao.findUserById(UUID.fromString("8ca51d2b-aa40-42cb-b73a-46329e3423b5"));
        car = carDao.findCarById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    }

    private CarBooking newBooking() {
        return new CarBooking(
                UUID.randomUUID(),
                user,
                car,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 3),
                new BigDecimal("178.00"),
                BookingStatus.ACTIVE,
                LocalDateTime.of(2026, 9, 23, 12, 0)
        );
    }

    @Test
    @DisplayName("a new file starts empty")
    void itShouldStartEmpty() {
        assertThat(underTest.getBookings()).isEmpty();
    }

    @Test
    @DisplayName("saveBooking writes to the file and getBookings reads it back")
    void itShouldSaveAndRetrieveBooking() {
        // Given
        CarBooking booking = newBooking();

        // When
        underTest.saveBooking(booking);
        List<CarBooking> bookings = underTest.getBookings();

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("every field survives the round trip through the file")
    void itShouldRoundTripEveryField() {
        // Given
        CarBooking booking = newBooking();

        // When
        underTest.saveBooking(booking);
        CarBooking read = underTest.getBookings().get(0);

        // Then
        assertThat(read.getUser()).isEqualTo(user);
        assertThat(read.getCar()).isEqualTo(car);
        assertThat(read.getStartDate()).isEqualTo(booking.getStartDate());
        assertThat(read.getEndDate()).isEqualTo(booking.getEndDate());
        assertThat(read.getPrice()).isEqualByComparingTo(booking.getPrice());
        assertThat(read.getBookingStatus()).isEqualTo(BookingStatus.ACTIVE);
        assertThat(read.getBookedAt()).isEqualTo(booking.getBookedAt());
    }

    @Test
    @DisplayName("a second booking is appended rather than overwriting the first")
    void itShouldSaveMultipleBookings() {
        // Given
        CarBooking first = newBooking();
        CarBooking second = newBooking();

        // When
        underTest.saveBooking(first);
        underTest.saveBooking(second);

        // Then
        assertThat(underTest.getBookings())
                .extracting(CarBooking::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("findBookingById reads the booking back from the file")
    void itShouldFindBookingById() {
        // Given
        CarBooking booking = newBooking();
        underTest.saveBooking(booking);

        // When
        CarBooking found = underTest.findBookingById(booking.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("findBookingById returns null for an unknown id and for null")
    void itShouldReturnNullWhenBookingNotFound() {
        underTest.saveBooking(newBooking());

        assertThat(underTest.findBookingById(UUID.randomUUID())).isNull();
        assertThat(underTest.findBookingById(null)).isNull();
    }

    @Test
    @DisplayName("deleteBooking marks the booking CANCELLED in the file rather than removing it")
    void itShouldCancelBookingOnDelete() {
        // Given
        CarBooking booking = newBooking();
        underTest.saveBooking(booking);

        // When
        underTest.deleteBooking(booking.getId());

        // Then: this DAO cancels rather than deletes, so the record stays on disk
        List<CarBooking> bookings = underTest.getBookings();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("deleteBooking throws when the booking does not exist")
    void itShouldThrowWhenDeletingUnknownBooking() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> underTest.deleteBooking(unknownId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    @DisplayName("a second instance sees bookings written by the first")
    void itShouldPersistAcrossInstances() {
        // Given
        CarBooking booking = newBooking();
        underTest.saveBooking(booking);

        // When
        CarBookingFileDataAccessService reopened = new CarBookingFileDataAccessService(
                tempDir.resolve("bookings.csv").toString(), userDao, carDao);

        // Then
        assertThat(reopened.getBookings())
                .extracting(CarBooking::getId)
                .containsExactly(booking.getId());
    }

    @Test
    @DisplayName("the constructor rejects a blank path and null DAOs")
    void itShouldRejectInvalidConstructorArguments() {
        assertThatThrownBy(() -> new CarBookingFileDataAccessService("  ", userDao, carDao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File path cannot be empty.");

        assertThatThrownBy(() -> new CarBookingFileDataAccessService("bookings.csv", null, carDao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserDao cannot be null.");

        assertThatThrownBy(() -> new CarBookingFileDataAccessService("bookings.csv", userDao, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CarDao cannot be null.");
    }

    @Test
    @DisplayName("saveBooking rejects a null booking")
    void itShouldRejectNullBooking() {
        assertThatThrownBy(() -> underTest.saveBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking cannot be null.");
    }
}
