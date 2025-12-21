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

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button registerLink;
    
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
        // 为登录按钮添加样式变化效果 - 使用CSS类
        loginButton.setOnMouseEntered(e -> loginButton.getStyleClass().add("login-button-hover"));
        loginButton.setOnMouseExited(e -> loginButton.getStyleClass().remove("login-button-hover"));
        
        // 输入框获得焦点时的样式变化 - 使用CSS类
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.getStyleClass().add("modern-input-focused");
            } else {
                usernameField.getStyleClass().remove("modern-input-focused");
            }
        });
        
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.getStyleClass().add("modern-input-focused");
            } else {
                passwordField.getStyleClass().remove("modern-input-focused");
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
        
        // 跳转到主界面
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/main.fxml"));
            Parent mainRoot = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(mainRoot, 1000, 800));
            stage.setTitle("GameTrade - 主界面");
        } catch (Exception e) {
            showErrorDialog("跳转失败", "无法加载主界面: " + e.getMessage());
        }
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
    private void handleRegisterLink() {
        try {
            // 加载注册界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/register.fxml"));
            Parent registerRoot = loader.load();
            
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(registerRoot, 1000, 800));
            stage.setTitle("GameTrade - 注册");
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
