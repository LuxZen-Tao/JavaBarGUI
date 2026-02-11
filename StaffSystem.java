import java.util.Random;

public class StaffSystem {

    // Centralized staff scaling knobs (single source for staffing pressure curve).
    public static final double WORKLOAD_EXPONENT = 1.6;
    private static final double WORKLOAD_UNSERVED_SCALE = 0.85;
    private static final double WORKLOAD_REFUND_SCALE = 0.60;
    private static final double WORKLOAD_CHAOS_SCALE = 7.5;
    private static final double WORKLOAD_INCIDENT_SCALE = 0.55;

    private final GameState s;
    private final EconomySystem eco;
    private final UpgradeSystem upgrades;

    public StaffSystem(GameState s, EconomySystem eco, UpgradeSystem upgrades) {
        this.s = s;
        this.eco = eco;
        this.upgrades = upgrades;
    }

    /** Landlord baseline service (so you can still sell something with zero staff). */
    private static final int LANDLORD_BASE_SERVICE = 1;

    public record StaffPerformanceProfile(
            int headcount,
            double avgSpeed,
            double avgQuality,
            double avgReliability,
            double avgComposure
    ) {}

    public record WorkloadProfile(
            int demand,
            int capacity,
            double workload,
            double penalty,
            int effectiveCapacity,
            String serviceDriverLine,
            String stabilityDriverLine
    ) {}

    public int baseServeCapacity() {
        int cap = LANDLORD_BASE_SERVICE;
        for (Staff st : s.fohStaff) cap += st.getServeCapacity();
        return Math.max(0, cap);
    }

    public int totalServeCapacity() {
        int base = Math.max(0, baseServeCapacity());

        double mult = 1.0;
        for (Staff st : s.generalManagers) {
            mult += (st.getCapacityMultiplier() - 1.0);
        }
        int cap = Math.max(1, (int) Math.floor(base * mult));

        if (s.activityTonight != null) cap += s.activityTonight.getCapacityBonus();
        cap += s.upgradeServeCapBonus;
        cap += s.pubLevelServeCapBonus;
        cap += s.tempServeBonusTonight;
        if (s.wageServePenaltyWeeks > 0 && s.wageServePenaltyPct > 0.0) {
            cap = Math.max(1, (int)Math.floor(cap * (1.0 - s.wageServePenaltyPct)));
        }
        double legacyEff = 1.0 + Math.max(0.0, s.legacy.staffEfficiencyBonus);
        cap = Math.max(1, (int)Math.floor(cap * legacyEff));
        return cap;
    }

    public StaffPerformanceProfile performanceProfile() {
        java.util.ArrayList<Staff> roster = new java.util.ArrayList<>();
        roster.addAll(s.fohStaff);
        roster.addAll(s.bohStaff);
        roster.addAll(s.generalManagers);
        if (roster.isEmpty()) {
            return new StaffPerformanceProfile(0, 40.0, 40.0, 40.0, 40.0);
        }

        double speed = 0.0;
        double quality = 0.0;
        double reliability = 0.0;
        double composure = 0.0;
        for (Staff st : roster) {
            speed += clamp(20.0 + (st.getServeCapacity() * 8.0) + (st.getSkill() * 2.0), 20.0, 100.0);
            quality += clamp(28.0 + (st.getSkill() * 5.0) + (Math.max(0, st.getRepMax()) * 2.0), 20.0, 100.0);
            reliability += clamp(25.0 + (st.getMorale() * 0.55) + (st.getSecurityBonus() * 8.0), 20.0, 100.0);
            composure += clamp(20.0 + (st.getChaosTolerance() * 0.9) + (st.getSkill() * 2.0), 20.0, 100.0);
        }
        double count = roster.size();
        return new StaffPerformanceProfile(
                roster.size(),
                speed / count,
                quality / count,
                reliability / count,
                composure / count
        );
    }

