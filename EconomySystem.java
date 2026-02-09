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

    public boolean tryPay(double amount, TransactionType type, String description) {
        return tryPay(amount, type, description, costTagFor(type));
    }

    public boolean tryPay(double amount, TransactionType type, String description, CostTag tag) {
        if (amount <= 0) return true;

        if (s.cash >= amount) {
            s.cash -= amount;
            s.reportCosts += amount;
            s.weekCosts += amount;
            s.addReportCost(tag, amount);
            log.info("Paid GBP " + fmt(amount) + " - " + description);
            return true;
        }

        double remaining = amount - s.cash;
        if (s.creditLines.hasAvailableCredit(remaining) && s.creditLines.applyCredit(remaining)) {
            s.reportCosts += amount;
            s.weekCosts += amount;
            s.addReportCost(tag, amount);
            s.cash = 0;
            CreditLine line = s.creditLines.consumeLastAppliedLine();
            if (line != null && "Loan Shark".equals(line.getLenderName())) {
                s.creditScore = s.clampCreditScore(s.creditScore - 10);
            }
            log.info("Paid GBP " + fmt(amount) + " (cash + credit) - " + description);
            return true;
        }

        log.neg("Insufficient funds: cannot pay GBP " + fmt(amount) + " for " + description + ".");
        return false;
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

    public boolean endOfWeekPayBills(double wagesDue) {
        double invoiceMult = s.supplierInvoiceMultiplier();
        double rentDue = s.rentAccruedThisWeek * invoiceMult;
        double securityDue = s.securityUpkeepAccruedThisWeek * invoiceMult;
        double wagesDueAdjusted = wagesDue;

        boolean wagesPaid = wagesDueAdjusted <= 0 || tryPay(wagesDueAdjusted, TransactionType.WAGES, "Staff wages", CostTag.WAGES);
        if (tryPay(rentDue, TransactionType.OTHER, "Rent (accrued daily)", CostTag.RENT)) {
            s.rentAccruedThisWeek = 0.0;
        }
        if (tryPay(securityDue, TransactionType.OTHER, "Security upkeep (accrued daily)", CostTag.SECURITY)) {
            s.securityUpkeepAccruedThisWeek = 0.0;
        }
        return wagesPaid;
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

    private CostTag costTagFor(TransactionType type) {
        return switch (type) {
            case UPGRADE -> CostTag.UPGRADE;
            case SUPPLIER_INVOICE -> CostTag.SUPPLIER;
            case WAGES -> CostTag.WAGES;
            case ACTIVITY -> CostTag.ACTIVITY;
            case REPAIR -> CostTag.EVENT;
            case RESTOCK -> CostTag.SUPPLIER;
            case OTHER -> CostTag.OTHER;
        };
    }
}
