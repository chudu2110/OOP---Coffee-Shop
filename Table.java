import java.time.LocalDateTime;

/**
 * Table class representing dining tables in the coffee shop
 * Manages table status, capacity, and reservations
 */
public class Table {
    public enum TableStatus {
        AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE
    }
    
    private int tableNumber;
    private int capacity;
    private TableStatus status;
    private int currentCustomerId;
    private LocalDateTime occupiedSince;
    private LocalDateTime reservedUntil;
    private String notes;
    
    // Constructor
    public Table(int tableNumber, int capacity) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("Table number must be positive");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be positive");
        }
        
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
        this.currentCustomerId = -1;
        this.notes = "";
    }
    
    // Getters
    public int getTableNumber() {
        return tableNumber;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public TableStatus getStatus() {
        return status;
    }
    
    public int getCurrentCustomerId() {
        return currentCustomerId;
    }
    
    public LocalDateTime getOccupiedSince() {
        return occupiedSince;
    }
    
    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }
    
    public String getNotes() {
        return notes;
    }
    
    // Setters
    public void setCapacity(int capacity) {
        if (capacity > 0) {
            this.capacity = capacity;
        }
    }
    
    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }
    
    // Methods
    public boolean isAvailable() {
        // Check if reservation has expired
        if (status == TableStatus.RESERVED && reservedUntil != null) {
            if (LocalDateTime.now().isAfter(reservedUntil)) {
                makeAvailable();
            }
        }
        return status == TableStatus.AVAILABLE;
    }
    
    public boolean occupyTable(int customerId) {
        if (isAvailable()) {
            this.status = TableStatus.OCCUPIED;
            this.currentCustomerId = customerId;
            this.occupiedSince = LocalDateTime.now();
            this.reservedUntil = null;
            return true;
        }
        return false;
    }
    
    public boolean reserveTable(LocalDateTime until) {
        if (isAvailable() && until != null && until.isAfter(LocalDateTime.now())) {
            this.status = TableStatus.RESERVED;
            this.reservedUntil = until;
            this.currentCustomerId = -1;
            this.occupiedSince = null;
            return true;
        }
        return false;
    }
    
    public void makeAvailable() {
        this.status = TableStatus.AVAILABLE;
        this.currentCustomerId = -1;
        this.occupiedSince = null;
        this.reservedUntil = null;
    }
    
    public void setOutOfService(String reason) {
        this.status = TableStatus.OUT_OF_SERVICE;
        this.currentCustomerId = -1;
        this.occupiedSince = null;
        this.reservedUntil = null;
        this.notes = reason != null ? reason : "Out of service";
    }
    
    public void putBackInService() {
        if (status == TableStatus.OUT_OF_SERVICE) {
            makeAvailable();
            this.notes = "";
        }
    }
    
    public long getOccupiedDurationMinutes() {
        if (status == TableStatus.OCCUPIED && occupiedSince != null) {
            return java.time.Duration.between(occupiedSince, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }
    
    public boolean isReservationExpired() {
        if (status == TableStatus.RESERVED && reservedUntil != null) {
            return LocalDateTime.now().isAfter(reservedUntil);
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Table %d (Capacity: %d) - Status: %s", 
                tableNumber, capacity, status));
        
        switch (status) {
            case OCCUPIED:
                sb.append(String.format("\nCustomer ID: %d", currentCustomerId));
                sb.append(String.format("\nOccupied since: %s", occupiedSince));
                sb.append(String.format("\nDuration: %d minutes", getOccupiedDurationMinutes()));
                break;
            case RESERVED:
                sb.append(String.format("\nReserved until: %s", reservedUntil));
                if (isReservationExpired()) {
                    sb.append(" (EXPIRED)");
                }
                break;
            case OUT_OF_SERVICE:
                if (!notes.isEmpty()) {
                    sb.append(String.format("\nReason: %s", notes));
                }
                break;
        }
        
        if (!notes.isEmpty() && status != TableStatus.OUT_OF_SERVICE) {
            sb.append(String.format("\nNotes: %s", notes));
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Table table = (Table) obj;
        return tableNumber == table.tableNumber;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(tableNumber);
    }
}