package com.database.gametradefrontend.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 控制器工具类，用于提取重复的控制器代码
 */
public class ControllerUtils {
    
    /**
     * 切换界面 - 使用指定窗口大小，保持窗口位置，带平滑过渡动画
     * @param currentButton 当前按钮（用于获取Stage）
     * @param fxmlPath FXML文件路径
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    public static void switchScene(Button currentButton, String fxmlPath, String title, int width, int height) {
        try {
            Stage stage = (Stage) currentButton.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();
            
            // 保存当前窗口的位置
            double currentX = stage.getX();
            double currentY = stage.getY();
            
            // 淡出当前场景
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    // 加载新场景
                    FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource(fxmlPath));
                    Parent newRoot = loader.load();
                    
                    // 设置新场景为透明
                    newRoot.setOpacity(0);
                    
                    // 创建新场景
                    Scene newScene = new Scene(newRoot, width, height);
                    stage.setScene(newScene);
                    stage.setTitle(title);
                    
                    // 恢复窗口位置
                    if (currentX > 0 && currentY > 0) {
                        stage.setX(currentX);
                        stage.setY(currentY);
                    }
                    
                    // 淡入新场景
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                    
                    // 确保窗口可见
                    if (!stage.isShowing()) {
                        stage.show();
                    }
                } catch (Exception ex) {
                    showErrorDialog("界面切换失败", ex.getMessage());
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            showErrorDialog("界面切换失败", e.getMessage());
        }
    }
    
    /**
     * 显示错误对话框
     * @param title 对话框标题
     * @param message 错误消息
     */
    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示错误消息（用于标签显示）
     * @param errorLabel 错误标签
     * @param message 错误消息
     */
    public static void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#ff4444"));
        errorLabel.setVisible(true);
    }
    
    /**
     * 显示成功消息（用于标签显示）
     * @param errorLabel 错误标签（复用为成功消息标签）
     * @param message 成功消息
     */
    public static void showSuccess(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.web("#00C851"));
        errorLabel.setVisible(true);
    }
    
    /**
     * 设置输入框焦点样式
     * @param field 输入框
     */
    public static void setupInputFieldFocus(TextField field) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.getStyleClass().add("modern-input-focused");
            } else {
                field.getStyleClass().remove("modern-input-focused");
            }
        });
    }
    
    /**
     * 重置按钮状态
     * @param button 按钮
     * @param originalText 原始文本
     */
    public static void resetButton(Button button, String originalText) {
        button.setDisable(false);
        button.setText(originalText);
    }
    
    /**
     * 设置按钮悬停样式
     * @param button 按钮
     * @param hoverClass 悬停样式类名
     */
    public static void setupButtonHover(Button button, String hoverClass) {
        button.setOnMouseEntered(e -> button.getStyleClass().add(hoverClass));
        button.setOnMouseExited(e -> button.getStyleClass().remove(hoverClass));
    }
}
