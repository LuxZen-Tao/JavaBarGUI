package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.Staff;
import com.luxzentao.javabar.core.sim.SimEventBus;

/**
 * Floating Scene2D Window for the Staff screen.
 * Shows FOH/BOH/Manager roster and hire buttons for basic roles.
 */
public class StaffWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Table rosterTable;
    private final Label summaryLabel;
    private final Skin uiSkin;

    // Roles the player can hire via the HUD
    private static final Staff.Type[] HIREABLE = {
            Staff.Type.TRAINEE,
            Staff.Type.EXPERIENCED,
            Staff.Type.SPEED,
            Staff.Type.CHARISMA,
            Staff.Type.SECURITY,
            Staff.Type.MANAGER
    };

    public StaffWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Staff", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        summaryLabel = new Label("", uiSkin);
        summaryLabel.setWrap(true);
        add(summaryLabel).growX().padBottom(8f).row();

        // Hire buttons row
        Table hireRow = new Table();
        add(new Label("Hire:", uiSkin)).left().padBottom(4f).row();
        for (Staff.Type t : HIREABLE) {
            TextButton btn = new TextButton(prettyName(t), uiSkin);
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    sim.hireStaff(t);
                    eventBus.fireLog("Hired " + prettyName(t));
                    refresh();
                }
            });
            hireRow.add(btn).pad(4f).width(120f).height(38f);
        }
        add(hireRow).growX().padBottom(8f).row();

        add(new Label("Current Roster:", uiSkin)).left().padBottom(4f).row();
        rosterTable = new Table();
        ScrollPane scroll = new ScrollPane(rosterTable, uiSkin);
        scroll.setFadeScrollBars(false);
        add(scroll).size(580f, 260f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setVisible(false);
            }
        });
        add(close).right().padTop(8f).row();

        pack();
    }

    public void refresh() {
        int total = state.fohStaff.size() + state.bohStaff.size() + state.generalManagers.size();
        summaryLabel.setText(String.format("FOH: %d  |  BOH: %d  |  Managers: %d  |  Total: %d / cap",
                state.fohStaff.size(), state.bohStaff.size(),
                state.generalManagers.size(), total));

        rosterTable.clear();
        rosterTable.add(new Label("#", uiSkin)).width(30f);
        rosterTable.add(new Label("Name", uiSkin)).minWidth(140f).padRight(8f);
        rosterTable.add(new Label("Role", uiSkin)).minWidth(120f).padRight(8f);
        rosterTable.add(new Label("Action", uiSkin)).width(90f);
        rosterTable.row();

        boolean nightOpen = state.nightOpen;

        int idx = 0;
        for (var s : state.fohStaff) {
            final int i = idx++;
            addStaffRow(rosterTable, i + 1, s, nightOpen, () -> {
                sim.fireStaffAt(i);
                eventBus.fireLog("Fired " + s.getName());
                refresh();
            });
        }
        for (var s : state.bohStaff) {
            final int i = idx++;
            addStaffRow(rosterTable, i + 1, s, nightOpen, () -> {
                sim.fireBohStaffAt(state.bohStaff.indexOf(s));
                eventBus.fireLog("Fired " + s.getName());
                refresh();
            });
        }
        for (var s : state.generalManagers) {
            final int i = idx++;
            addStaffRow(rosterTable, i + 1, s, nightOpen, () -> {
                sim.fireManagerAt(state.generalManagers.indexOf(s));
                eventBus.fireLog("Fired " + s.getName());
                refresh();
            });
        }

        if (idx == 0) {
            rosterTable.add(new Label("No staff hired yet.", uiSkin)).colspan(4).padTop(8f);
        }
    }

    private void addStaffRow(Table t, int num, com.luxzentao.javabar.core.Staff s,
                             boolean nightOpen, Runnable onFire) {
        t.add(new Label(num + "", uiSkin)).width(30f);
        Label nameLbl = new Label(s.getName(), uiSkin);
        t.add(nameLbl).minWidth(140f).padRight(8f);
        t.add(new Label(prettyName(s.getType()), uiSkin)).minWidth(120f).padRight(8f);
        TextButton fireBtn = new TextButton("Fire", uiSkin);
        fireBtn.setDisabled(nightOpen);
        fireBtn.getLabel().setColor(Color.SALMON);
        fireBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!fireBtn.isDisabled()) onFire.run();
            }
        });
        t.add(fireBtn).width(90f).height(34f).pad(2f);
        t.row();
    }

    private static String prettyName(Staff.Type t) {
        return t.name().replace("_", " ");
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
