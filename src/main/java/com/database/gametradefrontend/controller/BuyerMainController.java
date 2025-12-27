package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.application.Platform;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 买家主页面控制器
 * 支持游戏查询、游戏库管理、订单管理等功能
 */
public class BuyerMainController {

    // FXML 组件注入
    @FXML private Label userInfoLabel;
    @FXML private Button logoutButton;
    
    // 选项卡按钮
    @FXML private Button gameStoreTab;
    @FXML private Button myGamesTab;
    @FXML private Button ordersTab;
    @FXML private Button profileTab;
    
    // 游戏商店页面组件
    @FXML private VBox gameStoreContent;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> popularityFilter;
    @FXML private Button preferenceFilter;
    private boolean isPreferenceFilterActive = false;
    @FXML private Button resetFilterButton;
    @FXML private FlowPane gameCardsContainer;
    
    // 我的游戏库页面组件
    @FXML private VBox myGamesContent;
    @FXML private Button refreshLibraryButton;
    @FXML private Button downloadAllButton;
    @FXML private Button updateAllButton;
    @FXML private TableView<LibraryGame> libraryTable;
    
    // 订单管理页面组件
    @FXML private VBox ordersContent;
    @FXML private Button refreshOrdersButton;
    @FXML private TableView<Order> ordersTable;
    
    // 个人信息页面组件
    @FXML private VBox profileContent;
    @FXML private Label accountLabel;
    @FXML private Label nicknameLabel;
    @FXML private Button saveProfileButton;
    
    // 新增的个人信息字段
    @FXML private ToggleGroup genderToggleGroup;
    @FXML private RadioButton maleRadioButton;
    @FXML private RadioButton femaleRadioButton;
    @FXML private DatePicker birthdayPicker;
    @FXML private Label contactLabel;
    
    // 当前用户信息
    private User currentUser;
    
    // API客户端
    private ApiClient apiClient;
    
    // 数据集合
    private final List<Game> games = new ArrayList<>();
    private final ObservableList<LibraryGame> libraryGames = FXCollections.observableArrayList();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    
    /**
     * 游戏数据类
     */
    public static class Game {
        private final String name;
        private final String category;
        private final String price;
        private final String image;
        private final String description;
        private final String rating;
        private final String popularity;
        private final String companyName;
        
