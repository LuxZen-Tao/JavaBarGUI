package com.luxzentao.javabar.core.bridge;

import com.luxzentao.javabar.core.*;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HudSimBridge {
    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;

    private Runnable supplierHook;
    private Runnable foodSupplierHook;
    private Runnable bankHook;
    private Runnable loanSharkHook;
    private Runnable staffHook;
    private Runnable innHook;
    private Runnable upgradesHook;
    private Runnable securityHook;
    private Runnable activitiesHook;
    private Runnable actionsHook;

    public HudSimBridge(Simulation sim, GameState state, SimEventBus eventBus) {
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
    }

    public void setSupplierHook(Runnable r)      { this.supplierHook = r; }
    public void setFoodSupplierHook(Runnable r)  { this.foodSupplierHook = r; }
    public void setBankHook(Runnable r)           { this.bankHook = r; }
    public void setLoanSharkHook(Runnable r)      { this.loanSharkHook = r; }
    public void setStaffHook(Runnable r)          { this.staffHook = r; }
    public void setInnHook(Runnable r)            { this.innHook = r; }
    public void setUpgradesHook(Runnable r)       { this.upgradesHook = r; }
    public void setSecurityHook(Runnable r)       { this.securityHook = r; }
    public void setActivitiesHook(Runnable r)     { this.activitiesHook = r; }
    public void setActionsHook(Runnable r)        { this.actionsHook = r; }

    public MetricsSnapshot metrics() { return sim.buildMetricsSnapshot(); }

    public String weekLine() {
        return "Week " + Math.max(1, state.weekCount) + " " + state.dayName() + " | Service " + state.nightCount;
    }

    public String calendarLine() {
        return "Date " + (state.dateString() == null ? "--" : state.dateString()) + " | Weather " + state.weatherLabel();
    }

    public String serviceLine() {
        return (state.nightOpen ? "Service OPEN" : "Service CLOSED")
                + " Round " + state.roundInNight + "/" + state.getClosingRound()
                + " | Bar " + state.nightPunters.size() + "/" + state.maxBarOccupancy
                + " | Time " + state.getCurrentTime()
                + " | Phase " + state.getCurrentPhase()
                + " | Music " + state.currentMusicProfile.getLabel();
    }

    public String costsSummaryLineA() {
        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        return String.format(Locale.US, "Supplier £%.2f | Wages £%.2f | Rent £%.2f", due.supplier(), due.wages(), due.rent());
    }

    public String costsSummaryLineB() {
        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        return String.format(Locale.US, "Security £%.2f | Credit £%.2f | Shark £%.2f", due.security(), due.creditLines(), due.loanShark());
    }

    public String reportLine() {
        return "Report #" + state.reportIndex + " (week " + Math.max(1, state.weeksIntoReport + 1)
                + "/4) | profit " + money(state.reportRevenue - state.reportCosts)
                + " | sales " + state.reportSales + " | events " + state.reportEvents;
    }

    public String policyLine() {
        String task = state.activeSecurityTask == null ? "None" : state.activeSecurityTask.getLabel();
        return "Policy " + state.securityPolicy.getShortLabel() + " | Task " + task
                + " | Sec " + sim.securityBreakdown().total()
                + " | Chaos " + String.format(Locale.US, "%.1f", state.chaos)
                + " | TS " + state.tradingStandardsCounter;
    }

    public String staffLine() {
        GameState.StaffSummary ss = state.staff();
        return "Staff " + ss.staffCount() + "/" + ss.staffCap() + " | Managers " + ss.managerPoolCount() + "/" + ss.managerCap()
                + " | Morale " + (int) Math.round(ss.teamMorale()) + " | Upgrades " + ss.upgradesOwned()
                + " | Serve cap " + sim.peekServeCapacity();
    }

    public String countsLine() {
        return "FOH " + state.fohStaffCount() + "/" + state.fohStaffCap
                + " | HOH " + state.hohStaffCount() + "/" + state.hohStaffCap
                + " | BOH " + state.bohStaff.size() + "/" + state.kitchenChefCap;
    }

    public String forecastLine() {
        return "In " + state.nightPunters.size() + " | Out " + state.nightKickedOut
                + " (natural " + state.lastNaturalDepartures + ") | " + state.trafficForecastLine;
    }

    public String topSellerLine() {
        if (state.nightItemSales.isEmpty()) return "Top sellers: --";
        String tops = state.nightItemSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(e -> e.getKey() + " x" + e.getValue())
                .collect(Collectors.joining(" | "));
        return "Top sellers: " + tops;
    }

    public String reportsLiveText() {
        return "Underage: " + state.tradingStandardsCounter
                + "\nEvents: " + state.nightEvents
                + "\nRefunds: " + String.format(Locale.US, "%.2f", state.nightRefundTotal)
                + "\nBar: " + state.nightPunters.size() + "/" + state.maxBarOccupancy
                + "\nFood spoiled (last night): " + state.foodSpoiledLastNight
                + "\nInn: " + state.roomsBookedLast + "/" + state.roomsTotal + " rooms booked"
                + "\nInn rep: " + String.format(Locale.US, "%.2f", state.innRep)
                + "\nInn events: " + state.innEventLog.size();
    }

    public String inventoryText() {
        StringBuilder sb = new StringBuilder();
        state.rack.inventoryCounts().forEach((name, qty) -> sb.append(name).append(" x").append(qty).append("\n"));
        sb.append("Total: ").append(state.rack.count()).append("/").append(state.rack.getCapacity()).append("\n\n=== Food ===\n");
        state.foodRack.inventoryCounts().forEach((name, qty) -> sb.append(name).append(" x").append(qty).append("\n"));
        sb.append("Total: ").append(state.foodRack.count()).append("/").append(state.foodRack.getCapacity()).append("\n\n=== Spoilage forecast ===\n");
        state.rack.spoilageForecast(state.absDayIndex()).forEach(line -> sb.append(line.wineName()).append(" x").append(line.count()).append(" - spoil in ").append(line.daysRemaining()).append(" days\n"));
        return sb.toString();
    }

    public String missionOverview() { return metrics().operations; }
    public String missionMilestones() { return metrics().progression + "\n\n" + metrics().prestige; }
    public String missionRisk() { return metrics().risk + "\n\n" + metrics().security; }
    public String missionEconomy() { return metrics().economy + "\n\n" + metrics().financeBanking; }
    public String missionStaff() { return metrics().staffDetail + "\n\n" + metrics().inn; }

    public void commandOpenPub() { sim.openNight(); }
    public void commandNextRound() { sim.playRound(); }
    public void commandCloseNight() { sim.closeNight("Closed by landlord."); }
    public void commandSetHappyHour(boolean on) { sim.toggleHappyHour(on); }
    public void commandSetPriceMultiplier(double multiplier) { sim.setPriceMultiplier(multiplier); }

    public void commandSupplier() {
        if (supplierHook != null) { supplierHook.run(); return; }
        if (!state.supplier.isEmpty()) sim.buyFromSupplier(state.supplier.get(0), 1);
        else eventBus.fireLog("[TODO] Supplier purchase UI not available yet.");
    }

    public void commandFoodSupplier() {
        if (foodSupplierHook != null) { foodSupplierHook.run(); return; }
        if (!state.foodSupplier.isEmpty()) sim.buyFoodFromSupplier(state.foodSupplier.get(0), 1);
        else eventBus.fireLog("[TODO] Food supplier purchase UI not available yet.");
    }

    public void commandPayDebt() {
        if (bankHook != null) { bankHook.run(); return; }
        if (state.creditLines.getOpenLines().isEmpty()) {
            eventBus.fireLog("[TODO] No bank credit line to repay.");
            return;
        }
        sim.repayCreditLineInFull(state.creditLines.getOpenLines().get(0).getId());
    }

    public void commandLoanShark() {
        if (loanSharkHook != null) { loanSharkHook.run(); return; }
        if (state.loanShark.isOpen()) eventBus.fireLog("[TODO] Loan shark repayment flow not yet in HUD.");
        else sim.openSharkLine();
    }

    public void commandStaff()      { if (staffHook      != null) staffHook.run();      else eventBus.fireLog("[TODO] Staff roster modal hook pending."); }
    public void commandInn()        { if (innHook        != null) innHook.run();        else eventBus.fireLog("[TODO] Inn management modal hook pending."); }
    public void commandUpgrades()   { if (upgradesHook   != null) upgradesHook.run();   else eventBus.fireLog("[TODO] Upgrade browser modal hook pending."); }
    public void commandSecurity()   { if (securityHook   != null) securityHook.run();   else sim.setSecurityPolicy(SecurityPolicy.STRICT_DOOR); }
    public void commandActivities() { if (activitiesHook != null) activitiesHook.run(); else eventBus.fireLog("[TODO] Activities picker modal hook pending."); }
    public void commandActions()    { if (actionsHook    != null) actionsHook.run();    else eventBus.fireLog("[TODO] Landlord actions modal hook pending."); }
    public void commandAuto(boolean enabled) { eventBus.fireLog("[TODO] Automation toggled: " + enabled); }
    public void commandOptions() { eventBus.fireLog("[TODO] Options modal hook pending."); }

    private String money(double value) { return String.format(Locale.US, "£%,.2f", value); }
}
