package com.database.gametradefrontend.controller;

public class UserSession {

    private static UserSession instance;

    private String username;
    private String token;
    private boolean loggedIn = false;

    // 私有构造器
    private UserSession() {}

    // 单例模式
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // getter/setter
    public String getUsername() { return username; }
    public String getToken() { return token; }
    public boolean isLoggedIn() { return loggedIn; }

    public void setLoggedInUser(String username, String token) {
        this.username = username;
        this.token = token;
        this.loggedIn = true;
    }

    public void clearSession() {
        this.username = null;
        this.token = null;
        this.loggedIn = false;
    }
}