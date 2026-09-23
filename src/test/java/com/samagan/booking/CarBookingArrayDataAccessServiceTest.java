package com.samagan.booking;

import com.samagan.car.Brand;
import com.samagan.car.Car;
import com.samagan.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarBookingArrayDataAccessServiceTest {

    private CarBookingArrayDataAccessService underTest;

    private User user;
    private Car car;

    @BeforeEach
    void setUp() {
        underTest = new CarBookingArrayDataAccessService();
        user = new User(UUID.randomUUID(), "James");
        car = new Car(UUID.randomUUID(), "1234", new BigDecimal("89.00"), Brand.TESLA, true);
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
    @DisplayName("a new store starts empty")
    void itShouldStartEmpty() {
        assertThat(underTest.getBookings()).isEmpty();
    }

    @Test
    @DisplayName("saveBooking stores the booking and getBookings reads it back")
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
    @DisplayName("findBookingById returns the matching booking")
    void itShouldFindBookingById() {
        // Given
        CarBooking booking = newBooking();
        underTest.saveBooking(booking);

        // When / Then
        assertThat(underTest.findBookingById(booking.getId())).isEqualTo(booking);
    }

    @Test
    @DisplayName("findBookingById returns null for an unknown id and for null")
    void itShouldReturnNullWhenBookingNotFound() {
        underTest.saveBooking(newBooking());

        assertThat(underTest.findBookingById(UUID.randomUUID())).isNull();
        assertThat(underTest.findBookingById(null)).isNull();
    }

    @Test
    @DisplayName("deleteBooking marks the booking CANCELLED rather than removing it")
    void itShouldCancelBookingOnDelete() {
        // Given
        CarBooking booking = newBooking();
        underTest.saveBooking(booking);

        // When
        underTest.deleteBooking(booking.getId());

        // Then: this DAO cancels rather than deletes, so the record stays
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
    @DisplayName("saveBooking and deleteBooking reject nulls")
    void itShouldRejectNulls() {
        assertThatThrownBy(() -> underTest.saveBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking cannot be null.");

        assertThatThrownBy(() -> underTest.deleteBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking ID cannot be null.");
    }

    @Test
    @DisplayName("getBookings returns a copy, so callers cannot mutate the store")
    void itShouldReturnADefensiveCopy() {
        // Given
        underTest.saveBooking(newBooking());

        // When
        underTest.getBookings().clear();

        // Then
        assertThat(underTest.getBookings()).hasSize(1);
    }
}
