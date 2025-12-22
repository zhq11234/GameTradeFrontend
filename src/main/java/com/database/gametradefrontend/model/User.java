package com.database.gametradefrontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String account;
    private String role;
    private String password;  // 用于注册和登录请求，但不会被序列化到响应中
    private String contact;
    private String registerTime;
    
    // 默认构造函数（Jackson需要）
    public User() {}
    
    // 带参数的构造函数（用于注册）
    public User(String account, String role, String password, String contact) {
        this.account = account;
        this.role = role;
        this.password = password;
        this.contact = contact;
    }
    
    // Getters and Setters
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    @JsonProperty("role")
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    @JsonProperty("password")
    @com.fasterxml.jackson.annotation.JsonIgnore  // 序列化时忽略密码，防止泄露
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @JsonProperty("contact")
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    @JsonProperty("registerTime")
    public String getRegisterTime() {
        return registerTime;
    }
    
    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "account='" + account + '\'' +
                ", role='" + role + '\'' +
                ", contact='" + contact + '\'' +
                ", registerTime='" + registerTime + '\'' +
                '}';
    }
}
