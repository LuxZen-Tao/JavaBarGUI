import javax.swing.JTextPane;

public class EarlyCloseRepPenaltyTests {
    public static void main(String[] args) {
        EarlyClose_RepPenalty();
        System.out.println("All EarlyCloseRepPenaltyTests passed.");
    }

    private static void EarlyClose_RepPenalty() {
        GameState state = GameFactory.newGame();
        Simulation sim = new Simulation(state, new UILogger(new JTextPane()));

        state.roundInNight = 10;
        state.nightOpen = true;
        int before = state.reputation;

        sim.closeNight("Manual early close for test");

        int delta = state.reputation - before;
        assert delta == -3 : "Early close should apply rounded -3 integer rep penalty.";
        assert state.lastEarlyCloseRepPenalty == -3 : "State should track rounded -3 penalty.";
        assert !state.earlyClosePenaltyLog.isEmpty() : "Early close should be logged.";
        assert state.earlyClosePenaltyLog.peekFirst().contains("spec -2.5")
                : "Early close log should clearly include -2.5 spec penalty.";
    }
}
