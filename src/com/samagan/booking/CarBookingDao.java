package com.samagan.booking;

import com.samagan.car.Car;

import java.util.UUID;

public class CarBookingDao {

    private static final int CAPACITY = 100;
    private final CarBooking[] bookings = new CarBooking[CAPACITY];
    private int nextIndex = 0;

    public void save(CarBooking booking) {
        bookings[nextIndex] = booking;
        nextIndex++;
    }

    public CarBooking[] getAllBooking(){
        return bookings;
    }

    public CarBooking getBookingById(UUID id){
        for(CarBooking book: bookings){
            if(book.getId().equals(id)){
                return book;
            }
        }
        return null;
    }

    public void updateStatus(UUID id, BookingStatus bookingStatus) {
        for (CarBooking book : bookings) {
            if (book != null && book.getId().equals(id)) {
                book.setBookingStatus(bookingStatus);
                return;
            }
        }
        throw new IllegalArgumentException("Booking not found with id: " + id);
    }
}
