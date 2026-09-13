package com.samagan.booking;

import com.samagan.car.Car;
import com.samagan.car.CarService;
import com.samagan.user.User;
import com.samagan.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static java.lang.Math.max;

public class CarBookingService {
    private final CarBookingDao carBookingDao;
    private final UserService userService;
    private final CarService carService;

    public CarBookingService(CarBookingDao carBookingDao, UserService userService, CarService carService) {
        this.carBookingDao = carBookingDao;
        this.userService = userService;
        this.carService = carService;
    }

    public CarBooking bookCar(UUID userId, UUID carId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalStateException("User with this id not found: " + userId);
        }

        Car car = carService.getCarById(carId);
        if (car == null) {
            throw new IllegalStateException("Car with this id not found: " + carId);
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        CarBooking[] allBookings = carBookingDao.getBookings();
        if (isCarBooked(carId, allBookings)) {
            throw new IllegalStateException("Car with ID " + carId + " is already booked.");
        }

        long days = max(ChronoUnit.DAYS.between(startDate, endDate), 1);
        BigDecimal price = car.getRentalPricePerDay().multiply(BigDecimal.valueOf(days));

        CarBooking newBooking = new CarBooking(
                UUID.randomUUID(),
                user,
                car,
                startDate,
                endDate,
                price,
                BookingStatus.ACTIVE,
                LocalDateTime.now()
        );

        carBookingDao.saveBooking(newBooking);
        return newBooking;
    }

    public void deleteBooking(UUID id) {
        carBookingDao.deleteBooking(id);
    }

    public CarBooking[] getUserBookings(UUID id) {
        CarBooking[] all = carBookingDao.getBookings();

        int count = 0;
        for (CarBooking booking : all) {
            if (booking != null && booking.getUser() != null && booking.getUser().getId().equals(id)) {
                count++;
            }
        }

        CarBooking[] result = new CarBooking[count];
        int index = 0;
        for (CarBooking booking : all) {
            if (booking != null && booking.getUser() != null && booking.getUser().getId().equals(id)) {
                result[index++] = booking;
            }
        }

        return result;
    }

    public CarBooking[] getAllBookings() {
        return carBookingDao.getBookings();
    }

    private boolean isCarBooked(UUID carId, CarBooking[] activeBookings) {
        if (activeBookings == null) {
            return false;
        }
        for (CarBooking booking : activeBookings) {
            if (booking != null
                    && booking.getBookingStatus() == BookingStatus.ACTIVE
                    && booking.getCar() != null
                    && booking.getCar().getId().equals(carId)) {
                return true;
            }
        }
        return false;
    }

    public Car[] getAvailableCars(Car[] allCars) {
        CarBooking[] bookings = carBookingDao.getBookings();

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