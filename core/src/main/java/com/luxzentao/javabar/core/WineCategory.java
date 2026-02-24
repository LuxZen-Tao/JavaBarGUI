package com.luxzentao.javabar.core;

public enum WineCategory {
    CHEAP_HOUSE("Cheap house"),
    MID_TIER_CLASSIC("Mid-tier classic"),
    PREMIUM_BOTTLE("Premium bottle"),
    NICHE_REGIONAL("Niche/regional");

    private final String label;

    WineCategory(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
