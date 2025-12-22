package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.service.UserService;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    
    @FXML
    private TextField accountField;
    
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
        // 为登录按钮添加样式变化效果
        ControllerUtils.setupButtonHover(loginButton, "login-button-hover");
        
        // 输入框获得焦点时的样式变化
        ControllerUtils.setupInputFieldFocus(accountField);
        ControllerUtils.setupInputFieldFocus(passwordField);
    }
    
    @FXML
    private void handleLogin() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();
        
        // 验证输入
        if (account.isEmpty() || password.isEmpty()) {
            ControllerUtils.showError(errorLabel, "请输入账号和密码");
            return;
        }
        
        // 禁用登录按钮，显示加载状态
        loginButton.setDisable(true);
        loginButton.setText("登录中...");
        
        // 在新线程中执行登录操作
        new Thread(() -> {
            try {
                // 调用用户服务进行登录验证
                User user = userService.login(account, password);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (user != null) {
                        // 登录成功
                        onLoginSuccess(user);
                    } else {
                        // 登录失败
                        onLoginFailure("账号或密码错误");
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
        ControllerUtils.resetButton(loginButton, "登录");
        
        // 跳转到主界面
        ControllerUtils.switchScene(loginButton, "/com/database/gametradefrontend/view/main.fxml", "GameTrade - 主界面", 1000, 800);
    }
    
    private void onLoginFailure(String errorMessage) {
        ControllerUtils.resetButton(loginButton, "登录");
        ControllerUtils.showError(errorLabel, errorMessage);
    }
    
    @FXML
    private void handleBack() {
        ControllerUtils.switchScene(backButton, "/com/database/gametradefrontend/view/welcome.fxml", "GameTrade - 欢迎", 1000, 800);
    }
    
    @FXML
    private void handleRegisterLink() {
        ControllerUtils.switchScene(registerLink, "/com/database/gametradefrontend/view/register.fxml", "GameTrade - 注册", 1000, 800);
    }
}
