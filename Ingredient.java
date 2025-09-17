import java.time.LocalDate;

/**
 * Ingredient class representing ingredients used in coffee shop items
 * Manages inventory, expiration dates, and stock levels
 */
public class Ingredient {
    public enum Unit {
        GRAMS, KILOGRAMS, MILLILITERS, LITERS, PIECES, CUPS, TABLESPOONS, TEASPOONS
    }
    
    private int ingredientId;
    private String name;
    private String description;
    private Unit unit;
    private double currentStock;
    private double minimumStock;
    private double maximumStock;
    private double costPerUnit;
    private LocalDate expirationDate;
    private String supplier;
    private boolean isActive;
    
    // Constructor
    public Ingredient(int ingredientId, String name, String description, Unit unit, 
                     double minimumStock, double costPerUnit) {
        if (minimumStock < 0 || costPerUnit < 0) {
            throw new IllegalArgumentException("Stock and cost values cannot be negative");
        }
        
        this.ingredientId = ingredientId;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.currentStock = 0.0;
        this.minimumStock = minimumStock;
        this.maximumStock = minimumStock * 10; // Default max is 10x minimum
        this.costPerUnit = costPerUnit;
        this.supplier = "";
        this.isActive = true;
    }
    
    // Getters
    public int getIngredientId() {
        return ingredientId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Unit getUnit() {
        return unit;
    }
    
    public double getCurrentStock() {
        return currentStock;
    }
    
    public double getMinimumStock() {
        return minimumStock;
    }
    
    public double getMaximumStock() {
        return maximumStock;
    }
    
    public double getCostPerUnit() {
        return costPerUnit;
    }
    
    public LocalDate getExpirationDate() {
        return expirationDate;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    // Setters
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }
    
    public void setMinimumStock(double minimumStock) {
        if (minimumStock >= 0) {
            this.minimumStock = minimumStock;
        }
    }
    
    public void setMaximumStock(double maximumStock) {
        if (maximumStock >= minimumStock) {
            this.maximumStock = maximumStock;
        }
    }
    
    public void setCostPerUnit(double costPerUnit) {
        if (costPerUnit >= 0) {
            this.costPerUnit = costPerUnit;
        }
    }
    
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier != null ? supplier.trim() : "";
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    // Methods
    public boolean addStock(double quantity) {
        if (quantity > 0) {
            double newStock = currentStock + quantity;
            if (newStock <= maximumStock) {
                currentStock = newStock;
                return true;
            }
        }
        return false;
    }
    
    public boolean removeStock(double quantity) {
        if (quantity > 0 && currentStock >= quantity) {
            currentStock -= quantity;
            return true;
        }
        return false;
    }
    
    public boolean isLowStock() {
        return currentStock <= minimumStock;
    }
    
    public boolean isOutOfStock() {
        return currentStock <= 0;
    }
    
    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expirationDate);
    }
    
    public boolean isExpiringSoon(int daysThreshold) {
        if (expirationDate == null) {
            return false;
        }
        return LocalDate.now().plusDays(daysThreshold).isAfter(expirationDate) || 
               LocalDate.now().plusDays(daysThreshold).isEqual(expirationDate);
    }
    
    public double getStockValue() {
        return currentStock * costPerUnit;
    }
    
    public double getStockPercentage() {
        if (maximumStock == 0) {
            return 0;
        }
        return (currentStock / maximumStock) * 100;
    }
    
    public String getStockStatus() {
        if (isOutOfStock()) {
            return "OUT OF STOCK";
        } else if (isLowStock()) {
            return "LOW STOCK";
        } else if (currentStock >= maximumStock * 0.8) {
            return "WELL STOCKED";
        } else {
            return "NORMAL";
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s (ID: %d)\n", name, ingredientId));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Current Stock: %.2f %s\n", currentStock, unit.toString().toLowerCase()));
        sb.append(String.format("Stock Range: %.2f - %.2f %s\n", 
                minimumStock, maximumStock, unit.toString().toLowerCase()));
        sb.append(String.format("Status: %s\n", getStockStatus()));
        sb.append(String.format("Cost per %s: $%.2f\n", unit.toString().toLowerCase(), costPerUnit));
        sb.append(String.format("Total Value: $%.2f\n", getStockValue()));
        
        if (expirationDate != null) {
            sb.append(String.format("Expiration Date: %s", expirationDate));
            if (isExpired()) {
                sb.append(" (EXPIRED)");
            } else if (isExpiringSoon(7)) {
                sb.append(" (EXPIRES SOON)");
            }
            sb.append("\n");
        }
        
        if (!supplier.isEmpty()) {
            sb.append(String.format("Supplier: %s\n", supplier));
        }
        
        sb.append(String.format("Active: %s\n", isActive ? "Yes" : "No"));
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ingredient ingredient = (Ingredient) obj;
        return ingredientId == ingredient.ingredientId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(ingredientId);
    }
}