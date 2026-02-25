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
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;
import com.luxzentao.javabar.core.sim.SimListener;

import java.util.Locale;

/**
 * Primary HUD view for the LibGDX game window.
 *
 * Layout (top → bottom):
 *   [stat bubbles: Week | Cash | Rep | Staff | Punters | Status]
 *   [expandable centre – empty for now, game world will go here]
 *   [night row:    Open Bar | Next Round | Close Night]
 *   [bottom bar:   Supplier | Bank | Mission | Staff | Upgrades | Log]
 *   [right-side drawer (Activity Log) slides in/out]
 *
 * Lifecycle:
 *   create()  – new HudView(stage, skin, sim, state, eventBus)
 *   render()  – sim adapter calls sync(), HUD updates via SimListener callbacks
 *   resize()  – call hudView.resize(w, h)
 *   dispose() – hudView.dispose()
 */
public class HudView implements SimListener {
    private final Stage stage;
    private final Skin skin;
    private final SimEventBus eventBus;
    private final ToastManager toastManager;

    // Stat bubble labels
    private final Label weekValueLabel;
    private final Label cashValueLabel;
    private final Label repValueLabel;
    private final Label staffValueLabel;
    private final Label puntersValueLabel;
    private final Label statusLabel;

    // Night cycle buttons (enabled/disabled based on nightOpen)
    private final TextButton openBarBtn;
    private final TextButton nextRoundBtn;
    private final TextButton closeNightBtn;

    // Log drawer
    private final Table logDrawer;
    private final Table logLines;
    private final ScrollPane logScrollPane;
    private boolean drawerOpen;

    // Windows (lazily created)
    private SupplierWindow supplierWindow;
    private BankWindow bankWindow;
    private MissionWindow missionWindow;
    private StaffWindow staffWindow;
    private UpgradesWindow upgradesWindow;

    // Simulation references
    private final Simulation sim;
    private final GameState state;

