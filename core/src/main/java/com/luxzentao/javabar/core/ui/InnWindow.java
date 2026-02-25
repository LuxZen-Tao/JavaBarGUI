package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for Inn management.
 * Shows room counts, bookings, rep, and the room price slider.
 */
public class InnWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Label roomsLabel;
    private final Label bookedLabel;
    private final Label priceLabel;
    private final Label repLabel;
    private final Label summaryLabel;
    private final Slider priceSlider;

    public InnWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Inn", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        add(new Label("Rooms, bookings, and nightly upkeep.", uiSkin)).left().growX().padBottom(8f).row();

        roomsLabel = new Label("", uiSkin);
        add(roomsLabel).left().growX().row();

        bookedLabel = new Label("", uiSkin);
        add(bookedLabel).left().growX().padTop(4f).row();

        priceLabel = new Label("", uiSkin);
        add(priceLabel).left().growX().padTop(4f).row();

        priceSlider = new Slider(20f, 120f, 1f, false, uiSkin);
        add(priceSlider).growX().padTop(4f).row();
        priceSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!state.innUnlocked) return;
                sim.setRoomPrice(priceSlider.getValue());
                priceLabel.setText("Room price: £" + String.format(Locale.US, "%.0f", priceSlider.getValue()));
            }
        });

        repLabel = new Label("", uiSkin);
        add(repLabel).left().growX().padTop(6f).row();

        summaryLabel = new Label("", uiSkin);
        summaryLabel.setWrap(true);
        add(summaryLabel).left().growX().padTop(6f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(close).right().padTop(8f).row();

        pack();
        setSize(Math.max(getPrefWidth(), 440f), Math.max(getPrefHeight(), 320f));
    }

    public void refresh() {
        boolean unlocked = state.innUnlocked;
        roomsLabel.setText(unlocked
                ? "Rooms: " + state.roomsTotal + " (Tier " + state.innTier + ")"
                : "Rooms: Locked (purchase Inn Wing upgrade)");
        bookedLabel.setText(unlocked
                ? String.format(Locale.US, "Booked last night: %d  |  Revenue: £%.2f",
                        state.lastNightRoomsBooked, state.lastNightRoomRevenue)
                : "Booked last night: 0");
        priceLabel.setText(unlocked
                ? "Room price: £" + String.format(Locale.US, "%.0f", state.roomPrice)
                : "Room price: Locked");
        priceSlider.setDisabled(!unlocked);
        if (unlocked) priceSlider.setValue((float) state.roomPrice);
        repLabel.setText(String.format(Locale.US, "Inn reputation: %.1f / 100", state.innRep * 100));
        summaryLabel.setText(unlocked
                ? "Manage room price to balance demand and revenue. Upgrade the Inn Wing to add more rooms."
                : "Unlock the inn by purchasing the Inn Wing upgrade.");
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
