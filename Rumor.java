public enum Rumor {

    WATERED_DOWN_DRINKS(
            "Watered-down drinks",
            -0.06,   // traffic
            -0.10,   // wealth bias
            -0.02,   // event bias
            -0.04    // rep drift
    ),
    FIGHTS_EVERY_WEEKEND(
            "Fights every weekend",
            -0.10,
            -0.06,
            -0.08,
            -0.06
    ),
    BEST_SUNDAY_ROAST(
            "Best Sunday roast",
            +0.08,
            +0.06,
            +0.03,
            +0.05
    ),
    FOOD_POISONING_SCARE(
            "Food poisoning scare",
            -0.09,
            -0.05,
            -0.06,
            -0.07
    ),
    LIVE_MUSIC_SCENE(
            "Live music scene",
            +0.05,
            +0.02,
            +0.05,
            +0.03
    ),
    DODGY_LATE_NIGHTS(
            "Dodgy late nights",
            -0.05,
            -0.03,
            -0.04,
            -0.04
    ),
    STAFF_STEALING(
            "Staff stealing",
            -0.07,
            -0.04,
            -0.03,
            -0.08
    ),
    SLOW_SERVICE(
            "Slow service",
            -0.06,
            -0.04,
            -0.02,
            -0.04
    ),
    FRIENDLY_STAFF(
            "Friendly staff",
            +0.06,
            +0.03,
            +0.02,
            +0.05
    ),
    GREAT_ATMOSPHERE(
            "Great atmosphere",
            +0.07,
            +0.04,
            +0.03,
            +0.04
    );

    private final String label;
    private final double trafficImpactAt1;
    private final double wealthImpactAt1;
    private final double eventImpactAt1;
    private final double repImpactAt1;

    Rumor(String label, double trafficImpactAt1, double wealthImpactAt1, double eventImpactAt1, double repImpactAt1) {
        this.label = label;
        this.trafficImpactAt1 = trafficImpactAt1;
        this.wealthImpactAt1 = wealthImpactAt1;
        this.eventImpactAt1 = eventImpactAt1;
        this.repImpactAt1 = repImpactAt1;
    }

    public String getLabel() {
        return label;
    }

    /** normalized in [0,1]; return additive multiplier delta (e.g. +0.05 means +5%) */
    public double trafficImpact(double normalized) {
        return trafficImpactAt1 * clamp01(normalized);
    }

    /** normalized in [0,1]; return additive bias in roughly [-1,1] */
    public double wealthImpact(double normalized) {
        return wealthImpactAt1 * clamp01(normalized);
    }

    public double eventImpact(double normalized) {
        return eventImpactAt1 * clamp01(normalized);
    }

    public double repImpact(double normalized) {
        return repImpactAt1 * clamp01(normalized);
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
