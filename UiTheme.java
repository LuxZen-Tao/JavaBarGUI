import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public final class UiTheme {
    private UiTheme() {
    }

    public static void apply() {
        boolean flatlafLoaded = false;
        try {
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            laf.getMethod("setUseNativeWindowDecorations", boolean.class).invoke(null, false);
            laf.getMethod("setup").invoke(null);
            flatlafLoaded = true;
        } catch (ReflectiveOperationException ignored) {
            // FlatLaf not on classpath.
        }

        // ----------------------------
        // Midnight Manager (Theme 1)
        // ----------------------------
        // Works best with FlatLaf, but we also apply a Nimbus-friendly dark palette
        // if FlatLaf isn't available.
        if (!flatlafLoaded) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // keep default
            }

            ColorUIResource bg = new ColorUIResource(new Color(28, 31, 36));      // #1C1F24
            ColorUIResource panel = new ColorUIResource(new Color(34, 37, 43));   // #22252B
            ColorUIResource panel2 = new ColorUIResource(new Color(40, 44, 52));  // #282C34
            ColorUIResource text = new ColorUIResource(new Color(223, 226, 231)); // soft white
            ColorUIResource muted = new ColorUIResource(new Color(168, 174, 184));
            ColorUIResource accent = new ColorUIResource(new Color(49, 179, 164)); // teal
            ColorUIResource danger = new ColorUIResource(new Color(210, 80, 88));

            // Nimbus core keys
            UIManager.put("control", panel);
            UIManager.put("info", panel);
            UIManager.put("nimbusBase", new ColorUIResource(new Color(45, 49, 58)));
            UIManager.put("nimbusBlueGrey", panel2);
            UIManager.put("nimbusLightBackground", bg);
            UIManager.put("text", text);

            // Common component colors
            UIManager.put("Panel.background", bg);
            UIManager.put("Viewport.background", bg);
            UIManager.put("ScrollPane.background", bg);
            UIManager.put("ScrollBar.background", bg);
            UIManager.put("Label.foreground", text);
            UIManager.put("TitledBorder.titleColor", muted);

            UIManager.put("Button.background", panel2);
            UIManager.put("Button.foreground", text);
            UIManager.put("ToggleButton.background", panel2);
            UIManager.put("ToggleButton.foreground", text);

            UIManager.put("CheckBox.background", bg);
            UIManager.put("CheckBox.foreground", text);
            UIManager.put("RadioButton.background", bg);
            UIManager.put("RadioButton.foreground", text);

            UIManager.put("TextField.background", panel2);
            UIManager.put("TextField.foreground", text);
            UIManager.put("TextField.caretForeground", text);

            UIManager.put("TextArea.background", new ColorUIResource(new Color(20, 22, 26)));
            UIManager.put("TextArea.foreground", text);
            UIManager.put("TextArea.caretForeground", text);

            UIManager.put("TextPane.background", new ColorUIResource(new Color(20, 22, 26)));
            UIManager.put("TextPane.foreground", text);
            UIManager.put("TextPane.caretForeground", text);

            UIManager.put("List.background", new ColorUIResource(new Color(20, 22, 26)));
            UIManager.put("List.foreground", text);
            UIManager.put("List.selectionBackground", accent);
            UIManager.put("List.selectionForeground", new ColorUIResource(Color.BLACK));

            UIManager.put("Table.background", new ColorUIResource(new Color(20, 22, 26)));
            UIManager.put("Table.foreground", text);
            UIManager.put("Table.selectionBackground", accent);
            UIManager.put("Table.selectionForeground", new ColorUIResource(Color.BLACK));

            UIManager.put("MenuBar.background", bg);
            UIManager.put("Menu.background", bg);
            UIManager.put("Menu.foreground", text);
            UIManager.put("MenuItem.background", bg);
            UIManager.put("MenuItem.foreground", text);

            UIManager.put("OptionPane.background", bg);
            UIManager.put("OptionPane.messageForeground", text);
            UIManager.put("OptionPane.foreground", text);
            UIManager.put("ComboBox.background", panel2);
            UIManager.put("ComboBox.foreground", text);
            UIManager.put("TabbedPane.background", bg);
            UIManager.put("TabbedPane.foreground", text);

            // Light semantic accents you can re-use elsewhere
            UIManager.put("pub.accent", accent);
            UIManager.put("pub.danger", danger);
        }

        Font uiFont = pickUiFont(17);

        UIManager.put("defaultFont", uiFont);
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("ToggleButton.font", uiFont);
        UIManager.put("CheckBox.font", uiFont);
        UIManager.put("TabbedPane.font", uiFont);
        UIManager.put("TextField.font", uiFont);
        UIManager.put("TextArea.font", uiFont);
        UIManager.put("TextPane.font", uiFont);
        UIManager.put("List.font", uiFont);
        UIManager.put("TitledBorder.font", uiFont.deriveFont(Font.BOLD, 13f));

        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("CheckBox.arc", 10);
        UIManager.put("ProgressBar.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 12);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.borderWidth", 1);
    }

    private static Font pickUiFont(int size) {
        // Windows-friendly with sane fallbacks.
        String[] candidates = new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "SansSerif"};
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, size);
            if (f.canDisplay('A') && f.canDisplay('£')) {
                return f;
            }
        }
        return new Font("SansSerif", Font.PLAIN, size);
    }

}
