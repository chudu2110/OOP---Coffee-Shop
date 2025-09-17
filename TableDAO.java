import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Table operations
 * Handles all database operations related to table management
 */
public class TableDAO {
    private DatabaseConnection dbConnection;
    
    public TableDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    // Create a new table
    public int createTable(Table table) {
        String query = "INSERT INTO tables (table_number, capacity, status, location) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus().toString());
            pstmt.setString(4, table.getNotes());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
        
        return -1;
    }
    
    // Get table by ID
    public Table getTableById(int tableId) {
        String query = "SELECT * FROM tables WHERE table_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createTableFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting table by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get table by table number
    public Table getTableByNumber(int tableNumber) {
        String query = "SELECT * FROM tables WHERE table_number = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, tableNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createTableFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting table by number: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get all tables
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables ORDER BY table_number";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                if (table != null) {
                    tables.add(table);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all tables: " + e.getMessage());
        }
        
        return tables;
    }
    
    // Get tables by status
    public List<Table> getTablesByStatus(Table.TableStatus status) {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables WHERE status = ? ORDER BY table_number";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                if (table != null) {
                    tables.add(table);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tables by status: " + e.getMessage());
        }
        
        return tables;
    }
    
    // Get available tables
    public List<Table> getAvailableTables() {
        return getTablesByStatus(Table.TableStatus.AVAILABLE);
    }
    
    // Get occupied tables
    public List<Table> getOccupiedTables() {
        return getTablesByStatus(Table.TableStatus.OCCUPIED);
    }
    
    // Get reserved tables
    public List<Table> getReservedTables() {
        return getTablesByStatus(Table.TableStatus.RESERVED);
    }
    
    // Get tables by capacity
    public List<Table> getTablesByCapacity(int minCapacity, int maxCapacity) {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables WHERE capacity BETWEEN ? AND ? ORDER BY table_number";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, minCapacity);
            pstmt.setInt(2, maxCapacity);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                if (table != null) {
                    tables.add(table);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tables by capacity: " + e.getMessage());
        }
        
        return tables;
    }
    
    // Get tables by location
    public List<Table> getTablesByLocation(String location) {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables WHERE location LIKE ? ORDER BY table_number";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, "%" + location + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                if (table != null) {
                    tables.add(table);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tables by location: " + e.getMessage());
        }
        
        return tables;
    }
    
    // Update table status
    public boolean updateTableStatus(int tableId, Table.TableStatus status) {
        String query = "UPDATE tables SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE table_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, tableId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating table status: " + e.getMessage());
            return false;
        }
    }
    
    // Occupy table
    public boolean occupyTable(int tableId) {
        return updateTableStatus(tableId, Table.TableStatus.OCCUPIED);
    }
    
    // Reserve table
    public boolean reserveTable(int tableId) {
        return updateTableStatus(tableId, Table.TableStatus.RESERVED);
    }
    
    // Make table available
    public boolean makeTableAvailable(int tableId) {
        return updateTableStatus(tableId, Table.TableStatus.AVAILABLE);
    }
    
    // Set table out of service
    public boolean setTableOutOfService(int tableId) {
        return updateTableStatus(tableId, Table.TableStatus.OUT_OF_SERVICE);
    }
    
    // Update table capacity
    public boolean updateTableCapacity(int tableId, int capacity) {
        String query = "UPDATE tables SET capacity = ?, updated_at = CURRENT_TIMESTAMP WHERE table_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, capacity);
            pstmt.setInt(2, tableId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating table capacity: " + e.getMessage());
            return false;
        }
    }
    
    // Update table location
    public boolean updateTableLocation(int tableId, String location) {
        String query = "UPDATE tables SET location = ?, updated_at = CURRENT_TIMESTAMP WHERE table_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, location);
            pstmt.setInt(2, tableId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating table location: " + e.getMessage());
            return false;
        }
    }
    
    // Update table number
    public boolean updateTableNumber(int tableId, int newTableNumber) {
        // First check if the new table number already exists
        if (getTableByNumber(newTableNumber) != null) {
            System.err.println("Table number " + newTableNumber + " already exists");
            return false;
        }
        
        String query = "UPDATE tables SET table_number = ?, updated_at = CURRENT_TIMESTAMP WHERE table_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, newTableNumber);
            pstmt.setInt(2, tableId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating table number: " + e.getMessage());
            return false;
        }
    }
    
    // Delete table
    public boolean deleteTable(int tableId) {
        // Check if table has any active orders
        if (hasActiveOrders(tableId)) {
            System.err.println("Cannot delete table with active orders");
            return false;
        }
        
        String query = "DELETE FROM tables WHERE table_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting table: " + e.getMessage());
            return false;
        }
    }
    
    // Check if table has active orders
    private boolean hasActiveOrders(int tableId) {
        String query = "SELECT COUNT(*) as count FROM orders WHERE table_id = ? AND status IN ('PENDING', 'PREPARING', 'READY')";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking active orders: " + e.getMessage());
        }
        
        return false;
    }
    
    // Check if table number exists
    public boolean tableNumberExists(int tableNumber) {
        return getTableByNumber(tableNumber) != null;
    }
    
    // Get table statistics
    public TableStats getTableStats() {
        String query = "SELECT COUNT(*) as total_tables, " +
                      "COUNT(CASE WHEN status = 'AVAILABLE' THEN 1 END) as available_tables, " +
                      "COUNT(CASE WHEN status = 'OCCUPIED' THEN 1 END) as occupied_tables, " +
                      "COUNT(CASE WHEN status = 'RESERVED' THEN 1 END) as reserved_tables, " +
                      "COUNT(CASE WHEN status = 'OUT_OF_SERVICE' THEN 1 END) as out_of_service_tables, " +
                      "SUM(capacity) as total_capacity, " +
                      "AVG(capacity) as avg_capacity " +
                      "FROM tables";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return new TableStats(
                    rs.getInt("total_tables"),
                    rs.getInt("available_tables"),
                    rs.getInt("occupied_tables"),
                    rs.getInt("reserved_tables"),
                    rs.getInt("out_of_service_tables"),
                    rs.getInt("total_capacity"),
                    rs.getDouble("avg_capacity")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting table statistics: " + e.getMessage());
        }
        
        return new TableStats(0, 0, 0, 0, 0, 0, 0.0);
    }
    
    // Find best available table for capacity
    public Table findBestTableForCapacity(int requiredCapacity) {
        String query = "SELECT * FROM tables WHERE status = 'AVAILABLE' AND capacity >= ? " +
                      "ORDER BY capacity ASC LIMIT 1";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, requiredCapacity);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createTableFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding best table for capacity: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get tables with current orders
    public List<Table> getTablesWithOrders() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT DISTINCT t.* FROM tables t " +
                      "INNER JOIN orders o ON t.table_id = o.table_id " +
                      "WHERE o.status IN ('PENDING', 'PREPARING', 'READY') " +
                      "ORDER BY t.table_number";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                if (table != null) {
                    tables.add(table);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tables with orders: " + e.getMessage());
        }
        
        return tables;
    }
    
    // Helper method to create Table object from ResultSet
    private Table createTableFromResultSet(ResultSet rs) throws SQLException {
        int tableId = rs.getInt("table_id");
        int tableNumber = rs.getInt("table_number");
        int capacity = rs.getInt("capacity");
        String statusStr = rs.getString("status");
        String location = rs.getString("location");
        
        Table.TableStatus status = Table.TableStatus.valueOf(statusStr);
        
        Table table = new Table(tableNumber, capacity);
        table.setNotes(location);
        
        return table;
    }
    
    // Inner class for table statistics
    public static class TableStats {
        private final int totalTables;
        private final int availableTables;
        private final int occupiedTables;
        private final int reservedTables;
        private final int outOfServiceTables;
        private final int totalCapacity;
        private final double avgCapacity;
        
        public TableStats(int totalTables, int availableTables, int occupiedTables,
                         int reservedTables, int outOfServiceTables, int totalCapacity,
                         double avgCapacity) {
            this.totalTables = totalTables;
            this.availableTables = availableTables;
            this.occupiedTables = occupiedTables;
            this.reservedTables = reservedTables;
            this.outOfServiceTables = outOfServiceTables;
            this.totalCapacity = totalCapacity;
            this.avgCapacity = avgCapacity;
        }
        
        // Getters
        public int getTotalTables() { return totalTables; }
        public int getAvailableTables() { return availableTables; }
        public int getOccupiedTables() { return occupiedTables; }
        public int getReservedTables() { return reservedTables; }
        public int getOutOfServiceTables() { return outOfServiceTables; }
        public int getTotalCapacity() { return totalCapacity; }
        public double getAvgCapacity() { return avgCapacity; }
        
        // Calculate utilization percentage
        public double getUtilizationPercentage() {
            if (totalTables == 0) return 0.0;
            return ((double) (occupiedTables + reservedTables) / totalTables) * 100.0;
        }
        
        // Calculate availability percentage
        public double getAvailabilityPercentage() {
            if (totalTables == 0) return 0.0;
            return ((double) availableTables / totalTables) * 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("Table Statistics:\n" +
                               "Total Tables: %d\n" +
                               "Available Tables: %d (%.1f%%)\n" +
                               "Occupied Tables: %d\n" +
                               "Reserved Tables: %d\n" +
                               "Out of Service Tables: %d\n" +
                               "Total Capacity: %d\n" +
                               "Average Capacity: %.1f\n" +
                               "Utilization: %.1f%%",
                               totalTables, availableTables, getAvailabilityPercentage(),
                               occupiedTables, reservedTables, outOfServiceTables,
                               totalCapacity, avgCapacity, getUtilizationPercentage());
        }
    }
}