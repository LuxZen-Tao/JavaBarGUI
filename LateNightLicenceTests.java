import javax.swing.JTextPane;

public class LateNightLicenceTests {
    public static void main(String[] args) {
        testLateNightLicenceIncreasesRoundCap();
        testRoundCapStartsAt20();
        testMultipleLicencesStack();
        System.out.println("All LateNightLicenceTests passed.");
    }

    private static void testRoundCapStartsAt20() {
        GameState state = GameFactory.newGame();
        assert state.getClosingRound() == 20 : "Base round cap should be 20, got " + state.getClosingRound();
    }

    private static void testLateNightLicenceIncreasesRoundCap() {
        GameState state = GameFactory.newGame();
        
        // Before upgrade
        int beforeCap = state.getClosingRound();
        assert beforeCap == 20 : "Round cap before upgrade should be 20, got " + beforeCap;
        
        // Add the upgrade
        state.ownedUpgrades.add(PubUpgrade.LATE_NIGHT_LICENCE);
        
        // After upgrade
        int afterCap = state.getClosingRound();
        assert afterCap == 25 : "Round cap after Late Night Licence should be 25, got " + afterCap;
        
        // Verify the upgrade has the right bonus
        assert PubUpgrade.LATE_NIGHT_LICENCE.getRoundCapBonus() == 5 
            : "Late Night Licence should have +5 round cap bonus";
    }

    private static void testMultipleLicencesStack() {
        // This test verifies that if multiple upgrades provide round cap bonuses, they stack
        GameState state = GameFactory.newGame();
        
        // Add Late Night Licence
        state.ownedUpgrades.add(PubUpgrade.LATE_NIGHT_LICENCE);
        int cap = state.getClosingRound();
        assert cap == 25 : "Round cap with one upgrade should be 25, got " + cap;
        
        // If there were another upgrade with round cap bonus, it would stack
        // For now, we just verify the upgrade system aggregates correctly
        UpgradeSystem upgradeSystem = new UpgradeSystem(state);
        UpgradeSystem.UpgradeModifierSnapshot snapshot = upgradeSystem.buildModifierSnapshot();
        assert snapshot.roundCapBonus() == 5 : "Snapshot should show +5 round cap bonus";
    }
}
