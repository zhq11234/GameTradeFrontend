package com.database.gametradefrontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    
    // 默认构造函数（Jackson需要）
    public User() {}
    
    // 带参数的构造函数
    public User(Long id, String username, String password, String email, String phone, String nickname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.nickname = nickname;
    }
    
    // Getters and Setters
    @JsonProperty("id")
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @JsonProperty("nickname")
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
