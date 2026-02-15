import java.util.EnumSet;
import java.util.List;

public class PubLevelSystem {

    public static final int MAX_LEVEL = PrestigeSystem.MAX_LEVEL;

    /**
     * Compute cumulative milestone threshold for a given level.
     * Level 1 requires 2 milestones.
     * Each subsequent level requires (level + 1) more milestones.
     * Cumulative: 2, 5, 9, 14, 20, 27...
     */
    public static int thresholdForLevel(int level) {
        if (level <= 0) return 0;
        if (level == 1) return 2;
        
        int cumulative = 2;  // Start with level 1's threshold
        for (int i = 1; i < level; i++) {
            cumulative += (i + 2);  // Each level i→i+1 requires i+2 milestones
        }
        return cumulative;
    }

    /**
     * Minimum weeks required at a level before progressing to the next level.
     * Level 0: 1 week (intro week), Level 1: 3 weeks, Level 2: 4 weeks, Level 3: 5 weeks, Level 4: 6 weeks
     */
    public static int weeksRequiredForLevel(int level) {
        if (level == 0) return 1;  // Level 0 = 1 week (intro week)
        return level + 2;  // Level 1 = 3 weeks, Level 2 = 4 weeks, etc.
    }

    /**
     * Update pub level based on both milestone count and time gate.
     * Level-up only occurs when BOTH conditions are met:
     * 1. Milestone threshold reached
     * 2. Minimum weeks at current level completed
     */
    public void updatePubLevel(GameState s) {
        int count = s.milestonesAchievedCount;
        int currentLevel = s.pubLevel;
        int newLevel = currentLevel;
        
        // Check each level threshold and find highest eligible level
        // Must meet BOTH milestone AND week requirements
        for (int i = currentLevel + 1; i <= MAX_LEVEL; i++) {
            if (count >= thresholdForLevel(i) && s.weeksAtCurrentLevel >= weeksRequiredForLevel(currentLevel)) {
                newLevel = i;
                // Only level up one at a time to prevent chain-leveling
                break;
            }
        }

        // If level changed, reset week counter and apply bonuses
        if (newLevel > currentLevel) {
            s.pubLevel = newLevel;
            s.weeksAtCurrentLevel = 0;
            applyLevelBonuses(s, newLevel);
        }
    }

    private void applyLevelBonuses(GameState s, int level) {
        s.pubLevelServeCapBonus = level * 1;
        s.pubLevelBarCapBonus = level * 2;
        s.pubLevelTrafficBonusPct = level * 0.05;
        s.pubLevelRepMultiplier = switch (level) {
            case 3 -> 1.10;
            case 2 -> 1.05;
            case 1 -> 1.02;
            default -> 0.98;
        };

        s.pubLevelStaffCapBonus = level;
        s.pubLevelManagerCapBonus = Math.max(0, level - 1);
        s.pubLevelChefCapBonus = Math.max(0, level - 1);
        s.pubLevelBouncerCapBonus = Math.max(0, level - 1);
    }

    public boolean meetsLevelRequirement(GameState s, int targetLevel) {
        // For prestige eligibility, only check milestone requirements
        return s.milestonesAchievedCount >= thresholdForLevel(targetLevel);
    }

    /**
     * Check if player can level up to the next level.
     * Requires both milestone threshold AND minimum weeks at current level.
     */
    public boolean canLevelUp(GameState s) {
        int next = s.pubLevel + 1;
        if (next > MAX_LEVEL) return false;
        
        boolean hasMilestones = s.milestonesAchievedCount >= thresholdForLevel(next);
        boolean hasWeeks = s.weeksAtCurrentLevel >= weeksRequiredForLevel(s.pubLevel);
        
        return hasMilestones && hasWeeks;
    }

    public String progressionSummary(GameState s) {
        int next = Math.min(MAX_LEVEL, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max pub level reached.";
        
        int currentMilestones = s.milestonesAchievedCount;
        int neededMilestones = thresholdForLevel(next);
        int currentWeeks = s.weeksAtCurrentLevel;
        int neededWeeks = weeksRequiredForLevel(s.pubLevel);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Next level requirements (Lv ").append(next).append("):\n");
        sb.append(" - Milestones: ").append(currentMilestones).append(" / ").append(neededMilestones);
        if (currentMilestones >= neededMilestones) {
            sb.append(" [✓]");
        }
        sb.append("\n");
        sb.append(" - Weeks at level: ").append(currentWeeks).append(" / ").append(neededWeeks);
        if (currentWeeks >= neededWeeks) {
            sb.append(" [✓]");
        }
        sb.append("\n");
        return sb.toString();
    }

    private List<String> levelRequirementsText(GameState s, int level) {
        java.util.List<String> requirements = new java.util.ArrayList<>();
        int currentMilestones = s.milestonesAchievedCount;
        int neededMilestones = thresholdForLevel(level);
        String milestoneStatus = currentMilestones >= neededMilestones ? "[✓]" : "[ ]";
        requirements.add(milestoneStatus + " Milestones: " + currentMilestones + " / " + neededMilestones);
        
        if (level > s.pubLevel) {
            int currentWeeks = s.weeksAtCurrentLevel;
            int neededWeeks = weeksRequiredForLevel(s.pubLevel);
            String weekStatus = currentWeeks >= neededWeeks ? "[✓]" : "[ ]";
            requirements.add(weekStatus + " Weeks: " + currentWeeks + " / " + neededWeeks);
        }
        
        return requirements;
    }

    public String compactNextLevelBadge(GameState s) {
        int next = Math.min(MAX_LEVEL, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max level";
        
        int currentMilestones = s.milestonesAchievedCount;
        int neededMilestones = thresholdForLevel(next);
        int currentWeeks = s.weeksAtCurrentLevel;
        int neededWeeks = weeksRequiredForLevel(s.pubLevel);
        
        boolean hasMilestones = currentMilestones >= neededMilestones;
        boolean hasWeeks = currentWeeks >= neededWeeks;
        
        if (hasMilestones && hasWeeks) return "Ready to level up";
        if (!hasMilestones && !hasWeeks) return "Next: M " + currentMilestones + "/" + neededMilestones + ", W " + currentWeeks + "/" + neededWeeks;
        if (!hasMilestones) return "Next: Milestones " + currentMilestones + "/" + neededMilestones;
        return "Next: Weeks " + currentWeeks + "/" + neededWeeks;
    }

    private List<String> compactLevelRequirements(GameState s, int level) {
        List<String> unmet = new java.util.ArrayList<>();
        int currentMilestones = s.milestonesAchievedCount;
        int neededMilestones = thresholdForLevel(level);
        
        if (currentMilestones < neededMilestones) {
            unmet.add("Milestones " + currentMilestones + "/" + neededMilestones);
        }
        
        if (level > s.pubLevel) {
            int currentWeeks = s.weeksAtCurrentLevel;
            int neededWeeks = weeksRequiredForLevel(s.pubLevel);
            if (currentWeeks < neededWeeks) {
                unmet.add("Weeks " + currentWeeks + "/" + neededWeeks);
            }
        }
        
        return unmet;
    }

    public boolean canAccessUpgradeTier(GameState s, PubUpgrade upgrade) {
        int tier = upgrade.getTier();
        if (tier <= 1) return true;
        return s.pubLevel >= tier - 1;
    }
}
