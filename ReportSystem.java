public class ReportSystem {

    private ReportSystem() {}

    public static String buildReportText(GameState s) {
        double profit = s.reportRevenue - s.reportCosts;

        double cashDelta = s.cash - s.reportStartCash;
        double debtDelta = s.debt - s.reportStartDebt;

        StringBuilder sb = new StringBuilder();
        sb.append("REPORT #").append(s.reportIndex)
                .append("  (week ").append(s.weeksIntoReport + 1).append("/4)\n\n");

        sb.append("Revenue: ").append(fmt2(s.reportRevenue)).append("\n");
        sb.append("Costs:   ").append(fmt2(s.reportCosts)).append("\n");
        sb.append("Profit:  ").append(fmt2(profit)).append("\n\n");

        // cost breakdown
        double rentC = s.reportCost(CostTag.RENT);
        double wagesC = s.reportCost(CostTag.WAGES);
        double opC = s.reportCost(CostTag.OPERATING);
        double foodC = s.reportCost(CostTag.FOOD);
        double supplierC = s.reportCost(CostTag.SUPPLIER);
        double upC = s.reportCost(CostTag.UPGRADE);
        double actC = s.reportCost(CostTag.ACTIVITY);
        double secC = s.reportCost(CostTag.SECURITY);
        double bouncerC = s.reportCost(CostTag.BOUNCER);
        double eventC = s.reportCost(CostTag.EVENT);
        double otherC = s.reportCost(CostTag.OTHER);

        sb.append("Costs breakdown:\n");
        sb.append("  Rent:       ").append(fmt2(rentC)).append("\n");
        sb.append("  Wages:      ").append(fmt2(wagesC)).append("\n");
        sb.append("  Operating:  ").append(fmt2(opC)).append("\n");
        sb.append("  Food:       ").append(fmt2(foodC)).append("\n");
        sb.append("  Supplier:   ").append(fmt2(supplierC)).append("\n");
        sb.append("  Upgrades:   ").append(fmt2(upC)).append("\n");
        sb.append("  Activities: ").append(fmt2(actC)).append("\n");
        sb.append("  Security:   ").append(fmt2(secC)).append("\n");
        sb.append("  Bouncer:    ").append(fmt2(bouncerC)).append("\n");
        sb.append("  Events:     ").append(fmt2(eventC)).append("\n");
        if (otherC > 0) sb.append("  Other:      ").append(fmt2(otherC)).append("\n");
        sb.append("\n");

        sb.append("Sales:   ").append(s.reportSales).append("\n");
        sb.append("Events:  ").append(s.reportEvents).append("\n\n");

        sb.append("Cash start: ").append(fmt2(s.reportStartCash)).append("\n");
        sb.append("Cash now:   ").append(fmt2(s.cash)).append(" ( ").append(fmt2(cashDelta)).append(")\n");
        sb.append("Debt start: ").append(fmt2(s.reportStartDebt)).append("\n");
        sb.append("Debt now:   ").append(fmt2(s.debt)).append(" ( ").append(fmt2(debtDelta)).append(")\n\n");

        sb.append("Rent accrued: ").append(fmt2(s.rentAccruedThisWeek))
                .append(" / ").append(fmt2(s.weeklyRent)).append("\n");
        sb.append("Security level: ").append(s.baseSecurityLevel).append("\n");
        double dailySecurity = s.baseSecurityLevel * SecuritySystem.SECURITY_UPKEEP_PER_LEVEL;
        sb.append("Security upkeep (daily): ").append(fmt2(dailySecurity)).append("\n");
        sb.append("Security upkeep accrued: ").append(fmt2(s.securityUpkeepAccruedThisWeek)).append("\n");
        sb.append("Wages accrued (this week): ").append(fmt2(s.wagesAccruedThisWeek)).append("\n");
        sb.append("Invoice due (weekend): ").append(fmt2(s.invoiceDueNow(s.wagesAccruedThisWeek))).append("\n");
        sb.append("Refunds this week: ").append(fmt2(s.weekRefundTotal)).append("\n");
        sb.append("Operating cost breakdown: base ").append(fmt2(s.opCostBaseThisWeek))
                .append(" | staff ").append(fmt2(s.opCostStaffThisWeek))
                .append(" | skill ").append(fmt2(s.opCostSkillThisWeek))
                .append(" | occupancy ").append(fmt2(s.opCostOccupancyThisWeek)).append("\n");
        sb.append("Team morale: ").append((int)Math.round(s.teamMorale)).append("\n");
        sb.append("Loss reports streak: ").append(s.consecutiveDebtReports).append("\n\n");

        sb.append("Between-night event: ").append(s.lastBetweenNightEventSummary).append("\n\n");

        sb.append("NIGHT\n");
        sb.append("Revenue:  ").append(fmt2(s.nightRevenue)).append("\n");
        sb.append("Sales:    ").append(s.nightSales).append("\n");
        sb.append("Unserved: ").append(s.nightUnserved).append("\n");
        sb.append("Food misses: ").append(s.nightFoodUnserved).append("\n");
        sb.append("Kicked:   ").append(s.nightKickedOut).append("\n");
        sb.append("Underage: ").append(s.nightRefusedUnderage).append("\n");
        sb.append("Events:   ").append(s.nightEvents).append("\n");
        sb.append("Refunds:  ").append(fmt2(s.nightRefundTotal)).append("\n");
        sb.append("Bar:      ").append(s.nightPunters.size()).append("/").append(s.maxBarOccupancy).append("\n");
        if (s.kitchenUnlocked) {
            sb.append("Food spoiled (last night): ").append(s.foodSpoiledLastNight).append("\n");
        }

        // Tiny tycoon sauce: simple health hint
        if (s.debt > 0 && profit < 0) sb.append("\n Warning: Debt + loss combo. Cut costs or raise prices.\n");
        if (s.reputation <= -60) sb.append("\n Reputation is radioactive. Expect chaos + theft.\n");
        if (s.reputation >= 60) sb.append("\n Reputation is booming. You can charge more safely.\n");

        return sb.toString();
    }

    public static String buildWeeklyReportText(GameState s) {
        StringBuilder sb = new StringBuilder();
        sb.append("WEEKLY REPORT - Week ").append(s.weekCount).append("\n\n");
        sb.append("PUB IDENTITY\n");
        sb.append("Current identity: ").append(s.currentIdentity != null ? s.currentIdentity.name() : "UNKNOWN")
                .append(" ").append(s.identityDrift).append("\n");
        if (s.weeklyIdentityFlavorText != null && !s.weeklyIdentityFlavorText.isBlank()) {
            sb.append(s.weeklyIdentityFlavorText).append("\n");
        }
        if (s.identityDriftSummary != null && !s.identityDriftSummary.isBlank()) {
            sb.append(s.identityDriftSummary).append("\n");
        }
        sb.append("Press tone: ").append(pressTone(s)).append("\n");
        sb.append("Traffic modifiers: identity x").append(fmt2(identityTrafficMult(s)))
                .append(" | rumors x").append(fmt2(rumorTrafficMult(s)))
                .append(" | level x").append(fmt2(1.0 + s.pubLevelTrafficBonusPct)).append("\n");
        sb.append("Active rumors:\n");
        for (String line : summarizeRumors(s, 4)) {
            sb.append("  - ").append(line).append("\n");
        }
        sb.append("\n");
        sb.append(buildReportText(s));
        return sb.toString();
    }

    public static String buildFourWeekSummary(GameState s) {
        double profit = s.reportRevenue - s.reportCosts;
        StringBuilder sb = new StringBuilder();
        sb.append("4-WEEK SUMMARY - Report #").append(s.reportIndex).append("\n\n");
        sb.append("Revenue: ").append(fmt2(s.reportRevenue)).append("\n");
        sb.append("Costs:   ").append(fmt2(s.reportCosts)).append("\n");
        sb.append("Profit:  ").append(fmt2(profit)).append("\n\n");
        sb.append("Refunds: ").append(fmt2(s.reportRefundTotal)).append("\n\n");
        sb.append("Invoice due (current): ").append(fmt2(s.invoiceDueNow(s.wagesAccruedThisWeek))).append("\n\n");

        sb.append("Costs breakdown:\n");
        sb.append("  Rent:       ").append(fmt2(s.reportCost(CostTag.RENT))).append("\n");
        sb.append("  Wages:      ").append(fmt2(s.reportCost(CostTag.WAGES))).append("\n");
        sb.append("  Operating:  ").append(fmt2(s.reportCost(CostTag.OPERATING))).append("\n");
        sb.append("  Food:       ").append(fmt2(s.reportCost(CostTag.FOOD))).append("\n");
        sb.append("  Security:   ").append(fmt2(s.reportCost(CostTag.SECURITY))).append("\n");
        sb.append("  Supplier:   ").append(fmt2(s.reportCost(CostTag.SUPPLIER))).append("\n");
        sb.append("  Upgrades:   ").append(fmt2(s.reportCost(CostTag.UPGRADE))).append("\n");
        sb.append("  Activities: ").append(fmt2(s.reportCost(CostTag.ACTIVITY))).append("\n");
        sb.append("  Bouncer:    ").append(fmt2(s.reportCost(CostTag.BOUNCER))).append("\n");
        sb.append("  Events:     ").append(fmt2(s.reportCost(CostTag.EVENT))).append("\n");
        sb.append("  Interest:   ").append(fmt2(s.reportCost(CostTag.INTEREST))).append("\n");
        double other = s.reportCost(CostTag.OTHER);
        if (other > 0) sb.append("  Other:      ").append(fmt2(other)).append("\n");
        sb.append("\nSales:   ").append(s.reportSales).append("\n");
        sb.append("Events:  ").append(s.reportEvents).append("\n");

        return sb.toString();
    }




    private static String fmt2(double d){return String.format("%.2f",d);} 

    private static double identityTrafficMult(GameState s){
        if(s.pubIdentity==null) return 1.0;
        return switch(s.pubIdentity){
            case NEUTRAL->1.0; case RESPECTABLE->1.06; case ROWDY->1.08; case ARTSY->1.04;
            case SHADY->0.94; case FAMILY_FRIENDLY->1.05; case UNDERGROUND->1.00;
        };
    }

    private static double rumorTrafficMult(GameState s){
        double mult=1.0;
        mult -= s.rumorHeat.getOrDefault(Rumor.WATERED_DOWN_DRINKS,0)*0.002;
        mult -= s.rumorHeat.getOrDefault(Rumor.FIGHTS_EVERY_WEEKEND,0)*0.0025;
        mult += s.rumorHeat.getOrDefault(Rumor.BEST_SUNDAY_ROAST,0)*0.002;
        mult -= s.rumorHeat.getOrDefault(Rumor.FOOD_POISONING_SCARE,0)*0.002;
        mult -= s.rumorHeat.getOrDefault(Rumor.SLOW_SERVICE,0)*0.002;
        mult += s.rumorHeat.getOrDefault(Rumor.FRIENDLY_STAFF,0)*0.002;
        mult += s.rumorHeat.getOrDefault(Rumor.GREAT_ATMOSPHERE,0)*0.002;
        return Math.max(0.80,Math.min(1.20,mult));
    }

    private static String pressTone(GameState s){
        double tone = s.currentIdentity!=null? s.currentIdentity.getPressToneBias():0.0;
        if(tone>=0.25) return "Positive";
        if(tone<=-0.25) return "Negative";
        return "Neutral";
    }

    private static java.util.List<String> summarizeRumors(GameState s,int limit){
        java.util.List<Rumor> sorted=new java.util.ArrayList<>(java.util.Arrays.asList(Rumor.values()));
        sorted.sort((a,b)->Integer.compare(s.rumorHeat.getOrDefault(b,0),s.rumorHeat.getOrDefault(a,0)));
        java.util.List<String> out=new java.util.ArrayList<>();
        int n=Math.min(limit,sorted.size());
        for(int i=0;i<n;i++){
            Rumor r=sorted.get(i);
            int heat=s.rumorHeat.getOrDefault(r,0);
            if(heat<=0) continue;
            out.add(r.name().replace('_',' ') + " ("+heat+")");
        }
        if(out.isEmpty()) out.add("None");
        return out;
    }
}
