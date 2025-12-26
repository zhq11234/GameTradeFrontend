package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.*;


/**
 * 厂商主页面控制器
 * 负责厂商用户的所有功能实现
 */
public class VendorMainController {

    public TextField contactPersonField;
    // 顶部导航组件
    @FXML private Label userInfoLabel;
    @FXML private Button logoutButton;
    
    // 选项卡按钮
    @FXML private Button dashboardTab;
    @FXML private Button gameManagementTab;
    @FXML private Button salesDataTab;
    @FXML private Button profileTab;
    
    // 内容区域
    @FXML private VBox dashboardContent;
    @FXML private VBox gameManagementContent;
    @FXML private VBox salesDataContent;
    @FXML private VBox profileContent;
    
    // 游戏卡片容器
    @FXML private FlowPane gameCardsContainer;
    
    // 刷新按钮
    @FXML private Button refreshButton;
    
    // 游戏管理功能按钮
    @FXML private Button createGameButton;
    @FXML private Button editGameButton;
    @FXML private Button publishGameButton;
    @FXML private Button unpublishGameButton;
    @FXML private Button viewApplicationsButton;
    
    // 数据表格
    @FXML private TableView<SalesData> salesTable;
    
    // 销售数据刷新按钮
    @FXML private Button refreshSalesDataButton;
    
    // 个人信息表单
    @FXML private Label accountLabel;
    @FXML private Label companyLabel;
    @FXML private TextField addressField;
    @FXML private TextField contactField;
    @FXML private Button saveProfileButton;
    
    // 游戏创建表单字段
    @FXML private TextField gameNameField;
    @FXML private TextField gameCategoryField;
    @FXML private TextField gamePriceField;
    @FXML private TextField companyNameField;
    @FXML private TextArea gameDescriptionField;
    @FXML private TextField downloadLinkField;
    @FXML private TextField licenseNumberField;
    @FXML private Button submitGameButton;
    @FXML private Button cancelGameButton;
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
    @FXML private TextField downloadLinkFieldDetails;
    @FXML private TextField licenseNumberFieldDetails;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    // 当前用户信息
    private User currentUser;
    
    // API客户端
    private ApiClient apiClient;
    
    // 模拟数据
    private final List<Game> games = new ArrayList<>();
    private final ObservableList<SalesData> salesData = FXCollections.observableArrayList();
    
    // 当前正在编辑的游戏
    private Game currentEditingGame;
    
    @FXML
    public void initialize() {
        // 获取当前用户信息
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            ControllerUtils.showErrorAlert("用户未登录");
            return;
        }
        
        // 创建API客户端实例
        apiClient = new ApiClient();
        
        // 初始化用户界面
        initializeUserInfo();
        initializeTabs();
        initializeGameCards();
        initializeTables();
        setupEventHandlers();
        
