# Coffee Shop Management System

A comprehensive Java-based coffee shop management system implementing Object-Oriented Programming principles with SQLite database integration.

## Features

### Customer Features
- **Menu Browsing**: View available coffee items with detailed information
- **Order Placement**: Create orders with multiple items and customizations
- **Service Type Selection**: Choose between Dine-in and Takeaway options
- **Order Management**: View current orders and order history
- **Customer Registration**: Create and manage customer accounts

### Management Features
- **Menu Management**: Add, update, and remove menu items
- **Order Management**: View and manage all customer orders
- **Customer Management**: Access customer information and statistics
- **Inventory Management**: Track ingredients and stock levels
- **Table Management**: Manage table availability and reservations
- **Payment Processing**: Handle various payment methods and statuses
- **Analytics**: View sales reports and business statistics

## System Architecture

The system follows Object-Oriented Programming principles with the following key components:

### Core Classes
- **MenuItem**: Represents coffee items with pricing and descriptions
- **Customer**: Manages customer information and preferences
- **Order**: Handles order details, items, and status tracking
- **OrderItem**: Individual items within an order
- **Payment**: Processes payment information and methods
- **Table**: Manages table reservations and availability
- **Ingredient**: Tracks inventory items and stock levels

### Data Access Objects (DAO)
- **MenuItemDAO**: Database operations for menu items
- **CustomerDAO**: Customer data management
- **OrderDAO**: Order processing and retrieval
- **PaymentDAO**: Payment transaction handling
- **TableDAO**: Table management operations
- **IngredientDAO**: Inventory management

### Views
- **CustomerView**: Customer-facing interface
- **ManagementView**: Administrative interface
- **CoffeeShopApp**: Main application controller

## Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **SQLite JDBC Driver** (included in the project)
- **SLF4J Logging Framework** (included in the project)

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/chudu2110/OOP---Coffee-Shop.git
cd OOP---Coffee-Shop
```

### 2. Verify Dependencies
The following JAR files should be present in the project directory:
- `sqlite-jdbc.jar` - SQLite JDBC driver
- `slf4j-api.jar` - SLF4J API
- `slf4j-simple.jar` - SLF4J Simple Logger

### 3. Compile the Project
```bash
javac -cp . *.java
```

### 4. Run the Application
```bash
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" CoffeeShopApp
```

**Note for Linux/Mac users**: Use colon (`:`) instead of semicolon (`;`) as classpath separator:
```bash
java -cp ".:sqlite-jdbc.jar:slf4j-api.jar:slf4j-simple.jar" CoffeeShopApp
```

### 5. Database Initialization
- The application will automatically create `coffee_shop.db` on first run
- Sample menu items, customers, and tables will be populated
- No manual database setup required

## Usage Guide

### Starting the Application
1. Run the application using the command above
2. The system will automatically initialize the SQLite database
3. The main menu will appear with the following options:

```
========================================
           MAIN MENU
========================================
1. Customer Mode - Place Orders
2. Management Mode - Admin Panel
3. About
4. System Information
5. Exit
```

### Customer Mode
- **Browse Menu**: View available coffee items with prices
- **Place Orders**: Add items to cart and checkout
- **Account Management**: Register or login to existing account
- **Order History**: View previous orders and reorder items

### Management Mode
- **Menu Management**: Add new items, update prices, manage availability
- **Order Processing**: View pending orders, update order status
- **Customer Analytics**: View customer statistics and preferences
- **Inventory Control**: Manage ingredient stock levels
- **Financial Reports**: View sales data and revenue analytics

## Database Schema

The system uses SQLite database (`coffee_shop.db`) with the following main tables:

### Core Tables
- **`menu_items`** - Coffee menu items with pricing and descriptions
- **`customers`** - Customer information and loyalty points
- **`orders`** - Order records with status tracking
- **`order_items`** - Individual items within orders
- **`payments`** - Payment transactions and methods
- **`tables`** - Table management and reservations
- **`ingredients`** - Inventory tracking and stock levels

### Sample Menu Items
The system comes pre-loaded with the following menu items:

#### ☕ Coffee (5 items)
- **Espresso** - $2.50 - Rich and bold espresso shot
- **Americano** - $3.00 - Espresso with hot water
- **Latte** - $4.50 - Espresso with steamed milk
- **Cappuccino** - $4.00 - Espresso with steamed milk and foam
- **Mocha** - $5.00 - Espresso with chocolate and steamed milk

#### 🥐 Pastry (2 items)
- **Croissant** - $3.50 - Fresh buttery croissant
- **Blueberry Muffin** - $4.00 - Fresh blueberry muffin

#### 🍵 Tea (1 item)
- **Green Tea** - $2.00 - Premium green tea

### Database Initialization
The database is automatically created and populated with sample data when the application runs for the first time. The schema includes proper foreign key relationships, indexes for performance, and triggers for data consistency.

## Project Structure

```
├── Coffee.java              # Coffee item implementation
├── CoffeeShopApp.java       # Main application entry point
├── Customer.java            # Customer entity
├── CustomerDAO.java         # Customer data access
├── CustomerView.java        # Customer interface
├── DatabaseConnection.java  # Database connectivity
├── Ingredient.java          # Inventory item entity
├── IngredientDAO.java       # Inventory data access
├── ManagementView.java      # Management interface
├── MenuItem.java            # Menu item entity
├── MenuItemDAO.java         # Menu data access
├── Order.java               # Order entity
├── OrderDAO.java            # Order data access
├── OrderItem.java           # Order item entity
├── Payment.java             # Payment entity
├── PaymentDAO.java          # Payment data access
├── Table.java               # Table entity
├── TableDAO.java            # Table data access
├── database_schema.sql      # Database schema definition
├── sqlite-jdbc.jar         # SQLite JDBC driver
├── slf4j-api.jar           # SLF4J API
└── slf4j-simple.jar        # SLF4J Simple Logger
```

## OOP Principles Implemented

### Encapsulation
- Private fields with public getter/setter methods
- Data hiding and controlled access to object properties

### Inheritance
- Base classes extended by specialized implementations
- Code reuse through inheritance hierarchies

### Polymorphism
- Interface implementations for different payment methods
- Method overriding for specialized behaviors

### Abstraction
- DAO pattern for database operations
- Abstract interfaces for common operations

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## Troubleshooting

### Common Issues

**Database Connection Error**
- Ensure SQLite JDBC driver is in classpath
- Check file permissions for database creation

**Compilation Errors**
- Verify JDK version compatibility
- Ensure all source files are present

**Runtime Errors**
- Check classpath includes all required JAR files
- Verify database file permissions

## License

This project is developed for educational purposes as part of Object-Oriented Programming coursework.

## Author

- **GitHub**: [chudu2110](https://github.com/chudu2110)
- **Repository**: [OOP---Coffee-Shop](https://github.com/chudu2110/OOP---Coffee-Shop.git)

---

*Last updated: January 2025*