package org.example;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getDifus() {
        return difus;
    }

    public void setDifus(double difus) {
        this.difus = difus;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }
}