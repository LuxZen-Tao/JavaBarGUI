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

    public HudSimBridge(Simulation sim, GameState state, SimEventBus eventBus) {
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
    }

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
        return String.format(Locale.US, "Supplier £%.2f | Wages £%.2f | Rent £%.2f", due.supplierDue(), due.wagesDue(), due.rentDue());
    }

    public String costsSummaryLineB() {
        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        return String.format(Locale.US, "Security £%.2f | Credit £%.2f | Shark £%.2f", due.securityDue(), due.creditDue(), due.sharkDue());
    }

    public String reportLine() {
        return "Report #" + state.reportIndex + " (week " + Math.max(1, state.weeksIntoReport + 1)
                + "/4) | profit " + money(state.reportRevenue - state.reportCosts)
                + " | sales " + state.reportSales + " | events " + state.reportEvents;
    }

    public String policyLine() {
        String task = state.activeSecurityTask == null ? "None" : state.activeSecurityTask.getLabel();
        return "Policy " + state.securityPolicy.getShortLabel() + " | Task " + task
                + " | Sec " + sim.securityBreakdown().effectiveSecurity()
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
                + " | BOH " + state.bohStaff.size() + "/" + state.bohStaffCap;
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
        if (!state.supplier.isEmpty()) sim.buyFromSupplier(state.supplier.get(0), 1);
        else eventBus.fireLog("[TODO] Supplier purchase UI not available yet.");
    }

    public void commandFoodSupplier() {
        if (!state.foodSupplier.isEmpty()) sim.buyFoodFromSupplier(state.foodSupplier.get(0), 1);
        else eventBus.fireLog("[TODO] Food supplier purchase UI not available yet.");
    }

    public void commandPayDebt() {
        if (state.creditLines.all().isEmpty()) {
            eventBus.fireLog("[TODO] No bank credit line to repay.");
            return;
        }
        sim.repayCreditLineInFull(state.creditLines.all().get(0).id());
    }

    public void commandLoanShark() {
        if (state.loanShark.isOpen()) eventBus.fireLog("[TODO] Loan shark repayment flow not yet in HUD.");
        else sim.openSharkLine();
    }

    public void commandStaff() { eventBus.fireLog("[TODO] Staff roster modal hook pending."); }
    public void commandInn() { eventBus.fireLog("[TODO] Inn management modal hook pending."); }
    public void commandUpgrades() { eventBus.fireLog("[TODO] Upgrade browser modal hook pending."); }
    public void commandSecurity() { sim.setSecurityPolicy(SecurityPolicy.STRICT_DOOR); }
    public void commandActivities() { eventBus.fireLog("[TODO] Activities picker modal hook pending."); }
    public void commandActions() { eventBus.fireLog("[TODO] Landlord actions modal hook pending."); }
    public void commandAuto(boolean enabled) { eventBus.fireLog("[TODO] Automation toggled: " + enabled); }
    public void commandOptions() { eventBus.fireLog("[TODO] Options modal hook pending."); }

    private String money(double value) { return String.format(Locale.US, "£%,.2f", value); }
}
