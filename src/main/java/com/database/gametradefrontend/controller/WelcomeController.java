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
        // 播放文字动画效果
        playTextAnimation();
    }
    
    private void playTextAnimation() {
        // 初始化所有文字为不可见
        Label[] textLabels = {welcomeText1, welcomeText2, welcomeText3, welcomeText4,
                             welcomeText5, welcomeText6, welcomeText7, welcomeText8};
        
        for (Label label : textLabels) {
            label.setVisible(false);
            label.setOpacity(0);
        }
        
        // 初始化按钮为不可见
        loginButton.setOpacity(0);
        registerButton.setOpacity(0);
        
        // 为每个文字设置不同的入场动画
        animateText(welcomeText1, -200, -200, 0, 0, 0);  // 从左上角进入
        animateText(welcomeText2, 0, -300, 0, 0, 200);   // 从正上方进入
        animateText(welcomeText3, 200, -200, 0, 0, 400); // 从右上角进入
        animateText(welcomeText4, -300, 0, 0, 0, 600);   // 从正左方进入
        animateText(welcomeText5, 300, 0, 0, 0, 800);    // 从正右方进入
        animateText(welcomeText6, -200, 200, 0, 0, 1000); // 从左下角进入
        animateText(welcomeText7, 0, 300, 0, 0, 1200);    // 从正下方进入
        animateText(welcomeText8, 200, 200, 0, 0, 1400);  // 从右下角进入
        
        // 延迟显示按钮
        FadeTransition buttonFade = new FadeTransition(Duration.millis(1200), loginButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);
        buttonFade.setDelay(Duration.millis(1500));
        buttonFade.play();
        
        FadeTransition registerButtonFade = new FadeTransition(Duration.millis(1200), registerButton);
        registerButtonFade.setFromValue(0);
        registerButtonFade.setToValue(1);
        registerButtonFade.setDelay(Duration.millis(1800));
        registerButtonFade.play();
    }
    
    private void animateText(Label label, double fromX, double fromY, double toX, double toY, int delay) {
        // 设置初始位置
        label.setTranslateX(fromX);
        label.setTranslateY(fromY);
        label.setVisible(true);
        
        // 创建移动动画
        TranslateTransition moveTransition = new TranslateTransition(Duration.millis(1200), label);
        moveTransition.setFromX(fromX);
        moveTransition.setFromY(fromY);
        moveTransition.setToX(toX);
        moveTransition.setToY(toY);
        moveTransition.setDelay(Duration.millis(delay));
        
        // 创建淡入动画
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), label);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setDelay(Duration.millis(delay));
        
        // 同时播放移动和淡入动画
        moveTransition.play();
        fadeTransition.play();
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
