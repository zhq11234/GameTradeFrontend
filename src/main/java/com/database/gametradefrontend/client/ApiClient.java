package com.database.gametradefrontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * API客户端类 - 封装HTTP请求逻辑
 * 为服务层提供统一的API调用接口
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final ObjectMapper objectMapper;
    
    public ApiClient() {
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
    
    /**
     * 发送GET请求
     */
    public <T> T get(String endpoint, Class<T> responseType) throws Exception {
        return sendRequest("GET", endpoint, null, responseType);
    }
    
    /**
     * 发送POST请求
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        return sendRequest("POST", endpoint, requestBody, responseType);
    }
    
    /**
     * 发送PUT请求
     */
    public <T> T put(String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        return sendRequest("PUT", endpoint, requestBody, responseType);
    }
    
    /**
     * 发送DELETE请求
     */
    public boolean delete(String endpoint) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK || 
                   responseCode == HttpURLConnection.HTTP_NO_CONTENT;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * 通用的HTTP请求发送方法
     */
    private <T> T sendRequest(String method, String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
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
                    
                    if (responseType == Void.class) {
                        return null;
                    }
                    return objectMapper.readValue(response.toString(), responseType);
                }
            } else {
                // 错误响应
                String errorMessage = getErrorMessage(connection);
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
        try {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
                return errorResponse.toString();
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
