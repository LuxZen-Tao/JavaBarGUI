public record MusicEffects(
        double trafficMultiplier,
        double spendMultiplier,
        double lingerMultiplier,
        double chaosDelta,
        double reputationDriftDelta,
        double staffMoraleDelta,
        double identityPressure,
        boolean lateChaosRisk,
        String summary
) {
    public static MusicEffects neutral() {
        return new MusicEffects(1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, false,
                "Balanced crowd response.");
    }
}
