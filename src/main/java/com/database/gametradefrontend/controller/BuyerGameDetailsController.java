package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

/**
 * 买家游戏详情页面控制器
 * 支持游戏信息查看、评价、购买等功能
 */
public class BuyerGameDetailsController {

    // FXML 组件注入
    @FXML private Label pageTitleLabel;
    @FXML private Button purchaseButton;
    @FXML private Button downloadButton;
    @FXML private Button backButton;
    
    // 游戏信息组件
    @FXML private ImageView gameImageView;
    @FXML private Label priceLabel;
    @FXML private Label gameNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label ratingLabel;
    @FXML private Label developerLabel;
    @FXML private Label popularityLabel;
    @FXML private Label releaseTimeLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea descriptionArea;
    
    // 系统要求组件
    @FXML private Label osRequirementLabel;
    @FXML private Label cpuRequirementLabel;
    @FXML private Label memoryRequirementLabel;
    @FXML private Label gpuRequirementLabel;
    @FXML private Label storageRequirementLabel;
    
    // 评价组件
    @FXML private ComboBox<String> ratingComboBox;
    @FXML private TextField reviewTextField;
    @FXML private Button submitReviewButton;
    @FXML private VBox reviewsContainer;
    @FXML private Button loadMoreReviewsButton;
    
    // 当前用户和游戏信息
    private User currentUser;
    private ApiClient apiClient;
    private BuyerMainController.Game currentGame;
    
    // 评价数据
    private final ObservableList<Review> reviews = FXCollections.observableArrayList();
    private int currentReviewPage = 0;
    private static final int REVIEWS_PER_PAGE = 5;

    /**
     * 评价数据类
     */
    public static class Review {
        private final String reviewer;
        private final String rating;
        private final String content;
        private final String reviewTime;

        public Review(String reviewer, String rating, String content, String reviewTime) {
            this.reviewer = reviewer;
            this.rating = rating;
            this.content = content;
            this.reviewTime = reviewTime;
        }

        // Getters
        public String getReviewer() { return reviewer; }
        public String getRating() { return rating; }
        public String getContent() { return content; }
        public String getReviewTime() { return reviewTime; }
    }
    
    /**
     * 设置游戏数据
     */
    public void setGameData(BuyerMainController.Game game) {
        this.currentGame = game;
        initializeGameInfo();
    }
    
    /**
     * 设置API客户端
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * 设置当前用户
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    @FXML
    public void initialize() {
        // 初始化评价评分选项
        ratingComboBox.setItems(FXCollections.observableArrayList(
            "5星 ⭐⭐⭐⭐⭐", "4星 ⭐⭐⭐⭐", "3星 ⭐⭐⭐", "2星 ⭐⭐", "1星 ⭐"
        ));
        
        // 设置默认值
        ratingComboBox.setValue("5星 ⭐⭐⭐⭐⭐");
    }
    
    /**
     * 初始化游戏信息
     */
    private void initializeGameInfo() {
        if (currentGame == null) {
            return;
        }
        
        // 设置页面标题
        pageTitleLabel.setText("游戏详情 - " + currentGame.getName());
        gameNameLabel.setText(currentGame.getName());
        
        // 设置游戏信息
        categoryLabel.setText(currentGame.getCategory());
        priceLabel.setText("价格: " + currentGame.getPrice());
        ratingLabel.setText("评分: " + currentGame.getRating() + "⭐");
        developerLabel.setText("未知开发商"); // 可以从API获取
        popularityLabel.setText(currentGame.getPopularity());
        releaseTimeLabel.setText("未知时间"); // 可以从API获取
        statusLabel.setText("可购买");
        descriptionArea.setText(currentGame.getDescription());
        
        // 加载游戏图片
        loadGameImage();
        
        // 加载系统要求信息
        loadSystemRequirements();
        
        // 加载评价数据
        loadReviews();
    }
    
    /**
     * 加载游戏图片
     */
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
    
    /**
     * 加载系统要求信息
     */
    private void loadSystemRequirements() {
        // 模拟系统要求数据，实际应从API获取
        osRequirementLabel.setText("Windows 10/11, macOS 10.14+, Ubuntu 18.04+");
        cpuRequirementLabel.setText("Intel Core i5 或同等处理器");
        memoryRequirementLabel.setText("8 GB RAM");
        gpuRequirementLabel.setText("NVIDIA GeForce GTX 1060 或同等显卡");
        storageRequirementLabel.setText("20 GB 可用空间");
    }
    
    /**
     * 加载评价数据
     */
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
    
    /**
     * 显示评价列表
     */
    private void displayReviews() {
        int startIndex = currentReviewPage * REVIEWS_PER_PAGE;
        int endIndex = Math.min(startIndex + REVIEWS_PER_PAGE, reviews.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Review review = reviews.get(i);
            addReviewToContainer(review);
        }
        
        // 检查是否还有更多评价可加载
        loadMoreReviewsButton.setVisible(endIndex < reviews.size());
    }
    
