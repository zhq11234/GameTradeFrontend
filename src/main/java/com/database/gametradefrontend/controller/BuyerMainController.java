package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.service.UserService;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuyerMainController {
    
    @FXML
    private AnchorPane rootPane;

    
    @FXML
    private Label accountValue;
    
    @FXML
    private Label roleValue;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private DialogPane personalInfoDialog;
    
    @FXML
    private Label dialogAccount;
    
    @FXML
    private Label dialogRole;
    
    @FXML
    private Label dialogContact;
    
    @FXML
    private Label dialogNickname;
    
    @FXML
    private Label dialogBirthday;
    
    @FXML
    private Label dialogCompanyName;
    
    @FXML
    private Label dialogRegisteredAddress;
    
    @FXML
    private Label dialogContactPerson;
    
    @FXML
    private Label dialogRegisterTime;
    
    @FXML
    private Label dialogAdminLevel;
    
    @FXML
    private Label dialogPermissionScope;
    
    @FXML
    private VBox buyerInfoSection;
    
    @FXML
    private VBox vendorInfoSection;
    
    @FXML
    private VBox adminInfoSection;


    
    @FXML
    private Button tradeButton;
    
    @FXML
    private Button collectionButton;

    
    @FXML
    private MenuButton orderButton;
    
    @FXML
    private MenuButton reviewButton;
    
    @FXML
    private MenuButton messageButton;
    
    @FXML
    private MenuButton settingsButton;

    
    @FXML
    private MenuButton userMenuButton;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private VBox dashboardContent;
    
    @FXML
    private FlowPane functionCardsContainer;
    
    @FXML
    private Button notificationButton;
    
    @FXML
    private Button quickTradeButton;
    
    @FXML
    private Button quickCollectionButton;
    
    @FXML
    private Button quickHistoryButton;
    
    @FXML
    private Button quickReviewButton;
    
    @FXML
    private Button quickMessageButton;
    
    @FXML
    private Button quickSettingsButton;
    
    @FXML
    private Button closeDialogButton;
    
    @FXML
    private MenuButton personalInfoButton;


    
    private UserService userService;
    private UserSession userSession;
    private Map<String, Object> personalInfoCache; // 个人信息缓存
    
    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            userSession = UserSession.getInstance();
            
            // 设置窗口大小监听器，实现响应式布局
            setupWindowSizeListener();
            
            // 初始化界面显示
            initializeUserInfo();
            
            // 设置会话监控
            setupSessionMonitor();
            
            // 默认显示首页
            showDashboard();
        } catch (Exception e) {
            ControllerUtils.handleException("初始化", e, messageLabel);
        }
    }
    
    /**
     * 初始化用户信息显示
     */
    private void initializeUserInfo() {
        try {
            if (userSession.isLoggedIn()) {
                String account = userSession.getAccount();
                String role = userSession.getRole();
                String roleDisplayName = getRoleDisplayName(role);
                
                // 更新界面显示
                updateUserInterface(account, roleDisplayName, "欢迎，" + account + "！");
                
                // 启用个人信息相关按钮
                enableUserInfoButtons(true);
                
                // 加载详细的个人信息
                loadPersonalInfo();
            } else {
                // 用户未登录，显示默认信息
                updateUserInterface("未登录", "访客", "欢迎来到GameTrade");
                
                // 禁用个人信息相关按钮
                enableUserInfoButtons(false);
            }
        } catch (Exception e) {
            ControllerUtils.handleException("初始化用户信息", e, messageLabel);
        }
    }
    
    /**
     * 获取角色显示名称
     */
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "buyer":
                return "买家";
            case "vendor":
                return "厂商";
            case "admin":
                return "管理员";
            default:
                return "用户";
        }
    }
    
    /**
     * 更新用户界面显示
     */
    private void updateUserInterface(String account, String role, String welcomeMessage) {
        accountValue.setText(account);
        roleValue.setText(role);
        welcomeLabel.setText(welcomeMessage);
    }
    
    /**
     * 启用或禁用个人信息相关按钮
     * 注意：原代码中的viewInfoButton和editInfoButton在FXML中不存在，已移除相关操作
     * 目前仅保留方法结构，实际无按钮需要控制
     */
    private void enableUserInfoButtons(boolean enabled) {
        // 当前版本中无需要控制的个人信息按钮
        // 保留方法结构以便未来扩展
    }
    
    /**
     * 加载并显示个人信息
     */
    private void loadPersonalInfo() {
        if (!userSession.isLoggedIn()) {
            return;
        }
        
        try {
            String account = userSession.getAccount();
            Object personalInfo = userService.getPersonalInfo(account);
            
            if (personalInfo instanceof Map<?, ?> infoMap) {
                // 更新缓存
                personalInfoCache = new HashMap<>();
                for (Map.Entry<?, ?> entry : infoMap.entrySet()) {
                    personalInfoCache.put(entry.getKey().toString(), entry.getValue());
                }
                
                // 更新对话框中的基本信息
                dialogAccount.setText(account);
                String role = userSession.getRole();
                dialogRole.setText(getRoleDisplayName(role));
                
                // 根据角色显示相应的信息区域
                showRoleSpecificInfo(role, infoMap);
            }
        } catch (Exception e) {
            ControllerUtils.handleException("加载个人信息", e, messageLabel);
        }
    }
    
    /**
     * 根据角色显示相应的信息区域
     */
    private void showRoleSpecificInfo(String role, Map<?, ?> infoMap) {
        // 首先隐藏所有区域
        buyerInfoSection.setVisible(false);
        vendorInfoSection.setVisible(false);
        adminInfoSection.setVisible(false);
        
        // 根据角色显示相应的区域
        switch (role) {
            case "buyer":
                buyerInfoSection.setVisible(true);
                setDialogField(infoMap, "nickname", dialogNickname, "未设置");
                setDialogField(infoMap, "contact", dialogContact, "未设置");
                setDialogField(infoMap, "birthday", dialogBirthday, "未设置");
                break;
                
            case "vendor":
                vendorInfoSection.setVisible(true);
                setDialogField(infoMap, "companyName", dialogCompanyName, "未设置");
                setDialogField(infoMap, "registeredAddress", dialogRegisteredAddress, "未设置");
                setDialogField(infoMap, "contactPerson", dialogContactPerson, "未设置");
                setDialogField(infoMap, "registerTime", dialogRegisterTime, "未知");
                break;
                
            case "admin":
                adminInfoSection.setVisible(true);
                setDialogField(infoMap, "adminLevel", dialogAdminLevel, "普通管理员");
                setDialogField(infoMap, "permissionScope", dialogPermissionScope, "全部权限");
                break;
                
            default:
                // 默认显示买家信息
                buyerInfoSection.setVisible(true);
                setDialogField(infoMap, "nickname", dialogNickname, "未设置");
                setDialogField(infoMap, "contact", dialogContact, "未设置");
                setDialogField(infoMap, "birthday", dialogBirthday, "未设置");
                break;
        }
    }
    
    /**
     * 设置对话框字段值
     */
    private void setDialogField(Map<?, ?> infoMap, String fieldName, Label label, String defaultValue) {
        if (infoMap.containsKey(fieldName)) {
            Object value = infoMap.get(fieldName);
            label.setText(value != null ? value.toString() : defaultValue);
        } else {
            label.setText(defaultValue);
        }
    }
    
    /**
     * 处理查看个人信息
     */
    @FXML
    private void handleViewPersonalInfo() {
        if (!userSession.isLoggedIn()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "请先登录", false);
            return;
        }
        
        // 确保信息是最新的
        loadPersonalInfo();
        
        // 显示个人信息对话框
        personalInfoDialog.setVisible(true);
    }
    
    /**
     * 处理编辑个人信息
     */
    @FXML
    private void handleEditPersonalInfo() {
        if (!userSession.isLoggedIn()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "请先登录", false);
            return;
        }
        
        // 根据用户角色创建不同的编辑对话框
        String role = userSession.getRole();
        if (ROLE_BUYER.equals(role)) {
            showBuyerEditDialog();
        } else if (ROLE_VENDOR.equals(role)) {
            showVendorEditDialog();
        } else {
            ControllerUtils.showAutoHideMessage(messageLabel, "当前角色不支持编辑个人信息", false);
        }
    }
    
    /**
     * 显示买家编辑对话框
     */
    private void showBuyerEditDialog() {
        createEditDialog(
            "编辑个人信息", 
            "修改您的个人信息",
            new String[]{"昵称", "联系方式", "生日 (YYYY-MM-DD)"},
            new String[]{"nickname", "contact", "birthday"},
            this::updateBuyerPersonalInfo
        );
    }
    
    /**
     * 显示厂商编辑对话框
     */
    private void showVendorEditDialog() {
        createEditDialog(
            "编辑企业信息", 
            "修改您的企业信息",
            new String[]{"企业名称", "注册地址", "联系人"},
            new String[]{"companyName", "registeredAddress", "contactPerson"},
            this::updateVendorPersonalInfo
        );
    }
    
    /**
     * 创建编辑对话框的通用方法
     */
    private void createEditDialog(String title, String header, String[] labels, String[] fieldNames, java.util.function.Consumer<Map<String, String>> resultHandler) {
        javafx.scene.control.Dialog<Map<String, String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        
        // 设置按钮类型
        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("保存", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        // 创建表单布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        // 创建输入框数组
        javafx.scene.control.TextField[] fields = new javafx.scene.control.TextField[labels.length];
        
        // 添加表单字段
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new javafx.scene.control.TextField();
            fields[i].setPromptText(labels[i]);
            fields[i].setText(getCurrentInfoValue(fieldNames[i]));
            
            grid.add(new javafx.scene.control.Label(labels[i] + ":"), 0, i);
            grid.add(fields[i], 1, i);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, String> result = new HashMap<>();
                for (int i = 0; i < fieldNames.length; i++) {
                    result.put(fieldNames[i], fields[i].getText().trim());
                }
                return result;
            }
            return null;
        });
        
        // 显示对话框并处理结果
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(resultHandler);
    }
    
    /**
     * 获取当前信息值
     */
    private String getCurrentInfoValue(String fieldName) {
        if (!userSession.isLoggedIn()) {
            return "";
        }
        
        // 首先尝试从缓存获取
        if (personalInfoCache != null && personalInfoCache.containsKey(fieldName)) {
            Object value = personalInfoCache.get(fieldName);
            return value != null ? value.toString() : "";
        }
        
        // 缓存为空，从API获取
        try {
            String account = userSession.getAccount();
            Object personalInfo = userService.getPersonalInfo(account);
            
            if (personalInfo instanceof Map<?, ?> infoMap) {
                // 更新缓存
                personalInfoCache = new HashMap<>();
                for (Map.Entry<?, ?> entry : infoMap.entrySet()) {
                    personalInfoCache.put(entry.getKey().toString(), entry.getValue());
                }
                
                if (infoMap.containsKey(fieldName)) {
                    Object value = infoMap.get(fieldName);
                    return value != null ? value.toString() : "";
                }
            }
        } catch (Exception e) {
            // 记录异常但不中断流程，返回空字符串
            System.err.println("获取个人信息字段 " + fieldName + " 失败: " + e.getMessage());
            // 可以选择记录到日志系统
        }
        return "";
    }
    
    /**
     * 更新买家个人信息
     */
    private void updateBuyerPersonalInfo(Map<String, String> personalInfo) {
        // 输入验证
        if (personalInfo.get("nickname").isEmpty()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "昵称不能为空", false);
            return;
        }
        if (personalInfo.get("contact").isEmpty()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "联系方式不能为空", false);
            return;
        }
        
        // 生日格式验证（如果提供了生日）
        String birthday = personalInfo.get("birthday");
        if (birthday != null && !birthday.isEmpty() && !isValidBirthdayFormat(birthday)) {
            ControllerUtils.showAutoHideMessage(messageLabel, "生日格式不正确，请使用YYYY-MM-DD格式", false);
            return;
        }
        
        updatePersonalInfo(personalInfo, "个人信息");
    }
    
    /**
     * 验证生日格式（YYYY-MM-DD）
     */
    private boolean isValidBirthdayFormat(String birthday) {
        if (birthday == null || birthday.isEmpty()) {
            return true; // 空值视为有效
        }
        
        try {
            java.time.LocalDate.parse(birthday, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 更新厂商个人信息
     */
    private void updateVendorPersonalInfo(Map<String, String> personalInfo) {
        // 输入验证
        if (personalInfo.get("companyName").isEmpty()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "企业名称不能为空", false);
            return;
        }
        if (personalInfo.get("registeredAddress").isEmpty()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "注册地址不能为空", false);
            return;
        }
        if (personalInfo.get("contactPerson").isEmpty()) {
            ControllerUtils.showAutoHideMessage(messageLabel, "联系人不能为空", false);
            return;
        }
        
        updatePersonalInfo(personalInfo, "企业信息");
    }
    
    /**
     * 通用的个人信息更新方法
     */
    private void updatePersonalInfo(Map<String, String> personalInfo, String infoType) {
        // 使用Task进行安全的异步操作
        javafx.concurrent.Task<Boolean> updateTask = new javafx.concurrent.Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Map<String, Object> updateInfo = new HashMap<>(personalInfo);
                return userService.updatePersonalInfo(userSession.getAccount(), updateInfo);
            }
        };
        
        // 设置成功处理
        updateTask.setOnSucceeded(event -> {
            boolean success = updateTask.getValue();
            if (success) {
                ControllerUtils.showAutoHideMessage(messageLabel, infoType + "更新成功", true);
                loadPersonalInfo(); // 重新加载信息
            } else {
                ControllerUtils.showAutoHideMessage(messageLabel, infoType + "更新失败", false);
            }
        });
        
        // 设置失败处理
        updateTask.setOnFailed(event -> {
            Throwable throwable = updateTask.getException();
            // 将Throwable转换为Exception
            Exception exception = throwable instanceof Exception ? (Exception) throwable : new Exception(throwable);
            ControllerUtils.handleException("更新" + infoType, exception, messageLabel);
        });
        
        // 启动任务
        new Thread(updateTask).start();
    }
    
    /**
     * 处理关闭对话框
     */
    @FXML
    private void handleCloseDialog() {
        personalInfoDialog.setVisible(false);
    }
    
    // 角色常量
    private static final String ROLE_BUYER = "buyer";
    private static final String ROLE_VENDOR = "vendor";
    
    /**
     * 处理退出登录
     */
    @FXML
    private void handleLogout() {
        if (userSession.isLoggedIn()) {
            try {
                // 清除缓存
                personalInfoCache = null;
                
                // 调用后端登出接口
                userSession.logout();
                ControllerUtils.showAutoHideMessage(messageLabel, "退出登录成功", true);
                
                // 延迟跳转到欢迎页面
                javafx.application.Platform.runLater(() -> ControllerUtils.switchScene(userMenuButton,
                        "/com/database/gametradefrontend/view/welcome.fxml",
                        "GameTrade - 欢迎", 1000, 800));
                
            } catch (Exception e) {
                ControllerUtils.handleException("退出登录", e, messageLabel);
            }
        } else {
            // 清除缓存
            personalInfoCache = null;
            
            // 直接跳转到欢迎页面
            ControllerUtils.switchScene(userMenuButton, 
                    "/com/database/gametradefrontend/view/welcome.fxml", 
                    "GameTrade - 欢迎", 1000, 800);
        }
    }
    /**
     * 通用的事件处理方法，用于处理功能按钮点击
     */
    private void handleFunctionButton(String title, String subtitle, String icon, javafx.scene.control.ButtonBase button) {
        showContentPage(title, subtitle, icon);
        updateNavButtonState(button);
    }
    
    // 以下为功能按钮的事件处理方法（占位实现）
    
    @FXML
    private void handleTrade() {
        handleFunctionButton("游戏交易", "浏览和购买您喜欢的游戏", "🛒", tradeButton);
    }
    
    @FXML
    private void handleCollection() {
        handleFunctionButton("我的收藏", "管理您收藏的游戏和心愿单", "📚", collectionButton);
    }
    
    @FXML
    private void handleHistory() {
        handleFunctionButton("交易记录", "查看您的交易历史和订单详情", "📊", orderButton);
    }
    
    @FXML
    private void handleReview() {
        handleFunctionButton("评价管理", "查看和发布游戏评价", "⭐", reviewButton);
    }
    
    @FXML
    private void handleMessage() {
        handleFunctionButton("消息中心", "查看系统通知和交易消息", "🔔", messageButton);
    }
    
    @FXML
    private void handleSettings() {
        handleFunctionButton("系统设置", "账户管理、隐私设置和通知设置", "⚙️", settingsButton);
    }
    
    /**
     * 通用的级联菜单事件处理方法
     */
    private void handleCascadingMenu(String title, String subtitle, String icon, javafx.scene.control.ButtonBase parentButton) {
        showContentPage(title, subtitle, icon);
        updateNavButtonState(parentButton);
    }
    
    // 交易管理级联菜单方法
    @FXML
    private void handleOrderManagement() {
        handleCascadingMenu("订单管理", "查看和管理您的所有订单", "📦", orderButton);
    }
    
    @FXML
    private void handleRefundRequest() {
        handleCascadingMenu("退款申请", "申请退款和查看退款进度", "💸", orderButton);
    }
    
    // 评价管理级联菜单方法
    @FXML
    private void handleMyReviews() {
        handleCascadingMenu("我的评价", "查看您发布的所有评价", "⭐", reviewButton);
    }
    
    @FXML
    private void handlePostReview() {
        handleCascadingMenu("发布评价", "为购买的游戏发布评价", "✍️", reviewButton);
    }
    
    @FXML
    private void handleReviewStats() {
        handleCascadingMenu("评价统计", "查看评价统计和分析", "📊", reviewButton);
    }
    
    // 消息中心级联菜单方法
    @FXML
    private void handleSystemMessages() {
        handleCascadingMenu("系统通知", "查看系统公告和重要通知", "📢", messageButton);
    }
    
    @FXML
    private void handleTradeMessages() {
        handleCascadingMenu("交易消息", "查看交易相关的消息", "💬", messageButton);
    }
    
    @FXML
    private void handlePrivateMessages() {
        handleCascadingMenu("私信", "查看和管理私信", "✉️", messageButton);
    }
    
    // 系统设置级联菜单方法
    @FXML
    private void handleAccountSettings() {
        handleCascadingMenu("账户设置", "修改账户信息和安全设置", "👤", settingsButton);
    }
    
    @FXML
    private void handlePrivacySettings() {
        handleCascadingMenu("隐私设置", "管理您的隐私偏好", "🔒", settingsButton);
    }
    
    @FXML
    private void handleNotificationSettings() {
        handleCascadingMenu("通知设置", "自定义接收的通知类型", "🔔", settingsButton);
    }
    
    /**
     * 显示首页仪表板
     */
    @FXML
    private void showDashboard() {
        javafx.application.Platform.runLater(() -> {
            // 清空内容区域
            contentArea.getChildren().clear();
            
            // 显示仪表板内容
            contentArea.getChildren().add(dashboardContent);
            
            // 更新导航按钮激活状态
            updateNavButtonState(dashboardButton);
            
            // 显示成功消息
            ControllerUtils.showAutoHideMessage(messageLabel, "已切换到首页", true);
        });
    }
    
    /**
     * 更新导航按钮激活状态
     * @param activeButton 当前激活的按钮
     */
    private void updateNavButtonState(javafx.scene.control.ButtonBase activeButton) {
        // 清除所有按钮的激活状态
        dashboardButton.getStyleClass().remove("active");
        tradeButton.getStyleClass().remove("active");
        collectionButton.getStyleClass().remove("active");
        orderButton.getStyleClass().remove("active");
        reviewButton.getStyleClass().remove("active");
        messageButton.getStyleClass().remove("active");
        settingsButton.getStyleClass().remove("active");
        
        // 设置当前按钮为激活状态
        activeButton.getStyleClass().add("active");
    }
    
    /**
     * 在内容区域显示页面内容
     * @param title 页面标题
     * @param subtitle 页面副标题
     * @param icon 页面图标
     */
    private void showContentPage(String title, String subtitle, String icon) {
        javafx.application.Platform.runLater(() -> {
            // 清空内容区域
            contentArea.getChildren().clear();
            
            // 创建页面内容
            VBox pageContent = new VBox(20);
            pageContent.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            pageContent.setPadding(new javafx.geometry.Insets(40, 20, 20, 20));
            
            // 页面标题
            Label titleLabel = new Label(icon + " " + title);
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            
            // 页面副标题
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-text-alignment: center;");
            subtitleLabel.setWrapText(true);
            subtitleLabel.setMaxWidth(400);
            
            // 功能开发中提示
            Label devLabel = new Label("🚧 功能开发中...");
            devLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
            
            // 添加内容到页面
            pageContent.getChildren().addAll(titleLabel, subtitleLabel, devLabel);
            
            // 添加到内容区域
            contentArea.getChildren().add(pageContent);
            
            // 显示成功消息
            ControllerUtils.showAutoHideMessage(messageLabel, "已切换到：" + title, true);
        });
    }
    
    /**
     * 设置窗口大小监听器，实现响应式布局（带防抖处理）
     */
    private void setupWindowSizeListener() {
        // 防抖计时器
        Timeline resizeTimer = new Timeline(new KeyFrame(Duration.millis(250)));
        resizeTimer.setCycleCount(1);
        
        // 监听窗口宽度变化
        rootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((observable1, oldValue, newValue) -> {
                    // 防抖处理：停止之前的计时器，重新开始
                    resizeTimer.stop();
                    resizeTimer.getKeyFrames().setAll(new KeyFrame(Duration.millis(250),
                        e -> adjustLayoutForScreenSize(newValue.doubleValue())));
                    resizeTimer.play();
                });
                
                // 初始调整
                adjustLayoutForScreenSize(newScene.getWidth());
            }
        });
    }
    
    /**
     * 根据屏幕宽度调整布局
     * @param screenWidth 屏幕宽度
     */
    private void adjustLayoutForScreenSize(double screenWidth) {
        javafx.application.Platform.runLater(() -> {
            // 清除所有响应式样式类
            rootPane.getStyleClass().removeAll("extra-small-screen", "small-screen", "medium-screen", "large-screen");
            
            // 根据屏幕宽度应用不同的样式类
            if (screenWidth < 480) {
                // 超小屏幕（手机设备）
                rootPane.getStyleClass().add("extra-small-screen");
                adjustFlowPaneForExtraSmallScreen();
            } else if (screenWidth < 800) {
                // 小屏幕
                rootPane.getStyleClass().add("small-screen");
                adjustFlowPaneForSmallScreen();
            } else if (screenWidth < 1200) {
                // 中等屏幕
                rootPane.getStyleClass().add("medium-screen");
                adjustFlowPaneForMediumScreen();
            } else {
                // 大屏幕
                rootPane.getStyleClass().add("large-screen");
                adjustFlowPaneForLargeScreen();
            }
        });
    }
    
    /**
     * 超小屏幕下的FlowPane调整
     */
    private void adjustFlowPaneForExtraSmallScreen() {
        if (functionCardsContainer != null) {
            functionCardsContainer.setPrefWrapLength(400); // 更小的换行长度
            functionCardsContainer.setHgap(15);
            functionCardsContainer.setVgap(15);
        }
    }
    
    /**
     * 小屏幕下的FlowPane调整
     */
    private void adjustFlowPaneForSmallScreen() {
        if (functionCardsContainer != null) {
            functionCardsContainer.setPrefWrapLength(600); // 较小的换行长度
            functionCardsContainer.setHgap(20);
            functionCardsContainer.setVgap(20);
        }
    }
    
    /**
     * 中等屏幕下的FlowPane调整
     */
    private void adjustFlowPaneForMediumScreen() {
        if (functionCardsContainer != null) {
            functionCardsContainer.setPrefWrapLength(800);
            functionCardsContainer.setHgap(25);
            functionCardsContainer.setVgap(25);
        }
    }
    
    /**
     * 大屏幕下的FlowPane调整
     */
    private void adjustFlowPaneForLargeScreen() {
        if (functionCardsContainer != null) {
            functionCardsContainer.setPrefWrapLength(1000);
            functionCardsContainer.setHgap(30);
            functionCardsContainer.setVgap(30);
        }
    }
    /**
     * 设置会话监控，检查会话超时
     */
    private void setupSessionMonitor() {
        Timeline sessionTimer = new Timeline(
            new KeyFrame(Duration.minutes(1), e -> checkSessionTimeout())
        );
        sessionTimer.setCycleCount(Timeline.INDEFINITE);
        sessionTimer.play();
    }
    
    /**
     * 检查会话超时状态
     */
    private void checkSessionTimeout() {
        if (userSession.isLoggedIn()) {
            long remainingTime = userSession.getRemainingSessionTime();
            if (remainingTime < 5 * 60 * 1000) { // 5分钟提醒
                javafx.application.Platform.runLater(() -> 
                    ControllerUtils.showAutoHideMessage(messageLabel, 
                        "会话即将过期，请及时保存工作", false));
            }
        }
    }
    
    /**
     * 处理窗口最小化
     */
    @FXML
    private void handleMinimize() {
        javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
        stage.setIconified(true);
    }
    
    /**
     * 处理窗口最大化/还原
     */
    @FXML
    private void handleMaximize() {
        javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            stage.setMaximized(true);
        }
    }
    
    /**
     * 处理窗口关闭
     */
    @FXML
    private void handleClose() {
        javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}
