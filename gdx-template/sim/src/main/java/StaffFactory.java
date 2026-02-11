import java.util.Random;

public class StaffFactory {
    private static final double WAGE_MULTIPLIER = 1.20;

    public record StaffTemplate(
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
            int morale
    ) {}

    private StaffFactory() {}

    public static Staff createStaff(int id, String name, Staff.Type type, Random random) {
        StaffTemplate template = templateFor(type, random);
        return createFromTemplate(id, name, type, template);
    }

    public static Staff createStaff(int id, String name, Staff.Type type, Random random, int week, int reputation) {
        StaffTemplate template = templateFor(type, random, week, reputation);
        return createFromTemplate(id, name, type, template);
    }

    private static Staff createFromTemplate(int id, String name, Staff.Type type, StaffTemplate template) {
        return new Staff(
                id,
                name,
                type,
                template.serveCapacity(),
                template.skill(),
                template.repMin(),
                template.repMax(),
                template.weeklyWage(),
                template.capacityMultiplier(),
                template.tipRate(),
                template.tipBonus(),
                template.securityBonus(),
                template.chaosTolerance(),
                template.morale()
        );
    }

    public static StaffTemplate templateFor(Staff.Type type, Random random, int week, int reputation) {
        StaffTemplate base = templateFor(type, random);
        double weekFactor = clamp01((Math.max(1, week) - 1) / 36.0);
        double repFactor = clamp01((reputation + 40.0) / 140.0);
        double progression = clamp01((weekFactor * 0.6) + (repFactor * 0.4));

        int speed = base.serveCapacity();
        int skill = base.skill();
        int repMin = base.repMin();
        int repMax = base.repMax();
        int chaosTolerance = base.chaosTolerance();

        // Better candidates become more common later, but not dominant.
        if (random.nextDouble() < progression * 0.65) {
            speed += random.nextInt(2);
            skill += random.nextInt(2);
            chaosTolerance += random.nextInt(4);
            repMax += random.nextInt(2);
        }

        // Tradeoff candidates: fast but sloppy, or polished but slower.
        if (random.nextDouble() < 0.28) {
            if (random.nextBoolean()) {
                speed += 2;
                skill = Math.max(1, skill - 1);
                repMin -= 1;
            } else {
                skill += 2;
                speed = Math.max(0, speed - 1);
                chaosTolerance += 2;
            }
        }

        double power = speed * 6.0 + skill * 5.0 + Math.max(0, repMax) * 3.0 + chaosTolerance * 0.35
                + (base.securityBonus() * 8.0) + (base.tipBonus() * 220.0) + (base.tipRate() * 260.0)
                + ((base.capacityMultiplier() - 1.0) * 320.0);
        double statPremium = power * (0.015 + progression * 0.010);
        double levelPremium = Math.max(0.0, progression * 14.0);
        double weeklyWage = Math.max(18.0, base.weeklyWage() + statPremium + levelPremium);

        return new StaffTemplate(
                Math.max(0, speed),
                Math.max(1, skill),
                Math.min(repMin, repMax),
                Math.max(repMin, repMax),
                weeklyWage,
                base.capacityMultiplier(),
                base.tipRate(),
                base.tipBonus(),
                base.securityBonus(),
                Math.max(20, Math.min(100, chaosTolerance)),
                base.morale()
        );
    }

    public static StaffTemplate templateFor(Staff.Type type, Random random) {
        StaffTemplate base = baseTemplateFor(type, random);
        return new StaffTemplate(
                base.serveCapacity(),
                base.skill(),
                base.repMin(),
                base.repMax(),
                applyWageMultiplier(base.weeklyWage()),
                base.capacityMultiplier(),
                base.tipRate(),
                base.tipBonus(),
                base.securityBonus(),
                base.chaosTolerance(),
                base.morale()
        );
    }

    static double baseWeeklyWageFor(Staff.Type type, Random random) {
        return baseTemplateFor(type, random).weeklyWage();
    }

    public static double wageMultiplier() {
        return WAGE_MULTIPLIER;
    }

