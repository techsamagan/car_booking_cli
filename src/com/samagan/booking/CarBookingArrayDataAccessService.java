package com.samagan.booking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CarBookingArrayDataAccessService implements CarBookingDao {

    private static final int CAPACITY = 100;
    private final List<CarBooking> bookings;

    public CarBookingArrayDataAccessService() {
        this.bookings = new ArrayList<>(CAPACITY);
    }

    @Override
    public List<CarBooking> getBookings() {
        return new ArrayList<>(bookings);
    }

    @Override
    public CarBooking findBookingById(UUID bookingId) {
        if (bookingId == null) {
            return null;
        }
        return bookings.stream()
                .filter(book -> book.getId().equals(bookingId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void saveBooking(CarBooking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }

        if (bookings.size() >= CAPACITY) {
            throw new IllegalStateException("Booking storage capacity reached (" + CAPACITY + ").");
        }

        bookings.add(booking);
    }

    @Override
    public void deleteBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null.");
        }

        CarBooking targetBooking = bookings.stream()
                .filter(booking -> booking != null && bookingId.equals(booking.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Booking with ID " + bookingId + " not found."));

        targetBooking.setBookingStatus(BookingStatus.CANCELLED);
    }
}