package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label userInfoLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    public void initialize() {
        // 显示当前用户信息
        displayUserInfo();
    }
    
    private void displayUserInfo() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String account = session.getAccount();
            welcomeLabel.setText("欢迎，" + account + "！");
            userInfoLabel.setText("账号: " + account);
        } else {
            welcomeLabel.setText("未登录");
            userInfoLabel.setText("请先登录");
        }
    }
    
    @FXML
    private void handleLogout() {
        // 清除用户会话
        UserSession.getInstance().logout();
        
        // 加载欢迎界面
        ControllerUtils.switchScene(logoutButton, "/com/database/gametradefrontend/view/welcome.fxml", "GameTrade - 欢迎", 800, 600);
    }
}
