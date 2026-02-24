package com.luxzentao.javabar.core;

import java.util.*;

public class InventorySystem {

    private final GameState s;

    public InventorySystem(GameState s) { this.s = s; }

    /** Supplier buy cost multiplier based on reputation (clamped to sane bounds). */
    public double repToSupplierCostMultiplier() {
        double mult;
        if (s.reputation <= 0) mult = 1.0 + (-s.reputation) * 0.003;
        else mult = 1.0 - s.reputation * 0.0025;

        // Prevent supplier pays you or infinite markup nonsense
        return clamp(mult, 0.65, 1.60);
    }

    public double sellPrice(Wine w, double effectiveMult) {
        return s.rack.getSellPrice(w, effectiveMult);
    }

    public Wine cheapestWine(double effectiveMult) {
        List<Wine> bottles = s.rack.getWinesSnapshot();
        if (bottles.isEmpty()) return null;

        Wine best = null;
        double bestPrice = Double.MAX_VALUE;
        for (Wine w : bottles) {
            double p = sellPrice(w, effectiveMult);
            if (p < bestPrice) { bestPrice = p; best = w; }
        }
        return best;
    }

    /** Slightly more "tycoon": biased toward mid-range bottles rather than pure uniform chaos. */
    public Wine randomWine() {
        List<Wine> bottles = s.rack.getWinesSnapshot();
        if (bottles.isEmpty()) return null;

        // Pick 2 randoms and take the one with higher base price 55% of time.
        Wine a = bottles.get(s.random.nextInt(bottles.size()));
        Wine b = bottles.get(s.random.nextInt(bottles.size()));
        if (s.random.nextInt(100) < 55) return (a.getBasePrice() >= b.getBasePrice()) ? a : b;
        return (a.getBasePrice() < b.getBasePrice()) ? a : b;
    }

    public Wine randomWineForTier(Punter.Tier tier) {
        List<Wine> bottles = s.rack.getWinesSnapshot();
        if (bottles.isEmpty()) return null;

        List<Wine> weighted = new ArrayList<>();
        for (Wine w : bottles) {
            int weight = 1;
            if (w.getTargetTier() == tier) weight += 3;
            if (tier == Punter.Tier.BIG_SPENDER && w.getBasePrice() > 25) weight += 2;
            if (tier == Punter.Tier.LOWLIFE && w.getBasePrice() < 10) weight += 2;
            for (int i = 0; i < weight; i++) weighted.add(w);
        }
        return weighted.get(s.random.nextInt(weighted.size()));
    }

    public Wine randomCheaperThan(double ceiling, double mult) {
        List<Wine> bottles = s.rack.getWinesSnapshot();
        if (bottles.isEmpty()) return null;

        List<Wine> cheaper = new ArrayList<>();
        for (Wine w : bottles) {
            if (sellPrice(w, mult) < ceiling) cheaper.add(w);
        }
        if (cheaper.isEmpty()) return null;
        return cheaper.get(s.random.nextInt(cheaper.size()));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
