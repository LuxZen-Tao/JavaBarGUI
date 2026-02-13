import javax.swing.JTextPane;

/**
 * Tests for M18_STORMPROOF_OPERATOR milestone criteria.
 * Validates that the milestone only triggers at week-end when:
 * - Week profit > 0 (weekRevenue - weekCosts > 0)
 * - Reputation never dipped below 0 during the week (weekMinReputation >= 0)
 * - Week had at least 3 negative events (weekNegativeEvents >= 3)
 */
public class StormproofMilestoneTests {
    public static void main(String[] args) {
        testStormproofWithRepDipBelowZero();
        testStormproofWithNoProfitOrLoss();
        testStormproofWithAllCriteriaMet();
        testStormproofNotEvaluatedMidWeek();
        testWeekMinReputationTracking();
        System.out.println("All StormproofMilestoneTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that Stormproof is NOT awarded if reputation dips below 0 during the week.
     */
    private static void testStormproofWithRepDipBelowZero() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Set up positive week conditions
        s.weekRevenue = 500.0;
        s.weekCosts = 300.0; // Profit = 200
        s.weekNegativeEvents = 3;
        
        // Simulate reputation dropping below 0 mid-week
        s.reputation = 50;
        s.weekMinReputation = 50;
        EconomySystem eco = new EconomySystem(s, new UILogger(new JTextPane()));
        MilestoneSystem milestones = new MilestoneSystem(s, new UILogger(new JTextPane()));
        eco.setMilestones(milestones);
        
        // Drop reputation below 0
        eco.applyRep(-60, "Test negative event");
        assert s.reputation < 0 : "Reputation should be negative";
        assert s.weekMinReputation < 0 : "weekMinReputation should track the dip below 0";
        
        // Recover reputation
        eco.applyRep(30, "Test positive event");
        
        // Evaluate at week end
        milestones.onWeekEnd();
        
        // Should NOT have Stormproof because rep dipped below 0
        assert !s.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should NOT be awarded when reputation dips below 0";
    }

    /**
     * Test that Stormproof is NOT awarded if week profit <= 0.
     */
    private static void testStormproofWithNoProfitOrLoss() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Set up conditions with no profit
        s.weekRevenue = 300.0;
        s.weekCosts = 300.0; // Profit = 0
        s.weekNegativeEvents = 3;
        s.reputation = 50;
        s.weekMinReputation = 50;
        
        MilestoneSystem milestones = new MilestoneSystem(s, new UILogger(new JTextPane()));
        milestones.onWeekEnd();
        
        assert !s.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should NOT be awarded with zero profit";
        
        // Test with loss
        GameState s2 = GameFactory.newGame();
        s2.weekRevenue = 200.0;
        s2.weekCosts = 300.0; // Profit = -100
        s2.weekNegativeEvents = 3;
        s2.reputation = 50;
        s2.weekMinReputation = 50;
        
        MilestoneSystem milestones2 = new MilestoneSystem(s2, new UILogger(new JTextPane()));
        milestones2.onWeekEnd();
        
        assert !s2.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should NOT be awarded with negative profit";
    }

    /**
     * Test that Stormproof IS awarded when all criteria are met.
     */
    private static void testStormproofWithAllCriteriaMet() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Set up all positive conditions
        s.weekRevenue = 500.0;
        s.weekCosts = 300.0; // Profit = 200 > 0
        s.weekNegativeEvents = 3; // Exactly 3 negative events
        s.reputation = 50;
        s.weekMinReputation = 10; // Never dipped below 0
        
        MilestoneSystem milestones = new MilestoneSystem(s, new UILogger(new JTextPane()));
        milestones.onWeekEnd();
        
        assert s.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should be awarded when all criteria are met";
    }

    /**
     * Test that Stormproof is only evaluated at week-end, not mid-week.
     */
    private static void testStormproofNotEvaluatedMidWeek() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Set up all positive conditions
        s.weekRevenue = 500.0;
        s.weekCosts = 300.0;
        s.weekNegativeEvents = 3;
        s.reputation = 50;
        s.weekMinReputation = 50;
        
        MilestoneSystem milestones = new MilestoneSystem(s, new UILogger(new JTextPane()));
        
        // Evaluate at other times (not week end)
        milestones.onRepChanged();
        assert !s.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should NOT be evaluated on reputation change";
        
        milestones.onNightEnd();
        assert !s.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should NOT be evaluated at night end";
        
        // Only evaluated at week end
        milestones.onWeekEnd();
        assert s.achievedMilestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)
            : "M18_STORMPROOF_OPERATOR should be evaluated at week end";
    }

    /**
     * Test that weekMinReputation is properly tracked and reset.
     */
    private static void testWeekMinReputationTracking() {
        GameState s = GameFactory.newGame();
        s.reputation = 50;
        s.weekMinReputation = 50;
        
        EconomySystem eco = new EconomySystem(s, new UILogger(new JTextPane()));
        MilestoneSystem milestones = new MilestoneSystem(s, new UILogger(new JTextPane()));
        eco.setMilestones(milestones);
        
        // Apply positive reputation change - min should not change
        eco.applyRep(10, "Test positive");
        assert s.reputation > 50 : "Reputation should increase";
        assert s.weekMinReputation == 50 : "weekMinReputation should remain at 50";
        
        // Apply negative reputation change - min should update
        eco.applyRep(-20, "Test negative");
        assert s.reputation < 60 : "Reputation should decrease";
        assert s.weekMinReputation <= s.reputation : "weekMinReputation should track minimum";
        
        // Verify reset at week rollover
        Simulation sim = newSimulation(s);
        s.dayIndex = 6; // Last day of week
        sim.closeNight("Closing time.");
        
        // Week rollover should reset weekMinReputation to current reputation
        assert s.weekMinReputation == s.reputation 
            : "weekMinReputation should reset to current reputation at week start";
    }
}
