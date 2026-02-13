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

    public void updatePubLevel(GameState s) {
        int count = s.milestonesAchievedCount;
        int level = 0;
        
        // Check each level threshold and assign highest met level
        // Allow skipping multiple levels if count jumps
        for (int i = 1; i <= MAX_LEVEL; i++) {
            if (count >= thresholdForLevel(i)) {
                level = i;
            } else {
                break;
            }
        }

        s.pubLevel = level;
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
        return s.milestonesAchievedCount >= thresholdForLevel(targetLevel);
    }

    public String progressionSummary(GameState s) {
        int next = Math.min(MAX_LEVEL, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max pub level reached.";
        
        int current = s.milestonesAchievedCount;
        int needed = thresholdForLevel(next);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Next level requirements (Lv ").append(next).append("):\n");
        sb.append(" - Milestones: ").append(current).append(" / ").append(needed);
        if (current >= needed) {
            sb.append(" [✓]");
        }
        sb.append("\n");
        return sb.toString();
    }

    private List<String> levelRequirementsText(GameState s, int level) {
        int current = s.milestonesAchievedCount;
        int needed = thresholdForLevel(level);
        String status = current >= needed ? "[✓]" : "[ ]";
        return List.of(status + " Milestones: " + current + " / " + needed);
    }

    public String compactNextLevelBadge(GameState s) {
        int next = Math.min(MAX_LEVEL, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max level";
        
        int current = s.milestonesAchievedCount;
        int needed = thresholdForLevel(next);
        
        if (current >= needed) return "Ready to level up";
        return "Next: Milestones " + current + "/" + needed;
    }

    private List<String> compactLevelRequirements(GameState s, int level) {
        List<String> unmet = new java.util.ArrayList<>();
        int current = s.milestonesAchievedCount;
        int needed = thresholdForLevel(level);
        
        if (current < needed) {
            unmet.add("Milestones " + current + "/" + needed);
        }
        return unmet;
    }

    public boolean canAccessUpgradeTier(GameState s, PubUpgrade upgrade) {
        int tier = upgrade.getTier();
        if (tier <= 1) return true;
        return s.pubLevel >= tier - 1;
    }
}
