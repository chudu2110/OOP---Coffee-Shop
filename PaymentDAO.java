import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment operations
 * Handles all database operations related to payments
 */
public class PaymentDAO {
    private DatabaseConnection dbConnection;
    
    public PaymentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    // Create a new payment
    public int createPayment(Payment payment) {
        String query = "INSERT INTO payments (order_id, payment_method, amount, status, " +
                      "transaction_reference, notes) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, payment.getOrderId());
            pstmt.setString(2, payment.getPaymentMethod().toString());
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getStatus().toString());
            pstmt.setString(5, payment.getTransactionReference());
            pstmt.setString(6, payment.getFailureReason());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating payment: " + e.getMessage());
        }
        
        return -1;
    }
    
    // Get payment by ID
    public Payment getPaymentById(int paymentId) {
        String query = "SELECT * FROM payments WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, paymentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createPaymentFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payment by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get payments by order ID
    public List<Payment> getPaymentsByOrderId(int orderId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE order_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                if (payment != null) {
                    payments.add(payment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payments by order ID: " + e.getMessage());
        }
        
        return payments;
    }
    
    // Get payments by status
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE status = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                if (payment != null) {
                    payments.add(payment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payments by status: " + e.getMessage());
        }
        
        return payments;
    }
    
    // Get payments by payment method
    public List<Payment> getPaymentsByMethod(Payment.PaymentMethod method) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE payment_method = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, method.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                if (payment != null) {
                    payments.add(payment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payments by method: " + e.getMessage());
        }
        
        return payments;
    }
    
    // Get all payments
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments ORDER BY created_at DESC";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                if (payment != null) {
                    payments.add(payment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all payments: " + e.getMessage());
        }
        
        return payments;
    }
    
    // Update payment status
    public boolean updatePaymentStatus(int paymentId, Payment.PaymentStatus status) {
        String query = "UPDATE payments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }
    
    // Update payment transaction reference
    public boolean updateTransactionReference(int paymentId, String transactionReference) {
        String query = "UPDATE payments SET transaction_reference = ?, updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, transactionReference);
            pstmt.setInt(2, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating transaction reference: " + e.getMessage());
            return false;
        }
    }
    
    // Update payment notes
    public boolean updatePaymentNotes(int paymentId, String notes) {
        String query = "UPDATE payments SET notes = ?, updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, notes);
            pstmt.setInt(2, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating payment notes: " + e.getMessage());
            return false;
        }
    }
    
    // Process payment (mark as completed)
    public boolean processPayment(int paymentId, String transactionReference) {
        String query = "UPDATE payments SET status = 'COMPLETED', transaction_reference = ?, " +
                      "updated_at = CURRENT_TIMESTAMP WHERE payment_id = ? AND status = 'PENDING'";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, transactionReference);
            pstmt.setInt(2, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }
    
    // Refund payment
    public boolean refundPayment(int paymentId, String refundReason) {
        String query = "UPDATE payments SET status = 'REFUNDED', notes = ?, " +
                      "updated_at = CURRENT_TIMESTAMP WHERE payment_id = ? AND status = 'COMPLETED'";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, refundReason);
            pstmt.setInt(2, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error refunding payment: " + e.getMessage());
            return false;
        }
    }
    
    // Cancel payment
    public boolean cancelPayment(int paymentId, String cancelReason) {
        String query = "UPDATE payments SET status = 'CANCELLED', notes = ?, " +
                      "updated_at = CURRENT_TIMESTAMP WHERE payment_id = ? AND status = 'PENDING'";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, cancelReason);
            pstmt.setInt(2, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error cancelling payment: " + e.getMessage());
            return false;
        }
    }
    
    // Delete payment
    public boolean deletePayment(int paymentId) {
        String query = "DELETE FROM payments WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting payment: " + e.getMessage());
            return false;
        }
    }
    
    // Get payments by date range
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                if (payment != null) {
                    payments.add(payment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payments by date range: " + e.getMessage());
        }
        
        return payments;
    }
    
    // Get payment by transaction reference
    public Payment getPaymentByTransactionReference(String transactionReference) {
        String query = "SELECT * FROM payments WHERE transaction_reference = ?";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, transactionReference);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createPaymentFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payment by transaction reference: " + e.getMessage());
        }
        
        return null;
    }
    
    // Check if order is fully paid
    public boolean isOrderFullyPaid(int orderId) {
        String query = "SELECT SUM(amount) as total_paid FROM payments " +
                      "WHERE order_id = ? AND status = 'COMPLETED'";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double totalPaid = rs.getDouble("total_paid");
                
                // Get order total
                String orderQuery = "SELECT total_amount FROM orders WHERE order_id = ?";
                try (PreparedStatement orderPstmt = dbConnection.prepareStatement(orderQuery)) {
                    orderPstmt.setInt(1, orderId);
                    ResultSet orderRs = orderPstmt.executeQuery();
                    
                    if (orderRs.next()) {
                        double orderTotal = orderRs.getDouble("total_amount");
                        return totalPaid >= orderTotal;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking if order is fully paid: " + e.getMessage());
        }
        
        return false;
    }
    
    // Get total paid amount for order
    public double getTotalPaidForOrder(int orderId) {
        String query = "SELECT SUM(amount) as total_paid FROM payments " +
                      "WHERE order_id = ? AND status = 'COMPLETED'";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total_paid");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total paid for order: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    // Get payment statistics
    public PaymentStats getPaymentStats() {
        String query = "SELECT COUNT(*) as total_payments, " +
                      "SUM(CASE WHEN status = 'COMPLETED' THEN amount ELSE 0 END) as total_revenue, " +
                      "AVG(CASE WHEN status = 'COMPLETED' THEN amount ELSE NULL END) as avg_payment_amount, " +
                      "COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_payments, " +
                      "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_payments, " +
                      "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_payments, " +
                      "COUNT(CASE WHEN status = 'REFUNDED' THEN 1 END) as refunded_payments, " +
                      "COUNT(CASE WHEN payment_method = 'CASH' THEN 1 END) as cash_payments, " +
                      "COUNT(CASE WHEN payment_method = 'CARD' THEN 1 END) as card_payments, " +
                      "COUNT(CASE WHEN payment_method = 'MOBILE_PAYMENT' THEN 1 END) as mobile_payments, " +
                      "COUNT(CASE WHEN payment_method = 'LOYALTY_POINTS' THEN 1 END) as loyalty_payments " +
                      "FROM payments";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return new PaymentStats(
                    rs.getInt("total_payments"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("avg_payment_amount"),
                    rs.getInt("completed_payments"),
                    rs.getInt("pending_payments"),
                    rs.getInt("cancelled_payments"),
                    rs.getInt("refunded_payments"),
                    rs.getInt("cash_payments"),
                    rs.getInt("card_payments"),
                    rs.getInt("mobile_payments"),
                    rs.getInt("loyalty_payments")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payment statistics: " + e.getMessage());
        }
        
        return new PaymentStats(0, 0.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
    
    // Helper method to create Payment object from ResultSet
    private Payment createPaymentFromResultSet(ResultSet rs) throws SQLException {
        int paymentId = rs.getInt("payment_id");
        int orderId = rs.getInt("order_id");
        String paymentMethodStr = rs.getString("payment_method");
        double amount = rs.getDouble("amount");
        String statusStr = rs.getString("status");
        String transactionReference = rs.getString("transaction_reference");
        String notes = rs.getString("notes");
        
        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.valueOf(paymentMethodStr);
        Payment.PaymentStatus status = Payment.PaymentStatus.valueOf(statusStr);
        
        Payment payment = new Payment(paymentId, orderId, paymentMethod, amount);
        // Note: Payment status is set through payment processing methods
        payment.setTransactionReference(transactionReference);
        // Note: Payment notes functionality not available in current Payment class
        
        return payment;
    }
    
    // Inner class for payment statistics
    public static class PaymentStats {
        private final int totalPayments;
        private final double totalRevenue;
        private final double avgPaymentAmount;
        private final int completedPayments;
        private final int pendingPayments;
        private final int cancelledPayments;
        private final int refundedPayments;
        private final int cashPayments;
        private final int cardPayments;
        private final int mobilePayments;
        private final int loyaltyPayments;
        
        public PaymentStats(int totalPayments, double totalRevenue, double avgPaymentAmount,
                           int completedPayments, int pendingPayments, int cancelledPayments,
                           int refundedPayments, int cashPayments, int cardPayments,
                           int mobilePayments, int loyaltyPayments) {
            this.totalPayments = totalPayments;
            this.totalRevenue = totalRevenue;
            this.avgPaymentAmount = avgPaymentAmount;
            this.completedPayments = completedPayments;
            this.pendingPayments = pendingPayments;
            this.cancelledPayments = cancelledPayments;
            this.refundedPayments = refundedPayments;
            this.cashPayments = cashPayments;
            this.cardPayments = cardPayments;
            this.mobilePayments = mobilePayments;
            this.loyaltyPayments = loyaltyPayments;
        }
        
        // Getters
        public int getTotalPayments() { return totalPayments; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAvgPaymentAmount() { return avgPaymentAmount; }
        public int getCompletedPayments() { return completedPayments; }
        public int getPendingPayments() { return pendingPayments; }
        public int getCancelledPayments() { return cancelledPayments; }
        public int getRefundedPayments() { return refundedPayments; }
        public int getCashPayments() { return cashPayments; }
        public int getCardPayments() { return cardPayments; }
        public int getMobilePayments() { return mobilePayments; }
        public int getLoyaltyPayments() { return loyaltyPayments; }
        
        // Calculate success rate
        public double getSuccessRate() {
            if (totalPayments == 0) return 0.0;
            return ((double) completedPayments / totalPayments) * 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("Payment Statistics:\n" +
                               "Total Payments: %d\n" +
                               "Total Revenue: $%.2f\n" +
                               "Average Payment: $%.2f\n" +
                               "Completed: %d (%.1f%%)\n" +
                               "Pending: %d\n" +
                               "Cancelled: %d\n" +
                               "Refunded: %d\n" +
                               "Cash: %d, Card: %d, Mobile: %d, Loyalty: %d",
                               totalPayments, totalRevenue, avgPaymentAmount,
                               completedPayments, getSuccessRate(),
                               pendingPayments, cancelledPayments, refundedPayments,
                               cashPayments, cardPayments, mobilePayments, loyaltyPayments);
        }
    }
}