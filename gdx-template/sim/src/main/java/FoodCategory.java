public enum FoodCategory {
    CHEAP_BAR_FOOD("Cheap bar food"),
    MID_QUALITY_MEAL("Mid-quality meal"),
    PREMIUM_DISH("Premium dish");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
