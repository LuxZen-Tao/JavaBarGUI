import java.util.ArrayList;
import java.util.List;

public enum SecurityTask {
    T1_VISIBLE_PATROL(
            1,
            SecurityTaskCategory.SOFT,
            "Visible Patrol",
            "Patrol",
            "Friendly presence on the floor to keep things calm.",
            0.96,
            1.03,
            2
    ),
    T1_CHECK_IDS(
            1,
            SecurityTaskCategory.BALANCED,
            "Check IDs",
            "IDs",
            "Spot checks at the door to deter trouble.",
            0.92,
            0.99,
            2
    ),
    T1_TIGHT_DOOR_TONIGHT(
            1,
            SecurityTaskCategory.STRICT,
            "Tight Door Tonight",
            "Tight",
            "Strict door for a quieter crowd.",
            0.88,
            0.96,
            3
    ),
    T2_DEESCALATION_FOCUS(
            2,
            SecurityTaskCategory.SOFT,
            "De-escalation Focus",
            "De-escalate",
            "Staff focus on early de-escalation and quick check-ins.",
            0.93,
            1.02,
            3
    ),
    T2_TARGETED_SCREENING(
            2,
            SecurityTaskCategory.BALANCED,
            "Targeted Screening",
            "Screen",
            "Targeted screening of rowdier arrivals.",
            0.89,
            0.98,
            3
    ),
    T2_HARD_LINE_DOOR(
            2,
            SecurityTaskCategory.STRICT,
            "Hard Line Door",
            "Hard Line",
            "No-nonsense door for risky nights.",
            0.84,
            0.95,
            4
    ),
    T3_CROWD_CONTROL_PROTOCOL(
            3,
            SecurityTaskCategory.SOFT,
            "Crowd Control Protocol",
            "Crowd",
            "Proactive crowd control keeps the floor steady.",
            0.91,
            1.01,
            3
    ),
    T3_SELECTIVE_ENTRY(
            3,
            SecurityTaskCategory.BALANCED,
            "Selective Entry",
            "Selective",
            "Selective entry to balance vibe and safety.",
            0.86,
            0.97,
            4
    ),
    T3_ZERO_TOLERANCE_NIGHT(
            3,
            SecurityTaskCategory.STRICT,
            "Zero Tolerance Night",
            "Zero",
            "Strict zero-tolerance policy for maximum control.",
            0.80,
            0.93,
            4
    );

    private final int tier;
    private final SecurityTaskCategory category;
    private final String label;
    private final String shortLabel;
    private final String description;
    private final double incidentChanceMultiplier;
    private final double trafficMultiplier;
    private final int cooldownRounds;

    SecurityTask(int tier,
                 SecurityTaskCategory category,
                 String label,
                 String shortLabel,
                 String description,
                 double incidentChanceMultiplier,
                 double trafficMultiplier,
                 int cooldownRounds) {
        this.tier = tier;
        this.category = category;
        this.label = label;
        this.shortLabel = shortLabel;
        this.description = description;
        this.incidentChanceMultiplier = incidentChanceMultiplier;
        this.trafficMultiplier = trafficMultiplier;
        this.cooldownRounds = cooldownRounds;
    }

    public int getTier() {
        return tier;
    }

    public SecurityTaskCategory getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getDescription() {
        return description;
    }

    public double getIncidentChanceMultiplier() {
        return incidentChanceMultiplier;
    }

    public double getTrafficMultiplier() {
        return trafficMultiplier;
    }

    public int getCooldownRounds() {
        return cooldownRounds;
    }

    public String effectSummary() {
        return "Incident x" + String.format("%.2f", incidentChanceMultiplier)
                + " | Traffic x" + String.format("%.2f", trafficMultiplier)
                + " | Cooldown " + cooldownRounds + "r";
    }

    public static List<SecurityTask> tasksForTier(int tier) {
        return switch (tier) {
            case 1 -> List.of(T1_VISIBLE_PATROL, T1_CHECK_IDS, T1_TIGHT_DOOR_TONIGHT);
            case 2 -> List.of(T2_DEESCALATION_FOCUS, T2_TARGETED_SCREENING, T2_HARD_LINE_DOOR);
            case 3 -> List.of(T3_CROWD_CONTROL_PROTOCOL, T3_SELECTIVE_ENTRY, T3_ZERO_TOLERANCE_NIGHT);
            default -> List.of();
        };
    }

    public static List<SecurityTask> tasksUpToTier(int tier) {
        List<SecurityTask> tasks = new ArrayList<>();
        for (int t = 1; t <= tier; t++) {
            tasks.addAll(tasksForTier(t));
        }
        return tasks;
    }
}
