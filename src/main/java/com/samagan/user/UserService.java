package com.samagan.user;

import java.util.List;
import java.util.UUID;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("UserDao cannot be null");
        }
        this.userDao = userDao;
    }

    public User getUserById(UUID id) {
        return userDao.findUserById(id);
    }

    public List<User> getAllUsers() {
        return userDao.getUsers();
    }
}