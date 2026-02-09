public enum LandlordActionCategory {
    CLASSY("Classy"),
    BALANCED("Balanced"),
    SHADY("Shady");

    private final String label;

    LandlordActionCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
