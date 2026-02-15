public enum SupplierTrustLevel {
    VERY_POOR("Very Poor", 1.5, 1000.0),
    POOR("Poor", 1.3, 1500.0),
    NEUTRAL("Neutral", 1.1, 1800.0),
    GOOD("Good", 0.9, 2200.0),
    GREAT("Great", 0.7, 3000.0);

    private final String label;
    private final double priceMultiplier;
    private final double creditCap;

    SupplierTrustLevel(String label, double priceMultiplier, double creditCap) {
        this.label = label;
        this.priceMultiplier = priceMultiplier;
        this.creditCap = creditCap;
    }

    public String getLabel() {
        return label;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public double getCreditCap() {
        return creditCap;
    }

    /**
     * Get trust level based on credit score.
     */
    public static SupplierTrustLevel fromCreditScore(int creditScore) {
        if (creditScore >= 750) return GREAT;
        if (creditScore >= 700) return GOOD;
        if (creditScore >= 550) return NEUTRAL;
        if (creditScore >= 450) return POOR;
        return VERY_POOR;
    }

    /**
     * Get trust level from label string (for compatibility).
     */
    public static SupplierTrustLevel fromLabel(String label) {
        if (label == null) return NEUTRAL;
        for (SupplierTrustLevel level : values()) {
            if (level.label.equals(label)) {
                return level;
            }
        }
        return NEUTRAL;
    }
}
