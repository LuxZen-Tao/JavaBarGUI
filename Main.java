void main() {
    GameState state = GameFactory.newGame();
    javax.swing.SwingUtilities.invokeLater(() -> {
        UiTheme.apply();
        new WineBarGUI(state).show();
    });
}
