import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order class representing customer orders
 * Manages order items, payment, and order status
 */
public class Order {
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED
    }
    
    public enum ServiceType {
        DINE_IN, TAKEAWAY
    }
    
    private int orderId;
    private int customerId;
    private List<OrderItem> orderItems;
    private OrderStatus status;
    private ServiceType serviceType;
    private LocalDateTime orderTime;
    private LocalDateTime completionTime;
    private double subtotal;
    private double tax;
    private double discount;
    private double totalAmount;
    private int tableNumber; // -1 for takeaway
    private String specialInstructions;
    
    // Constructor
    public Order(int orderId, int customerId, ServiceType serviceType) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.orderItems = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
        this.tableNumber = -1;
        this.specialInstructions = "";
        this.tax = 0.0;
        this.discount = 0.0;
    }
    
    // Getters
    public int getOrderId() {
        return orderId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public List<OrderItem> getOrderItems() {
        return new ArrayList<>(orderItems);
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public double getTax() {
        return tax;
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public int getTableNumber() {
        return tableNumber;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    // Setters
    public void setStatus(OrderStatus status) {
        this.status = status;
        if (status == OrderStatus.COMPLETED) {
            this.completionTime = LocalDateTime.now();
        }
    }
    
    public void setTableNumber(int tableNumber) {
        if (serviceType == ServiceType.DINE_IN && tableNumber > 0) {
            this.tableNumber = tableNumber;
        }
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions != null ? specialInstructions : "";
    }
    
    public void setDiscount(double discount) {
        if (discount >= 0) {
            this.discount = discount;
            calculateTotal();
        }
    }
    
    // Methods
    public void addItem(MenuItem menuItem, int quantity) {
        if (menuItem != null && quantity > 0) {
            // Check if item already exists in order
            for (OrderItem item : orderItems) {
                if (item.getMenuItem().getId() == menuItem.getId()) {
                    item.setQuantity(item.getQuantity() + quantity);
                    calculateTotal();
                    return;
                }
            }
            // Add new item
            orderItems.add(new OrderItem(menuItem, quantity));
            calculateTotal();
        }
    }
    
    public void removeItem(int menuItemId) {
        orderItems.removeIf(item -> item.getMenuItem().getId() == menuItemId);
        calculateTotal();
    }
    
    public void updateItemQuantity(int menuItemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(menuItemId);
            return;
        }
        
        for (OrderItem item : orderItems) {
            if (item.getMenuItem().getId() == menuItemId) {
                item.setQuantity(newQuantity);
                calculateTotal();
                return;
            }
        }
    }
    
    public void clearOrder() {
        orderItems.clear();
        calculateTotal();
    }
    
    private void calculateTotal() {
        subtotal = orderItems.stream()
                .mapToDouble(item -> item.getMenuItem().calculatePrice() * item.getQuantity())
                .sum();
        
        // Calculate tax (8% tax rate)
        tax = subtotal * 0.08;
        
        // Calculate total
        totalAmount = subtotal + tax - discount;
        
        // Ensure total is not negative
        if (totalAmount < 0) {
            totalAmount = 0;
        }
    }
    
    public boolean isEmpty() {
        return orderItems.isEmpty();
    }
    
    public int getTotalItems() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Order #%d (Customer ID: %d)\n", orderId, customerId));
        sb.append(String.format("Status: %s | Service: %s\n", status, serviceType));
        sb.append(String.format("Order Time: %s\n", orderTime));
        
        if (serviceType == ServiceType.DINE_IN && tableNumber > 0) {
            sb.append(String.format("Table: %d\n", tableNumber));
        }
        
        sb.append("\nItems:\n");
        for (OrderItem item : orderItems) {
            sb.append(String.format("- %s x%d = $%.2f\n", 
                    item.getMenuItem().getName(), 
                    item.getQuantity(), 
                    item.getMenuItem().calculatePrice() * item.getQuantity()));
        }
        
        sb.append(String.format("\nSubtotal: $%.2f\n", subtotal));
        sb.append(String.format("Tax: $%.2f\n", tax));
        if (discount > 0) {
            sb.append(String.format("Discount: -$%.2f\n", discount));
        }
        sb.append(String.format("Total: $%.2f\n", totalAmount));
        
        if (!specialInstructions.isEmpty()) {
            sb.append(String.format("Special Instructions: %s\n", specialInstructions));
        }
        
        return sb.toString();
    }
}