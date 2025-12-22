package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.service.UserService;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterController {
    
    private ToggleGroup roleToggleGroup;
    
    @FXML
    private RadioButton buyerRadio;
    
    @FXML
    private RadioButton vendorRadio;
    
    @FXML
    private TextField accountField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField contactField;
    
    @FXML
    private TextField nicknameField;

    @FXML
    private TextField companyNameField;
    
    @FXML
    private TextField registeredAddressField;
    
    @FXML
    private TextField contactPersonField;
    
    @FXML
    private VBox buyerInfoBox;
    
    @FXML
    private VBox vendorInfoBox;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button loginLink;

    @FXML
    private Label errorLabel;

    @FXML
    private VBox successBox;

    @FXML
    private Button loginNowButton;
    
    // 实时验证状态标签
    @FXML
    private Label accountStatusLabel;
    @FXML
    private Label accountErrorLabel;
    @FXML
    private Label passwordStatusLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label confirmPasswordStatusLabel;
    @FXML
    private Label confirmPasswordErrorLabel;
    @FXML
    private Label contactStatusLabel;
    @FXML
    private Label contactErrorLabel;
    @FXML
    private Label nicknameStatusLabel;
    @FXML
    private Label nicknameErrorLabel;
    @FXML
    private Label companyNameStatusLabel;
    @FXML
    private Label companyNameErrorLabel;
    @FXML
    private Label registeredAddressStatusLabel;
    @FXML
    private Label registeredAddressErrorLabel;
    @FXML
    private Label contactPersonStatusLabel;
    @FXML
    private Label contactPersonErrorLabel;

    private final UserService userService;
    
    public RegisterController() {
        this.userService = new UserService();
    }
    
    @FXML
    public void initialize() {
        // 创建ToggleGroup并绑定到RadioButton
        roleToggleGroup = new ToggleGroup();
        buyerRadio.setToggleGroup(roleToggleGroup);
        vendorRadio.setToggleGroup(roleToggleGroup);
        buyerRadio.setSelected(true);
        
        // 初始化控制器
        setupEventHandlers();
        setupRoleToggleListener();
        
        // 确保初始状态正确
        showBuyerFields();
    }
    
    private void setupEventHandlers() {
        // 为注册按钮添加样式变化效果
        ControllerUtils.setupButtonHover(registerButton, "register-button-hover");
        
        // 输入框获得焦点时的样式变化
        ControllerUtils.setupInputFieldFocus(accountField);
        ControllerUtils.setupInputFieldFocus(passwordField);
        ControllerUtils.setupInputFieldFocus(confirmPasswordField);
        ControllerUtils.setupInputFieldFocus(contactField);
        ControllerUtils.setupInputFieldFocus(nicknameField);
        ControllerUtils.setupInputFieldFocus(companyNameField);
        ControllerUtils.setupInputFieldFocus(registeredAddressField);
        ControllerUtils.setupInputFieldFocus(contactPersonField);
        
        // 设置实时数据验证监听器
        setupRealTimeValidation();
    }
    
    private void setupRealTimeValidation() {
        // 账号实时验证
        accountField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateAccount(newValue);
        });
        
        // 密码实时验证
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword(newValue);
            validatePasswordMatch(); // 同时验证密码匹配
        });
        
        // 确认密码实时验证
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswordMatch();
        });
        
        // 联系方式实时验证
        contactField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateContact(newValue);
        });
        
        // 昵称实时验证
        nicknameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNickname(newValue);
        });
        
        // 企业名实时验证
        companyNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateCompanyName(newValue);
        });
        
        // 注册地址实时验证
        registeredAddressField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateRegisteredAddress(newValue);
        });
        
        // 联系人实时验证
        contactPersonField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateContactPerson(newValue);
        });
    }
    
    private void validateAccount(String account) {
        if (account == null || account.trim().isEmpty()) {
            showFieldError(accountField, "账号不能为空");
        } else if (account.length() < 3) {
            showFieldError(accountField, "账号长度不能少于3位");
        } else if (account.length() > 50) {
            showFieldError(accountField, "账号长度不能超过50位");
        } else if (!account.matches("^[a-zA-Z0-9_]+$")) {
            showFieldError(accountField, "账号只能包含字母、数字和下划线");
        } else {
            showFieldSuccess(accountField, "账号格式正确");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            showFieldError(passwordField, "密码不能为空");
        } else if (password.length() < 8) {
            showFieldError(passwordField, "密码长度不能少于8位");
        } else if (password.length() > 100) {
            showFieldError(passwordField, "密码长度不能超过100位");
        } else if (!password.matches(".*[A-Z].*")) {
            showFieldError(passwordField, "密码应包含至少一个大写字母");
        } else if (!password.matches(".*[a-z].*")) {
            showFieldError(passwordField, "密码应包含至少一个小写字母");
        } else if (!password.matches(".*\\d.*")) {
            showFieldError(passwordField, "密码应包含至少一个数字");
        } else {
            showFieldSuccess(passwordField, "密码强度良好");
        }
    }
    
    private void validatePasswordMatch() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            showFieldError(confirmPasswordField, "请确认密码");
        } else if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordField, "两次输入的密码不一致");
        } else if (password.length() >= 8) {
            showFieldSuccess(confirmPasswordField, "密码匹配");
        }
    }
    
    private void validateContact(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            showFieldError(contactField, "联系方式不能为空");
        } else if (contact.length() > 100) {
            showFieldError(contactField, "联系方式长度不能超过100位");
        } else if (contact.matches("^1[3-9]\\d{9}$")) {
            showFieldSuccess(contactField, "手机号格式正确");
        } else if (contact.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            showFieldSuccess(contactField, "邮箱格式正确");
        } else {
            showFieldError(contactField, "请输入有效的手机号或邮箱");
        }
    }
    
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            showFieldError(nicknameField, "昵称不能为空");
        } else if (nickname.length() > 50) {
            showFieldError(nicknameField, "昵称长度不能超过50位");
        } else {
            showFieldSuccess(nicknameField, "昵称格式正确");
        }
    }
    
    private void validateCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            showFieldError(companyNameField, "企业名不能为空");
        } else if (companyName.length() > 100) {
            showFieldError(companyNameField, "企业名长度不能超过100位");
        } else {
            showFieldSuccess(companyNameField, "企业名格式正确");
        }
    }
    
    private void validateRegisteredAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            showFieldError(registeredAddressField, "注册地址不能为空");
        } else if (address.length() > 200) {
            showFieldError(registeredAddressField, "注册地址长度不能超过200位");
        } else {
            showFieldSuccess(registeredAddressField, "注册地址格式正确");
        }
    }
    
    private void validateContactPerson(String contactPerson) {
        if (contactPerson == null || contactPerson.trim().isEmpty()) {
            showFieldError(contactPersonField, "联系人不能为空");
        } else if (contactPerson.length() > 50) {
            showFieldError(contactPersonField, "联系人长度不能超过50位");
        } else {
            showFieldSuccess(contactPersonField, "联系人格式正确");
        }
    }
    
    private void showFieldError(TextField field, String message) {
        // 设置输入框错误样式
        field.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2px;");
        
        // 根据字段类型更新对应的UI元素
        if (field == accountField) {
            accountStatusLabel.setText("❌");
            accountStatusLabel.setVisible(true);
            accountErrorLabel.setText(message);
            accountErrorLabel.setVisible(true);
        } else if (field == passwordField) {
            passwordStatusLabel.setText("❌");
            passwordStatusLabel.setVisible(true);
            passwordErrorLabel.setText(message);
            passwordErrorLabel.setVisible(true);
        } else if (field == confirmPasswordField) {
            confirmPasswordStatusLabel.setText("❌");
            confirmPasswordStatusLabel.setVisible(true);
            confirmPasswordErrorLabel.setText(message);
            confirmPasswordErrorLabel.setVisible(true);
        } else if (field == contactField) {
            contactStatusLabel.setText("❌");
            contactStatusLabel.setVisible(true);
            contactErrorLabel.setText(message);
            contactErrorLabel.setVisible(true);
        } else if (field == nicknameField) {
            nicknameStatusLabel.setText("❌");
            nicknameStatusLabel.setVisible(true);
            nicknameErrorLabel.setText(message);
            nicknameErrorLabel.setVisible(true);
        } else if (field == companyNameField) {
            companyNameStatusLabel.setText("❌");
            companyNameStatusLabel.setVisible(true);
            companyNameErrorLabel.setText(message);
            companyNameErrorLabel.setVisible(true);
        } else if (field == registeredAddressField) {
            registeredAddressStatusLabel.setText("❌");
            registeredAddressStatusLabel.setVisible(true);
            registeredAddressErrorLabel.setText(message);
            registeredAddressErrorLabel.setVisible(true);
        } else if (field == contactPersonField) {
            contactPersonStatusLabel.setText("❌");
            contactPersonStatusLabel.setVisible(true);
            contactPersonErrorLabel.setText(message);
            contactPersonErrorLabel.setVisible(true);
        }
    }
    
    private void showFieldSuccess(TextField field, String message) {
        // 设置输入框成功样式
        field.setStyle("-fx-border-color: #00C851; -fx-border-width: 2px;");
        
        // 根据字段类型更新对应的UI元素
        if (field == accountField) {
            accountStatusLabel.setText("✅");
            accountStatusLabel.setVisible(true);
            accountErrorLabel.setVisible(false);
        } else if (field == passwordField) {
            passwordStatusLabel.setText("✅");
            passwordStatusLabel.setVisible(true);
            passwordErrorLabel.setVisible(false);
        } else if (field == confirmPasswordField) {
            confirmPasswordStatusLabel.setText("✅");
            confirmPasswordStatusLabel.setVisible(true);
            confirmPasswordErrorLabel.setVisible(false);
        } else if (field == contactField) {
            contactStatusLabel.setText("✅");
            contactStatusLabel.setVisible(true);
            contactErrorLabel.setVisible(false);
        } else if (field == nicknameField) {
            nicknameStatusLabel.setText("✅");
            nicknameStatusLabel.setVisible(true);
            nicknameErrorLabel.setVisible(false);
        } else if (field == companyNameField) {
            companyNameStatusLabel.setText("✅");
            companyNameStatusLabel.setVisible(true);
            companyNameErrorLabel.setVisible(false);
        } else if (field == registeredAddressField) {
            registeredAddressStatusLabel.setText("✅");
            registeredAddressStatusLabel.setVisible(true);
            registeredAddressErrorLabel.setVisible(false);
        } else if (field == contactPersonField) {
            contactPersonStatusLabel.setText("✅");
            contactPersonStatusLabel.setVisible(true);
            contactPersonErrorLabel.setVisible(false);
        }
    }
    
    private void setupRoleToggleListener() {
        // 监听角色选择变化
        roleToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == buyerRadio) {
                showBuyerFields();
            } else if (newValue == vendorRadio) {
                showVendorFields();
            }
        });
    }
    
    private void showBuyerFields() {
        buyerInfoBox.setVisible(true);
        vendorInfoBox.setVisible(false);
        
        // 清空厂商相关字段
        companyNameField.clear();
        registeredAddressField.clear();
        contactPersonField.clear();
    }
    
    private void showVendorFields() {
        buyerInfoBox.setVisible(false);
        vendorInfoBox.setVisible(true);
        
        // 清空买家相关字段
        nicknameField.clear();
    }
    
    @FXML
    private void handleRegister() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String contact = contactField.getText().trim();
        
        // 基本验证
        if (account.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || contact.isEmpty()) {
            ControllerUtils.showError(errorLabel, "请填写所有必填字段");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            ControllerUtils.showError(errorLabel, "两次输入的密码不一致");
            return;
        }
        
        if (password.length() < 8) {
            ControllerUtils.showError(errorLabel, "密码长度不能少于8位");
            return;
        }
        
        // 根据角色进行验证和注册
        if (buyerRadio.isSelected()) {
            handleBuyerRegistration(account, password, contact);
        } else if (vendorRadio.isSelected()) {
            handleVendorRegistration(account, password, contact);
        }
    }
    
    private void handleBuyerRegistration(String account, String password, String contact) {
        String nickname = nicknameField.getText().trim();
        
        if (nickname.isEmpty()) {
            ControllerUtils.showError(errorLabel, "请输入昵称");
            return;
        }
        
        // 禁用注册按钮，显示加载状态
        registerButton.setDisable(true);
        registerButton.setText("注册中...");
        
        // 在新线程中执行注册操作
        new Thread(() -> {
            try {
                // 调用用户服务进行买家注册
                boolean success = userService.registerBuyer(account, password, contact, nickname);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        // 注册成功
                        onRegisterSuccess();
                    } else {
                        // 注册失败
                        onRegisterFailure("账号、联系方式或昵称已存在");
                    }
                });
            } catch (Exception e) {
                // 网络错误或其他异常
                javafx.application.Platform.runLater(() -> onRegisterFailure("注册失败: " + e.getMessage()));
            }
        }).start();
    }
    
    private void handleVendorRegistration(String account, String password, String contact) {
        String companyName = companyNameField.getText().trim();
        String registeredAddress = registeredAddressField.getText().trim();
        String contactPerson = contactPersonField.getText().trim();
        
        if (companyName.isEmpty()) {
            ControllerUtils.showError(errorLabel, "请输入企业名");
            return;
        }
        
        if (registeredAddress.isEmpty()) {
            ControllerUtils.showError(errorLabel, "请输入注册地址");
            return;
        }
        
        if (contactPerson.isEmpty()) {
            ControllerUtils.showError(errorLabel, "请输入联系人");
            return;
        }
        
        // 禁用注册按钮，显示加载状态
        registerButton.setDisable(true);
        registerButton.setText("注册中...");
        
        // 在新线程中执行注册操作
        new Thread(() -> {
            try {
                // 调用用户服务进行厂商注册
                boolean success = userService.registerVendor(account, password, contact, 
                    companyName, registeredAddress, contactPerson);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        // 注册成功
                        onRegisterSuccess();
                    } else {
                        // 注册失败
                        onRegisterFailure("账号、联系方式或企业名已存在");
                    }
                });
            } catch (Exception e) {
                // 网络错误或其他异常
                javafx.application.Platform.runLater(() -> onRegisterFailure("注册失败: " + e.getMessage()));
            }
        }).start();
    }
    
    private void onRegisterSuccess() {
        // 重置UI状态
        ControllerUtils.resetButton(registerButton, "注册");
        
        // 隐藏错误信息，显示成功区域
        errorLabel.setVisible(false);
        successBox.setVisible(true);
        
        // 清空输入框
        clearFields();
        
        System.out.println("注册成功");
    }
    
    private void onRegisterFailure(String errorMessage) {
        // 重置UI状态
        ControllerUtils.resetButton(registerButton, "注册");
        
        // 隐藏成功区域，显示错误信息
        successBox.setVisible(false);
        ControllerUtils.showError(errorLabel, errorMessage);
    }
    
    private void clearFields() {
        accountField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        contactField.clear();
        nicknameField.clear();
        companyNameField.clear();
        registeredAddressField.clear();
        contactPersonField.clear();
    }
    
    @FXML
    private void handleBack() {
        ControllerUtils.switchScene(backButton, "/com/database/gametradefrontend/view/welcome.fxml", "GameTrade - 欢迎", 1000, 800);
    }
    
    @FXML
    private void handleLoginLink() {
        ControllerUtils.switchScene(loginLink, "/com/database/gametradefrontend/view/login.fxml", "GameTrade - 登录", 1000, 800);
    }
}
