package com.samagan.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserArrayDataAccessService implements UserDao {

    private static final String USERS_RESOURCE = "users.csv";

    private final List<User> users;

    public UserArrayDataAccessService() {
        this.users = loadUsers();
    }

    @Override
    public List<User> getUsers() {
        return List.copyOf(users);
    }

    @Override
    public User findUserById(UUID userId) {
        if (userId == null) {
            return null;
        }

        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);

    }

    /**
     * Reads the seed users from the classpath rather than a path relative to the
     * working directory, so the data is found no matter where the app is launched
     * from and keeps working once packaged into a jar.
     */
    private static List<User> loadUsers() {
        InputStream in = UserArrayDataAccessService.class
                .getClassLoader()
                .getResourceAsStream(USERS_RESOURCE);

        if (in == null) {
            throw new IllegalStateException("Could not find " + USERS_RESOURCE + " on the classpath.");
        }

        List<User> loaded = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    loaded.add(parseLine(line));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + USERS_RESOURCE + " from the classpath.", e);
        }

        return List.copyOf(loaded);
    }

    private static User parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            throw new IllegalStateException("Invalid user record: " + line);
        }

        try {
            return new User(UUID.fromString(parts[0].trim()), parts[1].trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid user data: " + line, e);
        }
    }
}
