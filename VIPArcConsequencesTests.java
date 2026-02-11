import java.util.List;
import java.util.Random;

public class VIPArcConsequencesTests {
    public static void main(String[] args) {
        testAdvocatePathTriggersOnce();
        testBacklashPathTriggersOnce();
        System.out.println("All VIPArcConsequencesTests passed.");
        System.exit(0);
    }

    private static void testAdvocatePathTriggersOnce() {
        FeatureFlags.FEATURE_VIPS = true;
        VIPSystem system = new VIPSystem();
        system.ensureRosterFromNames(List.of("Alex", "Morgan", "Casey"), new Random(7L));
        VIPRegular vip = system.roster().get(0);

        int loops = 0;
        int triggerCount = 0;
        while (loops++ < 30 && vip.getArcStage() != VIPArcStage.ADVOCATE) {
            List<VIPSystem.VIPConsequence> cons = system.evaluateNightWithConsequences(
                    new VIPNightOutcome(0, 0, 2, 0, 0.95, 0.9));
            triggerCount += countStageForVip(cons, VIPArcStage.ADVOCATE, vip.getName());
        }
        if (vip.getArcStage() != VIPArcStage.ADVOCATE) {
            throw new IllegalStateException("VIP failed to reach ADVOCATE in deterministic positive path.");
        }
        if (triggerCount != 1) {
            throw new IllegalStateException("ADVOCATE consequence must trigger exactly once.");
        }

        List<VIPSystem.VIPConsequence> after = system.evaluateNightWithConsequences(
                new VIPNightOutcome(0, 0, 1, 0, 1.0, 0.8));
        if (countStageForVip(after, VIPArcStage.ADVOCATE, vip.getName()) != 0) {
            throw new IllegalStateException("ADVOCATE should not re-trigger after first trigger.");
        }
    }

    private static void testBacklashPathTriggersOnce() {
        FeatureFlags.FEATURE_VIPS = true;
        VIPSystem system = new VIPSystem();
        system.ensureRosterFromNames(List.of("Jordan", "Taylor", "Sam"), new Random(9L));
        VIPRegular vip = system.roster().get(0);

        int loops = 0;
        int triggerCount = 0;
        while (loops++ < 30 && vip.getArcStage() != VIPArcStage.BACKLASH) {
            List<VIPSystem.VIPConsequence> cons = system.evaluateNightWithConsequences(
                    new VIPNightOutcome(10, 3, 0, 5, 1.40, 0.1));
            triggerCount += countStageForVip(cons, VIPArcStage.BACKLASH, vip.getName());
        }
        if (vip.getArcStage() != VIPArcStage.BACKLASH) {
            throw new IllegalStateException("VIP failed to reach BACKLASH in deterministic negative path.");
        }
        if (triggerCount != 1) {
            throw new IllegalStateException("BACKLASH consequence must trigger exactly once.");
        }

        List<VIPSystem.VIPConsequence> after = system.evaluateNightWithConsequences(
                new VIPNightOutcome(8, 2, 0, 4, 1.35, 0.2));
        if (countStageForVip(after, VIPArcStage.BACKLASH, vip.getName()) != 0) {
            throw new IllegalStateException("BACKLASH should not re-trigger after first trigger.");
        }
    }

    private static int countStageForVip(List<VIPSystem.VIPConsequence> list, VIPArcStage stage, String vipName) {
        int count = 0;
        for (VIPSystem.VIPConsequence c : list) {
            if (c.stage() == stage && c.vip().getName().equals(vipName)) count++;
        }
        return count;
    }
}
