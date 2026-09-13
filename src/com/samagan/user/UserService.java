package com.samagan.user;

import java.util.UUID;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUserById(UUID id) {
        return userDao.findUserById(id);
    }

    public User[] getAllUser() {
        return userDao.getUsers();
    }
}