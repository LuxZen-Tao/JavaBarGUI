public class CreditLine {
    private final String id;
    private final String lenderName;
    private final double limit;
    private double balance;
    private final double interestAPR;
    private double weeklyPayment;
    private boolean isEnabled;
    private int missedPaymentCount;

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
    }
}
