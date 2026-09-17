package com.samagan;

import com.samagan.booking.CarBooking;
import com.samagan.booking.CarBookingArrayDataAccessService;
import com.samagan.booking.CarBookingDao;
import com.samagan.booking.CarBookingFileDataAccessService;
import com.samagan.booking.CarBookingService;
import com.samagan.car.Car;
import com.samagan.car.CarArrayDataAccessService;
import com.samagan.car.CarDao;
import com.samagan.car.CarService;
import com.samagan.user.User;
import com.samagan.user.UserArrayDataAccessService;
import com.samagan.user.UserDao;
import com.samagan.user.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        System.out.println("Car Booking CLI");

        boolean keepRunning = true;
        Scanner scanner = new Scanner(System.in);

        // Wiring & Dependency Injection
        UserDao userDao = new UserArrayDataAccessService();
        CarDao carDao = new CarArrayDataAccessService();

        // Easily swap DAO implementations:
        CarBookingDao carBookingDao = new CarBookingArrayDataAccessService();
        // CarBookingDao carBookingDao = new CarBookingFileDataAccessService("bookings.csv", userDao, carDao);

        UserService userService = new UserService(userDao);
        CarService carService = new CarService(carDao);
        CarBookingService bookingService = new CarBookingService(carBookingDao, userService, carService);

        String menu = """
        
        1 - Book a car
        2 - Delete booking
        3 - View user bookings
        4 - View all bookings
        5 - View available cars
        6 - View electric cars
        7 - View all users
        8 - Exit
        """;

        while (keepRunning) {
            System.out.println(menu);
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    try {
                        System.out.print("Enter userId: ");
                        UUID userId = UUID.fromString(scanner.nextLine().trim());

                        System.out.print("Enter carId: ");
                        UUID carId = UUID.fromString(scanner.nextLine().trim());

                        System.out.print("Enter start date (yyyy-MM-dd): ");
                        LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());

                        System.out.print("Enter end date (yyyy-MM-dd): ");
                        LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());

                        CarBooking booking = bookingService.bookCar(userId, carId, startDate, endDate);
                        System.out.println("Booking successful! Booking ID: " + booking.getId());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Input error: " + e.getMessage());
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Please use yyyy-MM-dd.");
                    } catch (IllegalStateException e) {
                        System.out.println("Booking failed: " + e.getMessage());
                    }
                    break;

                case "2":
                    System.out.print("Enter booking ID to cancel: ");
                    String bookingIdInput = scanner.nextLine().trim();
                    try {
                        UUID bookingId = UUID.fromString(bookingIdInput);
                        bookingService.deleteBooking(bookingId);
                        System.out.println("Booking " + bookingId + " has been cancelled successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid UUID format: " + bookingIdInput);
                    } catch (IllegalStateException e) {
                        System.out.println("Cancellation failed: " + e.getMessage());
                    }
                    break;

                case "3":
                    System.out.print("Enter user ID: ");
                    String userIdInput = scanner.nextLine().trim();
                    try {
                        UUID userBookingId = UUID.fromString(userIdInput);
                        List<CarBooking> userBookings = bookingService.getUserBookings(userBookingId);

                        if (userBookings.isEmpty()) {
                            System.out.println("No bookings found for user: " + userBookingId);
                        } else {
                            System.out.println("Bookings for user " + userBookingId + ":");
                            for (CarBooking booking : userBookings) {
                                if (booking != null) {
                                    System.out.println(booking);
                                }
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid UUID format: " + userIdInput);
                    }
                    break;

                case "4":
                    System.out.println("All bookings:");
                    List<CarBooking> bookings = bookingService.getAllBookings();

                    if (bookings.isEmpty()) {
                        System.out.println("No bookings found.");
                    } else {
                        for (CarBooking booking : bookings) {
                            if (booking != null) {
                                System.out.println(booking);
                            }
                        }
                    }
                    break;

                case "5":
                    List<Car> allCars = carService.getAllCars();
                    List<Car> availableCars = bookingService.getAvailableCars(allCars);

                    if (availableCars.isEmpty()) {
                        System.out.println("No cars available at the moment.");
                    } else {
                        System.out.println("Available cars:");
                        for (Car car : availableCars) {
                            System.out.println(car);
                        }
                    }
                    break;

                case "6":
                    List<Car> all = carService.getAllCars();
                    List<Car> available = bookingService.getAvailableCars(all);

                    List<Car> availableElectric = available.stream()
                            .filter(car -> car != null && car.isElectric())
                            .toList();

                    if (availableElectric.isEmpty()) {
                        System.out.println("No available electric cars found.");
                    } else {
                        System.out.println("Available electric cars:");
                        for (Car car : availableElectric) {
                            System.out.println(car);
                        }
                    }
                    break;

                case "7":
                    System.out.println("All users:");
                    List<User> users = userService.getAllUsers();
                    if (users.isEmpty()) {
                        System.out.println("No users registered.");
                    } else {
                        for (User user : users) {
                            if (user != null) {
                                System.out.println(user);
                            }
                        }
                    }
                    break;

                case "8":
                    keepRunning = false;
                    System.out.println("Exiting application...");
                    break;

                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 8.");
                    break;
            }
        }

        scanner.close();
    }
}