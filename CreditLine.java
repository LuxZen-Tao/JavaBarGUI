public class CreditLine  implements java.io.Serializable {
    private final String id;
    private final String lenderName;
    private final double limit;
    private double balance;
    private final double interestAPR;
    private double weeklyPayment;
    private boolean isEnabled;
    private int missedPaymentCount;
    private int consecutiveMissedPayments;
    private int weeksInGoodStanding;
    private double penaltyAddOnApr;
    private int consecutiveFullPays;
    private int penaltyRecoveryStage;

    public CreditLine(String id,
                      String lenderName,
                      double limit,
                      double balance,
                      double interestAPR,
                      double weeklyPayment,
                      boolean isEnabled) {
        this.id = id;
        this.lenderName = lenderName;
        this.limit = limit;
        this.balance = balance;
        this.interestAPR = interestAPR;
        this.weeklyPayment = weeklyPayment;
        this.isEnabled = isEnabled;
    }

    public String getId() { return id; }
    public String getLenderName() { return lenderName; }
    public double getLimit() { return limit; }
    public double getBalance() { return balance; }
    public double getInterestAPR() { return interestAPR; }
    public double getWeeklyPayment() { return weeklyPayment; }
    public boolean isEnabled() { return isEnabled; }
    public int getMissedPaymentCount() { return missedPaymentCount; }
    public int getConsecutiveMissedPayments() { return consecutiveMissedPayments; }
    public int getWeeksInGoodStanding() { return weeksInGoodStanding; }
    public double getPenaltyAddOnApr() { return penaltyAddOnApr; }
    public int getConsecutiveFullPays() { return consecutiveFullPays; }
    public int getPenaltyRecoveryStage() { return penaltyRecoveryStage; }

    public double availableCredit() {
        return Math.max(0.0, limit - balance);
    }

    public void addBalance(double amount) {
        if (amount <= 0) return;
        balance += amount;
    }

    public void applyPayment(double amount) {
        if (amount <= 0) return;
        balance = Math.max(0.0, balance - amount);
    }

    public void setWeeklyPayment(double amount) {
        weeklyPayment = Math.max(0.0, amount);
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void resetMissedPayments() {
        missedPaymentCount = 0;
    }

    public void markMissedPayment() {
        missedPaymentCount++;
        consecutiveMissedPayments++;
        weeksInGoodStanding = 0;
        consecutiveFullPays = 0;
    }

    public void markPaidOnTime() {
        consecutiveMissedPayments = 0;
        weeksInGoodStanding++;
    }

    public void markFullPayment() {
        consecutiveFullPays++;
    }

    public void resetFullPayStreak() {
        consecutiveFullPays = 0;
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
