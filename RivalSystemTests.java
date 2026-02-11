import java.util.List;
import java.util.Random;

public class RivalSystemTests {
    public static void main(String[] args) {
        testFeatureGateOff();
        testDeterministicWithSeed();
        testTraitBiasWithFixedSeed();
        FeatureFlags.FEATURE_RIVALS = false;
        System.out.println("All RivalSystemTests passed.");
        System.exit(0);
    }

    private static void testFeatureGateOff() {
        FeatureFlags.FEATURE_RIVALS = false;
        RivalSystem system = new RivalSystem();
        MarketPressure pressure = system.runWeekly(sampleRivals(), new Random(7));

        if (pressure.totalRivals() != 0) throw new IllegalStateException("FEATURE_RIVALS OFF should return empty pressure.");
        for (RivalStance stance : RivalStance.values()) {
            if (pressure.countFor(stance) != 0) {
                throw new IllegalStateException("Expected zero count for " + stance + " when gate is off.");
            }
        }
    }

    private static void testDeterministicWithSeed() {
        FeatureFlags.FEATURE_RIVALS = true;
        RivalSystem system = new RivalSystem();
        List<RivalPub> rivals = sampleRivals();

        MarketPressure first = system.runWeekly(rivals, new Random(123456L));
        MarketPressure second = system.runWeekly(rivals, new Random(123456L));

        for (RivalStance stance : RivalStance.values()) {
            if (first.countFor(stance) != second.countFor(stance)) {
                throw new IllegalStateException("Determinism failed for stance " + stance + ".");
            }
        }
    }

    private static void testTraitBiasWithFixedSeed() {
        FeatureFlags.FEATURE_RIVALS = true;
        RivalSystem system = new RivalSystem();

        RivalPub priceAggro = new RivalPub("Cutthroat Arms", 2, 0, 1, "gritty");
        RivalPub qualityAggro = new RivalPub("Silk & Oak", 0, 2, 1, "polished");

        int priceWarCount = 0;
        int qualityPushCount = 0;
        Random seeded = new Random(42L);
        for (int i = 0; i < 250; i++) {
            if (system.pickStance(priceAggro, seeded) == RivalStance.PRICE_WAR) priceWarCount++;
            if (system.pickStance(qualityAggro, seeded) == RivalStance.QUALITY_PUSH) qualityPushCount++;
        }

        if (priceWarCount < 70) {
            throw new IllegalStateException("Expected strong PRICE_WAR tendency for high price aggression rival.");
        }
        if (qualityPushCount < 70) {
            throw new IllegalStateException("Expected strong QUALITY_PUSH tendency for high quality focus rival.");
        }
    }

    private static List<RivalPub> sampleRivals() {
        return List.of(
                new RivalPub("The Copper Fox", 2, 1, 1, "noisy"),
                new RivalPub("Pearl Street Tap", 0, 2, 2, "upscale"),
                new RivalPub("North Lane Inn", 1, 1, 0, "mixed")
        );
    }
}
