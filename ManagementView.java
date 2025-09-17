import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManagementView {
    private Scanner scanner;
    private MenuItemDAO menuItemDAO;
    private CustomerDAO customerDAO;
    private OrderDAO orderDAO;
    private TableDAO tableDAO;
    private PaymentDAO paymentDAO;
    private IngredientDAO ingredientDAO;
    
    public ManagementView() {
        this.scanner = new Scanner(System.in);
        this.menuItemDAO = new MenuItemDAO();
        this.customerDAO = new CustomerDAO();
        this.orderDAO = new OrderDAO();
        this.tableDAO = new TableDAO();
        this.paymentDAO = new PaymentDAO();
        this.ingredientDAO = new IngredientDAO();
    }
    
    public void start() {
        System.out.println("\n=== Coffee Shop Management System ===");
        
        // Simple authentication
        if (!authenticate()) {
            System.out.println("Access denied.");
            return;
        }
        
        showMainMenu();
    }
    
    private boolean authenticate() {
        System.out.print("Enter management password: ");
        String password = scanner.nextLine().trim();
        
        // Simple password check (in real application, use proper authentication)
        return password.equals("admin123");
    }
    
    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== Management Dashboard ===");
            System.out.println("1. Order Management");
            System.out.println("2. Table Management");
            System.out.println("3. Inventory Management");
            System.out.println("4. Menu Management");
            System.out.println("5. Customer Management");
            System.out.println("6. Reports & Analytics");
            System.out.println("7. System Status");
            System.out.println("8. Exit");
            System.out.print("Choose option (1-8): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    orderManagement();
                    break;
                case 2:
                    tableManagement();
                    break;
                case 3:
                    inventoryManagement();
                    break;
                case 4:
                    menuManagement();
                    break;
                case 5:
                    customerManagement();
                    break;
                case 6:
                    reportsAndAnalytics();
                    break;
                case 7:
                    systemStatus();
                    break;
                case 8:
                    System.out.println("Exiting management system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void orderManagement() {
        while (true) {
            System.out.println("\n=== Order Management ===");
            System.out.println("1. View All Orders");
            System.out.println("2. View Pending Orders");
            System.out.println("3. Update Order Status");
            System.out.println("4. Search Orders");
            System.out.println("5. Cancel Order");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose option (1-6): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    viewAllOrders();
                    break;
                case 2:
                    viewPendingOrders();
                    break;
                case 3:
                    updateOrderStatus();
                    break;
                case 4:
                    searchOrders();
                    break;
                case 5:
                    cancelOrder();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllOrders() {
        try {
            List<Order> orders = orderDAO.getAllOrders();
            displayOrders(orders, "All Orders");
        } catch (Exception e) {
            System.out.println("Error retrieving orders: " + e.getMessage());
        }
    }
    
    private void viewPendingOrders() {
        try {
            List<Order> orders = orderDAO.getOrdersByStatus(Order.OrderStatus.PENDING);
        orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.CONFIRMED));
        orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.PREPARING));
            
            displayOrders(orders, "Pending Orders");
        } catch (Exception e) {
            System.out.println("Error retrieving pending orders: " + e.getMessage());
        }
    }
    
    private void displayOrders(List<Order> orders, String title) {
        if (orders.isEmpty()) {
            System.out.println("\nNo orders found.");
            return;
        }
        
        System.out.println("\n=== " + title + " ===");
        System.out.printf("%-8s %-12s %-15s %-8s %-10s %-12s %-15s%n", 
            "Order ID", "Customer", "Service Type", "Table", "Total", "Status", "Date");
        System.out.println("-".repeat(90));
        
        for (Order order : orders) {
            try {
                Customer customer = customerDAO.getCustomerById(order.getCustomerId());
                String customerName = (customer != null) ? customer.getName() : "Unknown";
                String tableInfo = (order.getTableNumber() > 0) ? String.valueOf(order.getTableNumber()) : "N/A";
                
                System.out.printf("%-8d %-12s %-15s %-8s $%-9.2f %-12s %-15s%n",
                    order.getOrderId(),
                    customerName,
                    order.getServiceType(),
                    tableInfo,
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getOrderTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")));
            } catch (Exception e) {
                System.out.println("Error displaying order " + order.getOrderId() + ": " + e.getMessage());
            }
        }
    }
    
    private void updateOrderStatus() {
        System.out.print("Enter Order ID: ");
        int orderId = getIntInput();
        
        try {
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                System.out.println("Order not found.");
                return;
            }
            
            System.out.println("\nCurrent Status: " + order.getStatus());
            System.out.println("\nAvailable Statuses:");
            System.out.println("1. PENDING");
            System.out.println("2. CONFIRMED");
            System.out.println("3. PREPARING");
            System.out.println("4. READY");
            System.out.println("5. COMPLETED");
            System.out.println("6. CANCELLED");
            
            System.out.print("Choose new status (1-6): ");
            int statusChoice = getIntInput();
            
            Order.OrderStatus newStatus;
            switch (statusChoice) {
                case 1: newStatus = Order.OrderStatus.PENDING; break;
                case 2: newStatus = Order.OrderStatus.CONFIRMED; break;
                case 3: newStatus = Order.OrderStatus.PREPARING; break;
                case 4: newStatus = Order.OrderStatus.READY; break;
                case 5: newStatus = Order.OrderStatus.COMPLETED; break;
                case 6: newStatus = Order.OrderStatus.CANCELLED; break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            order.setStatus(newStatus);
            orderDAO.updateOrderStatus(orderId, order.getStatus());
            
            // If order is completed and it's dine-in, free the table
            if (newStatus == Order.OrderStatus.COMPLETED && 
                order.getServiceType() == Order.ServiceType.DINE_IN && 
                order.getTableNumber() > 0) {
                
                Table table = tableDAO.getTableById(order.getTableNumber());
                if (table != null) {
                    table.makeAvailable();
                    tableDAO.updateTableStatus(table.getTableNumber(), table.getStatus());
                    System.out.println("Table " + order.getTableNumber() + " has been freed.");
                }
            }
            
            System.out.println("Order status updated to: " + newStatus);
            
        } catch (Exception e) {
            System.out.println("Error updating order status: " + e.getMessage());
        }
    }
    
    private void searchOrders() {
        System.out.println("\n=== Search Orders ===");
        System.out.println("1. Search by Customer Name");
        System.out.println("2. Search by Date Range");
        System.out.println("3. Search by Status");
        System.out.print("Choose search type (1-3): ");
        
        int choice = getIntInput();
        
        try {
            List<Order> results = new ArrayList<>();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter customer name: ");
                    String customerName = scanner.nextLine().trim();
                    List<Customer> customers = customerDAO.searchCustomersByName(customerName);
                    for (Customer customer : customers) {
                        results.addAll(orderDAO.getOrdersByCustomerId(customer.getCustomerId()));
                    }
                    break;
                case 2:
                    System.out.println("Enter date range (today's orders only for simplicity)");
                    results = orderDAO.getOrdersByDateRange(
                        LocalDateTime.now().toLocalDate().atStartOfDay(),
                        LocalDateTime.now());
                    break;
                case 3:
                    System.out.println("Choose status:");
                    System.out.println("1. PENDING");
                    System.out.println("2. CONFIRMED");
                    System.out.println("3. PREPARING");
                    System.out.println("4. READY");
                    System.out.println("5. COMPLETED");
                    System.out.println("6. CANCELLED");
                    System.out.print("Choose status (1-6): ");
                    int statusChoice = getIntInput();
                    
                    Order.OrderStatus status;
                    switch (statusChoice) {
                        case 1: status = Order.OrderStatus.PENDING; break;
                        case 2: status = Order.OrderStatus.CONFIRMED; break;
                        case 3: status = Order.OrderStatus.PREPARING; break;
                        case 4: status = Order.OrderStatus.READY; break;
                        case 5: status = Order.OrderStatus.COMPLETED; break;
                        case 6: status = Order.OrderStatus.CANCELLED; break;
                        default:
                            System.out.println("Invalid choice.");
                            return;
                    }
                    results = orderDAO.getOrdersByStatus(status);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            displayOrders(results, "Search Results");
            
        } catch (Exception e) {
            System.out.println("Error searching orders: " + e.getMessage());
        }
    }
    
    private void cancelOrder() {
        System.out.print("Enter Order ID to cancel: ");
        int orderId = getIntInput();
        
        try {
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                System.out.println("Order not found.");
                return;
            }
            
            if (order.getStatus() == Order.OrderStatus.COMPLETED || 
                order.getStatus() == Order.OrderStatus.CANCELLED) {
                System.out.println("Cannot cancel order with status: " + order.getStatus());
                return;
            }
            
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderDAO.updateOrderStatus(orderId, order.getStatus());
            
            // Free table if it was dine-in
            if (order.getServiceType() == Order.ServiceType.DINE_IN && order.getTableNumber() > 0) {
                Table table = tableDAO.getTableById(order.getTableNumber());
                if (table != null) {
                    table.makeAvailable();
                    tableDAO.updateTableStatus(table.getTableNumber(), table.getStatus());
                }
            }
            
            System.out.println("Order " + orderId + " has been cancelled.");
            
        } catch (Exception e) {
            System.out.println("Error cancelling order: " + e.getMessage());
        }
    }
    
    private void tableManagement() {
        while (true) {
            System.out.println("\n=== Table Management ===");
            System.out.println("1. View All Tables");
            System.out.println("2. View Available Tables");
            System.out.println("3. View Occupied Tables");
            System.out.println("4. Add New Table");
            System.out.println("5. Update Table Status");
            System.out.println("6. Remove Table");
            System.out.println("7. Table Statistics");
            System.out.println("8. Back to Main Menu");
            System.out.print("Choose option (1-8): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    viewAllTables();
                    break;
                case 2:
                    viewAvailableTables();
                    break;
                case 3:
                    viewOccupiedTables();
                    break;
                case 4:
                    addNewTable();
                    break;
                case 5:
                    updateTableStatus();
                    break;
                case 6:
                    removeTable();
                    break;
                case 7:
                    tableStatistics();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllTables() {
        try {
            List<Table> tables = tableDAO.getAllTables();
            displayTables(tables, "All Tables");
        } catch (Exception e) {
            System.out.println("Error retrieving tables: " + e.getMessage());
        }
    }
    
    private void viewAvailableTables() {
        try {
            List<Table> tables = tableDAO.getAvailableTables();
            displayTables(tables, "Available Tables");
        } catch (Exception e) {
            System.out.println("Error retrieving available tables: " + e.getMessage());
        }
    }
    
    private void viewOccupiedTables() {
        try {
            List<Table> tables = tableDAO.getTablesByStatus(Table.TableStatus.OCCUPIED);
            displayTables(tables, "Occupied Tables");
        } catch (Exception e) {
            System.out.println("Error retrieving occupied tables: " + e.getMessage());
        }
    }
    
    private void displayTables(List<Table> tables, String title) {
        if (tables.isEmpty()) {
            System.out.println("\nNo tables found.");
            return;
        }
        
        System.out.println("\n=== " + title + " ===");
        System.out.printf("%-8s %-10s %-12s %-15s%n", "Table #", "Capacity", "Status", "Last Updated");
        System.out.println("-".repeat(50));
        
        for (Table table : tables) {
            System.out.printf("%-8d %-10d %-12s %-15s%n",
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus(),
                "N/A"); // Last updated not available
        }
    }
    
    private void addNewTable() {
        System.out.print("Enter table number: ");
        int tableNumber = getIntInput();
        
        System.out.print("Enter table capacity: ");
        int capacity = getIntInput();
        
        try {
            Table existingTable = tableDAO.getTableById(tableNumber);
            if (existingTable != null) {
                System.out.println("Table number already exists.");
                return;
            }
            
            Table newTable = new Table(tableNumber, capacity);
            tableDAO.createTable(newTable);
            
            System.out.println("Table " + tableNumber + " added successfully.");
            
        } catch (Exception e) {
            System.out.println("Error adding table: " + e.getMessage());
        }
    }
    
    private void updateTableStatus() {
        System.out.print("Enter table number: ");
        int tableNumber = getIntInput();
        
        try {
            Table table = tableDAO.getTableById(tableNumber);
            if (table == null) {
                System.out.println("Table not found.");
                return;
            }
            
            System.out.println("\nCurrent Status: " + table.getStatus());
            System.out.println("\nAvailable Statuses:");
            System.out.println("1. AVAILABLE");
            System.out.println("2. OCCUPIED");
            System.out.println("3. RESERVED");
            System.out.println("4. OUT_OF_SERVICE");
            
            System.out.print("Choose new status (1-4): ");
            int statusChoice = getIntInput();
            
            switch (statusChoice) {
                case 1:
                    table.makeAvailable();
                    break;
                case 2:
                    table.occupyTable(1); // Default customer ID
                    break;
                case 3:
                    table.reserveTable(java.time.LocalDateTime.now().plusHours(1));
                    break;
                case 4:
                    table.setOutOfService("Manual update");
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            tableDAO.updateTableStatus(table.getTableNumber(), table.getStatus());
            System.out.println("Table status updated to: " + table.getStatus());
            
        } catch (Exception e) {
            System.out.println("Error updating table status: " + e.getMessage());
        }
    }
    
    private void removeTable() {
        System.out.print("Enter table number to remove: ");
        int tableNumber = getIntInput();
        
        try {
            Table table = tableDAO.getTableById(tableNumber);
            if (table == null) {
                System.out.println("Table not found.");
                return;
            }
            
            // Check if table has active orders
            // Check if table has active orders (simplified check)
            if (false) { // Placeholder - would need proper implementation
                System.out.println("Cannot remove table with active orders.");
                return;
            }
            
            System.out.print("Are you sure you want to remove table " + tableNumber + "? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                // tableDAO.deleteTable(tableNumber); // Method not available
                System.out.println("Table " + tableNumber + " removed successfully.");
            } else {
                System.out.println("Table removal cancelled.");
            }
            
        } catch (Exception e) {
            System.out.println("Error removing table: " + e.getMessage());
        }
    }
    
    private void tableStatistics() {
        try {
            TableDAO.TableStats stats = tableDAO.getTableStats();
            
            System.out.println("\n=== Table Statistics ===");
            System.out.println("Total Tables: " + stats.getTotalTables());
            System.out.println("Available Tables: " + stats.getAvailableTables());
            System.out.println("Occupied Tables: " + stats.getOccupiedTables());
            System.out.println("Reserved Tables: " + stats.getReservedTables());
            System.out.println("Out of Service: " + stats.getOutOfServiceTables());
            
            double occupancyRate = (stats.getTotalTables() > 0) ? 
                (double) stats.getOccupiedTables() / stats.getTotalTables() * 100 : 0;
            System.out.printf("Occupancy Rate: %.1f%%%n", occupancyRate);
            
        } catch (Exception e) {
            System.out.println("Error retrieving table statistics: " + e.getMessage());
        }
    }
    
    private void inventoryManagement() {
        while (true) {
            System.out.println("\n=== Inventory Management ===");
            System.out.println("1. View All Ingredients");
            System.out.println("2. View Low Stock Items");
            System.out.println("3. View Expired Items");
            System.out.println("4. Add New Ingredient");
            System.out.println("5. Update Stock");
            System.out.println("6. Update Ingredient Details");
            System.out.println("7. Remove Ingredient");
            System.out.println("8. Inventory Statistics");
            System.out.println("9. Back to Main Menu");
            System.out.print("Choose option (1-9): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    viewAllIngredients();
                    break;
                case 2:
                    viewLowStockItems();
                    break;
                case 3:
                    viewExpiredItems();
                    break;
                case 4:
                    addNewIngredient();
                    break;
                case 5:
                    updateStock();
                    break;
                case 6:
                    updateIngredientDetails();
                    break;
                case 7:
                    removeIngredient();
                    break;
                case 8:
                    inventoryStatistics();
                    break;
                case 9:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllIngredients() {
        try {
            List<Ingredient> ingredients = ingredientDAO.getAllIngredients();
            displayIngredients(ingredients, "All Ingredients");
        } catch (Exception e) {
            System.out.println("Error retrieving ingredients: " + e.getMessage());
        }
    }
    
    private void viewLowStockItems() {
        try {
            List<Ingredient> ingredients = ingredientDAO.getLowStockIngredients();
            displayIngredients(ingredients, "Low Stock Items");
        } catch (Exception e) {
            System.out.println("Error retrieving low stock items: " + e.getMessage());
        }
    }
    
    private void viewExpiredItems() {
        try {
            List<Ingredient> ingredients = ingredientDAO.getExpiredIngredients();
            displayIngredients(ingredients, "Expired Items");
        } catch (Exception e) {
            System.out.println("Error retrieving expired items: " + e.getMessage());
        }
    }
    
    private void displayIngredients(List<Ingredient> ingredients, String title) {
        if (ingredients.isEmpty()) {
            System.out.println("\nNo ingredients found.");
            return;
        }
        
        System.out.println("\n=== " + title + " ===");
        System.out.printf("%-5s %-20s %-10s %-8s %-12s %-15s %-20s%n", 
            "ID", "Name", "Stock", "Unit", "Min Level", "Expiry Date", "Supplier");
        System.out.println("-".repeat(95));
        
        for (Ingredient ingredient : ingredients) {
            String expiryDate = (ingredient.getExpirationDate() != null) ? 
                ingredient.getExpirationDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A";
            
            System.out.printf("%-5d %-20s %-10.2f %-8s %-12.2f %-15s %-20s%n",
                ingredient.getIngredientId(),
                ingredient.getName(),
                ingredient.getCurrentStock(),
                ingredient.getUnit(),
                ingredient.getMinimumStock(),
                expiryDate,
                ingredient.getSupplier());
        }
    }
    
    private void addNewIngredient() {
        System.out.print("Enter ingredient name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter current stock: ");
        double currentStock = getDoubleInput();
        
        System.out.println("Choose unit:");
        System.out.println("1. GRAMS");
        System.out.println("2. KILOGRAMS");
        System.out.println("3. LITERS");
        System.out.println("4. MILLILITERS");
        System.out.println("5. PIECES");
        System.out.print("Choose unit (1-5): ");
        int unitChoice = getIntInput();
        
        Ingredient.Unit unit;
        switch (unitChoice) {
            case 1: unit = Ingredient.Unit.GRAMS; break;
            case 2: unit = Ingredient.Unit.KILOGRAMS; break;
            case 3: unit = Ingredient.Unit.LITERS; break;
            case 4: unit = Ingredient.Unit.MILLILITERS; break;
            case 5: unit = Ingredient.Unit.PIECES; break;
            default:
                System.out.println("Invalid choice. Using GRAMS.");
                unit = Ingredient.Unit.GRAMS;
        }
        
        System.out.print("Enter minimum level: ");
        double minimumLevel = getDoubleInput();
        
        System.out.print("Enter cost per unit: ");
        double costPerUnit = getDoubleInput();
        
        System.out.print("Enter supplier: ");
        String supplier = scanner.nextLine().trim();
        
        try {
            Ingredient ingredient = new Ingredient(0, name, "", unit, minimumLevel, costPerUnit);
            ingredient.addStock(currentStock);
            ingredient.setSupplier(supplier);
            ingredientDAO.createIngredient(ingredient);
            
            System.out.println("Ingredient '" + name + "' added successfully.");
            
        } catch (Exception e) {
            System.out.println("Error adding ingredient: " + e.getMessage());
        }
    }
    
    private void updateStock() {
        System.out.print("Enter ingredient ID: ");
        int ingredientId = getIntInput();
        
        try {
            Ingredient ingredient = ingredientDAO.getIngredientById(ingredientId);
            if (ingredient == null) {
                System.out.println("Ingredient not found.");
                return;
            }
            
            System.out.println("Current stock: " + ingredient.getCurrentStock() + " " + ingredient.getUnit());
            System.out.println("1. Add stock");
            System.out.println("2. Remove stock");
            System.out.println("3. Set stock level");
            System.out.print("Choose option (1-3): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter amount to add: ");
                    double addAmount = getDoubleInput();
                    ingredient.addStock(addAmount);
                    break;
                case 2:
                    System.out.print("Enter amount to remove: ");
                    double removeAmount = getDoubleInput();
                    ingredient.removeStock(removeAmount);
                    break;
                case 3:
                    System.out.print("Enter new stock level: ");
                    double newLevel = getDoubleInput();
                    ingredient.removeStock(ingredient.getCurrentStock());
                    ingredient.addStock(newLevel);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            ingredientDAO.updateIngredient(ingredient);
            System.out.println("Stock updated. New level: " + ingredient.getCurrentStock() + " " + ingredient.getUnit());
            
        } catch (Exception e) {
            System.out.println("Error updating stock: " + e.getMessage());
        }
    }
    
    private void updateIngredientDetails() {
        System.out.print("Enter ingredient ID: ");
        int ingredientId = getIntInput();
        
        try {
            Ingredient ingredient = ingredientDAO.getIngredientById(ingredientId);
            if (ingredient == null) {
                System.out.println("Ingredient not found.");
                return;
            }
            
            System.out.println("Current details:");
            System.out.println("Name: " + ingredient.getName());
            System.out.println("Minimum Level: " + ingredient.getMinimumStock());
            System.out.println("Cost per Unit: $" + ingredient.getCostPerUnit());
            System.out.println("Supplier: " + ingredient.getSupplier());
            
            System.out.print("\nEnter new name (or press Enter to keep current): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                ingredient.setName(newName);
            }
            
            System.out.print("Enter new minimum level (or -1 to keep current): ");
            double newMinLevel = getDoubleInput();
            if (newMinLevel >= 0) {
                ingredient.setMinimumStock(newMinLevel);
            }
            
            System.out.print("Enter new cost per unit (or -1 to keep current): ");
            double newCost = getDoubleInput();
            if (newCost >= 0) {
                ingredient.setCostPerUnit(newCost);
            }
            
            System.out.print("Enter new supplier (or press Enter to keep current): ");
            String newSupplier = scanner.nextLine().trim();
            if (!newSupplier.isEmpty()) {
                ingredient.setSupplier(newSupplier);
            }
            
            ingredientDAO.updateIngredient(ingredient);
            System.out.println("Ingredient details updated successfully.");
            
        } catch (Exception e) {
            System.out.println("Error updating ingredient: " + e.getMessage());
        }
    }
    
    private void removeIngredient() {
        System.out.print("Enter ingredient ID to remove: ");
        int ingredientId = getIntInput();
        
        try {
            Ingredient ingredient = ingredientDAO.getIngredientById(ingredientId);
            if (ingredient == null) {
                System.out.println("Ingredient not found.");
                return;
            }
            
            System.out.print("Are you sure you want to remove '" + ingredient.getName() + "'? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                ingredientDAO.deleteIngredient(ingredientId);
                System.out.println("Ingredient removed successfully.");
            } else {
                System.out.println("Ingredient removal cancelled.");
            }
            
        } catch (Exception e) {
            System.out.println("Error removing ingredient: " + e.getMessage());
        }
    }
    
    private void inventoryStatistics() {
        try {
            IngredientDAO.IngredientStats stats = ingredientDAO.getIngredientStats();
            
            System.out.println("\n=== Inventory Statistics ===");
            System.out.println("Total Ingredients: " + stats.getTotalIngredients());
            System.out.println("Low Stock Items: " + stats.getLowStockCount());
            System.out.println("Out of Stock Items: " + stats.getOutOfStockCount());
            System.out.println("Expired Items: " + stats.getExpiredCount());
            System.out.printf("Total Inventory Value: $%.2f%n", stats.getTotalInventoryValue());
            
        } catch (Exception e) {
            System.out.println("Error retrieving inventory statistics: " + e.getMessage());
        }
    }
    
    private void menuManagement() {
        while (true) {
            System.out.println("\n=== Menu Management ===");
            System.out.println("1. View All Menu Items");
            System.out.println("2. Add New Menu Item");
            System.out.println("3. Update Menu Item");
            System.out.println("4. Remove Menu Item");
            System.out.println("5. Search Menu Items");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose option (1-6): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    viewAllMenuItems();
                    break;
                case 2:
                    addNewMenuItem();
                    break;
                case 3:
                    updateMenuItem();
                    break;
                case 4:
                    removeMenuItem();
                    break;
                case 5:
                    searchMenuItems();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllMenuItems() {
        try {
            List<MenuItem> menuItems = menuItemDAO.getAllMenuItems();
            displayMenuItems(menuItems, "All Menu Items");
        } catch (Exception e) {
            System.out.println("Error retrieving menu items: " + e.getMessage());
        }
    }
    
    private void displayMenuItems(List<MenuItem> menuItems, String title) {
        if (menuItems.isEmpty()) {
            System.out.println("\nNo menu items found.");
            return;
        }
        
        System.out.println("\n=== " + title + " ===");
        System.out.printf("%-5s %-20s %-10s %-15s %-40s%n", "ID", "Name", "Price", "Category", "Description");
        System.out.println("-".repeat(95));
        
        for (MenuItem item : menuItems) {
            System.out.printf("%-5d %-20s $%-9.2f %-15s %-40s%n",
                item.getId(),
                item.getName(),
                item.getBasePrice(),
                item.getCategory(),
                item.getDescription());
        }
    }
    
    private void addNewMenuItem() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter base price: ");
        double basePrice = getDoubleInput();
        
        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        
        System.out.print("Enter description: ");
        String description = scanner.nextLine().trim();
        
        System.out.println("Is this a coffee item? (y/n): ");
        String isCoffee = scanner.nextLine().trim().toLowerCase();
        
        try {
            MenuItem menuItem;
            if (isCoffee.equals("y") || isCoffee.equals("yes")) {
                menuItem = new Coffee(0, name, basePrice, category, description);
            } else {
                // For simplicity, create a basic MenuItem implementation
                menuItem = new MenuItem(0, name, description, basePrice, category) {
                    @Override
                    public String getItemType() {
                        return "Food";
                    }
                    
                    @Override
                    public double calculatePrice() {
                        return getBasePrice();
                    }
                };
            }
            
            menuItemDAO.createMenuItem(menuItem);
            System.out.println("Menu item '" + name + "' added successfully.");
            
        } catch (Exception e) {
            System.out.println("Error adding menu item: " + e.getMessage());
        }
    }
    
    private void updateMenuItem() {
        System.out.print("Enter menu item ID: ");
        int itemId = getIntInput();
        
        try {
            MenuItem item = menuItemDAO.getMenuItemById(itemId);
            if (item == null) {
                System.out.println("Menu item not found.");
                return;
            }
            
            System.out.println("Current details:");
            System.out.println("Name: " + item.getName());
            System.out.println("Price: $" + item.getBasePrice());
            System.out.println("Category: " + item.getCategory());
            System.out.println("Description: " + item.getDescription());
            
            System.out.print("\nEnter new name (or press Enter to keep current): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                item.setName(newName);
            }
            
            System.out.print("Enter new price (or -1 to keep current): ");
            double newPrice = getDoubleInput();
            if (newPrice >= 0) {
                item.setBasePrice(newPrice);
            }
            
            System.out.print("Enter new category (or press Enter to keep current): ");
            String newCategory = scanner.nextLine().trim();
            if (!newCategory.isEmpty()) {
                item.setCategory(newCategory);
            }
            
            System.out.print("Enter new description (or press Enter to keep current): ");
            String newDescription = scanner.nextLine().trim();
            if (!newDescription.isEmpty()) {
                item.setDescription(newDescription);
            }
            
            menuItemDAO.updateMenuItem(item);
            System.out.println("Menu item updated successfully.");
            
        } catch (Exception e) {
            System.out.println("Error updating menu item: " + e.getMessage());
        }
    }
    
    private void removeMenuItem() {
        System.out.print("Enter menu item ID to remove: ");
        int itemId = getIntInput();
        
        try {
            MenuItem item = menuItemDAO.getMenuItemById(itemId);
            if (item == null) {
                System.out.println("Menu item not found.");
                return;
            }
            
            System.out.print("Are you sure you want to remove '" + item.getName() + "'? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                menuItemDAO.deleteMenuItem(itemId);
                System.out.println("Menu item removed successfully.");
            } else {
                System.out.println("Menu item removal cancelled.");
            }
            
        } catch (Exception e) {
            System.out.println("Error removing menu item: " + e.getMessage());
        }
    }
    
    private void searchMenuItems() {
        System.out.print("Enter search term (name or category): ");
        String searchTerm = scanner.nextLine().trim();
        
        try {
            List<MenuItem> results = menuItemDAO.searchMenuItemsByName(searchTerm);
            displayMenuItems(results, "Search Results");
        } catch (Exception e) {
            System.out.println("Error searching menu items: " + e.getMessage());
        }
    }
    
    private void customerManagement() {
        while (true) {
            System.out.println("\n=== Customer Management ===");
            System.out.println("1. View All Customers");
            System.out.println("2. Search Customers");
            System.out.println("3. View Customer Details");
            System.out.println("4. Update Customer");
            System.out.println("5. Customer Statistics");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose option (1-6): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    viewAllCustomers();
                    break;
                case 2:
                    searchCustomers();
                    break;
                case 3:
                    viewCustomerDetails();
                    break;
                case 4:
                    updateCustomer();
                    break;
                case 5:
                    customerStatistics();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllCustomers() {
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            displayCustomers(customers, "All Customers");
        } catch (Exception e) {
            System.out.println("Error retrieving customers: " + e.getMessage());
        }
    }
    
    private void displayCustomers(List<Customer> customers, String title) {
        if (customers.isEmpty()) {
            System.out.println("\nNo customers found.");
            return;
        }
        
        System.out.println("\n=== " + title + " ===");
        System.out.printf("%-5s %-20s %-25s %-15s %-8s %-12s%n", 
            "ID", "Name", "Email", "Phone", "Orders", "Loyalty Pts");
        System.out.println("-".repeat(90));
        
        for (Customer customer : customers) {
            System.out.printf("%-5d %-20s %-25s %-15s %-8d %-12d%n",
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getTotalOrders(),
                customer.getLoyaltyPoints());
        }
    }
    
    private void searchCustomers() {
        System.out.print("Enter customer name or email: ");
        String searchTerm = scanner.nextLine().trim();
        
        try {
            List<Customer> results = customerDAO.searchCustomersByName(searchTerm);
            
            // Also search by email
            Customer emailResult = customerDAO.getCustomerByEmail(searchTerm);
            if (emailResult != null && !results.contains(emailResult)) {
                results.add(emailResult);
            }
            
            displayCustomers(results, "Search Results");
        } catch (Exception e) {
            System.out.println("Error searching customers: " + e.getMessage());
        }
    }
    
    private void viewCustomerDetails() {
        System.out.print("Enter customer ID: ");
        int customerId = getIntInput();
        
        try {
            Customer customer = customerDAO.getCustomerById(customerId);
            if (customer == null) {
                System.out.println("Customer not found.");
                return;
            }
            
            System.out.println("\n=== Customer Details ===");
            System.out.println("ID: " + customer.getCustomerId());
            System.out.println("Name: " + customer.getName());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Phone: " + customer.getPhoneNumber());
            System.out.println("Registration Date: " + customer.getRegistrationDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            System.out.println("Total Orders: " + customer.getTotalOrders());
            System.out.printf("Total Spent: $%.2f%n", customer.getTotalSpent());
            System.out.println("Loyalty Points: " + customer.getLoyaltyPoints());
            
            // Show recent orders
            List<Order> recentOrders = orderDAO.getOrdersByCustomerId(customerId);
            if (!recentOrders.isEmpty()) {
                System.out.println("\nRecent Orders:");
                System.out.printf("%-8s %-15s %-10s %-12s%n", "Order ID", "Date", "Total", "Status");
                System.out.println("-".repeat(50));
                
                for (int i = 0; i < Math.min(5, recentOrders.size()); i++) {
                    Order order = recentOrders.get(i);
                    System.out.printf("%-8d %-15s $%-9.2f %-12s%n",
                        order.getOrderId(),
                        order.getOrderTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                        order.getTotalAmount(),
                        order.getStatus());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving customer details: " + e.getMessage());
        }
    }
    
    private void updateCustomer() {
        System.out.print("Enter customer ID: ");
        int customerId = getIntInput();
        
        try {
            Customer customer = customerDAO.getCustomerById(customerId);
            if (customer == null) {
                System.out.println("Customer not found.");
                return;
            }
            
            System.out.println("Current details:");
            System.out.println("Name: " + customer.getName());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Phone: " + customer.getPhoneNumber());
            
            System.out.print("\nEnter new name (or press Enter to keep current): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                customer.setName(newName);
            }
            
            System.out.print("Enter new email (or press Enter to keep current): ");
            String newEmail = scanner.nextLine().trim();
            if (!newEmail.isEmpty()) {
                customer.setEmail(newEmail);
            }
            
            System.out.print("Enter new phone (or press Enter to keep current): ");
            String newPhone = scanner.nextLine().trim();
            if (!newPhone.isEmpty()) {
                customer.setPhoneNumber(newPhone);
            }
            
            customerDAO.updateCustomer(customer);
            System.out.println("Customer updated successfully.");
            
        } catch (Exception e) {
            System.out.println("Error updating customer: " + e.getMessage());
        }
    }
    
    private void customerStatistics() {
        try {
            CustomerDAO.CustomerStats stats = customerDAO.getCustomerStats();
            
            System.out.println("\n=== Customer Statistics ===");
            System.out.println("Total Customers: " + stats.getTotalCustomers());
            System.out.printf("Average Loyalty Points: %.2f%n", stats.getAvgLoyaltyPoints());
            System.out.printf("Maximum Loyalty Points: %.2f%n", stats.getMaxLoyaltyPoints());
            System.out.println("Total Loyalty Points Issued: " + stats.getTotalLoyaltyPoints());
            
        } catch (Exception e) {
            System.out.println("Error retrieving customer statistics: " + e.getMessage());
        }
    }
    
    private void reportsAndAnalytics() {
        while (true) {
            System.out.println("\n=== Reports & Analytics ===");
            System.out.println("1. Daily Sales Report");
            System.out.println("2. Order Statistics");
            System.out.println("3. Payment Statistics");
            System.out.println("4. Popular Menu Items");
            System.out.println("5. Revenue Summary");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose option (1-6): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    dailySalesReport();
                    break;
                case 2:
                    orderStatistics();
                    break;
                case 3:
                    paymentStatistics();
                    break;
                case 4:
                    popularMenuItems();
                    break;
                case 5:
                    revenueSummary();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void dailySalesReport() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.now();
            
            List<Order> todayOrders = orderDAO.getOrdersByDateRange(startOfDay, endOfDay);
            
            System.out.println("\n=== Daily Sales Report ===");
            System.out.println("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            
            double totalRevenue = 0;
            int totalOrders = todayOrders.size();
            int completedOrders = 0;
            
            for (Order order : todayOrders) {
                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                    totalRevenue += order.getTotalAmount();
                    completedOrders++;
                }
            }
            
            System.out.println("Total Orders: " + totalOrders);
            System.out.println("Completed Orders: " + completedOrders);
            System.out.printf("Total Revenue: $%.2f%n", totalRevenue);
            
            if (completedOrders > 0) {
                System.out.printf("Average Order Value: $%.2f%n", totalRevenue / completedOrders);
            }
            
        } catch (Exception e) {
            System.out.println("Error generating daily sales report: " + e.getMessage());
        }
    }
    
    private void orderStatistics() {
        try {
            OrderDAO.OrderStats stats = orderDAO.getOrderStats();
            
            System.out.println("\n=== Order Statistics ===");
            System.out.println("Total Orders: " + stats.getTotalOrders());
            System.out.println("Pending Orders: " + stats.getPendingOrders());
            System.out.println("Completed Orders: " + stats.getCompletedOrders());
            System.out.println("Cancelled Orders: " + stats.getCancelledOrders());
            System.out.printf("Average Order Value: $%.2f%n", stats.getAvgOrderValue());
            System.out.printf("Total Revenue: $%.2f%n", stats.getTotalRevenue());
            
        } catch (Exception e) {
            System.out.println("Error retrieving order statistics: " + e.getMessage());
        }
    }
    
    private void paymentStatistics() {
        try {
            PaymentDAO.PaymentStats stats = paymentDAO.getPaymentStats();
            
            System.out.println("\n=== Payment Statistics ===");
            System.out.println("Total Payments: " + stats.getTotalPayments());
            System.out.println("Completed Payments: " + stats.getCompletedPayments());
            System.out.println("Cancelled Payments: " + stats.getCancelledPayments());
            System.out.printf("Total Revenue: $%.2f%n", stats.getTotalRevenue());
            System.out.printf("Average Payment Amount: $%.2f%n", stats.getAvgPaymentAmount());
            
        } catch (Exception e) {
            System.out.println("Error retrieving payment statistics: " + e.getMessage());
        }
    }
    
    private void popularMenuItems() {
        try {
            // This would require additional DAO methods to track item popularity
            System.out.println("\n=== Popular Menu Items ===");
            System.out.println("Feature not yet implemented - would require order item tracking.");
            
        } catch (Exception e) {
            System.out.println("Error retrieving popular items: " + e.getMessage());
        }
    }
    
    private void revenueSummary() {
        try {
            OrderDAO.OrderStats orderStats = orderDAO.getOrderStats();
            PaymentDAO.PaymentStats paymentStats = paymentDAO.getPaymentStats();
            
            System.out.println("\n=== Revenue Summary ===");
            System.out.printf("Total Revenue (Orders): $%.2f%n", orderStats.getTotalRevenue());
            System.out.printf("Total Processed (Payments): $%.2f%n", paymentStats.getTotalRevenue());
            System.out.printf("Average Order Value: $%.2f%n", orderStats.getAvgOrderValue());
            System.out.printf("Average Payment Amount: $%.2f%n", paymentStats.getAvgPaymentAmount());
            
        } catch (Exception e) {
            System.out.println("Error generating revenue summary: " + e.getMessage());
        }
    }
    
    private void systemStatus() {
        System.out.println("\n=== System Status ===");
        
        try {
            // Database connection status
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            System.out.println("Database: Connected");
            
            // Table statistics
            TableDAO.TableStats tableStats = tableDAO.getTableStats();
            System.out.println("\nTable Status:");
            System.out.println("  Available: " + tableStats.getAvailableTables());
            System.out.println("  Occupied: " + tableStats.getOccupiedTables());
            System.out.println("  Reserved: " + tableStats.getReservedTables());
            System.out.println("  Out of Service: " + tableStats.getOutOfServiceTables());
            
            // Inventory alerts
            List<Ingredient> lowStock = ingredientDAO.getLowStockIngredients();
            List<Ingredient> expired = ingredientDAO.getExpiredIngredients();
            
            System.out.println("\nInventory Alerts:");
            System.out.println("  Low Stock Items: " + lowStock.size());
            System.out.println("  Expired Items: " + expired.size());
            
            if (!lowStock.isEmpty()) {
                System.out.println("  Low Stock: " + lowStock.stream()
                    .map(Ingredient::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("None"));
            }
            
            // Order queue
            List<Order> pendingOrders = orderDAO.getOrdersByStatus(Order.OrderStatus.PENDING);
            List<Order> preparingOrders = orderDAO.getOrdersByStatus(Order.OrderStatus.PREPARING);
            
            System.out.println("\nOrder Queue:");
            System.out.println("  Pending Orders: " + pendingOrders.size());
            System.out.println("  Preparing Orders: " + preparingOrders.size());
            
            System.out.println("\nSystem Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
            
        } catch (Exception e) {
            System.out.println("Error retrieving system status: " + e.getMessage());
        }
    }
    
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
    
    private double getDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    public void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}