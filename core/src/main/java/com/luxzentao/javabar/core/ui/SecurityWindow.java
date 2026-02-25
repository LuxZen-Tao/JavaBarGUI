package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.SecurityPolicy;
import com.luxzentao.javabar.core.SecuritySystem;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for Security management.
 * Shows current security breakdown, policy buttons, and bouncer/marshall hiring.
 */
public class SecurityWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Label breakdownLabel;
    private final TextButton upgradeBtn;
    private final TextButton bouncerBtn;
    private final TextButton marshallBtn;

    public SecurityWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Security", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        add(new Label("Manage base security, bouncers, and policy.", uiSkin)).left().growX().padBottom(8f).row();

        breakdownLabel = new Label("", uiSkin);
        breakdownLabel.setWrap(true);
        add(breakdownLabel).left().growX().padBottom(8f).row();

        // Policy buttons
        add(new Label("Door Policy:", uiSkin)).left().padBottom(4f).row();
        Table policyRow = new Table();
        for (SecurityPolicy policy : SecurityPolicy.values()) {
            TextButton btn = new TextButton(policy.getLabel(), uiSkin);
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    sim.setSecurityPolicy(policy);
                    eventBus.fireLog("Security policy: " + policy.getLabel());
                    refresh();
                }
            });
            policyRow.add(btn).pad(3f).height(36f);
        }
        add(policyRow).left().padBottom(8f).row();

        // Upgrade / bouncer / marshall
        Table actionRow = new Table();
        upgradeBtn = new TextButton("", uiSkin);
        upgradeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (upgradeBtn.isDisabled()) return;
                sim.upgradeSecurity();
                eventBus.fireLog("Security level upgraded.");
                refresh();
            }
        });
        bouncerBtn = new TextButton("", uiSkin);
        bouncerBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (bouncerBtn.isDisabled()) return;
                sim.hireBouncerTonight();
                eventBus.fireLog("Bouncer hired for tonight.");
                refresh();
            }
        });
        marshallBtn = new TextButton("Hire Marshall", uiSkin);
        marshallBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                sim.hireMarshall();
                eventBus.fireLog("Marshall hired.");
                refresh();
            }
        });
        actionRow.add(upgradeBtn).pad(3f).height(38f).minWidth(240f);
        actionRow.add(bouncerBtn).pad(3f).height(38f).minWidth(180f);
        actionRow.add(marshallBtn).pad(3f).height(38f).minWidth(140f);
        add(actionRow).left().padBottom(8f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(close).right().padTop(4f).row();

        pack();
        setSize(Math.max(getPrefWidth(), 540f), Math.max(getPrefHeight(), 340f));
    }

    public void refresh() {
        SecuritySystem.SecurityBreakdown bd = sim.securityBreakdown();
        String policy = state.securityPolicy == null ? "None" : state.securityPolicy.getLabel();
        breakdownLabel.setText(String.format(Locale.US,
                "Policy: %s  |  Total: %d  (base %d + upgrades %d + policy %d + bouncers %d + manager %d + staff %d)",
                policy, bd.total(), bd.base(), bd.upgrades(), bd.policy(),
                bd.bouncers(), bd.manager(), bd.staff()));

        upgradeBtn.setText(String.format(Locale.US,
                "Base Security +1 (level %d, cost £%.0f)", state.baseSecurityLevel, sim.peekSecurityUpgradeCost()));
        upgradeBtn.setDisabled(state.nightOpen);

        bouncerBtn.setText("Hire Bouncer Tonight (+2 security)");
        bouncerBtn.setDisabled(!state.nightOpen);
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
