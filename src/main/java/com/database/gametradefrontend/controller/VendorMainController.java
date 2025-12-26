package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import com.database.gametradefrontend.client.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Logger;
import java.util.Optional;


/**
 * 厂商主页面控制器
 * 负责厂商用户的所有功能实现
 */
public class VendorMainController {

    public TextField contactPersonField;
    // 顶部导航组件
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label userInfoLabel;
    @FXML private Button logoutButton;
    
    // 选项卡按钮
    @FXML private Button dashboardTab;
    @FXML private Button gameManagementTab;
    @FXML private Button salesDataTab;
    @FXML private Button reviewsTab;
    @FXML private Button profileTab;
    
    // 内容区域
    @FXML private VBox dashboardContent;
    @FXML private VBox gameManagementContent;
    @FXML private VBox salesDataContent;
    @FXML private VBox reviewsContent;
    @FXML private VBox profileContent;
    
    // 游戏卡片容器
    @FXML private FlowPane gameCardsContainer;
    
    // 游戏管理功能按钮
    @FXML private Button createGameButton;
    @FXML private Button editGameButton;
    @FXML private Button publishGameButton;
    @FXML private Button unpublishGameButton;
    
    // 数据表格
    @FXML private TableView<SalesData> salesTable;
    @FXML private TableView<ReviewData> reviewsTable;
    
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
    
    // 当前用户信息
    private User currentUser;
    
    // API客户端
    private ApiClient apiClient;
    
    // 模拟数据
    private final List<Game> games = new ArrayList<>();
    private final ObservableList<SalesData> salesData = FXCollections.observableArrayList();
    private final ObservableList<ReviewData> reviewData = FXCollections.observableArrayList();
    
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
        
        // 模拟游戏数据
        games.add(new Game("原神", "角色扮演", "免费", "yuanshen.png"));
        games.add(new Game("王者荣耀", "MOBA", "免费", "yuanshen.png"));
        games.add(new Game("和平精英", "射击", "免费", "yuanshen.png"));
        games.add(new Game("英雄联盟", "MOBA", "免费", "yuanshen.png"));
        
        // 创建游戏卡片
        for (Game game : games) {
            StackPane gameCard = createGameCard(game);
            gameCardsContainer.getChildren().add(gameCard);
        }
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
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/" + game.getImage())));
            imageView.setImage(image);
        } catch (Exception e) {
            // 使用默认图片
            try {
                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png")));
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                // 如果默认图片也不存在，创建一个占位符
                imageView.setStyle("-fx-background-color: #667eea; -fx-min-width: 250px; -fx-min-height: 150px;");
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
        
        content.getChildren().addAll(titleLabel, categoryLabel, priceLabel);
        
        // 创建悬停覆盖层
        VBox overlay = new VBox();
        overlay.getStyleClass().add("game-card-overlay");
        overlay.setMouseTransparent(true);
        
        Label overlayTitle = new Label(game.getName());
        overlayTitle.getStyleClass().add("overlay-text");
        overlayTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label overlayInfo = new Label("类别: " + game.getCategory() + "\n价格: " + game.getPrice());
        overlayInfo.getStyleClass().add("overlay-text");
        
        overlay.getChildren().addAll(overlayTitle, overlayInfo);
        
        // 组装卡片
        card.getChildren().addAll(imageView, content, overlay);
        
        // 添加点击事件
        card.setOnMouseClicked(event -> showGameDetails(game));
        
        return card;
    }
    
    private void initializeTables() {
        // 初始化销售数据表格
        salesData.add(new SalesData("原神", "免费", 10000, 0, 50000, "0%"));
        salesData.add(new SalesData("王者荣耀", "免费", 50000, 0, 200000, "0%"));
        salesTable.setItems(salesData);
        
        // 初始化评价数据表格
        reviewData.add(new ReviewData("玩家1", "5", "很好玩的游戏", "2024-01-15"));
        reviewData.add(new ReviewData("玩家2", "4", "画面精美", "2024-01-14"));
        reviewsTable.setItems(reviewData);
    }
    
    private void setupEventHandlers() {
        // 搜索按钮事件
        searchButton.setOnAction(event -> handleSearch());
        
        // 退出登录事件
        logoutButton.setOnAction(event -> handleLogout());
        
        // 个人信息保存事件
        saveProfileButton.setOnAction(event -> handleSaveProfile());
        
        // 游戏管理功能事件
        createGameButton.setOnAction(event -> handleCreateGame());
        editGameButton.setOnAction(event -> handleEditGame());
        publishGameButton.setOnAction(event -> handlePublishGame());
        unpublishGameButton.setOnAction(event -> handleUnpublishGame());
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
    private void showReviews() {
        showContent(reviewsContent);
        setActiveTab(reviewsTab);
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
        reviewsContent.setVisible(false);
        reviewsContent.setManaged(false);
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
        reviewsTab.getStyleClass().remove("tab-active");
        profileTab.getStyleClass().remove("tab-active");
    }
    
    // 事件处理方法
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            ControllerUtils.showInfoAlert("请输入搜索关键词");
            return;
        }
        
        // 模拟搜索功能
        ControllerUtils.showInfoAlert("搜索关键词: " + keyword + "\n(搜索功能待实现)");
    }
    
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
        ControllerUtils.showInfoAlert("游戏修改功能\n(待实现)");
    }
    
    @FXML
    private void handlePublishGame() {
        ControllerUtils.showInfoAlert("游戏上架功能\n(待实现)");
    }
    
    @FXML
    private void handleUnpublishGame() {
        ControllerUtils.showInfoAlert("游戏下架功能\n(待实现)");
    }
    
    private void showGameDetails(Game game) {
        ControllerUtils.showInfoAlert("游戏详情:\n" +
                                    "名称: " + game.getName() + "\n" +
                                    "类别: " + game.getCategory() + "\n" +
                                    "价格: " + game.getPrice() + "\n" +
                                    "(详细信息和评论功能待实现)");
    }
    
    // 内部数据类
    public static class Game {
        private String name;
        private String category;
        private String price;
        private String image;
        
        public Game(String name, String category, String price, String image) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.image = image;
        }
        
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getImage() { return image; }
    }
    
    public static class SalesData {
        private String gameName;
        private String price;
        private int sales;
        private double revenue;
        private int visitors;
        private String conversionRate;
        
        public SalesData(String gameName, String price, int sales, double revenue, int visitors, String conversionRate) {
            this.gameName = gameName;
            this.price = price;
            this.sales = sales;
            this.revenue = revenue;
            this.visitors = visitors;
            this.conversionRate = conversionRate;
        }
        
        public String getGameName() { return gameName; }
        public String getPrice() { return price; }
        public int getSales() { return sales; }
        public double getRevenue() { return revenue; }
        public int getVisitors() { return visitors; }
        public String getConversionRate() { return conversionRate; }
    }
    
    public static class ReviewData {
        private String nickname;
        private String rating;
        private String comment;
        private String reviewTime;
        
        public ReviewData(String nickname, String rating, String comment, String reviewTime) {
            this.nickname = nickname;
            this.rating = rating;
            this.comment = comment;
            this.reviewTime = reviewTime;
        }
        
        public String getNickname() { return nickname; }
        public String getRating() { return rating; }
        public String getComment() { return comment; }
        public String getReviewTime() { return reviewTime; }
    }
}
