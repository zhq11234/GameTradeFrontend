package com.database.gametradefrontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * 控制器工具类，用于提取重复的控制器代码
 */
public class ControllerUtils {
    
    /**
     * 切换界面
     * @param currentButton 当前按钮（用于获取Stage）
     * @param fxmlPath FXML文件路径
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    public static void switchScene(Button currentButton, String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) currentButton.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
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
