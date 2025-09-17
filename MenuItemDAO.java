import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for MenuItem operations
 * Handles all database operations related to menu items
 */
public class MenuItemDAO {
    private DatabaseConnection dbConnection;
    
    public MenuItemDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    // Create a new menu item
    public boolean createMenuItem(MenuItem menuItem) {
        String query = "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, menuItem.getName());
            pstmt.setString(2, menuItem.getDescription());
            pstmt.setDouble(3, menuItem.getPrice());
            pstmt.setString(4, menuItem.getCategory());
            pstmt.setString(5, menuItem.getItemType());
            
            // Handle coffee-specific data
            if (menuItem instanceof Coffee) {
                Coffee coffee = (Coffee) menuItem;
                pstmt.setString(6, coffee.getCoffeeType().toString());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }
            
            pstmt.setBoolean(7, menuItem.isAvailable());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating menu item: " + e.getMessage());
            return false;
        }
    }
    
    // Get menu item by ID
    public MenuItem getMenuItemById(int id) {
        String query = "SELECT * FROM menu_items WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createMenuItemFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting menu item by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get all menu items
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items ORDER BY category, name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                MenuItem item = createMenuItemFromResultSet(rs);
                if (item != null) {
                    menuItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all menu items: " + e.getMessage());
        }
        
        return menuItems;
    }
    
    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE category = ? AND is_available = TRUE ORDER BY name";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MenuItem item = createMenuItemFromResultSet(rs);
                if (item != null) {
                    menuItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting menu items by category: " + e.getMessage());
        }
        
        return menuItems;
    }
    
    // Get available menu items
    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE is_available = TRUE ORDER BY category, name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                MenuItem item = createMenuItemFromResultSet(rs);
                if (item != null) {
                    menuItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available menu items: " + e.getMessage());
        }
        
        return menuItems;
    }
    
    // Update menu item
    public boolean updateMenuItem(MenuItem menuItem) {
        String query = "UPDATE menu_items SET name = ?, description = ?, base_price = ?, " +
                      "category = ?, is_available = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, menuItem.getName());
            pstmt.setString(2, menuItem.getDescription());
            pstmt.setDouble(3, menuItem.getPrice());
            pstmt.setString(4, menuItem.getCategory());
            pstmt.setBoolean(5, menuItem.isAvailable());
            pstmt.setInt(6, menuItem.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            return false;
        }
    }
    
    // Update menu item availability
    public boolean updateMenuItemAvailability(int id, boolean isAvailable) {
        String query = "UPDATE menu_items SET is_available = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating menu item availability: " + e.getMessage());
            return false;
        }
    }
    
    // Delete menu item
    public boolean deleteMenuItem(int id) {
        String query = "DELETE FROM menu_items WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            return false;
        }
    }
    
    // Search menu items by name
    public List<MenuItem> searchMenuItemsByName(String searchTerm) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE name LIKE ? AND is_available = TRUE ORDER BY name";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MenuItem item = createMenuItemFromResultSet(rs);
                if (item != null) {
                    menuItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching menu items: " + e.getMessage());
        }
        
        return menuItems;
    }
    
    // Get menu items by price range
    public List<MenuItem> getMenuItemsByPriceRange(double minPrice, double maxPrice) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE base_price BETWEEN ? AND ? AND is_available = TRUE ORDER BY base_price";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, minPrice);
            pstmt.setDouble(2, maxPrice);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MenuItem item = createMenuItemFromResultSet(rs);
                if (item != null) {
                    menuItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting menu items by price range: " + e.getMessage());
        }
        
        return menuItems;
    }
    
    // Get distinct categories
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT category FROM menu_items WHERE is_available = TRUE ORDER BY category";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        
        return categories;
    }
    
    // Helper method to create MenuItem object from ResultSet
    private MenuItem createMenuItemFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double basePrice = rs.getDouble("base_price");
        String category = rs.getString("category");
        String itemType = rs.getString("item_type");
        String coffeeTypeStr = rs.getString("coffee_type");
        boolean isAvailable = rs.getBoolean("is_available");
        
        MenuItem menuItem;
        
        // Create appropriate subclass based on item type
        if ("Coffee".equals(itemType) && coffeeTypeStr != null) {
            try {
                Coffee.CoffeeType coffeeType = Coffee.CoffeeType.valueOf(coffeeTypeStr);
                menuItem = new Coffee(id, name, description, basePrice, coffeeType, Coffee.Size.MEDIUM, true);
            } catch (IllegalArgumentException e) {
                // If coffee type is invalid, create a generic coffee
                menuItem = new Coffee(id, name, description, basePrice, Coffee.CoffeeType.AMERICANO, Coffee.Size.MEDIUM, true);
            }
        } else {
            // Create a generic MenuItem using an anonymous subclass
            menuItem = new MenuItem(id, name, description, basePrice, category) {
                @Override
                public String getItemType() {
                    return itemType;
                }
            };
        }
        
        menuItem.setAvailable(isAvailable);
        return menuItem;
    }
    
    // Get menu item count
    public int getMenuItemCount() {
        String query = "SELECT COUNT(*) as count FROM menu_items";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting menu item count: " + e.getMessage());
        }
        
        return 0;
    }
    
    // Get available menu item count
    public int getAvailableMenuItemCount() {
        String query = "SELECT COUNT(*) as count FROM menu_items WHERE is_available = TRUE";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available menu item count: " + e.getMessage());
        }
        
        return 0;
    }
}