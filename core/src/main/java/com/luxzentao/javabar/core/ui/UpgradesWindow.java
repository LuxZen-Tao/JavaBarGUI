package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.MilestoneSystem;
import com.luxzentao.javabar.core.PubUpgrade;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for the Upgrades screen.
 * Lists all PubUpgrade values; owned/installing are greyed out.
 * Purchase button calls sim.buyUpgrade and refreshes HUD.
 */
public class UpgradesWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Table upgradeRows;
    private final Skin uiSkin;
    private final Label hintLabel;

    public UpgradesWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Upgrades", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        hintLabel = new Label("", uiSkin);
        hintLabel.setColor(Color.GOLD);
        hintLabel.setWrap(true);
        add(hintLabel).growX().padBottom(8f).row();

        upgradeRows = new Table();
        ScrollPane scroll = new ScrollPane(upgradeRows, uiSkin);
        scroll.setFadeScrollBars(false);
        add(scroll).size(640f, 380f).row();

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
        String hint = sim.upgradeBottleneckHint();
        hintLabel.setText(hint == null ? "" : hint);

        upgradeRows.clear();
        upgradeRows.add(new Label("Upgrade", uiSkin)).left().minWidth(200f).padRight(8f);
        upgradeRows.add(new Label("Cost", uiSkin)).width(70f).center();
        upgradeRows.add(new Label("Status", uiSkin)).minWidth(130f).center();
        upgradeRows.add(new Label("Action", uiSkin)).width(110f).center();
        upgradeRows.row();

        for (PubUpgrade up : PubUpgrade.values()) {
            boolean owned = state.ownedUpgrades.contains(up);
            boolean installing = isInstalling(up);

            MilestoneSystem.UpgradeAvailability avail = sim.getUpgradeAvailability(up);

            Label nameLbl = new Label(up.getLabel(), uiSkin);
            if (owned || installing) nameLbl.setColor(Color.GRAY);
            upgradeRows.add(nameLbl).left().minWidth(200f).padRight(8f);

            upgradeRows.add(new Label(
                    String.format(Locale.US, "£%.0f", up.getCost()), uiSkin))
                    .width(70f).center();

            String statusText;
            Color statusColor;
            if (owned) {
                statusText = "Owned";
                statusColor = Color.GREEN;
            } else if (installing) {
                statusText = "Installing...";
                statusColor = Color.YELLOW;
            } else if (!avail.unlocked()) {
                statusText = "Locked";
                statusColor = Color.RED;
            } else if (state.cash < up.getCost()) {
                statusText = "Need £" + String.format(Locale.US, "%.0f", up.getCost() - state.cash);
                statusColor = Color.ORANGE;
            } else {
                statusText = "Available";
                statusColor = Color.WHITE;
            }
            Label statusLbl = new Label(statusText, uiSkin);
            statusLbl.setColor(statusColor);
            upgradeRows.add(statusLbl).minWidth(130f).center();

            TextButton buyBtn = new TextButton("Buy", uiSkin);
            boolean canBuy = !owned && !installing && avail.unlocked() && !state.nightOpen;
            buyBtn.setDisabled(!canBuy);
            buyBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (buyBtn.isDisabled()) return;
                    sim.buyUpgrade(up);
                    refresh();
                }
            });
            upgradeRows.add(buyBtn).width(110f).height(38f).pad(2f);
            upgradeRows.row();
        }
    }

    private boolean isInstalling(PubUpgrade up) {
        for (var install : state.pendingUpgradeInstalls) {
            if (install.upgrade() == up) return true;
        }
        return false;
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
