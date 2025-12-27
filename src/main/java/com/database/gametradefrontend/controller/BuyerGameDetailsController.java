package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;

public class BuyerGameDetailsController implements Initializable {
    
    @FXML private Label pageTitleLabel;
    @FXML private Label gameNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label priceLabel;
    @FXML private Label ratingLabel;
    @FXML private Label developerLabel;
    @FXML private Label popularityLabel;
    @FXML private Label releaseTimeLabel;
    @FXML private Label statusLabel;
    @FXML private Label licenseNumberLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView gameImageView;
    
    // 系统要求相关标签
    @FXML private Label osRequirementLabel;
    @FXML private Label cpuRequirementLabel;
    @FXML private Label memoryRequirementLabel;
    @FXML private Label gpuRequirementLabel;
    @FXML private Label storageRequirementLabel;
    
    // 评价相关组件
    @FXML private Label averageRatingLabel;
    @FXML private ComboBox<String> ratingComboBox;
    @FXML private TextArea reviewTextArea;
    @FXML private VBox reviewsContainer;
    @FXML private Button submitReviewButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    
    // 购买相关按钮
    @FXML private Button addToCartButton;
    @FXML private Button backButton;
    
    private ApiClient apiClient;
    private User currentUser;
    private BuyerMainController.Game currentGame;
    private ObservableList<Review> reviews;
    private int currentReviewPage = 0;
    private static final int REVIEWS_PER_PAGE = 5;
    
    // 内部类用于存储评价数据
    public static class Review {
        private final String username;
        private final String rating;
        private final String content;
        private final String date;
        
        public Review(String username, String rating, String content, String date) {
            this.username = username;
            this.rating = rating;
            this.content = content;
            this.date = date;
        }
        
        public String getUsername() { return username; }
        public String getRating() { return rating; }
        public String getContent() { return content; }
        public String getDate() { return date; }
    }
    
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    public void setCurrentGame(BuyerMainController.Game currentGame) {
        this.currentGame = currentGame;
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化评价评分选项
        ratingComboBox.setItems(FXCollections.observableArrayList(
            "5星 ⭐⭐⭐⭐⭐", "4星 ⭐⭐⭐⭐", "3星 ⭐⭐⭐", "2星 ⭐⭐", "1星 ⭐"
        ));
        
        // 设置默认值
        ratingComboBox.setValue("5星 ⭐⭐⭐⭐⭐");
        
        // 初始化评价列表
        reviews = FXCollections.observableArrayList();
        
        // 初始化后加载游戏信息
        Platform.runLater(this::initializeGameInfo);
    }
    
