import javax.swing.JTextPane;
import java.util.Random;

public class HappyHourFairTradingTests {

    public static void main(String[] args) {
        testFairTradingThresholdMath();
        testOnlyOneFairTradingStrikePerRound();
        testFairTradingStrikeDisabledWhenHappyHourInactive();
        System.out.println("All HappyHourFairTradingTests passed.");
        System.exit(0);
    }

    private static void testFairTradingThresholdMath() {
        double base = 10.0;
        assertTrue(PunterSystem.isWithinFairTradingThreshold(7.5, base), "Lower edge (75%) should be included");
        assertTrue(PunterSystem.isWithinFairTradingThreshold(10.0, base), "Base price should be included");
        assertTrue(PunterSystem.isWithinFairTradingThreshold(12.5, base), "Upper edge (125%) should be included");

        assertFalse(PunterSystem.isWithinFairTradingThreshold(7.49, base), "Below 75% should be excluded");
        assertFalse(PunterSystem.isWithinFairTradingThreshold(12.51, base), "Above 125% should be excluded");
    }

    private static void testOnlyOneFairTradingStrikePerRound() {
        GameState s = GameFactory.newGame();
        s.happyHour = true;
        s.random = new AlwaysLowRandom(); // deterministic: strike chance always succeeds

        PunterSystem punters = buildPunterSystem(s);

        punters.maybeApplyFairTradingStrike(10.0, 10.0);
        assertEqual(1, s.tradingStandardsCounter, "First qualifying sale should add one TS strike");
        assertTrue(s.strikeRolledThisRound, "Round guard should be marked after first qualifying sale");

        // Additional qualifying sales in the same round should never add further strikes
        for (int i = 0; i < 20; i++) {
            punters.maybeApplyFairTradingStrike(10.0, 10.0);
        }
        assertEqual(1, s.tradingStandardsCounter, "Should remain at one strike for the round");

        // Simulate next round reset and verify strikes can happen again in a new round
        s.strikeRolledThisRound = false;
        punters.maybeApplyFairTradingStrike(10.0, 10.0);
        assertEqual(2, s.tradingStandardsCounter, "New round should allow another strike roll");
    }

    private static void testFairTradingStrikeDisabledWhenHappyHourInactive() {
        GameState s = GameFactory.newGame();
        s.happyHour = false;
        s.random = new AlwaysLowRandom();

        PunterSystem punters = buildPunterSystem(s);
        punters.maybeApplyFairTradingStrike(10.0, 10.0);

        assertEqual(0, s.tradingStandardsCounter, "No fair-trading strike when Happy Hour is OFF");
        assertFalse(s.strikeRolledThisRound, "Round guard should not trip when Happy Hour is OFF");
    }

    private static PunterSystem buildPunterSystem(GameState s) {
        UILogger logger = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(s, logger);
        InventorySystem inv = new InventorySystem(s);
        EventSystem events = new EventSystem(s, eco, logger);
        RumorSystem rumors = new RumorSystem(s, logger);
        return new PunterSystem(s, eco, inv, events, rumors, logger);
    }

    private static class AlwaysLowRandom extends Random {
        @Override
        public double nextDouble() {
            return 0.0;
        }
    }

    private static void assertEqual(int expected, int actual, String message) {
        if (expected != actual) {
            throw new IllegalStateException(message + " | expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalStateException(message);
        }
    }
}
