package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.model.User;

/**
 * 用户会话管理类（单例模式）
 * 用于管理当前登录用户的状态
 */
public class UserSession {
    private static volatile UserSession instance;
    private User currentUser;
    private long loginTime;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30分钟超时
    
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
        this.loginTime = System.currentTimeMillis();
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 检查用户是否已登录（包含会话过期检查）
     */
    public boolean isLoggedIn() {
        return currentUser != null && isSessionValid();
    }
    
    /**
     * 检查会话是否有效（未过期）
     */
    public boolean isSessionValid() {
        if (currentUser == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - loginTime) < SESSION_TIMEOUT;
    }
    
    /**
     * 获取会话剩余时间（毫秒）
     */
    public long getRemainingSessionTime() {
        if (!isSessionValid()) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        return SESSION_TIMEOUT - (currentTime - loginTime);
    }
    
    /**
     * 获取当前用户的账号
     */
    public String getAccount() {
        return isSessionValid() ? currentUser.getAccount() : null;
    }
    
    /**
     * 获取当前用户的角色
     */
    public String getRole() {
        return isSessionValid() ? currentUser.getRole() : null;
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        this.currentUser = null;
        this.loginTime = 0;
    }
    
    /**
     * 清除会话（重置单例实例）
     */
    public static void clearSession() {
        instance = null;
    }
    
    /**
     * 刷新会话时间（延长会话有效期）
     */
    public void refreshSession() {
        if (currentUser != null) {
            this.loginTime = System.currentTimeMillis();
        }
    }
}
