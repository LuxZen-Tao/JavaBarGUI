import java.util.List;
import java.util.Random;

public class VIPSystemTests {
    public static void main(String[] args) {
        testLoyaltyIncreaseAndDecrease();
        testFeatureGateOff();
        System.out.println("All VIPSystemTests passed.");
        System.exit(0);
    }

    private static void testLoyaltyIncreaseAndDecrease() {
        FeatureFlags.FEATURE_VIPS = true;
        VIPSystem vipSystem = new VIPSystem();
        vipSystem.ensureRosterFromNames(List.of("Alex", "Morgan", "Casey", "Jordan"), new Random(77L));
        if (vipSystem.roster().isEmpty()) throw new IllegalStateException("VIP roster should be populated when feature is on.");

        VIPRegular vip = vipSystem.roster().get(0);
        int start = vip.getLoyalty();

        vipSystem.evaluateNight(new VIPNightOutcome(0, 0, 2, 0, 1.00, 0.8));
        int afterGood = vip.getLoyalty();
        if (afterGood <= start) throw new IllegalStateException("Expected loyalty to increase after favorable night.");

        vipSystem.evaluateNight(new VIPNightOutcome(8, 2, 0, 4, 1.35, 0.2));
        int afterBad = vip.getLoyalty();
        if (afterBad >= afterGood) throw new IllegalStateException("Expected loyalty to decrease after poor night.");
    }

    private static void testFeatureGateOff() {
        FeatureFlags.FEATURE_VIPS = false;
        VIPSystem vipSystem = new VIPSystem();
        vipSystem.ensureRosterFromNames(List.of("Alex", "Morgan", "Casey"), new Random(11L));
        if (!vipSystem.roster().isEmpty()) {
            throw new IllegalStateException("VIP roster should remain empty when feature flag is off.");
        }
    }
}
