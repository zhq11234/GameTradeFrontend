package com.database.gametradefrontend.controller;

import com.database.gametradefrontend.client.ApiClient;
import com.database.gametradefrontend.model.User;
import com.database.gametradefrontend.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 游戏详情页面控制器
 * 负责游戏详情显示和修改功能
 */
public class GameDetailsController {

    @FXML private Button backButton;
    @FXML private Button viewReviewsButton;
    // 游戏详情页面字段
    @FXML private Label pageTitleLabel;
    @FXML private Label gameNameLabel;
    @FXML private Label companyNameLabel;
    @FXML private Label releaseTimeLabel;
    @FXML private Label statusLabel;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private TextField downloadLinkField;
    @FXML private TextField licenseNumberField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // 当前用户信息
    private User currentUser;

    // API客户端
    private ApiClient apiClient;

    // 当前正在编辑的游戏
    private VendorMainController.Game currentEditingGame;

    @FXML
    public void initialize() {
        // 初始化代码（如果需要）
    }

    // 设置API客户端
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // 设置当前用户
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // 加载游戏详情数据到表单
    public void loadGameDetails(VendorMainController.Game game) {

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
        String downloadLink = downloadLinkField.getText().trim();
        String licenseNumber = licenseNumberField.getText().trim();

        // 验证必填字段
        if (category.isEmpty() || price.isEmpty()) {
            ControllerUtils.showErrorAlert("游戏类别和价格不能为空");
            return;
        }

        // 验证价格格式（必须是有效的数字格式或"免费"）
        if (!isValidPriceFormat(price)) {
            ControllerUtils.showErrorAlert("价格格式不正确，请输入有效的数字格式（如：19.99）或输入'免费'");
            return;
        }

        // 处理免费显示逻辑
        if ("免费".equals(price) || "0".equals(price) || "0.0".equals(price) || "0.00".equals(price)) {
            price = "0";
        }

        // 显示保存状态
        saveButton.setDisable(true);
        saveButton.setText("保存中...");

        String finalPrice = price;
        new Thread(() -> {
            try {
                // 调用API更新游戏信息
                String endpoint = "/vendors/update-game";

                // 准备请求数据
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("account", currentUser.getAccount());
                requestData.put("gameName", currentEditingGame.getName());
                requestData.put("price", finalPrice);
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

    // 安全转换为字符串，处理null值
    private String safeToString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    // 验证价格格式（数字格式或"免费"）
    private boolean isValidPriceFormat(String price) {
        if (price == null || price.isEmpty()) {
            return false;
        }

        // 如果是"免费"，直接通过验证
        if ("免费".equals(price)) {
            return true;
        }

        // 验证数字格式（允许小数）
        try {
            // 尝试转换为数字
            double priceValue = Double.parseDouble(price);
            // 价格不能为负数
            return priceValue >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    private void handleViewReviews() {
        if (currentEditingGame == null) {
            ControllerUtils.showErrorAlert("没有正在查看的游戏");
            return;
        }

        // 显示加载状态
        viewReviewsButton.setDisable(true);
        viewReviewsButton.setText("加载中...");

        new Thread(() -> {
            try {
                // 调用API查询游戏评论
                String endpoint = "/vendors/query-game-reviews";

                // 准备请求数据
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("account", currentUser.getAccount());
                requestData.put("gameName", currentEditingGame.getName());

                // 调用API获取评论数据
                Object response = apiClient.post(endpoint, requestData, Object.class);

                // 在主线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    viewReviewsButton.setDisable(false);
                    viewReviewsButton.setText("买家评论");

                    if (response != null) {
                        // 处理API响应
                        List<Map<String, Object>> reviewsList = null;

                        if (response instanceof List) {
                            reviewsList = (List<Map<String, Object>>) response;
                        }

                        if (reviewsList != null && !reviewsList.isEmpty()) {
                            // 显示评论窗口
                            showReviewsWindow(reviewsList);
                        } else {
                            ControllerUtils.showInfoAlert("该游戏暂无评论");
                        }
                    } else {
                        ControllerUtils.showErrorAlert("查询评论失败");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    viewReviewsButton.setDisable(false);
                    viewReviewsButton.setText("买家评论");
                    ControllerUtils.showErrorAlert("查询评论失败: " + e.getMessage());
                });
            }
        }).start();
    }

    // 显示评论窗口
    private void showReviewsWindow(List<Map<String, Object>> reviewsList) {
        try {
            // 创建新窗口
            Stage reviewsStage = new Stage();
            reviewsStage.setTitle("买家评论 - " + currentEditingGame.getName());
            reviewsStage.setWidth(800);
            reviewsStage.setHeight(600);

            // 设置模态
            reviewsStage.initModality(Modality.WINDOW_MODAL);
            reviewsStage.initOwner(viewReviewsButton.getScene().getWindow());
            reviewsStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/yuanshen.png"))));
            // 创建评论列表容器
            VBox reviewsContainer = new VBox(10);
            reviewsContainer.setStyle("-fx-padding: 20; -fx-background-color: white;");

            // 添加标题
            Label titleLabel = new Label("买家评论 (" + reviewsList.size() + "条)");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");
            reviewsContainer.getChildren().add(titleLabel);

            // 添加评论项
            for (Map<String, Object> review : reviewsList) {
                VBox reviewItem = createReviewItem(review);
                reviewsContainer.getChildren().add(reviewItem);
            }


            // 创建滚动面板并优化滚轮体验
            ScrollPane scrollPane = new ScrollPane(reviewsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: white;");

            // 设置滚动条策略
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // 优化滚动速度
            scrollPane.setVvalue(0);
            scrollPane.setPannable(true);

            // 设置场景
            Scene scene = new Scene(scrollPane);
            reviewsStage.setScene(scene);

            // 显示窗口
            reviewsStage.show();

        } catch (Exception e) {
            ControllerUtils.showErrorAlert("显示评论窗口失败: " + e.getMessage());
        }
    }

    // 创建单个评论项
    private VBox createReviewItem(Map<String, Object> review) {
        VBox reviewItem = new VBox(5);
        reviewItem.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5; -fx-border-radius: 5; -fx-background-radius: 5;");

        // 顶部信息行（昵称、时间、评分）
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        // 昵称
        String nickname = safeToString(review.get("nickname"), "匿名用户");
        Label nicknameLabel = new Label(nickname);
        nicknameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // 评价时间
        String reviewTime = safeToString(review.get("reviewTime"), "未知时间");
        Label timeLabel = new Label(reviewTime);
        timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-padding: 0 10 0 10;");

        // 评分（星星显示）
        String scoreStr = safeToString(review.get("score"), "0");
        double score = 0;
        try {
            score = Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {
            score = 0;
        }

        // 计算星星数量（0-10分转换为0-5颗星）
        int stars = (int) Math.round(score / 2.0);
        HBox starsContainer = new HBox(2);
        starsContainer.setAlignment(Pos.CENTER_RIGHT);
        starsContainer.setStyle("-fx-padding: 0 0 0 10;");

        // 添加星星
        for (int i = 0; i < 5; i++) {
            Label starLabel = new Label(i < stars ? "★" : "☆");
            starLabel.setStyle("-fx-text-fill: " + (i < stars ? "#ffa500" : "#ccc") + "; -fx-font-size: 16px;");
            starsContainer.getChildren().add(starLabel);
        }

        // 评分文本
        Label scoreLabel = new Label(String.format("(%.1f分)", score));
        scoreLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-padding: 0 0 0 5;");
        starsContainer.getChildren().add(scoreLabel);

        // 使用Region填充空间，使星星靠右对齐
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(nicknameLabel, timeLabel, spacer, starsContainer);

        // 评论内容
        String comment = safeToString(review.get("comment"), "暂无评论");
        Label commentLabel = new Label(comment);
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 13px; -fx-padding: 5 0 0 0;");

        // 如果评论超过50个字符，添加展开功能
        if (comment.length() > 50) {
            String shortComment = comment.substring(0, 50) + "...";
            Label shortCommentLabel = new Label(shortComment);
            shortCommentLabel.setWrapText(true);
            shortCommentLabel.setStyle("-fx-font-size: 13px; -fx-padding: 5 0 0 0;");

            Button expandButton = new Button("展开");
            expandButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; -fx-border: none; -fx-padding: 0;");
            expandButton.setOnAction(e -> {
                if (reviewItem.getChildren().contains(shortCommentLabel)) {
                    reviewItem.getChildren().remove(shortCommentLabel);
                    reviewItem.getChildren().remove(expandButton);
                    reviewItem.getChildren().add(commentLabel);

                    Button collapseButton = new Button("收起");
                    collapseButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; -fx-border: none; -fx-padding: 0;");
                    collapseButton.setOnAction(e2 -> {
                        reviewItem.getChildren().remove(commentLabel);
                        reviewItem.getChildren().remove(collapseButton);
                        reviewItem.getChildren().add(shortCommentLabel);
                        reviewItem.getChildren().add(expandButton);
                    });
                    reviewItem.getChildren().add(collapseButton);
                }
            });

            reviewItem.getChildren().addAll(topRow, shortCommentLabel, expandButton);
        } else {
            reviewItem.getChildren().addAll(topRow, commentLabel);
        }

        return reviewItem;
    }

    // 评论数据类
    public static class ReviewData {
        private String nickname;
        private String score;
        private String comment;
        private String reviewTime;

        public ReviewData(String nickname, String score, String comment, String reviewTime) {
            this.nickname = nickname;
            this.score = score;
            this.comment = comment;
            this.reviewTime = reviewTime;
        }

        public String getNickname() { return nickname; }
        public String getScore() { return score; }
        public String getComment() { return comment; }
        public String getReviewTime() { return reviewTime; }
    }
}
