import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Customer class representing coffee shop customers
 * Encapsulates customer data and order history
 */
public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private List<Order> orderHistory;
    private double loyaltyPoints;
    private LocalDateTime registrationDate;
    
    // Constructor
    public Customer(int customerId, String name, String email, String phoneNumber) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.orderHistory = new ArrayList<>();
        this.loyaltyPoints = 0.0;
        this.registrationDate = LocalDateTime.now();
    }
    
    // Getters
    public int getCustomerId() {
        return customerId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public List<Order> getOrderHistory() {
        return new ArrayList<>(orderHistory);
    }
    
    public double getLoyaltyPoints() {
        return loyaltyPoints;
    }
    
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    // Setters
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }
    
    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email.trim();
        }
    }
    
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = phoneNumber.trim();
        }
    }
    
    // Methods
    public void addOrder(Order order) {
        if (order != null) {
            orderHistory.add(order);
            // Add loyalty points (1 point per dollar spent)
            addLoyaltyPoints(order.getTotalAmount());
        }
    }
    
    public void addLoyaltyPoints(double points) {
        if (points > 0) {
            this.loyaltyPoints += points;
        }
    }
    
    public boolean redeemLoyaltyPoints(double points) {
        if (points > 0 && this.loyaltyPoints >= points) {
            this.loyaltyPoints -= points;
            return true;
        }
        return false;
    }
    
    public double getTotalSpent() {
        return orderHistory.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
    
    public int getTotalOrders() {
        return orderHistory.size();
    }
    
    public Order getLastOrder() {
        if (!orderHistory.isEmpty()) {
            return orderHistory.get(orderHistory.size() - 1);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Customer: %s (ID: %d)\nEmail: %s\nPhone: %s\nLoyalty Points: %.2f\nTotal Orders: %d\nTotal Spent: $%.2f",
                name, customerId, email, phoneNumber, loyaltyPoints, getTotalOrders(), getTotalSpent());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return customerId == customer.customerId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(customerId);
    }
}