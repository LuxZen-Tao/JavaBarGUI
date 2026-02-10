import javax.swing.JTextPane;

public class TruthPassTests {
    public static void main(String[] args) {
        testEarlyClosePenaltyFormula();
        testChaosStreakAmplification();
        testChaosClamp();
        testSecurityHudBadgeShowsChaos();
        testMissionControlContainsFormulaAndStreak();
        System.out.println("All TruthPassTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testEarlyClosePenaltyFormula() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        assert sim.earlyClosePenaltyForRemaining(20) == -40 : "R=20 should yield -40 rep.";
        assert sim.earlyClosePenaltyForRemaining(5) == -10 : "R=5 should yield -10 rep.";
    }

    private static void testChaosStreakAmplification() {
        GameState badState = GameFactory.newGame();
        Simulation badSim = newSimulation(badState);
        badState.chaos = 0.0;
        badSim.applyChaosClassificationForTest(true);
        double badFirst = badState.lastChaosDelta;
        badSim.applyChaosClassificationForTest(true);
        double badSecond = badState.lastChaosDelta;
        assert badSecond > badFirst : "Second bad-round chaos delta should be larger.";

        GameState goodState = GameFactory.newGame();
        Simulation goodSim = newSimulation(goodState);
        goodState.chaos = 50.0;
        goodSim.applyChaosClassificationForTest(false);
        double goodFirst = goodState.lastChaosDelta;
        goodSim.applyChaosClassificationForTest(false);
        goodSim.applyChaosClassificationForTest(false);
        double goodThird = goodState.lastChaosDelta;
        assert goodThird < goodFirst : "Third good-round chaos delta should be more negative.";
    }

    private static void testChaosClamp() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.chaos = 99.0;
        sim.applyChaosClassificationForTest(true);
        sim.applyChaosClassificationForTest(true);
        assert state.chaos <= 100.0 : "Chaos should clamp at max.";

        state.chaos = 1.0;
        sim.applyChaosClassificationForTest(false);
        sim.applyChaosClassificationForTest(false);
        sim.applyChaosClassificationForTest(false);
        assert state.chaos >= 0.0 : "Chaos should clamp at min.";
    }

    private static void testSecurityHudBadgeShowsChaos() {
        String badge = WineBarGUI.buildSecurityBadgeText(8, "B", "Patrol", "Bouncers: 1/2", "Rep x0.80", 37.5);
        assert badge.contains("Chaos") : "Security badge should include chaos label.";
        assert badge.contains("37.5") : "Security badge should include current chaos value.";
    }

    private static void testMissionControlContainsFormulaAndStreak() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.lastEarlyCloseRepPenalty = -10;
        state.lastEarlyCloseRoundsRemaining = 5;
        state.posStreak = 2;
        state.negStreak = 1;
        MetricsSnapshot snapshot = sim.buildMetricsSnapshot();
        assert snapshot.security.contains("repPenalty = -2 * roundsRemaining") : "Mission Control should show early-close formula.";
        assert snapshot.security.contains("Chaos streaks") : "Mission Control should show chaos streak info.";
    }
}
