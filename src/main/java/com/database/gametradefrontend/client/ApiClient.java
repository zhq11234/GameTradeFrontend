package com.database.gametradefrontend.client;

import com.database.gametradefrontend.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 优化的API客户端类 - 封装HTTP请求逻辑
 * 支持配置外部化和重试机制
 */
public class ApiClient {
    private static final String BASE_URL = AppConfig.getApiBaseUrl();
    private static final int CONNECT_TIMEOUT = AppConfig.getApiConnectTimeout();
    private static final int READ_TIMEOUT = AppConfig.getApiReadTimeout();
    private static final int MAX_RETRY_ATTEMPTS = AppConfig.getApiMaxRetryAttempts();
    private static final int RETRY_DELAY = AppConfig.getApiRetryDelay();
    
    private final ObjectMapper objectMapper;
    
    public ApiClient() {
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
    
    /**
     * 发送GET请求（带重试机制）
     */
    public <T> T get(String endpoint, Class<T> responseType) throws Exception {
        return sendRequestWithRetry("GET", endpoint, null, responseType);
    }
    
    /**
     * 发送POST请求（带重试机制）
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        return sendRequestWithRetry("POST", endpoint, requestBody, responseType);
    }
    
    /**
     * 发送PUT请求（带重试机制）
     */
    public <T> T put(String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        return sendRequestWithRetry("PUT", endpoint, requestBody, responseType);
    }
    
    /**
     * 发送DELETE请求（带重试机制）
     */
    public boolean delete(String endpoint) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(BASE_URL + endpoint);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    
                    int responseCode = connection.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_OK || 
                           responseCode == HttpURLConnection.HTTP_NO_CONTENT;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } catch (Exception e) {
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                Thread.sleep(RETRY_DELAY * attempt);
            }
        }
        return false;
    }
    
    /**
     * 带重试机制的请求发送方法
     */
    private <T> T sendRequestWithRetry(String method, String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return sendRequest(method, endpoint, requestBody, responseType);
            } catch (Exception e) {
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                Thread.sleep(RETRY_DELAY * attempt);
            }
        }
        return null;
    }
    
    /**
     * 通用的HTTP请求发送方法
     */
    private <T> T sendRequest(String method, String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        HttpURLConnection connection = null;
        try {
            String fullUrl = BASE_URL + endpoint;
            System.out.println("API Request: " + method + " " + fullUrl);
            
            if (requestBody != null) {
                System.out.println("Request Body: " + objectMapper.writeValueAsString(requestBody));
            }
            
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            // 如果有请求体，发送请求体
            if (requestBody != null && ("POST".equals(method) || "PUT".equals(method))) {
                connection.setDoOutput(true);
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBodyJson.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            // 获取响应
            int responseCode = connection.getResponseCode();
            
            if (responseCode >= 200 && responseCode < 300) {
                // 成功响应
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    System.out.println("API Response: " + responseCode + " - Success");
                    if (!response.toString().isEmpty()) {
                        System.out.println("Response Body: " + response.toString());
                    }
                    
                    if (responseType == Void.class) {
                        return null;
                    }
                    return objectMapper.readValue(response.toString(), responseType);
                }
            } else {
                // 错误响应
                String errorMessage = getErrorMessage(connection);
                System.out.println("API Response: " + responseCode + " - Error: " + errorMessage);
                throw new ApiException("HTTP " + responseCode + ": " + errorMessage, responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * 获取错误信息
     */
    private String getErrorMessage(HttpURLConnection connection) throws IOException {
        if (connection == null) {
            return "Unknown error";
        }
        
        // 先尝试读取 error stream，如果为 null 则回退到 response message
        try {
            java.io.InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    String result = errorResponse.toString();
                    return result.isEmpty() ? connection.getResponseMessage() : result;
                }
            } else {
                String msg = connection.getResponseMessage();
                return msg != null ? msg : "No error message";
            }
        } catch (Exception e) {
            return connection.getResponseMessage();
        }
    }
    
    /**
     * API异常类
     */
    public static class ApiException extends RuntimeException {
        private final int statusCode;
        
        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}
