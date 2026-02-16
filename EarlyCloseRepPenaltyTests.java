import javax.swing.JTextPane;

public class EarlyCloseRepPenaltyTests {
    public static void main(String[] args) {
        testPenaltyCalculation();
        System.out.println("All EarlyCloseRepPenaltyTests passed.");
    }

    private static void testPenaltyCalculation() {
        GameState state = GameFactory.newGame();
        Simulation sim = new Simulation(state, new UILogger(new JTextPane()));

        // Test 10 rounds remaining: should be -25 (10 * 2.5)
        int penalty10 = sim.earlyClosePenaltyForRemaining(10);
        assert penalty10 == -25 : "10 rounds remaining should give -25 penalty, got " + penalty10;

        // Test 5 rounds remaining: should be -13 (5 * 2.5 = -12.5, rounded away from zero to -13)
        int penalty5 = sim.earlyClosePenaltyForRemaining(5);
        assert penalty5 == -13 : "5 rounds remaining should give -13 penalty (rounded away from zero from -12.5), got " + penalty5;

        // Test 1 round remaining: should be -3 (1 * 2.5 = -2.5, rounded away from zero to -3)
        int penalty1 = sim.earlyClosePenaltyForRemaining(1);
        assert penalty1 == -3 : "1 round remaining should give -3 penalty (rounded away from zero from -2.5), got " + penalty1;

        // Test 0 rounds remaining: should be 0
        int penalty0 = sim.earlyClosePenaltyForRemaining(0);
        assert penalty0 == 0 : "0 rounds remaining should give 0 penalty, got " + penalty0;

        // Test 20 rounds remaining: should be -50 (20 * 2.5)
        int penalty20 = sim.earlyClosePenaltyForRemaining(20);
        assert penalty20 == -50 : "20 rounds remaining should give -50 penalty, got " + penalty20;
    }
}
