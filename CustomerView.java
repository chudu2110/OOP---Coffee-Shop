import java.util.*;
import java.time.LocalDateTime;

public class CustomerView {
    private Scanner scanner;
    private MenuItemDAO menuItemDAO;
    private CustomerDAO customerDAO;
    private OrderDAO orderDAO;
    private TableDAO tableDAO;
    private PaymentDAO paymentDAO;
    private Customer currentCustomer;
    private Order currentOrder;
    
    public CustomerView() {
        this.scanner = new Scanner(System.in);
        this.menuItemDAO = new MenuItemDAO();
        this.customerDAO = new CustomerDAO();
        this.orderDAO = new OrderDAO();
        this.tableDAO = new TableDAO();
        this.paymentDAO = new PaymentDAO();
    }
    
    public void start() {
        System.out.println("\n=== Welcome to Coffee Shop ===\n");
        
        // Customer login/registration
        handleCustomerLogin();
        
        if (currentCustomer != null) {
            showMainMenu();
        }
    }
    
    private void handleCustomerLogin() {
        System.out.println("1. Login with existing account");
        System.out.println("2. Register new account");
        System.out.println("3. Continue as guest");
        System.out.print("Choose option (1-3): ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                loginCustomer();
                break;
            case 2:
                registerCustomer();
                break;
            case 3:
                createGuestCustomer();
                break;
            default:
                System.out.println("Invalid choice. Continuing as guest.");
                createGuestCustomer();
        }
    }
    
    private void loginCustomer() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        
        try {
            currentCustomer = customerDAO.getCustomerByEmail(email);
            if (currentCustomer != null) {
                System.out.println("Welcome back, " + currentCustomer.getName() + "!");
                System.out.println("Loyalty Points: " + currentCustomer.getLoyaltyPoints());
            } else {
                System.out.println("Customer not found. Please register or continue as guest.");
                handleCustomerLogin();
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            createGuestCustomer();
        }
    }
    
    private void registerCustomer() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine().trim();
        
