public enum Bank {
    TOWNLAND("Bank of Townland", 1500, 3000, 0.05, 0.08, 0, "Conservative, forgiving"),
    SANTNERE("Santnere", 2000, 4000, 0.07, 0.10, 0, "Flexible, slightly riskier"),
    BOYD_MSG("Boyd MSG", 3500, 6000, 0.06, 0.09, 580, "Structured, reputation-sensitive"),
    HALIFIX("Halifix", 4000, 8000, 0.05, 0.07, 620, "Legacy lender"),
    ROYAL_POUND("Royal Pound of Scotland", 6000, 12000, 0.04, 0.06, 680, "Premier terms"),
    UNION_ALBION("Union of Albion", 10000, 20000, 0.03, 0.05, 740, "Elite access");

    private final String name;
    private final int minLimit;
    private final int maxLimit;
    private final double minApr;
    private final double maxApr;
    private final int minScore;
    private final String personality;

    Bank(String name,
         int minLimit,
         int maxLimit,
         double minApr,
         double maxApr,
         int minScore,
         String personality) {
        this.name = name;
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
        this.minApr = minApr;
        this.maxApr = maxApr;
        this.minScore = minScore;
        this.personality = personality;
    }

    public String getName() { return name; }
    public int getMinLimit() { return minLimit; }
    public int getMaxLimit() { return maxLimit; }
    public double getMinApr() { return minApr; }
    public double getMaxApr() { return maxApr; }
    public int getMinScore() { return minScore; }
    public String getPersonality() { return personality; }

    public boolean isUnlocked(int creditScore) {
        return creditScore >= minScore;
    }

    public double rollLimit(java.util.Random random) {
        if (minLimit == maxLimit) return minLimit;
        return minLimit + random.nextInt(maxLimit - minLimit + 1);
    }

    public double rollApr(java.util.Random random) {
        if (minApr == maxApr) return minApr;
        return minApr + (random.nextDouble() * (maxApr - minApr));
    }
}