    private static StaffTemplate baseTemplateFor(Staff.Type type, Random random) {
        return switch (type) {
            case TRAINEE -> new StaffTemplate(
                    1 + random.nextInt(2),
                    2 + random.nextInt(3),
                    -1, 3,
                    30 + random.nextInt(16),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    45,
                    68
            );
            case EXPERIENCED -> new StaffTemplate(
                    2 + random.nextInt(3),
                    5 + random.nextInt(3),
                    -2, 4,
                    55 + random.nextInt(31),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    55,
                    70
            );
            case SPEED -> new StaffTemplate(
                    5 + random.nextInt(4),
                    6 + random.nextInt(4),
                    -6, 2,
                    110 + random.nextInt(71),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    50,
                    64
            );
            case CHARISMA -> new StaffTemplate(
                    2 + random.nextInt(2),
                    5 + random.nextInt(3),
                    1, 5,
                    70 + random.nextInt(36),
                    1.0,
                    0.0,
                    0.02,
                    0,
                    52,
                    72
            );
            case SECURITY -> new StaffTemplate(
                    1 + random.nextInt(2),
                    5 + random.nextInt(3),
                    -2, 2,
                    75 + random.nextInt(36),
                    1.0,
                    0.0,
                    0.0,
                    1,
                    70,
                    70
            );
            case CHEF -> new StaffTemplate(
                    0,
                    5 + random.nextInt(4),
                    -1, 3,
                    80 + random.nextInt(61),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    55,
                    68
            );
            case HEAD_CHEF -> new StaffTemplate(
                    0,
                    9 + random.nextInt(4),
                    1, 4,
                    140 + random.nextInt(71),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    60,
                    78
            );
            case SOUS_CHEF -> new StaffTemplate(
                    0,
                    8 + random.nextInt(4),
                    0, 3,
                    110 + random.nextInt(61),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    58,
                    74
            );
            case CHEF_DE_PARTIE -> new StaffTemplate(
                    0,
                    7 + random.nextInt(3),
                    -1, 2,
                    95 + random.nextInt(56),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    55,
                    72
            );
            case KITCHEN_ASSISTANT -> new StaffTemplate(
                    0,
                    4 + random.nextInt(3),
                    -1, 1,
                    70 + random.nextInt(41),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    52,
                    70
            );
            case KITCHEN_PORTER -> new StaffTemplate(
                    0,
                    3 + random.nextInt(2),
                    -2, 1,
                    55 + random.nextInt(31),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    50,
                    68
            );
            case ASSISTANT_MANAGER -> new StaffTemplate(
                    0,
                    6 + random.nextInt(4),
                    -2, 4,
                    65 + random.nextInt(51),
                    1.05 + (random.nextInt(11) / 100.0),
                    0.01 + (random.nextInt(3) / 100.0),
                    0.0,
                    0,
                    60,
                    74
            );
            case MANAGER -> new StaffTemplate(
                    0,
                    7 + random.nextInt(4),
                    -3, 5,
                    90 + random.nextInt(71),
                    1.10 + (random.nextInt(26) / 100.0),
                    0.02 + (random.nextInt(6) / 100.0),
                    0.0,
                    0,
                    65,
                    76
            );
            case RECEPTION_TRAINEE -> new StaffTemplate(
                    0,
                    4 + random.nextInt(2),
                    0, 0,
                    40 + random.nextInt(21),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    48,
                    70
            );
            case RECEPTIONIST -> new StaffTemplate(
                    0,
                    5 + random.nextInt(3),
                    0, 0,
                    60 + random.nextInt(31),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    50,
                    72
            );
            case SENIOR_RECEPTIONIST -> new StaffTemplate(
                    0,
                    6 + random.nextInt(3),
                    0, 0,
                    85 + random.nextInt(46),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    55,
                    74
            );
            case HOUSEKEEPING_TRAINEE -> new StaffTemplate(
                    0,
                    4 + random.nextInt(2),
                    0, 0,
                    35 + random.nextInt(21),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    45,
                    70
            );
            case HOUSEKEEPER -> new StaffTemplate(
                    0,
                    5 + random.nextInt(2),
                    0, 0,
                    55 + random.nextInt(26),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    50,
                    72
            );
            case HEAD_HOUSEKEEPER -> new StaffTemplate(
                    0,
                    6 + random.nextInt(3),
                    0, 0,
                    75 + random.nextInt(36),
                    1.0,
                    0.0,
                    0.0,
                    0,
                    55,
                    74
            );
            case DUTY_MANAGER -> new StaffTemplate(
                    0,
                    6 + random.nextInt(3),
                    0, 0,
                    95 + random.nextInt(46),
                    1.02,
                    0.0,
                    0.0,
                    0,
                    60,
                    74
            );
        };
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double applyWageMultiplier(double wage) {
        return wage * WAGE_MULTIPLIER;
    }
}
