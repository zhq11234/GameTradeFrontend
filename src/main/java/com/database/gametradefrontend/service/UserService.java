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
    public User login(String username, String password) throws Exception {
        // 输入验证
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 预处理
        username = username.trim();
        password = password.trim();
        
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
        // 输入验证
        if (user == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 预处理
        user.setUsername(user.getUsername().trim());
        user.setPassword(user.getPassword().trim());
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim());
        }
        
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
        // 输入验证
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        // URL编码用户名
        String encoded = java.net.URLEncoder.encode(username.trim(), java.nio.charset.StandardCharsets.UTF_8);
        
        try {
            Boolean exists = apiClient.get("/users/check-username?username=" + encoded, Boolean.class);
            // 返回实际的布尔值（防止null造成的问题）
            return Boolean.TRUE.equals(exists);
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
        private final String username;
        private final String password;
        
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
