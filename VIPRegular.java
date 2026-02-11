import java.util.EnumSet;
import java.util.List;

public final class VIPRegular {
    private final String name;
    private final VIPArchetype archetype;
    private final List<VIPPreferenceTag> preferenceTags;
    private final int toleranceThreshold;
    private int loyalty;
    private VIPArcStage arcStage;
    private final EnumSet<VIPArcStage> consequenceTriggered = EnumSet.noneOf(VIPArcStage.class);

    public VIPRegular(String name,
                      VIPArchetype archetype,
                      List<VIPPreferenceTag> preferenceTags,
                      int toleranceThreshold,
                      int loyalty,
                      VIPArcStage arcStage) {
        this.name = name;
        this.archetype = archetype;
        this.preferenceTags = List.copyOf(preferenceTags);
        this.toleranceThreshold = Math.max(0, Math.min(100, toleranceThreshold));
        this.loyalty = Math.max(0, Math.min(100, loyalty));
        this.arcStage = arcStage;
    }

    public String getName() { return name; }
    public VIPArchetype getArchetype() { return archetype; }
    public List<VIPPreferenceTag> getPreferenceTags() { return preferenceTags; }
    public int getToleranceThreshold() { return toleranceThreshold; }
    public int getLoyalty() { return loyalty; }
    public VIPArcStage getArcStage() { return arcStage; }

    public boolean isConsequenceTriggered(VIPArcStage stage) {
        return consequenceTriggered.contains(stage);
    }

    public void markConsequenceTriggered(VIPArcStage stage) {
        consequenceTriggered.add(stage);
    }

    VIPArcStage adjustLoyalty(int delta) {
        VIPArcStage previous = arcStage;
        loyalty = Math.max(0, Math.min(100, loyalty + delta));
        arcStage = stageFromLoyalty(loyalty);
        return previous;
    }

    private VIPArcStage stageFromLoyalty(int value) {
        if (value >= 85) return VIPArcStage.ADVOCATE;
        if (value >= 65) return VIPArcStage.LOYAL;
        if (value >= 50) return VIPArcStage.WARMING;
        if (value <= 15) return VIPArcStage.BACKLASH;
        if (value <= 30) return VIPArcStage.DISGRUNTLED;
        if (value <= 45) return VIPArcStage.ANNOYED;
        return VIPArcStage.NEUTRAL;
    }
}
