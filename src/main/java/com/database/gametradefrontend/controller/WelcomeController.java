package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.util.ControllerUtils;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class WelcomeController {
    
    @FXML private Label welcomeText1, welcomeText2, welcomeText3, welcomeText4;
    @FXML private Label welcomeText5, welcomeText6, welcomeText7, welcomeText8;
    @FXML private Button loginButton, registerButton;
    
    @FXML
    public void initialize() {
        // 初始化文字动画
        setupTextAnimations();
        
        // 初始化按钮动画
        setupButtonAnimations();
    }
    
    private void setupTextAnimations() {
        Label[] textLabels = {welcomeText1, welcomeText2, welcomeText3, welcomeText4,
                             welcomeText5, welcomeText6, welcomeText7, welcomeText8};
        
        // 为每个文字设置不同的进入方向和延迟
        for (int i = 0; i < textLabels.length; i++) {
            Label label = textLabels[i];
            label.setVisible(true);
            
            // 设置不同的进入方向
            String[] directions = {"left", "right", "top", "bottom"};
            String direction = directions[i % directions.length];
            
            // 设置初始位置和透明度
            switch (direction) {
                case "left":
                    label.setTranslateX(-100);
                    break;
                case "right":
                    label.setTranslateX(100);
                    break;
                case "top":
                    label.setTranslateY(-100);
                    break;
                case "bottom":
                    label.setTranslateY(100);
                    break;
            }
            label.setOpacity(0);
            
            // 创建进入动画
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(800), label);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), label);
            
            switch (direction) {
                case "left":
                    translateTransition.setToX(0);
                    break;
                case "right":
                    translateTransition.setToX(0);
                    break;
                case "top":
                    translateTransition.setToY(0);
                    break;
                case "bottom":
                    translateTransition.setToY(0);
                    break;
            }
            
            fadeTransition.setToValue(1);
            
            // 设置延迟
            translateTransition.setDelay(Duration.millis(i * 150));
            fadeTransition.setDelay(Duration.millis(i * 150));
            
            // 播放动画
            translateTransition.play();
            fadeTransition.play();
        }
    }
    

    

    
    private void setupButtonAnimations() {
        // 按钮进入动画
        FadeTransition loginFade = new FadeTransition(Duration.millis(1000), loginButton);
        FadeTransition registerFade = new FadeTransition(Duration.millis(1000), registerButton);
        
        loginButton.setOpacity(0);
        registerButton.setOpacity(0);
        
        loginFade.setToValue(1);
        registerFade.setToValue(1);
        
        loginFade.setDelay(Duration.millis(1200));
        registerFade.setDelay(Duration.millis(1400));
        
        loginFade.play();
        registerFade.play();
    }
    
    @FXML
    private void handleLogin() {
        ControllerUtils.switchScene(loginButton, "/com/database/gametradefrontend/view/login.fxml", "GameTrade - 登录", 1000, 800);
    }
    
    @FXML
    private void handleRegister() {
        ControllerUtils.switchScene(registerButton, "/com/database/gametradefrontend/view/register.fxml", "GameTrade - 注册", 1000, 800);
    }
}
