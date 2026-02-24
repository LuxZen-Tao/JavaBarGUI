package com.luxzentao.javabar.core;

public class Main {
    public static void main(String[] args) {
        GameState state = GameFactory.newGame();
        javax.swing.SwingUtilities.invokeLater(() -> {
            UiTheme.apply();
            new WineBarGUI(state).show();
        });
    }
}
