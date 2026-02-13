import javax.swing.JTextPane;
import java.util.HashMap;
import java.util.Map;

public class IdentityAndPressToneTests {
    public static void main(String[] args) {
        testPressToneAffectsTierDistribution();
        testCurrentIdentityUsedInPunterSystem();
        System.out.println("All IdentityAndPressToneTests passed.");
        System.exit(0);
    }

    /**
     * Test that press tone affects patron tier distribution.
     * Positive press tone (RESPECTABLE) should attract more higher-tier patrons.
     * Negative press tone (UNDERGROUND) should attract more lower-tier patrons.
     */
    private static void testPressToneAffectsTierDistribution() {
        // Test with RESPECTABLE identity (positive press tone: +0.50)
        GameState stateRespectable = GameFactory.newGame();
        stateRespectable.currentIdentity = PubIdentity.RESPECTABLE;
        stateRespectable.reputation = 50; // Medium reputation to isolate press tone effect
        
        UILogger log = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(stateRespectable, log);
        InventorySystem inv = new InventorySystem(stateRespectable);
        EventSystem events = new EventSystem(stateRespectable, eco, log);
        RumorSystem rumors = new RumorSystem(stateRespectable, log);
        PunterSystem punterSysRespectable = new PunterSystem(stateRespectable, eco, inv, events, rumors, log);
        
        // Generate many patrons and count tier distribution
        Map<Punter.Tier, Integer> respectableTiers = new HashMap<>();
        for (int i = 0; i < 200; i++) {
            punterSysRespectable.seedNightPunters(1);
            if (!stateRespectable.nightPunters.isEmpty()) {
                Punter p = stateRespectable.nightPunters.get(0);
                respectableTiers.put(p.getTier(), respectableTiers.getOrDefault(p.getTier(), 0) + 1);
                stateRespectable.nightPunters.clear();
            }
        }
        
        // Test with UNDERGROUND identity (negative press tone: -0.20)
        GameState stateUnderground = GameFactory.newGame();
        stateUnderground.currentIdentity = PubIdentity.UNDERGROUND;
        stateUnderground.reputation = 50; // Same reputation for comparison
        
        UILogger log2 = new UILogger(new JTextPane());
        EconomySystem eco2 = new EconomySystem(stateUnderground, log2);
        InventorySystem inv2 = new InventorySystem(stateUnderground);
        EventSystem events2 = new EventSystem(stateUnderground, eco2, log2);
        RumorSystem rumors2 = new RumorSystem(stateUnderground, log2);
        PunterSystem punterSysUnderground = new PunterSystem(stateUnderground, eco2, inv2, events2, rumors2, log2);
        
        Map<Punter.Tier, Integer> undergroundTiers = new HashMap<>();
        for (int i = 0; i < 200; i++) {
            punterSysUnderground.seedNightPunters(1);
            if (!stateUnderground.nightPunters.isEmpty()) {
                Punter p = stateUnderground.nightPunters.get(0);
                undergroundTiers.put(p.getTier(), undergroundTiers.getOrDefault(p.getTier(), 0) + 1);
                stateUnderground.nightPunters.clear();
            }
        }
        
        // Verify: RESPECTABLE should have more higher-tier patrons
        int respectableHighTier = respectableTiers.getOrDefault(Punter.Tier.BIG_SPENDER, 0) + 
                                  respectableTiers.getOrDefault(Punter.Tier.DECENT, 0);
        int undergroundHighTier = undergroundTiers.getOrDefault(Punter.Tier.BIG_SPENDER, 0) + 
                                  undergroundTiers.getOrDefault(Punter.Tier.DECENT, 0);
        
        // Verify: UNDERGROUND should have more lower-tier patrons
        int respectableLowTier = respectableTiers.getOrDefault(Punter.Tier.LOWLIFE, 0);
        int undergroundLowTier = undergroundTiers.getOrDefault(Punter.Tier.LOWLIFE, 0);
        
        System.out.println("RESPECTABLE high-tier patrons: " + respectableHighTier + " vs UNDERGROUND: " + undergroundHighTier);
        System.out.println("RESPECTABLE low-tier patrons: " + respectableLowTier + " vs UNDERGROUND: " + undergroundLowTier);
        
        // Press tone should create a noticeable difference (at least 10% difference in distribution)
        assert respectableHighTier > undergroundHighTier : 
            "RESPECTABLE should attract more high-tier patrons due to positive press tone";
        assert undergroundLowTier > respectableLowTier : 
            "UNDERGROUND should attract more low-tier patrons due to negative press tone";
        
        System.out.println("✓ Press tone affects tier distribution correctly");
    }

    /**
     * Test that currentIdentity is used in PunterSystem tier adjustment.
     */
    private static void testCurrentIdentityUsedInPunterSystem() {
        GameState state = GameFactory.newGame();
        state.currentIdentity = PubIdentity.FAMILY_FRIENDLY; // Positive press tone: +0.55
        state.pubIdentity = PubIdentity.NEUTRAL; // Legacy field set to neutral
        state.reputation = 50;
        
        UILogger log = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(state, log);
        InventorySystem inv = new InventorySystem(state);
        EventSystem events = new EventSystem(state, eco, log);
        RumorSystem rumors = new RumorSystem(state, log);
        PunterSystem punterSys = new PunterSystem(state, eco, inv, events, rumors, log);
        
        // Generate patrons and verify they are affected by currentIdentity
        Map<Punter.Tier, Integer> tiers = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            punterSys.seedNightPunters(1);
            if (!state.nightPunters.isEmpty()) {
                Punter p = state.nightPunters.get(0);
                tiers.put(p.getTier(), tiers.getOrDefault(p.getTier(), 0) + 1);
                state.nightPunters.clear();
            }
        }
        
        // FAMILY_FRIENDLY has the highest positive press tone (+0.55) and wealthBias (+0.25)
        // so we should see fewer lowlifes and more higher-tier patrons
        int lowlifes = tiers.getOrDefault(Punter.Tier.LOWLIFE, 0);
        int highTier = tiers.getOrDefault(Punter.Tier.BIG_SPENDER, 0) + 
                       tiers.getOrDefault(Punter.Tier.DECENT, 0);
        
        System.out.println("FAMILY_FRIENDLY distribution - Lowlifes: " + lowlifes + ", High-tier: " + highTier);
        
        // With positive press tone and wealth bias, high-tier should outnumber lowlifes
        assert highTier > lowlifes : 
            "FAMILY_FRIENDLY currentIdentity should attract more high-tier patrons";
        
        System.out.println("✓ currentIdentity is used correctly in PunterSystem");
    }
}
