import javax.swing.JTextPane;
import java.util.Random;

public class TipsSystemTests {
    public static void main(String[] args) {
        testTipsAccumulateAcrossNights();
        testTipsResetAfterPayday();
        testTipSplitMathMultiplePercentages();
        testWagePaymentInvariant();
        testTierAEffects();
        testTierBEffects();
        testTierCEffects();
        testTierDEffects();
        testTierEEffects();
        testRepModifierOnlyInTierE();
        testTipsNotAddedToCashDirectly();
        testTipsCapAt25Percent();
        System.out.println("All TipsSystemTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test: Tips accumulate across nights and reset after payday.
     */
    private static void testTipsAccumulateAcrossNights() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Simulate tips across multiple nights
        state.nightRevenue = 100.0;
        state.reputation = 50;
        state.chaos = 10.0;
        state.nightSales = 10;
        state.nightUnserved = 0;
        
        // Night 1
        sim.calculateTipsTonight();
        double tips1 = state.tipsEarnedTonight;
        assert tips1 > 0.0 : "Tips should be generated on night 1";
        assert state.tipsPotWeek == tips1 : "Tips pot should equal first night tips";
        
        // Night 2
        state.nightRevenue = 120.0;
        sim.calculateTipsTonight();
        double tips2 = state.tipsEarnedTonight;
        assert tips2 > 0.0 : "Tips should be generated on night 2";
        assert closeTo(state.tipsPotWeek, tips1 + tips2) : "Tips pot should accumulate";
        
        System.out.println("✓ testTipsAccumulateAcrossNights passed");
    }

    /**
     * Test: Tips pot resets to zero after payday.
     */
    private static void testTipsResetAfterPayday() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 60;
        
        double cashBefore = state.cash;
        sim.applyTipSplit();
        
        assert state.tipsPotWeek == 0.0 : "Tips pot should reset to 0 after payday";
        assert state.cash > cashBefore : "Cash should increase from house portion of tips";
        
        System.out.println("✓ testTipsResetAfterPayday passed");
    }

    /**
     * Test: Tip split math is correct for multiple percentage values.
     */
    private static void testTipSplitMathMultiplePercentages() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Test 0%
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 0;
        double cashBefore = state.cash;
        sim.applyTipSplit();
        assert closeTo(state.cash - cashBefore, 100.0) : "0% split: all tips should go to house";
        
        // Test 50%
        state.cash = cashBefore;
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 50;
        sim.applyTipSplit();
        assert closeTo(state.cash - cashBefore, 50.0) : "50% split: half tips should go to house";
        
        // Test 100%
        state.cash = cashBefore;
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 100;
        sim.applyTipSplit();
        assert closeTo(state.cash - cashBefore, 0.0) : "100% split: no tips should go to house";
        
        // Test 75%
        state.cash = cashBefore;
        state.tipsPotWeek = 200.0;
        state.tipSplitPercent = 75;
        sim.applyTipSplit();
        assert closeTo(state.cash - cashBefore, 50.0) : "75% split: 25% (50 of 200) should go to house";
        
