import java.util.EnumSet;
import java.util.List;

public class PubLevelSystem {

    public void updatePubLevel(GameState s) {
        int level = 0;
        for (int i = 1; i <= 3; i++) {
            if (meetsLevelRequirement(s, i)) {
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
        return switch (targetLevel) {
            case 1 -> s.weekCount >= 2 && s.achievedMilestones.contains(MilestoneSystem.Milestone.FIVE_NIGHTS);
            case 2 -> s.weekCount >= 4
                    && s.achievedMilestones.contains(MilestoneSystem.Milestone.KNOWN_VENUE)
                    && s.achievedMilestones.contains(MilestoneSystem.Milestone.KITCHEN_LAUNCH);
            case 3 -> s.weekCount >= 6
                    && s.achievedMilestones.contains(MilestoneSystem.Milestone.PROFIT_STREAK_4)
                    && s.achievedMilestones.contains(MilestoneSystem.Milestone.REP_STAR);
            default -> true;
        };
    }

    public String progressionSummary(GameState s) {
        int next = Math.min(3, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max pub level reached.";
        StringBuilder sb = new StringBuilder();
        sb.append("Next level requirements (Lv ").append(next).append("):\n");
        for (String req : levelRequirementsText(s, next)) {
            sb.append(" - ").append(req).append("\n");
        }
        return sb.toString();
    }

    private List<String> levelRequirementsText(GameState s, int level) {
        return switch (level) {
            case 1 -> List.of(
                    formatRequirement("Week 2+", s.weekCount >= 2),
                    formatRequirement("Milestone: Five Nights", s.achievedMilestones.contains(MilestoneSystem.Milestone.FIVE_NIGHTS))
            );
            case 2 -> List.of(
                    formatRequirement("Week 4+", s.weekCount >= 4),
                    formatRequirement("Milestone: Known Venue", s.achievedMilestones.contains(MilestoneSystem.Milestone.KNOWN_VENUE)),
                    formatRequirement("Milestone: Kitchen Launch", s.achievedMilestones.contains(MilestoneSystem.Milestone.KITCHEN_LAUNCH))
            );
            case 3 -> List.of(
                    formatRequirement("Week 6+", s.weekCount >= 6),
                    formatRequirement("Milestone: Profit Streak (4 weeks)", s.achievedMilestones.contains(MilestoneSystem.Milestone.PROFIT_STREAK_4)),
                    formatRequirement("Milestone: Reputation Star", s.achievedMilestones.contains(MilestoneSystem.Milestone.REP_STAR))
            );
            default -> List.of("No further requirements.");
        };
    }

    private String formatRequirement(String label, boolean met) {
        return (met ? "[✓] " : "[ ] ") + label;
    }

    public String compactNextLevelBadge(GameState s) {
        int next = Math.min(3, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max level";
        List<String> unmet = compactLevelRequirements(s, next);
        if (unmet.isEmpty()) return "Ready to level up";
        String join = unmet.size() > 1
                ? (unmet.get(0) + ", " + unmet.get(1))
                : unmet.get(0);
        return "Next: " + join;
    }

    private List<String> compactLevelRequirements(GameState s, int level) {
        List<String> unmet = new java.util.ArrayList<>();
        switch (level) {
            case 1 -> {
                if (s.weekCount < 2) unmet.add("Week 2+ (" + s.weekCount + "/2)");
                if (!s.achievedMilestones.contains(MilestoneSystem.Milestone.FIVE_NIGHTS)) {
                    unmet.add("Milestone: Five Nights");
                }
            }
            case 2 -> {
                if (s.weekCount < 4) unmet.add("Week 4+ (" + s.weekCount + "/4)");
                if (!s.achievedMilestones.contains(MilestoneSystem.Milestone.KNOWN_VENUE)) {
                    unmet.add("Milestone: Known Venue");
                }
                if (!s.achievedMilestones.contains(MilestoneSystem.Milestone.KITCHEN_LAUNCH)) {
                    unmet.add("Milestone: Kitchen Launch");
                }
            }
            case 3 -> {
                if (s.weekCount < 6) unmet.add("Week 6+ (" + s.weekCount + "/6)");
                if (!s.achievedMilestones.contains(MilestoneSystem.Milestone.PROFIT_STREAK_4)) {
                    unmet.add("Milestone: Profit Streak");
                }
                if (!s.achievedMilestones.contains(MilestoneSystem.Milestone.REP_STAR)) {
                    unmet.add("Milestone: Reputation Star");
                }
            }
            default -> {
                // no-op
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
