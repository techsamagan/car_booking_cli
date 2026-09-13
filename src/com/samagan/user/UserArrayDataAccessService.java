package com.samagan.user;

import java.util.UUID;

public class UserArrayDataAccessService implements UserDao{

    private static final User[] USERS = new User[]{
        new User(UUID.fromString("8ca51d2b-aa40-42cb-b73a-46329e3423b5"), "James"),
        new User(UUID.fromString("b10d126a-3608-4980-9819-ac1b0587e471"), "Jamila")
    };


    @Override
    public User[] getUsers() {
        return USERS;
    }

    @Override
    public User findUserById(UUID userId) {
        for (User user : USERS){
            if (user != null && user.getId().equals(userId)){
                return user;
            }
        }
        return null;
    }
}
