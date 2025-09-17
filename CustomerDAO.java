import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer operations
 * Handles all database operations related to customers
 */
public class CustomerDAO {
    private DatabaseConnection dbConnection;
    
    public CustomerDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    // Create a new customer
    public int createCustomer(Customer customer) {
        String query = "INSERT INTO customers (name, email, phone_number, loyalty_points) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setDouble(4, customer.getLoyaltyPoints());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating customer: " + e.getMessage());
        }
        
        return -1;
    }
    
    // Get customer by ID
    public Customer getCustomerById(int customerId) {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createCustomerFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting customer by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get customer by email
    public Customer getCustomerByEmail(String email) {
        String query = "SELECT * FROM customers WHERE email = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createCustomerFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting customer by email: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get customer by phone number
    public Customer getCustomerByPhone(String phoneNumber) {
        String query = "SELECT * FROM customers WHERE phone_number = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createCustomerFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting customer by phone: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Customer customer = createCustomerFromResultSet(rs);
                if (customer != null) {
                    customers.add(customer);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all customers: " + e.getMessage());
        }
        
        return customers;
    }
    
    // Update customer
    public boolean updateCustomer(Customer customer) {
        String query = "UPDATE customers SET name = ?, email = ?, phone_number = ?, " +
                      "loyalty_points = ?, updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setDouble(4, customer.getLoyaltyPoints());
            pstmt.setInt(5, customer.getCustomerId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }
    
    // Update customer loyalty points
    public boolean updateLoyaltyPoints(int customerId, double loyaltyPoints) {
        String query = "UPDATE customers SET loyalty_points = ?, updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, loyaltyPoints);
            pstmt.setInt(2, customerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating loyalty points: " + e.getMessage());
            return false;
        }
    }
    
    // Add loyalty points
    public boolean addLoyaltyPoints(int customerId, double pointsToAdd) {
        String query = "UPDATE customers SET loyalty_points = loyalty_points + ?, " +
                      "updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, pointsToAdd);
            pstmt.setInt(2, customerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding loyalty points: " + e.getMessage());
            return false;
        }
    }
    
    // Redeem loyalty points
    public boolean redeemLoyaltyPoints(int customerId, double pointsToRedeem) {
        // First check if customer has enough points
        Customer customer = getCustomerById(customerId);
        if (customer == null || customer.getLoyaltyPoints() < pointsToRedeem) {
            return false;
        }
        
        String query = "UPDATE customers SET loyalty_points = loyalty_points - ?, " +
                      "updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setDouble(1, pointsToRedeem);
            pstmt.setInt(2, customerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error redeeming loyalty points: " + e.getMessage());
            return false;
        }
    }
    
    // Delete customer
    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM customers WHERE customer_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }
    
    // Search customers by name
    public List<Customer> searchCustomersByName(String searchTerm) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers WHERE name LIKE ? ORDER BY name";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Customer customer = createCustomerFromResultSet(rs);
                if (customer != null) {
                    customers.add(customer);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching customers: " + e.getMessage());
        }
        
        return customers;
    }
    
    // Get customers with high loyalty points
    public List<Customer> getTopLoyaltyCustomers(int limit) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY loyalty_points DESC LIMIT ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Customer customer = createCustomerFromResultSet(rs);
                if (customer != null) {
                    customers.add(customer);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting top loyalty customers: " + e.getMessage());
        }
        
        return customers;
    }
    
    // Check if email exists
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) as count FROM customers WHERE email = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        
        return false;
    }
    
    // Get customer statistics
    public CustomerStats getCustomerStats() {
        String query = "SELECT COUNT(*) as total_customers, " +
                      "AVG(loyalty_points) as avg_loyalty_points, " +
                      "MAX(loyalty_points) as max_loyalty_points, " +
                      "SUM(loyalty_points) as total_loyalty_points " +
                      "FROM customers";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return new CustomerStats(
                    rs.getInt("total_customers"),
                    rs.getDouble("avg_loyalty_points"),
                    rs.getDouble("max_loyalty_points"),
                    rs.getDouble("total_loyalty_points")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting customer statistics: " + e.getMessage());
        }
        
        return new CustomerStats(0, 0.0, 0.0, 0.0);
    }
    
    // Helper method to create Customer object from ResultSet
    private Customer createCustomerFromResultSet(ResultSet rs) throws SQLException {
        int customerId = rs.getInt("customer_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phoneNumber = rs.getString("phone_number");
        double loyaltyPoints = rs.getDouble("loyalty_points");
        
        Customer customer = new Customer(customerId, name, email, phoneNumber);
        customer.addLoyaltyPoints(loyaltyPoints);
        
        return customer;
    }
    
    // Inner class for customer statistics
    public static class CustomerStats {
        private final int totalCustomers;
        private final double avgLoyaltyPoints;
        private final double maxLoyaltyPoints;
        private final double totalLoyaltyPoints;
        
        public CustomerStats(int totalCustomers, double avgLoyaltyPoints, 
                           double maxLoyaltyPoints, double totalLoyaltyPoints) {
            this.totalCustomers = totalCustomers;
            this.avgLoyaltyPoints = avgLoyaltyPoints;
            this.maxLoyaltyPoints = maxLoyaltyPoints;
            this.totalLoyaltyPoints = totalLoyaltyPoints;
        }
        
        public int getTotalCustomers() { return totalCustomers; }
        public double getAvgLoyaltyPoints() { return avgLoyaltyPoints; }
        public double getMaxLoyaltyPoints() { return maxLoyaltyPoints; }
        public double getTotalLoyaltyPoints() { return totalLoyaltyPoints; }
        
        @Override
        public String toString() {
            return String.format("Customer Statistics:\n" +
                               "Total Customers: %d\n" +
                               "Average Loyalty Points: %.2f\n" +
                               "Maximum Loyalty Points: %.2f\n" +
                               "Total Loyalty Points: %.2f",
                               totalCustomers, avgLoyaltyPoints, maxLoyaltyPoints, totalLoyaltyPoints);
        }
    }
}