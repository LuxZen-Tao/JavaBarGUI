import java.util.UUID;

public class PaydayBill  implements java.io.Serializable {
    public enum Type {
        SUPPLIER,
        WAGES,
        RENT,
        SECURITY,
        INN_MAINTENANCE,
        CREDIT_LINE,
        LOAN_SHARK,
        OTHER
    }

    private final String id;
    private final Type type;
    private final String displayName;
    private final double minDue;
    private final double fullDue;
    private final String referenceId;
    private double selectedAmount;
    private String selectedSourceId;

    public PaydayBill(Type type, String displayName, double minDue, double fullDue, String referenceId) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.displayName = displayName;
        this.minDue = Math.max(0.0, minDue);
        this.fullDue = Math.max(this.minDue, fullDue);
        this.referenceId = referenceId;
        this.selectedAmount = this.minDue;
        this.selectedSourceId = "CASH";
    }

    public String getId() { return id; }
    public Type getType() { return type; }
    public String getDisplayName() { return displayName; }
    public double getMinDue() { return minDue; }
    public double getFullDue() { return fullDue; }
    public String getReferenceId() { return referenceId; }
    public double getSelectedAmount() { return selectedAmount; }
    public String getSelectedSourceId() { return selectedSourceId; }

    public void setSelectedAmount(double amount) {
        selectedAmount = Math.max(0.0, Math.min(fullDue, amount));
    }

    public void setSelectedSourceId(String sourceId) {
        selectedSourceId = sourceId == null ? "CASH" : sourceId;
    }

    public boolean isFullPayment(double amount) {
        return amount >= fullDue - 0.01;
    }
}
