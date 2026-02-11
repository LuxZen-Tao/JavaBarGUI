import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public final class VIPSystem {
    public record VIPConsequence(VIPRegular vip, VIPArcStage stage, String popupTitle, String popupBody, String weeklyLine, String observationLine) {}

    private static final int TARGET_VIPS = 3;
    private final List<VIPRegular> roster = new ArrayList<>();

    public List<VIPRegular> roster() {
        return Collections.unmodifiableList(roster);
    }

    public void ensureRosterFromNames(List<String> punterNames, Random random) {
        if (!FeatureFlags.FEATURE_VIPS) return;
        if (punterNames == null || punterNames.isEmpty()) return;

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String n : punterNames) {
            if (n != null && !n.isBlank()) unique.add(n.trim());
        }
        if (unique.isEmpty()) return;

        List<String> candidates = new ArrayList<>(unique);
        Collections.shuffle(candidates, random == null ? new Random(0L) : random);

        for (String candidate : candidates) {
            if (roster.size() >= TARGET_VIPS) break;
            if (containsName(candidate)) continue;
            roster.add(newVip(candidate, random == null ? new Random(candidate.hashCode()) : random));
        }
    }

    public void evaluateNight(VIPNightOutcome outcome) {
        evaluateNightWithConsequences(outcome);
    }

    public List<VIPConsequence> evaluateNightWithConsequences(VIPNightOutcome outcome) {
        if (!FeatureFlags.FEATURE_VIPS || outcome == null) return List.of();

        List<VIPConsequence> consequences = new ArrayList<>();
        for (VIPRegular vip : roster) {
            int delta = loyaltyDelta(vip, outcome);
            VIPArcStage previous = vip.adjustLoyalty(delta);
            VIPArcStage current = vip.getArcStage();

            if (current != previous && (current == VIPArcStage.ADVOCATE || current == VIPArcStage.BACKLASH)
                    && !vip.isConsequenceTriggered(current)) {
                vip.markConsequenceTriggered(current);
                consequences.add(buildConsequence(vip, current));
            }
        }
        return consequences;
    }

    private VIPConsequence buildConsequence(VIPRegular vip, VIPArcStage stage) {
        if (stage == VIPArcStage.ADVOCATE) {
            return new VIPConsequence(
                    vip,
                    stage,
                    "VIP Advocate",
                    vip.getName() + " became an advocate and is championing your bar.",
                    "VIP " + vip.getName() + " is now an advocate, drawing friend groups and positive chatter.",
                    vip.getName() + " is hyping your venue around the district."
            );
        }
        return new VIPConsequence(
                vip,
                stage,
                "VIP Backlash",
                vip.getName() + " turned against the bar and is spreading backlash.",
                "VIP " + vip.getName() + " entered backlash; word-of-mouth turned hostile.",
                vip.getName() + " was openly critical of tonight's experience."
        );
    }

    int loyaltyDelta(VIPRegular vip, VIPNightOutcome outcome) {
        int delta = 0;
        for (VIPPreferenceTag tag : vip.getPreferenceTags()) {
            switch (tag) {
                case SERVICE -> delta += (outcome.unservedCount() <= 1) ? 2 : -2;
                case VALUE -> delta += (outcome.priceMultiplier() <= 1.10) ? 1 : -1;
                case CALM -> delta += (outcome.fightCount() == 0) ? 1 : -2;
                case EVENTS -> delta += (outcome.eventCount() > 0) ? 1 : 0;
                case QUALITY -> delta += (outcome.foodQualitySignal() >= 0.6) ? 1 : -1;
            }
        }
        if (outcome.refundCount() > vip.getToleranceThreshold() / 20) delta -= 1;
        return Math.max(-5, Math.min(5, delta));
    }

    private VIPRegular newVip(String name, Random random) {
        VIPArchetype archetype = VIPArchetype.values()[Math.abs(name.hashCode()) % VIPArchetype.values().length];
        List<VIPPreferenceTag> tags = preferenceFor(archetype);
        int tolerance = switch (archetype) {
            case SOCIAL_BUTTERFLY -> 45;
            case CONNOISSEUR -> 35;
            case VALUE_SEEKER -> 50;
            case NIGHT_OWL -> 40;
        };
        int baseLoyalty = 40 + (random.nextInt(11));
        return new VIPRegular(name, archetype, tags, tolerance, baseLoyalty, VIPArcStage.NEUTRAL);
    }

    private List<VIPPreferenceTag> preferenceFor(VIPArchetype archetype) {
        return switch (archetype) {
            case SOCIAL_BUTTERFLY -> List.of(VIPPreferenceTag.SERVICE, VIPPreferenceTag.EVENTS, VIPPreferenceTag.CALM);
            case CONNOISSEUR -> List.of(VIPPreferenceTag.QUALITY, VIPPreferenceTag.SERVICE, VIPPreferenceTag.CALM);
            case VALUE_SEEKER -> List.of(VIPPreferenceTag.VALUE, VIPPreferenceTag.SERVICE);
            case NIGHT_OWL -> List.of(VIPPreferenceTag.EVENTS, VIPPreferenceTag.VALUE, VIPPreferenceTag.CALM);
        };
    }

    private boolean containsName(String name) {
        for (VIPRegular vip : roster) {
            if (vip.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
