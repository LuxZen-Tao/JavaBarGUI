import java.util.List;
import java.util.Random;

public class SupplierSystem {

    private final GameState s;

    public SupplierSystem(GameState s) {
        this.s = s;
    }

    /** Call this at the start of each week (or day if you prefer). */
    public void rollNewDeal() {
        if (s.supplier == null || s.supplier.isEmpty()) {
            s.supplierDeal = SupplierDeal.none();
            return;
        }

        Random r = s.random;
        int tier = Math.max(0, s.debtSpiralTier);

        int noDealChance = 35 + (tier * 6) + (s.bankruptcySupplierStigma ? 12 : 0);
        if (r.nextInt(100) < Math.min(85, noDealChance)) {
            s.supplierDeal = SupplierDeal.none();
            return;
        }

        Wine target = s.supplier.get(r.nextInt(s.supplier.size()));

        int discountChance = 50 - (tier * 10) - (s.bankruptcySupplierStigma ? 20 : 0);
        boolean discount = r.nextInt(100) < Math.max(5, discountChance);

        if (discount) {
            // 20%..60% off
            double mult = 0.40 + (r.nextInt(21) / 100.0); // 0.40..0.60
            s.supplierDeal = new SupplierDeal(
                    SupplierDeal.Type.DISCOUNT,
                    target,
                    mult,
                    " DEAL: " + target.getName() + " is " + (int)((1 - mult) * 100) + "% OFF!"
            );
        } else {
            // 20%..70% price increase (shortage)
            double mult = 1.20 + (r.nextInt(51) / 100.0); // 1.20..1.70
            s.supplierDeal = new SupplierDeal(
                    SupplierDeal.Type.SHORTAGE,
                    target,
                    mult,
                    " SHORTAGE: " + target.getName() + " costs +" + (int)((mult - 1) * 100) + "%!"
            );
        }
    }

    /** Cost used when buying from supplier. Includes rep pricing + deal pricing. */
    public double supplierBuyCost(Wine w, double repMult) {
        double base = w.getBaseCost() * repMult;
        double cost = (s.supplierDeal == null) ? base : s.supplierDeal.applyToCost(w, base);
        cost *= seasonalSupplierPriceMultiplier();
        if (s.premiumSupplierCatalogUnlocked && w.getCategory() == WineCategory.PREMIUM_BOTTLE) {
            cost *= 0.94;
        }
        return cost * s.supplierPriceMultiplier();
    }

    /** Total cost for buying a quantity, with a tiny bulk discount (tycoon-friendly, not broken). */
    public double supplierBuyCost(Wine w, double repMult, int qty) {
        qty = Math.max(1, qty);
        double each = supplierBuyCost(w, repMult);

        // bulk discount (applied to whole basket)
        double disc = bulkDiscountPct(qty);
        double total = each * qty;
        return total * (1.0 - disc);
    }


    double seasonalSupplierPriceMultiplier() {
        if (!FeatureFlags.FEATURE_SEASONS) return 1.0;

        List<SeasonTag> tags = new SeasonCalendar(s).getActiveSeasonTags();
        if (tags.isEmpty()) return 1.0;

        double mult = 1.0;
        for (SeasonTag tag : tags) {
            switch (tag) {
                case TOURIST_WAVE -> mult *= 1.02;
                case EXAM_SEASON -> mult *= 1.01;
                case WINTER_SLUMP -> mult *= 0.98;
                case DERBY_WEEK -> mult *= 1.01;
            }
        }
        return mult;
    }

    public double bulkDiscountPct(int qty) {
        if (qty >= 50) return 0.06;
        if (qty >= 25) return 0.04;
        if (qty >= 10) return 0.025;
        if (qty >= 5) return 0.015;
        return 0.0;
    }

    public String dealLabel() {
        if (s.supplierDeal == null) return "No supplier deal today.";
        return s.supplierDeal.getLabel();
    }
}
