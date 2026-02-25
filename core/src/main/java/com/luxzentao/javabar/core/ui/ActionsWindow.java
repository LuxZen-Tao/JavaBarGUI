package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.LandlordActionDef;
import com.luxzentao.javabar.core.LandlordActionId;
import com.luxzentao.javabar.core.LandlordActionResolution;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for the Landlord Actions panel.
 * Shows available actions with availability status and trigger buttons.
 */
public class ActionsWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Label headerLabel;
    private final Table actionRows;

    public ActionsWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Landlord Actions", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        headerLabel = new Label("", uiSkin);
        headerLabel.setWrap(true);
        add(headerLabel).growX().padBottom(8f).row();

        actionRows = new Table();
        ScrollPane scroll = new ScrollPane(actionRows, uiSkin);
        scroll.setFadeScrollBars(false);
        add(scroll).size(680f, 380f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(close).right().padTop(8f).row();

        pack();
    }

    public void refresh() {
        boolean nightOpen = state.nightOpen;
        boolean canAct = sim.canUseLandlordActionThisRound();
        headerLabel.setText(nightOpen
                ? (canAct ? "Select an action for this round." : "Already used an action this round.")
                : "Open the pub to use landlord actions.");

        actionRows.clear();
        actionRows.add(new Label("Action", uiSkin)).left().minWidth(180f).padRight(6f);
        actionRows.add(new Label("Cost", uiSkin)).width(60f).center();
        actionRows.add(new Label("Chance", uiSkin)).width(70f).center();
        actionRows.add(new Label("Status", uiSkin)).minWidth(130f).center();
        actionRows.add(new Label("Use", uiSkin)).width(80f).center();
        actionRows.row();

        for (LandlordActionDef def : sim.getAvailableActionsForCurrentTier()) {
            Simulation.LandlordActionAvailability avail = sim.landlordActionAvailability(def);

            Label nameLbl = new Label(def.getName(), uiSkin);
            if (!avail.canUse()) nameLbl.setColor(Color.GRAY);
            actionRows.add(nameLbl).left().minWidth(180f).padRight(6f);

            actionRows.add(new Label(
                    String.format(Locale.US, "£%d", def.getBaseCost()), uiSkin)).width(60f).center();

            actionRows.add(new Label(
                    String.format(Locale.US, "%.0f%%", sim.computeActionChance(def) * 100), uiSkin)).width(70f).center();

            Label statusLbl = new Label(avail.canUse() ? "Ready" : avail.reason(), uiSkin);
            statusLbl.setColor(avail.canUse() ? Color.GREEN : Color.ORANGE);
            actionRows.add(statusLbl).minWidth(130f).center();

            TextButton useBtn = new TextButton("Use", uiSkin);
            useBtn.setDisabled(!avail.canUse());
            final LandlordActionId id = def.getId();
            useBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (useBtn.isDisabled()) return;
                    LandlordActionResolution result = sim.resolveLandlordAction(id);
                    if (result != null) {
                        eventBus.fireLog(result.blocked()
                                ? "Action blocked: " + result.message()
                                : "Action: " + def.getName() + " — " + result.message());
                    }
                    refresh();
                }
            });
            actionRows.add(useBtn).width(80f).height(34f).pad(2f);
            actionRows.row();
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
