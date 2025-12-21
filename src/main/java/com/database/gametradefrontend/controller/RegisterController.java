package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RegisterController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button loginLink;
    
    @FXML
    private Label errorLabel;
    
    private final UserService userService;
    
    public RegisterController() {
        this.userService = new UserService();
    }
    
    @FXML
    public void initialize() {
        // 初始化控制器
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // 为注册按钮添加样式变化效果 - 使用CSS类
        registerButton.setOnMouseEntered(e -> registerButton.getStyleClass().add("register-button-hover"));
        registerButton.setOnMouseExited(e -> registerButton.getStyleClass().remove("register-button-hover"));
        
        // 输入框获得焦点时的样式变化 - 使用CSS类
        setupInputFieldFocus(usernameField);
        setupInputFieldFocus(passwordField);
        setupInputFieldFocus(confirmPasswordField);
        setupInputFieldFocus(emailField);
    }
    
    private void setupInputFieldFocus(TextField field) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.getStyleClass().add("modern-input-focused");
            } else {
                field.getStyleClass().remove("modern-input-focused");
            }
        });
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();
        
        // 验证输入
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("请填写所有必填字段");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            return;
        }
        
        if (password.length() < 6) {
            showError("密码长度不能少于6位");
            return;
        }
        
        if (email.isEmpty()) {
            showError("请输入邮箱地址");
            return;
        }
        
        if (!isValidEmail(email)) {
            showError("请输入有效的邮箱地址");
            return;
        }
        
        // 禁用注册按钮，显示加载状态
        registerButton.setDisable(true);
        registerButton.setText("注册中...");
        
        // 在新线程中执行注册操作
        new Thread(() -> {
            try {
                // 创建用户对象（包含密码用于注册）
                User user = new User(null, username, password, email, null, username);
                
                // 调用用户服务进行注册
                boolean success = userService.register(user);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        // 注册成功
                        onRegisterSuccess();
                    } else {
                        // 注册失败
                        onRegisterFailure("用户名或邮箱已存在");
                    }
                });
            } catch (Exception e) {
                // 网络错误或其他异常
                javafx.application.Platform.runLater(() -> onRegisterFailure("注册失败: " + e.getMessage()));
            }
        }).start();
    }
    
    private boolean isValidEmail(String email) {
        // 简单的邮箱验证
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private void onRegisterSuccess() {
        // 重置UI状态
        resetRegisterButton();
        
        // 显示成功消息
        showSuccess("注册成功！请返回登录页面登录");
        
        // 清空输入框
        clearFields();
        
        System.out.println("注册成功");
    }
    
    private void onRegisterFailure(String errorMessage) {
        resetRegisterButton();
        showError(errorMessage);
    }
    
    private void resetRegisterButton() {
        registerButton.setDisable(false);
        registerButton.setText("注册");
    }
    
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
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
    
    @FXML
    private void handleBack() {
        try {
            // 加载欢迎界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/welcome.fxml"));
            Parent welcomeRoot = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(welcomeRoot, 1000, 800));
            stage.setTitle("GameTrade - 欢迎");
        } catch (Exception e) {
            showErrorDialog("界面切换失败", e.getMessage());
        }
    }
    
    @FXML
    private void handleLoginLink() {
        try {
            // 加载登录界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/login.fxml"));
            Parent loginRoot = loader.load();
            
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, 1000, 800));
            stage.setTitle("GameTrade - 登录");
        } catch (Exception e) {
            showErrorDialog("界面切换失败", e.getMessage());
        }
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
}
