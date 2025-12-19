package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.service.UserService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // 初始化界面
        progressIndicator.setVisible(false);

        // 设置按钮事件
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());

        // 测试：自动填充（开发时使用）
        usernameField.setText("testuser");
        passwordField.setText("password123");
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("输入错误", "请输入用户名和密码");
            return;
        }

        // 禁用按钮，显示进度
        setFormDisabled(true);
        statusLabel.setText("正在登录...");

        // 创建并执行登录任务
        Task<Map<String, Object>> loginTask = userService.loginTask(username, password);

        // 绑定进度
        progressIndicator.progressProperty().bind(loginTask.progressProperty());
        statusLabel.textProperty().bind(loginTask.messageProperty());

        loginTask.setOnSucceeded(event -> {
            Map<String, Object> result = loginTask.getValue();
            boolean success = (boolean) result.get("success");

            if (success) {
                // 登录成功
                Platform.runLater(() -> {
                    statusLabel.setText("登录成功！");

                    // 获取用户信息
                    Map<String, Object> userMap = (Map<String, Object>) result.get("user");
                    String token = (String) result.get("token");

                    // 保存登录状态（简单实现）
                    UserSession.getInstance().setLoggedInUser(
                            (String) userMap.get("username"),
                            token
                    );

                    // 跳转到主界面
                    navigateToMain();
                });
            } else {
                // 登录失败
                Platform.runLater(() -> {
                    setFormDisabled(false);
                    String message = (String) result.get("message");
                    statusLabel.setText("登录失败: " + message);
                    showAlert("登录失败", message);
                });
            }
        });

        loginTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                setFormDisabled(false);
                Throwable exception = loginTask.getException();
                statusLabel.setText("登录失败");
                showAlert("网络错误", "无法连接到服务器: " + exception.getMessage());
                exception.printStackTrace();
            });
        });

        // 在新线程中执行
        new Thread(loginTask).start();
    }

    private void handleRegister() {
        // 打开注册窗口
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gametrade/frontend/view/register.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("注册新用户");
            stage.setScene(new Scene(root, 400, 400));
            stage.show();

            // 可选：关闭当前登录窗口
            // ((Stage) loginButton.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法打开注册窗口: " + e.getMessage());
        }
    }

    private void navigateToMain() {
        try {
            // 加载主界面
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gametrade/frontend/view/main.fxml")
            );
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setTitle("GameTrade - 游戏交易平台");
            mainStage.setScene(new Scene(root, 1024, 768));
            mainStage.show();

            // 关闭登录窗口
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法加载主界面: " + e.getMessage());
        }
    }

    private void setFormDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
        registerButton.setDisable(disabled);
        progressIndicator.setVisible(disabled);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}