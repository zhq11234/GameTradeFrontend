package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 游戏创建页面控制器
 * 负责处理游戏创建表单的提交和验证
 */
public class GameCreationController {
    
    // 表单字段
    @FXML private TextField gameNameField;
    @FXML private TextField gameCategoryField;
    @FXML private TextField gamePriceField;
    @FXML private TextField companyNameField;
    @FXML private TextArea gameDescriptionField;
    @FXML private TextField downloadLinkField;
    @FXML private TextField licenseNumberField;
    @FXML private ImageView gameImageView;
    @FXML private Button selectImageButton;
    @FXML private Label imageFileNameLabel;
    @FXML private Button submitGameButton;
    @FXML private Button cancelGameButton;
    
    // API客户端
    private ApiClient apiClient;
    
    // 当前用户信息
    private User currentUser;
    
    // 选择的图片文件
    private File selectedImageFile;
    private String imageFileName = "yuanshen.png";
    
    @FXML
    public void initialize() {
        // 获取当前用户信息
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            ControllerUtils.showErrorAlert("用户未登录");
            closeWindow();
            return;
        }
        
        // 创建API客户端实例
        apiClient = new ApiClient();
        
        // 设置默认图片
        loadDefaultImage();
    }
    
    private void loadDefaultImage() {
        try {
            // 加载默认图片
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png")));
            gameImageView.setImage(defaultImage);
            imageFileNameLabel.setText("默认图片: yuanshen.png");
        } catch (Exception e) {
            // 如果默认图片加载失败，显示占位符
            gameImageView.setStyle("-fx-background-color: #ccc; -fx-min-width: 80px; -fx-min-height: 60px;");
            imageFileNameLabel.setText("默认图片加载失败");
        }
    }
    
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择游戏图片");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(selectImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // 显示选中的图片
                Image image = new Image(selectedFile.toURI().toString());
                gameImageView.setImage(image);
                
                // 生成新的文件名
                String originalName = selectedFile.getName();
                String extension = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = originalName.substring(dotIndex);
                }
                imageFileName = System.currentTimeMillis() + extension;
                
                // 只保存文件引用，不立即复制
                selectedImageFile = selectedFile;
                
                // 更新文件名显示
                imageFileNameLabel.setText("已选择: " + originalName);
                
            } catch (Exception e) {
                ControllerUtils.showErrorAlert("图片加载失败: " + e.getMessage());
            }
        }
    }
    
    private void copyImageToIconDirectory(File sourceFile, String targetFileName) {
        try {
            // 获取项目根目录
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path iconDir = projectRoot.resolve("src/main/resources/icon");
            
            // 确保icon目录存在
            if (!Files.exists(iconDir)) {
                Files.createDirectories(iconDir);
            }
            
            // 复制文件
            Path targetPath = iconDir.resolve(targetFileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (IOException e) {
            ControllerUtils.showErrorAlert("图片保存失败: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSubmitGame() {
        // 获取表单数据
        String gameName = gameNameField.getText().trim();
        String gameCategory = gameCategoryField.getText().trim();
        String gamePrice = gamePriceField.getText().trim();
        String companyName = companyNameField.getText().trim();
        String gameDescription = gameDescriptionField.getText().trim();
        String downloadLink = downloadLinkField.getText().trim();
        String licenseNumber = licenseNumberField.getText().trim();
        
        // 验证必填字段
        if (gameName.isEmpty() || gameCategory.isEmpty() || gamePrice.isEmpty() || 
            companyName.isEmpty() || gameDescription.isEmpty() || downloadLink.isEmpty() || licenseNumber.isEmpty()) {
            ControllerUtils.showErrorAlert("请填写所有必填字段");
            return;
        }
        
        // 验证下载链接格式
        if (!downloadLink.startsWith("http://") && !downloadLink.startsWith("https://")) {
            ControllerUtils.showErrorAlert("下载链接必须以 http:// 或 https:// 开头");
            return;
        }
        
        // 显示提交状态
        submitGameButton.setDisable(true);
        submitGameButton.setText("提交中...");
        
        new Thread(() -> {
            try {
                // 准备请求数据
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("gameName", gameName);
                requestData.put("category", gameCategory);
                requestData.put("price", gamePrice);
                requestData.put("companyName", companyName);
                requestData.put("description", gameDescription);
                requestData.put("downloadLink", downloadLink);
                requestData.put("licenseNumber", licenseNumber);
                requestData.put("account", currentUser.getAccount());
                
                // 调用API创建游戏
                String result = apiClient.post("/vendors/create-game", requestData, String.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (result != null && result.contains("成功")) {
                        // 如果用户选择了新图片，复制到icon目录
                        if (selectedImageFile != null) {
                            copyImageToIconDirectory(selectedImageFile, imageFileName);
                        }
                        ControllerUtils.showInfoAlert("游戏创建成功！\n游戏名称: " + gameName);
                        closeWindow();
                    } else {
                        ControllerUtils.showErrorAlert("游戏创建失败: " + (result != null ? result : "未知错误"));
                    }
                    
                    // 恢复按钮状态
                    submitGameButton.setDisable(false);
                    submitGameButton.setText("提交创建");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("游戏创建失败: " + e.getMessage());
                    submitGameButton.setDisable(false);
                    submitGameButton.setText("提交创建");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleCancelGame() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelGameButton.getScene().getWindow();
        stage.close();
    }
}
