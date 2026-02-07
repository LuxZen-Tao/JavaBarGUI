import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UIPopup {

    private UIPopup() {}

    public enum PopupStyle {
        POSITIVE("✅", new Color(64, 170, 110)),
        NEGATIVE("❌", new Color(210, 80, 88)),
        EVENT("🎉", new Color(110, 150, 230)),
        WARNING("⚠️", new Color(230, 180, 80)),
        INFO("ℹ️", new Color(120, 160, 180));

        private final String emoji;
        private final Color accent;

        PopupStyle(String emoji, Color accent) {
            this.emoji = emoji;
            this.accent = accent;
        }

        public String emoji() { return emoji; }
        public Color accent() { return accent; }
    }

    public static void showPopup(Component parent, String title, String body, String effects) {
        showPopup(parent, PopupStyle.EVENT, title, body, effects);
    }

    public static void showPopup(Component parent, PopupStyle style, String title, String body, String effects) {
        String safeTitle = title == null ? "Event" : title;
        String safeBody = body == null ? "" : body;
        String safeEffects = effects == null ? "" : effects;
        PopupStyle useStyle = style == null ? PopupStyle.INFO : style;
        String displayTitle = useStyle.emoji() + " " + safeTitle;

        StringBuilder html = new StringBuilder();
        // Midnight Manager: dark, readable, low-contrast.
        html.append("<html><body style='font-family:sans-serif;font-size:12pt;color:#dfe2e7;'>");
        html.append("<div style='font-size:13pt;font-weight:bold;margin-bottom:6px;'>")
                .append(escapeHtml(displayTitle))
                .append("</div>");
        if (!safeBody.isBlank()) {
            // safeBody may already contain <br/> / <b> etc from your logger, so do not escape it.
            html.append("<div style='margin-bottom:6px;'>")
                    .append(safeBody)
                    .append("</div>");
        }
        if (!safeEffects.isBlank()) {
            html.append("<div style='color:#a8aeb8;font-size:11pt;'>")
                    .append(escapeHtml(safeEffects))
                    .append("</div>");
        }
        html.append("</body></html>");

        JEditorPane pane = new JEditorPane("text/html", html.toString());
        pane.setEditable(false);
        pane.setOpaque(false);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(true);
        content.setBackground(new Color(34, 37, 43));
        content.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 6, 0, 0, useStyle.accent()),
                new EmptyBorder(10, 12, 10, 12)
        ));
        content.add(pane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(closeBtn);
        content.add(actions, BorderLayout.SOUTH);

        JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new Object[]{}, null);
        JDialog dialog = optionPane.createDialog(parent, safeTitle);
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setBackground(new Color(28, 31, 36));
        dialog.pack();

        closeBtn.addActionListener(e -> dialog.dispose());

        // Click-anywhere-to-dismiss (content + pane) + auto-dismiss after 5 seconds.
        content.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dialog.dispose();
            }
        });
        pane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dialog.dispose();
            }
        });

        javax.swing.Timer t = new javax.swing.Timer(5000, e -> dialog.dispose());
        t.setRepeats(false);
        t.start();

        positionDialog(dialog, parent);
        dialog.setVisible(true);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static void positionDialog(JDialog dialog, Component parent) {
        if (parent != null) {
            dialog.setLocationRelativeTo(parent);
        } else {
            dialog.setLocationRelativeTo(null);
        }
    }
}
