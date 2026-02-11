public class Food  implements java.io.Serializable {
    private final String name;
    private final double baseCost;
    private final double basePrice;
    private final int qualityTier;
    private final FoodCategory category;
    private final double priceSensitivity;
    private final int spoilDays;

    public Food(String name,
                double baseCost,
                double basePrice,
                int qualityTier,
                FoodCategory category,
                double priceSensitivity,
                int spoilDays) {
        this.name = name;
        this.baseCost = baseCost;
        this.basePrice = basePrice;
        this.qualityTier = Math.max(1, qualityTier);
        this.category = category;
        this.priceSensitivity = Math.max(0.4, Math.min(1.6, priceSensitivity));
        this.spoilDays = Math.max(1, spoilDays);
    }

    public String getName() { return name; }
    public double getBaseCost() { return baseCost; }
    public double getBasePrice() { return basePrice; }
    public int getQualityTier() { return qualityTier; }
    public FoodCategory getCategory() { return category; }
    public double getPriceSensitivity() { return priceSensitivity; }
    public int getSpoilDays() { return spoilDays; }

    @Override
    public String toString() {
        return name + " | cost " + String.format("%.2f", baseCost)
                + " | price " + String.format("%.2f", basePrice)
                + " | tier " + qualityTier
                + " | " + (category != null ? category.getLabel() : "uncategorized");
    }
}
