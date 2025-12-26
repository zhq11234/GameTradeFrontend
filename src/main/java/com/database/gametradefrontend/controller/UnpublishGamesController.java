package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

/**
 * 游戏下架页面控制器
 * 负责显示状态为"上架"的游戏，并提供下架功能
 */
public class UnpublishGamesController {

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
        // 显示加载状态
        loadingLabel.setVisible(true);
        noDataLabel.setVisible(false);
        
        // 异步加载游戏数据
        loadGamesWithStatusOnline();
    }

    /**
     * 加载状态为"上架"的游戏数据
     */
    private void loadGamesWithStatusOnline() {
        new Thread(() -> {
            try {
                // 调用API获取状态为"上架"的厂商游戏数据
                String endpoint = "/vendors/query-vendor-games";
                // 准备请求数据，包含account和status参数
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("account", currentUser.getAccount());
                requestData.put("status", "上架"); // 查询上架状态的游戏
                
                Object response = apiClient.post(endpoint, requestData, Object.class);
                
                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    
                    if (response instanceof List) {
                        List<Map<String, Object>> gameList = (List<Map<String, Object>>) response;
                        
                        if (!gameList.isEmpty()) {
                            games.clear();
                            gameCardsContainer.getChildren().clear();
                            
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
                            noDataLabel.setText("暂无上架状态的游戏");
                        }
                    } else {
                        // 如果响应格式不正确
                        noDataLabel.setVisible(true);
                        noDataLabel.setText("数据格式错误");
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

    /**
     * 创建游戏卡片
     */
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
        
        // 添加点击事件 - 点击卡片进行下架操作
        card.setOnMouseClicked(event -> handleOfflineApplication(game));
        
        return card;
    }

    /**
     * 处理下架申请
     */
    private void handleOfflineApplication(VendorMainController.Game game) {
        // 显示确认对话框
        boolean confirmed = ControllerUtils.showConfirmationAlert(
            "下架确认",
            "确认要对游戏 '" + game.getName() + "' 进行下架操作吗？",
            "下架后游戏将不再对用户可见"
        );
        
        if (confirmed) {
            // 发送下架请求
            new Thread(() -> {
                try {
                    // 调用API进行下架操作
                    String endpoint = "/vendors/game-off-shelf";
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("gameName", game.getName());
                    requestData.put("account", currentUser.getAccount());
                    
                    String result = apiClient.post(endpoint, requestData, String.class);
                    
                    // 在主线程中显示结果
                    javafx.application.Platform.runLater(() -> {
                        if (result != null && result.contains("成功")) {
                            ControllerUtils.showInfoAlert("游戏下架成功");
                            // 重新加载游戏列表
                            loadGamesWithStatusOnline();
                        } else {
                            ControllerUtils.showErrorAlert("游戏下架失败: " + result);
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        ControllerUtils.showErrorAlert("游戏下架失败: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleBackToMain() {
        // 关闭当前窗口
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
