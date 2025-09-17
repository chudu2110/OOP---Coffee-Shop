/**
 * Abstract base class for all menu items in the coffee shop
 * Follows OOP principles with encapsulation and abstraction
 */
public abstract class MenuItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private boolean available;
    private String category;
    
    // Constructor
    public MenuItem(int id, String name, String description, double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = true;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public double getBasePrice() {
        return price;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public String getCategory() {
        return category;
    }
    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }
    
    public void setBasePrice(double price) {
        setPrice(price);
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    // Abstract method to be implemented by subclasses
    public abstract String getItemType();
    
    // Method to calculate final price (can be overridden for customizations)
    public double calculatePrice() {
        return this.price;
    }
    
    @Override
    public String toString() {
        return String.format("%s - $%.2f\n%s", name, price, description);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MenuItem menuItem = (MenuItem) obj;
        return id == menuItem.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}