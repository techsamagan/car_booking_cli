package com.samagan.booking;

import com.samagan.car.Car;
import com.samagan.car.CarService;
import com.samagan.user.User;
import com.samagan.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static java.lang.Math.max;

public class CarBookingService {
    private final CarBookingDao carBookingDao = new CarBookingDao();
    private final UserService userService = new UserService();
    private final CarService carService = new CarService();

    public CarBooking bookCar(UUID userId, UUID carId, LocalDate startDate, LocalDate endDate) {
        // 1. Look up the user by userId
        // - if not found, throw an exception or print an error
        User user = userService.getUserById(userId);
        if(user == null){
            throw new IllegalStateException("User with this id not found: " + userId);
        }
        // 2. Look up the car by carId
        // - if not found, throw an exception or print an error
        Car car = carService.getCarById(carId);
        if(car == null){
            throw new IllegalStateException("Car with this id not found: " + car);
        }

        // 3. Validate the dates:
        // - startDate must not be in the past
        // - endDate must be after startDate
        // - if invalid, throw IllegalArgumentException or print an error
        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(startDate)){
            throw new IllegalArgumentException("The start date and end date must be valid");
        }

        // 4. Get all current bookings
        CarBooking[] allBookings = carBookingDao.getAllBooking();

        // 5. Check whether an active booking already holds this car
        // - if it does, reject: the car is not available
        for(CarBooking book: allBookings){
            if(book.getCar().getId().equals(carId)){
                System.out.println("The car is not avaiable");
                break;
            }
        }
        // 6. Count the days with ChronoUnit.DAYS.between(startDate, endDate)

        long days = max(ChronoUnit.DAYS.between(startDate, endDate), 1);


        // 7. Calculate the price: car.getRentalPricePerDay() x numberOfDays
        BigDecimal price = car.getRentalPricePerDay().multiply(BigDecimal.valueOf(days));
        // 8. Build a CarBooking with a UUID, user, car, dates, price,
        CarBooking newBooking = new CarBooking(UUID.randomUUID(), user, car, startDate, endDate, price, BookingStatus.COMPLETED, LocalDate.now().atStartOfDay());
        // BookingStatus.ACTIVE and bookedAt = LocalDateTime.now()
        // 9. Save the booking through the DAO
        carBookingDao.save(newBooking);
        // 10. Return the saved booking
        return newBooking;
    }

    public void deleteBooking(UUID id){
        CarBooking cur = carBookingDao.getBookingById(id);
        if (cur == null){
            throw new IllegalStateException("Booking not found with ID: " + id);
        }
        else {
            carBookingDao.updateStatus(id, BookingStatus.CANCELLED);
            System.out.println("Booking with this id was deleted " + id);
        }
    }

    public CarBooking[] getUserBookings(UUID id){
        CarBooking[] all = carBookingDao.getAllBooking();

        int count = 0;
        for (CarBooking booking : all) {
            if (booking != null && booking.getUser().getId().equals(id)) {
                count++;
            }
        }

        // 2. Instantiate array with the exact size needed
        CarBooking[] result = new CarBooking[count];
        int index = 0;
        for (CarBooking booking : all) {
            if (booking != null && booking.getUser().getId().equals(id)) {
                result[index] = booking;
                index++;
            }
        }

        return result;
    }

    public CarBooking[] getAllBooking(){
        return carBookingDao.getAllBooking();
    }

    private boolean isCarBooked(UUID carId, CarBooking[] activeBookings) {
        for (CarBooking booking : activeBookings) {
            if (booking != null
                    && booking.getBookingStatus() == BookingStatus.ACTIVE
                    && booking.getCar().getId().equals(carId)) {
                return true;
            }
        }
        return false;
    }

    public Car[] getAvailableCars(Car[] allCars) {
        CarBooking[] bookings = carBookingDao.getAllBooking();

        int count = 0;
        for (Car car : allCars) {
            if (car != null && !isCarBooked(car.getId(), bookings)) {
                count++;
            }
        }

        Car[] availableCars = new Car[count];
        int index = 0;
        for (Car car : allCars) {
            if (car != null && !isCarBooked(car.getId(), bookings)) {
                availableCars[index++] = car;
            }
        }

        return availableCars;
    }
}
