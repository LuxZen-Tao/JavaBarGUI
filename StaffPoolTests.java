import javax.swing.JTextPane;

public class StaffPoolTests {
    public static void main(String[] args) {
        testAssistantManagerCounts();
        testAssistantManagerRespectsManagerCap();
        testAssistantManagerIgnoresFohCap();
        System.out.println("All StaffPoolTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testAssistantManagerCounts() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.hireStaff(Staff.Type.ASSISTANT_MANAGER);
        assert state.managerPoolCount() == 1 : "Assistant manager should count toward manager pool.";
        assert state.fohStaffCount() == 0 : "Assistant manager should not count toward FOH.";
        assert state.staff().staffCount() == state.fohStaffCount() : "Staff summary FOH count should exclude assistant managers.";
    }

    private static void testAssistantManagerRespectsManagerCap() {
        GameState state = GameFactory.newGame();
        state.managerCap = 1;
        Simulation sim = newSimulation(state);
        sim.hireStaff(Staff.Type.ASSISTANT_MANAGER);
        int managersAfterHire = state.managerPoolCount();
        sim.hireStaff(Staff.Type.MANAGER);
        assert state.managerPoolCount() == managersAfterHire : "Manager cap should include assistant managers.";
    }

    private static void testAssistantManagerIgnoresFohCap() {
        GameState state = GameFactory.newGame();
        state.fohStaffCap = 0;
        Simulation sim = newSimulation(state);
        sim.hireStaff(Staff.Type.ASSISTANT_MANAGER);
        assert state.assistantManagerCount() == 1 : "Assistant manager should hire even at FOH cap.";
        assert state.fohStaffCount() == 0 : "Assistant manager should not change FOH count.";
    }
}
