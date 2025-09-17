import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class CoffeeShopFXApp extends Application {
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final Order currentOrder = new Order(0, 1, Order.ServiceType.TAKEAWAY);

    @Override
    public void start(Stage stage) {
        DatabaseConnection.getInstance().initializeDatabase();

        Label title = new Label("Coffee Shop - JavaFX");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Chọn danh mục");

        List<String> categories = menuItemDAO.getCategories();
        ObservableList<String> categoryData = FXCollections.observableArrayList();
        categoryData.add("Tất cả");
        categoryData.addAll(categories);
        categoryFilter.setItems(categoryData);
        categoryFilter.getSelectionModel().selectFirst();

        ListView<MenuItem> menuList = new ListView<>();
        menuList.setCellFactory(lv -> new ListCell<MenuItem>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - $" + String.format("%.2f", item.getPrice()));
                }
            }
        });

        Runnable loadMenu = () -> {
            List<MenuItem> items;
            String sel = categoryFilter.getSelectionModel().getSelectedItem();
            if (sel == null || sel.equals("Tất cả")) {
                items = menuItemDAO.getAvailableMenuItems();
            } else {
                items = menuItemDAO.getMenuItemsByCategory(sel);
            }
            menuList.setItems(FXCollections.observableArrayList(items));
        };
        loadMenu.run();

        categoryFilter.setOnAction(e -> loadMenu.run());

        Spinner<Integer> qtySpinner = new Spinner<>(1, 20, 1);
        Button addBtn = new Button("Thêm vào giỏ");

        TextArea orderArea = new TextArea();
        orderArea.setEditable(false);
        orderArea.setPrefRowCount(12);

        Label totalLabel = new Label();

        addBtn.setOnAction(e -> {
            MenuItem selected = menuList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Chưa chọn món");
                return;
            }
            currentOrder.addItem(selected, qtySpinner.getValue());
            updateOrderArea(orderArea, totalLabel);
        });

        Button clearBtn = new Button("Xóa giỏ");
        clearBtn.setOnAction(e -> { currentOrder.clearOrder(); updateOrderArea(orderArea, totalLabel); });

        Button checkoutBtn = new Button("Thanh toán");
        checkoutBtn.setOnAction(e -> {
            if (currentOrder.isEmpty()) { alert("Giỏ hàng trống"); return; }
            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.createOrder(currentOrder);
            if (orderId > 0) {
                alert("Đã lưu đơn #" + orderId + " - Tổng: $" + String.format("%.2f", currentOrder.getTotalAmount()));
                currentOrder.clearOrder();
                updateOrderArea(orderArea, totalLabel);
            } else {
                alert("Lưu đơn thất bại");
            }
        });

        HBox controls = new HBox(10, new Label("SL:"), qtySpinner, addBtn, clearBtn, checkoutBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(10, title, categoryFilter, menuList, controls);
        left.setPadding(new Insets(12));
        left.setPrefWidth(360);

        VBox right = new VBox(10, new Label("Đơn hàng"), orderArea, totalLabel);
        right.setPadding(new Insets(12));
        right.setPrefWidth(360);

        HBox root = new HBox(12, left, right);
        Scene scene = new Scene(root, 760, 520);
        stage.setTitle("Coffee Shop");
        stage.setScene(scene);
        stage.show();
        updateOrderArea(orderArea, totalLabel);
    }

    private void updateOrderArea(TextArea orderArea, Label totalLabel) {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : currentOrder.getOrderItems()) {
            sb.append(item.getMenuItem().getName())
              .append(" x")
              .append(item.getQuantity())
              .append(" = $")
              .append(String.format("%.2f", item.getMenuItem().calculatePrice() * item.getQuantity()))
              .append("\n");
        }
        sb.append("\nTạm tính: $").append(String.format("%.2f", currentOrder.getSubtotal()));
        sb.append("\nThuế: $").append(String.format("%.2f", currentOrder.getTax()));
        sb.append("\nTổng: $").append(String.format("%.2f", currentOrder.getTotalAmount()));
        orderArea.setText(sb.toString());
        totalLabel.setText("Tổng: $" + String.format("%.2f", currentOrder.getTotalAmount()));
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