    public WorkloadProfile workloadProfile(int demand, int capacity) {
        int safeDemand = Math.max(0, demand);
        int safeCapacity = Math.max(1, capacity);
        double workload = safeDemand / (double) safeCapacity;
        double penalty = Math.pow(Math.max(0.0, workload - 1.0), WORKLOAD_EXPONENT);
        int effectiveCapacity = Math.max(1, (int) Math.floor(safeCapacity / (1.0 + penalty * WORKLOAD_UNSERVED_SCALE)));
        StaffPerformanceProfile perf = performanceProfile();

        String serviceLine = String.format(
                "Service: workload %.2f (%s), avgSpeed %.0f (%s), quality %.0f (%s)",
                workload,
                workload > 1.0 ? "+" : "-",
                perf.avgSpeed,
                perf.avgSpeed >= 60.0 ? "-" : "+",
                perf.avgQuality,
                perf.avgQuality >= 60.0 ? "-" : "+"
        );
        String stabilityLine = String.format(
                "Stability: workload %.2f (%s), composure %.0f (%s), reliability %.0f (%s)",
                workload,
                workload > 1.0 ? "+" : "-",
                perf.avgComposure,
                perf.avgComposure >= 60.0 ? "-" : "+",
                perf.avgReliability,
                perf.avgReliability >= 60.0 ? "-" : "+"
        );

        return new WorkloadProfile(safeDemand, safeCapacity, workload, penalty, effectiveCapacity, serviceLine, stabilityLine);
    }

    public double refundPressureMultiplier(double workloadPenalty) {
        WorkloadProfile profile = new WorkloadProfile(1, 1, 1.0 + Math.max(0.0, workloadPenalty), Math.max(0.0, workloadPenalty), 1, "", "");
        return refundPressureMultiplier(profile);
    }

    public double refundPressureMultiplier(WorkloadProfile profile) {
        if (profile == null) return 1.0;
        StaffPerformanceProfile perf = performanceProfile();
        double qualityRelief = clamp((perf.avgQuality - 50.0) / 100.0, -0.20, 0.30);
        double multiplier = (1.0 + profile.penalty * WORKLOAD_REFUND_SCALE) * (1.0 - qualityRelief);
        return clamp(multiplier, 0.75, 2.25);
    }

    public double chaosPressureDelta(WorkloadProfile profile) {
        if (profile == null) return 0.0;
        StaffPerformanceProfile perf = performanceProfile();
        double composureRelief = clamp((perf.avgComposure - 50.0) / 100.0, -0.25, 0.35);
        double delta = (profile.penalty * WORKLOAD_CHAOS_SCALE) * (1.0 - composureRelief);
        return clamp(delta, 0.0, 15.0);
    }

    public double misconductPressureMultiplier(WorkloadProfile profile) {
        if (profile == null) return 1.0;
        StaffPerformanceProfile perf = performanceProfile();
        double reliabilityRelief = clamp((perf.avgReliability - 50.0) / 100.0, -0.20, 0.30);
        double multiplier = (1.0 + profile.penalty * WORKLOAD_INCIDENT_SCALE) * (1.0 - reliabilityRelief);
        return clamp(multiplier, 0.75, 2.0);
    }

    public double tipRate() {
        double tipRate = 0.0;
        for (Staff st : s.generalManagers) tipRate += st.getTipRate();
        for (Staff st : s.fohStaff) tipRate += st.getTipBonus();
        return tipRate;
    }

    public void accrueDailyWages() {
        for (Staff st : s.fohStaff) st.accrueDailyWage();
        for (Staff st : s.bohStaff) st.accrueDailyWage();
        for (Staff st : s.generalManagers) st.accrueDailyWage();
    }

    /** Total wages accrued so far this week (staff + manager). */
    public double wagesDueRaw() {
        double sum = 0;
        for (Staff st : s.fohStaff) sum += st.getAccruedThisWeek();
        for (Staff st : s.bohStaff) sum += st.getAccruedThisWeek();
        for (Staff st : s.generalManagers) sum += st.getAccruedThisWeek();
        return sum;
    }

    /** Convenience alias (older code calls wagesDue()). */
    public double wagesDue() {
        double raw = wagesDueRaw();
        double eff = upgrades != null ? upgrades.wageEfficiencyPct() : 0.0;
        return raw * (1.0 - Math.max(0.0, eff));
    }