    private void initializeGameInfo() {
        if (currentGame == null) {
            return;
        }
        
        // 设置页面标题和游戏名（使用当前游戏数据作为默认值）
        pageTitleLabel.setText("游戏详情 - " + currentGame.getName());
        gameNameLabel.setText(currentGame.getName());
        
        // 显示加载状态
        categoryLabel.setText("加载中...");
        priceLabel.setText("价格: 加载中...");
        ratingLabel.setText("评分: 加载中...");
        developerLabel.setText("厂商: 加载中...");
        popularityLabel.setText("销量: 加载中...");
        releaseTimeLabel.setText("发布时间: 加载中...");
        statusLabel.setText("状态: 加载中...");
        licenseNumberLabel.setText("版号: 加载中...");
        descriptionArea.setText("正在加载游戏描述...");
        averageRatingLabel.setText("平均评分: 加载中...");
        
        // 异步调用API获取游戏详细信息
        new Thread(() -> {
            try {
                // 调用API获取游戏详细信息
                String endpoint = "/buyers/games/details?gameName=" + 
                    java.net.URLEncoder.encode(currentGame.getName(), "UTF-8");
                Object response = apiClient.get(endpoint, Object.class);
                
                // 在主线程中更新UI
                Platform.runLater(() -> {
                    if (response instanceof Map) {
                        Map<String, Object> gameDetails = (Map<String, Object>) response;
                        
                        // 提取游戏详细信息（添加null检查）
                        String gameName = safeToString(gameDetails.get("gameName"), currentGame.getName());
                        String category = safeToString(gameDetails.get("category"), currentGame.getCategory());
                        String price = safeToString(gameDetails.get("price"), currentGame.getPrice());
                        String companyName = safeToString(gameDetails.get("companyName"), "未知厂商");
                        String releaseTime = safeToString(gameDetails.get("releaseTime"), "未知时间");
                        String description = safeToString(gameDetails.get("description"), currentGame.getDescription());
                        String licenseNumber = safeToString(gameDetails.get("licenseNumber"), "未知");
                        String score = safeToString(gameDetails.get("score"), currentGame.getRating());
                        String salesVolume = safeToString(gameDetails.get("salesVolume"), currentGame.getPopularity());
                        
                        // 处理免费显示逻辑
                        String displayPrice = price;
                        if ("0".equals(price) || "0.0".equals(price) || "0.00".equals(price)) {
                            displayPrice = "免费";
                        }
                        
                        // 更新UI组件
                        pageTitleLabel.setText("游戏详情 - " + gameName);
                        gameNameLabel.setText(gameName);
                        categoryLabel.setText("类别: " + category);
                        priceLabel.setText("价格: " + displayPrice);
                        ratingLabel.setText("评分: " + score + "⭐");
                        developerLabel.setText("厂商: " + companyName);
                        popularityLabel.setText("销量: " + salesVolume);
                        releaseTimeLabel.setText("发布时间: " + releaseTime);
                        statusLabel.setText("状态: 可购买");
                        licenseNumberLabel.setText("版号: " + licenseNumber);
                        descriptionArea.setText(description);
                        averageRatingLabel.setText("平均评分: " + score + "⭐");
                        
                    } else {
                        // 如果API调用失败，使用默认的游戏数据
                        updateUIWithDefaultData();
                        ControllerUtils.showErrorAlert("获取游戏详细信息失败，显示基础信息");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateUIWithDefaultData();
                    ControllerUtils.showErrorAlert("获取游戏详细信息失败: " + e.getMessage());
                });
            }
        }).start();
        
        // 加载游戏图片
        loadGameImage();
        
        // 加载系统要求信息
        loadSystemRequirements();
        
        // 加载评价数据
        loadReviews();
    }
    
    private void updateUIWithDefaultData() {
        categoryLabel.setText("类别: " + currentGame.getCategory());
        priceLabel.setText("价格: " + currentGame.getPrice());
        ratingLabel.setText("评分: " + currentGame.getRating() + "⭐");
        developerLabel.setText("厂商: 未知");
        popularityLabel.setText("销量: " + currentGame.getPopularity());
        releaseTimeLabel.setText("发布时间: 未知");
        statusLabel.setText("状态: 可购买");
        licenseNumberLabel.setText("版号: 未知");
        descriptionArea.setText(currentGame.getDescription());
        averageRatingLabel.setText("平均评分: " + currentGame.getRating() + "⭐");
    }
    
    // 安全转换为字符串，处理null值
    private String safeToString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    private void loadGameImage() {
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/" + currentGame.getImage())));
            gameImageView.setImage(image);
        } catch (Exception e) {
            try {
                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png")));
                gameImageView.setImage(defaultImage);
            } catch (Exception ex) {
                gameImageView.setStyle("-fx-background-color: #667eea; -fx-min-width: 300px; -fx-min-height: 200px;");
            }
        }
    }
    
    private void loadSystemRequirements() {
        // 模拟系统要求数据，实际应从API获取
        osRequirementLabel.setText("Windows 10/11, macOS 10.14+, Ubuntu 18.04+");
        cpuRequirementLabel.setText("Intel Core i5 或同等处理器");
        memoryRequirementLabel.setText("8 GB RAM");
        gpuRequirementLabel.setText("NVIDIA GeForce GTX 1060 或同等显卡");
        storageRequirementLabel.setText("20 GB 可用空间");
    }
    
