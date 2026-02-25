package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.bridge.HudSimBridge;
import com.luxzentao.javabar.core.sim.SimEventBus;
import com.luxzentao.javabar.core.sim.SimListener;
import com.luxzentao.javabar.core.ui.hud.ActivityLogPanel;
import com.luxzentao.javabar.core.ui.hud.MissionControlModal;
import com.luxzentao.javabar.core.ui.hud.RightDrawer;
import com.luxzentao.javabar.core.ui.hud.StatBarPanel;

import java.util.Locale;

public class HudView implements SimListener {
    private final Stage stage;
    private final SimEventBus eventBus;
    private final ToastManager toastManager;
    private final HudSimBridge bridge;

    private final StatBarPanel leftA, leftB, leftC, leftD;
    private final StatBarPanel midA, midB, midC, midD;
    private final StatBarPanel rightA, rightB, rightC, rightD;
    private final ActivityLogPanel activityLogPanel;

    private final RightDrawer reportsDrawer;
    private final RightDrawer inventoryDrawer;
    private final Label reportsBody;
    private final Label inventoryBody;

    private final MissionControlModal missionControlModal;
    public HudView(Stage stage, Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        this.stage = stage;
        this.eventBus = eventBus;
        this.toastManager = new ToastManager(stage, skin);
        this.bridge = new HudSimBridge(sim, state, eventBus);
        this.missionControlModal = new MissionControlModal(skin, bridge);

        Table root = new Table(skin);
        root.setFillParent(true);
        root.pad(8f);
        stage.addActor(root);

        // Top stat bar area (3 columns x multi-row)
        Table top = new Table(skin);
        leftA = new StatBarPanel(skin, new Color(0.20f, 0.34f, 0.58f, 0.95f));
        leftB = new StatBarPanel(skin, new Color(0.34f, 0.35f, 0.78f, 0.95f));
        leftC = new StatBarPanel(skin, new Color(0.28f, 0.40f, 0.60f, 0.95f));
        leftD = new StatBarPanel(skin, new Color(0.31f, 0.46f, 0.79f, 0.95f));

        midA = new StatBarPanel(skin, new Color(0.13f, 0.56f, 0.36f, 0.95f));
        midB = new StatBarPanel(skin, new Color(0.30f, 0.32f, 0.42f, 0.95f));
        midC = new StatBarPanel(skin, new Color(0.35f, 0.33f, 0.52f, 0.95f));
        midD = new StatBarPanel(skin, new Color(0.38f, 0.43f, 0.52f, 0.95f));

        rightA = new StatBarPanel(skin, new Color(0.65f, 0.20f, 0.25f, 0.95f));
        rightB = new StatBarPanel(skin, new Color(0.75f, 0.54f, 0.19f, 0.95f));
        rightC = new StatBarPanel(skin, new Color(0.33f, 0.50f, 0.50f, 0.95f));
        rightD = new StatBarPanel(skin, new Color(0.26f, 0.37f, 0.55f, 0.95f));

        addColumn(top, leftA, leftB, leftC, leftD);
        addColumn(top, midA, midB, midC, midD);
        addColumn(top, rightA, rightB, rightC, rightD);

        root.add(top).growX().top().row();

        // Main center with activity log
        activityLogPanel = new ActivityLogPanel(skin);
        root.add(activityLogPanel).grow().padTop(8f).row();

        // Bottom grouped action bar
        root.add(buildBottomBar(skin, state)).growX().padTop(8f);

        // Drawers
        reportsBody = new Label("--", skin);
        reportsBody.setWrap(true);
        reportsBody.setAlignment(Align.topLeft);

        Table reportsTable = new Table(skin);
        TextButton mcBtn = new TextButton("Mission Control", skin);
        mcBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { missionControlModal.show(stage); }
        });
        TextButton optionsBtn = new TextButton("Options", skin);
        optionsBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                bridge.commandOptions();
                eventBus.fireLog("Opened options placeholder.");
            }
        });
        reportsTable.add(new Label("Reports (Live)", skin)).left().expandX();
        reportsTable.add(mcBtn).padRight(6f);
        reportsTable.add(optionsBtn).row();
        reportsTable.add(new ScrollPane(reportsBody, skin)).grow().colspan(3).padTop(8f);
        reportsDrawer = new RightDrawer(stage, skin, "Reports", 440f, this::closeDrawers);
        reportsDrawer.setBody(reportsTable);
        stage.addActor(reportsDrawer);

        inventoryBody = new Label("--", skin);
        inventoryBody.setWrap(true);
        inventoryBody.setAlignment(Align.topLeft);
        inventoryDrawer = new RightDrawer(stage, skin, "Inventory", 440f, this::closeDrawers);
        inventoryDrawer.setBody(new ScrollPane(inventoryBody, skin));
        stage.addActor(inventoryDrawer);

        eventBus.addListener(this);
        refreshStats();
    }

    private void addColumn(Table top, StatBarPanel a, StatBarPanel b, StatBarPanel c, StatBarPanel d) {
        Table col = new Table();
        col.add(a).growX().height(58f).row();
        col.add(b).growX().height(58f).padTop(2f).row();
        col.add(c).growX().height(58f).padTop(2f).row();
        col.add(d).growX().height(58f).padTop(2f);
        top.add(col).growX().uniformX().padRight(2f);
    }

    private Table buildBottomBar(Skin skin, GameState state) {
        Table bottom = new Table(skin);
        bottom.setBackground(skin.newDrawable("white", new Color(0.13f, 0.14f, 0.18f, 0.97f)));
        bottom.pad(6f);

        Slider slider = new Slider(0.8f, 2.0f, 0.01f, false, skin);
        slider.setValue((float) state.priceMultiplier);

        bottom.add(group(skin, "Night", actionBtn(skin, "Open Pub", () -> bridge.commandOpenPub()),
                actionBtn(skin, "Next Round", () -> bridge.commandNextRound()),
                actionBtn(skin, "Close Night", () -> bridge.commandCloseNight()),
                toggleBtn(skin, "Happy Hour", state.happyHour, bridge::commandSetHappyHour),
                actionBtn(skin, "Reports", this::toggleReportsDrawer))).growX().padRight(4f);

        bottom.add(group(skin, "Economy", new Label("Price", skin), slider,
                actionBtn(skin, "Supplier", () -> bridge.commandSupplier()),
                actionBtn(skin, "Food Supplier", () -> bridge.commandFoodSupplier()),
                actionBtn(skin, "Pay Debt", () -> bridge.commandPayDebt()),
                actionBtn(skin, "Loan Shark", () -> bridge.commandLoanShark()))).growX().padRight(4f);

        bottom.add(group(skin, "Management",
                actionBtn(skin, "Staff", () -> bridge.commandStaff()),
                actionBtn(skin, "Inn", () -> bridge.commandInn()),
                actionBtn(skin, "Upgrades", () -> bridge.commandUpgrades()),
                actionBtn(skin, "Inventory", this::toggleInventoryDrawer))).growX().padRight(4f);

        bottom.add(group(skin, "Risk", actionBtn(skin, "Security", () -> bridge.commandSecurity()))).growX().padRight(4f);
        bottom.add(group(skin, "Activities", actionBtn(skin, "Activities", () -> bridge.commandActivities()), actionBtn(skin, "Actions", () -> bridge.commandActions()))).growX().padRight(4f);
        bottom.add(group(skin, "Automation", toggleBtn(skin, "Auto", false, bridge::commandAuto))).growX();

        slider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                bridge.commandSetPriceMultiplier(slider.getValue());
                eventBus.fireLog(String.format(Locale.US, "Price multiplier set to x%.2f", slider.getValue()));
            }
        });
        return bottom;
    }

    private Table group(Skin skin, String title, Actor... actors) {
        Table g = new Table(skin);
        g.setBackground(skin.newDrawable("white", new Color(0.10f, 0.12f, 0.16f, 0.98f)));
        g.pad(4f);
        g.add(new Label(title, skin)).left().row();
        Table body = new Table(skin);
        for (Actor actor : actors) body.add(actor).pad(2f).height(34f);
        g.add(body).left();
        return g;
    }

    private TextButton actionBtn(Skin skin, String text, Runnable action) {
        TextButton btn = new TextButton(text, skin);
        btn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                action.run();
                toastManager.show(text);
            }
        });
        return btn;
    }

    private CheckBox toggleBtn(Skin skin, String text, boolean checked, java.util.function.Consumer<Boolean> action) {
        CheckBox cb = new CheckBox(text, skin);
        cb.setChecked(checked);
        cb.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                action.accept(cb.isChecked());
                eventBus.fireLog(text + ": " + (cb.isChecked() ? "ON" : "OFF"));
            }
        });
        return cb;
    }

    private void refreshStats() {
        var m = bridge.metrics();
        leftA.setText(m.hudPubName + "\n" + bridge.weekLine());
        leftB.setText(m.hudRep + "\nIdentity: " + bridge.metrics().reputationIdentity.split("\n")[0] + "\nRumor: " + bridge.metrics().rumors.split("\n")[0]);
        leftC.setText(bridge.weekLine() + "\n" + bridge.calendarLine());
        leftD.setText(bridge.serviceLine());

        midA.setText(m.hudCash);
        midB.setText("Weekly Costs\n" + bridge.costsSummaryLineA() + "\n" + bridge.costsSummaryLineB());
        midC.setText(bridge.reportLine());
        midD.setText(stateFlavor(m));

        rightA.setText(m.hudDebt);
        rightB.setText(bridge.policyLine());
        rightC.setText(bridge.staffLine() + "\n" + bridge.countsLine());
        rightD.setText(bridge.forecastLine() + "\n" + bridge.topSellerLine());

        reportsBody.setText(bridge.reportsLiveText());
        inventoryBody.setText(bridge.inventoryText());
        missionControlModal.refresh();
    }

    private String stateFlavor(com.luxzentao.javabar.core.MetricsSnapshot m) {
        return m.overviewLines.isEmpty() ? "NPC: Keeping the taps warm." : m.overviewLines.get(Math.min(3, m.overviewLines.size() - 1));
    }

    private void closeDrawers() {
        reportsDrawer.close();
        inventoryDrawer.close();
    }

    private void toggleReportsDrawer() {
        if (reportsDrawer.isOpen()) reportsDrawer.close();
        else {
            inventoryDrawer.close();
            reportsDrawer.open();
        }
    }

    private void toggleInventoryDrawer() {
        if (inventoryDrawer.isOpen()) inventoryDrawer.close();
        else {
            reportsDrawer.close();
            inventoryDrawer.open();
        }
    }

    public void resize(int width, int height) {
        reportsDrawer.onResize();
        inventoryDrawer.onResize();
    }

    public void tick() { refreshStats(); }

    public void dispose() { eventBus.removeListener(this); }

    @Override public void onWeekChanged(int week) { refreshStats(); }
    @Override public void onCashChanged(double cash) { refreshStats(); }
    @Override public void onRepChanged(int rep) { refreshStats(); }
    @Override public void onStaffChanged(int total) { refreshStats(); }
    @Override public void onPuntersChanged(int count) { refreshStats(); }
    @Override public void onNightStatusChanged(boolean nightOpen) { refreshStats(); }

    @Override
    public void onLog(String message) {
        activityLogPanel.append(message);
        toastManager.show(message);
    }
}
