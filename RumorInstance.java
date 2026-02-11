public record RumorInstance(
        Rumor type,
        RumorSource source,
        RumorTruth truth,
        int intensity,
        double spreadRate,
        int daysRemaining
) implements java.io.Serializable {
    public double trafficMultiplier() {
        double normalized = intensity / 100.0;
        return 1.0 + type.trafficImpact(normalized);
    }

    public double wealthBias() {
        double normalized = intensity / 100.0;
        return type.wealthImpact(normalized);
    }

    public double eventBias() {
        double normalized = intensity / 100.0;
        return type.eventImpact(normalized);
    }

    public double repDrift() {
        double normalized = intensity / 100.0;
        return type.repImpact(normalized);
    }

    public String describe() {
        return type.getLabel() + " (" + truth.name().toLowerCase().replace('_', ' ')
                + ", " + intensity + "%, " + daysRemaining + "d)";
    }
}