        // 默认显示游戏概览页面
        showDashboard();
    }
    
    private void initializeUserInfo() {
        userInfoLabel.setText("厂商用户 - " + currentUser.getAccount());
    }
    
    private void initializeTabs() {
        // 设置选项卡样式
        resetTabStyles();
        dashboardTab.getStyleClass().add("tab-active");
    }
    
    private void initializeGameCards() {
        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在加载游戏数据...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步从API获取游戏数据
        new Thread(() -> {
            try {
                // 调用API获取厂商游戏数据
                String endpoint = "/vendors/query-vendor-games";
                // 准备请求数据，包含account参数
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("account", currentUser.getAccount());
                
                Object response = apiClient.post(endpoint, requestData, Object.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    // 清空加载状态
                    gameCardsContainer.getChildren().clear();
                    
                    List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                    
                    if (gameList != null && !gameList.isEmpty()) {
                        games.clear();
                        
                        // 处理API返回的游戏数据
                        for (Map<String, Object> gameData : gameList) {
                            String name = gameData.getOrDefault("gameName", gameData.getOrDefault("name", "未知游戏")).toString();
                            String category = gameData.getOrDefault("category", "未知类别").toString();
                            String price = gameData.getOrDefault("price", "免费").toString();
                            String status = gameData.getOrDefault("status", "未知状态").toString();
                            String description = gameData.getOrDefault("description", "暂无简介").toString();
                            String image = gameData.getOrDefault("image", "yuanshen.png").toString();

                            
                            // 创建游戏对象
                            Game game = new Game(name, category, price, image, description, status);
                            games.add(game);
                            
                            // 创建游戏卡片
                            StackPane gameCard = createGameCard(game);
                            gameCardsContainer.getChildren().add(gameCard);
                        }
                    } else {
                        // 如果没有数据，显示提示信息
                        Label noDataLabel = new Label("暂无游戏数据");
                        noDataLabel.getStyleClass().add("no-data-label");
                        gameCardsContainer.getChildren().add(noDataLabel);
                    }
                });
            } catch (Exception e) {
                // 在主线程中显示错误信息
                javafx.application.Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    Label errorLabel = new Label("加载游戏数据失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    gameCardsContainer.getChildren().add(errorLabel);
                    ControllerUtils.showErrorAlert("加载游戏数据失败: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private StackPane createGameCard(Game game) {
        // 创建游戏卡片容器
        StackPane card = new StackPane();
        card.getStyleClass().add("game-card");
        
        // 创建主内容区域
        VBox content = new VBox();
        content.getStyleClass().add("game-card-content");
        
        // 游戏图片
        ImageView imageView = new ImageView();

        // 首先尝试加载用户传入的图片路径
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(game.getImage())));
            imageView.setImage(image);
        } catch (Exception e) {
            // 如果直接路径找不到，尝试在icon目录下查找
            try {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/" + game.getImage())));
                imageView.setImage(image);
            } catch (Exception ex) {
                // 如果icon目录下也找不到，使用默认图片
                try {
                    Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png")));
                    imageView.setImage(defaultImage);
                } catch (Exception exc) {
                    // 如果默认图片也不存在，创建一个占位符
                    imageView.setStyle("-fx-background-color: #667eea; -fx-min-width: 250px; -fx-min-height: 150px;");
                }
            }
        }
        imageView.setFitWidth(250);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("game-card-image");
        
        // 游戏信息
        Label titleLabel = new Label(game.getName());
        titleLabel.getStyleClass().add("game-card-title");
        
        Label categoryLabel = new Label("类别: " + game.getCategory());
        categoryLabel.getStyleClass().add("game-card-category");
        
        Label priceLabel = new Label("价格: " + game.getPrice());
        priceLabel.getStyleClass().add("game-card-price");
        
        // 状态标签
        Label statusLabel = new Label("状态: " + game.getStatus());
        statusLabel.getStyleClass().add("game-card-status");
        
        content.getChildren().addAll(titleLabel, categoryLabel, priceLabel, statusLabel);
        
        // 创建悬停覆盖层
        VBox overlay = new VBox();
        overlay.getStyleClass().add("game-card-overlay");
        overlay.setMouseTransparent(true);
        overlay.setVisible(false);
        
        Label overlayTitle = new Label(game.getName());
        overlayTitle.getStyleClass().add("overlay-text");
        overlayTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label overlayCategory = new Label("类别: " + game.getCategory());
        overlayCategory.getStyleClass().add("overlay-text");
        
        Label overlayPrice = new Label("价格: " + game.getPrice());
        overlayPrice.getStyleClass().add("overlay-text");
        
        Label overlayStatus = new Label("状态: " + game.getStatus());
        overlayStatus.getStyleClass().add("overlay-text");
        
        // 限制简介长度为50个字符
        String description = game.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        Label overlayDescription = new Label("简介: " + description);
        overlayDescription.getStyleClass().add("overlay-text");
        overlayDescription.setWrapText(true);
        overlayDescription.setMaxWidth(230);
        
        overlay.getChildren().addAll(overlayTitle, overlayCategory, overlayPrice, overlayStatus, overlayDescription);
        
        // 组装卡片
        card.getChildren().addAll(imageView, content, overlay);
        
        // 添加鼠标悬停事件
        card.setOnMouseEntered(event -> {
            overlay.setVisible(true);
        });
        
        card.setOnMouseExited(event -> {
            overlay.setVisible(false);
        });
        
        // 添加点击事件
        card.setOnMouseClicked(event -> showGameDetails(game));
        
        return card;
    }
    
    private void initializeTables() {
        // 初始化销售数据表格列
        initializeSalesTableColumns();
        
        // 异步加载销售数据
        loadSalesData();
    }
    
    private void initializeSalesTableColumns() {
        // 获取表格列并设置单元格值工厂
        TableColumn<SalesData, String> gameNameColumn = (TableColumn<SalesData, String>) salesTable.getColumns().get(0);
        TableColumn<SalesData, String> categoryColumn = (TableColumn<SalesData, String>) salesTable.getColumns().get(1);
        TableColumn<SalesData, String> priceColumn = (TableColumn<SalesData, String>) salesTable.getColumns().get(2);
        TableColumn<SalesData, Integer> salesVolumeColumn = (TableColumn<SalesData, Integer>) salesTable.getColumns().get(3);
        TableColumn<SalesData, Integer> visitorCountColumn = (TableColumn<SalesData, Integer>) salesTable.getColumns().get(4);
        TableColumn<SalesData, BigDecimal> salesAmountColumn = (TableColumn<SalesData, BigDecimal>) salesTable.getColumns().get(5);
        TableColumn<SalesData, BigDecimal> conversionRateColumn = (TableColumn<SalesData, BigDecimal>) salesTable.getColumns().get(6);
        TableColumn<SalesData, String> statusColumn = (TableColumn<SalesData, String>) salesTable.getColumns().get(7);
        
        // 设置单元格值工厂
        gameNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGameName()));
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrice()));
        salesVolumeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSalesVolume()).asObject());
        visitorCountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getVisitorCount()).asObject());
        salesAmountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSalesAmount()));
        conversionRateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getConversionRate()));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
    }
    
    private void loadSalesData() {
        new Thread(() -> {
            try {
                // 调用API获取销售数据
                String endpoint = "/vendors/query-game-sales";
                Map<String, Object> requestData = Map.of("account", currentUser.getAccount());
                
                Object response = apiClient.post(endpoint, requestData, Object.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    // 恢复按钮状态
                    refreshSalesDataButton.setDisable(false);
                    refreshSalesDataButton.setText("刷新");
                    
                    if (response instanceof List) {
                        List<Map<String, Object>> salesList = (List<Map<String, Object>>) response;
                        
                        salesData.clear();
                        
                        if (!salesList.isEmpty()) {
                            // 处理API返回的销售数据
                            for (Map<String, Object> saleData : salesList) {
                                String gameName = getStringValue(saleData, "gameName", "未知游戏");
                                String category = getStringValue(saleData, "category", "未知类别");
                                String price = getStringValue(saleData, "price", "免费");
                                int salesVolume = getIntValue(saleData, "salesVolume", 0);
                                int visitorCount = getIntValue(saleData, "visitorCount", 0);
                                BigDecimal salesAmount = getBigDecimalValue(saleData, "salesAmount", BigDecimal.ZERO);
                                BigDecimal conversionRate = getBigDecimalValue(saleData, "conversionRate", BigDecimal.ZERO);
                                String status = getStringValue(saleData, "status", "未知状态");
                                
                                SalesData salesItem = new SalesData(gameName, category, price, salesVolume, 
                                                                   visitorCount, salesAmount, conversionRate, status);
                                salesData.add(salesItem);
                            }
                        } else {
                            // 如果没有数据，添加一条提示信息
                            SalesData emptyData = new SalesData("暂无数据", "-", "-", 0, 0, 
                                                              BigDecimal.ZERO, BigDecimal.ZERO, "-");
                            salesData.add(emptyData);
                        }
                        
                        salesTable.setItems(salesData);
                    } else {
                        ControllerUtils.showErrorAlert("销售数据格式错误");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    // 恢复按钮状态
                    refreshSalesDataButton.setDisable(false);
                    refreshSalesDataButton.setText("刷新");
                    
                    ControllerUtils.showErrorAlert("加载销售数据失败: " + e.getMessage());
                    // 添加错误提示数据
                    salesData.clear();
                    SalesData errorData = new SalesData("加载失败", "-", "-", 0, 0, 
                                                      BigDecimal.ZERO, BigDecimal.ZERO, "-");
                    salesData.add(errorData);
                    salesTable.setItems(salesData);
                });
            }
        }).start();
    }
    
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private int getIntValue(Map<String, Object> data, String key, int defaultValue) {
        try {
            Object value = data.get(key);
            return value != null ? Integer.parseInt(value.toString()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private BigDecimal getBigDecimalValue(Map<String, Object> data, String key, BigDecimal defaultValue) {
        try {
            Object value = data.get(key);
            return value != null ? new BigDecimal(value.toString()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    private void setupEventHandlers() {
        // 刷新按钮事件
        refreshButton.setOnAction(event -> handleRefresh());
        
        // 销售数据刷新按钮事件
        refreshSalesDataButton.setOnAction(event -> handleRefreshSalesData());
        
        // 退出登录事件
        logoutButton.setOnAction(event -> handleLogout());
        
        // 个人信息保存事件
        saveProfileButton.setOnAction(event -> handleSaveProfile());
        
        // 游戏管理功能事件
        createGameButton.setOnAction(event -> handleCreateGame());
        editGameButton.setOnAction(event -> handleEditGame());
        publishGameButton.setOnAction(event -> handlePublishGame());
        unpublishGameButton.setOnAction(event -> handleUnpublishGame());
        viewApplicationsButton.setOnAction(event -> handleViewApplications());
    }
    
    @FXML
    private void handleRefreshSalesData() {
        // 设置按钮为加载状态
        refreshSalesDataButton.setDisable(true);
        refreshSalesDataButton.setText("刷新中...");
        
        // 重新加载销售数据
        loadSalesData();
        
        // 延迟恢复按钮状态（在loadSalesData的异步回调中处理）
        // 实际的恢复逻辑在loadSalesData方法的Platform.runLater中处理
    }
    
    // 选项卡切换方法
    @FXML
    private void showDashboard() {
        showContent(dashboardContent);
        setActiveTab(dashboardTab);
    }
    
    @FXML
    private void showGameManagement() {
        showContent(gameManagementContent);
        setActiveTab(gameManagementTab);
    }
    
    @FXML
    private void showSalesData() {
        showContent(salesDataContent);
        setActiveTab(salesDataTab);
    }
    
    @FXML
    private void showProfile() {
        showContent(profileContent);
        setActiveTab(profileTab);
        
        // 从API获取个人信息
        loadPersonalInfoFromAPI();
    }
    
    private void loadPersonalInfoFromAPI() {
        // 显示加载状态
        saveProfileButton.setDisable(true);
        saveProfileButton.setText("加载中...");
        
        new Thread(() -> {
            try {
                // 将account作为查询参数添加到GET请求
                String endpoint = "/vendors/personal-info?account=" + currentUser.getAccount();
                
                // 调用API获取个人信息
                Map<String, Object> personalInfo = apiClient.get(endpoint, Map.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (personalInfo != null) {
                        // 填充个人信息表单
                        accountLabel.setText(currentUser.getAccount());
                        companyLabel.setText(personalInfo.getOrDefault("companyName", "未设置").toString());
                        addressField.setText(personalInfo.getOrDefault("registeredAddress", "").toString());
                        contactField.setText(personalInfo.getOrDefault("contact", "").toString());
                        contactPersonField.setText(personalInfo.getOrDefault("contactPerson", "").toString());

                    } else {
                        ControllerUtils.showErrorAlert("获取个人信息失败");
                    }
                    
                    // 恢复按钮状态
                    saveProfileButton.setDisable(false);
                    saveProfileButton.setText("保存修改");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("获取个人信息失败: " + e.getMessage());
                    saveProfileButton.setDisable(false);
                    saveProfileButton.setText("保存修改");
                });
            }
        }).start();
    }
    
    private void showContent(VBox contentToShow) {
        // 隐藏所有内容区域
        dashboardContent.setVisible(false);
        dashboardContent.setManaged(false);
        gameManagementContent.setVisible(false);
        gameManagementContent.setManaged(false);
        salesDataContent.setVisible(false);
        salesDataContent.setManaged(false);
        profileContent.setVisible(false);
        profileContent.setManaged(false);
        
        // 显示选中的内容区域
        contentToShow.setVisible(true);
        contentToShow.setManaged(true);
    }
    
    private void setActiveTab(Button activeTab) {
        resetTabStyles();
        activeTab.getStyleClass().add("tab-active");
    }
    
    private void resetTabStyles() {
        dashboardTab.getStyleClass().remove("tab-active");
        gameManagementTab.getStyleClass().remove("tab-active");
        salesDataTab.getStyleClass().remove("tab-active");
        profileTab.getStyleClass().remove("tab-active");
    }
    
    // 事件处理方法
    @FXML
    private void handleLogout() {
        UserSession.getInstance().logout();
        ControllerUtils.switchScene(logoutButton, "/com/database/gametradefrontend/view/welcome.fxml", 
                                  "GameTrade - 欢迎", 1000, 800);
    }
    
    @FXML
    private void handleSaveProfile() {
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();
        String contactPerson = contactPersonField.getText().trim();
        // 验证输入
        if (address.isEmpty() && contact.isEmpty()&& contactPerson.isEmpty() ){
            ControllerUtils.showErrorAlert("请输入要修改的信息");
            return;
        }
        
        // 显示保存状态
        saveProfileButton.setDisable(true);
        saveProfileButton.setText("保存中...");
        
        new Thread(() -> {
            try {
                // 准备请求数据（包含所有必要字段）
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("contact", contact); // 对应后端的contact
                requestData.put("registeredAddress", address); // 对应后端的registeredAddress
                requestData.put("contactPerson", contactPerson); // 对应后端的contactPerson
                
                // 将account作为查询参数添加到URL
                String endpoint = "/vendors/personal-info?account=" + currentUser.getAccount();
                
                // 调用API保存个人信息
                String result = apiClient.put(endpoint, requestData, String.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    if (result != null) {
                        // 更新本地用户信息
                        currentUser.setAddress(address);
                        currentUser.setContact(contact);
                        currentUser.setContactPerson(contactPerson);

                    } else {
                        ControllerUtils.showErrorAlert("保存个人信息失败");
                    }
                    
                    // 恢复按钮状态
                    saveProfileButton.setDisable(false);
                    saveProfileButton.setText("保存修改");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("保存个人信息失败: " + e.getMessage());
                    saveProfileButton.setDisable(false);
                    saveProfileButton.setText("保存修改");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleCreateGame() {
        try {
            // 创建新窗口
            Stage gameCreationStage = new Stage();
            gameCreationStage.setTitle("GameTrade - 创建游戏");
            gameCreationStage.setWidth(800);
            gameCreationStage.setHeight(600);
            
            // 设置模态，但不阻塞主窗口
            gameCreationStage.initModality(Modality.WINDOW_MODAL);
            gameCreationStage.initOwner(createGameButton.getScene().getWindow());
            gameCreationStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/create-game.fxml"));
            Parent root = loader.load();
            
            // 设置场景
            Scene scene = new Scene(root);
            gameCreationStage.setScene(scene);
            
            // 显示窗口
            gameCreationStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开游戏创建页面失败: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditGame() {
        try {
            // 创建新窗口
            Stage editGamesStage = new Stage();
            editGamesStage.setTitle("GameTrade - 修改游戏");
            editGamesStage.setWidth(1200);
            editGamesStage.setHeight(800);
            
            // 设置模态，但不阻塞主窗口
            editGamesStage.initModality(Modality.WINDOW_MODAL);
            editGamesStage.initOwner(editGameButton.getScene().getWindow());
            editGamesStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            
            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/edit-games.fxml"));
            Parent root = loader.load();
            
            // 设置场景
            Scene scene = new Scene(root);
            editGamesStage.setScene(scene);
            
            // 显示窗口
            editGamesStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开游戏修改页面失败: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePublishGame() {
        try {
            // 创建新窗口
            Stage publishGameStage = new Stage();
            publishGameStage.setTitle("GameTrade - 上架申请");
            publishGameStage.setWidth(1200);
            publishGameStage.setHeight(800);
            
            // 设置模态，但不阻塞主窗口
            publishGameStage.initModality(Modality.WINDOW_MODAL);
            publishGameStage.initOwner(publishGameButton.getScene().getWindow());
            publishGameStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            
            // 加载FXML文件 - 创建一个新的FXML文件来显示上架申请结果
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/publish-games.fxml"));
            Parent root = loader.load();
            
            // 获取控制器实例
            PublishGamesController controller = loader.getController();
            
            // 传递必要的引用给新窗口的控制器
            controller.setApiClient(this.apiClient);
            controller.setCurrentUser(this.currentUser);
            
            // 设置场景
            Scene scene = new Scene(root);
            publishGameStage.setScene(scene);
            
            // 显示窗口
            publishGameStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开上架申请页面失败: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUnpublishGame() {
        try {
            // 创建新窗口
            Stage unpublishGameStage = new Stage();
            unpublishGameStage.setTitle("GameTrade - 游戏下架");
            unpublishGameStage.setWidth(1200);
            unpublishGameStage.setHeight(800);
            
            // 设置模态，但不阻塞主窗口
            unpublishGameStage.initModality(Modality.WINDOW_MODAL);
            unpublishGameStage.initOwner(unpublishGameButton.getScene().getWindow());
            unpublishGameStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            
            // 加载FXML文件 - 创建一个新的FXML文件来显示下架申请结果
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/unpublish-games.fxml"));
            Parent root = loader.load();
            
            // 获取控制器实例
            UnpublishGamesController controller = loader.getController();
            
            // 传递必要的引用给新窗口的控制器
            controller.setApiClient(this.apiClient);
            controller.setCurrentUser(this.currentUser);
            
            // 设置场景
            Scene scene = new Scene(root);
            unpublishGameStage.setScene(scene);
            
            // 显示窗口
            unpublishGameStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开下架申请页面失败: " + e.getMessage());
        }
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
        String downloadLink = downloadLinkFieldDetails.getText().trim();
        String licenseNumber = licenseNumberFieldDetails.getText().trim();
        
        // 验证必填字段
        if (category.isEmpty() || price.isEmpty()) {
            ControllerUtils.showErrorAlert("游戏类别和价格不能为空");
            return;
        }
        
        // 显示保存状态
        saveButton.setDisable(true);
        saveButton.setText("保存中...");
        
        new Thread(() -> {
            try {
                // 调用API更新游戏信息
                String endpoint = "/vendors/update-game";
                
                // 准备请求数据
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("account", currentUser.getAccount());
                requestData.put("gameName", currentEditingGame.getName());
                requestData.put("price", price);
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
                        
                        // 刷新主页面游戏数据
                        initializeGameCards();
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
    
    @FXML
    private void handleRefresh() {
        // 重新加载游戏数据
        initializeGameCards();
        ControllerUtils.showInfoAlert("游戏数据已刷新");
    }
    
    private void showGameDetails(Game game) {
        try {
            // 创建新窗口
            Stage gameDetailsStage = new Stage();
            gameDetailsStage.setTitle("GameTrade - 游戏详情");
            gameDetailsStage.setWidth(800);
            gameDetailsStage.setHeight(700);
            
            // 设置模态，但不阻塞主窗口
            gameDetailsStage.initModality(Modality.WINDOW_MODAL);
            gameDetailsStage.initOwner(gameCardsContainer.getScene().getWindow());
            gameDetailsStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            
            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/game-details.fxml"));
            Parent root = loader.load();
            
            // 获取控制器实例
            GameDetailsController controller = loader.getController();
            
            // 传递必要的引用给新窗口的控制器
            controller.setApiClient(this.apiClient);
            controller.setCurrentUser(this.currentUser);
            
            // 加载游戏详情数据
            controller.loadGameDetails(game);
            
            // 设置场景
            Scene scene = new Scene(root);
            gameDetailsStage.setScene(scene);
            
            // 显示窗口
            gameDetailsStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开游戏详情页面失败: " + e.getMessage());
        }
    }

    // 内部数据类
    public static class Game {
        private String name;
        private String category;
        private String price;
        private String image;
        private String description;
        private String status;
        private String downloadLink;
        private String licenseNumber;
        private String companyName;
        private String releaseTime;

        public Game(String name, String category, String price, String image, String description, String status) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.image = image;
            this.description = description;
            this.status = status;
        }
        
        public Game(String name, String category, String price, String image, String description, String status, 
                   String downloadLink, String licenseNumber, String companyName, String releaseTime) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.image = image;
            this.description = description;
            this.status = status;
            this.downloadLink = downloadLink;
            this.licenseNumber = licenseNumber;
            this.companyName = companyName;
            this.releaseTime = releaseTime;
        }
        
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getImage() { return image; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getDownloadLink() { return downloadLink; }
        public String getLicenseNumber() { return licenseNumber; }
        public String getCompanyName() { return companyName; }
        public String getReleaseTime() { return releaseTime; }
        
        public void setPrice(String price) { this.price = price; }
        public void setDescription(String description) { this.description = description; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
        public void setDownloadLink(String downloadLink) { this.downloadLink = downloadLink; }
        public void setCategory(String category) { this.category = category; }
    }
    
    public static class SalesData {
        private String gameName;
        private String category;
        private String price;
        private int salesVolume;
        private int visitorCount;
        private BigDecimal salesAmount;
        private BigDecimal conversionRate;
        private String status;
        
        public SalesData(String gameName, String category, String price, int salesVolume, 
                        int visitorCount, BigDecimal salesAmount, BigDecimal conversionRate, String status) {
            this.gameName = gameName;
            this.category = category;
            this.price = price;
            this.salesVolume = salesVolume;
            this.visitorCount = visitorCount;
            this.salesAmount = salesAmount;
            this.conversionRate = conversionRate;
            this.status = status;
        }
        
        public String getGameName() { return gameName; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public int getSalesVolume() { return salesVolume; }
        public int getVisitorCount() { return visitorCount; }
        public BigDecimal getSalesAmount() { return salesAmount; }
        public BigDecimal getConversionRate() { return conversionRate; }
        public String getStatus() { return status; }
    }
    
    // 上架申请数据类
    public static class ApplicationData {
        private String applicationId;
        private String gameName;
        private String companyName;
        private String approvalStatus;
        private String approvalResult;
        private String applicationTime;
        
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
    
    @FXML
    private void handleViewApplications() {
        try {
            // 创建新窗口
            Stage viewApplicationsStage = new Stage();
            viewApplicationsStage.setTitle("GameTrade - 上架申请信息");
            viewApplicationsStage.setWidth(1200);
            viewApplicationsStage.setHeight(800);
            
            // 设置模态，但不阻塞主窗口
            viewApplicationsStage.initModality(Modality.WINDOW_MODAL);
            viewApplicationsStage.initOwner(viewApplicationsButton.getScene().getWindow());
            viewApplicationsStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            
            // 加载FXML文件 - 创建一个新的FXML文件来显示上架申请信息
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/view-applications.fxml"));
            Parent root = loader.load();
            
            // 获取控制器实例
            ViewApplicationsController controller = loader.getController();
            
            // 传递必要的引用给新窗口的控制器
            controller.setApiClient(this.apiClient);
            controller.setCurrentUser(this.currentUser);
            
            // 设置场景
            Scene scene = new Scene(root);
            viewApplicationsStage.setScene(scene);
            
            // 显示窗口
            viewApplicationsStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开上架申请信息页面失败: " + e.getMessage());
        }
    }
}
