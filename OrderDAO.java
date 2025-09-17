import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Order operations
 * Handles all database operations related to orders and order items
 */
public class OrderDAO {
    private DatabaseConnection dbConnection;
    private MenuItemDAO menuItemDAO;
    
    public OrderDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.menuItemDAO = new MenuItemDAO();
    }
    
    // Create a new order
    public int createOrder(Order order) {
        String orderQuery = "INSERT INTO orders (customer_id, table_id, service_type, status, total_amount, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = dbConnection.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            // Insert order
            try (PreparedStatement pstmt = conn.prepareStatement(orderQuery)) {
                pstmt.setInt(1, order.getCustomerId());
                if (order.getTableNumber() > 0) {
                    pstmt.setInt(2, order.getTableNumber());
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.setString(3, order.getServiceType().toString());
                pstmt.setString(4, order.getStatus().toString());
                pstmt.setDouble(5, order.getTotalAmount());
                pstmt.setString(6, order.getSpecialInstructions());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        
                        // Insert order items
                        if (insertOrderItems(orderId, order.getOrderItems(), conn)) {
                            conn.commit();
                            return orderId;
                        }
                    }
                }
            }
            
            conn.rollback();
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.err.println("Error creating order: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        
        return -1;
    }
    
    // Insert order items
    private boolean insertOrderItems(int orderId, List<OrderItem> orderItems, Connection conn) throws SQLException {
        String itemQuery = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, customizations) " +
                          "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(itemQuery)) {
            for (OrderItem item : orderItems) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getUnitPrice());
                pstmt.setString(5, item.getCustomizations());
                
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            
            // Check if all items were inserted successfully
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    // Get order by ID
    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE order_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                if (order != null) {
                    // Load order items
                    List<OrderItem> orderItems = getOrderItems(orderId);
                    for (OrderItem item : orderItems) {
                        order.addItem(item.getMenuItem(), item.getQuantity());
                    }
                }
                return order;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get order items for a specific order
    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int menuItemId = rs.getInt("menu_item_id");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getDouble("unit_price");
                String customizations = rs.getString("customizations");
                
                MenuItem menuItem = menuItemDAO.getMenuItemById(menuItemId);
                if (menuItem != null) {
                    OrderItem orderItem = new OrderItem(menuItem, quantity, customizations);
                    orderItems.add(orderItem);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order items: " + e.getMessage());
        }
        
        return orderItems;
    }
    
    // Get orders by customer ID
    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE customer_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                if (order != null) {
                    // Load order items
                    List<OrderItem> orderItems = getOrderItems(order.getOrderId());
                    for (OrderItem item : orderItems) {
                        order.addItem(item.getMenuItem(), item.getQuantity());
                    }
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting orders by customer ID: " + e.getMessage());
        }
        
        return orders;
    }
    
    // Get orders by status
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE status = ? ORDER BY created_at ASC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                if (order != null) {
                    // Load order items
                    List<OrderItem> orderItems = getOrderItems(order.getOrderId());
                    for (OrderItem item : orderItems) {
                        order.addItem(item.getMenuItem(), item.getQuantity());
                    }
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting orders by status: " + e.getMessage());
        }
        
        return orders;
    }
    
    // Get orders by table ID
    public List<Order> getOrdersByTableId(int tableId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE table_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                if (order != null) {
                    // Load order items
                    List<OrderItem> orderItems = getOrderItems(order.getOrderId());
                    for (OrderItem item : orderItems) {
                        order.addItem(item.getMenuItem(), item.getQuantity());
                    }
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting orders by table ID: " + e.getMessage());
        }
        
        return orders;
    }
    
    // Get all orders
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders ORDER BY created_at DESC";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                if (order != null) {
                    // Load order items
                    List<OrderItem> orderItems = getOrderItems(order.getOrderId());
                    for (OrderItem item : orderItems) {
                        order.addItem(item.getMenuItem(), item.getQuantity());
                    }
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
        }
        
        return orders;
    }
    
    // Update order status
    public boolean updateOrderStatus(int orderId, Order.OrderStatus status) {
        String query = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, orderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }
    
    // Update order total amount
    public boolean updateOrderTotal(int orderId, double totalAmount) {
        String query = "UPDATE orders SET total_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, totalAmount);
            pstmt.setInt(2, orderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating order total: " + e.getMessage());
            return false;
        }
    }
    
    // Update order notes
    public boolean updateOrderNotes(int orderId, String notes) {
        String query = "UPDATE orders SET notes = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, notes);
            pstmt.setInt(2, orderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating order notes: " + e.getMessage());
            return false;
        }
    }
    
    // Delete order
    public boolean deleteOrder(int orderId) {
        Connection conn = dbConnection.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            // Delete order items first
            String deleteItemsQuery = "DELETE FROM order_items WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteItemsQuery)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }
            
            // Delete order
            String deleteOrderQuery = "DELETE FROM orders WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteOrderQuery)) {
                pstmt.setInt(1, orderId);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                }
            }
            
            conn.rollback();
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.err.println("Error deleting order: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    // Get orders by date range
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                if (order != null) {
                    // Load order items
                    List<OrderItem> orderItems = getOrderItems(order.getOrderId());
                    for (OrderItem item : orderItems) {
                        order.addItem(item.getMenuItem(), item.getQuantity());
                    }
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting orders by date range: " + e.getMessage());
        }
        
        return orders;
    }
    
    // Get order statistics
    public OrderStats getOrderStats() {
        String query = "SELECT COUNT(*) as total_orders, " +
                      "SUM(total_amount) as total_revenue, " +
                      "AVG(total_amount) as avg_order_value, " +
                      "COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_orders, " +
                      "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_orders, " +
                      "COUNT(CASE WHEN status = 'PREPARING' THEN 1 END) as preparing_orders, " +
                      "COUNT(CASE WHEN status = 'READY' THEN 1 END) as ready_orders, " +
                      "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_orders " +
                      "FROM orders";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return new OrderStats(
                    rs.getInt("total_orders"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("avg_order_value"),
                    rs.getInt("completed_orders"),
                    rs.getInt("pending_orders"),
                    rs.getInt("preparing_orders"),
                    rs.getInt("ready_orders"),
                    rs.getInt("cancelled_orders")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order statistics: " + e.getMessage());
        }
        
        return new OrderStats(0, 0.0, 0.0, 0, 0, 0, 0, 0);
    }
    
    // Helper method to create Order object from ResultSet
    private Order createOrderFromResultSet(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");
        int customerId = rs.getInt("customer_id");
        int tableId = rs.getInt("table_id");
        String serviceTypeStr = rs.getString("service_type");
        String statusStr = rs.getString("status");
        double totalAmount = rs.getDouble("total_amount");
        String notes = rs.getString("notes");
        
        Order.ServiceType serviceType = Order.ServiceType.valueOf(serviceTypeStr);
        Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr);
        
        Order order = new Order(orderId, customerId, serviceType);
        if (tableId > 0) {
            order.setTableNumber(tableId);
        }
        order.setStatus(status);
        order.setSpecialInstructions(notes);
        
        return order;
    }
    
    // Inner class for order statistics
    public static class OrderStats {
        private final int totalOrders;
        private final double totalRevenue;
        private final double avgOrderValue;
        private final int completedOrders;
        private final int pendingOrders;
        private final int preparingOrders;
        private final int readyOrders;
        private final int cancelledOrders;
        
        public OrderStats(int totalOrders, double totalRevenue, double avgOrderValue,
                         int completedOrders, int pendingOrders, int preparingOrders,
                         int readyOrders, int cancelledOrders) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.avgOrderValue = avgOrderValue;
            this.completedOrders = completedOrders;
            this.pendingOrders = pendingOrders;
            this.preparingOrders = preparingOrders;
            this.readyOrders = readyOrders;
            this.cancelledOrders = cancelledOrders;
        }
        
        // Getters
        public int getTotalOrders() { return totalOrders; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAvgOrderValue() { return avgOrderValue; }
        public int getCompletedOrders() { return completedOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public int getPreparingOrders() { return preparingOrders; }
        public int getReadyOrders() { return readyOrders; }
        public int getCancelledOrders() { return cancelledOrders; }
        
        @Override
        public String toString() {
            return String.format("Order Statistics:\n" +
                               "Total Orders: %d\n" +
                               "Total Revenue: $%.2f\n" +
                               "Average Order Value: $%.2f\n" +
                               "Completed Orders: %d\n" +
                               "Pending Orders: %d\n" +
                               "Preparing Orders: %d\n" +
                               "Ready Orders: %d\n" +
                               "Cancelled Orders: %d",
                               totalOrders, totalRevenue, avgOrderValue,
                               completedOrders, pendingOrders, preparingOrders,
                               readyOrders, cancelledOrders);
        }
    }
}