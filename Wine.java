// Wine.java
public class Wine  implements java.io.Serializable {
    private final String name;
    private final int year;
    private final String region;
    private final double baseCost;
    private final double basePrice;
    private final WineCategory category;
    private final double priceSensitivity;
    private final Punter.Tier targetTier;
    private final int spoilDays;

    public Wine(String name,
                int year,
                String region,
                double baseCost,
                double basePrice,
                WineCategory category,
                double priceSensitivity,
                Punter.Tier targetTier,
                int spoilDays) {
        this.name = name;
        this.year = year;
        this.region = region;
        this.baseCost = baseCost;
        this.basePrice = basePrice;
        this.category = category;
        this.priceSensitivity = Math.max(0.4, Math.min(1.6, priceSensitivity));
        this.targetTier = targetTier;
        this.spoilDays = Math.max(1, spoilDays);
    }

    public String getName() { return name; }
    public int getYear() { return year; }
    public String getRegion() { return region; }
    public double getBaseCost() { return baseCost; }
    public double getBasePrice() { return basePrice; }
    public WineCategory getCategory() { return category; }
    public double getPriceSensitivity() { return priceSensitivity; }
    public Punter.Tier getTargetTier() { return targetTier; }
    public int getSpoilDays() { return spoilDays; }

    @Override
    public String toString() {
        return name + " (" + year + ", " + region + ")"
                + " | cost " + String.format("%.2f", baseCost)
                + " | base " + String.format("%.2f", basePrice)
                + " | " + (category != null ? category.getLabel() : "uncategorized");
    }
}
