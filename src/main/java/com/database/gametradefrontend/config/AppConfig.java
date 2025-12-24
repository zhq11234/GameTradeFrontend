package com.database.gametradefrontend.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用配置管理类
 */
public class AppConfig {
    private static final Properties properties = new Properties();
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("配置文件 config.properties 未找到，使用默认配置");
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private static void setDefaultProperties() {
        properties.setProperty("api.base.url", "http://localhost:8080/api");
        properties.setProperty("api.timeout.connect", "5000");
        properties.setProperty("api.timeout.read", "5000");
        properties.setProperty("api.retry.maxAttempts", "3");
        properties.setProperty("api.retry.delay", "1000");
        properties.setProperty("session.timeout", "1800000");
        properties.setProperty("session.checkInterval", "60000");
        properties.setProperty("log.level", "INFO");
    }
    
    public static String getApiBaseUrl() {
        return properties.getProperty("api.base.url");
    }
    
    public static int getApiConnectTimeout() {
        return Integer.parseInt(properties.getProperty("api.timeout.connect"));
    }
    
    public static int getApiReadTimeout() {
        return Integer.parseInt(properties.getProperty("api.timeout.read"));
    }
    
    public static int getApiMaxRetryAttempts() {
        return Integer.parseInt(properties.getProperty("api.retry.maxAttempts"));
    }
    
    public static int getApiRetryDelay() {
        return Integer.parseInt(properties.getProperty("api.retry.delay"));
    }
    
    public static long getSessionTimeout() {
        return Long.parseLong(properties.getProperty("session.timeout"));
    }
    
    public static long getSessionCheckInterval() {
        return Long.parseLong(properties.getProperty("session.checkInterval"));
    }
    
    public static String getLogLevel() {
        return properties.getProperty("log.level");
    }
    
    public static boolean isLogFileEnabled() {
        return Boolean.parseBoolean(properties.getProperty("log.file.enabled", "false"));
    }
    
    public static String getLogFilePath() {
        return properties.getProperty("log.file.path", "logs/app.log");
    }
}
