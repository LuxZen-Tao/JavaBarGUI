package com.luxzentao.javabar.legacy.swing;

import com.luxzentao.javabar.core.*;

import javax.swing.JTextPane;
import java.util.Random;

/** Quick deterministic staff balance sweep across 30 nights per scenario. */
public class StaffBalanceDebugSim {
    private static final int NIGHTS = 30;

    public static void main(String[] args) {
        runScenario("Understaffed", 2, 0, 0, 14);
        runScenario("Adequate", 4, 1, 0, 14);
        runScenario("Overstaffed", 6, 1, 1, 14);
        runScenario("Quality-heavy expensive", 2, 2, 1, 26);
        runScenario("Cheap volatility", 5, 0, 0, 4);
    }

    private static void runScenario(String label, int experienced, int charisma, int speed, int baseDemand) {
        GameState state = GameFactory.newGame();
        EconomySystem eco = new EconomySystem(state, new UILogger(new JTextPane()));
        UpgradeSystem upgrades = new UpgradeSystem(state);
        StaffSystem staff = new StaffSystem(state, eco, upgrades);
        Random r = new Random(42);

        for (int i = 0; i < experienced; i++) {
            state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Exp" + i, Staff.Type.EXPERIENCED, r, 12, 10));
        }
        for (int i = 0; i < charisma; i++) {
            state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Cha" + i, Staff.Type.CHARISMA, r, 12, 10));
        }
        for (int i = 0; i < speed; i++) {
            state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Spd" + i, Staff.Type.SPEED, r, 12, 10));
        }

        double unservedTotal = 0.0;
        double refundChanceTotal = 0.0;
        double chaosDeltaTotal = 0.0;
        double payrollTotal = 0.0;

        for (int n = 0; n < NIGHTS; n++) {
            int demand = baseDemand + (n % 6);
            int cap = Math.max(1, staff.totalServeCapacity());
            StaffSystem.WorkloadProfile workload = staff.workloadProfile(demand, cap);
            int unserved = Math.max(0, demand - Math.min(demand, workload.effectiveCapacity()));
            double refundChance = Math.min(1.0, 0.11 * staff.refundPressureMultiplier(workload));
            double chaosDelta = staff.chaosPressureDelta(workload);

            unservedTotal += unserved;
            refundChanceTotal += refundChance;
            chaosDeltaTotal += chaosDelta;

            for (Staff st : state.fohStaff) {
                payrollTotal += st.getWeeklyWage() / 7.0;
            }
        }

        System.out.printf(
                "%s -> avgUnserved=%.2f avgRefundChance=%.3f avgChaosDelta=%.2f payroll=%.2f%n",
                label,
                unservedTotal / NIGHTS,
                refundChanceTotal / NIGHTS,
                chaosDeltaTotal / NIGHTS,
                payrollTotal
        );
    }
}
