import java.sql.*;
import java.io.File;

/**
 * DatabaseConnection class for managing SQLite database connections
 * Implements singleton pattern for connection management
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DATABASE_NAME = "coffee_shop.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    
    // Private constructor for singleton pattern
    private DatabaseConnection() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            
            // Enable foreign key constraints
            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.close();
            
            System.out.println("Database connection established successfully.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            System.err.println("Please add sqlite-jdbc jar to your classpath.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }
    
    // Get singleton instance
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    // Get database connection
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL);
                
                // Enable foreign key constraints
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }
    
    // Initialize database with schema
    public boolean initializeDatabase() {
        try {
            // Check if database file exists
            File dbFile = new File(DATABASE_NAME);
            boolean isNewDatabase = !dbFile.exists();
            
            if (isNewDatabase) {
                System.out.println("Creating new database...");
                createTables();
                insertSampleData();
                System.out.println("Database initialized successfully with sample data.");
            } else {
                System.out.println("Database already exists.");
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            return false;
        }
    }
    
    // Create database tables
    private void createTables() throws SQLException {
        String[] createTableQueries = {
            // Menu items table
            "CREATE TABLE IF NOT EXISTS menu_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "description TEXT," +
            "base_price DECIMAL(10,2) NOT NULL," +
            "category VARCHAR(50) NOT NULL," +
            "item_type VARCHAR(50) NOT NULL," +
            "coffee_type VARCHAR(50)," +
            "is_available BOOLEAN DEFAULT TRUE," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Customers table
            "CREATE TABLE IF NOT EXISTS customers (" +
            "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "email VARCHAR(150) UNIQUE," +
            "phone_number VARCHAR(20)," +
            "loyalty_points DECIMAL(10,2) DEFAULT 0.00," +
            "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Tables table
            "CREATE TABLE IF NOT EXISTS tables (" +
            "table_number INTEGER PRIMARY KEY," +
            "capacity INTEGER NOT NULL," +
            "status VARCHAR(20) DEFAULT 'AVAILABLE'," +
            "current_customer_id INTEGER," +
            "occupied_since TIMESTAMP," +
            "reserved_until TIMESTAMP," +
            "notes TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (current_customer_id) REFERENCES customers(customer_id)" +
            ")",
            
            // Orders table
            "CREATE TABLE IF NOT EXISTS orders (" +
            "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "customer_id INTEGER NOT NULL," +
            "status VARCHAR(20) DEFAULT 'PENDING'," +
            "service_type VARCHAR(20) NOT NULL," +
            "table_number INTEGER," +
            "subtotal DECIMAL(10,2) NOT NULL," +
            "tax DECIMAL(10,2) NOT NULL," +
            "discount DECIMAL(10,2) DEFAULT 0.00," +
            "total_amount DECIMAL(10,2) NOT NULL," +
            "special_instructions TEXT," +
            "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "completion_time TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
            "FOREIGN KEY (table_number) REFERENCES tables(table_number)" +
            ")",
            
            // Order items table
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER NOT NULL," +
            "menu_item_id INTEGER NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "unit_price DECIMAL(10,2) NOT NULL," +
            "total_price DECIMAL(10,2) NOT NULL," +
            "customizations TEXT," +
            "size VARCHAR(20)," +
            "is_hot BOOLEAN," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
            "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
            ")",
            
            // Payments table
            "CREATE TABLE IF NOT EXISTS payments (" +
            "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER NOT NULL," +
            "payment_method VARCHAR(20) NOT NULL," +
            "status VARCHAR(20) DEFAULT 'PENDING'," +
            "amount DECIMAL(10,2) NOT NULL," +
            "amount_paid DECIMAL(10,2) DEFAULT 0.00," +
            "change_given DECIMAL(10,2) DEFAULT 0.00," +
            "transaction_reference VARCHAR(100)," +
            "card_last_four_digits VARCHAR(4)," +
            "failure_reason TEXT," +
            "payment_time TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (order_id) REFERENCES orders(order_id)" +
            ")",
            
            // Ingredients table
            "CREATE TABLE IF NOT EXISTS ingredients (" +
            "ingredient_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "description TEXT," +
            "unit VARCHAR(20) NOT NULL," +
            "current_stock DECIMAL(10,3) DEFAULT 0.000," +
            "minimum_stock DECIMAL(10,3) NOT NULL," +
            "maximum_stock DECIMAL(10,3) NOT NULL," +
            "cost_per_unit DECIMAL(10,2) NOT NULL," +
            "expiration_date DATE," +
            "supplier VARCHAR(100)," +
            "is_active BOOLEAN DEFAULT TRUE," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        };
        
        Statement stmt = connection.createStatement();
        
        for (String query : createTableQueries) {
            stmt.execute(query);
        }
        
        // Create indexes
        String[] indexQueries = {
            "CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id)",
            "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)",
            "CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id)",
            "CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id)",
            "CREATE INDEX IF NOT EXISTS idx_tables_status ON tables(status)",
            "CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category)"
        };
        
        for (String query : indexQueries) {
            stmt.execute(query);
        }
        
        stmt.close();
    }
    
    // Insert sample data
    private void insertSampleData() throws SQLException {
        // Sample menu items
        String insertMenuItems = "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type) VALUES " +
            "('Espresso', 'Rich and bold espresso shot', 2.50, 'Coffee', 'Coffee', 'ESPRESSO'), " +
            "('Americano', 'Espresso with hot water', 3.00, 'Coffee', 'Coffee', 'AMERICANO'), " +
            "('Latte', 'Espresso with steamed milk', 4.50, 'Coffee', 'Coffee', 'LATTE'), " +
            "('Cappuccino', 'Espresso with steamed milk and foam', 4.00, 'Coffee', 'Coffee', 'CAPPUCCINO'), " +
            "('Mocha', 'Espresso with chocolate and steamed milk', 5.00, 'Coffee', 'Coffee', 'MOCHA')";
        
        // Sample customers
        String insertCustomers = "INSERT INTO customers (name, email, phone_number, loyalty_points) VALUES " +
            "('John Doe', 'john.doe@email.com', '555-0101', 25.50), " +
            "('Jane Smith', 'jane.smith@email.com', '555-0102', 15.75), " +
            "('Bob Johnson', 'bob.johnson@email.com', '555-0103', 42.25)";
        
        // Sample tables
        String insertTables = "INSERT INTO tables (table_number, capacity) VALUES " +
            "(1, 2), (2, 4), (3, 2), (4, 6), (5, 4)";
        
        Statement stmt = connection.createStatement();
        stmt.execute(insertMenuItems);
        stmt.execute(insertCustomers);
        stmt.execute(insertTables);
        stmt.close();
    }
    
    // Test database connection
    public boolean testConnection() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            boolean hasResult = rs.next();
            rs.close();
            stmt.close();
            return hasResult;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    // Close database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
    
    // Execute query and return ResultSet
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }
    
    // Execute update query
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        int result = stmt.executeUpdate(query);
        stmt.close();
        return result;
    }
    
    // Prepare statement
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
    
    // Begin transaction
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }
    
    // Commit transaction
    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }
    
    // Rollback transaction
    public void rollbackTransaction() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }
}