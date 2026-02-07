public class LoanSharkAccount {

    private boolean active = false;
    private double principalOwed = 0.0;

    private int borrowedAbsWeek = 0;
    private int borrowedReportIndex = 0;
    private int borrowedWeekInReport = 0;

    // punishment mechanics
    private int cheapPayStreak = 0;
    private int protectionReportsRemaining = 0;

    public boolean hasActiveLoan() { return active; }
    public double getPrincipalOwed() { return principalOwed; }
    public int getProtectionReportsRemaining() { return protectionReportsRemaining; }

    public double borrowLimit(int reputation) {
        // rep increases limit, bad rep lowers it slightly
        double base = 800;
        base += Math.max(0, reputation) * 20;
        base -= Math.max(0, -reputation) * 5;
        return Math.max(300, Math.min(8000, base));
    }

    public boolean canBorrow(double amt, int reputation) {
        if (active) return false;
        if (amt <= 0) return false;
        return amt <= borrowLimit(reputation);
    }

    public void borrow(double amt, int absWeek, int reportIndex, int weekInReport, int reputation) {
        active = true;
        principalOwed = amt;
        borrowedAbsWeek = absWeek;
        borrowedReportIndex = reportIndex;
        borrowedWeekInReport = weekInReport;
    }

    public double totalDueNow(int absWeek, int reportIndex, int weekInReport) {
        if (!active) return 0;

        // time since borrow in "report weeks"
        int weeksPassed = ((reportIndex - borrowedReportIndex) * 4) + (weekInReport - borrowedWeekInReport);
        weeksPassed = Math.max(0, weeksPassed);

        double rate = interestRate(weeksPassed);
        return principalOwed * (1.0 + rate);
    }

    private double interestRate(int weeksPassed) {
        if (weeksPassed <= 1) return 0.05;
        if (weeksPassed <= 3) return 0.10;
        return 0.20;
    }

    public double payInFull(int absWeek, int reportIndex, int weekInReport) {
        if (!active) return 0;
        int weeksPassed = ((reportIndex - borrowedReportIndex) * 4) + (weekInReport - borrowedWeekInReport);
        weeksPassed = Math.max(0, weeksPassed);

        double rate = interestRate(weeksPassed);

        // cheap pay streak (5% band)
        if (rate <= 0.05) cheapPayStreak++;
        else cheapPayStreak = 0;

        active = false;
        principalOwed = 0;
        return rate;
    }

    public boolean annoyedByLowInterest() {
        return cheapPayStreak >= 5;
    }

    public void resetAnnoyanceStreak() { cheapPayStreak = 0; }

    public void startProtection(int reports) { protectionReportsRemaining = Math.max(0, reports); }

    public void tickReportProtection() {
        if (protectionReportsRemaining > 0) protectionReportsRemaining--;
    }

    public double protectionFee(double reportRevenue) {
        // 5% of report revenue, minimum 10
        return Math.max(10, reportRevenue * 0.05);
    }

    public String buildLoanText(int absWeek, int reportIndex, int weekInReport, int reputation) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LOAN SHARK ===\n");
        sb.append("Borrow limit: ").append(String.format("%.0f", borrowLimit(reputation))).append("\n");
        sb.append("Protection reports remaining: ").append(protectionReportsRemaining).append("\n\n");

        if (!active) {
            sb.append("No active loan.\n");
            return sb.toString();
        }

        sb.append("Principal owed: ").append(String.format("%.2f", principalOwed)).append("\n");
        sb.append("Total due now: ").append(String.format("%.2f", totalDueNow(absWeek, reportIndex, weekInReport))).append("\n");
        sb.append("Must repay in full.\n");
        return sb.toString();
    }
}
