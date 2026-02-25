package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.Bank;
import com.luxzentao.javabar.core.CreditLine;
import com.luxzentao.javabar.core.GameState;
import com.luxzentao.javabar.core.Simulation;
import com.luxzentao.javabar.core.sim.SimEventBus;

import java.util.Locale;

/**
 * Floating Scene2D Window for the Bank / Finance screen.
 * Shows credit score, open credit lines, available banks.
 */
public class BankWindow extends Window {

    private final Simulation sim;
    private final GameState state;
    private final SimEventBus eventBus;
    private final Label scoreLabel;
    private final Table linesTable;
    private final Table banksTable;
    private final Skin uiSkin;

    public BankWindow(Skin skin, Simulation sim, GameState state, SimEventBus eventBus) {
        super("Bank / Finance", skin);
        this.sim = sim;
        this.state = state;
        this.eventBus = eventBus;
        this.uiSkin = skin;

        setMovable(true);
        setModal(false);
        pad(10f);

        scoreLabel = new Label("", uiSkin);
        add(scoreLabel).growX().padBottom(8f).row();

        add(new Label("Open Credit Lines:", uiSkin)).left().padBottom(4f).row();
        linesTable = new Table();
        add(linesTable).growX().padBottom(12f).row();

        add(new Label("Available Banks:", uiSkin)).left().padBottom(4f).row();
        banksTable = new Table();
        add(banksTable).growX().padBottom(8f).row();

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setVisible(false);
            }
        });
        add(close).right().padTop(4f).row();

        pack();
        setSize(Math.max(getPrefWidth(), 560f), Math.max(getPrefHeight(), 360f));
    }

    public void refresh() {
        scoreLabel.setText(String.format(Locale.US,
                "Cash: £%.2f  |  Credit Score: %d  |  Total Debt: £%.2f / £%.2f",
                state.cash, state.creditScore,
                state.totalCreditBalance(), state.totalCreditLimit()));

        // Open lines
        linesTable.clear();
        var openLines = state.creditLines.getOpenLines();
        if (openLines.isEmpty()) {
            linesTable.add(new Label("No credit lines open.", uiSkin)).left();
        } else {
            linesTable.add(new Label("Bank", uiSkin)).minWidth(180f).padRight(8f);
            linesTable.add(new Label("Balance", uiSkin)).width(90f);
            linesTable.add(new Label("Limit", uiSkin)).width(90f);
            linesTable.add(new Label("APR", uiSkin)).width(60f);
            linesTable.add(new Label("Action", uiSkin)).width(100f);
            linesTable.row();
            for (CreditLine line : openLines) {
                linesTable.add(new Label(line.getLenderName(), uiSkin)).minWidth(180f).padRight(8f);
                linesTable.add(new Label(fmt(line.getBalance()), uiSkin)).width(90f).center();
                linesTable.add(new Label(fmt(line.getLimit()), uiSkin)).width(90f).center();
                linesTable.add(new Label(String.format(Locale.US, "%.1f%%", line.getInterestAPR() * 100), uiSkin)).width(60f).center();
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
                linesTable.add(repayBtn).width(100f).height(34f).pad(2f);
                linesTable.row();
            }
        }

        // Available banks to open
        banksTable.clear();
        banksTable.add(new Label("Bank", uiSkin)).minWidth(180f).padRight(8f);
        banksTable.add(new Label("Min Score", uiSkin)).width(90f);
        banksTable.add(new Label("Limit Range", uiSkin)).minWidth(120f);
        banksTable.add(new Label("Action", uiSkin)).width(110f);
        banksTable.row();
        for (Bank bank : Bank.values()) {
            boolean alreadyOpen = state.creditLines.hasLine(bank.getName());
            boolean unlocked = bank.isUnlocked(state.creditScore);

            Label nameLbl = new Label(bank.getName(), uiSkin);
            if (!unlocked || alreadyOpen) nameLbl.setColor(Color.GRAY);
            banksTable.add(nameLbl).minWidth(180f).padRight(8f);
            banksTable.add(new Label("" + bank.getMinScore(), uiSkin)).width(90f).center();
            banksTable.add(new Label(String.format(Locale.US, "£%d–£%d",
                    bank.getMinLimit(), bank.getMaxLimit()), uiSkin)).minWidth(120f).center();

            TextButton openBtn = new TextButton(alreadyOpen ? "Open" : "Apply", uiSkin);
            openBtn.setDisabled(alreadyOpen || !unlocked);
            openBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (!openBtn.isDisabled()) {
                        sim.openCreditLine(bank);
                        eventBus.fireLog("Applied for credit: " + bank.getName());
                        refresh();
                    }
                }
            });
            banksTable.add(openBtn).width(110f).height(34f).pad(2f);
            banksTable.row();
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
