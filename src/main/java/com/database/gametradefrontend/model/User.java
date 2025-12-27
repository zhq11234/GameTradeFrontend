package com.database.gametradefrontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String account;
    private String role;
    private String password;  // 用于注册和登录请求，但不会被序列化到响应中
    private String contact;
    private String registerTime;
    private String company;   // 企业名称（厂商用户专用）
    private String address;   // 注册地址（厂商用户专用）
    private String contactPerson;
    // 联系人（厂商用户专用）
    
    // 买家用户专用字段
    private String nickname;      // 昵称
    
    // 默认构造函数（Jackson需要）
    public User() {}
    
    // 带参数的构造函数（用于注册）
    public User(String account, String role, String password, String contact,String nickname) {
        this.account = account;
        this.role = role;
        this.password = password;
        this.contact = contact;
        this.nickname = nickname;
    }
    
    // 带完整参数的构造函数（厂商用户专用）
    public User(String account, String role, String password, String contact, String company, String address) {
        this.account = account;
        this.role = role;
        this.password = password;
        this.contact = contact;
        this.company = company;
        this.address = address;
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
    
    @JsonProperty("company")
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    @JsonProperty("address")
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPerson() {
        return contactPerson;
    }
    
    // 买家用户字段的Getters和Setters
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
                "account='" + account + '\'' +
                ", role='" + role + '\'' +
                ", contact='" + contact + '\'' +
                ", registerTime='" + registerTime + '\'' +
                ", company='" + company + '\'' +
                ", address='" + address + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
