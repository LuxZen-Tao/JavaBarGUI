public class LoanSharkAccount  implements java.io.Serializable {
    private boolean open;
    private double balance;
    private double apr;
    private double penaltyAddOnApr;
    private int consecutiveFullPays;
    private int penaltyRecoveryStage;
    private int missedPaymentCount;

    public boolean isOpen() { return open; }
    public double getBalance() { return balance; }
    public double getApr() { return apr; }
    public double getPenaltyAddOnApr() { return penaltyAddOnApr; }
    public int getConsecutiveFullPays() { return consecutiveFullPays; }
    public int getPenaltyRecoveryStage() { return penaltyRecoveryStage; }
    public int getMissedPaymentCount() { return missedPaymentCount; }

    public void openLoan(double amount, double apr) {
        if (amount <= 0) return;
        open = true;
        balance += amount;
        this.apr = Math.max(this.apr, apr);
    }

    public void applyPayment(double amount) {
        if (amount <= 0) return;
        balance = Math.max(0.0, balance - amount);
    }

    public double weeklyInterestDue() {
        if (balance <= 0) return 0.0;
        return balance * ((apr + penaltyAddOnApr) / 52.0);
    }

    public double minPaymentDue() {
        if (balance <= 0) return 0.0;
        return Math.max(60.0, balance * 0.08);
    }

    public void applyInterest(double amount) {
        if (amount <= 0) return;
        balance += amount;
    }

    public void markMissedPayment() {
        missedPaymentCount++;
        consecutiveFullPays = 0;
    }

    public void markFullPayment() {
        consecutiveFullPays++;
    }

    public void resetRecoveryStreak() {
        consecutiveFullPays = 0;
    }


    public void setApr(double apr) {
        this.apr = Math.max(0.0, apr);
    }

    public void setPenaltyAddOnApr(double penaltyAddOnApr) {
        this.penaltyAddOnApr = Math.max(0.0, penaltyAddOnApr);
    }

    public void setPenaltyRecoveryStage(int penaltyRecoveryStage) {
        this.penaltyRecoveryStage = Math.max(0, penaltyRecoveryStage);
    }

    public void setConsecutiveFullPays(int consecutiveFullPays) {
        this.consecutiveFullPays = Math.max(0, consecutiveFullPays);
    }
}
