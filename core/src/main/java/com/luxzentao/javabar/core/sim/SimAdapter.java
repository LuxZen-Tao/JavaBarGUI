package com.luxzentao.javabar.core.sim;

import com.luxzentao.javabar.core.GameState;

public class SimAdapter {
    private final GameState state;
    private final SimEventBus eventBus;

    private Integer lastWeek;
    private Double lastCash;
    private Integer lastRep;
    private Integer lastStaff;
    private Integer lastPunters;
    private Boolean lastNightOpen;
    private boolean startupLogSent;

    public SimAdapter(GameState state, SimEventBus eventBus) {
        this.state = state;
        this.eventBus = eventBus;
    }

    public void sync() {
        if (state == null) return;

        int currentWeek = Math.max(1, state.weekCount);
        double currentCash = state.cash;
        int currentRep = state.reputation;
        int currentStaff = state.fohStaff.size() + state.bohStaff.size() + state.generalManagers.size();
        int currentPunters = state.nightPunters.size();
        boolean currentNightOpen = state.nightOpen;

        if (lastWeek == null || lastWeek != currentWeek) {
            lastWeek = currentWeek;
            eventBus.fireWeek(currentWeek);
        }

        if (lastCash == null || Math.abs(lastCash - currentCash) > 0.0001d) {
            lastCash = currentCash;
            eventBus.fireCash(currentCash);
        }

        if (lastRep == null || lastRep != currentRep) {
            lastRep = currentRep;
            eventBus.fireRep(currentRep);
        }

        if (lastStaff == null || lastStaff != currentStaff) {
            lastStaff = currentStaff;
            eventBus.fireStaff(currentStaff);
        }

        if (lastPunters == null || lastPunters != currentPunters) {
            lastPunters = currentPunters;
            eventBus.firePunters(currentPunters);
        }

        if (lastNightOpen == null || lastNightOpen != currentNightOpen) {
            lastNightOpen = currentNightOpen;
            eventBus.fireNightStatus(currentNightOpen);
        }

        if (!startupLogSent) {
            startupLogSent = true;
            eventBus.fireLog("HUD connected to simulation feed.");
        }
    }
}
