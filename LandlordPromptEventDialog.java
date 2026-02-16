import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class LandlordPromptEventDialog {
    
    public static class Result {
        public final LandlordPromptOption choice;
        public final boolean cancelled;
        
        public Result(LandlordPromptOption choice, boolean cancelled) {
            this.choice = choice;
            this.cancelled = cancelled;
        }
    }

    /**
     * Show a modal dialog for the landlord prompt event.
     * Blocks until the player makes a choice.
     */
    public static Result showEventDialog(Component parent, LandlordPromptEventDef event) {
        CountDownLatch latch = new CountDownLatch(1);
        Result[] resultHolder = new Result[1];

        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Landlord Event", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            
            JPanel content = new JPanel(new BorderLayout(12, 12));
            content.setBackground(new Color(34, 37, 43));
            content.setBorder(new EmptyBorder(20, 24, 20, 24));

            // Title with emoji
            JLabel titleLabel = new JLabel("⚡ Landlord Event");
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
            titleLabel.setForeground(new Color(230, 180, 80));
            content.add(titleLabel, BorderLayout.NORTH);

            // Prompt text
            JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
            centerPanel.setOpaque(false);
            
            JTextArea promptArea = new JTextArea(event.getPromptText());
            promptArea.setFont(promptArea.getFont().deriveFont(14f));
            promptArea.setForeground(new Color(220, 228, 235));
            promptArea.setBackground(new Color(42, 45, 52));
            promptArea.setBorder(new EmptyBorder(12, 12, 12, 12));
            promptArea.setEditable(false);
            promptArea.setLineWrap(true);
            promptArea.setWrapStyleWord(true);
            promptArea.setRows(3);
            centerPanel.add(promptArea, BorderLayout.NORTH);

            // Options panel
            JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 0, 12));
            optionsPanel.setOpaque(false);
            optionsPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

            ButtonGroup buttonGroup = new ButtonGroup();
            JRadioButton[] optionButtons = new JRadioButton[3];
            LandlordPromptOption[] options = {LandlordPromptOption.A, LandlordPromptOption.B, LandlordPromptOption.C};

            for (int i = 0; i < 3; i++) {
                LandlordPromptOption option = options[i];
                String optionText = event.getOptionText(option);
                
                JRadioButton radioButton = new JRadioButton(option.name() + ": " + optionText);
                radioButton.setFont(radioButton.getFont().deriveFont(13f));
                radioButton.setForeground(new Color(210, 218, 230));
                radioButton.setBackground(new Color(42, 45, 52));
                radioButton.setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 4, 0, 0, new Color(110, 150, 230)),
                        new EmptyBorder(10, 12, 10, 12)
                ));
                radioButton.setOpaque(true);
                
                buttonGroup.add(radioButton);
                optionButtons[i] = radioButton;
                optionsPanel.add(radioButton);
            }

            // Select first option by default
            optionButtons[0].setSelected(true);

            centerPanel.add(optionsPanel, BorderLayout.CENTER);
            content.add(centerPanel, BorderLayout.CENTER);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttonPanel.setOpaque(false);

            JButton confirmButton = new JButton("Confirm");
            confirmButton.setFont(confirmButton.getFont().deriveFont(Font.BOLD, 13f));
            JButton cancelButton = new JButton("Cancel (Risk Random)");

            confirmButton.addActionListener(e -> {
                // Find selected option
                for (int i = 0; i < optionButtons.length; i++) {
                    if (optionButtons[i].isSelected()) {
                        resultHolder[0] = new Result(options[i], false);
                        break;
                    }
                }
                dialog.dispose();
                latch.countDown();
            });

            cancelButton.addActionListener(e -> {
                // Random choice if cancelled
                resultHolder[0] = new Result(options[0], true); // Default to A if cancelled
                dialog.dispose();
                latch.countDown();
            });

            buttonPanel.add(cancelButton);
            buttonPanel.add(confirmButton);
            content.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(content);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(550, 350));
            
            // Center on screen
            dialog.setLocationRelativeTo(parent);
            
            dialog.setVisible(true);
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result(LandlordPromptOption.A, true);
        }

        return resultHolder[0] != null ? resultHolder[0] : new Result(LandlordPromptOption.A, true);
    }

    /**
     * Show a modal dialog with the event outcome.
     */
    public static void showOutcomeDialog(Component parent, LandlordPromptEventDef event, 
                                         LandlordPromptOption choice, LandlordPromptResultType resultType,
                                         String narrativeText, String effectsSummary) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Event Outcome", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            
            JPanel content = new JPanel(new BorderLayout(12, 12));
            content.setBackground(new Color(34, 37, 43));
            content.setBorder(new EmptyBorder(20, 24, 20, 24));

            // Title with result type
            String emoji = resultType == LandlordPromptResultType.GOOD ? "✅" : 
                          resultType == LandlordPromptResultType.NEUTRAL ? "➖" : "❌";
            Color accentColor = resultType == LandlordPromptResultType.GOOD ? new Color(64, 170, 110) :
                               resultType == LandlordPromptResultType.NEUTRAL ? new Color(120, 160, 180) :
                               new Color(210, 80, 88);
            
            JLabel titleLabel = new JLabel(emoji + " " + resultType.name() + " Outcome");
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
            titleLabel.setForeground(accentColor);
            content.add(titleLabel, BorderLayout.NORTH);

            // Narrative text
            JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
            centerPanel.setOpaque(false);
            
            JTextArea narrativeArea = new JTextArea(narrativeText);
            narrativeArea.setFont(narrativeArea.getFont().deriveFont(14f));
            narrativeArea.setForeground(new Color(220, 228, 235));
            narrativeArea.setBackground(new Color(42, 45, 52));
            narrativeArea.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 4, 0, 0, accentColor),
                    new EmptyBorder(12, 12, 12, 12)
            ));
            narrativeArea.setEditable(false);
            narrativeArea.setLineWrap(true);
            narrativeArea.setWrapStyleWord(true);
            narrativeArea.setRows(3);
            centerPanel.add(narrativeArea, BorderLayout.NORTH);

            // Effects summary
            if (effectsSummary != null && !effectsSummary.isEmpty()) {
                JLabel effectsLabel = new JLabel("Effects: " + effectsSummary);
                effectsLabel.setFont(effectsLabel.getFont().deriveFont(12f));
                effectsLabel.setForeground(new Color(180, 190, 200));
                effectsLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
                centerPanel.add(effectsLabel, BorderLayout.CENTER);
            }

            content.add(centerPanel, BorderLayout.CENTER);

            // Close button
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setOpaque(false);
            
            JButton closeButton = new JButton("Continue");
            closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, 13f));
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);
            
            content.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(content);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(500, 280));
            
            // Center on screen
            dialog.setLocationRelativeTo(parent);
            
            dialog.setVisible(true);
        });
    }
}
