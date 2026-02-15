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

        java.util.List<UILogger.Segment> segments = new java.util.ArrayList<>();
        if (adjusted > 0) {
            segments.add(new UILogger.Segment(reason + " | rep ", UILogger.Tone.POS));
            segments.add(new UILogger.Segment("+" + adjusted, UILogger.Tone.REPUTATION));
            segments.add(new UILogger.Segment("  " + s.reputation, UILogger.Tone.REPUTATION));
        } else {
            segments.add(new UILogger.Segment(reason + " | rep ", UILogger.Tone.NEG));
            segments.add(new UILogger.Segment(String.valueOf(adjusted), UILogger.Tone.REPUTATION));
            segments.add(new UILogger.Segment("  " + s.reputation, UILogger.Tone.REPUTATION));
        }
        log.appendLogSegments(segments);

        if (s.reputation > s.peakReputation) s.peakReputation = s.reputation;
        
        // Track minimum reputation during the week for Stormproof milestone
        if (s.reputation < s.weekMinReputation) s.weekMinReputation = s.reputation;

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
            java.util.List<UILogger.Segment> segments = new java.util.ArrayList<>();
            segments.add(new UILogger.Segment("Paid ", UILogger.Tone.INFO));
            segments.add(new UILogger.Segment("GBP " + fmt(amount), UILogger.Tone.MONEY));
            segments.add(new UILogger.Segment(" - " + description, UILogger.Tone.INFO));
            log.appendLogSegments(segments);
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
                java.util.List<UILogger.Segment> segments = new java.util.ArrayList<>();
                segments.add(new UILogger.Segment("Paid ", UILogger.Tone.INFO));
                segments.add(new UILogger.Segment("GBP " + fmt(amount), UILogger.Tone.MONEY));
                segments.add(new UILogger.Segment(" (cash + credit) - " + description, UILogger.Tone.INFO));
                log.appendLogSegments(segments);
                return true;
            }
        }

        java.util.List<UILogger.Segment> segments = new java.util.ArrayList<>();
        segments.add(new UILogger.Segment("Insufficient funds: cannot pay ", UILogger.Tone.NEG));
        segments.add(new UILogger.Segment("GBP " + fmt(amount), UILogger.Tone.MONEY));
        segments.add(new UILogger.Segment(" for " + description + ".", UILogger.Tone.NEG));
        log.appendLogSegments(segments);
        return false;
    }

    /**
     * Cash-only payment method for security purchases.
     * Security purchases (base security upgrades, bouncer hires) require cash - credit not accepted.
     * @return true if payment succeeded (sufficient cash), false otherwise
     */
    public boolean tryPayCashOnly(double amount, TransactionType type, String description, CostTag tag) {
        if (amount <= 0) return true;

        if (s.cash >= amount) {
            s.cash -= amount;
            s.reportCosts += amount;
            s.weekCosts += amount;
            s.addReportCost(tag, amount);
            java.util.List<UILogger.Segment> segments = new java.util.ArrayList<>();
            segments.add(new UILogger.Segment("Paid ", UILogger.Tone.INFO));
            segments.add(new UILogger.Segment("GBP " + fmt(amount), UILogger.Tone.MONEY));
            segments.add(new UILogger.Segment(" (cash) - " + description, UILogger.Tone.INFO));
            log.appendLogSegments(segments);
            return true;
        }

        java.util.List<UILogger.Segment> segments = new java.util.ArrayList<>();
        segments.add(new UILogger.Segment("Insufficient cash: need ", UILogger.Tone.NEG));
        segments.add(new UILogger.Segment("GBP " + fmt(amount), UILogger.Tone.MONEY));
        segments.add(new UILogger.Segment(" cash for " + description + " (credit not accepted).", UILogger.Tone.NEG));
        log.appendLogSegments(segments);
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
