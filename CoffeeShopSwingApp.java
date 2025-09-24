import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CoffeeShopSwingApp extends JFrame {
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final Order currentOrder = new Order(0, 1, Order.ServiceType.TAKEAWAY);

    private final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    private final JList<MenuItem> menuList = new JList<>(menuModel);
    private final JComboBox<String> categoryCombo = new JComboBox<>();
    private final JTextArea orderArea = new JTextArea(14, 30);
    private final JLabel totalLabel = new JLabel();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

    public CoffeeShopSwingApp() {
        super("Coffee Shop - Swing");
        // Initialize database first and wait for completion
        DatabaseConnection db = DatabaseConnection.getInstance();
        if (!db.initializeDatabase()) {
            JOptionPane.showMessageDialog(null, "Không thể khởi tạo cơ sở dữ liệu");
            System.exit(1);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 560);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JLabel title = new JLabel("Coffee Shop - Swing");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        root.add(center, BorderLayout.CENTER);

        // Left panel
        JPanel left = new JPanel(new BorderLayout(8, 8));
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeft.add(new JLabel("Danh mục:"));
        categoryCombo.addItem("Tất cả");
        for (String c : menuItemDAO.getCategories()) { categoryCombo.addItem(c); }
        categoryCombo.addActionListener(e -> loadMenu());
        topLeft.add(categoryCombo);
        left.add(topLeft, BorderLayout.NORTH);

        menuList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MenuItem) {
                    MenuItem mi = (MenuItem) value;
                    setText(mi.getName() + " - $" + String.format("%.2f", mi.getPrice()));
                }
                return c;
            }
        });
        JScrollPane menuScroll = new JScrollPane(menuList);
        left.add(menuScroll, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("SL:"));
        controls.add(qtySpinner);
        JButton addBtn = new JButton(new AbstractAction("Thêm vào giỏ") {
            @Override public void actionPerformed(ActionEvent e) { onAdd(); }
        });
        JButton clearBtn = new JButton(new AbstractAction("Xóa giỏ") {
            @Override public void actionPerformed(ActionEvent e) { currentOrder.clearOrder(); updateOrderArea(); }
        });
        JButton checkoutBtn = new JButton(new AbstractAction("Thanh toán") {
            @Override public void actionPerformed(ActionEvent e) { onCheckout(); }
        });
        controls.add(addBtn);
        controls.add(clearBtn);
        controls.add(checkoutBtn);
        left.add(controls, BorderLayout.SOUTH);

        center.add(left);

        // Right panel
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(new JLabel("Đơn hàng"), BorderLayout.NORTH);
        orderArea.setEditable(false);
        right.add(new JScrollPane(orderArea), BorderLayout.CENTER);
        right.add(totalLabel, BorderLayout.SOUTH);
        center.add(right);

        loadMenu();
        updateOrderArea();
    }

    private void loadMenu() {
        menuModel.clear();
        String sel = (String) categoryCombo.getSelectedItem();
        List<MenuItem> items = (sel == null || sel.equals("Tất cả")) ?
                menuItemDAO.getAvailableMenuItems() : menuItemDAO.getMenuItemsByCategory(sel);
        for (MenuItem mi : items) menuModel.addElement(mi);
    }

    private void onAdd() {
        MenuItem selected = menuList.getSelectedValue();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Chưa chọn món"); return; }
        int qty = (Integer) qtySpinner.getValue();
        currentOrder.addItem(selected, qty);
        updateOrderArea();
    }

    private void onCheckout() {
        if (currentOrder.isEmpty()) { JOptionPane.showMessageDialog(this, "Giỏ hàng trống"); return; }
        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.createOrder(currentOrder);
        if (orderId > 0) {
            JOptionPane.showMessageDialog(this, "Đã lưu đơn #" + orderId + ". Tổng: $" + String.format("%.2f", currentOrder.getTotalAmount()));
            currentOrder.clearOrder();
            updateOrderArea();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu đơn thất bại");
        }
    }

    private void updateOrderArea() {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CoffeeShopSwingApp().setVisible(true));
    }
}

