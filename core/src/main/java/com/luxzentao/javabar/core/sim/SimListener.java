package com.luxzentao.javabar.core.sim;

public interface SimListener {
    void onWeekChanged(int week);
    void onCashChanged(double cash);
    void onLog(String message);

    /** Called whenever the bar's open/closed status changes. */
    default void onNightStatusChanged(boolean nightOpen) {}

    /** Called when reputation changes. */
    default void onRepChanged(int rep) {}

    /** Called when total active staff count changes. */
    default void onStaffChanged(int total) {}

    /** Called when the number of punters currently in the bar changes. */
    default void onPuntersChanged(int count) {}
}
