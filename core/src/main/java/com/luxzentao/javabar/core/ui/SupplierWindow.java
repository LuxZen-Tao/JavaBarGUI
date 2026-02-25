package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.Wine;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for the Supplier screen.
 * Shows current deal, inventory status, and buy buttons (x1 / x5 / x10).
 *
 * Lifecycle:
 *   show()  -> add to stage and bring to front
 *   refresh() -> call after any sim action to update labels
 */
public class SupplierWindow extends Window {

    private static final int[] QUANTITIES = {1, 5, 10, 25};

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Label dealLabel;
    private final Label creditLabel;
    private final Table wineRows;

    public SupplierWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Supplier (Bulk Buy)", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setResizable(false);
        setModal(false);
        pad(10f);

        // Deal header
        dealLabel = new Label("", uiSkin);
        dealLabel.setWrap(true);
        add(dealLabel).growX().colspan(QUANTITIES.length + 1).padBottom(4f).row();

        creditLabel = new Label("", uiSkin);
        creditLabel.setColor(Color.SALMON);
        creditLabel.setWrap(true);
        add(creditLabel).growX().colspan(QUANTITIES.length + 1).padBottom(8f).row();

        // Column headers
        add(new Label("Wine", uiSkin)).left().minWidth(200f);
        for (int q : QUANTITIES) {
            add(new Label("x" + q, uiSkin)).width(72f).center();
        }
        row();

        // Wine rows container (rebuilt in refresh)
        wineRows = new Table();
        add(wineRows).growX().colspan(QUANTITIES.length + 1).row();

        // Close button
        TextButton closeBtn = new TextButton("Close", uiSkin);
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setVisible(false);
            }
        });
        add(closeBtn).right().padTop(8f).row();

        pack();
        setSize(Math.max(getPrefWidth(), 600f), Math.max(getPrefHeight(), 340f));
    }

    /** Re-populate labels and buttons from live GameState. */
    public void refresh() {
        String dealText = (state.supplierDeal == null)
                ? "No deal today."
                : "Deal locked until next night ends: " + state.supplierDeal.getLabel();
        dealLabel.setText(dealText);

        double creditBalance = state.supplierWineCredit.getBalance();
        double creditCap = state.supplierCreditCap();
        creditLabel.setText(String.format(Locale.US,
                "Credit used: £%.2f / £%.2f  |  Inventory: %d / %d",
                creditBalance, creditCap,
                state.rack.count(), state.rack.getCapacity()));

        wineRows.clear();
        boolean canBuy = !state.nightOpen || state.canEmergencyRestock();
        int freeSlots = state.rack.getCapacity() - state.rack.count();

        for (Wine w : state.supplier) {
            boolean dealApplied = state.supplierDeal != null && state.supplierDeal.appliesTo(w);
            String dealTag = dealApplied ? "  [DEAL]" : "";
            wineRows.add(new Label(w.getName() + dealTag
                    + "  1x £" + String.format(Locale.US, "%.2f", sim.peekSupplierCost(w, 1)), uiSkin)).left().minWidth(200f).padRight(6f);

            for (int q : QUANTITIES) {
                double cost = sim.peekSupplierCost(w, q);
                TextButton btn = new TextButton(
                        "x" + q + "\n£" + String.format(Locale.US, "%.0f", cost), uiSkin);
                final int qty = q;
                btn.setDisabled(!canBuy || freeSlots < q);
                btn.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent event, Actor actor) {
                        if (btn.isDisabled()) return;
                        sim.buyFromSupplier(w, qty);
                        eventBus.fireLog("Bought " + qty + "x " + w.getName());
                        refresh();
                    }
                });
                wineRows.add(btn).width(72f).height(46f).pad(2f);
            }
            wineRows.row();
        }
        pack();
    }

    /** Position centred on stage and show. */
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
