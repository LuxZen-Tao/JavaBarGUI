// Staff.java
import java.util.Random;

public class Staff {

    public enum Type {
        TRAINEE,
        EXPERIENCED,
        SPEED,
        CHARISMA,
        SECURITY,
        CHEF,
        HEAD_CHEF,
        SOUS_CHEF,
        CHEF_DE_PARTIE,
        KITCHEN_ASSISTANT,
        KITCHEN_PORTER,
        ASSISTANT_MANAGER,
        MANAGER
    }

    private final int id;
    private final String name;
    private Type type;

    private int baseServeCapacity;
    private int baseSkill;

    private int repMin;
    private int repMax;

    private double weeklyWage;

    private double capacityMultiplier; // manager
    private double tipRate;            // manager
    private double tipBonus;           // staff
    private int securityBonus;         // staff
    private int chaosTolerance;

    private double accruedThisWeek = 0;
    private int morale = 70;
    private int level = 0;
    private int weeksEmployed = 0;

    public Staff(int id,
                 String name,
                 Type type,
                 int serveCapacity,
                 int skill,
                 int repMin,
                 int repMax,
                 double weeklyWage,
                 double capacityMultiplier,
                 double tipRate,
                 double tipBonus,
                 int securityBonus,
                 int chaosTolerance,
                 int morale) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.baseServeCapacity = serveCapacity;
        this.baseSkill = skill;
        this.repMin = repMin;
        this.repMax = repMax;
        this.weeklyWage = weeklyWage;
        this.capacityMultiplier = capacityMultiplier;
        this.tipRate = tipRate;
        this.tipBonus = tipBonus;
        this.securityBonus = securityBonus;
        this.chaosTolerance = chaosTolerance;
        this.morale = clamp(morale);
    }

    public Type getType() { return type; }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getServeCapacity() { return baseServeCapacity + (level / 3); }
    public int getSkill() { return baseSkill + level; }

    public int getRepMin() { return repMin; }
    public int getRepMax() { return repMax; }

    public double getWeeklyWage() { return weeklyWage; }
    public double getCapacityMultiplier() { return capacityMultiplier; }
    public double getTipRate() { return tipRate; }
    public double getTipBonus() { return tipBonus; }
    public int getSecurityBonus() { return securityBonus; }
    public int getChaosTolerance() { return chaosTolerance; }
    public boolean isKitchenRole() {
        return switch (type) {
            case CHEF, HEAD_CHEF, SOUS_CHEF, CHEF_DE_PARTIE, KITCHEN_ASSISTANT, KITCHEN_PORTER -> true;
            default -> false;
        };
    }

    public int getKitchenCapacity() {
        return switch (type) {
            case HEAD_CHEF -> 4;
            case SOUS_CHEF -> 3;
            case CHEF_DE_PARTIE, CHEF -> 2;
            case KITCHEN_ASSISTANT -> 1;
            case KITCHEN_PORTER -> 1;
            default -> 0;
        };
    }

    public double getAccruedThisWeek() { return accruedThisWeek; }
    public int getMorale() { return morale; }
    public int getLevel() { return level; }
    public int getWeeksEmployed() { return weeksEmployed; }

    public void accrueDailyWage() {
        accruedThisWeek += (weeklyWage / 7.0);
    }

    public double cashOutAccrued() {
        double due = accruedThisWeek;
        accruedThisWeek = 0;
        return due;
    }

    public void adjustMorale(int delta) {
        morale = clamp(morale + delta);
    }

    public boolean isManagerRole() {
        return type == Type.MANAGER || type == Type.ASSISTANT_MANAGER;
    }

    public void incrementWeeksEmployed() {
        weeksEmployed++;
    }

    public void levelUpWeekly(int levels) {
        if (levels <= 0) return;
        level += levels;
    }

    public boolean promoteIfEligible(Random random) {
        if (level == 0 || level % 4 != 0) return false;
        switch (type) {
            case TRAINEE -> type = applyPromotion(Type.EXPERIENCED, random);
            case EXPERIENCED -> type = applyPromotion(Type.SPEED, random);
            case SPEED -> type = applyPromotion(Type.CHARISMA, random);
            case CHEF, CHEF_DE_PARTIE -> type = applyPromotion(Type.SOUS_CHEF, random);
            case SOUS_CHEF -> type = applyPromotion(Type.HEAD_CHEF, random);
            case KITCHEN_PORTER -> type = applyPromotion(Type.KITCHEN_ASSISTANT, random);
            case KITCHEN_ASSISTANT -> type = applyPromotion(Type.CHEF_DE_PARTIE, random);
            case ASSISTANT_MANAGER -> type = applyPromotion(Type.MANAGER, random);
            default -> { return false; }
        }
        return true;
    }

    public int repRollThisRound(Random random) {
        if (repMin == repMax) return repMin;
        int lo = Math.min(repMin, repMax);
        int hi = Math.max(repMin, repMax);
        return lo + random.nextInt(hi - lo + 1);
    }

    public static String rangeLabel(Type t) {
        return switch (t) {
            case TRAINEE ->
                    "Trainee Bartender | serves 1-2 | wage 30-45/wk | rep/round -1..+3";
            case EXPERIENCED ->
                    "Experienced Bartender | serves 2-4 | wage 55-85/wk | rep/round -2..+4";
            case SPEED ->
                    "Speed Demon Bartender | serves 5-8 | wage 110-180/wk | rep/round -6..+2 (risky!)";
            case CHARISMA ->
                    "Charisma Bartender | serves 2-3 | tips +2% | wage 70-105/wk | rep/round +1..+5";
            case SECURITY ->
                    "Security Bartender | serves 1-2 | sec +1 | wage 75-110/wk | rep/round -2..+2";
            case CHEF ->
                    "Chef | serves food only | wage 80-140/wk | rep/round -1..+3";
            case HEAD_CHEF ->
                    "Head Chef | kitchen lead | refunds - | wage 140-210/wk | rep/round +1..+4";
            case SOUS_CHEF ->
                    "Sous Chef | kitchen support | wage 110-170/wk | rep/round 0..+3";
            case CHEF_DE_PARTIE ->
                    "Chef de Partie | line cook | wage 95-150/wk | rep/round -1..+2";
            case KITCHEN_ASSISTANT ->
                    "Kitchen Assistant | prep helper | wage 70-110/wk | rep/round -1..+1";
            case KITCHEN_PORTER ->
                    "Kitchen Porter | dish & prep | wage 55-85/wk | rep/round -2..+1";
            case ASSISTANT_MANAGER ->
                    "Assistant Manager | x1.05x1.15 capacity | tips +1% | wage 65-115/wk | rep/round -2..+4";
            case MANAGER ->
                    "Manager | x1.10x1.35 capacity | 2%7% tips | wage 90-160/wk | rep/round -3..+5";
        };
    }

    @Override
    public String toString() {
        String base = "#" + id + " " + name + " | " + type.name()
                + " | lvl " + level
                + " | morale " + morale
                + " | wage " + String.format("%.2f", weeklyWage) + "/wk";
        if (isManagerRole()) {
            return base
                    + " | cap x" + String.format("%.2f", capacityMultiplier)
                    + " | tips " + (int)(tipRate * 100) + "%"
                    + " | rep/round " + repMin + ".." + repMax
                    + " | skill " + getSkill()
                    + " | accrued " + String.format("%.2f", accruedThisWeek);
        }

        return base
                + " | serves " + getServeCapacity() + "/round"
                + (tipBonus > 0 ? " | tips +" + (int)Math.round(tipBonus * 100) + "%" : "")
                + (securityBonus > 0 ? " | sec +" + securityBonus : "")
                + " | rep/round " + repMin + ".." + repMax
                + " | skill " + getSkill()
                + " | chaos tol " + chaosTolerance
                + " | accrued " + String.format("%.2f", accruedThisWeek);
    }

    private Type applyPromotion(Type target, Random random) {
        applyTemplate(StaffFactory.templateFor(target, random));
        return target;
    }

    private void applyTemplate(StaffFactory.StaffTemplate template) {
        this.baseServeCapacity = template.serveCapacity();
        this.baseSkill = template.skill();
        this.repMin = template.repMin();
        this.repMax = template.repMax();
        this.weeklyWage = template.weeklyWage();
        this.capacityMultiplier = template.capacityMultiplier();
        this.tipRate = template.tipRate();
        this.tipBonus = template.tipBonus();
        this.securityBonus = template.securityBonus();
        this.chaosTolerance = template.chaosTolerance();
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
