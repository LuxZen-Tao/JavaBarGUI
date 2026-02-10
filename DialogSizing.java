import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

public final class DialogSizing {
    private static final double SCREEN_USAGE_RATIO = 0.90;
    private static final int MIN_DIALOG_WIDTH = 900;

    private DialogSizing() {}

    public static void packClampAndCenter(Window window, Window parent) {
        if (window == null) return;

        window.pack();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxW = Math.max(1, (int) Math.floor(screen.width * SCREEN_USAGE_RATIO));
        int maxH = Math.max(1, (int) Math.floor(screen.height * SCREEN_USAGE_RATIO));

        int minW = Math.min(MIN_DIALOG_WIDTH, maxW);

        Dimension packed = window.getSize();
        int width = Math.max(minW, Math.min(packed.width, maxW));
        int height = Math.max(1, Math.min(packed.height, maxH));

        window.setMinimumSize(new Dimension(minW, 1));
        window.setSize(width, height);
        window.setLocationRelativeTo(parent);
    }

    public static void clampToScreen(Window window) {
        if (window == null) return;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxW = Math.max(1, (int) Math.floor(screen.width * SCREEN_USAGE_RATIO));
        int maxH = Math.max(1, (int) Math.floor(screen.height * SCREEN_USAGE_RATIO));

        int minW = Math.min(MIN_DIALOG_WIDTH, maxW);

        Dimension size = window.getSize();
        int width = Math.max(minW, Math.min(size.width, maxW));
        int height = Math.max(1, Math.min(size.height, maxH));
        window.setMinimumSize(new Dimension(minW, 1));
        window.setSize(width, height);
    }
}
