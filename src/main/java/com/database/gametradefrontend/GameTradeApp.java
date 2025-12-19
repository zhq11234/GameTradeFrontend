package com.database.gametradefrontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GameTradeApp extends Application {

    @Override
    public void start(Stage primaryStage){
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameTradeApp.class.getResource("view/login.fxml")
            );

            Scene scene = new Scene(loader.load(), 400, 300);

            primaryStage.setTitle("GameTrade - 登录");
            primaryStage.setScene(scene);
            primaryStage.show();

            // 测试后端连接
            testBackendConnection();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("启动失败", e.getMessage());
        }
    }

    private void testBackendConnection() {
        // 在新线程中测试后端连接
        new Thread(() -> {
            try {
                // 简单的连接测试
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                        new java.net.URL("http://localhost:8080/api/users").openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    System.out.println("✅ 后端连接成功");
                } else {
                    System.out.println("⚠️ 后端返回: " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("❌ 后端连接失败: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
                    showErrorDialog("连接失败",
                            "无法连接到Spring Boot后端\n" +
                                    "请确保后端服务已启动: http://localhost:8080\n" +
                                    "错误: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // 初始化Jackson
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.findAndRegisterModules();

        launch(args);
    }

}