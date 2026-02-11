import java.util.List;

public final class VIPRegular {
    private final String name;
    private final VIPArchetype archetype;
    private final List<VIPPreferenceTag> preferenceTags;
    private final int toleranceThreshold;
    private int loyalty;
    private VIPArcStage arcStage;

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

    void adjustLoyalty(int delta) {
        loyalty = Math.max(0, Math.min(100, loyalty + delta));
        if (loyalty >= 85) arcStage = VIPArcStage.DEVOTED;
        else if (loyalty >= 65) arcStage = VIPArcStage.LOYAL;
        else if (loyalty >= 45) arcStage = VIPArcStage.CURIOUS;
        else if (loyalty <= 25) arcStage = VIPArcStage.FADING;
        else arcStage = VIPArcStage.NEWCOMER;
    }
}
