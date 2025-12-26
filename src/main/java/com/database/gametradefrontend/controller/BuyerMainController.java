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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
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
    @FXML private ComboBox<String> preferenceFilter;
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
    @FXML private Button createOrderButton;
    @FXML private TableView<Order> ordersTable;
    
    // 个人信息页面组件
    @FXML private VBox profileContent;
    @FXML private Label accountLabel;
    @FXML private TextField nicknameField;
    @FXML private TextField emailField;
    @FXML private TextArea preferenceField;
    @FXML private Button saveProfileButton;
    
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
        
        public Game(String name, String category, String price, String image, 
                   String description, String rating, String popularity) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.image = image;
            this.description = description;
            this.rating = rating;
            this.popularity = popularity;
        }
        
        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getImage() { return image; }
        public String getDescription() { return description; }
        public String getRating() { return rating; }
        public String getPopularity() { return popularity; }
    }
    
    /**
     * 游戏库游戏数据类
     */
    public static class LibraryGame {
        private final String name;
        private final String category;
        private final String purchaseTime;
        private final String status;
        
        public LibraryGame(String name, String category, String purchaseTime, String status) {
            this.name = name;
            this.category = category;
            this.purchaseTime = purchaseTime;
            this.status = status;
        }
        
        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPurchaseTime() { return purchaseTime; }
        public String getStatus() { return status; }
    }
    
    /**
     * 订单数据类
     */
    public static class Order {
        private final String orderId;
        private final String gameName;
        private final String price;
        private final String status;
        private final String orderTime;
        
        public Order(String orderId, String gameName, String price, String status, String orderTime) {
            this.orderId = orderId;
            this.gameName = gameName;
            this.price = price;
            this.status = status;
            this.orderTime = orderTime;
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public String getGameName() { return gameName; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
        public String getOrderTime() { return orderTime; }
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
        
        // 初始化用户界面
        initializeUserInfo();
        initializeTabs();
        initializeFilters();
        initializeTables();
        setupEventHandlers();
        
        // 默认显示游戏商店页面
        showGameStore();
    }
    
    private void initializeUserInfo() {
        userInfoLabel.setText("买家用户 - " + currentUser.getAccount());
        accountLabel.setText(currentUser.getAccount());
        nicknameField.setText(currentUser.getNickname() != null ? currentUser.getNickname() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        preferenceField.setText(currentUser.getPreferences() != null ? currentUser.getPreferences() : "");
    }
    
    private void initializeTabs() {
        // 设置选项卡样式
        resetTabStyles();
        gameStoreTab.getStyleClass().add("tab-active");
    }
    
    private void initializeFilters() {
        // 初始化筛选器选项
        categoryFilter.getItems().addAll("全部", "动作", "角色扮演", "策略", "射击", "体育", "模拟", "冒险", "益智");
        popularityFilter.getItems().addAll("全部", "热门", "最新", "评分最高", "销量最高");
        preferenceFilter.getItems().addAll("全部", "推荐", "根据历史购买", "根据浏览记录");
    }
    
    private void initializeTables() {
        // 初始化表格列（具体列绑定在FXML中已定义）
        // 这里可以添加表格数据绑定逻辑
    }
    
    private void setupEventHandlers() {
        // 设置各种事件处理器
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
    }
    
    // 游戏商店功能
    private void loadGameStoreData() {
        // 清空现有卡片
        gameCardsContainer.getChildren().clear();
        
        // 显示加载状态
        Label loadingLabel = new Label("正在加载游戏数据...");
        loadingLabel.getStyleClass().add("loading-label");
        gameCardsContainer.getChildren().add(loadingLabel);
        
        // 异步从API获取游戏数据
        new Thread(() -> {
            try {
                // 调用API获取游戏数据
                String endpoint = "/games/query-all-games";
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    gameCardsContainer.getChildren().clear();
                    
                    List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                    
                    if (gameList != null && !gameList.isEmpty()) {
                        games.clear();
                        
                        for (Map<String, Object> gameData : gameList) {
                            String name = gameData.getOrDefault("gameName", "未知游戏").toString();
                            String category = gameData.getOrDefault("category", "未知类别").toString();
                            String price = gameData.getOrDefault("price", "免费").toString();
                            String image = gameData.getOrDefault("image", "yuanshen.png").toString();
                            String description = gameData.getOrDefault("description", "暂无简介").toString();
                            String rating = gameData.getOrDefault("rating", "0").toString();
                            String popularity = gameData.getOrDefault("popularity", "普通").toString();
                            
                            Game game = new Game(name, category, price, image, description, rating, popularity);
                            games.add(game);
                            
                            // 创建游戏卡片
                            StackPane gameCard = createGameCard(game);
                            gameCardsContainer.getChildren().add(gameCard);
                        }
                    } else {
                        Label noDataLabel = new Label("暂无游戏数据");
                        noDataLabel.getStyleClass().add("no-data-label");
                        gameCardsContainer.getChildren().add(noDataLabel);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
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
        
        // 添加到内容区域
        content.getChildren().addAll(imageView, titleLabel, categoryLabel, priceLabel, ratingLabel);
        
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
        // 实现搜索逻辑
        filterGames();
    }
    
    @FXML
    private void handleResetFilter() {
        searchField.clear();
        categoryFilter.getSelectionModel().clearSelection();
        popularityFilter.getSelectionModel().clearSelection();
        preferenceFilter.getSelectionModel().clearSelection();
        loadGameStoreData();
    }
    
    private void filterGames() {
        // 实现游戏筛选逻辑
        // 这里可以根据搜索条件和筛选条件过滤游戏列表
    }
    
    // 游戏库功能
    private void loadLibraryData() {
        // 实现加载游戏库数据的逻辑
        libraryGames.clear();
        // 模拟数据
        libraryGames.add(new LibraryGame("原神", "角色扮演", "2024-01-15", "已下载"));
        libraryGames.add(new LibraryGame("王者荣耀", "MOBA", "2024-01-10", "可更新"));
        libraryTable.setItems(libraryGames);
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
        // 实现加载订单数据的逻辑
        orders.clear();
        // 模拟数据
        orders.add(new Order("ORD001", "原神", "¥68.00", "已支付", "2024-01-15 10:30"));
        orders.add(new Order("ORD002", "王者荣耀", "免费", "已完成", "2024-01-10 14:20"));
        ordersTable.setItems(orders);
    }
    
    @FXML
    private void handleRefreshOrders() {
        loadOrdersData();
        ControllerUtils.showInfoAlert("订单列表已刷新");
    }
    
    @FXML
    private void handleCreateOrder() {
        // 实现生成订单逻辑
        ControllerUtils.showInfoAlert("打开订单生成页面");
    }
    
    // 个人信息功能
    @FXML
    private void handleSaveProfile() {
        // 实现保存个人信息逻辑
        String nickname = nicknameField.getText();
        String email = emailField.getText();
        String preferences = preferenceField.getText();
        
        // 更新用户信息
        currentUser.setNickname(nickname);
        currentUser.setEmail(email);
        currentUser.setPreferences(preferences);
        
        ControllerUtils.showInfoAlert("个人信息保存成功");
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
            controller.setGameData(game);
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
