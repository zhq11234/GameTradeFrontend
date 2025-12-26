package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

/**
 * 上架申请信息控制器
 * 负责显示厂商的上架申请信息
 */
public class ViewApplicationsController {

    // FXML组件
    @FXML private Button backButton;
    @FXML private FlowPane applicationsContainer;
    @FXML private Label noDataLabel;
    @FXML private Label loadingLabel;
    @FXML private ComboBox<String> sortComboBox;

    // 业务数据
    private User currentUser;
    private ApiClient apiClient;
    private final ObservableList<ApplicationData> applications = FXCollections.observableArrayList();

    // 配置方法
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @FXML
    public void initialize() {
        initializeSortComboBox();
        loadApplications();
    }

    // 初始化方法
    private void initializeSortComboBox() {
        sortComboBox.getItems().addAll("全部", "待审核", "通过", "拒绝");
        sortComboBox.setValue("全部");
        sortComboBox.setOnAction(event -> filterApplications());
    }

    // 数据加载方法
    private void loadApplications() {
        setLoadingState(true);
        applicationsContainer.getChildren().clear();

        new Thread(() -> {
            try {
                Object response = apiClient.post("/vendors/query-applications-by-company", 
                    Map.of("account", currentUser.getAccount()), Object.class);
                
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(false);
                    handleApiResponse(response);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(false);
                    handleError("加载上架申请数据失败: " + e.getMessage());
                });
            }
        }).start();
    }

    private void filterApplications() {
        String selectedStatus = sortComboBox.getValue();
        
        if ("全部".equals(selectedStatus)) {
            displayApplications();
        } else {
            setLoadingState(true);
            applicationsContainer.getChildren().clear();

            new Thread(() -> {
                try {
                    String statusValue = convertStatusToApiValue(selectedStatus);
                    Object response = apiClient.post("/vendors/query-game-applications", 
                        Map.of("account", currentUser.getAccount(), "approvalStatus", statusValue), Object.class);
                    
                    javafx.application.Platform.runLater(() -> {
                        setLoadingState(false);
                        handleFilteredApiResponse(response);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        setLoadingState(false);
                        handleError("筛选上架申请数据失败: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    // 状态转换方法
    private String convertStatusToApiValue(String chineseStatus) {
        switch (chineseStatus) {
            case "待审核": return "待审批";
            case "通过": return "通过";
            case "拒绝": return "拒绝";
            default: return "";
        }
    }

    // UI状态管理
    private void setLoadingState(boolean loading) {
        loadingLabel.setVisible(loading);
        noDataLabel.setVisible(false);
    }

    // API响应处理
    private void handleApiResponse(Object response) {
        if (response instanceof List) {
            List<Map<String, Object>> applicationList = (List<Map<String, Object>>) response;
            
            if (!applicationList.isEmpty()) {
                applications.clear();
                processApplicationList(applicationList);
                displayApplications();
            } else {
                showNoData("暂无上架申请数据");
            }
        } else {
            showNoData("数据格式错误");
        }
    }

    private void handleFilteredApiResponse(Object response) {
        if (response instanceof List) {
            List<Map<String, Object>> applicationList = (List<Map<String, Object>>) response;
            
            if (!applicationList.isEmpty()) {
                ObservableList<ApplicationData> filteredApplications = FXCollections.observableArrayList();
                processApplicationList(applicationList, filteredApplications);
                displayFilteredApplications(filteredApplications);
            } else {
                showNoData("没有符合条件的上架申请");
            }
        } else {
            showNoData("数据格式错误");
        }
    }

    private void processApplicationList(List<Map<String, Object>> applicationList) {
        processApplicationList(applicationList, applications);
    }

    private void processApplicationList(List<Map<String, Object>> applicationList, ObservableList<ApplicationData> targetList) {
        for (Map<String, Object> appData : applicationList) {
            ApplicationData application = createApplicationFromMap(appData);
            targetList.add(application);
        }
    }

    private ApplicationData createApplicationFromMap(Map<String, Object> appData) {
        String applicationId = getStringValue(appData, "applicationId", "");
        String gameName = getStringValue(appData, "gameName", "未知游戏");
        String companyName = getStringValue(appData, "companyName", "未知企业");
        String approvalStatus = getStringValue(appData, "approvalStatus", "未知状态");
        String approvalResult = getStringValue(appData, "approvalResult", "暂无结果");
        String applicationTime = getStringValue(appData, "applicationTime", "未知时间");

        return new ApplicationData(applicationId, gameName, companyName, approvalStatus, approvalResult, applicationTime);
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    // 错误处理
    private void handleError(String message) {
        noDataLabel.setVisible(true);
        noDataLabel.setText(message);
        ControllerUtils.showErrorAlert(message);
    }

    private void showNoData(String message) {
        noDataLabel.setVisible(true);
        noDataLabel.setText(message);
    }

    // 显示方法
    private void displayApplications() {
        // 彻底清理容器，确保没有残留的卡片
        applicationsContainer.getChildren().clear();
        
        for (ApplicationData application : applications) {
            StackPane card = createApplicationCard(application);
            applicationsContainer.getChildren().add(card);
        }
    }

    private void displayFilteredApplications(ObservableList<ApplicationData> filteredApplications) {
        // 彻底清理容器
        applicationsContainer.getChildren().clear();
        
        if (filteredApplications.isEmpty()) {
            showNoData("没有符合条件的上架申请");
        } else {
            noDataLabel.setVisible(false);
            for (ApplicationData application : filteredApplications) {
                StackPane card = createApplicationCard(application);
                applicationsContainer.getChildren().add(card);
            }
        }
    }

    // 卡片创建方法
    private StackPane createApplicationCard(ApplicationData application) {
        StackPane card = new StackPane();
        card.getStyleClass().add("game-card");
        
        // 创建图片
        ImageView imageView = createCardImage();
        
        // 创建内容区域
        VBox content = createCardContent(application);
        content.setMouseTransparent(true);
        
        // 创建覆盖层
        VBox overlay = createCardOverlay(application);
        
        // 组装卡片
        card.getChildren().addAll(imageView, content, overlay);
        
        // 添加事件处理
        setupCardEvents(card, content, overlay, application);
        
        return card;
    }

    private ImageView createCardImage() {
        ImageView imageView = new ImageView();
        try {
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png")));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            imageView.setStyle("-fx-background-color: #667eea; -fx-min-width: 250px; -fx-min-height: 150px;");
        }
        imageView.setFitWidth(250);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("game-card-image");
        return imageView;
    }

    private VBox createCardContent(ApplicationData application) {
        VBox content = new VBox();
        content.getStyleClass().add("game-card-content");
        
        Label titleLabel = new Label(application.getGameName());
        titleLabel.getStyleClass().add("game-card-title");
        
        Label companyLabel = new Label("企业: " + application.getCompanyName());
        companyLabel.getStyleClass().add("game-card-category");
        
        Label statusLabel = new Label("状态: " + application.getApprovalStatus());
        statusLabel.getStyleClass().add("game-card-status");
        
        Label resultLabel = new Label("结果: " + application.getApprovalResult());
        resultLabel.getStyleClass().add("game-card-price");
        
        content.getChildren().addAll(titleLabel, companyLabel, statusLabel, resultLabel);
        return content;
    }

    private VBox createCardOverlay(ApplicationData application) {
        VBox overlay = new VBox();
        overlay.getStyleClass().add("game-card-overlay");
        overlay.setMouseTransparent(true);
        overlay.setVisible(false);
        
        Label overlayTitle = createOverlayLabel(application.getGameName(), true);
        Label overlayCompany = createOverlayLabel("企业: " + application.getCompanyName(), false);
        Label overlayStatus = createOverlayLabel("状态: " + application.getApprovalStatus(), false);
        Label overlayResult = createOverlayLabel("结果: " + application.getApprovalResult(), false);
        Label overlayTime = createOverlayLabel("申请时间: " + application.getApplicationTime(), false);
        Label overlayId = createOverlayLabel("申请编号: " + application.getApplicationId(), false);
        
        overlay.getChildren().addAll(overlayTitle, overlayCompany, overlayStatus, overlayResult, overlayTime, overlayId);
        return overlay;
    }

    private Label createOverlayLabel(String text, boolean isTitle) {
        Label label = new Label(text);
        label.getStyleClass().add("overlay-text");
        label.setMouseTransparent(true);
        if (isTitle) {
            label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        }
        return label;
    }

    private void setupCardEvents(StackPane card, VBox content, VBox overlay, ApplicationData application) {
        // 悬停效果
        card.setOnMouseEntered(event -> {
            overlay.setVisible(true);
            content.setVisible(false);
        });
        
        card.setOnMouseExited(event -> {
            overlay.setVisible(false);
            content.setVisible(true);
        });
        
        // 使用直接的事件处理，避免复杂的逻辑
        card.setOnMouseClicked(event -> {
            // 直接调用确认对话框，避免中间方法调用
            showCancelConfirmation(application);
        });
    }

    private void showCancelConfirmation(ApplicationData application) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("取消上架申请");
        confirmationAlert.setHeaderText("确认取消上架申请");
        confirmationAlert.setContentText("您确定要取消游戏 '" + application.getGameName() + "' 的上架申请吗？");
        
        // 使用一次性确认对话框
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cancelApplication(application);
        }
    }

    private void cancelApplication(ApplicationData application) {
        new Thread(() -> {
            try {
                Map<String, Object> requestData = Map.of(
                    "account", currentUser.getAccount(),
                    "applicationId", Integer.parseInt(application.getApplicationId())
                );
                
                apiClient.delete("/vendors/cancel-game-application", requestData, Object.class);
                
                javafx.application.Platform.runLater(() -> {
                    ControllerUtils.showInfoAlert("上架申请取消成功");
                    loadApplications(); // 刷新数据
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("取消上架申请失败: " + e.getMessage());
                });
            }
        }).start();
    }

    // FXML事件处理
    @FXML
    private void handleBackToMain() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRefresh() {
        loadApplications();
        ControllerUtils.showInfoAlert("上架申请数据已刷新");
    }

    // 数据类
    public static class ApplicationData {
        private final String applicationId;
        private final String gameName;
        private final String companyName;
        private final String approvalStatus;
        private final String approvalResult;
        private final String applicationTime;
        
        public ApplicationData(String applicationId, String gameName, String companyName, 
                              String approvalStatus, String approvalResult, String applicationTime) {
            this.applicationId = applicationId;
            this.gameName = gameName;
            this.companyName = companyName;
            this.approvalStatus = approvalStatus;
            this.approvalResult = approvalResult;
            this.applicationTime = applicationTime;
        }
        
        public String getApplicationId() { return applicationId; }
        public String getGameName() { return gameName; }
        public String getCompanyName() { return companyName; }
        public String getApprovalStatus() { return approvalStatus; }
        public String getApprovalResult() { return approvalResult; }
        public String getApplicationTime() { return applicationTime; }
    }
}
