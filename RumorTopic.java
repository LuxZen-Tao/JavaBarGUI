public enum RumorTopic {
    STAFF_GOSSIP("Staff gossip", RumorSource.STAFF),
    STAFF_THEFT("Theft accusations", RumorSource.STAFF),
    STAFF_FAVORITISM("Favoritism rumors", RumorSource.STAFF),
    FOOD_QUALITY("Food quality", RumorSource.PUNTERS),
    PRICE_FAIRNESS("Price fairness", RumorSource.PUNTERS),
    SAFETY_FIGHTS("Safety & fights", RumorSource.PUNTERS);

    private final String label;
    private final RumorSource source;

    RumorTopic(String label, RumorSource source) {
        this.label = label;
        this.source = source;
    }

    public String getLabel() { return label; }
    public RumorSource getSource() { return source; }
}
