package com.luxzentao.javabar.core.sim;

import com.luxzentao.javabar.core.GameState;

public class SimAdapter {
    private final GameState state;
    private final SimEventBus eventBus;

    private Integer lastWeek;
    private Double lastCash;
    private boolean startupLogSent;

    public SimAdapter(GameState state, SimEventBus eventBus) {
        this.state = state;
        this.eventBus = eventBus;
    }

    public void sync() {
        if (state == null) return;

        int currentWeek = Math.max(1, state.weekCount);
        double currentCash = state.cash;

        if (lastWeek == null || lastWeek != currentWeek) {
            lastWeek = currentWeek;
            eventBus.fireWeek(currentWeek);
        }

        if (lastCash == null || Math.abs(lastCash - currentCash) > 0.0001d) {
            lastCash = currentCash;
            eventBus.fireCash(currentCash);
        }

        if (!startupLogSent) {
            startupLogSent = true;
            eventBus.fireLog("HUD connected to simulation feed.");
        }
    }
}
