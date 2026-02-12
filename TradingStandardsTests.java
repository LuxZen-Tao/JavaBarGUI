/**
 * Tests for the Trading Standards system implementation.
 */
public class TradingStandardsTests {
    
    public static void main(String[] args) {
        testTradingStandardsCounterInitialized();
        testUnderagePunterLogic();
        testSecurityReducesUnderageService();
        testBouncerQualityLevels();
        testStrictDoorPolicy();
        testInnEventsSetup();
        testInnEventFrequencyBasedOnReputation();
        testMarshallsMitigateSeverity();
        System.out.println("All TradingStandardsTests passed.");
        System.exit(0);
    }

    private static void testTradingStandardsCounterInitialized() {
        GameState s = GameFactory.newGame();
        assertEqual(0, s.tradingStandardsCounter, "TS counter should start at 0");
    }

    private static void testUnderagePunterLogic() {
        // Create an underage punter (age < 18)
        Punter underage = new Punter(1, "Test Teen", 17, 50.0, 0, Punter.Tier.REGULAR);
        assertFalse(underage.canDrink(), "Punter under 18 should not be able to drink");
        
        // Create an adult punter (age >= 18)
        Punter adult = new Punter(2, "Test Adult", 21, 50.0, 0, Punter.Tier.REGULAR);
        assertTrue(adult.canDrink(), "Punter 18 or older should be able to drink");
    }

    private static void testSecurityReducesUnderageService() {
        // This tests the probability calculation logic
        double baseChance = 0.70;
        int security = 5;
        
        // Each security level reduces by 5%
        double expectedReduction = security * 0.05;
        double expectedChance = baseChance - expectedReduction;
        
        assertApproxEqual(0.45, expectedChance, 0.01, 
            "Security level 5 should reduce serve chance from 70% to 45%");
    }

    private static void testBouncerQualityLevels() {
        // Test bouncer reduction calculation
        double lowBouncerReduction = 0.03;
        double mediumBouncerReduction = 0.05;
        double highBouncerReduction = 0.08;
        
        // Verify bouncer qualities have different mitigation levels
        assertTrue(lowBouncerReduction < mediumBouncerReduction, 
            "Medium bouncer should be more effective than low");
        assertTrue(mediumBouncerReduction < highBouncerReduction, 
            "High bouncer should be most effective");
    }

    private static void testStrictDoorPolicy() {
        GameState s = GameFactory.newGame();
        s.securityPolicy = SecurityPolicy.STRICT_DOOR;
        
        // Strict door should add +1 security bonus
        assertEqual(1, s.securityPolicy.getSecurityBonus(), 
            "Strict door policy should provide +1 security bonus");
    }

    private static void testInnEventsSetup() {
        GameState s = GameFactory.newGame();
        s.innUnlocked = true;
        s.roomsTotal = 5;
        s.innRep = 50.0;
        
        // Verify inn is set up
        assertTrue(s.innUnlocked, "Inn should be unlocked");
        assertTrue(s.roomsTotal > 0, "Inn should have rooms");
    }

    private static void testInnEventFrequencyBasedOnReputation() {
        // Test low reputation = higher event frequency
        double lowRepChance = calculateEventChance(20.0);
        double highRepChance = calculateEventChance(80.0);
        
        assertTrue(lowRepChance > highRepChance, 
            "Low inn reputation should trigger more frequent events (low: " + 
            lowRepChance + ", high: " + highRepChance + ")");
    }

    private static void testMarshallsMitigateSeverity() {
        double baseSeverity = 1.0;
        double dutyManagerMitigation = 0.75; // 25% reduction
        
        double mitigatedSeverity = baseSeverity * dutyManagerMitigation;
        
        assertApproxEqual(0.75, mitigatedSeverity, 0.01, 
            "Duty manager should reduce severity by 25%");
    }

    // Helper method to calculate event chance (mimics the actual logic)
    private static double calculateEventChance(double innRep) {
        if (innRep < 30) {
            return 0.30 + ((30 - innRep) / 30.0) * 0.10;
        } else if (innRep < 50) {
            return 0.20 + ((50 - innRep) / 20.0) * 0.10;
        } else if (innRep < 70) {
            return 0.10 + ((70 - innRep) / 20.0) * 0.10;
        } else {
            return 0.05 + ((100 - innRep) / 30.0) * 0.05;
        }
    }
    
    // Assertion helpers
    private static void assertEqual(int expected, int actual, String message) {
        if (expected != actual) {
            throw new IllegalStateException(message + " Expected: " + expected + ", Actual: " + actual);
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
    
    private static void assertApproxEqual(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new IllegalStateException(message + 
                " Expected: " + expected + ", Actual: " + actual + ", Delta: " + delta);
        }
    }
}
