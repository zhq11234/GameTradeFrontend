package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

/**
 * 游戏修改页面控制器
 * 专门用于修改游戏信息的页面
 */
public class EditGamesController {

    @FXML private Button backButton;
    @FXML private FlowPane gameCardsContainer;
    @FXML private Label noDataLabel;
    @FXML private Label loadingLabel;

    // 当前用户信息
    private User currentUser;
    
    // API客户端
    private ApiClient apiClient;
    
    // 游戏数据 - 使用VendorMainController.Game类
    private final List<VendorMainController.Game> games = new ArrayList<>();

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
        
        // 加载游戏数据
        loadGameData();
    }
    
    private void loadGameData() {
        // 显示加载状态
        loadingLabel.setVisible(true);
        noDataLabel.setVisible(false);
        gameCardsContainer.getChildren().clear();
        
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
                    loadingLabel.setVisible(false);
                    
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
                            VendorMainController.Game game = new VendorMainController.Game(name, category, price, image, description, status);
                            games.add(game);
                            
                            // 创建游戏卡片
                            StackPane gameCard = createGameCard(game);
                            gameCardsContainer.getChildren().add(gameCard);
                        }
                    } else {
                        // 如果没有数据，显示提示信息
                        noDataLabel.setVisible(true);
                    }
                });
            } catch (Exception e) {
                // 在主线程中显示错误信息
                javafx.application.Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    noDataLabel.setVisible(true);
                    noDataLabel.setText("加载游戏数据失败: " + e.getMessage());
                    ControllerUtils.showErrorAlert("加载游戏数据失败: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private StackPane createGameCard(VendorMainController.Game game) {
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
        card.setOnMouseEntered(event -> overlay.setVisible(true));
        
        card.setOnMouseExited(event -> overlay.setVisible(false));
        
        // 添加点击事件 - 点击卡片打开游戏详情页面进行修改
        card.setOnMouseClicked(event -> showGameDetails(game));
        
        return card;
    }
    
    private void showGameDetails(VendorMainController.Game game) {
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
    
    @FXML
    private void handleBackToMain() {
        // 关闭当前窗口
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
