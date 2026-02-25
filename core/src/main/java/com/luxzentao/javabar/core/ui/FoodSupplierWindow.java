package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.Food;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for the Food Supplier screen.
 * Shows current kitchen status, food inventory, and buy buttons (x1 / x5 / x10 / x25).
 */
public class FoodSupplierWindow extends Window {

    private static final int[] QUANTITIES = {1, 5, 10, 25};

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Label noticeLabel;
    private final Label creditLabel;
    private final Table foodRows;

    public FoodSupplierWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Food Supplier (Bulk Buy)", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setResizable(false);
        setModal(false);
        pad(10f);

        add(new Label("Kitchen supplier deals: bulk discounts only.", uiSkin)).left().growX()
                .colspan(QUANTITIES.length + 1).padBottom(2f).row();

        noticeLabel = new Label("", uiSkin);
        noticeLabel.setColor(Color.SALMON);
        noticeLabel.setWrap(true);
        add(noticeLabel).growX().colspan(QUANTITIES.length + 1).padBottom(2f).row();

        creditLabel = new Label("", uiSkin);
        creditLabel.setColor(Color.SALMON);
        creditLabel.setWrap(true);
        add(creditLabel).growX().colspan(QUANTITIES.length + 1).padBottom(8f).row();

        // Column headers
        add(new Label("Food", uiSkin)).left().minWidth(200f);
        for (int q : QUANTITIES) {
            add(new Label("x" + q, uiSkin)).width(72f).center();
        }
        row();

        foodRows = new Table();
        add(foodRows).growX().colspan(QUANTITIES.length + 1).row();

        TextButton closeBtn = new TextButton("Close", uiSkin);
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(closeBtn).right().padTop(8f).row();

        pack();
        setSize(Math.max(getPrefWidth(), 600f), Math.max(getPrefHeight(), 300f));
    }

    public void refresh() {
        boolean kitchenUnlocked = state.kitchenUnlocked;
        noticeLabel.setText(kitchenUnlocked ? "" : "Kitchen not installed (requires Kitchen upgrade).");

        double creditBalance = state.supplierFoodCredit.getBalance();
        double creditCap = state.supplierCreditCap();
        creditLabel.setText(String.format(Locale.US,
                "Credit used: £%.2f / £%.2f  |  Food rack: %d / %d",
                creditBalance, creditCap,
                state.foodRack.count(), state.foodRack.getCapacity()));

        foodRows.clear();
        boolean canBuy = kitchenUnlocked && (!state.nightOpen || state.canEmergencyRestock());
        int freeSlots = state.foodRack.getCapacity() - state.foodRack.count();

        for (Food f : state.foodSupplier) {
            foodRows.add(new Label(f.getName()
                    + "  1x £" + String.format(Locale.US, "%.2f", sim.peekFoodCost(f, 1)), uiSkin))
                    .left().minWidth(200f).padRight(6f);

            for (int q : QUANTITIES) {
                double cost = sim.peekFoodCost(f, q);
                TextButton btn = new TextButton("x" + q + "\n£" + String.format(Locale.US, "%.0f", cost), uiSkin);
                final int qty = q;
                btn.setDisabled(!canBuy || freeSlots < q);
                btn.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent event, Actor actor) {
                        if (btn.isDisabled()) return;
                        sim.buyFoodFromSupplier(f, qty);
                        eventBus.fireLog("Bought " + qty + "x " + f.getName());
                        refresh();
                    }
                });
                foodRows.add(btn).width(72f).height(46f).pad(2f);
            }
            foodRows.row();
        }
        pack();
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
