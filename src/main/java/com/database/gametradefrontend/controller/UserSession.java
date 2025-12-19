package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.model.User;

/**
 * 用户会话管理类（单例模式）
 * 用于管理当前登录用户的状态
 */
public class UserSession {
    private static volatile UserSession instance;
    private User currentUser;
    
    private UserSession() {
        // 私有构造函数，防止外部实例化
    }
    
    /**
     * 获取UserSession单例实例
     */
    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession();
                }
            }
        }
        return instance;
    }
    
    /**
     * 设置当前登录用户
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 检查用户是否已登录
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * 获取当前用户的用户名
     */
    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    /**
     * 获取当前用户的ID
     */
    public Long getUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * 清除会话（重置单例实例）
     */
    public static void clearSession() {
        instance = null;
    }
}