        try {
            if (customerDAO.emailExists(email)) {
                System.out.println("Email already exists. Please login instead.");
                loginCustomer();
                return;
            }
            
            Customer newCustomer = new Customer(0, name, email, phone);
            int customerId = customerDAO.createCustomer(newCustomer);
            currentCustomer = customerDAO.getCustomerById(customerId);
            
            System.out.println("Registration successful! Welcome, " + name + "!");
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
            createGuestCustomer();
        }
    }
    
    private void createGuestCustomer() {
        currentCustomer = new Customer(0, "Guest", "guest@temp.com", "000-000-0000");
        System.out.println("Continuing as guest user.");
    }
    
    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. View Menu & Place Order");
            System.out.println("2. View My Orders");
            System.out.println("3. View Loyalty Points");
            System.out.println("4. Exit");
            System.out.print("Choose option (1-4): ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    startNewOrder();
                    break;
                case 2:
                    viewMyOrders();
                    break;
                case 3:
                    viewLoyaltyPoints();
                    break;
                case 4:
                    System.out.println("Thank you for visiting! Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void startNewOrder() {
        try {
            // Choose service type
            Order.ServiceType serviceType = chooseServiceType();
            
            // Create new order
            currentOrder = new Order(0, currentCustomer.getCustomerId(), serviceType);
            
            // If dine-in, select table
            if (serviceType == Order.ServiceType.DINE_IN) {
                selectTable();
            }
            
            // Show menu and add items
            addItemsToOrder();
            
            // Confirm and process order
            if (currentOrder.getOrderItems().size() > 0) {
                confirmOrder();
            } else {
                System.out.println("No items added to order. Order cancelled.");
            }
            
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
        }
    }
    
    private Order.ServiceType chooseServiceType() {
        System.out.println("\n=== Service Type ===");
        System.out.println("1. Dine In");
        System.out.println("2. Take Away");
        System.out.print("Choose service type (1-2): ");
        
        int choice = getIntInput();
        return (choice == 1) ? Order.ServiceType.DINE_IN : Order.ServiceType.TAKEAWAY;
    }
    
    private void selectTable() {
        try {
            List<Table> availableTables = tableDAO.getAvailableTables();
            
            if (availableTables.isEmpty()) {
                  System.out.println("No tables available. Switching to take-away.");
                  currentOrder = new Order(0, currentCustomer.getCustomerId(), Order.ServiceType.TAKEAWAY);
                  return;
              }
            
            System.out.println("\n=== Available Tables ===");
            for (Table table : availableTables) {
                System.out.println(table.getTableNumber() + ". Capacity: " + table.getCapacity());
            }
            
            System.out.print("Choose table number: ");
            int tableNumber = getIntInput();
            
            Table selectedTable = availableTables.stream()
                .filter(t -> t.getTableNumber() == tableNumber)
                .findFirst()
                .orElse(null);
            
            if (selectedTable != null) {
                currentOrder.setTableNumber(tableNumber);
                System.out.println("Table " + tableNumber + " selected.");
            } else {
                System.out.println("Invalid table number. Using first available table.");
                currentOrder.setTableNumber(availableTables.get(0).getTableNumber());
            }
            
        } catch (Exception e) {
              System.out.println("Error selecting table: " + e.getMessage());
              currentOrder = new Order(0, currentCustomer.getCustomerId(), Order.ServiceType.TAKEAWAY);
          }
    }
    
    private void addItemsToOrder() {
        while (true) {
            try {
                // Display menu
                displayMenu();
                
                System.out.println("\n=== Order Options ===");
                System.out.println("1. Add item to order");
                System.out.println("2. View current order");
                System.out.println("3. Remove item from order");
                System.out.println("4. Proceed to checkout");
                System.out.print("Choose option (1-4): ");
                
                int choice = getIntInput();
                
                switch (choice) {
                    case 1:
                        addMenuItem();
                        break;
                    case 2:
                        viewCurrentOrder();
                        break;
                    case 3:
                        removeMenuItem();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                System.out.println("Error managing order: " + e.getMessage());
            }
        }
    }
    
    private void displayMenu() {
        try {
            List<MenuItem> menuItems = menuItemDAO.getAllMenuItems();
            
            System.out.println("\n=== Coffee Menu ===");
            System.out.printf("%-5s %-20s %-10s %-30s%n", "ID", "Name", "Price", "Description");
            System.out.println("-".repeat(70));
            
            for (MenuItem item : menuItems) {
                System.out.printf("%-5d %-20s $%-9.2f %-30s%n", 
                    item.getId(), 
                    item.getName(), 
                    item.getBasePrice(), 
                    item.getDescription());
            }
            
        } catch (Exception e) {
            System.out.println("Error displaying menu: " + e.getMessage());
        }
    }
    
    private void addMenuItem() {
        try {
            System.out.print("Enter item ID: ");
            int itemId = getIntInput();
            
            MenuItem menuItem = menuItemDAO.getMenuItemById(itemId);
            if (menuItem == null) {
                System.out.println("Item not found.");
                return;
            }
            
            System.out.print("Enter quantity: ");
            int quantity = getIntInput();
            
            if (quantity <= 0) {
                System.out.println("Invalid quantity.");
                return;
            }
            
            // Handle coffee customizations
            String customizations = "";
            if (menuItem instanceof Coffee) {
                customizations = getCoffeeCustomizations();
            }
            
            currentOrder.addItem(menuItem, quantity);
            
            System.out.println("Added " + quantity + "x " + menuItem.getName() + " to order.");
            if (!customizations.isEmpty()) {
                System.out.println("Customizations: " + customizations);
            }
            
        } catch (Exception e) {
            System.out.println("Error adding item: " + e.getMessage());
        }
    }
    
    private String getCoffeeCustomizations() {
        List<String> customizations = new ArrayList<>();
        
        System.out.println("\n=== Coffee Customizations ===");
        System.out.println("1. Size (Small/Medium/Large)");
        System.out.println("2. Milk type");
        System.out.println("3. Extra shots");
        System.out.println("4. Skip customizations");
        
        while (true) {
            System.out.print("Choose customization (1-4): ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    System.out.print("Size (Small/Medium/Large): ");
                    String size = scanner.nextLine().trim();
                    if (!size.isEmpty()) {
                        customizations.add("Size: " + size);
                    }
                    break;
                case 2:
                    System.out.print("Milk type (Regular/Almond/Soy/Oat): ");
                    String milk = scanner.nextLine().trim();
                    if (!milk.isEmpty()) {
                        customizations.add("Milk: " + milk);
                    }
                    break;
                case 3:
                    System.out.print("Extra shots (1-3): ");
                    int shots = getIntInput();
                    if (shots > 0 && shots <= 3) {
                        customizations.add("Extra shots: " + shots);
                    }
                    break;
                case 4:
                    return String.join(", ", customizations);
                default:
                    System.out.println("Invalid choice.");
            }
            
            System.out.print("Add more customizations? (y/n): ");
            String more = scanner.nextLine().trim().toLowerCase();
            if (!more.equals("y") && !more.equals("yes")) {
                break;
            }
        }
        
        return String.join(", ", customizations);
    }
    
    private void viewCurrentOrder() {
        if (currentOrder == null || currentOrder.getOrderItems().isEmpty()) {
            System.out.println("\nNo items in current order.");
            return;
        }
        
        System.out.println("\n=== Current Order ===");
        System.out.println("Service Type: " + currentOrder.getServiceType());
        if (currentOrder.getTableNumber() > 0) {
            System.out.println("Table: " + currentOrder.getTableNumber());
        }
        System.out.println();
        
        System.out.printf("%-20s %-5s %-10s %-15s %-30s%n", "Item", "Qty", "Unit Price", "Total", "Customizations");
        System.out.println("-".repeat(85));
        
        for (OrderItem item : currentOrder.getOrderItems()) {
            System.out.printf("%-20s %-5d $%-9.2f $%-14.2f %-30s%n",
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice() * item.getQuantity(),
                item.getCustomizations());
        }
        
        System.out.println("-".repeat(85));
        System.out.printf("Total: $%.2f%n", currentOrder.getTotalAmount());
    }
    
    private void removeMenuItem() {
        if (currentOrder == null || currentOrder.getOrderItems().isEmpty()) {
            System.out.println("No items to remove.");
            return;
        }
        
        viewCurrentOrder();
        
        System.out.print("\nEnter item name to remove: ");
        String itemName = scanner.nextLine().trim();
        
        boolean removed = currentOrder.getOrderItems().removeIf(item -> 
            item.getMenuItem().getName().equalsIgnoreCase(itemName));
        
        if (removed) {
            System.out.println("Item removed from order.");
        } else {
            System.out.println("Item not found in order.");
        }
    }
    
    private void confirmOrder() {
        viewCurrentOrder();
        
        System.out.print("\nConfirm order? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            processPayment();
        } else {
            System.out.println("Order cancelled.");
        }
    }
    
    private void processPayment() {
        try {
            double totalAmount = currentOrder.getTotalAmount();
            
            System.out.println("\n=== Payment ===");
            System.out.printf("Total Amount: $%.2f%n", totalAmount);
            
            // Show payment options
            System.out.println("\nPayment Methods:");
            System.out.println("1. Cash");
            System.out.println("2. Credit Card");
            System.out.println("3. Mobile Payment");
            if (currentCustomer.getLoyaltyPoints() >= totalAmount * 100) {
                System.out.println("4. Loyalty Points (" + currentCustomer.getLoyaltyPoints() + " available)");
            }
            
            System.out.print("Choose payment method: ");
            int paymentChoice = getIntInput();
            
            Payment.PaymentMethod paymentMethod;
            switch (paymentChoice) {
                case 1:
                    paymentMethod = Payment.PaymentMethod.CASH;
                    break;
                case 2:
                    paymentMethod = Payment.PaymentMethod.CREDIT_CARD;
                    break;
                case 3:
                    paymentMethod = Payment.PaymentMethod.CASH;
                    break;
                case 4:
                    if (currentCustomer.getLoyaltyPoints() >= totalAmount * 100) {
                        paymentMethod = Payment.PaymentMethod.LOYALTY_POINTS;
                    } else {
                        System.out.println("Insufficient loyalty points. Using cash.");
                        paymentMethod = Payment.PaymentMethod.CASH;
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Using cash.");
                    paymentMethod = Payment.PaymentMethod.CASH;
            }
            
            // Create and save order
            int orderId = orderDAO.createOrder(currentOrder);
            // Order ID is set internally
            
            // Add order items
            // Order items are handled internally by the Order class
            
            // Process payment
            Payment payment = new Payment(0, orderId, paymentMethod, totalAmount);
            // Payment status is handled internally
            int paymentId = paymentDAO.createPayment(payment);
            
            // Update order status
            currentOrder.setStatus(Order.OrderStatus.CONFIRMED);
            orderDAO.updateOrderStatus(orderId, currentOrder.getStatus());
            
            // Update customer loyalty points and order history
            if (currentCustomer.getCustomerId() > 0) {
                // Order history is managed by the system
                if (paymentMethod == Payment.PaymentMethod.LOYALTY_POINTS) {
                    // Loyalty points deduction handled by system
                } else {
                    currentCustomer.addLoyaltyPoints((int)(totalAmount * 10)); // 10 points per dollar
                }
                customerDAO.updateCustomer(currentCustomer);
            }
            
            // Reserve table if dine-in
            if (currentOrder.getServiceType() == Order.ServiceType.DINE_IN && currentOrder.getTableNumber() > 0) {
                Table table = tableDAO.getTableById(currentOrder.getTableNumber());
                if (table != null) {
                    table.occupyTable(1);
                    tableDAO.updateTableStatus(table.getTableNumber(), table.getStatus());
                }
            }
            
            System.out.println("\n=== Order Confirmed ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Total Paid: $" + String.format("%.2f", totalAmount));
            System.out.println("Payment Method: " + paymentMethod);
            
            if (currentCustomer.getCustomerId() > 0) {
                System.out.println("Loyalty Points Earned: " + (int)(totalAmount * 10));
                System.out.println("Total Loyalty Points: " + currentCustomer.getLoyaltyPoints());
            }
            
            if (currentOrder.getServiceType() == Order.ServiceType.DINE_IN) {
                System.out.println("Table: " + currentOrder.getTableNumber());
                System.out.println("Please proceed to your table. Your order will be served shortly.");
            } else {
                System.out.println("Please wait for your order to be prepared for pickup.");
            }
            
        } catch (Exception e) {
            System.out.println("Error processing payment: " + e.getMessage());
        }
    }
    
    private void viewMyOrders() {
        if (currentCustomer.getCustomerId() <= 0) {
            System.out.println("Order history not available for guest users.");
            return;
        }
        
        try {
            List<Order> orders = orderDAO.getOrdersByCustomerId(currentCustomer.getCustomerId());
            
            if (orders.isEmpty()) {
                System.out.println("No previous orders found.");
                return;
            }
            
            System.out.println("\n=== My Orders ===");
            System.out.printf("%-8s %-12s %-15s %-10s %-12s%n", "Order ID", "Date", "Service Type", "Total", "Status");
            System.out.println("-".repeat(65));
            
            for (Order order : orders) {
                System.out.printf("%-8d %-12s %-15s $%-9.2f %-12s%n",
                    order.getOrderId(),
                    order.getOrderTime().toLocalDate(),
                    order.getServiceType(),
                    order.getTotalAmount(),
                    order.getStatus());
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving orders: " + e.getMessage());
        }
    }
    
    private void viewLoyaltyPoints() {
        if (currentCustomer.getCustomerId() <= 0) {
            System.out.println("Loyalty points not available for guest users.");
            return;
        }
        
        System.out.println("\n=== Loyalty Points ===");
        System.out.println("Current Points: " + currentCustomer.getLoyaltyPoints());
        System.out.println("Total Orders: " + currentCustomer.getTotalOrders());
        System.out.printf("Total Spent: $%.2f%n", currentCustomer.getTotalSpent());
        
        double pointsValue = currentCustomer.getLoyaltyPoints() / 100.0;
        System.out.printf("Points Value: $%.2f%n", pointsValue);
        
        System.out.println("\nNote: 100 points = $1.00");
        System.out.println("Earn 10 points for every $1 spent!");
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
    
    /**
     * Cleanup method for proper resource management
     */
    public void cleanup() {
        try {
            if (scanner != null) {
                // Note: Don't close System.in scanner as it may be used by other parts
                // scanner.close();
            }
            System.out.println("Customer view cleanup completed.");
        } catch (Exception e) {
            System.err.println("Error during customer view cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Utility method to display an error message
     * @param error the error message to display
     */
    private void displayError(String error) {
        System.err.println("\n[ERROR] " + error);
    }
}