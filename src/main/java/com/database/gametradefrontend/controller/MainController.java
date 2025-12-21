package com.database.gametradefrontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
            String username = session.getUsername();
            welcomeLabel.setText("欢迎回来，" + username + "！");
            userInfoLabel.setText("用户名: " + username);
        } else {
            welcomeLabel.setText("未登录");
            userInfoLabel.setText("请先登录");
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            // 清除用户会话
            UserSession.getInstance().logout();
            
            // 加载欢迎界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/welcome.fxml"));
            Parent welcomeRoot = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(welcomeRoot, 800, 600));
            stage.setTitle("GameTrade - 欢迎");
        } catch (Exception e) {
            showErrorDialog("退出登录失败", e.getMessage());
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
