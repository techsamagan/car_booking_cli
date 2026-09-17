package com.samagan.booking;

import com.samagan.car.Car;
import com.samagan.car.CarService;
import com.samagan.user.User;
import com.samagan.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.max;

public class CarBookingService {
    private final CarBookingDao carBookingDao;
    private final UserService userService;
    private final CarService carService;

    public CarBookingService(CarBookingDao carBookingDao, UserService userService, CarService carService) {
        if (carBookingDao == null) {
            throw new IllegalArgumentException("CarBookingDao cannot be null.");
        }
        if (userService == null) {
            throw new IllegalArgumentException("UserService cannot be null.");
        }
        if (carService == null) {
            throw new IllegalArgumentException("CarService cannot be null.");
        }
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

        List<CarBooking> allBookings = carBookingDao.getBookings();
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
        if (id == null) {
            throw new IllegalArgumentException("Booking ID cannot be null.");
        }
        carBookingDao.deleteBooking(id);
    }

    public List<CarBooking> getUserBookings(UUID id) {
        if (id == null) {
            return List.of();
        }

        List<CarBooking> bookings = carBookingDao.getBookings();
        if (bookings == null) {
            return List.of();
        }

        return bookings.stream()
                .filter(booking -> booking != null
                        && booking.getUser() != null
                        && id.equals(booking.getUser().getId()))
                .toList();
    }


    public List<CarBooking> getAllBookings() {
        return carBookingDao.getBookings();
    }

    private boolean isCarBooked(UUID carId, List<CarBooking> activeBookings) {
        if (activeBookings == null || carId == null) {
            return false;
        }

        return activeBookings.stream()
                .anyMatch(booking -> booking != null
                        && booking.getBookingStatus() == BookingStatus.ACTIVE
                        && booking.getCar() != null
                        && carId.equals(booking.getCar().getId()));
    }

    public List<Car> getAvailableCars(List<Car> allCars) {
        if (allCars == null) {
            return List.of();
        }

        List<CarBooking> bookings = carBookingDao.getBookings();
        return allCars.stream()
                .filter(car -> car != null && !isCarBooked(car.getId(), bookings))
                .toList();
    }
}