    /**
     * 添加单个评价到容器
     */
    private void addReviewToContainer(Review review) {
        VBox reviewBox = new VBox();
        reviewBox.getStyleClass().add("game-card");
        reviewBox.setStyle("-fx-padding: 15px; -fx-spacing: 8px;");
        
        // 评价头部信息
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        
        Label reviewerLabel = new Label(review.getReviewer());
        reviewerLabel.getStyleClass().add("game-card-title");
        
        Label ratingLabel = new Label(review.getRating());
        ratingLabel.getStyleClass().add("game-card-rating");
        
        Label timeLabel = new Label(review.getReviewTime());
        timeLabel.getStyleClass().add("game-card-category");
        timeLabel.setStyle("-fx-text-fill: #999;");
        
        headerBox.getChildren().addAll(reviewerLabel, ratingLabel, timeLabel);
        
        // 评价内容
        Text contentText = new Text(review.getContent());
        contentText.setWrappingWidth(600);
        contentText.getStyleClass().add("overlay-text");
        contentText.setStyle("-fx-fill: #333;");
        
        reviewBox.getChildren().addAll(headerBox, contentText);
        reviewsContainer.getChildren().add(reviewBox);
    }
    
    // 事件处理方法
    
    @FXML
    private void handlePurchase() {
        if (currentGame == null || currentUser == null) {
            ControllerUtils.showErrorAlert("无法购买游戏，信息不完整");
            return;
        }
        
        // 显示购买确认对话框
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认购买");
        confirmAlert.setHeaderText("购买游戏确认");
        confirmAlert.setContentText("您确定要购买《" + currentGame.getName() + "》吗？\n价格: " + currentGame.getPrice());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 执行购买逻辑
            performPurchase();
        }
    }
    
    @FXML
    private void handleDownload() {
        if (currentGame == null) {
            ControllerUtils.showErrorAlert("无法下载游戏，信息不完整");
            return;
        }
        
        // 检查是否已购买
        if (!checkPurchaseStatus()) {
            ControllerUtils.showErrorAlert("请先购买游戏才能下载");
            return;
        }
        
        // 执行下载逻辑
        performDownload();
    }
    
    @FXML
    private void handleSubmitReview() {
        if (currentUser == null || currentGame == null) {
            ControllerUtils.showErrorAlert("请先登录才能提交评价");
            return;
        }
        
        String rating = ratingComboBox.getValue();
        String content = reviewTextField.getText().trim();
        
        if (content.isEmpty()) {
            ControllerUtils.showErrorAlert("评价内容不能为空");
            return;
        }
        
        // 提交评价
        submitReview(rating, content);
    }
    
    @FXML
    private void handleLoadMoreReviews() {
        currentReviewPage++;
        displayReviews();
    }
    
    @FXML
    private void handleBackToMain() {
        // 关闭当前窗口
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 执行购买操作
     */
    private void performPurchase() {
        purchaseButton.setDisable(true);
        purchaseButton.setText("购买中...");
        
        new Thread(() -> {
            try {
                // 模拟API调用购买游戏
                Thread.sleep(1000); // 模拟网络延迟
                
                Platform.runLater(() -> {
                    ControllerUtils.showInfoAlert("购买成功！您现在可以下载游戏了");
                    purchaseButton.setText("已购买");
                    purchaseButton.setDisable(true);
                    downloadButton.setDisable(false);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("购买失败: " + e.getMessage());
                    purchaseButton.setText("购买游戏");
                    purchaseButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * 执行下载操作
     */
    private void performDownload() {
        downloadButton.setDisable(true);
        downloadButton.setText("下载中...");
        
        new Thread(() -> {
            try {
                // 模拟下载过程
                for (int i = 0; i <= 100; i += 10) {
                    final int progress = i;
                    Platform.runLater(() -> downloadButton.setText("下载中 " + progress + "%"));
                    Thread.sleep(200);
                }
                
                Platform.runLater(() -> {
                    ControllerUtils.showInfoAlert("下载完成！游戏已添加到您的游戏库");
                    downloadButton.setText("已下载");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("下载失败: " + e.getMessage());
                    downloadButton.setText("下载");
                    downloadButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * 提交评价
     */
    private void submitReview(String rating, String content) {
        submitReviewButton.setDisable(true);
        submitReviewButton.setText("提交中...");
        
        new Thread(() -> {
            try {
                // 模拟API调用提交评价
                Thread.sleep(500); // 模拟网络延迟
                
                Platform.runLater(() -> {
                    // 添加新评价到列表
                    Review newReview = new Review(currentUser.getNickname() != null ? 
                        currentUser.getNickname() : currentUser.getAccount(), 
                        rating, content, new Date().toString());
                    
                    reviews.addFirst(newReview); // 添加到开头
                    reviewsContainer.getChildren().clear();
                    displayReviews();
                    
                    // 清空输入框
                    reviewTextField.clear();
                    ratingComboBox.setValue("5星 ⭐⭐⭐⭐⭐");
                    
                    ControllerUtils.showInfoAlert("评价提交成功！");
                    submitReviewButton.setText("提交评价");
                    submitReviewButton.setDisable(false);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ControllerUtils.showErrorAlert("评价提交失败: " + e.getMessage());
                    submitReviewButton.setText("提交评价");
                    submitReviewButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * 检查购买状态（模拟）
     */
    private boolean checkPurchaseStatus() {
        // 模拟检查购买状态，实际应从API获取
        return "已购买".equals(purchaseButton.getText());
    }
}
