package com.database.gametradefrontend.service;

import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.client.ApiClient;

public class UserService {
    private final ApiClient apiClient;
    
    public UserService() {
        this.apiClient = new ApiClient();
    }
    
    /**
     * 用户登录
     */
    public User login(String username, String password) throws Exception {
        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            return apiClient.post("/users/login", loginRequest, User.class);
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 401) {
                // 用户名或密码错误
                return null;
            }
            throw e;
        }
    }
    
    /**
     * 用户注册
     */
    public boolean register(User user) throws Exception {
        try {
            apiClient.post("/users/register", user, Void.class);
            return true;
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 409) {
                // 用户名已存在
                return false;
            }
            throw e;
        }
    }
    
    /**
     * 检查用户名是否已存在
     */
    public boolean checkUsernameExists(String username) throws Exception {
        try {
            apiClient.get("/users/check-username?username=" + username, Boolean.class);
            return true;
        } catch (ApiClient.ApiException e) {
            if (e.getStatusCode() == 404) {
                // 用户名不存在
                return false;
            }
            throw e;
        }
    }
    
    /**
     * 内部登录请求类
     */
    private static class LoginRequest {
        private String username;
        private String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
    }
}
