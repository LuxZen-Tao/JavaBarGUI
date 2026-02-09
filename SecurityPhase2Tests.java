import javax.swing.JTextPane;
import java.util.Random;

public class SecurityPhase2Tests {
    public static void main(String[] args) {
        testReinforcedDoorTiers();
        testLightingTierEffects();
        testCctvProofEffect();
        testBurglarAlarmTiers();
        testMissionControlBreakdown();
        testObservationConsistency();
        System.out.println("All SecurityPhase2Tests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testReinforcedDoorTiers() {
        GameState tier1 = GameFactory.newGame();
        tier1.baseSecurityLevel = 0;
        tier1.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_I);
        Simulation sim1 = newSimulation(tier1);
        sim1.openNight();
        int sec1 = sim1.securityBreakdown().total();
        double mult1 = tier1.upgradeIncidentChanceMultiplier;

        GameState tier2 = GameFactory.newGame();
        tier2.baseSecurityLevel = 0;
        tier2.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_II);
        Simulation sim2 = newSimulation(tier2);
        sim2.openNight();
        int sec2 = sim2.securityBreakdown().total();
        double mult2 = tier2.upgradeIncidentChanceMultiplier;

        GameState tier3 = GameFactory.newGame();
        tier3.baseSecurityLevel = 0;
        tier3.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_III);
        Simulation sim3 = newSimulation(tier3);
        sim3.openNight();
        int sec3 = sim3.securityBreakdown().total();
        double mult3 = tier3.upgradeIncidentChanceMultiplier;

        assert sec2 > sec1 : "Tier 2 door should increase effective security.";
        assert sec3 > sec2 : "Tier 3 door should increase effective security.";
        assert mult2 < mult1 : "Tier 2 door should reduce incident chance.";
        assert mult3 < mult2 : "Tier 3 door should reduce incident chance further.";
    }

    private static void testLightingTierEffects() {
        GameState base = GameFactory.newGame();
        base.baseSecurityLevel = 0;
        Simulation baseSim = newSimulation(base);
        baseSim.openNight();
        double baseIncident = base.upgradeIncidentChanceMultiplier;

        GameState lit = GameFactory.newGame();
        lit.baseSecurityLevel = 0;
        lit.ownedUpgrades.add(PubUpgrade.LIGHTING_III);
        Simulation litSim = newSimulation(lit);
        litSim.openNight();
        double litIncident = lit.upgradeIncidentChanceMultiplier;
        assert litIncident < baseIncident : "Lighting tier should reduce incident chance.";

        lit.random.setSeed(42);
        base.random.setSeed(42);
        EconomySystem ecoBase = new EconomySystem(base, new UILogger(new JTextPane()));
        EconomySystem ecoLit = new EconomySystem(lit, new UILogger(new JTextPane()));
        StaffSystem staffBase = new StaffSystem(base, ecoBase, new UpgradeSystem(base));
        StaffSystem staffLit = new StaffSystem(lit, ecoLit, new UpgradeSystem(lit));
        Staff s1 = StaffFactory.createStaff(1, "Sam", Staff.Type.TRAINEE, new Random(2));
        Staff s2 = StaffFactory.createStaff(1, "Sam", Staff.Type.TRAINEE, new Random(2));
        base.fohStaff.add(s1);
        lit.fohStaff.add(s2);
        base.teamMorale = 70.0;
        lit.teamMorale = 70.0;
        staffBase.adjustMoraleAfterRound(6, 2, -30, 0.0, 0, 45.0);
        staffLit.adjustMoraleAfterRound(6, 2, -30, 0.0, 0, 45.0);
        assert lit.teamMorale > base.teamMorale : "Lighting morale stability should soften morale loss.";
    }

    private static void testCctvProofEffect() {
        GameState state = GameFactory.newGame();
        int baseline = state.mitigateSecurityRepHit(-10);

        state.ownedUpgrades.add(PubUpgrade.CCTV);
        Simulation sim = newSimulation(state);
        sim.openNight();
        int mitigated = state.mitigateSecurityRepHit(-10);
        assert mitigated > baseline : "CCTV should reduce rep loss.";
    }

    private static void testBurglarAlarmTiers() {
        GameState base = GameFactory.newGame();
        Simulation simBase = newSimulation(base);
        simBase.openNight();
        double baseLoss = base.upgradeLossSeverityMultiplier;
        double baseIncident = base.upgradeIncidentChanceMultiplier;

        GameState alarm = GameFactory.newGame();
        alarm.ownedUpgrades.add(PubUpgrade.BURGLAR_ALARM_III);
        Simulation simAlarm = newSimulation(alarm);
        simAlarm.openNight();
        double alarmLoss = alarm.upgradeLossSeverityMultiplier;
        double alarmIncident = alarm.upgradeIncidentChanceMultiplier;

        assert alarmLoss < baseLoss : "Alarm tier should reduce loss severity.";
        assert alarmIncident < baseIncident : "Alarm tier should reduce incident chance.";
    }

    private static void testMissionControlBreakdown() {
        GameState state = GameFactory.newGame();
        state.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_II);
        state.ownedUpgrades.add(PubUpgrade.LIGHTING_II);
        state.ownedUpgrades.add(PubUpgrade.BURGLAR_ALARM_I);
        Simulation sim = newSimulation(state);
        sim.openNight();
        String security = sim.buildMetricsSnapshot().security;
        assert security.contains("Reinforced Door (Tier 2)") : "Security tab should show door tier.";
        assert security.contains("Lighting (Tier 2)") : "Security tab should show lighting tier.";
        assert security.contains("Burglar Alarm (Tier 1)") : "Security tab should show alarm tier.";
        assert security.contains("Upgrade incident chance") : "Security tab should show upgrade incident modifier.";
        assert security.contains("Upgrade loss severity") : "Security tab should show loss severity modifier.";
    }

    private static void testObservationConsistency() {
        GameState state = GameFactory.newGame();
        state.lastObservationRound = -999;
        state.lastSecurityEventRound = state.currentRoundIndex();
        state.random.setSeed(7);
        ObservationEngine engine = new ObservationEngine();
        ObservationEngine.ObservationContext ctx = new ObservationEngine.ObservationContext(
                state.currentRoundIndex(),
                5,
                0,
                1,
                0,
                0,
                0,
                0,
                1.0,
                0.0,
                6,
                0,
                false,
                0,
                false,
                false
        );
        ObservationEngine.ObservationResult result = engine.nextObservation(state, ctx);
        if (result == null) return;
        String text = result.text().toLowerCase();
        assert !text.contains("camera") : "Observation should not mention CCTV when not installed.";
        assert !text.contains("cctv") : "Observation should not mention CCTV when not installed.";
        assert !text.contains("alarm") : "Observation should not mention alarm when not installed.";
        assert !text.contains("lighting") : "Observation should not mention lighting when not installed.";
        assert !text.contains("reinforced") : "Observation should not mention reinforced door when not installed.";
    }
}