        public Game(String name, String category, String price, String image, 
                   String description, String rating, String popularity, String companyName) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.image = image;
            this.description = (description != null) ? description : "暂无简介";
            this.rating = rating;
            this.popularity = popularity;
            this.companyName = companyName;
        }
        
        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getImage() { return image; }
        public String getDescription() { return description; }
        public String getRating() { return rating; }
        public String getPopularity() { return popularity; }
        public String getCompanyName() { return companyName; }
    }
    
    /**
     * 游戏库游戏数据类
     */
    public static class LibraryGame {
        private final String gameName;
        private final String licenseNumber;
        private final String score;
        private final String comment;
        private final String reviewTime;
        
        public LibraryGame(String gameName, String licenseNumber, String score, String comment, String reviewTime) {
            this.gameName = gameName;
            this.licenseNumber = licenseNumber;
            this.score = score;
            this.comment = comment;
            this.reviewTime = reviewTime;
        }
        
        // Getters
        public String getGameName() { return gameName; }
        public String getLicenseNumber() { return licenseNumber; }
        public String getScore() { return score; }
        public String getComment() { return comment; }
        public String getReviewTime() { return reviewTime; }
    }
    
    /**
     * 订单数据类
     */
    public static class Order {
        private final String orderId;
        private final String buyerNickname;
        private final String gameName;
        private final String category;
        private final String price;
        private final String orderTime;
        private final String paymentTime;
        private final String orderStatus;
        
        public Order(String orderId, String buyerNickname, String gameName, String category, 
                    String price, String orderTime, String paymentTime, String orderStatus) {
            this.orderId = orderId;
            this.buyerNickname = buyerNickname;
            this.gameName = gameName;
            this.category = category;
            this.price = price;
            this.orderTime = orderTime;
            this.paymentTime = paymentTime;
            this.orderStatus = orderStatus;
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public String getBuyerNickname() { return buyerNickname; }
        public String getGameName() { return gameName; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getOrderTime() { return orderTime; }
        public String getPaymentTime() { return paymentTime; }
        public String getOrderStatus() { return orderStatus; }
    }
    
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
        
        // 初始化性别选择组
        genderToggleGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderToggleGroup);
        femaleRadioButton.setToggleGroup(genderToggleGroup);
        
        // 初始化用户界面
        initializeUserInfo();
        initializeTabs();
        initializeFilters();
        initializeTables();
        setupEventHandlers();
        
        // 一进入页面就加载个人信息
        loadPersonalInfo();
        
        // 默认显示游戏商店页面
        showGameStore();
    }
    
    private void initializeUserInfo() {
        userInfoLabel.setText("买家用户 - " + currentUser.getAccount());
        accountLabel.setText(currentUser.getAccount());
        nicknameLabel.setText(currentUser.getNickname() != null ? currentUser.getNickname() : "");
    }
    
    private void initializeTabs() {
        // 设置选项卡样式
        resetTabStyles();
        gameStoreTab.getStyleClass().add("tab-active");
    }
    
    private void initializeFilters() {
        // 初始化筛选器选项
        categoryFilter.getItems().addAll("全部", "动作", "角色扮演", "策略", "射击", "体育", "模拟", "冒险", "益智");
        popularityFilter.getItems().addAll("全部", "0以上", "100以上", "1000以上", "10000以上", "100000以上", "1000000以上");
        
        // 设置偏好筛选器按钮文本
        preferenceFilter.setText("我的偏好");
    }
    
    private void initializeTables() {
        // 初始化订单表格列绑定
        if (ordersTable != null) {
            // 获取表格列
            TableColumn<Order, String> orderIdColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(0);
            TableColumn<Order, String> buyerNicknameColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(1);
            TableColumn<Order, String> gameNameColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(2);
            TableColumn<Order, String> categoryColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(3);
            TableColumn<Order, String> priceColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(4);
            TableColumn<Order, String> orderTimeColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(5);
            TableColumn<Order, String> paymentTimeColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(6);
            TableColumn<Order, String> orderStatusColumn = (TableColumn<Order, String>) ordersTable.getColumns().get(7);
            TableColumn<Order, Void> actionColumn = (TableColumn<Order, Void>) ordersTable.getColumns().get(8);
            
            // 设置单元格值工厂
            orderIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderId()));
            buyerNicknameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBuyerNickname()));
            gameNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGameName()));
            categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
            priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrice()));
            orderTimeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderTime()));
            paymentTimeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentTime()));
            orderStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderStatus()));
            
            // 设置操作列的自定义单元格
            actionColumn.setCellFactory(param -> new TableCell<Order, Void>() {
                private final Button payButton = new Button("💰");
                private final Button cancelButton = new Button("❌");
                private final HBox buttonsContainer = new HBox(3, payButton, cancelButton);
                
                {
                    buttonsContainer.setAlignment(javafx.geometry.Pos.CENTER);
                    
                    // 设置按钮样式
                    payButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");
                    cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");
                    
                    // 添加工具提示
                    Tooltip payTooltip = new Tooltip("支付订单");
                    Tooltip cancelTooltip = new Tooltip("取消订单");
                    payButton.setTooltip(payTooltip);
                    cancelButton.setTooltip(cancelTooltip);
                    
                    payButton.setOnAction(event -> {
                        Order order = getTableView().getItems().get(getIndex());
                        handlePayOrder(order);
                    });
                    
                    cancelButton.setOnAction(event -> {
                        Order order = getTableView().getItems().get(getIndex());
                        handleCancelOrder(order);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Order order = getTableView().getItems().get(getIndex());
                        String status = order.getOrderStatus();
                        
                        // 根据订单状态设置按钮可见性
                        if ("待支付".equals(status)) {
                            payButton.setDisable(false);
                            cancelButton.setDisable(false);
                            payButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");
                            cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");
                        } else {
                            payButton.setDisable(true);
                            cancelButton.setDisable(true);
                            payButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");
                            cancelButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");
                        }
                        
                        setGraphic(buttonsContainer);
                    }
                }
            });
        }
        
        // 初始化游戏库表格列绑定
        if (libraryTable != null) {
            // 获取表格列
            TableColumn<LibraryGame, String> gameNameColumn = (TableColumn<LibraryGame, String>) libraryTable.getColumns().get(0);
            TableColumn<LibraryGame, String> licenseNumberColumn = (TableColumn<LibraryGame, String>) libraryTable.getColumns().get(1);
            TableColumn<LibraryGame, String> scoreColumn = (TableColumn<LibraryGame, String>) libraryTable.getColumns().get(2);
            TableColumn<LibraryGame, String> commentColumn = (TableColumn<LibraryGame, String>) libraryTable.getColumns().get(3);
            TableColumn<LibraryGame, String> reviewTimeColumn = (TableColumn<LibraryGame, String>) libraryTable.getColumns().get(4);
            
            // 设置单元格值工厂
            gameNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGameName()));
            licenseNumberColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLicenseNumber()));
            scoreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScore()));
            commentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getComment()));
            reviewTimeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReviewTime()));
        }
    }
    
    private void setupEventHandlers() {
        // 设置分类筛选器事件处理器
        categoryFilter.setOnAction(event -> handleCategoryFilter());
        
        // 设置热度筛选器事件处理器
        popularityFilter.setOnAction(event -> handlePopularityFilter());
        
        // 设置偏好筛选器按钮事件处理器
        preferenceFilter.setOnAction(event -> handlePreferenceFilter());
    }
    
    private void handlePopularityFilter() {
        String selectedPopularity = popularityFilter.getValue();
        if (selectedPopularity == null || "全部".equals(selectedPopularity)) {
            // 如果选择全部或未选择，加载所有游戏数据
            loadGameStoreData();
            return;
        }
        
        // 根据选项确定minPopularity参数值
        String minPopularity = switch (selectedPopularity) {
            case "100以上" -> "100";
            case "1000以上" -> "1000";
            case "10000以上" -> "10000";
            case "100000以上" -> "100000";
            case "1000000以上" -> "1000000";
            default -> "0";
        };

        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在按热度搜索游戏...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步调用API按热度搜索游戏
        new Thread(() -> {
            try {
                // 调用API按热度搜索游戏，传递minPopularity参数
                String endpoint = "/buyers/games/search-by-popularity?minPopularity=" + minPopularity;
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    
                    if (response instanceof List) {
                        List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                        
                        if (!gameList.isEmpty()) {
                            for (Map<String, Object> gameData : gameList) {
                                // 解析游戏数据
                                String gameName = gameData.getOrDefault("gameName", "未知游戏").toString();
                                String category = gameData.getOrDefault("category", "未知类别").toString();
                                String price = gameData.getOrDefault("price", "免费").toString();
                                String score = gameData.getOrDefault("score", "0").toString();
                                String salesVolume = gameData.getOrDefault("salesVolume", "0").toString();
                                String companyName = gameData.getOrDefault("companyName", "未知厂商").toString();
                                Object descriptionObj = gameData.get("description");
                                String description = (descriptionObj != null) ? descriptionObj.toString() : "暂无简介";
                                
                                // 使用默认图片
                                String image = "yuanshen.png";
                                
                                // 创建游戏对象（使用现有的Game类，包含description字段）
                                Game game = new Game(gameName, category, price, image, 
                                                   description, score, salesVolume, companyName);
                                
                                // 创建游戏卡片
                                StackPane gameCard = createGameCard(game);
                                gameCardsContainer.getChildren().add(gameCard);
                            }
                        } else {
                            Label noDataLabel = new Label("该热度范围内暂无游戏数据");
                            noDataLabel.getStyleClass().add("no-data-label");
                            gameCardsContainer.getChildren().add(noDataLabel);
                        }
                    } else {
                        Label errorLabel = new Label("按热度搜索失败：返回数据格式错误");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    Label errorLabel = new Label("按热度搜索游戏失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    gameCardsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void handleCategoryFilter() {
        String selectedCategory = categoryFilter.getValue();
        if (selectedCategory == null || "全部".equals(selectedCategory)) {
            // 如果选择全部或未选择，加载所有游戏数据
            loadGameStoreData();
            return;
        }
        
        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在按分类搜索游戏...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步调用API按分类搜索游戏
        new Thread(() -> {
            try {
                // 调用API按分类搜索游戏，传递category参数（需要URL编码）
                String encodedCategory = URLEncoder.encode(selectedCategory, StandardCharsets.UTF_8);
                String endpoint = "/buyers/games/search-by-category?category=" + encodedCategory;
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    
                    if (response instanceof List) {
                        List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                        
                        if (!gameList.isEmpty()) {
                            for (Map<String, Object> gameData : gameList) {
                                // 解析游戏数据
                                String gameName = gameData.getOrDefault("gameName", "未知游戏").toString();
                                String category = gameData.getOrDefault("category", "未知类别").toString();
                                String price = gameData.getOrDefault("price", "免费").toString();
                                String score = gameData.getOrDefault("score", "0").toString();
                                String salesVolume = gameData.getOrDefault("salesVolume", "0").toString();
                                String companyName = gameData.getOrDefault("companyName", "未知厂商").toString();
                                Object descriptionObj = gameData.get("description");
                                String description = (descriptionObj != null) ? descriptionObj.toString() : "暂无简介";
                                
                                // 使用默认图片
                                String image = "yuanshen.png";
                                
                                // 创建游戏对象（使用现有的Game类，包含description字段）
                                Game game = new Game(gameName, category, price, image, 
                                                   description, score, salesVolume, companyName);
                                
                                // 创建游戏卡片
                                StackPane gameCard = createGameCard(game);
                                gameCardsContainer.getChildren().add(gameCard);
                            }
                        } else {
                            Label noDataLabel = new Label("该分类下暂无游戏数据");
                            noDataLabel.getStyleClass().add("no-data-label");
                            gameCardsContainer.getChildren().add(noDataLabel);
                        }
                    } else {
                        Label errorLabel = new Label("按分类搜索失败：返回数据格式错误");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    Label errorLabel = new Label("按分类搜索游戏失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    gameCardsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void handlePreferenceFilter() {
        // 切换按钮状态
        isPreferenceFilterActive = !isPreferenceFilterActive;
        
        // 更新按钮样式
        if (isPreferenceFilterActive) {
            preferenceFilter.getStyleClass().add("filter-active");
            preferenceFilter.setText("我的偏好 ✓");
        } else {
            preferenceFilter.getStyleClass().remove("filter-active");
            preferenceFilter.setText("我的偏好");
            // 如果取消偏好筛选，加载所有游戏数据
            loadGameStoreData();
            return;
        }
        
        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在按偏好搜索游戏...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步调用API按偏好搜索游戏
        new Thread(() -> {
            try {
                if (currentUser.getNickname() == null || currentUser.getNickname().trim().isEmpty()) {
                    Platform.runLater(() -> {
                        gameCardsContainer.getChildren().clear();
                        Label errorLabel = new Label("用户昵称为空，无法进行偏好搜索");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                        
                        // 出错时重置按钮状态
                        isPreferenceFilterActive = false;
                        preferenceFilter.getStyleClass().remove("filter-active");
                        preferenceFilter.setText("我的偏好");
                    });
                    return;
                }
                
                // 调用API按偏好搜索游戏，传递buyerNickname参数
                String endpoint = "/buyers/games/search-by-preference?buyerNickname=" + 
                    URLEncoder.encode(currentUser.getNickname(), StandardCharsets.UTF_8);
                
                // 调试信息：使用util包风格打印请求URL
                System.err.println("DEBUG: API endpoint: " + endpoint);
                
                Object response = apiClient.get(endpoint, Object.class);

                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    
                    if (response == null) {
                        Label errorLabel = new Label("你还未关注任何游戏, 无法进行偏好搜索");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                        
                        // 出错时重置按钮状态
                        isPreferenceFilterActive = false;
                        preferenceFilter.getStyleClass().remove("filter-active");
                        preferenceFilter.setText("我的偏好");
                    } else if (response instanceof List) {
                        List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                        
                        if (!gameList.isEmpty()) {
                            for (Map<String, Object> gameData : gameList) {
                                // 解析游戏数据
                                String gameName = gameData.getOrDefault("gameName", "未知游戏").toString();
                                String category = gameData.getOrDefault("category", "未知类别").toString();
                                String price = gameData.getOrDefault("price", "免费").toString();
                                String score = gameData.getOrDefault("score", "0").toString();
                                String salesVolume = gameData.getOrDefault("salesVolume", "0").toString();
                                String companyName = gameData.getOrDefault("companyName", "未知厂商").toString();
                                Object descriptionObj = gameData.get("description");
                                String description = (descriptionObj != null) ? descriptionObj.toString() : "暂无简介";
                                
                                // 使用默认图片
                                String image = "yuanshen.png";
                                
                                // 创建游戏对象（使用现有的Game类，包含description字段）
                                Game game = new Game(gameName, category, price, image, 
                                                   description, score, salesVolume, companyName);
                                
                                // 创建游戏卡片
                                StackPane gameCard = createGameCard(game);
                                gameCardsContainer.getChildren().add(gameCard);
                            }
                        } else {
                            Label noDataLabel = new Label("暂无偏好推荐游戏");
                            noDataLabel.getStyleClass().add("no-data-label");
                            gameCardsContainer.getChildren().add(noDataLabel);
                        }
                    } else {
                        Label errorLabel = new Label("按偏好搜索失败：返回数据格式错误");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                        
                        // 出错时重置按钮状态
                        isPreferenceFilterActive = false;
                        preferenceFilter.getStyleClass().remove("filter-active");
                        preferenceFilter.setText("我的偏好");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    Label errorLabel = new Label("按偏好搜索游戏失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    gameCardsContainer.getChildren().add(errorLabel);
                    
                    // 出错时重置按钮状态
                    isPreferenceFilterActive = false;
                    preferenceFilter.getStyleClass().remove("filter-active");
                    preferenceFilter.setText("我的偏好");
                });
            }
        }).start();
    }
    
    private void resetTabStyles() {
        gameStoreTab.getStyleClass().remove("tab-active");
        myGamesTab.getStyleClass().remove("tab-active");
        ordersTab.getStyleClass().remove("tab-active");
        profileTab.getStyleClass().remove("tab-active");
    }
    
    private void hideAllContent() {
        gameStoreContent.setVisible(false);
        gameStoreContent.setManaged(false);
        myGamesContent.setVisible(false);
        myGamesContent.setManaged(false);
        ordersContent.setVisible(false);
        ordersContent.setManaged(false);
        profileContent.setVisible(false);
        profileContent.setManaged(false);
    }
    
    // 选项卡切换方法
    @FXML
    private void showGameStore() {
        resetTabStyles();
        hideAllContent();
        gameStoreTab.getStyleClass().add("tab-active");
        gameStoreContent.setVisible(true);
        gameStoreContent.setManaged(true);
        
        // 加载游戏数据
        loadGameStoreData();
    }
    
    @FXML
    private void showMyGames() {
        resetTabStyles();
        hideAllContent();
        myGamesTab.getStyleClass().add("tab-active");
        myGamesContent.setVisible(true);
        myGamesContent.setManaged(true);
        
        // 加载游戏库数据
        loadLibraryData();
    }
    
    @FXML
    private void showOrders() {
        resetTabStyles();
        hideAllContent();
        ordersTab.getStyleClass().add("tab-active");
        ordersContent.setVisible(true);
        ordersContent.setManaged(true);
        
        // 加载订单数据
        loadOrdersData();
    }
    
    @FXML
    private void showProfile() {
        resetTabStyles();
        hideAllContent();
        profileTab.getStyleClass().add("tab-active");
        profileContent.setVisible(true);
        profileContent.setManaged(true);
        
        // 从API加载个人信息
        loadPersonalInfo();
    }
    
    // 游戏商店功能
    private void loadGameStoreData() {
        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在加载游戏数据...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步从API获取游戏数据（使用搜索API，gameName参数为空）
        new Thread(() -> {
            try {
                // 调用搜索API获取所有游戏数据，gameName参数为空
                String endpoint = "/buyers/games/search-by-name?gameName=";
                Object response = apiClient.get(endpoint, Object.class);
                System.out.println(response.toString());
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    
                    if (response instanceof List) {
                        List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                        
                        if (!gameList.isEmpty()) {
                            games.clear();
                            
                            for (Map<String, Object> gameData : gameList) {
                                // 解析游戏数据（使用搜索API返回的字段）
                                String gameName = Objects.toString(gameData.get("gameName"), "未知游戏");
                                String category = Objects.toString(gameData.get("category"), "未知类别");
                                String price = Objects.toString(gameData.get("price"), "免费");
                                String score = Objects.toString(gameData.get("score"), "0");
                                String salesVolume = Objects.toString(gameData.get("salesVolume"), "0");
                                String companyName = Objects.toString(gameData.get("companyName"), "未知厂商");
                                String description = Objects.toString(gameData.get("description"), "暂无简介");
                                
                                // 使用默认图片
                                String image = "yuanshen.png";
                                
                                // 创建游戏对象（使用现有的Game类，包含description字段）
                                Game game = new Game(gameName, category, price, image, 
                                                   description, score, salesVolume, companyName);
                                
                                // 创建游戏卡片
                                StackPane gameCard = createGameCard(game);
                                gameCardsContainer.getChildren().add(gameCard);
                            }
                        } else {
                            Label noDataLabel = new Label("暂无游戏数据");
                            noDataLabel.getStyleClass().add("no-data-label");
                            gameCardsContainer.getChildren().add(noDataLabel);
                        }
                    } else {
                        Label errorLabel = new Label("加载游戏数据失败：返回数据格式错误");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    Label errorLabel = new Label("加载游戏数据失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    gameCardsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private StackPane createGameCard(Game game) {
        StackPane card = new StackPane();
        card.getStyleClass().add("game-card");
        
        // 创建主内容区域
        VBox content = new VBox();
        content.getStyleClass().add("game-card-content");
        
        // 游戏图片
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/" + game.getImage())));
            imageView.setImage(image);
        } catch (Exception e) {
            try {
                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png")));
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                imageView.setStyle("-fx-background-color: #667eea; -fx-min-width: 250px; -fx-min-height: 150px;");
            }
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("game-card-image");
        
        // 游戏信息
        Label titleLabel = new Label(game.getName());
        titleLabel.getStyleClass().add("game-card-title");
        
        Label categoryLabel = new Label("类别: " + game.getCategory());
        categoryLabel.getStyleClass().add("game-card-category");
        
        Label priceLabel = new Label("价格: " + game.getPrice());
        priceLabel.getStyleClass().add("game-card-price");
        
        Label ratingLabel = new Label("评分: " + game.getRating() + "⭐");
        ratingLabel.getStyleClass().add("game-card-rating");
        
        // 添加销量信息（如果popularity字段包含销量数据）
        Label salesLabel = new Label("销量: " + game.getPopularity());
        salesLabel.getStyleClass().add("game-card-sales");
        
        // 添加厂商名称
        Label companyLabel = new Label("厂商: " + game.getCompanyName());
        companyLabel.getStyleClass().add("game-card-company");
        
        // 添加游戏描述（最多显示50个字符，多余用...表示）
        String description = game.getDescription();
        if (description == null) {
            description = "暂无简介";
        } else if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("game-card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200);
        
        // 添加到内容区域
        content.getChildren().addAll(imageView, titleLabel, categoryLabel, priceLabel, ratingLabel, salesLabel, companyLabel, descriptionLabel);
        
        // 悬停覆盖层
        VBox overlay = new VBox();
        overlay.getStyleClass().add("game-card-overlay");
        overlay.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label overlayText = new Label("点击查看详情");
        overlayText.getStyleClass().add("overlay-text");
        overlay.getChildren().add(overlayText);
        
        // 点击事件 - 打开游戏详情页面
        card.setOnMouseClicked(event -> openGameDetails(game));
        
        card.getChildren().addAll(content, overlay);
        return card;
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            // 如果搜索框为空，重新加载所有游戏数据
            loadGameStoreData();
            return;
        }
        
        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在搜索游戏...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步调用API搜索游戏
        new Thread(() -> {
            try {
                // 调用API搜索游戏，传递gameName参数
                String endpoint = "/buyers/games/search-by-name?gameName=" + searchText;
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    
                    if (response instanceof List) {
                        List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                        
                        if (!gameList.isEmpty()) {
                            for (Map<String, Object> gameData : gameList) {
                                // 解析游戏数据
                                String gameName = gameData.getOrDefault("gameName", "未知游戏").toString();
                                String category = gameData.getOrDefault("category", "未知类别").toString();
                                String price = gameData.getOrDefault("price", "免费").toString();
                                String score = gameData.getOrDefault("score", "0").toString();
                                String salesVolume = gameData.getOrDefault("salesVolume", "0").toString();
                                String companyName = gameData.getOrDefault("companyName", "未知厂商").toString();
                                String description = gameData.getOrDefault("description", "暂无简介").toString();
                                
                                // 使用默认图片
                                String image = "yuanshen.png";
                                
                                // 创建游戏对象（使用现有的Game类，包含description字段）
                                Game game = new Game(gameName, category, price, image, 
                                                   description, score, salesVolume, companyName);
                                
                                // 创建游戏卡片
                                StackPane gameCard = createGameCard(game);
                                gameCardsContainer.getChildren().add(gameCard);
                            }
                        } else {
                            Label noDataLabel = new Label("未找到相关游戏");
                            noDataLabel.getStyleClass().add("no-data-label");
                            gameCardsContainer.getChildren().add(noDataLabel);
                        }
                    } else {
                        Label errorLabel = new Label("搜索失败：返回数据格式错误");
                        errorLabel.getStyleClass().add("error-label");
                        gameCardsContainer.getChildren().add(errorLabel);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    Label errorLabel = new Label("搜索游戏失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    gameCardsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    @FXML
    private void handleResetFilter() {
        searchField.clear();
        categoryFilter.getSelectionModel().select("全部");
        popularityFilter.getSelectionModel().select("全部");
        
        // 重置偏好筛选器按钮状态
        isPreferenceFilterActive = false;
        preferenceFilter.getStyleClass().remove("filter-active");
        preferenceFilter.setText("我的偏好");
        
        loadGameStoreData();
    }
    
    private void filterGames() {
        // 实现游戏筛选逻辑
        // 这里可以根据搜索条件和筛选条件过滤游戏列表
    }
    
    // 安全转换为字符串，处理null值
    private String safeToString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    // 游戏库功能
    private void loadLibraryData() {
        if (currentUser == null || currentUser.getNickname() == null) {
            ControllerUtils.showErrorAlert("无法加载游戏库：用户信息不完整");
            return;
        }
        
        // 显示加载状态
        libraryGames.clear();
        libraryTable.setItems(libraryGames);
        
        // 异步调用API获取游戏库数据
        new Thread(() -> {
            try {
                // 调用API获取游戏库数据，传递buyerNickname参数
                String endpoint = "/buyers/game-library?buyerNickname=" + 
                    java.net.URLEncoder.encode(currentUser.getNickname(), StandardCharsets.UTF_8);
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    if (response instanceof List) {
                        List<Map<String, Object>> libraryList = (List<Map<String, Object>>) response;
                        
                        if (!libraryList.isEmpty()) {
                            for (Map<String, Object> libraryData : libraryList) {
                                // 解析游戏库数据
                                String gameName = safeToString(libraryData.get("gameName"), "未知游戏");
                                String licenseNumber = safeToString(libraryData.get("licenseNumber"), "未知");
                                String score = safeToString(libraryData.get("score"), "0");
                                String comment = safeToString(libraryData.get("comment"), "暂无评论");
                                String reviewTime = safeToString(libraryData.get("reviewTime"), "未知时间");
                                
                                // 创建游戏库对象
                                LibraryGame libraryGame = new LibraryGame(gameName, licenseNumber, score, comment, reviewTime);
                                libraryGames.add(libraryGame);
                            }
                        } else {
                            ControllerUtils.showInfoAlert("游戏库为空");
                        }
                    } else {
                        ControllerUtils.showErrorAlert("获取游戏库数据失败：返回数据格式错误");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> ControllerUtils.showErrorAlert("获取游戏库数据失败: " + e.getMessage()));
            }
        }).start();
    }
    
    @FXML
    private void handleRefreshLibrary() {
        loadLibraryData();
        ControllerUtils.showInfoAlert("游戏库已刷新");
    }
    
    @FXML
    private void handleDownloadAll() {
        // 实现批量下载逻辑
        ControllerUtils.showInfoAlert("开始批量下载选中的游戏");
    }
    
    @FXML
    private void handleUpdateAll() {
        // 实现批量更新逻辑
        ControllerUtils.showInfoAlert("开始批量更新选中的游戏");
    }
    
    // 订单管理功能
    private void loadOrdersData() {
        if (currentUser == null || currentUser.getNickname() == null) {
            ControllerUtils.showErrorAlert("无法加载订单：用户信息不完整");
            return;
        }
        
        // 显示加载状态
        orders.clear();
        ordersTable.setItems(orders);
        
        // 异步调用API获取订单数据
        new Thread(() -> {
            try {
                // 调用API获取订单数据，传递buyerNickname参数
                String endpoint = "/buyers/orders?buyerNickname=" + 
                    java.net.URLEncoder.encode(currentUser.getNickname(), StandardCharsets.UTF_8);
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    if (response instanceof List) {
                        List<Map<String, Object>> orderList = (List<Map<String, Object>>) response;
                        
                        if (!orderList.isEmpty()) {
                            for (Map<String, Object> orderData : orderList) {
                                // 解析订单数据
                                String orderId = safeToString(orderData.get("orderId"), "未知");
                                String buyerNickname = safeToString(orderData.get("buyerNickname"), currentUser.getNickname());
                                String gameName = safeToString(orderData.get("gameName"), "未知游戏");
                                String category = safeToString(orderData.get("category"), "未知类别");
                                String price = safeToString(orderData.get("price"), "免费");
                                String orderTime = safeToString(orderData.get("orderTime"), "未知时间");
                                String paymentTime = safeToString(orderData.get("paymentTime"), "未支付");
                                String orderStatus = safeToString(orderData.get("orderStatus"), "未知状态");
                                
                                // 创建订单对象
                                Order order = new Order(orderId, buyerNickname, gameName, category, 
                                                       price, orderTime, paymentTime, orderStatus);
                                orders.add(order);
                            }
                        } else {
                            ControllerUtils.showInfoAlert("暂无订单数据");
                        }
                    } else {
                        ControllerUtils.showErrorAlert("获取订单数据失败：返回数据格式错误");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> ControllerUtils.showErrorAlert("获取订单数据失败: " + e.getMessage()));
            }
        }).start();
    }
    
    @FXML
    private void handleRefreshOrders() {
        loadOrdersData();
        ControllerUtils.showInfoAlert("订单列表已刷新");
    }
    

    
    // 支付订单
    private void handlePayOrder(Order order) {
        if (order == null || order.getOrderId() == null) {
            ControllerUtils.showErrorAlert("无法支付：订单信息不完整");
            return;
        }
        
        // 异步调用API支付订单
        new Thread(() -> {
            try {
                // 调用API支付订单
                String endpoint = "/buyers/orders/pay?orderId=" + 
                    java.net.URLEncoder.encode(order.getOrderId(), StandardCharsets.UTF_8);
                String apiResponse = apiClient.put(endpoint, new HashMap<>(), String.class);
                
                // 在主线程中显示返回消息并刷新订单列表
                Platform.runLater(() -> {
                    ControllerUtils.showInfoAlert("支付订单结果: " + apiResponse);
                    loadOrdersData(); // 刷新订单列表
                });
                
            } catch (Exception e) {
                // 在主线程中显示错误信息
                Platform.runLater(() -> ControllerUtils.showErrorAlert("支付订单失败: " + e.getMessage()));
            }
        }).start();
    }
    
    // 取消订单
    private void handleCancelOrder(Order order) {
        if (order == null || order.getOrderId() == null) {
            ControllerUtils.showErrorAlert("无法取消：订单信息不完整");
            return;
        }
        
        // 确认对话框
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("确认取消");
        confirmation.setHeaderText("确认取消订单？");
        confirmation.setContentText("订单号: " + order.getOrderId() + "\n游戏: " + order.getGameName());
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 异步调用API取消订单
                new Thread(() -> {
                    try {
                        // 调用API取消订单
                        String endpoint = "/buyers/orders/cancel?orderId=" + 
                            java.net.URLEncoder.encode(order.getOrderId(), StandardCharsets.UTF_8);
                        String apiResponse = apiClient.put(endpoint, new HashMap<>(), String.class);
                        
                        // 在主线程中显示返回消息并刷新订单列表
                        Platform.runLater(() -> {
                            ControllerUtils.showInfoAlert("取消订单结果: " + apiResponse);
                            loadOrdersData(); // 刷新订单列表
                        });
                        
                    } catch (Exception e) {
                        // 在主线程中显示错误信息
                        Platform.runLater(() -> ControllerUtils.showErrorAlert("取消订单失败: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }
    
    // 个人信息功能
    private void loadPersonalInfo() {
        // 异步从API获取个人信息
        new Thread(() -> {
            try {
                // 调用API获取个人信息，传递account参数
                String endpoint = "/buyers/personal-info?account=" + currentUser.getAccount();
                
                // 调试信息：打印请求URL
                System.err.println("DEBUG: Personal info API endpoint: " + endpoint);
                
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    if (response instanceof Map) {
                        Map<String, Object> personalInfo = (Map<String, Object>) response;
                        
                        // 更新UI字段
                        String nickname = personalInfo.getOrDefault("nickname", "").toString();
                        String account = personalInfo.getOrDefault("account", "").toString();
                        String gender = personalInfo.getOrDefault("gender", "").toString();
                        String birthday = personalInfo.getOrDefault("birthday", "").toString();
                        String contact = personalInfo.getOrDefault("contact", "").toString();
                        
                        nicknameLabel.setText(nickname);
                        accountLabel.setText(account);
                        
                        // 更新UserSession中的用户昵称信息
                        currentUser.setNickname(nickname);
                        
                        // 设置性别选择
                        if ("男".equals(gender)) {
                            maleRadioButton.setSelected(true);
                        } else if ("女".equals(gender)) {
                            femaleRadioButton.setSelected(true);
                        }
                        
                        // 设置生日（需要解析日期格式）
                        if (!birthday.isEmpty()) {
                            try {
                                java.time.LocalDate birthDate = java.time.LocalDate.parse(birthday);
                                birthdayPicker.setValue(birthDate);
                            } catch (Exception e) {
                                // 如果日期格式解析失败，保持为空
                            }
                        }
                        
                        // 设置联系方式（只读）
                        contactLabel.setText(contact);
                        
                    } else {
                        ControllerUtils.showErrorAlert("获取个人信息失败：返回数据格式错误");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> ControllerUtils.showErrorAlert("获取个人信息失败: " + e.getMessage()));
            }
        }).start();
    }
    
    @FXML
    private void handleSaveProfile() {
        // 实现保存个人信息逻辑
        String gender = maleRadioButton.isSelected() ? "男" : (femaleRadioButton.isSelected() ? "女" : "");
        String birthday = birthdayPicker.getValue() != null ? birthdayPicker.getValue().toString() : "";
        
        // 异步保存个人信息到后端
        new Thread(() -> {
            try {
                // 构建请求参数
                String endpoint = "/buyers/personal-info?account=" + currentUser.getAccount();
                
                // 构建请求体（只包含可以修改的字段）
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("gender", gender);
                requestBody.put("birthday", birthday);
                
                // 调用API保存个人信息
                String response = apiClient.post(endpoint, requestBody, String.class);
                
                // 在主线程中显示结果
                Platform.runLater(() -> ControllerUtils.showInfoAlert("个人信息保存成功: " + response));
                
            } catch (Exception e) {
                // 在主线程中显示错误信息
                Platform.runLater(() -> ControllerUtils.showErrorAlert("保存个人信息失败: " + e.getMessage()));
            }
        }).start();
    }
    
    // 游戏详情页面
    private void openGameDetails(Game game) {
        try {
            Stage gameDetailsStage = new Stage();
            gameDetailsStage.setTitle("GameTrade - 游戏详情");
            gameDetailsStage.setWidth(900);
            gameDetailsStage.setHeight(700);
            
            gameDetailsStage.initModality(Modality.WINDOW_MODAL);
            gameDetailsStage.initOwner(gameCardsContainer.getScene().getWindow());
            gameDetailsStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/database/gametradefrontend/view/buyer-game-details.fxml"));
            Parent root = loader.load();
            
            BuyerGameDetailsController controller = loader.getController();
            controller.setCurrentGame(game);
            controller.setApiClient(apiClient);
            controller.setCurrentUser(currentUser);
            
            Scene scene = new Scene(root);
            gameDetailsStage.setScene(scene);
            gameDetailsStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("打开游戏详情页面失败: " + e.getMessage());
        }
    }
    
    // 退出登录
    @FXML
    private void handleLogout() {
        UserSession.getInstance().logout();
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
            
            // 返回欢迎页面
            Stage welcomeStage = new Stage();
            welcomeStage.setTitle("GameTrade - 欢迎");
            welcomeStage.setWidth(1000);
            welcomeStage.setHeight(700);
            
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/database/gametradefrontend/view/welcome.fxml")));
            Scene scene = new Scene(root);
            welcomeStage.setScene(scene);
            welcomeStage.show();
            
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("退出登录失败: " + e.getMessage());
        }
    }
}
