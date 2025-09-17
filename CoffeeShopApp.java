import java.util.Scanner;

/**
 * Main application class for the Coffee Shop Management System
 * This class serves as the entry point and provides navigation between
 * customer and management views while following OOP principles.
 */
public class CoffeeShopApp {
    private Scanner scanner;
    private CustomerView customerView;
    private ManagementView managementView;
    private DatabaseConnection databaseConnection;
    
    public CoffeeShopApp() {
        this.scanner = new Scanner(System.in);
        this.customerView = new CustomerView();
        this.managementView = new ManagementView();
    }
    
    /**
     * Main method - entry point of the application
     */
    public static void main(String[] args) {
        CoffeeShopApp app = new CoffeeShopApp();
        app.start();
    }
    
    /**
     * Starts the application and handles the main navigation
     */
    public void start() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("    WELCOME TO COFFEE SHOP MANAGEMENT SYSTEM");
        System.out.println("=".repeat(50));
        
        // Initialize database
        if (!initializeDatabase()) {
            System.out.println("Failed to initialize database. Exiting application.");
            return;
        }
        
        // Main application loop
        while (true) {
            showMainMenu();
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    startCustomerMode();
                    break;
                case 2:
                    startManagementMode();
                    break;
                case 3:
                    showAbout();
                    break;
                case 4:
                    showSystemInfo();
                    break;
                case 5:
                    exitApplication();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Displays the main menu options
     */
    private void showMainMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           MAIN MENU");
        System.out.println("=".repeat(40));
        System.out.println("1. Customer Mode - Place Orders");
        System.out.println("2. Management Mode - Admin Panel");
        System.out.println("3. About");
        System.out.println("4. System Information");
        System.out.println("5. Exit");
        System.out.println("-".repeat(40));
        System.out.print("Please select an option (1-5): ");
    }
    
