package com.samagan.booking;

import com.samagan.car.Car;
import com.samagan.car.CarDao;
import com.samagan.user.User;
import com.samagan.user.UserDao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class CarBookingFileDataAccessService implements CarBookingDao {

    private static final int CAPACITY = 100;

    private final File file;
    private final UserDao userDao;
    private final CarDao carDao;

    public CarBookingFileDataAccessService(
            String filePath,
            UserDao userDao,
            CarDao carDao
    ) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
        if (userDao == null) {
            throw new IllegalArgumentException("UserDao cannot be null.");
        }
        if (carDao == null) {
            throw new IllegalArgumentException("CarDao cannot be null.");
        }

        this.file = new File(filePath);
        this.userDao = userDao;
        this.carDao = carDao;

        try {
            if (!file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not create bookings file: " + filePath, e);
        }
    }

    @Override
    public List<CarBooking> getBookings() {
        List<CarBooking> bookings = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    bookings.add(parseLine(line));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read bookings from file.", e);
        }

        return bookings;
    }

    @Override
    public CarBooking findBookingById(UUID bookingId) {
        if (bookingId == null) {
            return null;
        }
        return getBookings().stream()
                .filter(booking -> booking.getId().equals(bookingId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void saveBooking(CarBooking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }

        List<CarBooking> current = getBookings();
        if (current.size() >= CAPACITY) {
            throw new IllegalStateException("Booking storage capacity reached (" + CAPACITY + ").");
        }

        current.add(booking);
        writeAll(current);
    }

    @Override
    public void deleteBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null.");
        }

        List<CarBooking> bookings = getBookings();

        CarBooking targetBooking = bookings.stream()
                .filter(booking -> booking != null && bookingId.equals(booking.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Booking with ID " + bookingId + " not found."));

        targetBooking.setBookingStatus(BookingStatus.CANCELLED);
        writeAll(bookings);
    }

    private void writeAll(List<CarBooking> bookings) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            for (CarBooking booking : bookings) {
                if (booking == null) {
                    continue;
                }
                writer.println(
                        booking.getId() + "," +
                                booking.getUser().getId() + "," +
                                booking.getCar().getId() + "," +
                                booking.getStartDate() + "," +
                                booking.getEndDate() + "," +
                                booking.getPrice() + "," +
                                booking.getBookingStatus() + "," +
                                booking.getBookedAt()
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write bookings to file.", e);
        }
    }

    private CarBooking parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 8) {
            throw new IllegalStateException("Invalid booking record: " + line);
        }

        try {
            UUID bookingId = UUID.fromString(parts[0].trim());
            UUID userId = UUID.fromString(parts[1].trim());
            UUID carId = UUID.fromString(parts[2].trim());
            LocalDate startDate = LocalDate.parse(parts[3].trim());
            LocalDate endDate = LocalDate.parse(parts[4].trim());
            BigDecimal price = new BigDecimal(parts[5].trim());
            BookingStatus status = BookingStatus.valueOf(parts[6].trim());
            LocalDateTime bookedAt = LocalDateTime.parse(parts[7].trim());

            User user = userDao.findUserById(userId);
            if (user == null) {
                throw new IllegalStateException("User with ID " + userId + " not found.");
            }

            Car car = carDao.findCarById(carId);
            if (car == null) {
                throw new IllegalStateException("Car with ID " + carId + " not found.");
            }

            return new CarBooking(
                    bookingId,
                    user,
                    car,
                    startDate,
                    endDate,
                    price,
                    status,
                    bookedAt
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid booking data: " + line, e);
        }
    }
}