    public void resetAccrual() {
        for (Staff st : s.fohStaff) st.cashOutAccrued();
        for (Staff st : s.bohStaff) st.cashOutAccrued();
        for (Staff st : s.generalManagers) st.cashOutAccrued();
    }

    public double applyWagePayment(double amount) {
        double remaining = Math.max(0.0, amount);
        for (Staff st : s.fohStaff) {
            if (remaining <= 0) break;
            remaining -= st.applyWagePayment(remaining);
        }
        for (Staff st : s.bohStaff) {
            if (remaining <= 0) break;
            remaining -= st.applyWagePayment(remaining);
        }
        for (Staff st : s.generalManagers) {
            if (remaining <= 0) break;
            remaining -= st.applyWagePayment(remaining);
        }
        return amount - remaining;
    }

    public int repDeltaThisRound(Random r) {
        int delta = 0;
        for (Staff st : s.fohStaff) delta += st.repRollThisRound(r);
        for (Staff st : s.bohStaff) delta += st.repRollThisRound(r);
        for (Staff st : s.generalManagers) delta += st.repRollThisRound(r);
        return delta;
    }

    /**
     * Small per-round operating cost (tycoon sauce).
     * Scales gently with headcount and skill so it matters later, not instantly.
     */
    public double roundOperatingCost(int barOccupancy) {
        double base = 0.35; // lights on, taps cleaned, existential dread
        int staffCount = s.fohStaff.size() + s.bohStaff.size() + s.generalManagers.size();
        double perStaff = 0.20 * staffCount;
        double skill = 0.0;

        for (Staff st : s.fohStaff) skill += st.getSkill();
        for (Staff st : s.bohStaff) skill += st.getSkill();
        for (Staff st : s.generalManagers) skill += st.getSkill() * 0.6;

        double perSkill = 0.02 * skill;
        double perPunter = 0.05 * Math.max(0, barOccupancy);

        s.opCostBaseThisWeek += base;
        s.opCostStaffThisWeek += perStaff;
        s.opCostSkillThisWeek += perSkill;
        s.opCostOccupancyThisWeek += perPunter;

        return Math.max(0.0, base + perStaff + perSkill + perPunter);
    }

    public void adjustMoraleAfterRound(int unserved, int eventsThisRound, int reputation, double tipRate, int security, double chaos, double musicMoraleDelta, double fatiguePressure) {
        if (s.fohStaff.isEmpty() && s.bohStaff.isEmpty() && s.generalManagers.isEmpty()) {
            s.teamMorale = 70.0;
            s.fohMorale = 70.0;
            s.bohMorale = 70.0;
            return;
        }

        int delta = 0;
        if (unserved > 0) delta -= Math.min(6, 1 + (unserved / 2));
        if (eventsThisRound > 0) delta -= Math.min(6, 2 * eventsThisRound);
        if (reputation >= 40) delta += 1;
        if (reputation <= -20) delta -= 1;
        if (tipRate >= 0.03) delta += 1;
        if (tipRate <= 0.005) delta -= 1;
        delta -= (int)Math.floor(chaos / 40.0);

        if (delta < 0 && security > 0) {
            double damp = Math.min(0.35, security * 0.03);
            delta = (int)Math.round(delta * (1.0 - damp));
        }

        if (delta < 0 && s.upgradeMoraleStabilityPct > 0.0) {
            delta = (int)Math.round(delta * (1.0 - s.upgradeMoraleStabilityPct));
        }

        if (delta != 0) {
            delta = (int)Math.round(delta * 1.15);
        }

        if (fatiguePressure > 8.0) {
            delta -= Math.min(4, (int)Math.round((fatiguePressure - 8.0) / 4.0));
        }

        double negMult = chaosMoraleNegMultiplier(chaos);
        double posMult = chaosMoralePosMultiplier(chaos);
        s.lastChaosMoraleNegMult = negMult;
        s.lastChaosMoralePosMult = posMult;
        if (delta < 0) {
            delta = (int)Math.round(delta * negMult);
        } else if (delta > 0) {
            delta = (int)Math.round(delta * posMult);
        }

        boolean smoothNight = unserved <= 0 && eventsThisRound <= 0 && chaos < 18;
        int bohDelta = delta;
        if (bohDelta < 0 && s.bohMoraleResiliencePct > 0.0) {
            bohDelta = (int)Math.round(bohDelta * (1.0 - s.bohMoraleResiliencePct));
        }
        int musicDelta = (int)Math.round(musicMoraleDelta);
        for (Staff st : s.fohStaff) st.adjustMorale(delta + musicDelta + smallMoraleDrift(chaos, smoothNight));
        for (Staff st : s.bohStaff) st.adjustMorale(bohDelta + musicDelta + smallMoraleDrift(chaos, smoothNight));
        for (Staff st : s.generalManagers) st.adjustMorale(delta + musicDelta + smallMoraleDrift(chaos, smoothNight));

        updateTeamMorale();
        s.foodRack.setCapacity(s.baseFoodRackCapacity + s.upgradeFoodRackCapBonus
                + (s.staffCountOfType(Staff.Type.HEAD_CHEF) * 5));
    }

