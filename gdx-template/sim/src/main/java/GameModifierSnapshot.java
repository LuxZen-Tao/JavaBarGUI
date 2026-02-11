public record GameModifierSnapshot(
        double seasonTrafficMultiplier,
        double rivalTrafficMultiplier,
        double vipTrafficMultiplier,
        double vipRumorShield,
        double finalTrafficMultiplier
) {
    public static GameModifierSnapshot from(GameState s) {
        double season = FeatureFlags.FEATURE_SEASONS ? 1.0 : 1.0;
        double rival = FeatureFlags.FEATURE_RIVALS ? readDoubleField(s, "rivalDemandTrafficMultiplier", 1.0) : 1.0;
        double vip = FeatureFlags.FEATURE_VIPS ? readDoubleField(s, "vipDemandBoostMultiplier", 1.0) : 1.0;
        double rumorShield = FeatureFlags.FEATURE_VIPS ? readDoubleField(s, "vipRumorShield", 0.0) : 0.0;
        return new GameModifierSnapshot(season, rival, vip, rumorShield, season * rival * vip);
    }

    private static double readDoubleField(GameState state, String fieldName, double fallback) {
        try {
            java.lang.reflect.Field field = GameState.class.getField(fieldName);
            return field.getDouble(state);
        } catch (ReflectiveOperationException ex) {
            return fallback;
        }
    }
}
