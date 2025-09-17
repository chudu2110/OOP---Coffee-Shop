-- Coffee Shop Database Schema
-- This file contains the SQL schema for the coffee shop application

-- Create database (uncomment if needed)
-- CREATE DATABASE coffee_shop;
-- USE coffee_shop;

-- Table for menu items
CREATE TABLE menu_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    coffee_type VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for customers
CREATE TABLE customers (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone_number VARCHAR(20),
    loyalty_points DECIMAL(10,2) DEFAULT 0.00,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for tables
CREATE TABLE tables (
    table_number INTEGER PRIMARY KEY,
    capacity INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    current_customer_id INTEGER,
    occupied_since TIMESTAMP,
    reserved_until TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (current_customer_id) REFERENCES customers(customer_id)
);

-- Table for orders
CREATE TABLE orders (
    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    service_type VARCHAR(20) NOT NULL,
    table_number INTEGER,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    special_instructions TEXT,
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (table_number) REFERENCES tables(table_number)
);

-- Table for order items
CREATE TABLE order_items (
    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    customizations TEXT,
    size VARCHAR(20),
    is_hot BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Table for payments
CREATE TABLE payments (
    payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    amount DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) DEFAULT 0.00,
    change_given DECIMAL(10,2) DEFAULT 0.00,
    transaction_reference VARCHAR(100),
    card_last_four_digits VARCHAR(4),
    failure_reason TEXT,
    payment_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- Table for ingredients
CREATE TABLE ingredients (
    ingredient_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    unit VARCHAR(20) NOT NULL,
    current_stock DECIMAL(10,3) DEFAULT 0.000,
    minimum_stock DECIMAL(10,3) NOT NULL,
    maximum_stock DECIMAL(10,3) NOT NULL,
    cost_per_unit DECIMAL(10,2) NOT NULL,
    expiration_date DATE,
    supplier VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for menu item ingredients (many-to-many relationship)
CREATE TABLE menu_item_ingredients (
    menu_item_id INTEGER NOT NULL,
    ingredient_id INTEGER NOT NULL,
    quantity_required DECIMAL(10,3) NOT NULL,
    PRIMARY KEY (menu_item_id, ingredient_id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_time ON orders(order_time);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_tables_status ON tables(status);
CREATE INDEX idx_ingredients_is_active ON ingredients(is_active);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_is_available ON menu_items(is_available);

-- Insert sample data

-- Sample menu items
INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES
('Espresso', 'Rich and bold espresso shot', 2.50, 'Coffee', 'Coffee', 'ESPRESSO', TRUE),
('Americano', 'Espresso with hot water', 3.00, 'Coffee', 'Coffee', 'AMERICANO', TRUE),
('Latte', 'Espresso with steamed milk', 4.50, 'Coffee', 'Coffee', 'LATTE', TRUE),
('Cappuccino', 'Espresso with steamed milk and foam', 4.00, 'Coffee', 'Coffee', 'CAPPUCCINO', TRUE),
('Macchiato', 'Espresso with a dollop of steamed milk', 4.25, 'Coffee', 'Coffee', 'MACCHIATO', TRUE),
('Mocha', 'Espresso with chocolate and steamed milk', 5.00, 'Coffee', 'Coffee', 'MOCHA', TRUE),
('Frappuccino', 'Blended iced coffee drink', 5.50, 'Coffee', 'Coffee', 'FRAPPUCCINO', TRUE),
('Croissant', 'Buttery, flaky pastry', 3.50, 'Pastry', 'Food', NULL, TRUE),
('Muffin', 'Fresh baked muffin', 2.75, 'Pastry', 'Food', NULL, TRUE),
('Sandwich', 'Grilled sandwich', 6.50, 'Food', 'Food', NULL, TRUE);

-- Sample customers
INSERT INTO customers (name, email, phone_number, loyalty_points) VALUES
('John Doe', 'john.doe@email.com', '555-0101', 25.50),
('Jane Smith', 'jane.smith@email.com', '555-0102', 15.75),
('Bob Johnson', 'bob.johnson@email.com', '555-0103', 42.25),
('Alice Brown', 'alice.brown@email.com', '555-0104', 8.00),
('Charlie Wilson', 'charlie.wilson@email.com', '555-0105', 33.50);

-- Sample tables
INSERT INTO tables (table_number, capacity, status) VALUES
(1, 2, 'AVAILABLE'),
(2, 4, 'AVAILABLE'),
(3, 2, 'AVAILABLE'),
(4, 6, 'AVAILABLE'),
(5, 4, 'AVAILABLE'),
(6, 2, 'AVAILABLE'),
(7, 4, 'AVAILABLE'),
(8, 8, 'AVAILABLE');

-- Sample ingredients
INSERT INTO ingredients (name, description, unit, current_stock, minimum_stock, maximum_stock, cost_per_unit, supplier, is_active) VALUES
('Coffee Beans - Arabica', 'Premium Arabica coffee beans', 'KILOGRAMS', 50.0, 10.0, 100.0, 12.50, 'Coffee Suppliers Inc', TRUE),
('Coffee Beans - Robusta', 'Strong Robusta coffee beans', 'KILOGRAMS', 30.0, 5.0, 80.0, 10.00, 'Coffee Suppliers Inc', TRUE),
('Milk', 'Fresh whole milk', 'LITERS', 25.0, 5.0, 50.0, 1.50, 'Local Dairy', TRUE),
('Sugar', 'White granulated sugar', 'KILOGRAMS', 15.0, 3.0, 30.0, 2.00, 'Sweet Supplies', TRUE),
('Chocolate Syrup', 'Rich chocolate syrup', 'LITERS', 8.0, 2.0, 20.0, 5.00, 'Flavor Co', TRUE),
('Vanilla Syrup', 'Pure vanilla syrup', 'LITERS', 6.0, 1.0, 15.0, 4.50, 'Flavor Co', TRUE),
('Caramel Syrup', 'Sweet caramel syrup', 'LITERS', 7.0, 1.0, 15.0, 4.75, 'Flavor Co', TRUE),
('Whipped Cream', 'Fresh whipped cream', 'LITERS', 5.0, 1.0, 10.0, 3.00, 'Local Dairy', TRUE),
('Paper Cups - Small', 'Small disposable cups', 'PIECES', 500, 100, 1000, 0.05, 'Cup Supply Co', TRUE),
('Paper Cups - Medium', 'Medium disposable cups', 'PIECES', 400, 100, 1000, 0.07, 'Cup Supply Co', TRUE),
('Paper Cups - Large', 'Large disposable cups', 'PIECES', 300, 100, 1000, 0.09, 'Cup Supply Co', TRUE);

-- Sample menu item ingredients relationships
INSERT INTO menu_item_ingredients (menu_item_id, ingredient_id, quantity_required) VALUES
-- Espresso
(1, 1, 0.020), -- 20g Arabica beans
-- Americano
(2, 1, 0.020), -- 20g Arabica beans
-- Latte
(3, 1, 0.020), -- 20g Arabica beans
(3, 3, 0.200), -- 200ml milk
-- Cappuccino
(4, 1, 0.020), -- 20g Arabica beans
(4, 3, 0.150), -- 150ml milk
-- Macchiato
(5, 1, 0.020), -- 20g Arabica beans
(5, 3, 0.050), -- 50ml milk
-- Mocha
(6, 1, 0.020), -- 20g Arabica beans
(6, 3, 0.200), -- 200ml milk
(6, 5, 0.030), -- 30ml chocolate syrup
-- Frappuccino
(7, 1, 0.020), -- 20g Arabica beans
(7, 3, 0.250), -- 250ml milk
(7, 8, 0.050); -- 50ml whipped cream

-- Create triggers to update timestamps
CREATE TRIGGER update_menu_items_timestamp 
    AFTER UPDATE ON menu_items
    BEGIN
        UPDATE menu_items SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
    END;

CREATE TRIGGER update_customers_timestamp 
    AFTER UPDATE ON customers
    BEGIN
        UPDATE customers SET updated_at = CURRENT_TIMESTAMP WHERE customer_id = NEW.customer_id;
    END;

CREATE TRIGGER update_tables_timestamp 
    AFTER UPDATE ON tables
    BEGIN
        UPDATE tables SET updated_at = CURRENT_TIMESTAMP WHERE table_number = NEW.table_number;
    END;

CREATE TRIGGER update_orders_timestamp 
    AFTER UPDATE ON orders
    BEGIN
        UPDATE orders SET updated_at = CURRENT_TIMESTAMP WHERE order_id = NEW.order_id;
    END;

CREATE TRIGGER update_payments_timestamp 
    AFTER UPDATE ON payments
    BEGIN
        UPDATE payments SET updated_at = CURRENT_TIMESTAMP WHERE payment_id = NEW.payment_id;
    END;

CREATE TRIGGER update_ingredients_timestamp 
    AFTER UPDATE ON ingredients
    BEGIN
        UPDATE ingredients SET updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = NEW.ingredient_id;
    END;

-- Views for common queries

-- View for current table status
CREATE VIEW table_status_view AS
SELECT 
    t.table_number,
    t.capacity,
    t.status,
    c.name as customer_name,
    t.occupied_since,
    t.reserved_until,
    t.notes
FROM tables t
LEFT JOIN customers c ON t.current_customer_id = c.customer_id;

-- View for order summary
CREATE VIEW order_summary_view AS
SELECT 
    o.order_id,
    c.name as customer_name,
    o.status,
    o.service_type,
    o.table_number,
    o.total_amount,
    o.order_time,
    COUNT(oi.order_item_id) as item_count
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id, c.name, o.status, o.service_type, o.table_number, o.total_amount, o.order_time;

-- View for low stock ingredients
CREATE VIEW low_stock_ingredients_view AS
SELECT 
    ingredient_id,
    name,
    current_stock,
    minimum_stock,
    unit,
    (current_stock / minimum_stock * 100) as stock_percentage
FROM ingredients
WHERE current_stock <= minimum_stock AND is_active = TRUE;

-- View for menu items with ingredient requirements
CREATE VIEW menu_items_with_ingredients_view AS
SELECT 
    mi.id as menu_item_id,
    mi.name as menu_item_name,
    mi.base_price,
    mi.category,
    i.name as ingredient_name,
    mii.quantity_required,
    i.unit,
    i.current_stock,
    (i.current_stock >= mii.quantity_required) as ingredient_available
FROM menu_items mi
LEFT JOIN menu_item_ingredients mii ON mi.id = mii.menu_item_id
LEFT JOIN ingredients i ON mii.ingredient_id = i.ingredient_id
WHERE mi.is_available = TRUE;