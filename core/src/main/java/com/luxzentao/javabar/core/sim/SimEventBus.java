package com.luxzentao.javabar.core.sim;

import java.util.concurrent.CopyOnWriteArrayList;

public class SimEventBus {
    private final CopyOnWriteArrayList<SimListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(SimListener listener) {
        if (listener != null) listeners.addIfAbsent(listener);
    }

    public void removeListener(SimListener listener) {
        listeners.remove(listener);
    }

    public void fireWeek(int week) {
        for (SimListener listener : listeners) listener.onWeekChanged(week);
    }

    public void fireCash(double cash) {
        for (SimListener listener : listeners) listener.onCashChanged(cash);
    }

    public void fireLog(String message) {
        if (message == null || message.isBlank()) return;
        for (SimListener listener : listeners) listener.onLog(message);
    }
}
