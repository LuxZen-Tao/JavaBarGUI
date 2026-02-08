public enum PubActivity {

    // label, cost, trafficBonusPct, capacityBonus, repInstantDelta, eventBonusChance, riskBonusPct, tipBonusPct, priceMultiplierPct,
    // requiresUnlock, requiredIdentity, requiredLevel, requiredUpgrade, identitySignal
    LIVE_BAND_NIGHT("Live Band Night", 120, 0.18, 6, +1, 10, 0.10, 0.02, 0.00,
            false, PubIdentity.ROWDY, 1, null, 1.2),
    QUIZ_NIGHT("Quiz Night", 60, 0.08, 4, +3, 2, -0.04, 0.01, 0.00,
            false, PubIdentity.RESPECTABLE, 1, null, 1.0),
    LIVE_ACOUSTIC("Live Acoustic", 70, 0.09, 4, +2, 3, 0.01, 0.02, 0.00,
            false, PubIdentity.ARTSY, 1, null, 1.2),
    DJ_NIGHT("DJ Night", 110, 0.16, 6, 0, 9, 0.12, 0.02, 0.03,
            false, PubIdentity.UNDERGROUND, 2, PubUpgrade.SOUNDPROOFING_I, 1.4),
    OPEN_MIC("Open Mic", 50, 0.08, 5, +1, 4, 0.02, 0.01, 0.00,
            true, PubIdentity.ARTSY, 1, null, 1.0),
    SPORTS_NIGHT("Sports Night", 115, 0.15, 5, 0, 8, 0.07, 0.01, 0.00,
            false, PubIdentity.ROWDY, 1, PubUpgrade.TVS, 0.9),
    FAMILY_LUNCH("Family Lunch", 65, 0.07, 3, +3, 1, -0.06, 0.01, -0.03,
            true, PubIdentity.FAMILY_FRIENDLY, 2, PubUpgrade.KITCHEN, 1.1),
    LADIES_NIGHT("Ladies Night", 90, 0.14, 5, +1, 6, 0.05, 0.02, -0.05,
            false, PubIdentity.RESPECTABLE, 1, null, 0.6),
    COCKTAIL_PROMO("Cocktail Promo", 80, 0.06, 3, 0, 3, 0.02, 0.03, 0.06,
            true, PubIdentity.ARTSY, 1, null, 0.6),
    SPORTS_SCREENING("Local Sports Screening", 110, 0.15, 5, 0, 7, 0.08, 0.01, 0.00,
            false, PubIdentity.ROWDY, 1, PubUpgrade.TVS, 0.8),
    CHARITY_NIGHT("Charity Night", 70, 0.04, 2, +4, 1, -0.05, 0.00, 0.00,
            true, PubIdentity.RESPECTABLE, 1, null, 1.0),
    KARAOKE("Karaoke", 75, 0.12, 5, -1, 8, 0.06, 0.01, 0.00,
            true, PubIdentity.ROWDY, 1, null, 0.9),
    BREWERY_TAKEOVER("Brewery Takeover", 140, 0.20, 6, +2, 12, 0.12, 0.03, 0.04,
            true, PubIdentity.RESPECTABLE, 2, PubUpgrade.EXTENDED_BAR, 1.0);

    private final String label;
    private final double cost;
    private final double trafficBonusPct;
    private final int capacityBonus;
    private final int repInstantDelta;
    private final int eventBonusChance;
    private final double riskBonusPct;
    private final double tipBonusPct;
    private final double priceMultiplierPct;
    private final boolean requiresUnlock;
    private final PubIdentity requiredIdentity;
    private final int requiredLevel;
    private final PubUpgrade requiredUpgrade;
    private final double identitySignal;

    PubActivity(String label,
                double cost,
                double trafficBonusPct,
                int capacityBonus,
                int repInstantDelta,
                int eventBonusChance,
                double riskBonusPct,
                double tipBonusPct,
                double priceMultiplierPct,
                boolean requiresUnlock,
                PubIdentity requiredIdentity,
                int requiredLevel,
                PubUpgrade requiredUpgrade,
                double identitySignal) {

        this.label = label;
        this.cost = cost;
        this.trafficBonusPct = trafficBonusPct;
        this.capacityBonus = capacityBonus;
        this.repInstantDelta = repInstantDelta;
        this.eventBonusChance = eventBonusChance;
        this.riskBonusPct = riskBonusPct;
        this.tipBonusPct = tipBonusPct;
        this.priceMultiplierPct = priceMultiplierPct;
        this.requiresUnlock = requiresUnlock;
        this.requiredIdentity = requiredIdentity;
        this.requiredLevel = requiredLevel;
        this.requiredUpgrade = requiredUpgrade;
        this.identitySignal = identitySignal;
    }

    public String getLabel() { return label; }
    public double getCost() { return cost; }
    public double getTrafficBonusPct() { return trafficBonusPct; }
    public int getCapacityBonus() { return capacityBonus; }
    public int getRepInstantDelta() { return repInstantDelta; }
    public int getEventBonusChance() { return eventBonusChance; }
    public double getRiskBonusPct() { return riskBonusPct; }
    public double getTipBonusPct() { return tipBonusPct; }
    public double getPriceMultiplierPct() { return priceMultiplierPct; }
    public boolean requiresUnlock() { return requiresUnlock; }
    public PubIdentity getRequiredIdentity() { return requiredIdentity; }
    public int getRequiredLevel() { return requiredLevel; }
    public PubUpgrade getRequiredUpgrade() { return requiredUpgrade; }
    public double getIdentitySignal() { return identitySignal; }

    @Override
    public String toString() {
        String priceTag = "";
        if (priceMultiplierPct > 0.0001) priceTag = " | price +" + (int)Math.round(priceMultiplierPct * 100) + "%";
        if (priceMultiplierPct < -0.0001) priceTag = " | price " + (int)Math.round(priceMultiplierPct * 100) + "%";

        String tipTag = tipBonusPct > 0 ? " | tips +" + (int)Math.round(tipBonusPct * 100) + "%" : "";
        String riskTag = riskBonusPct != 0 ? " | risk " + (riskBonusPct > 0 ? "+" : "")
                + (int)Math.round(riskBonusPct * 100) + "%" : "";
        String identityTag = requiredIdentity != null ? " | identity " + requiredIdentity.name() : "";
        String levelTag = requiredLevel > 0 ? " | level " + requiredLevel + "+" : "";

        return label
                + " | " + String.format("%.0f", cost)
                + " | traffic +" + (int)(trafficBonusPct * 100) + "%"
                + " | cap +" + capacityBonus
                + " | rep " + (repInstantDelta >= 0 ? "+" : "") + repInstantDelta
                + " | event +" + eventBonusChance + "%"
                + tipTag
                + priceTag
                + riskTag
                + identityTag
                + levelTag;
    }
}
