import java.util.ArrayList;
import java.util.List;

/**
 * Coffee class extending MenuItem
 * Represents coffee items with size options and customizations
 */
public class Coffee extends MenuItem {
    public enum Size {
        SMALL(1.0), MEDIUM(1.3), LARGE(1.6);
        
        private final double multiplier;
        
        Size(double multiplier) {
            this.multiplier = multiplier;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
    }
    
    public enum CoffeeType {
        ESPRESSO, AMERICANO, LATTE, CAPPUCCINO, MACCHIATO, MOCHA, FRAPPUCCINO
    }
    
    private CoffeeType coffeeType;
    private Size size;
    private List<String> customizations;
    private boolean isHot;
    
    // Constructors
    public Coffee(int id, String name, String description, double basePrice, 
                  CoffeeType coffeeType, Size size, boolean isHot) {
        super(id, name, description, basePrice, "Coffee");
        this.coffeeType = coffeeType;
        this.size = size;
        this.isHot = isHot;
        this.customizations = new ArrayList<>();
    }
    
    // Simplified constructor for basic coffee creation
    public Coffee(int id, String name, double basePrice, String category, String description) {
        super(id, name, description, basePrice, category);
        this.coffeeType = CoffeeType.AMERICANO; // Default type
        this.size = Size.MEDIUM; // Default size
        this.isHot = true; // Default to hot
        this.customizations = new ArrayList<>();
    }
    
    // Getters
    public CoffeeType getCoffeeType() {
        return coffeeType;
    }
    
    public Size getSize() {
        return size;
    }
    
    public List<String> getCustomizations() {
        return new ArrayList<>(customizations);
    }
    
    public boolean isHot() {
        return isHot;
    }
    
    // Setters
    public void setSize(Size size) {
        this.size = size;
    }
    
    public void setHot(boolean hot) {
        this.isHot = hot;
    }
    
    // Methods
    public void addCustomization(String customization) {
        if (customization != null && !customization.trim().isEmpty()) {
            customizations.add(customization.trim());
        }
    }
    
    public void removeCustomization(String customization) {
        customizations.remove(customization);
    }
    
    public void clearCustomizations() {
        customizations.clear();
    }
    
    @Override
    public String getItemType() {
        return "Coffee";
    }
    
    @Override
    public double calculatePrice() {
        double finalPrice = getPrice() * size.getMultiplier();
        
        // Add extra cost for customizations (e.g., $0.50 per customization)
        finalPrice += customizations.size() * 0.50;
        
        return finalPrice;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(String.format("\nType: %s | Size: %s | Temperature: %s", 
                coffeeType, size, isHot ? "Hot" : "Cold"));
        
        if (!customizations.isEmpty()) {
            sb.append("\nCustomizations: ");
            sb.append(String.join(", ", customizations));
        }
        
        sb.append(String.format("\nFinal Price: $%.2f", calculatePrice()));
        
        return sb.toString();
    }
}