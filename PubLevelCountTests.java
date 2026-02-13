import javax.swing.JTextPane;

/**
 * Tests for count-based pub leveling system.
 * Validates that pub levels are determined by the number of unique milestones achieved,
 * not by specific milestone gates.
 */
public class PubLevelCountTests {
    public static void main(String[] args) {
        testThresholdCalculation();
        testTwoMilestonesReachesLevel1();
        testFiveMilestonesReachesLevel2();
        testNineMilestonesReachesLevel3();
        testSameMilestoneDoesNotDoubleCount();
        testSaveLoadPreservesCountAndLevel();
        testLevelSkippingAllowed();
        testProgressionSummaryDisplaysCount();
        System.out.println("All PubLevelCountTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that thresholdForLevel() calculates correct cumulative values.
     * Level 1: 2, Level 2: 5, Level 3: 9, Level 4: 14, Level 5: 20, Level 6: 27
     */
    private static void testThresholdCalculation() {
        assert PubLevelSystem.thresholdForLevel(1) == 2 : "Level 1 should require 2 milestones";
        assert PubLevelSystem.thresholdForLevel(2) == 5 : "Level 2 should require 5 milestones (2+3)";
        assert PubLevelSystem.thresholdForLevel(3) == 9 : "Level 3 should require 9 milestones (5+4)";
        assert PubLevelSystem.thresholdForLevel(4) == 14 : "Level 4 should require 14 milestones (9+5)";
        assert PubLevelSystem.thresholdForLevel(5) == 20 : "Level 5 should require 20 milestones (14+6)";
        assert PubLevelSystem.thresholdForLevel(6) == 27 : "Level 6 should require 27 milestones (20+7)";
    }

    /**
     * Test that achieving 2 unique milestones sets pubLevel from 0 to 1.
     */
    private static void testTwoMilestonesReachesLevel1() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Manually grant 2 milestones
        state.achievedMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES);
        state.milestonesAchievedCount = 2;
        
        levelSystem.updatePubLevel(state);
        
        assert state.pubLevel == 1 : "Pub level should be 1 with 2 milestones, got " + state.pubLevel;
    }

    /**
     * Test that achieving 5 unique milestones sets pubLevel to 2.
     */
    private static void testFiveMilestonesReachesLevel2() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Manually grant 5 milestones
        state.achievedMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M5_CALM_HOUSE);
        state.milestonesAchievedCount = 5;
        
        levelSystem.updatePubLevel(state);
        
        assert state.pubLevel == 2 : "Pub level should be 2 with 5 milestones, got " + state.pubLevel;
    }

    /**
     * Test that achieving 9 unique milestones sets pubLevel to 3.
     */
    private static void testNineMilestonesReachesLevel3() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Manually grant 9 milestones
        state.achievedMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M5_CALM_HOUSE);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M6_MARGIN_WITH_MANNERS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M7_CREW_THAT_STAYS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M8_ORDER_RESTORED);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M9_KNOWN_FOR_SOMETHING);
        state.milestonesAchievedCount = 9;
        
        levelSystem.updatePubLevel(state);
        
        assert state.pubLevel == 3 : "Pub level should be 3 with 9 milestones, got " + state.pubLevel;
    }

    /**
     * Test that achieving the same milestone twice does not increase count twice.
     * This is enforced by EnumSet semantics and checking if milestone already exists.
     */
    private static void testSameMilestoneDoesNotDoubleCount() {
        GameState state = GameFactory.newGame();
        MilestoneSystem milestones = new MilestoneSystem(state, new UILogger(new JTextPane()));
        
        // Setup to trigger M1_OPEN_FOR_BUSINESS
        state.openForBusinessNights = 5;
        
        // Trigger milestone evaluation
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        
        int countAfterFirst = state.milestonesAchievedCount;
        boolean containsAfterFirst = state.achievedMilestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        
        // Try to trigger again
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        
        int countAfterSecond = state.milestonesAchievedCount;
        
        assert containsAfterFirst : "M1 should be achieved after first trigger";
        assert countAfterFirst == countAfterSecond : "Count should not increase on duplicate milestone: was " 
            + countAfterFirst + ", became " + countAfterSecond;
    }

    /**
     * Test that loading a save preserves achieved milestones and derived count/level correctly.
     */
    private static void testSaveLoadPreservesCountAndLevel() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Grant 5 milestones and set count
        state.achievedMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M5_CALM_HOUSE);
        state.prestigeMilestones.addAll(state.achievedMilestones);
        state.milestonesAchievedCount = 5;
        
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 2 : "Initial pub level should be 2";
        
        // Simulate save/load by creating new MilestoneSystem which syncs count
        MilestoneSystem milestones = new MilestoneSystem(state, new UILogger(new JTextPane()));
        
        // Verify count was preserved or synced
        assert state.milestonesAchievedCount == 5 : "Count should be preserved: " + state.milestonesAchievedCount;
        
        // Re-update pub level and verify
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 2 : "Pub level should still be 2 after reload";
    }

    /**
     * Test that achieving milestones allows skipping multiple levels if count jumps.
     */
    private static void testLevelSkippingAllowed() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Directly set count to 14 (enough for level 4)
        state.milestonesAchievedCount = 14;
        
        levelSystem.updatePubLevel(state);
        
        assert state.pubLevel == 4 : "Pub level should be 4 with 14 milestones, got " + state.pubLevel;
    }

    /**
     * Test that progressionSummary displays count-based progress.
     */
    private static void testProgressionSummaryDisplaysCount() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Set count to 3 (between levels 1 and 2)
        state.milestonesAchievedCount = 3;
        levelSystem.updatePubLevel(state);
        
        String summary = levelSystem.progressionSummary(state);
        
        assert summary.contains("3") : "Summary should show current count 3";
        assert summary.contains("5") : "Summary should show next threshold 5";
        assert summary.contains("Milestones") : "Summary should mention milestones";
    }
}
