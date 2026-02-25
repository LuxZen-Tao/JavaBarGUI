package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.luxzentao.javabar.core.sim.SimEventBus;
import com.luxzentao.javabar.core.sim.SimListener;

import java.util.Locale;

public class HudView implements SimListener {
    private final Stage stage;
    private final Skin skin;
    private final SimEventBus eventBus;
    private final ToastManager toastManager;

    private final Label weekValueLabel;
    private final Label cashValueLabel;
    private final Table logDrawer;
    private final Table logLines;
    private final ScrollPane logScrollPane;

    private boolean drawerOpen;

    public HudView(Stage stage, Skin skin, SimEventBus eventBus) {
        this.stage = stage;
        this.skin = skin;
        this.eventBus = eventBus;
        this.toastManager = new ToastManager(stage, skin);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table topRow = new Table();
        Table bubbles = new Table();

        weekValueLabel = createBubble(bubbles, "Week", "--");
        cashValueLabel = createBubble(bubbles, "Cash", "$0.00");

        topRow.add(bubbles).left().expandX().padTop(12f).padLeft(12f);
        root.top().add(topRow).expandX().fillX().row();

        root.add().expand().fill().row();

        Table bottomBar = new Table();
        bottomBar.setBackground(skin.newDrawable("white", new Color(0.08f, 0.08f, 0.1f, 0.9f)));

        addActionButton(bottomBar, "Supplier", "Opened Supplier menu.", "Supplier screen coming soon...");
        addActionButton(bottomBar, "Bank", "Opened Bank menu.", "Bank screen coming soon...");
        addActionButton(bottomBar, "Mission", "Opened Mission menu.", "Mission screen coming soon...");

        TextButton logButton = new TextButton("Log", skin);
        logButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { toggleDrawer(); }
        });
        bottomBar.add(logButton).pad(8f).width(120f).height(42f);

        root.bottom().add(bottomBar).fillX();

        logLines = new Table();
        logLines.top().left();

        logScrollPane = new ScrollPane(logLines, skin);
        logScrollPane.setFadeScrollBars(false);
        logScrollPane.setScrollingDisabled(true, false);

        Label title = new Label("Activity Log", skin);
        title.setAlignment(Align.left);

        logDrawer = new Table();
        logDrawer.setBackground(skin.newDrawable("white", new Color(0.03f, 0.03f, 0.06f, 0.95f)));
        logDrawer.top().left().pad(10f);
        logDrawer.setSize(320f, Math.max(220f, stage.getHeight()));
        logDrawer.add(title).left().padBottom(10f).row();
        logDrawer.add(logScrollPane).expand().fill();

        stage.addActor(logDrawer);
        hideDrawerImmediate();

        eventBus.addListener(this);
    }

    private Label createBubble(Table bubbles, String title, String value) {
        Table bubble = new Table();
        bubble.setBackground(skin.newDrawable("white", new Color(0.16f, 0.18f, 0.24f, 0.94f)));
        bubble.pad(8f);

        Label titleLabel = new Label(title, skin);
        Label valueLabel = new Label(value, skin);

        bubble.add(titleLabel).left().row();
        bubble.add(valueLabel).left();

        bubbles.add(bubble).padRight(10f).height(64f).minWidth(130f);
        return valueLabel;
    }

    private void addActionButton(Table table, String text, String logMessage, String toastMessage) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                eventBus.fireLog(logMessage);
                toastManager.show(toastMessage);
            }
        });
        table.add(button).pad(8f).width(120f).height(42f);
    }

    private void toggleDrawer() {
        float targetX = drawerOpen ? stage.getWidth() : stage.getWidth() - logDrawer.getWidth();
        drawerOpen = !drawerOpen;
        logDrawer.clearActions();
        logDrawer.addAction(Actions.moveTo(targetX, 0, 0.22f));
    }

    private void hideDrawerImmediate() {
        drawerOpen = false;
        logDrawer.setPosition(stage.getWidth(), 0f);
    }

    public void resize(int width, int height) {
        logDrawer.setHeight(Math.max(220f, height));
        float targetX = drawerOpen ? (width - logDrawer.getWidth()) : width;
        logDrawer.setPosition(targetX, 0f);
    }

    @Override
    public void onWeekChanged(int week) {
        weekValueLabel.setText("W" + Math.max(1, week));
    }

    @Override
    public void onCashChanged(double cash) {
        cashValueLabel.setText(String.format(Locale.US, "$%,.2f", cash));
    }

    @Override
    public void onLog(String message) {
        Label line = new Label(message, skin);
        line.setWrap(true);
        line.setAlignment(Align.left);

        logLines.add(line).left().growX().padBottom(6f).row();
        toastManager.show(message);

        Gdx.app.postRunnable(() -> {
            if (logLines.getChildren().size > 200) {
                logLines.getChildren().removeIndex(0);
            }
            logScrollPane.layout();
            logScrollPane.setScrollPercentY(1f);
        });
    }

    public void dispose() {
        eventBus.removeListener(this);
    }
}
