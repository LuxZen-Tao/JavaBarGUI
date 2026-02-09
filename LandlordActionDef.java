public class LandlordActionDef {
    private final LandlordActionId id;
    private final int tier;
    private final LandlordActionCategory category;
    private final String name;
    private final String description;
    private final double baseChance;
    private final int cooldownRounds;
    private final LandlordActionEffectRange successRange;
    private final LandlordActionEffectRange failureRange;
    private final int successTrafficRounds;
    private final int failureTrafficRounds;

    public LandlordActionDef(LandlordActionId id,
                             int tier,
                             LandlordActionCategory category,
                             String name,
                             String description,
                             double baseChance,
                             int cooldownRounds,
                             LandlordActionEffectRange successRange,
                             LandlordActionEffectRange failureRange,
                             int successTrafficRounds,
                             int failureTrafficRounds) {
        this.id = id;
        this.tier = tier;
        this.category = category;
        this.name = name;
        this.description = description;
        this.baseChance = baseChance;
        this.cooldownRounds = cooldownRounds;
        this.successRange = successRange;
        this.failureRange = failureRange;
        this.successTrafficRounds = successTrafficRounds;
        this.failureTrafficRounds = failureTrafficRounds;
    }

    public LandlordActionId getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public LandlordActionCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getBaseChance() {
        return baseChance;
    }

    public int getCooldownRounds() {
        return cooldownRounds;
    }

    public LandlordActionEffectRange getSuccessRange() {
        return successRange;
    }

    public LandlordActionEffectRange getFailureRange() {
        return failureRange;
    }

    public int getSuccessTrafficRounds() {
        return successTrafficRounds;
    }

    public int getFailureTrafficRounds() {
        return failureTrafficRounds;
    }

    public String formatSuccessSummary() {
        return formatRangeSummary(successRange, successTrafficRounds);
    }

    public String formatFailureSummary() {
        return formatRangeSummary(failureRange, failureTrafficRounds);
    }

    private String formatRangeSummary(LandlordActionEffectRange range, int trafficRounds) {
        StringBuilder sb = new StringBuilder();
        appendRange(sb, "Rep", range.repMin(), range.repMax());
        appendRange(sb, "Morale", range.moraleMin(), range.moraleMax());
        if (range.trafficMinPct() != 0.0 || range.trafficMaxPct() != 0.0) {
            int min = (int) Math.round(range.trafficMinPct() * 100);
            int max = (int) Math.round(range.trafficMaxPct() * 100);
            appendRange(sb, "Traffic", min, max);
            if (trafficRounds > 0) {
                sb.append(" (").append(trafficRounds).append("r)");
            }
        }
        if (range.chaosMin() != 0.0 || range.chaosMax() != 0.0) {
            int min = (int) Math.round(range.chaosMin());
            int max = (int) Math.round(range.chaosMax());
            appendRange(sb, "Chaos", min, max);
        }
        return sb.toString();
    }

    private void appendRange(StringBuilder sb, String label, int min, int max) {
        if (sb.length() > 0) sb.append(", ");
        String minText = formatSigned(min);
        String maxText = formatSigned(max);
        sb.append(label).append(" ").append(minText);
        if (min != max) {
            sb.append("..").append(maxText);
        }
    }

    private String formatSigned(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }
}
