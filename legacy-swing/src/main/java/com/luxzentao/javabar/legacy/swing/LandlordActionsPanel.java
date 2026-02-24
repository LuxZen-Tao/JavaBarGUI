package com.luxzentao.javabar.legacy.swing;

import com.luxzentao.javabar.core.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;

public class LandlordActionsPanel extends JPanel {
    private final Simulation sim;
    private final GameState state;
    private final Consumer<LandlordActionId> actionHandler;
    private final JLabel headerLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();
    private final JPanel cardsPanel = new JPanel();
    private int actionCount = 0;

    public LandlordActionsPanel(Simulation sim, GameState state, Consumer<LandlordActionId> actionHandler) {
        super(new BorderLayout(8, 8));
        this.sim = sim;
        this.state = state;
        this.actionHandler = actionHandler;
        buildLayout();
        refresh();
    }

    private void buildLayout() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(6, 8, 6, 8));
        headerLabel.setFont(headerLabel.getFont().deriveFont(headerLabel.getFont().getSize2D() + 1f));
        detailLabel.setForeground(new Color(180, 180, 180));
        header.add(headerLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(detailLabel);

        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh() {
        int tier = sim.landlordActionTier();
        String vibe = sim.landlordIdentityLabel();
        String roundLock = sim.canUseLandlordActionThisRound() ? "Action available" : "Action spent this round";
        headerLabel.setText("Tier " + tier + " Actions");
        detailLabel.setText("Pub vibe: " + vibe + " | " + roundLock + " | Cooldowns tick each round");

        cardsPanel.removeAll();
        List<LandlordActionDef> defs = sim.getAvailableActionsForCurrentTier();
        actionCount = defs.size();
        for (LandlordActionDef def : defs) {
            cardsPanel.add(makeActionCard(def));
            cardsPanel.add(Box.createVerticalStrut(6));
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public int getActionRowCount() {
        return actionCount;
    }

    private JPanel makeActionCard(LandlordActionDef def) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 80)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setBackground(new Color(40, 42, 48));

        JLabel title = new JLabel(def.getCategory().getLabel() + " · " + def.getName());
        title.setFont(title.getFont().deriveFont(title.getFont().getStyle() | java.awt.Font.BOLD));
        JLabel desc = new JLabel("<html>" + def.getDescription() + "</html>");
        desc.setForeground(new Color(200, 200, 200));
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(desc);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        meta.setOpaque(false);
        LandlordActionState actionState = state.landlordActionStates.get(def.getId());
        int cooldownRemaining = actionState == null ? 0 : actionState.getCooldownRemaining();
        JLabel costLabel = new JLabel("Cost: GBP " + def.getBaseCost());
        costLabel.setForeground(new Color(250, 200, 100));
        JLabel cooldown = new JLabel("Cooldown: " + cooldownRemaining + "r");
        cooldown.setForeground(new Color(180, 180, 180));
        double chance = sim.computeActionChance(def);
        JLabel chanceLabel = new JLabel("Chance: " + Math.round(chance * 100) + "%");
        chanceLabel.setForeground(new Color(180, 180, 180));
        meta.add(costLabel);
        meta.add(cooldown);
        meta.add(chanceLabel);

        JLabel successLabel = new JLabel("Success: " + def.formatSuccessSummary());
        JLabel failLabel = new JLabel("Failure: " + def.formatFailureSummary());
        successLabel.setForeground(new Color(120, 200, 140));
        failLabel.setForeground(new Color(220, 130, 130));
        JPanel rangePanel = new JPanel();
        rangePanel.setLayout(new BoxLayout(rangePanel, BoxLayout.Y_AXIS));
        rangePanel.setOpaque(false);
        rangePanel.add(successLabel);
        rangePanel.add(failLabel);

        JButton useBtn = new JButton("Use");
        useBtn.setHorizontalAlignment(SwingConstants.CENTER);
        useBtn.setPreferredSize(new Dimension(96, 30));

        Simulation.LandlordActionAvailability availability = sim.landlordActionAvailability(def);
        if (!availability.canUse()) {
            useBtn.setEnabled(false);
            useBtn.setText(availability.reason().startsWith("Locked") ? "Locked" : "Unavailable");
            JLabel lockLabel = new JLabel(availability.reason());
            lockLabel.setForeground(new Color(200, 160, 120));
            rangePanel.add(Box.createVerticalStrut(2));
            rangePanel.add(lockLabel);
        }

        useBtn.addActionListener(e -> actionHandler.accept(def.getId()));

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(useBtn, BorderLayout.NORTH);
        right.add(Box.createVerticalGlue(), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(meta, BorderLayout.NORTH);
        center.add(rangePanel, BorderLayout.CENTER);

        card.add(titlePanel, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }
}
