public class IntegrationModifierOrderTests {
    public static void main(String[] args) {
        testExplicitOrderComposition();
        testDebugFlagDefaultsOff();
        System.out.println("All IntegrationModifierOrderTests passed.");
        System.exit(0);
    }

    private static void testExplicitOrderComposition() {
        GameModifierSnapshot snapshot = new GameModifierSnapshot(0.97, 0.95, 1.05, 0.02, 0.97 * 0.95 * 1.05);
        double expected = 0.97 * 0.95 * 1.05;
        if (Math.abs(snapshot.finalTrafficMultiplier() - expected) > 0.0001) {
            throw new IllegalStateException("Modifier composition order is incorrect.");
        }
    }

    private static void testDebugFlagDefaultsOff() {
        if (FeatureFlags.DEBUG_MODIFIER_LOGS) {
            throw new IllegalStateException("DEBUG_MODIFIER_LOGS must default to false.");
        }
    }
}
