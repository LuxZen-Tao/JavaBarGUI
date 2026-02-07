public class ActivitySystem {

    private final GameState s;

    public ActivitySystem(GameState s) { this.s = s; }

    public boolean hasActivity() { return s.activityTonight != null; }

    public double trafficMultiplier() {
        return (s.activityTonight == null) ? 1.0 : (1.0 + s.activityTonight.getTrafficBonusPct());
    }

    public int eventBonusChance() {
        return (s.activityTonight == null) ? 0 : s.activityTonight.getEventBonusChance();
    }

    public double riskBonusPct() {
        return (s.activityTonight == null) ? 0.0 : s.activityTonight.getRiskBonusPct();
    }

    public double tipBonusPct() {
        return (s.activityTonight == null) ? 0.0 : s.activityTonight.getTipBonusPct();
    }

    public double priceMultiplierPct() {
        return (s.activityTonight == null) ? 0.0 : s.activityTonight.getPriceMultiplierPct();
    }
}