    public HudView(Stage stage, Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        this.stage = stage;
        this.skin = skin;
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.toastManager = new ToastManager(stage, skin);

        // ── Root table fills the whole viewport ──────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // ── Top stat bubbles ─────────────────────────────────────────────────
        Table bubbles = new Table();
        weekValueLabel    = createBubble(bubbles, "Week",    "W1");
        cashValueLabel    = createBubble(bubbles, "Cash",    "£0.00");
        repValueLabel     = createBubble(bubbles, "Rep",     "0");
        staffValueLabel   = createBubble(bubbles, "Staff",   "0");
        puntersValueLabel = createBubble(bubbles, "Punters", "0");
        statusLabel       = createBubble(bubbles, "Bar",     "CLOSED");
        statusLabel.setColor(Color.RED);

        Table topRow = new Table();
        topRow.add(bubbles).left().expandX().padTop(12f).padLeft(12f);
        root.top().add(topRow).expandX().fillX().row();

        // ── Expandable centre (future world view placeholder) ─────────────────
        root.add().expand().fill().row();

        // ── Night cycle control row ───────────────────────────────────────────
        Table nightRow = new Table();
        nightRow.setBackground(skin.newDrawable("white", new Color(0.05f, 0.12f, 0.08f, 0.88f)));

        openBarBtn    = new TextButton("Open Bar",    skin);
        nextRoundBtn  = new TextButton("Next Round",  skin);
        closeNightBtn = new TextButton("Close Night", skin);

        openBarBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                sim.openNight();
                eventBus.fireLog("Bar opened.");
                refreshWindowsIfOpen();
            }
        });
        nextRoundBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!state.nightOpen) return;
                sim.playRound();
                eventBus.fireLog("Round played.");
                refreshWindowsIfOpen();
            }
        });
        closeNightBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!state.nightOpen) return;
                sim.closeNight("Closed by landlord.");
                eventBus.fireLog("Night closed.");
                refreshWindowsIfOpen();
            }
        });

        nightRow.add(openBarBtn).pad(6f).width(140f).height(40f);
        nightRow.add(nextRoundBtn).pad(6f).width(140f).height(40f);
        nightRow.add(closeNightBtn).pad(6f).width(140f).height(40f);
        root.add(nightRow).fillX().row();

        // ── Bottom navigation bar ─────────────────────────────────────────────
        Table bottomBar = new Table();
        bottomBar.setBackground(skin.newDrawable("white", new Color(0.08f, 0.08f, 0.1f, 0.9f)));

        addNavButton(bottomBar, "Supplier", () -> getSupplierWindow().show(stage));
        addNavButton(bottomBar, "Bank",     () -> getBankWindow().show(stage));
        addNavButton(bottomBar, "Mission",  () -> getMissionWindow().show(stage));
        addNavButton(bottomBar, "Staff",    () -> getStaffWindow().show(stage));
        addNavButton(bottomBar, "Upgrades", () -> getUpgradesWindow().show(stage));

        TextButton logButton = new TextButton("Log", skin);
        logButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { toggleDrawer(); }
        });
        bottomBar.add(logButton).pad(8f).width(120f).height(42f);

        root.bottom().add(bottomBar).fillX();

        // ── Activity Log drawer (slides from right) ───────────────────────────
        logLines = new Table();
        logLines.top().left();

        logScrollPane = new ScrollPane(logLines, skin);
        logScrollPane.setFadeScrollBars(false);
        logScrollPane.setScrollingDisabled(true, false);

        Label logTitle = new Label("Activity Log", skin);
        logTitle.setAlignment(Align.left);

        logDrawer = new Table();
        logDrawer.setBackground(skin.newDrawable("white", new Color(0.03f, 0.03f, 0.06f, 0.95f)));
        logDrawer.top().left().pad(10f);
        logDrawer.setSize(320f, Math.max(220f, stage.getHeight()));
        logDrawer.add(logTitle).left().padBottom(10f).row();
        logDrawer.add(logScrollPane).expand().fill();

        stage.addActor(logDrawer);
        hideDrawerImmediate();

        // Initial button states
        updateNightButtons(false);

        eventBus.addListener(this);
    }

    // ── Lazy window accessors ─────────────────────────────────────────────────

    private SupplierWindow getSupplierWindow() {
        if (supplierWindow == null)
            supplierWindow = new SupplierWindow(skin, sim, state, eventBus);
        return supplierWindow;
    }

    private BankWindow getBankWindow() {
        if (bankWindow == null)
            bankWindow = new BankWindow(skin, sim, state, eventBus);
        return bankWindow;
    }

    private MissionWindow getMissionWindow() {
        if (missionWindow == null)
            missionWindow = new MissionWindow(skin, sim, state);
        return missionWindow;
    }

    private StaffWindow getStaffWindow() {
        if (staffWindow == null)
            staffWindow = new StaffWindow(skin, sim, state, eventBus);
        return staffWindow;
    }

    private UpgradesWindow getUpgradesWindow() {
        if (upgradesWindow == null)
            upgradesWindow = new UpgradesWindow(skin, sim, state, eventBus);
        return upgradesWindow;
    }

    /** Refresh any window that is currently visible (so it stays live). */
    private void refreshWindowsIfOpen() {
        if (supplierWindow != null && supplierWindow.isVisible()) supplierWindow.refresh();
        if (bankWindow     != null && bankWindow.isVisible())     bankWindow.refresh();
        if (missionWindow  != null && missionWindow.isVisible())  missionWindow.refresh();
        if (staffWindow    != null && staffWindow.isVisible())    staffWindow.refresh();
        if (upgradesWindow != null && upgradesWindow.isVisible()) upgradesWindow.refresh();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Label createBubble(Table bubbles, String title, String value) {
        Table bubble = new Table();
        bubble.setBackground(skin.newDrawable("white", new Color(0.16f, 0.18f, 0.24f, 0.94f)));
        bubble.pad(8f);

        Label titleLabel = new Label(title, skin);
        Label valueLabel = new Label(value, skin);

        bubble.add(titleLabel).left().row();
        bubble.add(valueLabel).left();

        bubbles.add(bubble).padRight(10f).height(64f).minWidth(100f);
        return valueLabel;
    }

    private void addNavButton(Table table, String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                eventBus.fireLog("Opened " + text + " menu.");
                action.run();
            }
        });
        table.add(button).pad(8f).width(120f).height(42f);
    }

    private void updateNightButtons(boolean nightOpen) {
        openBarBtn.setDisabled(nightOpen);
        nextRoundBtn.setDisabled(!nightOpen);
        closeNightBtn.setDisabled(!nightOpen);
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

    // ── Public lifecycle hooks ────────────────────────────────────────────────

    public void resize(int width, int height) {
        logDrawer.setHeight(Math.max(220f, height));
        float targetX = drawerOpen ? (width - logDrawer.getWidth()) : width;
        logDrawer.setPosition(targetX, 0f);
    }

    public void dispose() {
        eventBus.removeListener(this);
    }

    // ── SimListener ───────────────────────────────────────────────────────────

    @Override
    public void onWeekChanged(int week) {
        weekValueLabel.setText("W" + Math.max(1, week));
    }

    @Override
    public void onCashChanged(double cash) {
        cashValueLabel.setText(String.format(Locale.US, "£%,.2f", cash));
    }

    @Override
    public void onRepChanged(int rep) {
        repValueLabel.setText("" + rep);
    }

    @Override
    public void onStaffChanged(int total) {
        staffValueLabel.setText("" + total);
    }

    @Override
    public void onPuntersChanged(int count) {
        puntersValueLabel.setText("" + count);
    }

    @Override
    public void onNightStatusChanged(boolean nightOpen) {
        statusLabel.setText(nightOpen ? "OPEN" : "CLOSED");
        statusLabel.setColor(nightOpen ? Color.GREEN : Color.RED);
        updateNightButtons(nightOpen);
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

        // Keep open windows updated whenever the sim produces a log event
        refreshWindowsIfOpen();
    }
}
