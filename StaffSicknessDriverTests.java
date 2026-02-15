import javax.swing.JTextPane;
import java.util.Random;

public class StaffSicknessDriverTests {
    public static void main(String[] args) {
        testHighTipsReduceSickCalls();
        testSustainedChaosExposureIncreasesSickCalls();
        testStableConditionsDoNotSpamSickness();
        System.out.println("All StaffSicknessDriverTests passed.");
    }

    private static void testHighTipsReduceSickCalls() {
        int baselineCalls = runSicknessRolls(2000, 60, 58.0, 0, 48.0, 8.0, 1234L);
        int highTipCalls = runSicknessRolls(2000, 85, 58.0, 0, 48.0, 8.0, 1234L);

        assert highTipCalls < baselineCalls : "High tips should reduce sick calls.";
        assert highTipCalls <= Math.round(baselineCalls * 0.75)
                : "High tips should noticeably reduce sick calls.";

        System.out.println("✓ testHighTipsReduceSickCalls passed (baseline=" + baselineCalls
                + ", highTips=" + highTipCalls + ")");
    }

    private static void testSustainedChaosExposureIncreasesSickCalls() {
        int baselineCalls = runSicknessRolls(2000, 60, 58.0, 0, 48.0, 8.0, 2025L);
        int exposedCalls = runSicknessRolls(2000, 60, 35.0, 8, 72.0, 20.0, 2025L);

        assert exposedCalls > baselineCalls : "Sustained high chaos with low morale should increase sick calls.";
        assert exposedCalls >= Math.round(baselineCalls * 1.25)
                : "Sustained chaos exposure should noticeably increase sick calls.";

        System.out.println("✓ testSustainedChaosExposureIncreasesSickCalls passed (baseline=" + baselineCalls
                + ", exposed=" + exposedCalls + ")");
    }

    private static void testStableConditionsDoNotSpamSickness() {
        int stableCalls = runSicknessRolls(2000, 60, 80.0, 0, 20.0, 2.0, 99L);
        double stableRate = stableCalls / 2000.0;

        assert stableRate < 0.08 : "Stable conditions should avoid sickness spam.";

        System.out.println("✓ testStableConditionsDoNotSpamSickness passed (calls=" + stableCalls
                + ", rate=" + String.format("%.3f", stableRate) + ")");
    }

    private static int runSicknessRolls(int attempts,
                                        int tipSplitPercent,
                                        double teamMorale,
                                        int chaosConsecutiveHighTurns,
                                        double lastNightChaosPeak,
                                        double rollingFatigueStress,
                                        long seed) {
        GameState state = GameFactory.newGame();
        state.tipSplitPercent = tipSplitPercent;
        state.teamMorale = teamMorale;
        state.chaosConsecutiveHighTurns = chaosConsecutiveHighTurns;
        state.lastNightChaosPeak = lastNightChaosPeak;
        state.rollingFatigueStress = rollingFatigueStress;

        Simulation sim = new Simulation(state, new UILogger(new JTextPane()));
        double chance = sim.sickCallChance();

        Random rng = new Random(seed);
        int sickCalls = 0;
        for (int i = 0; i < attempts; i++) {
            if (rng.nextDouble() < chance) {
                sickCalls++;
            }
        }
        return sickCalls;
    }
}
