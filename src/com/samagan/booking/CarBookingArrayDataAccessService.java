package com.samagan.booking;

import java.util.Arrays;
import java.util.UUID;

public class CarBookingArrayDataAccessService implements CarBookingDao {

    private static final int CAPACITY = 100;

    private final CarBooking[] bookings = new CarBooking[CAPACITY];
    private int size = 0;

    @Override
    public CarBooking[] getBookings() {
        return Arrays.copyOf(bookings, size);
    }

    @Override
    public CarBooking findBookingById(UUID bookingId) {
        if (bookingId == null) {
            return null;
        }

        for (int i = 0; i < size; i++) {
            CarBooking booking = bookings[i];

            if (bookingId.equals(booking.getId())) {
                return booking;
            }
        }

        return null;
    }

    @Override
    public void saveBooking(CarBooking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }

        if (size >= CAPACITY) {
            throw new IllegalStateException(
                    "Booking storage capacity reached (" + CAPACITY + ")."
            );
        }

        bookings[size++] = booking;
    }

    @Override
    public void deleteBooking(UUID bookingId) {
        CarBooking booking = findBookingById(bookingId);

        if (booking == null) {
            throw new IllegalStateException(
                    "Booking with ID " + bookingId + " not found."
            );
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
    }
}