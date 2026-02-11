import java.util.List;
import java.util.Random;

public class RivalEffectsSimulationTests {
    public static void main(String[] args) {
        testRivalsOffBaseline();
        testDeterministicWeeklyPressureWithSeed();
        System.out.println("All RivalEffectsSimulationTests passed.");
        System.exit(0);
    }

    private static void testRivalsOffBaseline() {
        FeatureFlags.FEATURE_RIVALS = false;
        RivalSystem system = new RivalSystem();
        MarketPressure pressure = system.runWeekly(sampleRivals(), new Random(99L));

        if (pressure.totalRivals() != 0) {
            throw new IllegalStateException("Rival pressure should be empty when FEATURE_RIVALS is off.");
        }

        double demand = demandMultiplier(pressure);
        double mixBias = punterMixBias(pressure);
        double rumorBias = rumorSentimentBias(pressure);

        assertNear(demand, 1.0, "Demand should stay baseline with no pressure");
        assertNear(mixBias, 0.0, "Punter mix bias should stay baseline with no pressure");
        assertNear(rumorBias, 0.0, "Rumor bias should stay baseline with no pressure");
    }

    private static void testDeterministicWeeklyPressureWithSeed() {
        FeatureFlags.FEATURE_RIVALS = true;
        RivalSystem system = new RivalSystem();

        MarketPressure a = system.runWeekly(sampleRivals(), new Random(1234L));
        MarketPressure b = system.runWeekly(sampleRivals(), new Random(1234L));

        for (RivalStance stance : RivalStance.values()) {
            if (a.countFor(stance) != b.countFor(stance)) {
                throw new IllegalStateException("Weekly pressure must be deterministic for fixed seed.");
            }
        }

        assertNear(demandMultiplier(a), demandMultiplier(b), "Demand multiplier must be deterministic");
        assertNear(punterMixBias(a), punterMixBias(b), "Punter mix bias must be deterministic");
        assertNear(rumorSentimentBias(a), rumorSentimentBias(b), "Rumor bias must be deterministic");
    }

    private static double demandMultiplier(MarketPressure pressure) {
        int priceWar = pressure.countFor(RivalStance.PRICE_WAR);
        int eventSpam = pressure.countFor(RivalStance.EVENT_SPAM);
        int layLow = pressure.countFor(RivalStance.LAY_LOW);
        int recovery = pressure.countFor(RivalStance.CHAOS_RECOVERY);
        return clamp(1.0 - (priceWar * 0.03) - (eventSpam * 0.02) + (layLow * 0.01) + (recovery * 0.01), 0.90, 1.06);
    }

    private static double punterMixBias(MarketPressure pressure) {
        int priceWar = pressure.countFor(RivalStance.PRICE_WAR);
        int qualityPush = pressure.countFor(RivalStance.QUALITY_PUSH);
        int eventSpam = pressure.countFor(RivalStance.EVENT_SPAM);
        return clamp((qualityPush * 0.06) - (priceWar * 0.05) - (eventSpam * 0.03), -0.20, 0.20);
    }

    private static double rumorSentimentBias(MarketPressure pressure) {
        int priceWar = pressure.countFor(RivalStance.PRICE_WAR);
        int qualityPush = pressure.countFor(RivalStance.QUALITY_PUSH);
        int eventSpam = pressure.countFor(RivalStance.EVENT_SPAM);
        int layLow = pressure.countFor(RivalStance.LAY_LOW);
        return clamp((priceWar * 0.18) + (eventSpam * 0.14) - (qualityPush * 0.12) - (layLow * 0.08), -0.50, 0.60);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static List<RivalPub> sampleRivals() {
        return List.of(
                new RivalPub("The Copper Fox", 2, 1, 1, "noisy"),
                new RivalPub("Pearl Street Tap", 0, 2, 2, "upscale"),
                new RivalPub("North Lane Inn", 1, 1, 0, "mixed")
        );
    }

    private static void assertNear(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > 0.0001) {
            throw new IllegalStateException(message + " (actual=" + actual + ", expected=" + expected + ")");
        }
    }
}
