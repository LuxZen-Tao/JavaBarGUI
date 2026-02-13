/**
 * Demo to show the milestone count-based pub leveling system in action.
 * This helps verify the thresholds and progression visually.
 */
public class PubLevelDemo {
    public static void main(String[] args) {
        System.out.println("=== Pub Level Threshold Demo ===\n");
        
        // Display thresholds for each level
        System.out.println("Level Thresholds:");
        for (int level = 0; level <= 6; level++) {
            int threshold = PubLevelSystem.thresholdForLevel(level);
            int nextThreshold = PubLevelSystem.thresholdForLevel(level + 1);
            int required = nextThreshold - threshold;
            System.out.printf("Level %d: %d milestones (need +%d more for next level)\n", 
                level, threshold, required);
        }
        
        System.out.println("\n=== Simulated Progression ===\n");
        
        GameState state = GameFactory.newGame();
        PubLevelSystem levelSystem = new PubLevelSystem();
        
        // Simulate achieving milestones
        int[] milestoneCheckpoints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 14, 20, 27};
        
        for (int count : milestoneCheckpoints) {
            state.milestonesAchievedCount = count;
            levelSystem.updatePubLevel(state);
            
            String badge = levelSystem.compactNextLevelBadge(state);
            System.out.printf("Milestones: %2d → Pub Level: %d | %s\n", 
                count, state.pubLevel, badge);
        }
        
        System.out.println("\n=== Progression Summary Example ===\n");
        
        // Show progression summary at various levels
        state.milestonesAchievedCount = 3;
        levelSystem.updatePubLevel(state);
        System.out.println("With 3 milestones:");
        System.out.println(levelSystem.progressionSummary(state));
        
        state.milestonesAchievedCount = 7;
        levelSystem.updatePubLevel(state);
        System.out.println("With 7 milestones:");
        System.out.println(levelSystem.progressionSummary(state));
        
        state.milestonesAchievedCount = 27;
        levelSystem.updatePubLevel(state);
        System.out.println("With 27 milestones:");
        System.out.println(levelSystem.progressionSummary(state));
        
        System.out.println("=== Demo Complete ===");
    }
}
