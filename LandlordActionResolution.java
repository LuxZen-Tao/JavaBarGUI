public record LandlordActionResolution(
        LandlordActionDef def,
        boolean success,
        boolean blocked,
        String message,
        int repDelta,
        int moraleDelta,
        double trafficBonusPct,
        int trafficRounds,
        double chaosDelta
) {
    public static LandlordActionResolution blocked(LandlordActionDef def, String message) {
        return new LandlordActionResolution(def, false, true, message, 0, 0, 0.0, 0, 0.0);
    }
}
