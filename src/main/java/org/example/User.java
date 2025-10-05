package org.example;

import lombok.Data;

@Data
public class User {
    private static final User INSTANCE = new User();

    private String username;
    private Long id;
    private double difus;
    private boolean isAuthorized;

    private User() {}

    public static User user() {
        return INSTANCE;
    }
}