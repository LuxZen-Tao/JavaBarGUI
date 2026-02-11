import java.util.UUID;

public class SupplierInvoice  implements java.io.Serializable {
    public enum Status { OPEN, DUE, OVERDUE, PAID }

    private final String invoiceId;
    private final String supplierName;
    private final double totalAmount;
    private double amountDue;
    private int dueInWeeks;
    private int weeksOverdue;
    private Status status;
    private final int createdAtWeek;

    public SupplierInvoice(String supplierName, double amount, int dueInWeeks, int createdAtWeek) {
        this.invoiceId = UUID.randomUUID().toString().substring(0, 8);
        this.supplierName = supplierName;
        this.totalAmount = amount;
        this.amountDue = amount;
        this.dueInWeeks = Math.max(0, dueInWeeks);
        this.weeksOverdue = 0;
        this.status = Status.OPEN;
        this.createdAtWeek = createdAtWeek;
    }

    public String getInvoiceId() { return invoiceId; }
    public String getSupplierName() { return supplierName; }
    public double getTotalAmount() { return totalAmount; }
    public double getAmountDue() { return amountDue; }
    public int getDueInWeeks() { return dueInWeeks; }
    public int getWeeksOverdue() { return weeksOverdue; }
    public Status getStatus() { return status; }
    public int getCreatedAtWeek() { return createdAtWeek; }

    public boolean isPaid() { return status == Status.PAID; }

    public void markPaid() {
        amountDue = 0.0;
        status = Status.PAID;
    }

    public void applyLateFee(double fee) {
        if (fee <= 0) return;
        amountDue += fee;
    }

    public void advanceWeek() {
        if (status == Status.PAID) return;
        if (dueInWeeks > 0) {
            dueInWeeks--;
            if (dueInWeeks == 0) {
                status = Status.DUE;
            }
            return;
        }
        if (status == Status.DUE || status == Status.OVERDUE) {
            status = Status.OVERDUE;
            weeksOverdue++;
        }
    }
}
