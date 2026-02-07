public enum PubIdentity {
    NEUTRAL("neutral", 1.00, 0.00, 0.00, 0.00, 0.00, 1.00, 0.00),
    RESPECTABLE("respectable", 1.10, 0.35, 0.30, 0.10, 0.02, 0.85, 0.50),
    ROWDY("rowdy", 1.05, -0.25, -0.35, -0.12, -0.01, 1.10, -0.40),
    ARTSY("artsy", 1.03, 0.15, 0.10, 0.04, 0.01, 0.95, 0.15),
    SHADY("shady", 0.95, -0.40, -0.30, -0.18, -0.02, 1.18, -0.60),
    FAMILY_FRIENDLY("family-friendly", 1.06, 0.25, 0.35, 0.12, 0.02, 0.80, 0.55),
    UNDERGROUND("underground", 0.98, -0.10, -0.15, -0.06, -0.01, 1.05, -0.20);

    private final String descriptor;
    private final double trafficMultiplier;
    private final double wealthBias;
    private final double moodBias;
    private final double eventBias;
    private final double tipBonusPct;
    private final double rumorSpreadMultiplier;
    private final double pressToneBias;

    PubIdentity(String descriptor,
                double trafficMultiplier,
                double wealthBias,
                double moodBias,
                double eventBias,
                double tipBonusPct,
                double rumorSpreadMultiplier,
                double pressToneBias) {
        this.descriptor = descriptor;
        this.trafficMultiplier = trafficMultiplier;
        this.wealthBias = wealthBias;
        this.moodBias = moodBias;
        this.eventBias = eventBias;
        this.tipBonusPct = tipBonusPct;
        this.rumorSpreadMultiplier = rumorSpreadMultiplier;
        this.pressToneBias = pressToneBias;
    }

    public String getDescriptor() { return descriptor; }
    public double getTrafficMultiplier() { return trafficMultiplier; }
    public double getWealthBias() { return wealthBias; }
    public double getMoodBias() { return moodBias; }
    public double getEventBias() { return eventBias; }
    public double getTipBonusPct() { return tipBonusPct; }
    public double getRumorSpreadMultiplier() { return rumorSpreadMultiplier; }
    public double getPressToneBias() { return pressToneBias; }
}
