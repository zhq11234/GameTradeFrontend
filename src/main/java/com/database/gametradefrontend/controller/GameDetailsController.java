package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import com.database.gametradefrontend.client.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏详情页面控制器
 * 负责游戏详情显示和修改功能
 */
public class GameDetailsController {

    @FXML private Button backButton;
    // 游戏详情页面字段
    @FXML private Label pageTitleLabel;
    @FXML private Label gameNameLabel;
    @FXML private Label companyNameLabel;
    @FXML private Label releaseTimeLabel;
    @FXML private Label statusLabel;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private TextField downloadLinkField;
    @FXML private TextField licenseNumberField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    // 当前用户信息
    private User currentUser;
    
    // API客户端
    private ApiClient apiClient;
    
    // 当前正在编辑的游戏
    private VendorMainController.Game currentEditingGame;

    @FXML
    public void initialize() {
        // 初始化代码（如果需要）
    }
    
    // 设置API客户端
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    // 设置当前用户
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    // 加载游戏详情数据到表单
    public void loadGameDetails(VendorMainController.Game game) {
        // 显示加载状态
        saveButton.setDisable(true);
        saveButton.setText("加载中...");
        
        new Thread(() -> {
            try {
                // 调用API查询游戏详细信息
                String endpoint = "/vendors/query-game-info";
                
                // 准备请求数据，包含游戏名
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("gameName", game.getName());
                
                // 调用API获取游戏详细信息
                Object response = apiClient.post(endpoint, requestData, Object.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (response != null) {
                        // 处理API响应（可能是数组格式）
                        Map<String, Object> gameDetails = null;

                        if (response instanceof List) {
                            // 如果是数组，获取第一个元素
                            List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                            if (!gameList.isEmpty()) {
                                gameDetails = gameList.getFirst();
                            }
                        } else if (response instanceof Map) {
                            // 如果是Map，直接使用
                            gameDetails = (Map<String, Object>) response;
                        }

                        if (gameDetails != null) {
                            // 提取游戏详细信息（添加null检查）
                            String gameName = safeToString(gameDetails.get("gameName"), game.getName());
                            String category = safeToString(gameDetails.get("category"), game.getCategory());
                            String price = safeToString(gameDetails.get("price"), game.getPrice());
                            String companyName = safeToString(gameDetails.get("companyName"), "未知企业");
                            String releaseTime = safeToString(gameDetails.get("releaseTime"), "未知时间");
                            String description = safeToString(gameDetails.get("description"), game.getDescription());
                            String status = safeToString(gameDetails.get("status"), game.getStatus());
                            String downloadLink = safeToString(gameDetails.get("downloadLink"), "");
                            String licenseNumber = safeToString(gameDetails.get("licenseNumber"), "");

                            // 处理免费显示逻辑（如果价格为0，显示"免费"）
                            String displayPrice = price;
                            if ("0".equals(price) || "0.0".equals(price) || "0.00".equals(price)) {
                                displayPrice = "免费";
                            }
                            
                            // 更新UI组件
                            pageTitleLabel.setText("游戏详情 - " + gameName);
                            gameNameLabel.setText(gameName);
                            companyNameLabel.setText(companyName);
                            releaseTimeLabel.setText(releaseTime);
                            statusLabel.setText(status);
                            categoryField.setText(category);
                            priceField.setText(displayPrice);
                            descriptionField.setText(description);
                            downloadLinkField.setText(downloadLink);
                            licenseNumberField.setText(licenseNumber);

                            // 保存当前游戏信息用于后续修改
                            currentEditingGame = new VendorMainController.Game(gameName, category, price, "", description, status,
                                    downloadLink, licenseNumber, companyName, releaseTime);

                        } else {
                            ControllerUtils.showErrorAlert("查询游戏信息失败");
                        }
                    }
                    // 恢复按钮状态
                    saveButton.setDisable(false);
                    saveButton.setText("保存修改");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("查询游戏信息失败: " + e.getMessage());
                    ControllerUtils.showErrorAlert("查询游戏信息失败: " + e.getMessage());
                    saveButton.setDisable(false);
                    saveButton.setText("保存修改");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleBackToMain() {
        // 关闭当前窗口
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleSaveGameDetails() {
        if (currentEditingGame == null) {
            ControllerUtils.showErrorAlert("没有正在编辑的游戏");
            return;
        }
        
        // 获取修改后的数据
        String category = categoryField.getText().trim();
        String price = priceField.getText().trim();
        String description = descriptionField.getText().trim();
        String downloadLink = downloadLinkField.getText().trim();
        String licenseNumber = licenseNumberField.getText().trim();
        
        // 验证必填字段
        if (category.isEmpty() || price.isEmpty()) {
            ControllerUtils.showErrorAlert("游戏类别和价格不能为空");
            return;
        }
        
        // 验证价格格式（必须是有效的数字格式或"免费"）
        if (!isValidPriceFormat(price)) {
            ControllerUtils.showErrorAlert("价格格式不正确，请输入有效的数字格式（如：19.99）或输入'免费'");
            return;
        }
        
        // 处理免费显示逻辑
        if ("免费".equals(price) || "0".equals(price) || "0.0".equals(price) || "0.00".equals(price)) {
            price = "0";
        }
        
        // 显示保存状态
        saveButton.setDisable(true);
        saveButton.setText("保存中...");

        String finalPrice = price;
        new Thread(() -> {
            try {
                // 调用API更新游戏信息
                String endpoint = "/vendors/update-game";
                
                // 准备请求数据
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("account", currentUser.getAccount());
                requestData.put("gameName", currentEditingGame.getName());
                requestData.put("price", finalPrice);
                requestData.put("description", description);
                requestData.put("licenseNumber", licenseNumber);
                requestData.put("downloadLink", downloadLink);
                requestData.put("category", category);
                
                // 调用API更新游戏信息
                String result = apiClient.put(endpoint, requestData, String.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (result != null && result.contains("成功")) {
                        ControllerUtils.showInfoAlert("游戏信息修改成功");
                        
                        // 关闭窗口
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                    } else {
                        ControllerUtils.showErrorAlert("游戏信息修改失败: " + result);
                    }
                    
                    // 恢复按钮状态
                    saveButton.setDisable(false);
                    saveButton.setText("保存修改");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("游戏信息修改失败: " + e.getMessage());
                    saveButton.setDisable(false);
                    saveButton.setText("保存修改");
                });
            }
        }).start();
    }
    
    // 安全转换为字符串，处理null值
    private String safeToString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    // 验证价格格式（数字格式或"免费"）
    private boolean isValidPriceFormat(String price) {
        if (price == null || price.isEmpty()) {
            return false;
        }
        
        // 如果是"免费"，直接通过验证
        if ("免费".equals(price)) {
            return true;
        }
        
        // 验证数字格式（允许小数）
        try {
            // 尝试转换为数字
            double priceValue = Double.parseDouble(price);
            // 价格不能为负数
            return priceValue >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
