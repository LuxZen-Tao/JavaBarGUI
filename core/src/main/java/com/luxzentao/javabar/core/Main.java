package com.luxzentao.javabar.core;

/**
 * Legacy CLI-friendly entrypoint for core-only smoke checks.
 *
 * Desktop rendering is launched from :lwjgl3 via Lwjgl3Launcher.
 */
public class Main {
    public static void main(String[] args) {
        GameState state = GameFactory.newGame();
        Simulation simulation = new Simulation(state, new UILogger());
        System.out.println("JavaBar core initialized. Week=" + state.weekCount + ", Cash=" + state.cash);
        // Keep one no-op reference to ensure simulation object is retained for smoke checks.
        if (simulation == null) {
            throw new IllegalStateException("Simulation failed to initialize");
        }
    }
}
