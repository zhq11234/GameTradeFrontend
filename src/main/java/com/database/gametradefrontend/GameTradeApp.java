package com.database.gametradefrontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GameTradeApp extends Application {

    @Override
    public void start(Stage primaryStage){
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameTradeApp.class.getResource("view/welcome.fxml")
            );

            Scene scene = new Scene(loader.load(), 400, 600);
            primaryStage.setTitle("GameTrade - 欢迎");
            primaryStage.setScene(scene);
//            primaryStage.setResizable(false);
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
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
                // 使用实际存在的API路径进行连接测试
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                        new java.net.URL("http://localhost:8080/api/users/check-username?username=test").openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);

                int responseCode = conn.getResponseCode();
                // 检查用户名API返回200或404都是正常的（用户名存在或不存在）
                if (responseCode == 200 || responseCode == 404) {
                    System.out.println("✅ 后端连接成功 (响应码: " + responseCode + ")");
                } else {
                    System.out.println("⚠️ 后端返回异常状态码: " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("❌ 后端连接失败: " + e.getMessage());
                javafx.application.Platform.runLater(() -> showErrorDialog("连接失败",
                        "无法连接到Spring Boot后端\n" +
                                "请确保后端服务已启动: http://localhost:8080\n" +
                                "错误: " + e.getMessage()));
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
        // 初始化Jackson模块
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.findAndRegisterModules();

        launch(args);
    }
}