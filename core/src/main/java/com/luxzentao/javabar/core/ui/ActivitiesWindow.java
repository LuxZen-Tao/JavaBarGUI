package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.MilestoneSystem;
import com.luxzentao.javabar.core.PubActivity;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for scheduling pub activities.
 */
public class ActivitiesWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Table activityRows;

    public ActivitiesWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Activities (Scheduled)", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        add(new Label("Schedule one activity between nights (starts in 1-3 days).", uiSkin))
                .left().growX().padBottom(8f).row();

        activityRows = new Table();
        ScrollPane scroll = new ScrollPane(activityRows, uiSkin);
        scroll.setFadeScrollBars(false);
        add(scroll).size(620f, 380f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(close).right().padTop(8f).row();

        pack();
    }

    public void refresh() {
        sim.recomputeActivityAvailability();
        activityRows.clear();
        activityRows.add(new Label("Activity", uiSkin)).left().minWidth(180f).padRight(8f);
        activityRows.add(new Label("Cost", uiSkin)).width(60f).center();
        activityRows.add(new Label("Status", uiSkin)).minWidth(120f).center();
        activityRows.add(new Label("Action", uiSkin)).width(100f).center();
        activityRows.row();

        for (PubActivity a : PubActivity.values()) {
            MilestoneSystem.ActivityAvailability avail = sim.getActivityAvailability(a);
            boolean running = state.activityTonight == a;
            boolean canSchedule = avail.unlocked() && !state.nightOpen && state.scheduledActivity == null;

            Label nameLbl = new Label(a.toString(), uiSkin);
            if (!avail.unlocked()) nameLbl.setColor(Color.GRAY);
            activityRows.add(nameLbl).left().minWidth(180f).padRight(8f);

            activityRows.add(new Label(
                    String.format(Locale.US, "£%.0f", a.getCost()), uiSkin)).width(60f).center();

            String statusText;
            Color statusColor;
            if (running) {
                statusText = "Running";
                statusColor = Color.GREEN;
            } else if (!avail.unlocked()) {
                statusText = "Locked: " + sim.activityRequirementText(a);
                statusColor = Color.RED;
            } else if (state.scheduledActivity != null) {
                statusText = "Busy";
                statusColor = Color.GRAY;
            } else if (state.nightOpen) {
                statusText = "Night open";
                statusColor = Color.GRAY;
            } else {
                statusText = "Available";
                statusColor = Color.WHITE;
            }
            Label statusLbl = new Label(statusText, uiSkin);
            statusLbl.setColor(statusColor);
            activityRows.add(statusLbl).minWidth(120f).center();

            TextButton scheduleBtn = new TextButton("Schedule", uiSkin);
            scheduleBtn.setDisabled(!canSchedule);
            scheduleBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (scheduleBtn.isDisabled()) return;
                    sim.startActivity(a);
                    eventBus.fireLog("Scheduled: " + a);
                    refresh();
                }
            });
            activityRows.add(scheduleBtn).width(100f).height(34f).pad(2f);
            activityRows.row();
        }
    }

    public void show(Stage stage) {
        if (getStage() == null) stage.addActor(this);
        refresh();
        setPosition(
                (stage.getWidth() - getWidth()) / 2f,
                (stage.getHeight() - getHeight()) / 2f);
        setVisible(true);
        toFront();
    }
}
