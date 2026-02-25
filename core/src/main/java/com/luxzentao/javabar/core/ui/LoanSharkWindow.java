package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.CreditLine;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for the Finance / Loan Shark screen.
 * Shows bank credit lines (repay) and loan shark status (open/view).
 */
public class LoanSharkWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Skin uiSkin;
    private final Label sharkLabel;
    private final Table linesTable;
    private final Table sharkTable;

    public LoanSharkWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Finance", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        add(new Label("Bank Credit Lines:", uiSkin)).left().padBottom(4f).row();
        linesTable = new Table();
        add(linesTable).growX().padBottom(12f).row();

        add(new Label("Loan Shark:", uiSkin)).left().padBottom(4f).row();
        sharkLabel = new Label("", uiSkin);
        sharkLabel.setWrap(true);
        add(sharkLabel).growX().padBottom(4f).row();
        sharkTable = new Table();
        add(sharkTable).growX().padBottom(8f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(close).right().padTop(4f).row();

        pack();
        setSize(Math.max(getPrefWidth(), 520f), Math.max(getPrefHeight(), 340f));
    }

    public void refresh() {
        // Bank credit lines
        linesTable.clear();
        var openLines = state.creditLines.getOpenLines();
        if (openLines.isEmpty()) {
            linesTable.add(new Label("No credit lines open.", uiSkin)).left();
        } else {
            linesTable.add(new Label("Bank", uiSkin)).minWidth(160f).padRight(8f);
            linesTable.add(new Label("Balance", uiSkin)).width(80f);
            linesTable.add(new Label("Limit", uiSkin)).width(80f);
            linesTable.add(new Label("Action", uiSkin)).width(90f);
            linesTable.row();
            for (CreditLine line : openLines) {
                linesTable.add(new Label(line.getLenderName(), uiSkin)).minWidth(160f).padRight(8f);
                linesTable.add(new Label(fmt(line.getBalance()), uiSkin)).width(80f).center();
                linesTable.add(new Label(fmt(line.getLimit()), uiSkin)).width(80f).center();
                TextButton repayBtn = new TextButton("Repay", uiSkin);
                repayBtn.setDisabled(line.getBalance() <= 0.01);
                repayBtn.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent event, Actor actor) {
                        if (!repayBtn.isDisabled()) {
                            sim.repayCreditLineInFull(line.getId());
                            eventBus.fireLog("Repaid " + line.getLenderName());
                            refresh();
                        }
                    }
                });
                linesTable.add(repayBtn).width(90f).height(34f).pad(2f);
                linesTable.row();
            }
        }

        // Loan shark
        sharkTable.clear();
        if (state.loanShark.isOpen()) {
            sharkLabel.setText(String.format(Locale.US,
                    "Balance: £%.2f  |  APR: %.1f%%  |  Weekly minimum: £%.2f",
                    state.loanShark.getBalance(),
                    state.loanShark.getApr() * 100,
                    state.loanShark.minPaymentDue()));
            sharkTable.add(new Label("Loan is active — repaid automatically each week.", uiSkin)).left();
        } else {
            sharkLabel.setText("No active loan shark loan.");
            TextButton openBtn = new TextButton("Take Loan Shark Loan (High APR)", uiSkin);
            openBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    sim.openSharkLine();
                    eventBus.fireLog("Took loan shark loan.");
                    refresh();
                }
            });
            sharkTable.add(openBtn).height(38f).pad(2f);
        }
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "£%.2f", v);
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
