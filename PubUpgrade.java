public enum PubUpgrade {

    // Core attractions
    POOL_TABLE("Pool Table", 180, 0.08, 1, 1,
            2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    DARTS("Darts Board", 120, 0.05, 1, 1,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    TVS("Big TVs", 240, 0.10, 0, 2,
            2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    BEER_GARDEN("Beer Garden", 320, 0.12, 1, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    // Kitchen
    KITCHEN_SETUP("Kitchen Base", 450, 0.02, 0, 0,
            0, 0, 10, 0, 0, 0, 0, 0, 1, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    KITCHEN("Kitchen Upgrade I", 600, 0.10, 1, 2,
            2, 0, 10, 0, 1, 0, 0, 0, 0, 1,
            0.00, 0.00, 0.03, 0.00, 0.00, 0.00),

    KITCHEN_EQUIPMENT("Kitchen Upgrade III", 360, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    HYGIENE_TRAINING("Hygiene Training", 300, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.12, 0.00, 0.00, 0.00, 0.00, 0.00),

    KITCHEN_STAFFING_I("Kitchen Staffing I", 280, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    KITCHEN_STAFFING_II("Kitchen Staffing II", 420, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    KITCHEN_STAFFING_III("Kitchen Staffing III", 620, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    FRIDGE_EXTENSION_1("Fridge Extension I", 220, 0.00, 0, 0,
            0, 0, 0, 10, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    FRIDGE_EXTENSION_2("Fridge Extension II", 360, 0.00, 0, 0,
            0, 0, 0, 15, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    FRIDGE_EXTENSION_3("Fridge Extension III", 520, 0.00, 0, 0,
            0, 0, 0, 20, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    NEW_KITCHEN_PLAN("Kitchen Upgrade II", 480, 0.00, 0, 0,
            0, 0, 0, 10, 0, 0, 0, 0, 0, 1,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    CHEF_TRAINING("Chef Training", 340, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    // Infrastructure
    EXTENDED_BAR("Extended Bar", 50, 0.06, 0, 1,
            6, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    WINE_CELLAR("Wine Cellar", 520, 0.04, 0, 1,
            0, 0, 50, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    CELLAR_EXPANSION_I("Cellar Expansion I", 240, 0.00, 0, 0,
            0, 0, 25, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    CELLAR_EXPANSION_II("Cellar Expansion II", 420, 0.00, 0, 0,
            0, 0, 50, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    CELLAR_EXPANSION_III("Cellar Expansion III", 1000, 0.00, 0, 0,
            0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    // Staff & security
    CCTV("CCTV System", 260, 0.00, 0, 1,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.06, 0.00, 0.00, 0.00, 0.00),

    CCTV_PACKAGE("CCTV Package", 340, 0.00, 0, 1,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.08, 0.00, 0.00, 0.06, 0.00),

    REINFORCED_DOOR_I("Reinforced Door I", 220, 0.00, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.02, 0.00),

    REINFORCED_DOOR_II("Reinforced Door II", 360, 0.00, 0, 0,
            0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.04, 0.00),

    REINFORCED_DOOR_III("Reinforced Door III", 520, 0.00, 0, 0,
            0, 0, 0, 0, 3, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.06, 0.00),

    LIGHTING_I("Lighting I", 200, 0.01, 1, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    LIGHTING_II("Lighting II", 320, 0.01, 1, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    LIGHTING_III("Lighting III", 480, 0.01, 1, 0,
            0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    BURGLAR_ALARM_I("Burglar Alarm I", 260, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.04, 0.03),

    BURGLAR_ALARM_II("Burglar Alarm II", 420, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.08, 0.06),

    BURGLAR_ALARM_III("Burglar Alarm III", 620, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.14, 0.10),

    STAFF_TRAINING_I("Staff Training I", 380, 0.03, 1, 1,
            0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.04, 0.00, 0.00, 0.00),

    STAFF_TRAINING_II("Staff Training II", 520, 0.02, 1, 1,
            0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.05, 0.00, 0.00, 0.00),

    STAFF_TRAINING_III("Staff Training III", 720, 0.02, 1, 1,
            0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.06, 0.00, 0.00, 0.00),

    STAFF_ROOM_I("Staff Room I", 240, 0.00, 0, 0,
            0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    STAFF_ROOM_II("Staff Room II", 420, 0.00, 0, 0,
            0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    STAFF_ROOM_III("Staff Room III", 620, 0.00, 0, 0,
            0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    DOOR_TEAM_I("Door Team I", 260, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    DOOR_TEAM_II("Door Team II", 420, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    DOOR_TEAM_III("Door Team III", 620, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    LEADERSHIP_PROGRAM("Leadership Program", 480, 0.02, 1, 1,
            0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
            0.00, 0.00, 0.02, 0.00, 0.00, 0.00),

    // QoL & risk
    BETTER_GLASSWARE_I("Better Glassware I", 140, 0.00, 1, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.01, 0.00, 0.00),

    BETTER_GLASSWARE_II("Better Glassware II", 260, 0.00, 1, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.02, 0.00, 0.00),
            
    BETTER_GLASSWARE_III("Better Glassware III", 420, 0.00, 1, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.03, 0.00, 0.00),

    FASTER_TAPS_I("Faster Pour Taps I", 260, 0.02, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    FASTER_TAPS_II("Faster Pour Taps II", 420, 0.02, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    FASTER_TAPS_III("Faster Pour Taps III", 620, 0.02, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

  

  

    SOUNDPROOFING_I("Soundproofing I", 280, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.06),

    SOUNDPROOFING_II("Soundproofing II", 440, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.10),

    SOUNDPROOFING_III("Soundproofing III", 640, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.14),

    FIRE_SUPPRESSION_I("Fire Suppression I", 360, 0.00, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.08, 0.00),

    FIRE_SUPPRESSION_II("Fire Suppression II", 560, 0.00, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.14, 0.00),

    FIRE_SUPPRESSION_III("Fire Suppression III", 760, 0.00, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.20, 0.00),

    MARSHALLS_I("Marshalls I", 420, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    MARSHALLS_II("Marshalls II", 680, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    MARSHALLS_III("Marshalls III", 980, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),


    SOUND_SYSTEM("Sound System", 460, 0.02, 0, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    CURATED_PLAYLIST("Curated Playlist", 320, 0.01, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    DJ_NIGHT("DJ Night", 540, 0.04, 0, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    LIVE_MUSIC_LICENSE("Live Music License", 620, 0.03, 1, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),

    // Inn
    INN_WING_1("Inn Wing (Tier 1)", 800, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    INN_WING_2("Inn Wing (Tier 2)", 1200, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    INN_WING_3("Inn Wing (Tier 3)", 1700, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    INN_WING_4("Inn Wing (Tier 4)", 2400, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
    INN_WING_5("Inn Wing (Tier 5)", 3500, 0.00, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0.00, 0.00, 0.00, 0.00, 0.00, 0.00);
            

    private final String label;
    private final double cost;
    private final double trafficBonusPct;
    private final int repDriftPerRound;
    private final int eventBonusChance;

    private final int barCapBonus;
    private final int serveCapBonus;
    private final int rackCapBonus;
    private final int foodRackCapBonus;
    private final int securityBonus;
    private final int staffCapBonus;
    private final int bouncerCapBonus;
    private final int managerCapBonus;
    private final int chefCapBonus;
    private final int kitchenQualityBonus;

    private final double refundRiskReductionPct;
    private final double staffMisconductReductionPct;
    private final double wageEfficiencyPct;
    private final double tipBonusPct;
    private final double eventDamageReductionPct;
    private final double riskReductionPct;
    private final String chainKey;
    private final int tier;

    PubUpgrade(String label,
               double cost,
               double trafficBonusPct,
               int repDriftPerRound,
               int eventBonusChance,
               int barCapBonus,
               int serveCapBonus,
               int rackCapBonus,
               int foodRackCapBonus,
               int securityBonus,
               int staffCapBonus,
               int bouncerCapBonus,
               int managerCapBonus,
               int chefCapBonus,
               int kitchenQualityBonus,
               double refundRiskReductionPct,
               double staffMisconductReductionPct,
               double wageEfficiencyPct,
               double tipBonusPct,
               double eventDamageReductionPct,
               double riskReductionPct) {

        this.label = label;
        this.cost = cost;
        this.trafficBonusPct = trafficBonusPct;
        this.repDriftPerRound = repDriftPerRound;
        this.eventBonusChance = eventBonusChance;

        this.barCapBonus = barCapBonus;
        this.serveCapBonus = serveCapBonus;
        this.rackCapBonus = rackCapBonus;
        this.foodRackCapBonus = foodRackCapBonus;
        this.securityBonus = securityBonus;
        this.staffCapBonus = staffCapBonus;
        this.bouncerCapBonus = bouncerCapBonus;
        this.managerCapBonus = managerCapBonus;
        this.chefCapBonus = chefCapBonus;
        this.kitchenQualityBonus = kitchenQualityBonus;

        this.refundRiskReductionPct = refundRiskReductionPct;
        this.staffMisconductReductionPct = staffMisconductReductionPct;
        this.wageEfficiencyPct = wageEfficiencyPct;
        this.tipBonusPct = tipBonusPct;
        this.eventDamageReductionPct = eventDamageReductionPct;
        this.riskReductionPct = riskReductionPct;
        TierInfo tierInfo = TierInfo.fromName(name());
        this.chainKey = tierInfo.chainKey();
        this.tier = tierInfo.tier();
    }

    @Override
    public String toString() {
        StringBuilder extras = new StringBuilder();

        if (barCapBonus > 0) extras.append(" | bar +").append(barCapBonus);
        if (serveCapBonus > 0) extras.append(" | serve +").append(serveCapBonus);
        if (rackCapBonus > 0) extras.append(" | rack +").append(rackCapBonus);
        if (foodRackCapBonus > 0) extras.append(" | kitchen stock +").append(foodRackCapBonus);
        if (securityBonus > 0) extras.append(" | sec +").append(securityBonus);
        if (staffCapBonus > 0) extras.append(" | staff +").append(staffCapBonus);
        if (bouncerCapBonus > 0) extras.append(" | bouncers +").append(bouncerCapBonus);
        if (managerCapBonus > 0) extras.append(" | managers +").append(managerCapBonus);
        if (chefCapBonus > 0) extras.append(" | chefs +").append(chefCapBonus);
        if (kitchenQualityBonus > 0) extras.append(" | food +").append(kitchenQualityBonus);
        if (refundRiskReductionPct > 0) extras.append(" | refunds -").append((int) (refundRiskReductionPct * 100)).append("%");
        if (staffMisconductReductionPct > 0) extras.append(" | misconduct -").append((int) (staffMisconductReductionPct * 100)).append("%");
        if (wageEfficiencyPct > 0) extras.append(" | wages -").append((int) (wageEfficiencyPct * 100)).append("%");
        if (tipBonusPct > 0) extras.append(" | tips +").append((int) (tipBonusPct * 100)).append("%");
        if (eventDamageReductionPct > 0) extras.append(" | event dmg -").append((int) (eventDamageReductionPct * 100)).append("%");
        if (riskReductionPct > 0) extras.append(" | chaos -").append((int) (riskReductionPct * 100)).append("%");

        return label
                + " | GBP " + String.format("%.0f", cost)
                + " | traffic +" + (int) (trafficBonusPct * 100) + "%"
                + " | rep drift " + (repDriftPerRound >= 0 ? "+" : "") + repDriftPerRound
                + " | event +" + eventBonusChance + "%"
                + extras;
    }

    public String getLabel() { return label; }
    public double getCost() { return cost; }
    public double getTrafficBonusPct() { return trafficBonusPct; }
    public int getRepDriftPerRound() { return repDriftPerRound; }
    public int getEventBonusChance() { return eventBonusChance; }

    public int getBarCapBonus() { return barCapBonus; }
    public int getServeCapBonus() { return serveCapBonus; }
    public int getRackCapBonus() { return rackCapBonus; }
    public int getFoodRackCapBonus() { return foodRackCapBonus; }
    public int getSecurityBonus() { return securityBonus; }
    public int getStaffCapBonus() { return staffCapBonus; }
    public int getBouncerCapBonus() { return bouncerCapBonus; }
    public int getManagerCapBonus() { return managerCapBonus; }
    public int getChefCapBonus() { return chefCapBonus; }
    public int getKitchenQualityBonus() { return kitchenQualityBonus; }

    public double getRefundRiskReductionPct() { return refundRiskReductionPct; }
    public double getStaffMisconductReductionPct() { return staffMisconductReductionPct; }
    public double getWageEfficiencyPct() { return wageEfficiencyPct; }
    public double getTipBonusPct() { return tipBonusPct; }
    public double getEventDamageReductionPct() { return eventDamageReductionPct; }
    public double getRiskReductionPct() { return riskReductionPct; }
    public String getChainKey() { return chainKey; }
    public int getTier() { return tier; }

    public boolean isKitchenRelated() {
        return switch (this) {
            case KITCHEN_SETUP,
                    KITCHEN,
                    NEW_KITCHEN_PLAN,
                    KITCHEN_EQUIPMENT,
                    HYGIENE_TRAINING,
                    CHEF_TRAINING,
                    KITCHEN_STAFFING_I,
                    KITCHEN_STAFFING_II,
                    KITCHEN_STAFFING_III,
                    FRIDGE_EXTENSION_1,
                    FRIDGE_EXTENSION_2,
                    FRIDGE_EXTENSION_3 -> true;
            default -> false;
        };
    }

    public boolean isInnRelated() {
        return switch (this) {
            case INN_WING_1,
                    INN_WING_2,
                    INN_WING_3,
                    INN_WING_4,
                    INN_WING_5 -> true;
            default -> false;
        };
    }

    private record TierInfo(String chainKey, int tier) {
        static TierInfo fromName(String name) {
            if (name == null) return new TierInfo(null, 1);
            if (name.endsWith("_I")) return new TierInfo(name.replace("_I", ""), 1);
            if (name.endsWith("_II")) return new TierInfo(name.replace("_II", ""), 2);
            if (name.endsWith("_III")) return new TierInfo(name.replace("_III", ""), 3);
            if (name.endsWith("_IV")) return new TierInfo(name.replace("_IV", ""), 4);
            if (name.endsWith("_V")) return new TierInfo(name.replace("_V", ""), 5);
            if (name.endsWith("_1")) return new TierInfo(name.replace("_1", ""), 1);
            if (name.endsWith("_2")) return new TierInfo(name.replace("_2", ""), 2);
            if (name.endsWith("_3")) return new TierInfo(name.replace("_3", ""), 3);
            if (name.endsWith("_4")) return new TierInfo(name.replace("_4", ""), 4);
            if (name.endsWith("_5")) return new TierInfo(name.replace("_5", ""), 5);
            return new TierInfo(null, 1);
        }
    }
}
