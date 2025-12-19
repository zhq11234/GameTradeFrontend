package com.database.gametradefrontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.service.UserService;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    private final UserService userService;
    
    public LoginController() {
        this.userService = new UserService();
    }
    
    @FXML
    public void initialize() {
        // 初始化控制器
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // 为登录按钮添加样式变化效果
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #5a6fd8 0%, #6a42a0 100%); " +
                           "-fx-background-radius: 8; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-font-size: 14; -fx-cursor: hand;"));
        
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%); " +
                           "-fx-background-radius: 8; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-font-size: 14; -fx-cursor: hand;"));
        
        // 输入框获得焦点时的样式变化
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                                     "-fx-border-color: #667eea; -fx-padding: 12; -fx-font-size: 14;");
            } else {
                usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                                     "-fx-border-color: #e0e0e0; -fx-padding: 12; -fx-font-size: 14;");
            }
        });
        
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                                     "-fx-border-color: #667eea; -fx-padding: 12; -fx-font-size: 14;");
            } else {
                passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                                     "-fx-border-color: #e0e0e0; -fx-padding: 12; -fx-font-size: 14;");
            }
        });
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // 验证输入
        if (username.isEmpty() || password.isEmpty()) {
            showError("请输入用户名和密码");
            return;
        }
        
        // 禁用登录按钮，显示加载状态
        loginButton.setDisable(true);
        loginButton.setText("登录中...");
        
        // 在新线程中执行登录操作
        new Thread(() -> {
            try {
                // 调用用户服务进行登录验证
                User user = userService.login(username, password);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (user != null) {
                        // 登录成功
                        onLoginSuccess(user);
                    } else {
                        // 登录失败
                        onLoginFailure("用户名或密码错误");
                    }
                });
            } catch (Exception e) {
                // 网络错误或其他异常
                javafx.application.Platform.runLater(() -> onLoginFailure("登录失败: " + e.getMessage()));
            }
        }).start();
    }
    
    private void onLoginSuccess(User user) {
        // 保存用户会话
        UserSession.getInstance().setCurrentUser(user);
        
        // 重置UI状态
        resetLoginButton();
        
        // 显示成功消息（在实际应用中，这里应该跳转到主界面）
        showSuccess("登录成功！欢迎 " + user.getUsername());
        
        // TODO: 跳转到主界面
        System.out.println("登录成功，用户: " + user.getUsername());
    }
    
    private void onLoginFailure(String errorMessage) {
        resetLoginButton();
        showError(errorMessage);
    }
    
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("登录");
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#ff4444"));
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#00C851"));
        errorLabel.setVisible(true);
    }
}
