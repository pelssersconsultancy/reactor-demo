package com.pelssersconsultancy.reactordemo;

import java.util.function.Predicate;

public class UserPredicates {

    private  UserPredicates(){}


    public static Predicate<User> hasName(String name) {
        return user -> name.equals(user.getName());
    }
}
