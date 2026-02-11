public class SimBridge {
    private GameState gameState;
    private Simulation simulation;
    private UILogger logger;

    private int prevNightUnserved;
    private int prevNightRefunds;
    private int prevNightFights;

    public void newGame() {
        this.gameState = GameFactory.newGame();
        this.logger = new UILogger();
        this.simulation = new Simulation(gameState, logger);
        syncCounters();
    }

    public void openService() {
        ensureGame();
        simulation.openNight();
        syncCounters();
    }

    public void closeService() {
        ensureGame();
        simulation.closeNight("Manual close");
        syncCounters();
    }

    public void advanceTick() {
        ensureGame();
        if (!gameState.nightOpen) {
            simulation.openNight();
        }
        simulation.playRound();
    }

    public PresentationSnapshot snapshot() {
        ensureGame();
        int unservedDelta = Math.max(0, gameState.nightUnserved - prevNightUnserved);
        int refundsDelta = Math.max(0, gameState.nightRefunds - prevNightRefunds);
        int fightsDelta = Math.max(0, gameState.nightFights - prevNightFights);

        PresentationSnapshot snapshot = new PresentationSnapshot(
                gameState.cash,
                gameState.totalCreditBalance(),
                gameState.reputation,
                gameState.chaos,
                gameState.nightOpen,
                gameState.weekCount,
                gameState.dayIndex + 1,
                gameState.roundInNight,
                gameState.nightPunters.size(),
                unservedDelta,
                refundsDelta,
                fightsDelta
        );

        prevNightUnserved = gameState.nightUnserved;
        prevNightRefunds = gameState.nightRefunds;
        prevNightFights = gameState.nightFights;

        return snapshot;
    }

    private void ensureGame() {
        if (simulation == null || gameState == null) {
            throw new IllegalStateException("No game running. Call newGame() first.");
        }
    }

    private void syncCounters() {
        prevNightUnserved = gameState.nightUnserved;
        prevNightRefunds = gameState.nightRefunds;
        prevNightFights = gameState.nightFights;
    }
}
