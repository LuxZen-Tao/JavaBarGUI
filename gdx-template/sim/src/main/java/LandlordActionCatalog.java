import java.util.ArrayList;
import java.util.List;

public class LandlordActionCatalog {
    private static final List<LandlordActionDef> ACTIONS = buildActions();

    public static List<LandlordActionDef> actionsForTier(int tier) {
        List<LandlordActionDef> results = new ArrayList<>();
        for (LandlordActionDef def : ACTIONS) {
            if (def.getTier() == tier) results.add(def);
        }
        return results;
    }

    public static LandlordActionDef byId(LandlordActionId id) {
        for (LandlordActionDef def : ACTIONS) {
            if (def.getId() == id) return def;
        }
        return null;
    }

    public static List<LandlordActionDef> allActions() {
        return new ArrayList<>(ACTIONS);
    }

    private static List<LandlordActionDef> buildActions() {
        List<LandlordActionDef> actions = new ArrayList<>();

        actions.add(build(1, LandlordActionCategory.CLASSY, LandlordActionId.WORK_THE_ROOM,
                "Work the Room", "Greet regulars, smooth the vibe, and keep the chatter warm.",
                0.66, 2,
                range(1, 3, 1, 2, 0.02, 0.05, -2, 0),
                range(-1, -3, -1, 0, -0.02, 0.0, 1, 2),
                2, 1));
        actions.add(build(1, LandlordActionCategory.BALANCED, LandlordActionId.RUN_A_SPECIAL,
                "Run a Special", "Push a focused deal to lift tonight’s traffic and mood.",
                0.62, 2,
                range(0, 2, 0, 2, 0.03, 0.06, -1, 1),
                range(-1, -4, -1, -2, -0.03, -0.05, 1, 3),
                2, 1));
        actions.add(build(1, LandlordActionCategory.SHADY, LandlordActionId.PUSHY_UPSELL,
                "Pushy Upsell", "Lean on the staff to squeeze extra tickets from every table.",
                0.56, 2,
                range(-1, 1, -1, 0, 0.05, 0.09, 1, 3),
                range(-2, -5, -2, -3, -0.04, -0.08, 3, 5),
                2, 1));

        actions.add(build(2, LandlordActionCategory.CLASSY, LandlordActionId.LOCALS_NIGHT,
                "Locals Night", "Host a tidy locals-only push with friendly hospitality.",
                0.65, 3,
                range(2, 4, 1, 3, 0.03, 0.06, -3, -1),
                range(-2, -4, -1, 0, -0.03, -0.01, 2, 3),
                2, 1));
        actions.add(build(2, LandlordActionCategory.BALANCED, LandlordActionId.PARTNER_PROMO,
                "Partner Promo", "Coordinate a low-key promo with a nearby business.",
                0.60, 3,
                range(1, 3, 0, 2, 0.04, 0.07, -1, 1),
                range(-2, -5, -1, -3, -0.04, -0.07, 2, 4),
                2, 1));
        actions.add(build(2, LandlordActionCategory.SHADY, LandlordActionId.FLIRT_FOR_BUZZ,
                "Flirt for Buzz", "Turn up the charm for buzzier traffic, at a cost.",
                0.54, 3,
                range(-1, 2, -1, 0, 0.07, 0.12, 2, 4),
                range(-3, -6, -2, -4, -0.05, -0.09, 4, 6),
                2, 1));

        actions.add(build(3, LandlordActionCategory.CLASSY, LandlordActionId.VIP_HOSPITALITY,
                "VIP Hospitality", "Treat high-value guests with extra care and polish.",
                0.64, 3,
                range(3, 5, 2, 4, 0.04, 0.08, -4, -2),
                range(-2, -5, -1, -1, -0.03, -0.02, 2, 4),
                3, 2));
        actions.add(build(3, LandlordActionCategory.BALANCED, LandlordActionId.INVITE_REVIEWER,
                "Invite Reviewer", "Bring in a local reviewer and hope the room delivers.",
                0.58, 3,
                range(1, 4, 1, 3, 0.05, 0.09, -1, 2),
                range(-3, -6, -2, -4, -0.05, -0.08, 3, 5),
                3, 2));
        actions.add(build(3, LandlordActionCategory.SHADY, LandlordActionId.PLANT_A_RUMOR,
                "Plant a Rumor", "Seed a rumor to spike interest before anyone fact-checks.",
                0.52, 3,
                range(-2, 2, -1, 0, 0.09, 0.14, 3, 5),
                range(-4, -8, -3, -5, -0.06, -0.10, 5, 7),
                3, 2));

        actions.add(build(4, LandlordActionCategory.CLASSY, LandlordActionId.SPONSOR_CHARITY,
                "Sponsor Charity", "Tie the pub to a charity night for goodwill and respect.",
                0.63, 4,
                range(4, 7, 2, 5, 0.05, 0.10, -5, -3),
                range(-3, -6, -1, -2, -0.04, -0.02, 3, 5),
                3, 2));
        actions.add(build(4, LandlordActionCategory.BALANCED, LandlordActionId.EXCLUSIVE_DOOR_POLICY,
                "Exclusive Door Policy", "Dial up exclusivity to shape the crowd.",
                0.57, 4,
                range(2, 5, 1, 4, 0.06, 0.11, -2, 2),
                range(-4, -7, -2, -5, -0.06, -0.10, 4, 6),
                3, 2));
        actions.add(build(4, LandlordActionCategory.SHADY, LandlordActionId.QUIET_FIXER,
                "Quiet Fixer", "Lean on a fixer to smooth rough edges in the short term.",
                0.50, 4,
                range(-2, 3, -1, 0, 0.11, 0.16, 4, 6),
                range(-5, -9, -4, -6, -0.07, -0.12, 6, 8),
                3, 2));

        actions.add(build(5, LandlordActionCategory.CLASSY, LandlordActionId.HIGH_SOCIETY_NETWORKING,
                "High Society Networking", "Work the elite circles for long-lasting prestige.",
                0.62, 4,
                range(5, 9, 3, 6, 0.06, 0.12, -6, -4),
                range(-4, -7, -2, -3, -0.05, -0.03, 4, 6),
                3, 2));
        actions.add(build(5, LandlordActionCategory.BALANCED, LandlordActionId.CONTROL_THE_NARRATIVE,
                "Control the Narrative", "Shape the story before it shapes the pub.",
                0.56, 4,
                range(3, 6, 2, 5, 0.07, 0.13, -2, 3),
                range(-5, -8, -3, -6, -0.07, -0.12, 5, 7),
                3, 2));
        actions.add(build(5, LandlordActionCategory.SHADY, LandlordActionId.SABOTAGE_RIVAL,
                "Sabotage Rival", "Spike traffic with dirty tricks and weather the blowback.",
                0.48, 4,
                range(-3, 4, -1, 0, 0.13, 0.18, 5, 7),
                range(-6, -10, -5, -7, -0.08, -0.14, 7, 9),
                3, 2));

        return actions;
    }

    private static LandlordActionDef build(int tier,
                                           LandlordActionCategory category,
                                           LandlordActionId id,
                                           String name,
                                           String description,
                                           double baseChance,
                                           int cooldown,
                                           LandlordActionEffectRange success,
                                           LandlordActionEffectRange failure,
                                           int successTrafficRounds,
                                           int failureTrafficRounds) {
        return new LandlordActionDef(id, tier, category, name, description, baseChance, cooldown, success, failure,
                successTrafficRounds, failureTrafficRounds);
    }

    private static LandlordActionEffectRange range(int repMin, int repMax,
                                                   int moraleMin, int moraleMax,
                                                   double trafficMinPct, double trafficMaxPct,
                                                   double chaosMin, double chaosMax) {
        return new LandlordActionEffectRange(repMin, repMax, moraleMin, moraleMax, trafficMinPct, trafficMaxPct,
                chaosMin, chaosMax);
    }
}
