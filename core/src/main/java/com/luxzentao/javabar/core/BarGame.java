package com.luxzentao.javabar.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BarGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;

    private Simulation simulation;
    private GameState state;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        state = GameFactory.newGame();
        simulation = new Simulation(state, new UILogger());
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "JavaBar running", 40, Gdx.graphics.getHeight() - 40);
        font.draw(batch, "Week: " + state.week + " | Cash: " + String.format("%.2f", state.cash), 40, Gdx.graphics.getHeight() - 70);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
