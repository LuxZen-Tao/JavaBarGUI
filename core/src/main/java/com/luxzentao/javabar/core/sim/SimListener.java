package com.luxzentao.javabar.core.sim;

public interface SimListener {
    void onWeekChanged(int week);
    void onCashChanged(double cash);
    void onLog(String message);
}
