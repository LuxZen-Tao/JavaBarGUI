public class SupplierTradeCredit  implements java.io.Serializable {
    private double balance;
    private double penaltyAddOnApr;
    private int consecutiveFullPays;
    private int penaltyRecoveryStage;
    private double lateFeesThisWeek;

    public double getBalance() { return balance; }
    public double getPenaltyAddOnApr() { return penaltyAddOnApr; }
    public int getConsecutiveFullPays() { return consecutiveFullPays; }
    public int getPenaltyRecoveryStage() { return penaltyRecoveryStage; }
    public double getLateFeesThisWeek() { return lateFeesThisWeek; }

    public void addBalance(double amount) {
        if (amount <= 0) return;
        balance += amount;
    }

    public void applyPayment(double amount) {
        if (amount <= 0) return;
        balance = Math.max(0.0, balance - amount);
    }

    public void addLateFee(double fee) {
        if (fee <= 0) return;
        balance += fee;
        lateFeesThisWeek = fee;
    }

    public void clearLateFees() {
        lateFeesThisWeek = 0.0;
    }

    public void setPenaltyAddOnApr(double penaltyAddOnApr) {
        this.penaltyAddOnApr = Math.max(0.0, penaltyAddOnApr);
    }

    public void setConsecutiveFullPays(int consecutiveFullPays) {
        this.consecutiveFullPays = Math.max(0, consecutiveFullPays);
    }

    public void setPenaltyRecoveryStage(int penaltyRecoveryStage) {
        this.penaltyRecoveryStage = Math.max(0, penaltyRecoveryStage);
    }
}
