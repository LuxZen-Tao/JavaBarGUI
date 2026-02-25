package com.luxzentao.javabar.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.luxzentao.javabar.core.sim.BusUILogger;
import com.luxzentao.javabar.core.sim.SimAdapter;
import com.luxzentao.javabar.core.sim.SimEventBus;
import com.luxzentao.javabar.core.ui.HudView;

public class BarGame extends ApplicationAdapter {
    private Stage stage;
    private Skin skin;

    private Simulation simulation;
    private GameState state;

    private SimEventBus simEventBus;
    private SimAdapter simAdapter;
    private HudView hudView;

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());

        skin = loadSkin();
        state = GameFactory.newGame();

        simEventBus = new SimEventBus();
        simulation = new Simulation(state, new BusUILogger(simEventBus));
        simAdapter = new SimAdapter(state, simEventBus);
        hudView = new HudView(stage, skin, simulation, state, simEventBus);

        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(stage);
        Gdx.input.setInputProcessor(mux);

        simAdapter.sync();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        simAdapter.sync();
        hudView.tick();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        hudView.resize(width, height);
    }

    @Override
    public void dispose() {
        if (hudView != null) hudView.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }

    private Skin loadSkin() {
        FileHandle uiskinJson = Gdx.files.internal("uiskin.json");
        if (uiskinJson.exists()) {
            return new Skin(uiskinJson);
        }

        Gdx.app.error("HUD", "uiskin.json not found in assets. Falling back to generated skin.");
        Skin fallbackSkin = new Skin();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture skinTexture = new Texture(pixmap);
        pixmap.dispose();

        fallbackSkin.add("white", skinTexture);

        BitmapFont font = new BitmapFont();
        fallbackSkin.add("default-font", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        fallbackSkin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = fallbackSkin.newDrawable("white", new Color(0.2f, 0.24f, 0.32f, 1f));
        buttonStyle.down = fallbackSkin.newDrawable("white", new Color(0.15f, 0.18f, 0.26f, 1f));
        buttonStyle.over = fallbackSkin.newDrawable("white", new Color(0.25f, 0.30f, 0.40f, 1f));
        buttonStyle.font = font;
        fallbackSkin.add("default", buttonStyle);


        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.font = font;
        checkBoxStyle.fontColor = Color.WHITE;
        checkBoxStyle.checkboxOff = fallbackSkin.newDrawable("white", new Color(0.20f, 0.24f, 0.32f, 1f));
        checkBoxStyle.checkboxOn = fallbackSkin.newDrawable("white", new Color(0.32f, 0.60f, 0.36f, 1f));
        checkBoxStyle.checkboxOver = fallbackSkin.newDrawable("white", new Color(0.26f, 0.30f, 0.40f, 1f));
        fallbackSkin.add("default", checkBoxStyle);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = fallbackSkin.newDrawable("white", new Color(0.22f, 0.24f, 0.30f, 1f));
        sliderStyle.knob = fallbackSkin.newDrawable("white", new Color(0.75f, 0.78f, 0.86f, 1f));
        sliderStyle.knobOver = fallbackSkin.newDrawable("white", new Color(0.92f, 0.94f, 0.99f, 1f));
        sliderStyle.knobDown = fallbackSkin.newDrawable("white", new Color(0.58f, 0.62f, 0.72f, 1f));
        fallbackSkin.add("default-horizontal", sliderStyle);
        fallbackSkin.add("default-vertical", sliderStyle);

        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = fallbackSkin.newDrawable("white", new Color(0.09f, 0.10f, 0.14f, 0.95f));
        fallbackSkin.add("default", scrollPaneStyle);

        // Window style — required for any Scene2D Window subclass (modals, panels)
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle =
                new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont      = font;
        windowStyle.titleFontColor = Color.WHITE;
        windowStyle.background     = fallbackSkin.newDrawable("white", new Color(0.12f, 0.13f, 0.18f, 0.97f));
        fallbackSkin.add("default", windowStyle);

        // TextArea style (used by MissionWindow read-only stats)
        com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle tfStyle =
                new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle();
        tfStyle.font      = font;
        tfStyle.fontColor = Color.WHITE;
        tfStyle.background = fallbackSkin.newDrawable("white", new Color(0.07f, 0.08f, 0.11f, 0.9f));
        fallbackSkin.add("default", tfStyle);

        return fallbackSkin;
    }
}
