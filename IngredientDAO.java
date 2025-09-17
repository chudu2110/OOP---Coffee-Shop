import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Ingredient operations
 * Handles all database operations related to ingredient management
 */
public class IngredientDAO {
    private DatabaseConnection dbConnection;
    
    public IngredientDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    // Create a new ingredient
    public int createIngredient(Ingredient ingredient) {
        String query = "INSERT INTO ingredients (name, current_stock, minimum_stock, maximum_stock, " +
                      "unit, cost_per_unit, supplier, expiration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getCurrentStock());
            pstmt.setDouble(3, ingredient.getMinimumStock());
            pstmt.setDouble(4, ingredient.getMaximumStock());
            pstmt.setString(5, ingredient.getUnit().toString());
            pstmt.setDouble(6, ingredient.getCostPerUnit());
            pstmt.setString(7, ingredient.getSupplier());
            if (ingredient.getExpirationDate() != null) {
                pstmt.setDate(8, Date.valueOf(ingredient.getExpirationDate()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating ingredient: " + e.getMessage());
        }
        
        return -1;
    }
    
    // Get ingredient by ID
    public Ingredient getIngredientById(int ingredientId) {
        String query = "SELECT * FROM ingredients WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, ingredientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createIngredientFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting ingredient by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get ingredient by name
    public Ingredient getIngredientByName(String name) {
        String query = "SELECT * FROM ingredients WHERE name = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createIngredientFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting ingredient by name: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get all ingredients
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Get ingredients with low stock
    public List<Ingredient> getLowStockIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE current_stock <= minimum_stock ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting low stock ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Get out of stock ingredients
    public List<Ingredient> getOutOfStockIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE current_stock = 0 ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting out of stock ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Get expired ingredients
    public List<Ingredient> getExpiredIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE expiration_date < CURRENT_DATE ORDER BY expiration_date";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting expired ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Get ingredients expiring soon (within specified days)
    public List<Ingredient> getIngredientExpiringSoon(int days) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE expiration_date BETWEEN CURRENT_DATE AND DATE('now', '+' || ? || ' days') ORDER BY expiration_date";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, days);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting ingredients expiring soon: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Get ingredients by supplier
    public List<Ingredient> getIngredientsBySupplier(String supplier) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE supplier LIKE ? ORDER BY name";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, "%" + supplier + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting ingredients by supplier: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Update ingredient stock
    public boolean updateIngredientStock(int ingredientId, double newStock) {
        String query = "UPDATE ingredients SET current_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, newStock);
            pstmt.setInt(2, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating ingredient stock: " + e.getMessage());
            return false;
        }
    }
    
    // Add stock to ingredient
    public boolean addStock(int ingredientId, double quantity) {
        String query = "UPDATE ingredients SET current_stock = current_stock + ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, quantity);
            pstmt.setInt(2, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding stock: " + e.getMessage());
            return false;
        }
    }
    
    // Remove stock from ingredient
    public boolean removeStock(int ingredientId, double quantity) {
        // First check if there's enough stock
        Ingredient ingredient = getIngredientById(ingredientId);
        if (ingredient == null || ingredient.getCurrentStock() < quantity) {
            return false;
        }
        
        String query = "UPDATE ingredients SET current_stock = current_stock - ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, quantity);
            pstmt.setInt(2, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing stock: " + e.getMessage());
            return false;
        }
    }
    
    // Update ingredient cost per unit
    public boolean updateCostPerUnit(int ingredientId, double costPerUnit) {
        String query = "UPDATE ingredients SET cost_per_unit = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, costPerUnit);
            pstmt.setInt(2, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating cost per unit: " + e.getMessage());
            return false;
        }
    }
    
    // Update ingredient expiration date
    public boolean updateExpirationDate(int ingredientId, LocalDate expirationDate) {
        String query = "UPDATE ingredients SET expiration_date = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            if (expirationDate != null) {
                pstmt.setDate(1, Date.valueOf(expirationDate));
            } else {
                pstmt.setNull(1, Types.DATE);
            }
            pstmt.setInt(2, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating expiration date: " + e.getMessage());
            return false;
        }
    }
    
    // Update ingredient supplier
    public boolean updateSupplier(int ingredientId, String supplier) {
        String query = "UPDATE ingredients SET supplier = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, supplier);
            pstmt.setInt(2, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating supplier: " + e.getMessage());
            return false;
        }
    }
    
    // Update stock levels (minimum and maximum)
    public boolean updateStockLevels(int ingredientId, double minimumStock, double maximumStock) {
        String query = "UPDATE ingredients SET minimum_stock = ?, maximum_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, minimumStock);
            pstmt.setDouble(2, maximumStock);
            pstmt.setInt(3, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating stock levels: " + e.getMessage());
            return false;
        }
    }
    
    // Update entire ingredient
    public boolean updateIngredient(Ingredient ingredient) {
        String query = "UPDATE ingredients SET name = ?, current_stock = ?, minimum_stock = ?, " +
                      "maximum_stock = ?, unit = ?, cost_per_unit = ?, supplier = ?, " +
                      "expiration_date = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getCurrentStock());
            pstmt.setDouble(3, ingredient.getMinimumStock());
            pstmt.setDouble(4, ingredient.getMaximumStock());
            pstmt.setString(5, ingredient.getUnit().toString());
            pstmt.setDouble(6, ingredient.getCostPerUnit());
            pstmt.setString(7, ingredient.getSupplier());
            if (ingredient.getExpirationDate() != null) {
                pstmt.setDate(8, Date.valueOf(ingredient.getExpirationDate()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            pstmt.setInt(9, ingredient.getIngredientId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating ingredient: " + e.getMessage());
            return false;
        }
    }
    
    // Delete ingredient
    public boolean deleteIngredient(int ingredientId) {
        String query = "DELETE FROM ingredients WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, ingredientId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting ingredient: " + e.getMessage());
            return false;
        }
    }
    
    // Search ingredients by name
    public List<Ingredient> searchIngredientsByName(String searchTerm) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE name LIKE ? ORDER BY name";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // Check if ingredient name exists
    public boolean ingredientNameExists(String name) {
        return getIngredientByName(name) != null;
    }
    
    // Get ingredient statistics
    public IngredientStats getIngredientStats() {
        String query = "SELECT COUNT(*) as total_ingredients, " +
                      "COUNT(CASE WHEN current_stock <= minimum_stock THEN 1 END) as low_stock_count, " +
                      "COUNT(CASE WHEN current_stock = 0 THEN 1 END) as out_of_stock_count, " +
                      "COUNT(CASE WHEN expiration_date < CURRENT_DATE THEN 1 END) as expired_count, " +
                      "SUM(current_stock * cost_per_unit) as total_inventory_value, " +
                      "AVG(current_stock * cost_per_unit) as avg_ingredient_value " +
                      "FROM ingredients";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return new IngredientStats(
                    rs.getInt("total_ingredients"),
                    rs.getInt("low_stock_count"),
                    rs.getInt("out_of_stock_count"),
                    rs.getInt("expired_count"),
                    rs.getDouble("total_inventory_value"),
                    rs.getDouble("avg_ingredient_value")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting ingredient statistics: " + e.getMessage());
        }
        
        return new IngredientStats(0, 0, 0, 0, 0.0, 0.0);
    }
    
    // Get all unique suppliers
    public List<String> getAllSuppliers() {
        List<String> suppliers = new ArrayList<>();
        String query = "SELECT DISTINCT supplier FROM ingredients WHERE supplier IS NOT NULL ORDER BY supplier";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                suppliers.add(rs.getString("supplier"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting suppliers: " + e.getMessage());
        }
        
        return suppliers;
    }
    
    // Helper method to create Ingredient object from ResultSet
    private Ingredient createIngredientFromResultSet(ResultSet rs) throws SQLException {
        int ingredientId = rs.getInt("ingredient_id");
        String name = rs.getString("name");
        double currentStock = rs.getDouble("current_stock");
        double minimumStock = rs.getDouble("minimum_stock");
        double maximumStock = rs.getDouble("maximum_stock");
        String unitStr = rs.getString("unit");
        double costPerUnit = rs.getDouble("cost_per_unit");
        String supplier = rs.getString("supplier");
        Date expirationDate = rs.getDate("expiration_date");
        
        Ingredient.Unit unit = Ingredient.Unit.valueOf(unitStr);
        
        Ingredient ingredient = new Ingredient(ingredientId, name, "", unit, minimumStock, costPerUnit);
        ingredient.addStock(currentStock);
        ingredient.setMaximumStock(maximumStock);
        ingredient.setSupplier(supplier);
        
        if (expirationDate != null) {
            ingredient.setExpirationDate(expirationDate.toLocalDate());
        }
        
        return ingredient;
    }
    
    // Inner class for ingredient statistics
    public static class IngredientStats {
        private final int totalIngredients;
        private final int lowStockCount;
        private final int outOfStockCount;
        private final int expiredCount;
        private final double totalInventoryValue;
        private final double avgIngredientValue;
        
        public IngredientStats(int totalIngredients, int lowStockCount, int outOfStockCount,
                              int expiredCount, double totalInventoryValue, double avgIngredientValue) {
            this.totalIngredients = totalIngredients;
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
            this.expiredCount = expiredCount;
            this.totalInventoryValue = totalInventoryValue;
            this.avgIngredientValue = avgIngredientValue;
        }
        
        // Getters
        public int getTotalIngredients() { return totalIngredients; }
        public int getLowStockCount() { return lowStockCount; }
        public int getOutOfStockCount() { return outOfStockCount; }
        public int getExpiredCount() { return expiredCount; }
        public double getTotalInventoryValue() { return totalInventoryValue; }
        public double getAvgIngredientValue() { return avgIngredientValue; }
        
        // Calculate percentages
        public double getLowStockPercentage() {
            if (totalIngredients == 0) return 0.0;
            return ((double) lowStockCount / totalIngredients) * 100.0;
        }
        
        public double getOutOfStockPercentage() {
            if (totalIngredients == 0) return 0.0;
            return ((double) outOfStockCount / totalIngredients) * 100.0;
        }
        
        public double getExpiredPercentage() {
            if (totalIngredients == 0) return 0.0;
            return ((double) expiredCount / totalIngredients) * 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("Ingredient Statistics:\n" +
                               "Total Ingredients: %d\n" +
                               "Low Stock: %d (%.1f%%)\n" +
                               "Out of Stock: %d (%.1f%%)\n" +
                               "Expired: %d (%.1f%%)\n" +
                               "Total Inventory Value: $%.2f\n" +
                               "Average Ingredient Value: $%.2f",
                               totalIngredients,
                               lowStockCount, getLowStockPercentage(),
                               outOfStockCount, getOutOfStockPercentage(),
                               expiredCount, getExpiredPercentage(),
                               totalInventoryValue, avgIngredientValue);
        }
    }
}