    private int smallMoraleDrift(double chaos, boolean smoothNight) {
        int roll = s.random.nextInt(100);
        if (chaos >= 55) {
            if (roll < 55) return -1;
            if (roll < 80) return 0;
            return 1;
        }
        if (smoothNight) {
            if (roll < 25) return -1;
            if (roll < 55) return 0;
            return 1;
        }
        if (roll < 38) return -1;
        if (roll < 62) return 0;
        return 1;
    }

    private double chaosMoraleNegMultiplier(double chaos) {
        double normalized = clamp01(chaos / 100.0);
        return clamp(0.85 + (0.40 * normalized), 0.85, 1.25);
    }

    private double chaosMoralePosMultiplier(double chaos) {
        double normalized = clamp01(chaos / 100.0);
        return clamp(1.20 - (0.30 * normalized), 0.90, 1.20);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp01(double value) {
        return clamp(value, 0.0, 1.0);
    }

    public void updateTeamMorale() {
        s.fohMorale = poolMorale(s.fohStaff);
        s.bohMorale = poolMorale(s.bohStaff);
        s.teamMorale = combinedMorale();
        s.foodRack.setCapacity(s.baseFoodRackCapacity + s.upgradeFoodRackCapBonus
                + (s.staffCountOfType(Staff.Type.HEAD_CHEF) * 5));
    }

    public double teamMorale() {
        return combinedMorale();
    }

    /**
     * Weekly morale check: if fights were frequent, staff may quit.
     * This resets weekly (call at endOfWeek).
     */
    public void weeklyMoraleCheck(int fightsThisWeek, Random r, UILogger log) {
        int baseQuit = fightsThisWeek <= 0 ? 0 : Math.min(45, 6 + fightsThisWeek * 8);
        double moraleMult = moraleQuitMultiplier(s.teamMorale);
        int quitChance = (int)Math.round(baseQuit * moraleMult);
        if (quitChance <= 0) return;

        // FOH quit chance
        for (int i = s.fohStaff.size() - 1; i >= 0; i--) {
            if (r.nextInt(100) < quitChance) {
                Staff st = s.fohStaff.get(i);

                double due = st.getAccruedThisWeek();
                if (due > 0 && !eco.tryPay(due, TransactionType.WAGES, "Wages payout (staff quit)", CostTag.WAGES)) {
                    continue;
                }
                st.cashOutAccrued();

                s.fohStaff.remove(i);
                s.staffDeparturesThisWeek++;
                log.neg(" Staff quit after a rough week (" + fightsThisWeek + " fights).");
                eco.applyRep(-1, "Staff quits (morale)");
            }
        }

        // BOH quit chance if morale is low
        for (int i = s.bohStaff.size() - 1; i >= 0; i--) {
            Staff st = s.bohStaff.get(i);
            if (st.getMorale() < 35 && r.nextInt(100) < Math.max(12, quitChance)) {
                double due = st.getAccruedThisWeek();
                if (due > 0 && !eco.tryPay(due, TransactionType.WAGES, "Wages payout (chef quits)", CostTag.WAGES)) {
                    continue;
                }
                st.cashOutAccrued();
                s.bohStaff.remove(i);
                s.staffDeparturesThisWeek++;
                log.popup(" Chef quits", "Kitchen staff quit after a rough week.", "Morale low");
                eco.applyRep(-2, "Chef quits");
            }
        }

        //  Managers (generalManagers) are harder to lose, but possible
        int managerQuit = (int)Math.round(Math.min(35, 6 + fightsThisWeek * 5) * moraleMult);
        for (int i = s.generalManagers.size() - 1; i >= 0; i--) {
            if (fightsThisWeek >= 2 && r.nextInt(100) < managerQuit) {
                Staff manager = s.generalManagers.get(i);
                double due = manager.getAccruedThisWeek();
                if (due > 0 && !eco.tryPay(due, TransactionType.WAGES, "Wages payout (manager quits)", CostTag.WAGES)) {
                    continue;
                }
                manager.cashOutAccrued();
                s.generalManagers.remove(i);
                s.staffDeparturesThisWeek++;
                log.neg(" Manager resigns. \"This place is chaos.\"");
                eco.applyRep(-2, "Manager quits");
            }
        }

        updateTeamMorale();
    }

    public void handleWeeklyLevelUps(Random r, UILogger log, double chaos) {
        double volatility = Math.min(0.45, chaos / 200.0);
        for (Staff st : s.fohStaff) {
            st.incrementWeeksEmployed();
            st.levelUpWeekly(levelsFromChaos(r, volatility));
            log.event(" " + st.getName() + " levelled up to " + st.getLevel() + ".");
            if (st.promoteIfEligible(r)) {
                log.popup(" Promotion", "<b>" + st.getName() + "</b> promoted to " + st.getType() + ".", "");
            }
        }
        for (Staff st : s.bohStaff) {
            st.incrementWeeksEmployed();
            st.levelUpWeekly(levelsFromChaos(r, volatility));
            log.event(" " + st.getName() + " levelled up to " + st.getLevel() + ".");
            if (st.promoteIfEligible(r)) {
                log.popup(" Promotion", "<b>" + st.getName() + "</b> promoted to " + st.getType() + ".", "");
            }
        }
        for (Staff st : s.generalManagers) {
            st.incrementWeeksEmployed();
            st.levelUpWeekly(levelsFromChaos(r, volatility));
            log.event(" " + st.getName() + " levelled up to " + st.getLevel() + ".");
        }
        updateTeamMorale();
    }

    private int levelsFromChaos(Random r, double volatility) {
        int levels = 1;
        if (r.nextDouble() < volatility) {
            levels += r.nextBoolean() ? 1 : 0;
        }
        if (r.nextDouble() < volatility * 0.35) {
            levels = Math.max(0, levels - 1);
        }
        return levels;
    }

    private double moraleQuitMultiplier(double morale) {
        if (morale >= 80) return 0.2;
        if (morale >= 65) return 0.6;
        if (morale >= 50) return 1.0;
        if (morale >= 30) return 1.5;
        return 2.0;
    }

    private double poolMorale(java.util.List<Staff> pool) {
        if (pool == null || pool.isEmpty()) return 70.0;
        double total = 0.0;
        double weight = 0.0;
        for (Staff st : pool) {
            double w = 1.0 + (st.getSkill() / 6.0);
            total += st.getMorale() * w;
            weight += w;
        }
        return weight <= 0.0 ? 70.0 : total / weight;
    }

    private double combinedMorale() {
        int fohCount = s.fohStaff.size();
        int bohCount = s.bohStaff.size();
        int gmCount = s.generalManagers.size();

        double gmMorale = poolMorale(s.generalManagers);

        double total = (s.fohMorale * fohCount) + (s.bohMorale * bohCount) + (gmMorale * gmCount);
        int denom = fohCount + bohCount + gmCount;
        if (denom <= 0) return 70.0;
        return total / denom;
    }
}
