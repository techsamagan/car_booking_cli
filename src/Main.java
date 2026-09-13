import com.samagan.booking.CarBooking;
import com.samagan.booking.CarBookingService;
import com.samagan.car.Car;
import com.samagan.car.CarService;
import com.samagan.user.User;
import com.samagan.user.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        System.out.println("Car Booking CLI");

        boolean keepRunning = true;
        Scanner scanner = new Scanner(System.in);

        UserService userService = new UserService();
        CarService carService = new CarService();
        CarBookingService bookingService = new CarBookingService();

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
            switch (scanner.nextLine().trim()) {
                case "1":
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
                                CarBooking[] userBookings = bookingService.getUserBookings(userBookingId);

                                if (userBookings.length == 0) {
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
                                System.out.println("All bookings: ");
                                CarBooking[] bookings = bookingService.getAllBooking();
                                boolean hasBookings = false;

                                for (CarBooking booking : bookings) {
                                    if (booking != null) {
                                        System.out.println(booking);
                                        hasBookings = true;
                                    }
                                }

                                if (!hasBookings) {
                                    System.out.println("No bookings found.");
                                }
                                break;

                                case "5":
                                    Car[] allCars = carService.getAllCars();
                                    Car[] availableCars = bookingService.getAvailableCars(allCars);

                                    if (availableCars.length == 0) {
                                        System.out.println("No cars available at the moment.");
                                    } else {
                                        System.out.println("Available cars:");
                                        for (Car car : availableCars) {
                                            System.out.println(car);
                                        }
                                    }
                                    break;

                                    case "6":
                                        System.out.println("All Electric cars: ");
                                        Car[] electricCars = carService.getAllElectricCars();
                                        if (electricCars.length == 0) {
                                            System.out.println("No electric cars found.");
                                        } else {
                                            for (Car car : electricCars) {
                                                if (car != null) {
                                                    System.out.println(car);
                                                }
                                            }
                                        }
                                        break;

                                        case "7":
                                            System.out.println("All users: ");
                                            User[] users = userService.getAllUser();
                                            for (User user : users) {
                                                if (user != null) {
                                                    System.out.println(user);
                                                }
                                            }
                                            break;

                                            case "8":
                                                keepRunning = false;
                                                break;
            }
        }

        scanner.close();

    }
}