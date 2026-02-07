public class EconomySystem {

    private final GameState s;
    private final UILogger log;
    private MilestoneSystem milestones;

    public EconomySystem(GameState s, UILogger log) {
        this.s = s;
        this.log = log;
    }

    public void setMilestones(MilestoneSystem milestones) {
        this.milestones = milestones;
    }

    public void applyRep(int delta, String reason) {
        if (delta == 0) return;
        double mult = s.pubLevelRepMultiplier;
        int adjusted = (int)Math.round(delta * mult);
        if (adjusted == 0) adjusted = delta > 0 ? 1 : -1;
        s.reputation = s.clampRep(s.reputation + adjusted);
        s.weeklyRepDeltaAbs += Math.abs(adjusted);
        s.weeklyRepDeltaNet += adjusted;

        if (adjusted > 0) log.pos(reason + " | rep +" + adjusted + "  " + s.reputation);
        else log.neg(reason + " | rep " + adjusted + "  " + s.reputation);

        if (s.reputation > s.peakReputation) s.peakReputation = s.reputation;

        if (s.reputation <= -100) {
            s.reputation = -100;
            s.consecutiveNeg100Rounds++;
            log.neg(" Rep is -100 (" + s.consecutiveNeg100Rounds + "/3).");
        } else {
            s.consecutiveNeg100Rounds = 0;
        }

        if (milestones != null) milestones.onRepChanged();
    }

    public void payOrDebt(double amount, String reason) {
        payOrDebt(amount, reason, CostTag.OTHER);
    }

    public void payOrDebt(double amount, String reason, CostTag tag) {
        if (amount <= 0) return;

        if (s.cash >= amount) {
            s.cash -= amount;
            s.reportCosts += amount;
            s.weekCosts += amount;
            s.addReportCost(tag, amount);
            log.info("Paid GBP " + fmt(amount) + " - " + reason);
            return;
        }

        double shortfall = amount - s.cash;

        // cash part
        s.reportCosts += s.cash;
        s.weekCosts += s.cash;
        s.addReportCost(tag, s.cash);
        s.cash = 0;

        // debt part
        s.reportCosts += shortfall;
        s.weekCosts += shortfall;
        s.addReportCost(tag, shortfall);
        s.debt += shortfall;

        log.neg("Could not fully pay (GBP " + fmt(shortfall) + " short). Added to DEBT - " + reason);

        if (s.debt > s.maxDebt) {
            log.header(" GAME OVER");
            log.neg("Debt exceeded GBP " + fmt(s.maxDebt) + ". Shut down.");
        }
    }

    public void addCash(double amount, String reason) {
        if (amount <= 0) return;
        s.cash += amount;
        s.weekRevenue += amount;
        s.totalCashEarned += amount;
        log.pos("Cash +GBP " + fmt(amount) + " - " + reason);
    }

    public void accrueDailyRent() {
        s.rentAccruedThisWeek += (s.weeklyRent / 7.0);
    }

    public void accrueDailySecurityUpkeep(int baseSecurityLevel, double dailyRate) {
        if (baseSecurityLevel <= 0) return;
        s.securityUpkeepAccruedThisWeek += baseSecurityLevel * dailyRate;
    }

    public void endOfWeekPayBills(double wagesDue) {
        payOrDebt(s.rentAccruedThisWeek, "Rent (accrued daily)", CostTag.RENT);
        payOrDebt(s.securityUpkeepAccruedThisWeek, "Security upkeep (accrued daily)", CostTag.SECURITY);
        payOrDebt(wagesDue, "Wages", CostTag.WAGES);
        s.rentAccruedThisWeek = 0.0;
        s.securityUpkeepAccruedThisWeek = 0.0;
    }

    /** Weekly interest on debt. This is separate from LoanShark (that's its own hell). */
    public void applyWeeklyDebtInterest() {
        if (s.debt <= 0) return;

        double rate = s.weeklyDebtInterestRate;
        double interest = s.debt * rate;

        // Add interest to debt, and count as a "cost" for reporting clarity
        s.debt += interest;
        s.reportCosts += interest;
        s.weekCosts += interest;
        s.addReportCost(CostTag.INTEREST, interest);

        log.neg(" Debt interest +" + (int)Math.round(rate * 100) + "% = GBP " + fmt(interest)
                + " | debt now GBP " + fmt(s.debt));
    }

    private static String fmt(double d) { return String.format("%.2f", d); }
}
