public final class RivalPub {
    private final String name;
    private final int priceAggression; // 0..2
    private final int qualityFocus;    // 0..2
    private final int chaosTolerance;  // 0..2
    private final String vibeTag;

    public RivalPub(String name, int priceAggression, int qualityFocus, int chaosTolerance, String vibeTag) {
        this.name = name;
        this.priceAggression = clampTrait(priceAggression);
        this.qualityFocus = clampTrait(qualityFocus);
        this.chaosTolerance = clampTrait(chaosTolerance);
        this.vibeTag = vibeTag == null ? "" : vibeTag;
    }

    public String getName() {
        return name;
    }

    public int getPriceAggression() {
        return priceAggression;
    }

    public int getQualityFocus() {
        return qualityFocus;
    }

    public int getChaosTolerance() {
        return chaosTolerance;
    }

    public String getVibeTag() {
        return vibeTag;
    }

    private static int clampTrait(int value) {
        return Math.max(0, Math.min(2, value));
    }
}
