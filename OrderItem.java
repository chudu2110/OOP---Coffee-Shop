/**
 * OrderItem class representing individual items within an order
 * Encapsulates menu item and quantity information
 */
public class OrderItem {
    private MenuItem menuItem;
    private int quantity;
    private String customizations;
    
    // Constructor
    public OrderItem(MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("MenuItem cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.customizations = "";
    }
    
    // Constructor with customizations
    public OrderItem(MenuItem menuItem, int quantity, String customizations) {
        this(menuItem, quantity);
        this.customizations = customizations != null ? customizations : "";
    }
    
    // Getters
    public MenuItem getMenuItem() {
        return menuItem;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public String getCustomizations() {
        return customizations;
    }
    
    // Setters
    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        }
    }
    
    public void setCustomizations(String customizations) {
        this.customizations = customizations != null ? customizations : "";
    }
    
    // Methods
    public double getItemTotal() {
        return menuItem.calculatePrice() * quantity;
    }
    
    public double getUnitPrice() {
        return menuItem.calculatePrice();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s x%d = $%.2f", 
                menuItem.getName(), quantity, getItemTotal()));
        
        if (!customizations.isEmpty()) {
            sb.append(String.format(" (Customizations: %s)", customizations));
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItem orderItem = (OrderItem) obj;
        return menuItem.getId() == orderItem.menuItem.getId();
    }
    
    @Override
    public int hashCode() {
        return menuItem.hashCode();
    }
}