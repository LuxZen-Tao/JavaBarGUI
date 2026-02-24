package com.luxzentao.javabar.core;

public class ActivitySystem {

    private final GameState s;

    public ActivitySystem(GameState s) { this.s = s; }

    public boolean hasActivity() { return s.activityTonight != null; }

    public double identityMultiplier(PubActivity activity) {
        if (activity == null || activity.getRequiredIdentity() == null) return 1.0;
        return s.currentIdentity == activity.getRequiredIdentity() ? 1.10 : 0.94;
    }

    public double levelMultiplier(int level) {
        int safeLevel = Math.max(1, level);
        return Math.min(1.60, 1.0 + ((safeLevel - 1) * 0.12));
    }

    public double effectiveStrengthMultiplier(PubActivity activity) {
        if (activity == null) return 1.0;
        return levelMultiplier(s.pubLevel) * identityMultiplier(activity);
    }

    public double effectiveTrafficBonusPct(PubActivity activity) {
        if (activity == null) return 0.0;
        return activity.getTrafficBonusPct() * effectiveStrengthMultiplier(activity);
    }

    public double trafficMultiplier() {
        return (s.activityTonight == null) ? 1.0 : (1.0 + effectiveTrafficBonusPct(s.activityTonight));
    }

    public int eventBonusChance() {
        if (s.activityTonight == null) return 0;
        return (int)Math.round(s.activityTonight.getEventBonusChance() * effectiveStrengthMultiplier(s.activityTonight));
    }

    public double riskBonusPct() {
        return (s.activityTonight == null) ? 0.0 : (s.activityTonight.getRiskBonusPct() * effectiveStrengthMultiplier(s.activityTonight));
    }

    public double tipBonusPct() {
        return (s.activityTonight == null) ? 0.0 : (s.activityTonight.getTipBonusPct() * effectiveStrengthMultiplier(s.activityTonight));
    }

    public double priceMultiplierPct() {
        return (s.activityTonight == null) ? 0.0 : (s.activityTonight.getPriceMultiplierPct() * effectiveStrengthMultiplier(s.activityTonight));
    }
}
