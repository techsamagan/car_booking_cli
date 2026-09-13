package com.samagan.user;

import java.util.UUID;

public class UserService {
    private final UserDao userDao = new UserDao();

    public User getUserById(UUID id){
        return userDao.getUserById(id);
    }

    public User[] getAllUser(){
        return userDao.getAllUser();
    }
}