    /**
     * Initializes the database connection and creates tables if needed
     * @return true if initialization successful, false otherwise
     */
    private boolean initializeDatabase() {
        try {
            System.out.println("\nInitializing database...");
            
            // Get database connection instance (singleton pattern)
            databaseConnection = DatabaseConnection.getInstance();
            
            // Initialize database schema and sample data
            databaseConnection.initializeDatabase();
            
            System.out.println("Database initialized successfully!");
            return true;
            
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Starts the customer mode interface
     */
    private void startCustomerMode() {
        try {
            System.out.println("\nSwitching to Customer Mode...");
            customerView.start();
        } catch (Exception e) {
            System.err.println("Error in customer mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts the management mode interface
     */
    private void startManagementMode() {
        try {
            System.out.println("\nSwitching to Management Mode...");
            managementView.start();
        } catch (Exception e) {
            System.err.println("Error in management mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays information about the application
     */
    private void showAbout() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    ABOUT COFFEE SHOP APP");
        System.out.println("=".repeat(60));
        System.out.println("\nApplication: Coffee Shop Management System");
        System.out.println("Version: 1.0.0");
        System.out.println("Language: Java (Pure OOP Implementation)");
        System.out.println("Database: SQLite");
        System.out.println("\nFeatures:");
        System.out.println("  ✓ Customer ordering system with menu selection");
        System.out.println("  ✓ Payment processing (Cash, Card, Mobile, Loyalty Points)");
        System.out.println("  ✓ Table management (Dine-in and Take-away options)");
        System.out.println("  ✓ Inventory management with low stock alerts");
        System.out.println("  ✓ Order tracking and status management");
        System.out.println("  ✓ Customer loyalty points system");
        System.out.println("  ✓ Management dashboard with analytics");
        System.out.println("  ✓ Reports and statistics");
        System.out.println("\nOOP Principles Implemented:");
        System.out.println("  ✓ Encapsulation - Private fields with getters/setters");
        System.out.println("  ✓ Inheritance - MenuItem base class with Coffee subclass");
        System.out.println("  ✓ Abstraction - Abstract methods and interfaces");
        System.out.println("  ✓ Polymorphism - Method overriding and interface implementation");
        System.out.println("  ✓ Composition - Complex objects containing other objects");
        System.out.println("  ✓ Singleton Pattern - Database connection management");
        System.out.println("  ✓ Data Access Object (DAO) Pattern - Database operations");
        System.out.println("\nDeveloped following strict OOP methodology without frameworks.");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Displays system information and current status
     */
    private void showSystemInfo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("              SYSTEM INFORMATION");
        System.out.println("=".repeat(50));
        
        try {
            // Java system information
            System.out.println("\nJava Environment:");
            System.out.println("  Java Version: " + System.getProperty("java.version"));
            System.out.println("  Java Vendor: " + System.getProperty("java.vendor"));
            System.out.println("  Operating System: " + System.getProperty("os.name"));
            System.out.println("  OS Version: " + System.getProperty("os.version"));
            System.out.println("  Architecture: " + System.getProperty("os.arch"));
            
            // Memory information
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            System.out.println("\nMemory Usage:");
            System.out.printf("  Max Memory: %.2f MB%n", maxMemory / (1024.0 * 1024.0));
            System.out.printf("  Total Memory: %.2f MB%n", totalMemory / (1024.0 * 1024.0));
            System.out.printf("  Used Memory: %.2f MB%n", usedMemory / (1024.0 * 1024.0));
            System.out.printf("  Free Memory: %.2f MB%n", freeMemory / (1024.0 * 1024.0));
            
            // Database status
            System.out.println("\nDatabase Status:");
            if (databaseConnection != null) {
                System.out.println("  Connection: Active");
                System.out.println("  Database Type: SQLite");
                System.out.println("  Database File: coffee_shop.db");
            } else {
                System.out.println("  Connection: Not initialized");
            }
            
            // Application components status
            System.out.println("\nApplication Components:");
            System.out.println("  Customer View: " + (customerView != null ? "Loaded" : "Not loaded"));
            System.out.println("  Management View: " + (managementView != null ? "Loaded" : "Not loaded"));
            
            // Current time
            System.out.println("\nSystem Time: " + java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("Error retrieving system information: " + e.getMessage());
        }
        
        System.out.println("=".repeat(50));
    }
    
    /**
     * Handles application exit with proper cleanup
     */
    private void exitApplication() {
        System.out.println("\nShutting down Coffee Shop Management System...");
        
        try {
            // Cleanup resources
            if (customerView != null) {
                customerView.cleanup();
            }
            
            if (managementView != null) {
                managementView.cleanup();
            }
            
            if (databaseConnection != null) {
                databaseConnection.closeConnection();
            }
            
            if (scanner != null) {
                scanner.close();
            }
            
            System.out.println("Cleanup completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("    Thank you for using Coffee Shop Management System!");
        System.out.println("                    Goodbye!");
        System.out.println("=".repeat(50));
    }
    
    /**
     * Utility method to get integer input from user with error handling
     * @return valid integer input from user
     */
    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    /**
     * Utility method to display a formatted header
     * @param title the title to display
     * @param width the width of the header
     */
    private void displayHeader(String title, int width) {
        System.out.println("\n" + "=".repeat(width));
        
        // Center the title
        int padding = (width - title.length()) / 2;
        String paddedTitle = " ".repeat(Math.max(0, padding)) + title;
        System.out.println(paddedTitle);
        
        System.out.println("=".repeat(width));
    }
    
    /**
     * Utility method to display a formatted message
     * @param message the message to display
     */
    private void displayMessage(String message) {
        System.out.println("\n[INFO] " + message);
    }
    
    /**
     * Utility method to display an error message
     * @param error the error message to display
     */
    private void displayError(String error) {
        System.err.println("\n[ERROR] " + error);
    }
    
    /**
     * Utility method to display a success message
     * @param success the success message to display
     */
    private void displaySuccess(String success) {
        System.out.println("\n[SUCCESS] " + success);
    }
    
    /**
     * Getter for database connection (for testing purposes)
     * @return the database connection instance
     */
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
    
    /**
     * Getter for customer view (for testing purposes)
     * @return the customer view instance
     */
    public CustomerView getCustomerView() {
        return customerView;
    }
    
    /**
     * Getter for management view (for testing purposes)
     * @return the management view instance
     */
    public ManagementView getManagementView() {
        return managementView;
    }
}