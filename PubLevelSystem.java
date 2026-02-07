import java.util.EnumSet;

public class PubLevelSystem {

    private static final EnumSet<PubUpgrade> KEY_UPGRADES = EnumSet.of(
            PubUpgrade.EXTENDED_BAR,
            PubUpgrade.KITCHEN,
            PubUpgrade.BEER_GARDEN
    );

    public void updatePubLevel(GameState s) {
        int ownedKeys = 0;
        for (PubUpgrade upgrade : KEY_UPGRADES) {
            if (s.ownedUpgrades.contains(upgrade)) ownedKeys++;
        }

        int level = switch (ownedKeys) {
            case 3 -> 3;
            case 2 -> 2;
            case 1 -> 1;
            default -> 0;
        };

        s.pubLevel = level;
        s.pubLevelServeCapBonus = level * 1;
        s.pubLevelBarCapBonus = level * 2;
        s.pubLevelTrafficBonusPct = level * 0.05;
        s.pubLevelRepMultiplier = switch (level) {
            case 3 -> 1.10;
            case 2 -> 1.05;
            case 1 -> 1.02;
            default -> 0.98;
        };

        s.pubLevelStaffCapBonus = level;
        s.pubLevelManagerCapBonus = Math.max(0, level - 1);
        s.pubLevelChefCapBonus = Math.max(0, level - 1);
        s.pubLevelBouncerCapBonus = Math.max(0, level - 1);
    }

    public boolean canAccessUpgradeTier(GameState s, PubUpgrade upgrade) {
        int tier = upgrade.getTier();
        if (tier <= 1) return true;
        return s.pubLevel >= tier - 1;
    }
}
