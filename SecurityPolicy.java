public enum SecurityPolicy {
    FRIENDLY_WELCOME("Friendly Welcome", "F", -1, 1.08, 1.04,
            "Easygoing door, lively crowd."),
    BALANCED_DOOR("Balanced Door", "B", 0, 1.00, 1.00,
            "Door stays steady and calm."),
    STRICT_DOOR("Strict Door", "S", 1, 0.92, 0.97,
            "Door was tight tonight.");

    private final String label;
    private final String shortLabel;
    private final int securityBonus;
    private final double incidentChanceMultiplier;
    private final double trafficMultiplier;
    private final String observationLine;

    SecurityPolicy(String label,
                   String shortLabel,
                   int securityBonus,
                   double incidentChanceMultiplier,
                   double trafficMultiplier,
                   String observationLine) {
        this.label = label;
        this.shortLabel = shortLabel;
        this.securityBonus = securityBonus;
        this.incidentChanceMultiplier = incidentChanceMultiplier;
        this.trafficMultiplier = trafficMultiplier;
        this.observationLine = observationLine;
    }

    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public int getSecurityBonus() {
        return securityBonus;
    }

    public double getIncidentChanceMultiplier() {
        return incidentChanceMultiplier;
    }

    public double getTrafficMultiplier() {
        return trafficMultiplier;
    }

    public String getObservationLine() {
        return observationLine;
    }
}
