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
        double repBias = delta < 0 ? s.debtSpiralNegativeRepMultiplier : s.debtSpiralPositiveRepMultiplier;
        double mult = s.pubLevelRepMultiplier * repBias;
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
        if (s.creditLines.hasAvailableCredit(remaining)) {
            CreditLine selectedLine = selectCreditLineForShortfall(remaining, description);
            if (selectedLine == null) {
                log.neg("Payment cancelled: select a credit line to cover GBP " + fmt(remaining) + ".");
                return false;
            }
            if (selectedLine.isEnabled() && selectedLine.availableCredit() >= remaining) {
                s.creditLines.addBalanceToLine(selectedLine, remaining);
                s.reportCosts += amount;
                s.weekCosts += amount;
                s.addReportCost(tag, amount);
                s.cash = 0;
                if ("Loan Shark".equals(selectedLine.getLenderName())) {
                    s.creditScore = s.clampCreditScore(s.creditScore - 10);
                }
                log.info("Paid GBP " + fmt(amount) + " (cash + credit) - " + description);
                return true;
            }
        }

        log.neg("Insufficient funds: cannot pay GBP " + fmt(amount) + " for " + description + ".");
        return false;
    }

    private CreditLine selectCreditLineForShortfall(double shortfall, String description) {
        java.util.List<CreditLine> options = new java.util.ArrayList<>();
        for (CreditLine line : s.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            if (line.availableCredit() >= shortfall) {
                options.add(line);
            }
        }
        if (options.isEmpty()) return null;
        if (options.size() == 1) return options.get(0);
        if (s.creditLineSelector != null) {
            CreditLine selected = s.creditLineSelector.select(options, shortfall, description);
            if (selected != null) return selected;
        }
        CreditLine best = options.get(0);
        for (CreditLine line : options) {
            if (line.availableCredit() > best.availableCredit()) best = line;
        }
        return best;
    }

    public void addCash(double amount, String reason) {
        if (amount <= 0) return;
        s.cash += amount;
        s.weekRevenue += amount;
        s.totalCashEarned += amount;
        log.pos("Cash +GBP " + fmt(amount) + " - " + reason);
    }

    public void accrueDailyRent() {
        s.rentAccruedThisWeek += s.dailyRent();
    }

    public void accrueDailySecurityUpkeep(int baseSecurityLevel, double dailyRate) {
        if (baseSecurityLevel <= 0) return;
        s.securityUpkeepAccruedThisWeek += baseSecurityLevel * dailyRate;
    }

    public void recordCostOnly(double amount, CostTag tag, String description) {
        if (amount <= 0) return;
        s.reportCosts += amount;
        s.weekCosts += amount;
        s.addReportCost(tag, amount);
        log.info("Recorded GBP " + fmt(amount) + " - " + description);
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
