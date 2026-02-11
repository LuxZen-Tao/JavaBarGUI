public enum SecurityTaskCategory {
    SOFT("Soft"),
    BALANCED("Balanced"),
    STRICT("Strict");

    private final String label;

    SecurityTaskCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
