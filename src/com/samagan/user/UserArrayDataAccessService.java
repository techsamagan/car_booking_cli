package com.samagan.user;

import java.util.List;
import java.util.UUID;

public class UserArrayDataAccessService implements UserDao {

    private static final List<User> USERS = List.of(
            new User(UUID.fromString("8ca51d2b-aa40-42cb-b73a-46329e3423b5"), "James"),
            new User(UUID.fromString("b10d126a-3608-4980-9819-ac1b0587e471"), "Jamila")
    );

    @Override
    public List<User> getUsers() {
        return USERS;
    }

    @Override
    public User findUserById(UUID userId) {
        if (userId == null) {
            return null;
        }

        for (User user : USERS) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }
}