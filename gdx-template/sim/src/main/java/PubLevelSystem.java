import java.util.EnumSet;
import java.util.List;

public class PubLevelSystem {

    public static final int MAX_LEVEL = PrestigeSystem.MAX_LEVEL;

    public void updatePubLevel(GameState s) {
        int level = 0;
        for (int i = 1; i <= MAX_LEVEL; i++) {
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
        int prestigeWeeks = s.weeksSincePrestige();
        EnumSet<MilestoneSystem.Milestone> milestones = s.prestigeMilestones;
        return switch (targetLevel) {
            case 1 -> prestigeWeeks >= 2 && milestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
            case 2 -> prestigeWeeks >= 4
                    && milestones.contains(MilestoneSystem.Milestone.M9_KNOWN_FOR_SOMETHING)
                    && milestones.contains(MilestoneSystem.Milestone.M10_MIXED_CROWD_WHISPERER);
            case 3 -> prestigeWeeks >= 6
                    && milestones.contains(MilestoneSystem.Milestone.M14_DEBT_DIET)
                    && milestones.contains(MilestoneSystem.Milestone.M15_BALANCED_BOOKS_BUSY_HOUSE);
            case 4 -> prestigeWeeks >= 8
                    && milestones.contains(MilestoneSystem.Milestone.M12_BOOKED_OUT)
                    && milestones.contains(MilestoneSystem.Milestone.M16_SUPPLIERS_FAVOURITE);
            case 5 -> prestigeWeeks >= 10
                    && milestones.contains(MilestoneSystem.Milestone.M17_GOLDEN_QUARTER)
                    && milestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR);
            case 6 -> prestigeWeeks >= 12
                    && milestones.contains(MilestoneSystem.Milestone.M19_HEADLINER_VENUE)
                    && milestones.contains(MilestoneSystem.Milestone.M13_BRIDGE_DONT_BLEED)
                    && milestones.contains(MilestoneSystem.Milestone.M14_DEBT_DIET);
            default -> false;
        };
    }

    public String progressionSummary(GameState s) {
        int next = Math.min(MAX_LEVEL, s.pubLevel + 1);
        if (next <= s.pubLevel) return "Max pub level reached.";
        StringBuilder sb = new StringBuilder();
        sb.append("Next level requirements (Lv ").append(next).append("):\n");
        for (String req : levelRequirementsText(s, next)) {
            sb.append(" - ").append(req).append("\n");
        }
        return sb.toString();
    }

    private List<String> levelRequirementsText(GameState s, int level) {
        int prestigeWeeks = s.weeksSincePrestige();
        EnumSet<MilestoneSystem.Milestone> milestones = s.prestigeMilestones;
        return switch (level) {
            case 1 -> List.of(
                    formatRequirement("Week 2+ (since prestige)", prestigeWeeks >= 2),
                    formatRequirement("Milestone: Open For Business", milestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS))
            );
            case 2 -> List.of(
                    formatRequirement("Week 4+ (since prestige)", prestigeWeeks >= 4),
                    formatRequirement("Milestone: Known For Something", milestones.contains(MilestoneSystem.Milestone.M9_KNOWN_FOR_SOMETHING)),
                    formatRequirement("Milestone: Mixed Crowd Whisperer", milestones.contains(MilestoneSystem.Milestone.M10_MIXED_CROWD_WHISPERER))
            );
            case 3 -> List.of(
                    formatRequirement("Week 6+ (since prestige)", prestigeWeeks >= 6),
                    formatRequirement("Milestone: Debt Diet", milestones.contains(MilestoneSystem.Milestone.M14_DEBT_DIET)),
                    formatRequirement("Milestone: Balanced Books", milestones.contains(MilestoneSystem.Milestone.M15_BALANCED_BOOKS_BUSY_HOUSE))
            );
            case 4 -> List.of(
                    formatRequirement("Week 8+ (since prestige)", prestigeWeeks >= 8),
                    formatRequirement("Milestone: Booked Out", milestones.contains(MilestoneSystem.Milestone.M12_BOOKED_OUT)),
                    formatRequirement("Milestone: Supplier's Favourite", milestones.contains(MilestoneSystem.Milestone.M16_SUPPLIERS_FAVOURITE))
            );
            case 5 -> List.of(
                    formatRequirement("Week 10+ (since prestige)", prestigeWeeks >= 10),
                    formatRequirement("Milestone: Golden Quarter", milestones.contains(MilestoneSystem.Milestone.M17_GOLDEN_QUARTER)),
                    formatRequirement("Milestone: Stormproof Operator", milestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR))
            );
            default -> List.of("No further requirements.");
        };
    }

    private String formatRequirement(String label, boolean met) {
        return (met ? "[✓] " : "[ ] ") + label;
    }

    public String compactNextLevelBadge(GameState s) {
        int next = Math.min(MAX_LEVEL, s.pubLevel + 1);
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
        int prestigeWeeks = s.weeksSincePrestige();
        EnumSet<MilestoneSystem.Milestone> milestones = s.prestigeMilestones;
        switch (level) {
            case 1 -> {
                if (prestigeWeeks < 2) unmet.add("Week 2+ (" + prestigeWeeks + "/2 since prestige)");
                if (!milestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS)) {
                    unmet.add("Milestone: Open For Business");
                }
            }
            case 2 -> {
                if (prestigeWeeks < 4) unmet.add("Week 4+ (" + prestigeWeeks + "/4 since prestige)");
                if (!milestones.contains(MilestoneSystem.Milestone.M9_KNOWN_FOR_SOMETHING)) {
                    unmet.add("Milestone: Known For Something");
                }
                if (!milestones.contains(MilestoneSystem.Milestone.M10_MIXED_CROWD_WHISPERER)) {
                    unmet.add("Milestone: Mixed Crowd Whisperer");
                }
            }
            case 3 -> {
                if (prestigeWeeks < 6) unmet.add("Week 6+ (" + prestigeWeeks + "/6 since prestige)");
                if (!milestones.contains(MilestoneSystem.Milestone.M14_DEBT_DIET)) {
                    unmet.add("Milestone: Debt Diet");
                }
                if (!milestones.contains(MilestoneSystem.Milestone.M15_BALANCED_BOOKS_BUSY_HOUSE)) {
                    unmet.add("Milestone: Balanced Books");
                }
            }
            case 4 -> {
                if (prestigeWeeks < 8) unmet.add("Week 8+ (" + prestigeWeeks + "/8 since prestige)");
                if (!milestones.contains(MilestoneSystem.Milestone.M12_BOOKED_OUT)) {
                    unmet.add("Milestone: Booked Out");
                }
                if (!milestones.contains(MilestoneSystem.Milestone.M16_SUPPLIERS_FAVOURITE)) {
                    unmet.add("Milestone: Supplier's Favourite");
                }
            }
            case 5 -> {
                if (prestigeWeeks < 10) unmet.add("Week 10+ (" + prestigeWeeks + "/10 since prestige)");
                if (!milestones.contains(MilestoneSystem.Milestone.M17_GOLDEN_QUARTER)) {
                    unmet.add("Milestone: Golden Quarter");
                }
                if (!milestones.contains(MilestoneSystem.Milestone.M18_STORMPROOF_OPERATOR)) {
                    unmet.add("Milestone: Stormproof Operator");
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
