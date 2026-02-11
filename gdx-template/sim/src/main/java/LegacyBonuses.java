public class LegacyBonuses  implements java.io.Serializable {

    public int inventoryCapBonus = 0;
    public int innRoomBonus = 0;
    public double trafficMultiplierBonus = 0.0;
    public int supplierTradeCreditBonus = 0;
    public int baseSecurityBonus = 0;
    public double staffEfficiencyBonus = 0.0;

    public LegacyBonuses() {
    }

    public LegacyBonuses(int inventoryCapBonus,
                         int innRoomBonus,
                         double trafficMultiplierBonus,
                         int supplierTradeCreditBonus,
                         int baseSecurityBonus,
                         double staffEfficiencyBonus) {
        this.inventoryCapBonus = Math.max(0, inventoryCapBonus);
        this.innRoomBonus = Math.max(0, innRoomBonus);
        this.trafficMultiplierBonus = Math.max(0.0, trafficMultiplierBonus);
        this.supplierTradeCreditBonus = Math.max(0, supplierTradeCreditBonus);
        this.baseSecurityBonus = Math.max(0, baseSecurityBonus);
        this.staffEfficiencyBonus = Math.max(0.0, staffEfficiencyBonus);
    }

    public LegacyBonuses copy() {
        return new LegacyBonuses(
                inventoryCapBonus,
                innRoomBonus,
                trafficMultiplierBonus,
                supplierTradeCreditBonus,
                baseSecurityBonus,
                staffEfficiencyBonus
        );
    }

    public void add(LegacyBonuses other) {
        if (other == null) return;
        inventoryCapBonus += Math.max(0, other.inventoryCapBonus);
        innRoomBonus += Math.max(0, other.innRoomBonus);
        trafficMultiplierBonus += Math.max(0.0, other.trafficMultiplierBonus);
        supplierTradeCreditBonus += Math.max(0, other.supplierTradeCreditBonus);
        baseSecurityBonus += Math.max(0, other.baseSecurityBonus);
        staffEfficiencyBonus += Math.max(0.0, other.staffEfficiencyBonus);
    }

    public LegacyBonuses scaled(double factor) {
        double safe = Math.max(0.0, factor);
        return new LegacyBonuses(
                (int) Math.round(inventoryCapBonus * safe),
                (int) Math.round(innRoomBonus * safe),
                trafficMultiplierBonus * safe,
                (int) Math.round(supplierTradeCreditBonus * safe),
                (int) Math.round(baseSecurityBonus * safe),
                staffEfficiencyBonus * safe
        );
    }

    public boolean isEmpty() {
        return inventoryCapBonus <= 0
                && innRoomBonus <= 0
                && trafficMultiplierBonus <= 0.00001
                && supplierTradeCreditBonus <= 0
                && baseSecurityBonus <= 0
                && staffEfficiencyBonus <= 0.00001;
    }

    public String summaryLine() {
        return "Inventory +" + inventoryCapBonus
                + " | Inn rooms +" + innRoomBonus
                + " | Traffic +" + pct(trafficMultiplierBonus)
                + " | Trade credit +" + supplierTradeCreditBonus
                + " | Base security +" + baseSecurityBonus
                + " | Staff efficiency +" + pct(staffEfficiencyBonus);
    }

    public java.util.List<String> detailLines() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("Inventory cap bonus: +" + inventoryCapBonus);
        lines.add("Inn room bonus: +" + innRoomBonus);
        lines.add("Traffic multiplier bonus: +" + pct(trafficMultiplierBonus));
        lines.add("Supplier trade credit bonus: +" + supplierTradeCreditBonus);
        lines.add("Base security bonus: +" + baseSecurityBonus);
        lines.add("Staff efficiency bonus: +" + pct(staffEfficiencyBonus));
        return lines;
    }

    private static String pct(double v) {
        return String.format("%.1f%%", v * 100.0);
    }
}
