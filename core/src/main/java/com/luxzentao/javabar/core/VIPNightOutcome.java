package com.luxzentao.javabar.core;

public record VIPNightOutcome(
        int unservedCount,
        int fightCount,
        int eventCount,
        int refundCount,
        double priceMultiplier,
        double foodQualitySignal
) {
}