        System.out.println("✓ testTipSplitMathMultiplePercentages passed");
    }

    /**
     * Test: Wage payment invariant - tips split does not bypass wage payment.
     */
    private static void testWagePaymentInvariant() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        // Add staff with wages
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Alice", Staff.Type.EXPERIENCED, r));
        StaffSystem staffSys = new StaffSystem(state, new EconomySystem(state, new UILogger(new JTextPane())), new UpgradeSystem(state));
        
        // Accrue wages
        for (int i = 0; i < 7; i++) {
            staffSys.accrueDailyWages();
        }
        double wagesDue = staffSys.wagesDue();
        assert wagesDue > 0.0 : "Wages should be due";
        
        // Set tips pot
        state.tipsPotWeek = 500.0;
        state.tipSplitPercent = 100; // All tips to staff
        
        // Ensure applying tips doesn't pay wages
        double cashBefore = state.cash;
        sim.applyTipSplit();
        
        // Wages should still be due (tips don't pay wages)
        assert staffSys.wagesDue() == wagesDue : "Wages should still be due after tip split";
        assert state.cash == cashBefore : "Cash should not change when all tips go to staff";
        
        System.out.println("✓ testWagePaymentInvariant passed");
    }

    /**
     * Test: Tier A (0-20%) - Exploitative effects.
     */
    private static void testTierAEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Bob", Staff.Type.EXPERIENCED, r));
        state.bohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Carol", Staff.Type.CHEF, r));
        
        // Get initial morale
        int moraleBefore1 = state.fohStaff.get(0).getMorale();
        int moraleBefore2 = state.bohStaff.get(0).getMorale();
        
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 20; // Tier A boundary
        double chaosBefore = state.chaos;
        
        sim.applyTipSplit();
        
        // Check morale decreased
        assert state.fohStaff.get(0).getMorale() < moraleBefore1 : "Morale should decrease in Tier A";
        assert state.bohStaff.get(0).getMorale() < moraleBefore2 : "Morale should decrease in Tier A";
        
        // Check chaos increased
        assert state.chaos > chaosBefore : "Chaos should increase in Tier A";
        
        System.out.println("✓ testTierAEffects passed");
    }

    /**
     * Test: Tier B (>20-40%) - Bare minimum effects.
     */
    private static void testTierBEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Dave", Staff.Type.EXPERIENCED, r));
        
        int moraleBefore = state.fohStaff.get(0).getMorale();
        
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 40; // Tier B upper boundary
        double chaosBefore = state.chaos;
        
        sim.applyTipSplit();
        
        // Check mild morale penalty
        assert state.fohStaff.get(0).getMorale() < moraleBefore : "Morale should have mild decrease in Tier B";
        
        // Check small chaos increase
        assert state.chaos >= chaosBefore : "Chaos should increase slightly in Tier B";
        
        System.out.println("✓ testTierBEffects passed");
    }

    /**
     * Test: Tier C (>40-60%) - Neutral baseline (no bonuses/penalties).
     */
    private static void testTierCEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Eve", Staff.Type.EXPERIENCED, r));
        
        int moraleBefore = state.fohStaff.get(0).getMorale();
        
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 60; // Tier C upper boundary
        double chaosBefore = state.chaos;
        int repBefore = state.reputation;
        
        sim.applyTipSplit();
        
        // No morale/chaos/rep changes in Tier C
        assert state.fohStaff.get(0).getMorale() == moraleBefore : "Morale should remain unchanged in Tier C";
        assert state.chaos == chaosBefore : "Chaos should remain unchanged in Tier C";
        assert state.reputation == repBefore : "Reputation should remain unchanged in Tier C";
        
        System.out.println("✓ testTierCEffects passed");
    }

    /**
     * Test: Tier D (>60-80%) - Generous effects.
     */
    private static void testTierDEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Frank", Staff.Type.EXPERIENCED, r));
        
        int moraleBefore = state.fohStaff.get(0).getMorale();
        
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 80; // Tier D upper boundary
        
        sim.applyTipSplit();
        
        // Check morale boost
        assert state.fohStaff.get(0).getMorale() > moraleBefore : "Morale should increase in Tier D";
        
        System.out.println("✓ testTierDEffects passed");
    }

    /**
     * Test: Tier E (>80-100%) - Heroic effects.
     */
    private static void testTierEEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Grace", Staff.Type.EXPERIENCED, r));
        
        int moraleBefore = state.fohStaff.get(0).getMorale();
        
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 81; // Tier E (>80%)
        double chaosBefore = state.chaos = 10.0;
        int repBefore = state.reputation;
        double repMultBefore = state.pubLevelRepMultiplier;
        
        sim.applyTipSplit();
        
        // Check morale boost
        assert state.fohStaff.get(0).getMorale() > moraleBefore : "Morale should significantly increase in Tier E";
        
        // Check chaos reduction
        assert state.chaos < chaosBefore : "Chaos should decrease in Tier E";
        
        // Check reputation bonus
        assert state.reputation > repBefore : "Reputation should increase in Tier E";
        
        // Check permanent rep multiplier increase
        assert state.pubLevelRepMultiplier > repMultBefore : "Rep multiplier should increase in Tier E";
        
        System.out.println("✓ testTierEEffects passed");
    }

    /**
     * Test: Rep modifier (+3 and +0.005) only applied in Tier E and once per payday.
     */
    private static void testRepModifierOnlyInTierE() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Random r = new Random(42);
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Henry", Staff.Type.EXPERIENCED, r));
        
        // Test Tier D (should not get rep bonus)
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 80;
        int repBefore = state.reputation;
        double repMultBefore = state.pubLevelRepMultiplier;
        
        sim.applyTipSplit();
        
        assert state.reputation == repBefore : "Tier D should not give rep bonus";
        assert state.pubLevelRepMultiplier == repMultBefore : "Tier D should not change rep multiplier";
        
        // Test Tier E (should get rep bonus)
        state.tipsPotWeek = 100.0;
        state.tipSplitPercent = 100;
        repBefore = state.reputation;
        repMultBefore = state.pubLevelRepMultiplier;
        
        sim.applyTipSplit();
        
        assert state.reputation > repBefore : "Tier E should give rep bonus";
        assert state.pubLevelRepMultiplier > repMultBefore : "Tier E should increase rep multiplier";
        
        System.out.println("✓ testRepModifierOnlyInTierE passed");
    }

    /**
     * Test: Tips are not added to cash during service.
     */
    private static void testTipsNotAddedToCashDirectly() {
        GameState state = GameFactory.newGame();
        state.nightRevenue = 100.0;
        state.reputation = 50;
        state.chaos = 10.0;
        state.nightSales = 10;
        state.nightUnserved = 0;
        
        double cashBefore = state.cash;
        
        Simulation sim = newSimulation(state);
        sim.calculateTipsTonight();
        
        // Tips should NOT be added to cash during service
        assert state.cash == cashBefore : "Tips should not be added to cash during service";
        assert state.tipsPotWeek > 0.0 : "Tips should accumulate in tips pot";
        
        System.out.println("✓ testTipsNotAddedToCashDirectly passed");
    }

    /**
     * Test: Tips are capped at 25% of gross sales.
     */
    private static void testTipsCapAt25Percent() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Perfect conditions that would generate very high tips
        state.nightRevenue = 1000.0;
        state.reputation = 100; // Max rep
        state.chaos = 0.0; // No chaos
        state.nightSales = 100;
        state.nightUnserved = 0; // Perfect service
        
        sim.calculateTipsTonight();
        
        double maxTips = state.nightRevenue * 0.25; // 25% cap
        assert state.tipsEarnedTonight <= maxTips : "Tips should be capped at 25% of revenue";
        
        System.out.println("✓ testTipsCapAt25Percent passed");
    }

    private static boolean closeTo(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }
}
