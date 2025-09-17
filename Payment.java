import java.time.LocalDateTime;

/**
 * Payment class representing payment transactions
 * Handles different payment methods and transaction details
 */
public class Payment {
    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, MOBILE_PAYMENT, LOYALTY_POINTS
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
    }
    
    private int paymentId;
    private int orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private double amount;
    private double amountPaid;
    private double changeGiven;
    private LocalDateTime paymentTime;
    private String transactionReference;
    private String cardLastFourDigits;
    private String failureReason;
    
    // Constructor
    public Payment(int paymentId, int orderId, PaymentMethod paymentMethod, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.amountPaid = 0.0;
        this.changeGiven = 0.0;
        this.transactionReference = "";
        this.cardLastFourDigits = "";
        this.failureReason = "";
    }
    
    // Getters
    public int getPaymentId() {
        return paymentId;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public double getAmountPaid() {
        return amountPaid;
    }
    
    public double getChangeGiven() {
        return changeGiven;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public String getCardLastFourDigits() {
        return cardLastFourDigits;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    // Setters
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference != null ? transactionReference : "";
    }
    
    public void setCardLastFourDigits(String cardLastFourDigits) {
        if (cardLastFourDigits != null && cardLastFourDigits.length() == 4) {
            this.cardLastFourDigits = cardLastFourDigits;
        }
    }
    
    // Methods
    public boolean processCashPayment(double cashReceived) {
        if (cashReceived < amount) {
            this.status = PaymentStatus.FAILED;
            this.failureReason = "Insufficient cash provided";
            return false;
        }
        
        this.status = PaymentStatus.PROCESSING;
        this.amountPaid = cashReceived;
        this.changeGiven = cashReceived - amount;
        
        return completePayment();
    }
    
    public boolean processCardPayment(String cardNumber, String expiryDate, String cvv) {
        // Simulate card validation
        if (cardNumber == null || cardNumber.length() < 16 || 
            expiryDate == null || cvv == null || cvv.length() != 3) {
            this.status = PaymentStatus.FAILED;
            this.failureReason = "Invalid card details";
            return false;
        }
        
        this.status = PaymentStatus.PROCESSING;
        this.amountPaid = amount;
        this.cardLastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        this.transactionReference = generateTransactionReference();
        
        // Simulate payment processing
        return completePayment();
    }
    
    public boolean processMobilePayment(String mobilePaymentId) {
        if (mobilePaymentId == null || mobilePaymentId.trim().isEmpty()) {
            this.status = PaymentStatus.FAILED;
            this.failureReason = "Invalid mobile payment ID";
            return false;
        }
        
        this.status = PaymentStatus.PROCESSING;
        this.amountPaid = amount;
        this.transactionReference = mobilePaymentId;
        
        return completePayment();
    }
    
    public boolean processLoyaltyPointsPayment(double pointsUsed, double pointsToMoneyRatio) {
        double pointsValue = pointsUsed * pointsToMoneyRatio;
        
        if (pointsValue < amount) {
            this.status = PaymentStatus.FAILED;
            this.failureReason = "Insufficient loyalty points";
            return false;
        }
        
        this.status = PaymentStatus.PROCESSING;
        this.amountPaid = amount;
        this.transactionReference = "LOYALTY_" + (int)pointsUsed;
        
        return completePayment();
    }
    
    private boolean completePayment() {
        // Simulate payment completion (could fail in real scenario)
        if (Math.random() > 0.05) { // 95% success rate
            this.status = PaymentStatus.COMPLETED;
            this.paymentTime = LocalDateTime.now();
            return true;
        } else {
            this.status = PaymentStatus.FAILED;
            this.failureReason = "Payment processing failed";
            return false;
        }
    }
    
    public boolean refundPayment() {
        if (status == PaymentStatus.COMPLETED) {
            this.status = PaymentStatus.REFUNDED;
            return true;
        }
        return false;
    }
    
    private String generateTransactionReference() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }
    
    public boolean requiresChange() {
        return paymentMethod == PaymentMethod.CASH && changeGiven > 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Payment #%d (Order #%d)\n", paymentId, orderId));
        sb.append(String.format("Method: %s | Status: %s\n", paymentMethod, status));
        sb.append(String.format("Amount: $%.2f\n", amount));
        
        if (status == PaymentStatus.COMPLETED) {
            sb.append(String.format("Amount Paid: $%.2f\n", amountPaid));
            
            if (requiresChange()) {
                sb.append(String.format("Change Given: $%.2f\n", changeGiven));
            }
            
            if (!transactionReference.isEmpty()) {
                sb.append(String.format("Transaction Ref: %s\n", transactionReference));
            }
            
            if (!cardLastFourDigits.isEmpty()) {
                sb.append(String.format("Card: ****%s\n", cardLastFourDigits));
            }
            
            sb.append(String.format("Payment Time: %s\n", paymentTime));
        }
        
        if (status == PaymentStatus.FAILED && !failureReason.isEmpty()) {
            sb.append(String.format("Failure Reason: %s\n", failureReason));
        }
        
        return sb.toString();
    }
}