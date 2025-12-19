package com.database.gametradefrontend.service;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

public class UserService {

    // 获取所有用户（异步）
    public Task<List<User>> loadUsersTask() {
        return new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                updateMessage("正在加载用户数据...");
                Thread.sleep(500); // 模拟延迟

                return ApiClient.get("/users", new TypeReference<List<User>>() {});
            }
        };
    }

    // 登录（异步）
    public Task<Map<String, Object>> loginTask(String username, String password) {
        return new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                updateMessage("正在验证登录信息...");
                Thread.sleep(1000); // 模拟网络延迟

                Map<String, String> credentials = Map.of(
                        "username", username,
                        "password", password
                );

                return ApiClient.post("/users/login", credentials,
                        new TypeReference<Map<String, Object>>() {});
            }
        };
    }

    // 注册（异步）
    public Task<User> registerTask(User user) {
        return new Task<>() {
            @Override
            protected User call() throws Exception {
                updateMessage("正在创建用户...");
                Thread.sleep(1000);

                return ApiClient.post("/users", user, User.class);
            }
        };
    }

    // 更新用户信息
    public Task<User> updateUserTask(Long userId, User user) {
        return new Task<>() {
            @Override
            protected User call() throws Exception {
                updateMessage("正在更新用户信息...");
                Thread.sleep(500);

                return ApiClient.put("/users/" + userId, user, User.class);
            }
        };
    }

    // 删除用户
    public Task<Void> deleteUserTask(Long userId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("正在删除用户...");
                Thread.sleep(500);

                ApiClient.delete("/users/" + userId);
                return null;
            }
        };
    }
}