import javax.swing.JTextPane;

/**
 * Tests for time-gated pub level progression.
 * Validates that level-ups require both milestone count AND minimum weeks at current level.
 */
public class PubLevelTimeGateTests {
    public static void main(String[] args) {
        testLevel0To1RequiresTwoWeeks();
        testMilestonesEarlyDoesNotLevelUp();
        testBothConditionsMustBeMet();
        testWeekCounterResetsOnLevelUp();
        testChainLevelingPrevented();
        testProgressionSummaryShowsWeeks();
        System.out.println("All Pub Level Time Gate Tests passed.");
        System.exit(0);
    }

    /**
     * Test that leveling from 0 to 1 requires 2 milestones AND 2 weeks at level 0.
     */
    private static void testLevel0To1RequiresTwoWeeks() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Grant 2 milestones
        state.achievedMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES);
        state.milestonesAchievedCount = 2;
        
        // After 0 weeks, should not level up (need 2 weeks)
        state.weeksAtCurrentLevel = 0;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 0 : "Should stay at level 0 with 0 weeks, got " + state.pubLevel;
        
        // After 1 week, should still not level up
        state.weeksAtCurrentLevel = 1;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 0 : "Should stay at level 0 with 1 week, got " + state.pubLevel;
        
        // After 2 weeks, should level up
        state.weeksAtCurrentLevel = 2;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should level up to 1 with 2 weeks, got " + state.pubLevel;
        assert state.weeksAtCurrentLevel == 0 : "Week counter should reset after level-up, got " + state.weeksAtCurrentLevel;
    }

    /**
     * Test that achieving milestones early doesn't allow immediate level-up.
     */
    private static void testMilestonesEarlyDoesNotLevelUp() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Start at level 1 with 2 weeks already spent
        state.pubLevel = 1;
        state.milestonesAchievedCount = 2;
        state.weeksAtCurrentLevel = 2;
        levelSystem.updatePubLevel(state);
        applyBonusesForLevel(state, 1);
        
        // Immediately achieve enough milestones for level 2 (need 5 total)
        state.achievedMilestones.add(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M5_CALM_HOUSE);
        state.milestonesAchievedCount = 5;
        
        // Still at 2 weeks at level 1, need 3 weeks total
        state.weeksAtCurrentLevel = 2;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should stay at level 1, need 3 weeks, got level " + state.pubLevel;
        
        // After 3 weeks, should level up
        state.weeksAtCurrentLevel = 3;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 2 : "Should level up to 2 with 3 weeks, got " + state.pubLevel;
    }

    /**
     * Test that BOTH milestone and week conditions must be met.
     */
    private static void testBothConditionsMustBeMet() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Case 1: Has weeks but not milestones
        state.pubLevel = 0;
        state.weeksAtCurrentLevel = 10;
        state.milestonesAchievedCount = 1;  // Need 2 for level 1
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 0 : "Should stay at 0 without milestones, got " + state.pubLevel;
        
        // Case 2: Has milestones but not weeks
        state.milestonesAchievedCount = 2;
        state.weeksAtCurrentLevel = 1;  // Need 2 for level 1
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 0 : "Should stay at 0 without weeks, got " + state.pubLevel;
        
        // Case 3: Has both
        state.weeksAtCurrentLevel = 2;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should level up with both conditions, got " + state.pubLevel;
    }

    /**
     * Test that week counter resets when leveling up.
     */
    private static void testWeekCounterResetsOnLevelUp() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Set up for level 0 -> 1
        state.milestonesAchievedCount = 2;
        state.weeksAtCurrentLevel = 5;  // More than needed
        
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should be at level 1";
        assert state.weeksAtCurrentLevel == 0 : "Week counter should reset to 0, got " + state.weeksAtCurrentLevel;
    }

    /**
     * Test that chain-leveling is prevented (only one level per check).
     */
    private static void testChainLevelingPrevented() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Set up with enough milestones for level 3 (need 9)
        // and enough weeks for multiple level-ups
        state.pubLevel = 0;
        state.milestonesAchievedCount = 9;
        state.weeksAtCurrentLevel = 10;  // More than enough for any single level
        
        // Should only level up once per check (0 -> 1)
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should level up to 1 only, got " + state.pubLevel;
        assert state.weeksAtCurrentLevel == 0 : "Week counter should reset";
        
        // Need to accumulate weeks again for next level
        state.weeksAtCurrentLevel = 2;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should stay at 1, need 3 weeks for level 2, got " + state.pubLevel;
        
        state.weeksAtCurrentLevel = 3;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 2 : "Should level up to 2 now, got " + state.pubLevel;
    }

    /**
     * Test that progressionSummary displays week requirements.
     */
    private static void testProgressionSummaryShowsWeeks() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        state.pubLevel = 1;
        state.milestonesAchievedCount = 3;
        state.weeksAtCurrentLevel = 1;
        applyBonusesForLevel(state, 1);
        
        String summary = levelSystem.progressionSummary(state);
        
        assert summary.contains("Weeks at level") : "Summary should mention weeks, got: " + summary;
        assert summary.contains("1 / 3") : "Summary should show 1/3 weeks for level 1, got: " + summary;
    }

    private static void applyBonusesForLevel(GameState state, int level) {
        state.pubLevelServeCapBonus = level * 1;
        state.pubLevelBarCapBonus = level * 2;
        state.pubLevelTrafficBonusPct = level * 0.05;
        state.pubLevelRepMultiplier = switch (level) {
            case 3 -> 1.10;
            case 2 -> 1.05;
            case 1 -> 1.02;
            default -> 0.98;
        };
        state.pubLevelStaffCapBonus = level;
        state.pubLevelManagerCapBonus = Math.max(0, level - 1);
        state.pubLevelChefCapBonus = Math.max(0, level - 1);
        state.pubLevelBouncerCapBonus = Math.max(0, level - 1);
    }
}