    private void loadReviews() {
        reviewsContainer.getChildren().clear();
        currentReviewPage = 0;
        
        // 显示加载状态
        Label loadingLabel = new Label("正在加载评价...");
        loadingLabel.getStyleClass().add("loading-label");
        reviewsContainer.getChildren().add(loadingLabel);
        
        // 异步加载评价数据
        new Thread(() -> {
            try {
                // 模拟API调用获取评价数据
                Thread.sleep(500); // 模拟网络延迟
                
                // 模拟评价数据
                List<Review> mockReviews = Arrays.asList(
                    new Review("玩家123", "5星 ⭐⭐⭐⭐⭐", "游戏画面精美，玩法有趣，强烈推荐！", "2024-01-15"),
                    new Review("游戏爱好者", "4星 ⭐⭐⭐⭐", "整体体验不错，但有些小bug需要修复", "2024-01-14"),
                    new Review("资深玩家", "5星 ⭐⭐⭐⭐⭐", "这是我玩过的最好的游戏之一，物超所值！", "2024-01-13")
                );
                
                Platform.runLater(() -> {
                    reviewsContainer.getChildren().clear();
                    reviews.setAll(mockReviews);
                    
                    if (reviews.isEmpty()) {
                        Label noReviewsLabel = new Label("暂无评价");
                        noReviewsLabel.getStyleClass().add("no-data-label");
                        reviewsContainer.getChildren().add(noReviewsLabel);
                    } else {
                        displayReviews();
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    reviewsContainer.getChildren().clear();
                    Label errorLabel = new Label("加载评价失败: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    reviewsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void displayReviews() {
        int startIndex = currentReviewPage * REVIEWS_PER_PAGE;
        int endIndex = Math.min(startIndex + REVIEWS_PER_PAGE, reviews.size());
        
        reviewsContainer.getChildren().clear();
        
        for (int i = startIndex; i < endIndex; i++) {
            Review review = reviews.get(i);
            
            VBox reviewCard = new VBox(8);
            reviewCard.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-border-radius: 4; -fx-background-radius: 4;");
            
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label usernameLabel = new Label(review.getUsername());
            usernameLabel.setStyle("-fx-font-weight: bold;");
            
            Label ratingLabel = new Label(review.getRating());
            ratingLabel.setStyle("-fx-text-fill: #ffa500; -fx-font-weight: bold;");
            
            Label dateLabel = new Label(review.getDate());
            dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            
            HBox.setHgrow(dateLabel, Priority.ALWAYS);
            dateLabel.setAlignment(Pos.CENTER_RIGHT);
            
            header.getChildren().addAll(usernameLabel, ratingLabel, dateLabel);
            
            Label contentLabel = new Label(review.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-text-fill: #333;");
            
            reviewCard.getChildren().addAll(header, contentLabel);
            reviewsContainer.getChildren().add(reviewCard);
        }
        
        // 更新分页信息
        updatePaginationInfo();
    }
    
    private void updatePaginationInfo() {
        int totalPages = (int) Math.ceil((double) reviews.size() / REVIEWS_PER_PAGE);
        pageInfoLabel.setText("第 " + (currentReviewPage + 1) + " 页 / 共 " + totalPages + " 页");
        
        prevPageButton.setDisable(currentReviewPage == 0);
        nextPageButton.setDisable(currentReviewPage >= totalPages - 1);
    }
    
    @FXML
    private void handleSubmitReview() {
        String rating = ratingComboBox.getValue();
        String content = reviewTextArea.getText().trim();
        
        if (content.isEmpty()) {
            ControllerUtils.showErrorAlert("请输入评价内容");
            return;
        }
        
        // 模拟提交评价
        ControllerUtils.showInfoAlert("评价提交成功！");
        reviewTextArea.clear();
        
        // 重新加载评价
        loadReviews();
    }
    
    @FXML
    private void handlePrevPage() {
        if (currentReviewPage > 0) {
            currentReviewPage--;
            displayReviews();
        }
    }
    
    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) reviews.size() / REVIEWS_PER_PAGE);
        if (currentReviewPage < totalPages - 1) {
            currentReviewPage++;
            displayReviews();
        }
    }
    
    @FXML
    private void handleAddToCart() {
        if (currentGame == null || currentUser == null) {
            ControllerUtils.showErrorAlert("无法生成订单：游戏或用户信息缺失");
            return;
        }
        
        // 获取买家昵称和游戏名称
        String buyerNickname = currentUser.getNickname();
        String gameName = currentGame.getName();
        
        if (buyerNickname == null || buyerNickname.isEmpty()) {
            ControllerUtils.showErrorAlert("无法生成订单：用户昵称为空");
            return;
        }
        
        if (gameName == null || gameName.isEmpty()) {
            ControllerUtils.showErrorAlert("无法生成订单：游戏名称为空");
            return;
        }
        
        // 异步调用API生成订单
        new Thread(() -> {
            try {
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("buyerNickname", buyerNickname);
                requestBody.put("gameName", gameName);
                
                // 调用API生成订单
                String endpoint = "/buyers/orders";
                String response = apiClient.post(endpoint, requestBody, String.class);
                
                // 在主线程中显示成功信息
                Platform.runLater(() -> {
                    ControllerUtils.showInfoAlert("订单生成成功！游戏已加入购物车");
                });
                
            } catch (Exception e) {
                // 在主线程中显示错误信息
                Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("生成订单失败: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleBackToMain() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
