package com.samagan.user;

import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFakerDataAccessService implements UserDao {

    private static final int USER_COUNT = 20;

    private final List<User> users;

    public UserFakerDataAccessService() {
        this.users = generateUsers();
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

    private static List<User> generateUsers() {
        Faker faker = new Faker();
        List<User> generated = new ArrayList<>(USER_COUNT);

        for (int i = 0; i < USER_COUNT; i++) {
            generated.add(new User(UUID.randomUUID(), faker.name().fullName()));
        }

        return List.copyOf(generated);
    }
}
