public class CreditLine {
    private final String id;
    private final String lenderName;
    private final int limit;
    private final int balance;
    private final double interestAPR;
    private final int weeklyPayment;
    private final boolean isEnabled;

    public CreditLine(String id,
                      String lenderName,
                      int limit,
                      int balance,
                      double interestAPR,
                      int weeklyPayment,
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
    public int getLimit() { return limit; }
    public int getBalance() { return balance; }
    public double getInterestAPR() { return interestAPR; }
    public int getWeeklyPayment() { return weeklyPayment; }
    public boolean isEnabled() { return isEnabled; }
}
