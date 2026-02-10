import javax.swing.JTextPane;

public class WeatherMarshallTests {
    public static void main(String[] args) {
        testDateAndWeatherAdvance();
        testMarshallCapUpgrades();
        testMarshallMitigatesInnEvents();
        System.out.println("All WeatherMarshallTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testDateAndWeatherAdvance() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        String day0 = state.dateString();
        assert "16/01/1989".equals(day0) : "Start date should be 16/01/1989.";
        String weather0 = state.currentWeather;
        sim.openNight();
        assert weather0.equals(state.currentWeather) : "Weather should persist during the day.";
        sim.closeNight("Closing time.");
        assert "17/01/1989".equals(state.dateString()) : "Date should advance after a day.";
        assert state.currentWeather != null && !state.currentWeather.isBlank() : "Weather should be set daily.";
    }

    private static void testMarshallCapUpgrades() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.MARSHALLS_I);
        assert state.isMarshallUnlocked() : "Marshalls should unlock with upgrade.";
        assert state.marshallCap == 2 : "Marshall cap should start at 2.";
        sim.hireMarshall();
        sim.hireMarshall();
        sim.hireMarshall();
        assert state.marshallCount() == 2 : "Marshall cap should be enforced.";
        sim.installUpgradeForTest(PubUpgrade.MARSHALLS_II);
        assert state.marshallCap == 4 : "Marshall cap should increase with upgrades.";
        sim.hireMarshall();
        sim.hireMarshall();
        assert state.marshallCount() == 4 : "Marshall hires should fill upgraded cap.";
    }

    private static void testMarshallMitigatesInnEvents() {
        GameState base = GameFactory.newGame();
        Simulation simBase = newSimulation(base);
        simBase.installUpgradeForTest(PubUpgrade.INN_WING_1);
        base.roomsTotal = 5;
        base.roomPrice = 30.0;
        base.innRep = 60.0;
        base.cleanliness = 30.0;
        base.reputation = 20;
        base.random.setSeed(4);
        simBase.runInnNightly();
        double repWithout = base.innRep;

        GameState staffed = GameFactory.newGame();
        Simulation simStaffed = newSimulation(staffed);
        simStaffed.installUpgradeForTest(PubUpgrade.INN_WING_1);
        simStaffed.installUpgradeForTest(PubUpgrade.MARSHALLS_I);
        staffed.roomsTotal = 5;
        staffed.roomPrice = 30.0;
        staffed.innRep = 60.0;
        staffed.cleanliness = 30.0;
        staffed.reputation = 20;
        simStaffed.hireMarshall();
        staffed.random.setSeed(4);
        simStaffed.runInnNightly();
        double repWith = staffed.innRep;

        assert repWith >= repWithout : "Marshalls should mitigate inn rep losses.";
    }
}
