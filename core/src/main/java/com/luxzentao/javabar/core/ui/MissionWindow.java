package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;

import java.util.Locale;

/**
 * Floating Scene2D Window: Mission Control / Stats Dashboard.
 * Shows a read-only snapshot of all key game metrics.
 * Equivalent to the Swing "Mission Control" multi-tab dialog.
 */
public class MissionWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final TextArea statsArea;
    private final Skin uiSkin;

    public MissionWindow(Skin skin, Simulation sim, GameState state) {
        super("Mission Control", skin);
        this.sim = sim;
        this.state = state;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        statsArea = new TextArea("", uiSkin);
        statsArea.setDisabled(true);  // read-only
        ScrollPane scroll = new ScrollPane(statsArea, uiSkin);
        scroll.setFadeScrollBars(false);
        add(scroll).size(560f, 440f).row();

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
        StringBuilder sb = new StringBuilder();
        sb.append("=== OVERVIEW ===\n");
        sb.append(String.format(Locale.US, "  Pub:        %s\n", state.pubName));
        sb.append(String.format(Locale.US, "  Week:       %d  (Day %d)\n", state.weekCount, state.dayIndex + 1));
        sb.append(String.format(Locale.US, "  Night:      %s  (Round %d)\n",
                state.nightOpen ? "OPEN" : "closed", state.roundInNight));

        sb.append("\n=== ECONOMY ===\n");
        sb.append(String.format(Locale.US, "  Cash:       £%.2f\n", state.cash));
        sb.append(String.format(Locale.US, "  Reputation: %d\n", state.reputation));
        sb.append(String.format(Locale.US, "  Credit Scr: %d\n", state.creditScore));
        sb.append(String.format(Locale.US, "  Total Debt: £%.2f / £%.2f\n",
                state.totalCreditBalance(), state.totalCreditLimit()));

        sb.append("\n=== STAFF ===\n");
        sb.append(String.format(Locale.US, "  FOH staff:  %d\n", state.fohStaff.size()));
        sb.append(String.format(Locale.US, "  BOH staff:  %d\n", state.bohStaff.size()));
        sb.append(String.format(Locale.US, "  Managers:   %d\n", state.generalManagers.size()));
        sb.append(String.format(Locale.US, "  Serve cap:  %d\n", sim.peekServeCapacity()));

        sb.append("\n=== INVENTORY ===\n");
        sb.append(String.format(Locale.US, "  Wine rack:  %d / %d\n",
                state.rack.count(), state.rack.getCapacity()));
        if (state.kitchenUnlocked) {
            sb.append(String.format(Locale.US, "  Kitchen:    %d / %d\n",
                    state.foodRack.count(), state.foodRack.getCapacity()));
        } else {
            sb.append("  Kitchen:    locked\n");
        }

        sb.append("\n=== NIGHT STATS ===\n");
        sb.append(String.format(Locale.US, "  Punters:    %d / %d\n",
                state.nightPunters.size(), state.maxBarOccupancy));
        sb.append(String.format(Locale.US, "  Sales:      %d\n", state.nightSales));
        sb.append(String.format(Locale.US, "  Revenue:    £%.2f\n", state.nightRevenue));

        sb.append("\n=== UPGRADES ===\n");
        sb.append(String.format(Locale.US, "  Owned: %d  |  Installing: %d\n",
                state.ownedUpgrades.size(), state.pendingUpgradeInstalls.size()));
        for (var u : state.ownedUpgrades) {
            sb.append("    ✓ ").append(u.getLabel()).append("\n");
        }
        for (var u : state.pendingUpgradeInstalls) {
            sb.append("    … ").append(u.upgrade().getLabel())
              .append(" (").append(u.nightsRemaining()).append(" night(s))\n");
        }

        sb.append("\n=== SUPPLIER ===\n");
        String deal = (state.supplierDeal == null) ? "None" : state.supplierDeal.getLabel();
        sb.append("  Deal:       ").append(deal).append("\n");
        sb.append(String.format(Locale.US,
                "  Wine credit: £%.2f used / £%.2f cap\n",
                state.supplierWineCredit.getBalance(), state.supplierCreditCap()));

        statsArea.setText(sb.toString());
        statsArea.setCursorPosition(0);
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
