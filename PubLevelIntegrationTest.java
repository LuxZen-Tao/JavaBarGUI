import javax.swing.JTextPane;

/**
 * Integration test for count-based pub leveling in a realistic game scenario.
 * Validates the milestone count system works properly during actual gameplay.
 */
public class PubLevelIntegrationTest {
    public static void main(String[] args) {
        testRealisticProgression();
        testCountPersistsAcrossSessions();
        testMilestoneSpecificUnlocksStillWork();
        System.out.println("All PubLevelIntegrationTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Simulate a realistic game progression where player achieves milestones
     * and pub level increases accordingly.
     */
    private static void testRealisticProgression() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        MilestoneSystem milestones = new MilestoneSystem(state, new UILogger(new JTextPane()));
        PubLevelSystem levelSystem = new PubLevelSystem();

        // Initially at level 0
        assert state.pubLevel == 0 : "Should start at level 0";
        assert state.milestonesAchievedCount == 0 : "Should start with 0 milestones";

        // Achieve first milestone: M1_OPEN_FOR_BUSINESS
        state.openForBusinessNights = 5;
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        assert state.milestonesAchievedCount == 1 : "Should have 1 milestone, got " + state.milestonesAchievedCount;
        
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 0 : "Should still be level 0 with 1 milestone";

        // Achieve second milestone: M2_NO_EMPTY_SHELVES
        state.noStockoutStreakNights = 2;
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        assert state.milestonesAchievedCount == 2 : "Should have 2 milestones, got " + state.milestonesAchievedCount;
        
        // Still need to wait 2 weeks at level 0
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 0 : "Should still be level 0 without enough weeks";
        
        // Simulate 2 weeks passing
        state.weeksAtCurrentLevel = 2;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should be level 1 with 2 milestones and 2 weeks, got " + state.pubLevel;

        // Achieve three more milestones to reach level 2 (need 5 total)
        state.lastServiceRanFullRounds = true;
        state.punterKickedOffFromNeglect = 0;
        state.punterLeftBecauseBroke = 0;
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        
        state.paydayWindowClosed = true;
        state.wagesPaidLastWeek = true;
        state.rentAccruedThisWeek = 0.0;
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.WEEK_END);
        
        state.calmNightsStreak = 3;
        state.calmNightsWithActivityStreak = 1;
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        
        int countBefore6 = state.milestonesAchievedCount;
        assert countBefore6 == 5 : "Should have 5 milestones, got " + countBefore6;
        
        // Need 3 weeks at level 1 before reaching level 2
        state.weeksAtCurrentLevel = 3;
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 2 : "Should be level 2 with 5 milestones and 3 weeks, got " + state.pubLevel;
    }

    /**
     * Test that milestone count persists correctly when loading from save.
     */
    private static void testCountPersistsAcrossSessions() {
        GameState state = GameFactory.newGame();
        
        // Manually set up a saved game state with 3 milestones at level 1
        state.achievedMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES);
        state.achievedMilestones.add(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY);
        state.prestigeMilestones.addAll(state.achievedMilestones);
        state.milestonesAchievedCount = 3;
        state.pubLevel = 1;
        state.weeksAtCurrentLevel = 1;
        
        // Simulate loading by creating new MilestoneSystem
        MilestoneSystem milestones = new MilestoneSystem(state, new UILogger(new JTextPane()));
        
        // Count should be preserved
        assert state.milestonesAchievedCount == 3 : "Count should persist, got " + state.milestonesAchievedCount;
        
        // Level should be correct
        PubLevelSystem levelSystem = new PubLevelSystem();
        levelSystem.updatePubLevel(state);
        assert state.pubLevel == 1 : "Should be level 1 with 3 milestones";
    }

    /**
     * Test that milestone-specific unlocks (activities, upgrades) still work.
     * This ensures we didn't break the existing milestone unlock system.
     */
    private static void testMilestoneSpecificUnlocksStillWork() {
        GameState state = GameFactory.newGame();
        MilestoneSystem milestones = new MilestoneSystem(state, new UILogger(new JTextPane()));
        
        // Initially, karaoke should be locked (requires M1_OPEN_FOR_BUSINESS)
        assert !milestones.isActivityUnlocked(PubActivity.KARAOKE) 
            : "Karaoke should be locked initially";
        
        // Achieve M1_OPEN_FOR_BUSINESS
        state.openForBusinessNights = 5;
        milestones.evaluateMilestones(MilestoneSystem.EvaluationReason.NIGHT_END);
        
        // Now karaoke should be unlocked
        assert milestones.isActivityUnlocked(PubActivity.KARAOKE) 
            : "Karaoke should be unlocked after M1_OPEN_FOR_BUSINESS";
        
        // Test upgrade milestone lock (CCTV requires M6_MARGIN_WITH_MANNERS)
        assert !milestones.canBuyUpgrade(PubUpgrade.CCTV) 
            : "CCTV should be locked without M6_MARGIN_WITH_MANNERS";
        
        // Grant M6 milestone
        state.achievedMilestones.add(MilestoneSystem.Milestone.M6_MARGIN_WITH_MANNERS);
        state.prestigeMilestones.add(MilestoneSystem.Milestone.M6_MARGIN_WITH_MANNERS);
        milestones.recomputeUpgradeAvailability();
        
        // CCTV still needs cash, but milestone requirement should be met
        MilestoneSystem.UpgradeAvailability cctv = milestones.getUpgradeAvailability(PubUpgrade.CCTV, 1000000.0);
        assert cctv.unlocked() : "CCTV should be unlocked with M6 and sufficient cash";
    }
}
