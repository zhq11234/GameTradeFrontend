package com.database.gametradefrontend.service;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;

public class UserService {
    private final ApiClient apiClient;
    
    public UserService() {
        this.apiClient = new ApiClient();
    }
    
    /**
     * 用户登录
     */
    public User login(String account, String password) throws Exception {
        // 输入验证
        if (account == null || account.trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 预处理
        account = account.trim();
        password = password.trim();
        
        try {
            LoginRequest loginRequest = new LoginRequest(account, password);
            return apiClient.post("/users/login", loginRequest, User.class);
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 401) {
                // 账号或密码错误
                return null;
            }
            throw e;
        }
    }
    
    /**
     * 买家注册
     */
    public boolean registerBuyer(String account, String password, String contact, String nickname) throws Exception {
        // 输入验证
        if (account == null || account.trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("联系方式不能为空");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        
        // 预处理
        account = account.trim();
        password = password.trim();
        contact = contact.trim();
        nickname = nickname.trim();
        
        try {
            BuyerRegisterRequest registerRequest = new BuyerRegisterRequest(
                "buyer", account, password, contact, nickname
            );
            apiClient.post("/users/register/buyer", registerRequest, Void.class);
            return true;
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 409) {
                // 账号、联系方式或昵称已存在
                return false;
            }
            throw e;
        }
    }
    
    /**
     * 厂商注册
     */
    public boolean registerVendor(String account, String password, String contact, 
                                String companyName, String registeredAddress, String contactPerson) throws Exception {
        // 输入验证
        if (account == null || account.trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("联系方式不能为空");
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("企业名不能为空");
        }
        if (registeredAddress == null || registeredAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("注册地址不能为空");
        }
        if (contactPerson == null || contactPerson.trim().isEmpty()) {
            throw new IllegalArgumentException("联系人不能为空");
        }
        
        // 预处理
        account = account.trim();
        password = password.trim();
        contact = contact.trim();
        companyName = companyName.trim();
        registeredAddress = registeredAddress.trim();
        contactPerson = contactPerson.trim();
        
        try {
            VendorRegisterRequest registerRequest = new VendorRegisterRequest(
                "vendor", account, password, contact, companyName, registeredAddress, contactPerson
            );
            apiClient.post("/users/register/vendor", registerRequest, Void.class);
            return true;
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 409) {
                // 账号、联系方式或企业名已存在
                return false;
            }
            throw e;
        }
    }

    /**
     * 查询用户个人信息
     */
    public Object getPersonalInfo(String account) throws Exception {
        // 输入验证
        if (account == null || account.trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        
        // URL编码账号
        String encoded = java.net.URLEncoder.encode(account.trim(), java.nio.charset.StandardCharsets.UTF_8);
        
        try {
            return apiClient.get("/users/personal-info?account=" + encoded, Object.class);
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 404) {
                // 用户不存在
                return null;
            }
            throw e;
        }
    }
    
    /**
     * 修改用户个人信息
     */
    public boolean updatePersonalInfo(String account, java.util.Map<String, Object> personalInfo) throws Exception {
        // 输入验证
        if (account == null || account.trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (personalInfo == null || personalInfo.isEmpty()) {
            throw new IllegalArgumentException("个人信息不能为空");
        }
        
        // URL编码账号
        String encoded = java.net.URLEncoder.encode(account.trim(), java.nio.charset.StandardCharsets.UTF_8);
        
        try {
            apiClient.put("/users/personal-info?account=" + encoded, personalInfo, Void.class);
            return true;
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 404) {
                // 用户不存在
                return false;
            }
            throw e;
        }
    }
    
    /**
     * 内部登录请求类
     */
    private static class LoginRequest {
        private final String account;
        private final String password;
        
        public LoginRequest(String account, String password) {
            this.account = account;
            this.password = password;
        }
        
        public String getAccount() {
            return account;
        }
        
        public String getPassword() {
            return password;
        }
    }
    
    /**
     * 内部买家注册请求类
     */
    private static class BuyerRegisterRequest {
        private final String role;
        private final String account;
        private final String password;
        private final String contact;
        private final String nickname;
        
        public BuyerRegisterRequest(String role, String account, String password, String contact, String nickname) {
            this.role = role;
            this.account = account;
            this.password = password;
            this.contact = contact;
            this.nickname = nickname;
        }
        
        public String getRole() { return role; }
        public String getAccount() { return account; }
        public String getPassword() { return password; }
        public String getContact() { return contact; }
        public String getNickname() { return nickname; }
    }
    
    /**
     * 内部厂商注册请求类
     */
    private static class VendorRegisterRequest {
        private final String role;
        private final String account;
        private final String password;
        private final String contact;
        private final String companyName;
        private final String registeredAddress;
        private final String contactPerson;
        
        public VendorRegisterRequest(String role, String account, String password, String contact, 
                                   String companyName, String registeredAddress, String contactPerson) {
            this.role = role;
            this.account = account;
            this.password = password;
            this.contact = contact;
            this.companyName = companyName;
            this.registeredAddress = registeredAddress;
            this.contactPerson = contactPerson;
        }
        
        public String getRole() { return role; }
        public String getAccount() { return account; }
        public String getPassword() { return password; }
        public String getContact() { return contact; }
        public String getCompanyName() { return companyName; }
        public String getRegisteredAddress() { return registeredAddress; }
        public String getContactPerson() { return contactPerson; }
    }
}
