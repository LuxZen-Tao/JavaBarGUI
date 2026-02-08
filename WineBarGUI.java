import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class WineBarGUI {
    private static final Color CASH_BG = new Color(36, 130, 92);
    private static final Color DEBT_BG = new Color(170, 60, 72);
    private static final Color REP_BG = new Color(88, 92, 200);
    private static final Color SECURITY_BG = new Color(188, 132, 46);
    private static final Color NIGHT_BG = new Color(78, 120, 210);
    private static final Color CAL_BG = new Color(66, 96, 140);
    private static final Color REPORT_BG = new Color(92, 84, 124);
    private static final Color STAFF_BG = new Color(86, 122, 120);
    private static final Color SERVE_BG = new Color(92, 102, 122);
    private static final Color OBS_BG = new Color(72, 88, 126);
    private static final Color FLASH_GREEN = new Color(68, 168, 110);
    private static final Color FLASH_RED = new Color(210, 80, 88);
    private static final Color INSTALLING_BG = new Color(245, 214, 120);

    private final GameState state;
    private final Simulation sim;

    private final JFrame frame = new JFrame("Pub Landlord Idle");
    private final JPanel root = new JPanel(new BorderLayout(10, 10));
    private final JPanel hud = new JPanel(new GridLayout(0, 3, 6, 3));
    private final JPanel controls = new JPanel();
    private final JToggleButton happyHourBtn = new JToggleButton("Happy Hour: OFF");
    private final JCheckBox logTimestampToggle = new JCheckBox("Timestamps");

    // Price control
    private JSlider priceSlider;
    private JLabel priceLabel;

    // Supplier window
    private JDialog supplierDialog;
    private JPanel supplierListPanel;
    private JLabel supplierDealLabel;
    private JDialog kitchenSupplierDialog;
    private JPanel kitchenSupplierListPanel;

    // Staff window (hire + fire)
    private JDialog staffDialog;
    private JPanel staffHirePanel;
    private JPanel staffRosterPanel;

    // Loan shark window
    private JDialog loanDialog;
    private JTextArea loanTextArea;
    private JPanel loanButtonsPanel;

    // Upgrades window
    private JDialog upgradesDialog;
    private JPanel upgradesListPanel;

    // Activities window
    private JDialog activitiesDialog;
    private JPanel activitiesListPanel;

    // Reports panel (right side)
    private JTextArea reportArea;
    private JLabel reportSummaryCash;
    private JLabel reportSummaryDebt;
    private JLabel reportSummaryRep;
    private JLabel reportSummarySecurity;
    private JLabel reportSummaryStaff;
    private JLabel reportSummaryServe;
    private JLabel reportSummaryRefunds;
    private JLabel reportSummaryInvoice;

    private final JTextPane logPane = new JTextPane();
    private final UILogger log;
    private EventFeedDialog eventFeedDialog;

    private final DefaultListModel<String> invModel = new DefaultListModel<>();
    private final JList<String> invList = new JList<>(invModel);

    // HUD labels
    private final JLabel cashLabel = new JLabel();
    private final JLabel debtLabel = new JLabel();
    private final JLabel repLabel = new JLabel();
    private final JLabel pubNameLabel = new JLabel();
    private final JLabel invoiceDueLabel = new JLabel();
    private final JLabel calendarLabel = new JLabel();
    private final JLabel roundLabel = new JLabel();
    private final JLabel securityLabel = new JLabel();
    private final JLabel staffLabel = new JLabel();
    private final JLabel reportLabel = new JLabel();
    private final JLabel serveCapLabel = new JLabel();
    private final JLabel observationLabel = new JLabel();
    private JLabel nightIndicator;
    private JPanel cashBadge;
    private JPanel debtBadge;
    private JPanel securityBadge;
    private JPanel calendarBadge;
    private JPanel reportBadge;
    private JPanel staffBadge;
    private JPanel serveBadge;
    private JPanel nightBadge;
    private JPanel pubNameBadge;
    private JPanel observationBadge;


    // Buttons
    private final JButton openBtn = new JButton("Open Pub");
    private final JButton nextRoundBtn = new JButton("Next Round");
    private final JButton closeBtn = new JButton("Close Night");

    private final JButton supplierBtn = new JButton("Supplier");
    private final JButton kitchenSupplierBtn = new JButton("Food Supplier");
    private final JButton upgradesBtn = new JButton("Upgrades");
    private final JButton activitiesBtn = new JButton("Activities");
    private final JButton staffBtn = new JButton("Staff");

    private final JButton securityBtn = new JButton("Security");
    private final JButton payDebtBtn = new JButton("Pay Debt");
    private final JButton loanSharkBtn = new JButton("Loan Shark");

    private final JToggleButton autoBtn = new JToggleButton("Auto: OFF");
    private Timer autoTimer;
    private Timer nightPulseTimer;
    private Timer cashFlashTimer;
    private Timer debtFlashTimer;
    private double lastCash;
    private double lastDebt;
    private boolean nightPulseOn;
    private MetricsSnapshot lastSnapshot;

    private JDialog reportsDialog;
    private JTextArea reportsDialogArea;
    private JTextArea reportsDialogLoansArea;
    private JDialog missionControlDialog;
    private JTextArea missionOverviewArea;
    private JTextArea missionEconomyArea;
    private JTextArea missionOperationsArea;
    private JTextArea missionStaffArea;
    private JTextArea missionRiskArea;
    private JTextArea missionReputationArea;
    private JTextArea missionRumorsArea;
    private JTextArea missionTrafficArea;
    private JTextArea missionInventoryArea;
    private JTextArea missionLoansArea;
    private JTextArea missionLogArea;
    private JDialog weeklyReportDialog;
    private JTextArea weeklyReportArea;
    private JDialog fourWeekReportDialog;
    private JTextArea fourWeekReportArea;
    private final Preferences prefs = Preferences.userNodeForPackage(WineBarGUI.class);

    // Money formatting for UI (visual only)
    private static final DecimalFormat MONEY_2DP;
    private static final DecimalFormat MONEY_0DP;
    static {
        DecimalFormatSymbols uk = DecimalFormatSymbols.getInstance(Locale.UK);
        MONEY_2DP = new DecimalFormat("£#,##0.00", uk);
        MONEY_0DP = new DecimalFormat("£#,##0", uk);
    }

    private static String money2(double v) {
        synchronized (MONEY_2DP) {
            return MONEY_2DP.format(v);
        }
    }

    private static String money0(double v) {
        synchronized (MONEY_0DP) {
            return MONEY_0DP.format(v);
        }
    }

    private JDialog securityDialog;
    private JButton securityUpgradeBtn;
    private JButton bouncerBtn;

    public WineBarGUI(GameState state) {
        this.state = state;

        this.log = new UILogger(logPane);
        this.sim = new Simulation(state, log);

        buildUI();
        eventFeedDialog = new EventFeedDialog(frame);
        log.setEventSink(this::appendEventToFeeds);
        log.setPopupSink(this::showPopup);
        wireEvents();

        refreshAll();
        log.header("READY");
        log.info("Supplier  restock. Staff  hire. Then Open Pub.");
    }

    public void show() { frame.setVisible(true); }

    private void buildUI() {
        logPane.setEditable(false);
        Font base = UIManager.getFont("TextPane.font");
        if (base != null) {
            logPane.setFont(base.deriveFont((float) (base.getSize() + 2)));
        }
        logPane.setMargin(new Insets(10, 12, 10, 12));

        invList.setVisibleRowCount(14);

        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));

        hud.setBorder(new EmptyBorder(2, 4, 2, 4));
        hud.setOpaque(false);
        controls.setOpaque(false);
        cashBadge = createBadge(CASH_BG, cashLabel);
        debtBadge = createBadge(DEBT_BG, debtLabel);
        JPanel repBadge = createBadge(REP_BG, repLabel);
        pubNameLabel.setFont(pubNameLabel.getFont().deriveFont(Font.BOLD));
        pubNameBadge = createBadge(new Color(70, 110, 160), pubNameLabel);
        JPanel invoiceBadge = createBadge(REPORT_BG, invoiceDueLabel);
        securityBadge = createBadge(SECURITY_BG, securityLabel);
        calendarBadge = createBadge(CAL_BG, calendarLabel);
        reportBadge = createBadge(REPORT_BG, reportLabel);
        staffBadge = createBadge(STAFF_BG, staffLabel);
        serveBadge = createBadge(SERVE_BG, serveCapLabel);
        observationLabel.setVerticalAlignment(SwingConstants.TOP);
        Font obsFont = observationLabel.getFont();
        float obsSize = Math.max(10f, obsFont.getSize() - 2f);
        observationLabel.setFont(obsFont.deriveFont(obsSize));
        observationLabel.setPreferredSize(new Dimension(220, 32));
        observationLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        observationBadge = createBadge(OBS_BG, observationLabel);
        serveCapLabel.setVerticalAlignment(SwingConstants.TOP);
        Font quipFont = serveCapLabel.getFont();
        float quipSize = Math.max(10f, quipFont.getSize() - 2f);
        serveCapLabel.setFont(quipFont.deriveFont(quipSize));
        serveCapLabel.setPreferredSize(new Dimension(220, 32));
        serveCapLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        nightBadge = createNightBadge();

        hud.add(pubNameBadge);
        hud.add(cashBadge);
        hud.add(debtBadge);
        hud.add(repBadge);
        hud.add(invoiceBadge);
        hud.add(securityBadge);
        hud.add(calendarBadge);
        hud.add(reportBadge);
        hud.add(staffBadge);
        hud.add(nightBadge);
        hud.add(serveBadge);
        hud.add(observationBadge);

        // Price multiplier control (0.50x to 2.50x)
        priceLabel = new JLabel("Price x" + String.format("%.2f", state.priceMultiplier));
        priceSlider = new JSlider(50, 250, (int)Math.round(state.priceMultiplier * 100));
        priceSlider.setPreferredSize(new Dimension(140, 26));
        priceSlider.addChangeListener(e -> {
            double m = priceSlider.getValue() / 100.0;
            sim.setPriceMultiplier(m);
            priceLabel.setText("Price x" + String.format("%.2f", state.priceMultiplier));
            refreshAll();
        });

        JPanel nightControls = createControlGroup("Night", openBtn, nextRoundBtn, closeBtn, happyHourBtn);
        JPanel economyControls = createControlGroup("Economy", priceLabel, priceSlider, supplierBtn, kitchenSupplierBtn, payDebtBtn, loanSharkBtn);
        JPanel managementControls = createControlGroup("Management", staffBtn, upgradesBtn);
        JPanel riskControls = createControlGroup("Risk", securityBtn);
        JPanel activityControls = createControlGroup("Activities", activitiesBtn);
        JPanel autoControls = createControlGroup("Automation", autoBtn);

        controls.add(nightControls);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(economyControls);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(managementControls);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(riskControls);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(activityControls);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(autoControls);

        applyButtonIcons();

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(420, 1));
        right.add(makeReportsPanel());
        right.add(Box.createVerticalStrut(8));
        right.add(makeInventoryPanel());

        JPanel logPanel = new JPanel(new BorderLayout(6, 6));
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.add(new JLabel("Activity Log"), BorderLayout.WEST);
        logTimestampToggle.addActionListener(e -> log.setShowTimestamps(logTimestampToggle.isSelected()));
        JButton eventFeedBtn = new JButton("Event Feed");
        eventFeedBtn.addActionListener(e -> openEventFeedDialog());
        JPanel logActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        logActions.setOpaque(false);
        logActions.add(eventFeedBtn);
        logActions.add(logTimestampToggle);
        logHeader.add(logActions, BorderLayout.EAST);
        logPanel.add(logHeader, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logPane), BorderLayout.CENTER);

        root.add(hud, BorderLayout.NORTH);
        root.add(logPanel, BorderLayout.CENTER);
        root.add(controls, BorderLayout.SOUTH);
        root.add(right, BorderLayout.EAST);

        frame.setContentPane(root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1220, 740);
        frame.setLocationRelativeTo(null);
        frame.setTitle("Pub Landlord Idle - " + state.pubName);

        nextRoundBtn.setEnabled(false);
        closeBtn.setEnabled(false);
    }

    private void showPopup(UILogger.PopupMessage message) {
        if (message == null) return;
        UIPopup.showPopup(frame, message.style(), message.title(), message.body(), message.effects());
    }

    private JPanel createBadge(Color bg, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(3, 7, 3, 7));
        panel.setOpaque(true);
        panel.setBackground(bg);
        panel.putClientProperty("FlatLaf.style", "arc: 16");
        content.setForeground(Color.WHITE);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNightBadge() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        wrapper.setOpaque(false);
        nightIndicator = new JLabel("");
        nightIndicator.setForeground(new Color(220, 220, 220));
        nightIndicator.setVisible(false);
        wrapper.add(nightIndicator);
        wrapper.add(roundLabel);
        return createBadge(NIGHT_BG, wrapper);
    }

    private JPanel createControlGroup(String title, JComponent... components) {
        JPanel group = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        group.setBorder(BorderFactory.createTitledBorder(title));
        group.setOpaque(true);
        group.setBackground(new Color(34, 37, 43));
        group.putClientProperty("FlatLaf.style", "arc: 14");
        for (JComponent component : components) {
            group.add(component);
        }
        return group;
    }

    private void applyButtonIcons() {
        supplierBtn.setIcon(createGlyphIcon("S", new Color(110, 190, 140)));
        kitchenSupplierBtn.setIcon(createGlyphIcon("K", new Color(170, 140, 90)));
        staffBtn.setIcon(createGlyphIcon("P", new Color(120, 170, 220)));
        upgradesBtn.setIcon(createGlyphIcon("U", new Color(180, 150, 240)));
        activitiesBtn.setIcon(createGlyphIcon("A", new Color(200, 170, 110)));
        loanSharkBtn.setIcon(createGlyphIcon("L", new Color(220, 120, 120)));
    }

    private Icon createGlyphIcon(String text, Color color) {
        boolean enabled = UIManager.getBoolean("pub.icons.enabled");
        if (!enabled && UIManager.get("pub.icons.enabled") != null) {
            return null;
        }
        return new TextIcon(text, color);
    }

    private static class TextIcon implements Icon {
        private final String text;
        private final Color color;

        private TextIcon(String text, Color color) {
            this.text = text;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = getIconWidth();
            g2.setColor(new Color(0, 0, 0, 0));
            g2.fillOval(x, y, size, size);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.setColor(Color.WHITE);
            Font font = c.getFont().deriveFont(Font.BOLD, 11f);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(text)) / 2;
            int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }

    private JPanel makeInventoryPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Inventory"));
        p.add(new JScrollPane(invList), BorderLayout.CENTER);
        return p;
    }

    private JPanel makeReportsPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Reports"));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Reports (Live)");
        JButton missionControlBtn = new JButton("Mission Control");
        missionControlBtn.addActionListener(e -> openMissionControlDialog());
        header.add(title, BorderLayout.WEST);
        header.add(missionControlBtn, BorderLayout.EAST);

        JPanel summary = new JPanel(new GridLayout(0, 2, 6, 4));
        summary.setBorder(new EmptyBorder(4, 4, 4, 4));
        summary.setPreferredSize(new Dimension(0, 116));
        reportSummaryCash = new JLabel();
        reportSummaryDebt = new JLabel();
        reportSummaryRep = new JLabel();
        reportSummarySecurity = new JLabel();
        reportSummaryStaff = new JLabel();
        reportSummaryServe = new JLabel();
        reportSummaryRefunds = new JLabel();
        reportSummaryInvoice = new JLabel();
        summary.add(reportSummaryCash);
        summary.add(reportSummaryDebt);
        summary.add(reportSummaryRep);
        summary.add(reportSummarySecurity);
        summary.add(reportSummaryStaff);
        summary.add(reportSummaryServe);
        summary.add(reportSummaryRefunds);
        summary.add(reportSummaryInvoice);

        reportArea = new JTextArea(14, 30);
        reportArea.setEditable(false);
        reportArea.setFont(UIManager.getFont("TextArea.font"));
        reportArea.setLineWrap(false);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setPreferredSize(new Dimension(0, 240));

        p.add(header, BorderLayout.NORTH);
        p.add(summary, BorderLayout.CENTER);
        p.add(reportScroll, BorderLayout.SOUTH);
        return p;
    }

    private void openReportsDialog() {
        if (reportsDialog == null) {
            reportsDialog = new JDialog(frame, "Reports", false);
            reportsDialog.setLayout(new BorderLayout(8, 8));

            JTabbedPane tabs = new JTabbedPane();

            reportsDialogArea = new JTextArea(20, 50);
            reportsDialogArea.setEditable(false);
            reportsDialogArea.setFont(UIManager.getFont("TextArea.font"));
            tabs.add("Live Reports", new JScrollPane(reportsDialogArea));

            JList<String> reportsDialogInventoryList = new JList<>(invModel);
            tabs.add("Inventory", new JScrollPane(reportsDialogInventoryList));

            reportsDialogLoansArea = new JTextArea(14, 40);
            reportsDialogLoansArea.setEditable(false);
            reportsDialogLoansArea.setFont(UIManager.getFont("TextArea.font"));
            tabs.add("Loans", new JScrollPane(reportsDialogLoansArea));

            reportsDialog.add(tabs, BorderLayout.CENTER);

            reportsDialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    saveReportsDialogBounds();
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    saveReportsDialogBounds();
                }
            });

            restoreReportsDialogBounds();
        }

        refreshReportsDialog();
        reportsDialog.setVisible(true);
    }

    private void openMissionControlDialog() {
        if (missionControlDialog == null) {
            missionControlDialog = new JDialog(frame, "Mission Control", false);
            missionControlDialog.setLayout(new BorderLayout(8, 8));

            JTabbedPane tabs = new JTabbedPane();
            missionOverviewArea = createMissionTextArea();
            missionEconomyArea = createMissionTextArea();
            missionOperationsArea = createMissionTextArea();
            missionStaffArea = createMissionTextArea();
            missionRiskArea = createMissionTextArea();
            missionReputationArea = createMissionTextArea();
            missionRumorsArea = createMissionTextArea();
            missionTrafficArea = createMissionTextArea();
            missionInventoryArea = createMissionTextArea();
            missionLoansArea = createMissionTextArea();
            missionLogArea = createMissionTextArea();

            tabs.add("Overview", new JScrollPane(missionOverviewArea));
            tabs.add("Economy", new JScrollPane(missionEconomyArea));
            tabs.add("Operations", new JScrollPane(missionOperationsArea));
            tabs.add("Staff", new JScrollPane(missionStaffArea));
            tabs.add("Risk & Security", new JScrollPane(missionRiskArea));
            tabs.add("Reputation & Identity", new JScrollPane(missionReputationArea));
            tabs.add("Rumors", new JScrollPane(missionRumorsArea));
            tabs.add("Traffic & Punters", new JScrollPane(missionTrafficArea));
            tabs.add("Inventory", new JScrollPane(missionInventoryArea));
            tabs.add("Loans", new JScrollPane(missionLoansArea));
            tabs.add("Log / Events", new JScrollPane(missionLogArea));

            missionControlDialog.add(tabs, BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> missionControlDialog.setVisible(false));
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            missionControlDialog.add(bottom, BorderLayout.SOUTH);

            missionControlDialog.setSize(980, 720);
            missionControlDialog.setLocationRelativeTo(frame);
        }

        refreshMissionControl();
        missionControlDialog.setVisible(true);
    }

    private JTextArea createMissionTextArea() {
        JTextArea area = new JTextArea(16, 40);
        area.setEditable(false);
        area.setFont(UIManager.getFont("TextArea.font"));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private void restoreReportsDialogBounds() {
        int w = prefs.getInt("reportsDialog.w", 940);
        int h = prefs.getInt("reportsDialog.h", 620);
        int x = prefs.getInt("reportsDialog.x", Integer.MIN_VALUE);
        int y = prefs.getInt("reportsDialog.y", Integer.MIN_VALUE);
        reportsDialog.setSize(w, h);
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
            reportsDialog.setLocationRelativeTo(frame);
        } else {
            reportsDialog.setLocation(x, y);
        }
    }

    private void saveReportsDialogBounds() {
        if (reportsDialog == null) return;
        Rectangle bounds = reportsDialog.getBounds();
        prefs.putInt("reportsDialog.x", bounds.x);
        prefs.putInt("reportsDialog.y", bounds.y);
        prefs.putInt("reportsDialog.w", bounds.width);
        prefs.putInt("reportsDialog.h", bounds.height);
    }

    private void wireEvents() {
        openBtn.addActionListener(e -> {
            if (state.nightOpen) { log.info("Pub already open."); return; }
            sim.openNight();
            happyHourBtn.setSelected(false);
            happyHourBtn.setText("Happy Hour: OFF");
            happyHourBtn.setEnabled(true);
            nextRoundBtn.setEnabled(true);
            closeBtn.setEnabled(true);
            refreshAll();
            refreshAllMenus();
        });

        nextRoundBtn.addActionListener(e -> {
            if (!state.nightOpen) return;
            sim.playRound();
            refreshAll();

            if (!state.nightOpen) {
                nextRoundBtn.setEnabled(false);
                closeBtn.setEnabled(false);
            }

            refreshAllMenus();
        });

        closeBtn.addActionListener(e -> {
            if (!state.nightOpen) return;
            sim.closeNight("Closed early by landlord.");
            refreshAll();
            nextRoundBtn.setEnabled(false);
            closeBtn.setEnabled(false);

            refreshAllMenus();
        });

        supplierBtn.addActionListener(e -> openSupplierWindow());
        kitchenSupplierBtn.addActionListener(e -> openKitchenSupplierWindow());
        upgradesBtn.addActionListener(e -> openUpgradesWindow());
        activitiesBtn.addActionListener(e -> openActivitiesWindow());
        staffBtn.addActionListener(e -> openStaffWindow());

        securityBtn.addActionListener(e -> openSecurityWindow());
        happyHourBtn.addActionListener(e -> {
            boolean on = happyHourBtn.isSelected();
            sim.toggleHappyHour(on);
            happyHourBtn.setText(on ? "Happy Hour: ON" : "Happy Hour: OFF");
            refreshAll();
        });

        payDebtBtn.addActionListener(e -> openPayDebtDialog());
        loanSharkBtn.addActionListener(e -> openLoanDialog());

        autoBtn.addActionListener(e -> toggleAuto());
    }

    private void refreshAllMenus() {
        refreshSupplierButtons();
        refreshKitchenSupplierButtons();
        refreshStaffButtons();
        refreshSecurityButtons();
        refreshLoanDialog();
        refreshUpgradesButtons();
        refreshActivitiesButtons();
    }

    private void toggleAuto() {
        if (autoBtn.isSelected()) {
            autoBtn.setText("Auto: ON");
            autoTimer = new Timer(850, e -> {
                if (state.nightOpen) {
                    sim.playRound();
                    refreshAll();
                    refreshAllMenus();

                    if (state.rack.count() == 0) {
                        stopAutoTimer("Auto stopped: inventory empty.");
                        return;
                    }

                    if (!state.nightOpen) {
                        stopAutoTimer(null);
                    }
                }
            });
            autoTimer.start();
        } else {
            stopAutoTimer(null);
        }
    }

    private void stopAutoTimer(String reason) {
        autoBtn.setText("Auto: OFF");
        autoBtn.setSelected(false);
        if (autoTimer != null) autoTimer.stop();
        if (reason != null && !reason.isBlank()) log.neg(reason);
    }

    // -----------------------
    // Supplier Window
    // -----------------------

    private void openSupplierWindow() {
        if (state.nightOpen) {
            log.neg("No restocking while pub is OPEN.");
            return;
        }

        if (supplierDialog == null) {
            supplierDialog = new JDialog(frame, "Supplier (Bulk Buy)", false);
            supplierDialog.setLayout(new BorderLayout(10, 10));

            supplierDealLabel = new JLabel("Supplier deal: (loading...)");
            supplierDealLabel.setBorder(new EmptyBorder(8, 10, 0, 10));
            supplierDialog.add(supplierDealLabel, BorderLayout.NORTH);

            supplierListPanel = new JPanel();
            supplierListPanel.setLayout(new BoxLayout(supplierListPanel, BoxLayout.Y_AXIS));

            int[] qs = new int[]{1, 5, 10, 25, 50};
            for (Wine w : state.supplier) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

                JLabel lbl = new JLabel();
                lbl.putClientProperty("wine", w);
                row.add(lbl);

                for (int q : qs) {
                    JButton qb = new JButton();
                    qb.putClientProperty("wine", w);
                    qb.putClientProperty("qty", q);
                    qb.addActionListener(e -> {
                        sim.buyFromSupplier(w, q);
                        refreshAll();
                        refreshAllMenus();
                    });
                    row.add(qb);
                }

                supplierListPanel.add(row);
                supplierListPanel.add(Box.createVerticalStrut(6));
            }

            supplierDialog.add(new JScrollPane(supplierListPanel), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> supplierDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            supplierDialog.add(bottom, BorderLayout.SOUTH);

            supplierDialog.setSize(780, 500);
            supplierDialog.setLocationRelativeTo(frame);
        }

        refreshSupplierButtons();
        supplierDialog.setVisible(true);
    }

    private void refreshSupplierButtons() {
        if (supplierDialog == null || supplierListPanel == null || supplierDealLabel == null) return;

        String dealText = (state.supplierDeal == null) ? "No supplier deal today." : state.supplierDeal.getLabel();
        supplierDealLabel.setText("Supplier deal (locked until next night ends): " + dealText);

        boolean emergencyAllowed = state.canEmergencyRestock();
        boolean canBuy = !state.nightOpen || emergencyAllowed;
        int freeSlots = state.rack.getCapacity() - state.rack.count();

        for (Component rowC : supplierListPanel.getComponents()) {
            if (!(rowC instanceof JPanel row)) continue;

            // 1) label
            for (Component c : row.getComponents()) {
                if (c instanceof JLabel lbl) {
                    Wine w = (Wine) lbl.getClientProperty("wine");
                    if (w == null) continue;

                    boolean dealApplied = (state.supplierDeal != null && state.supplierDeal.appliesTo(w));
                    String tag = dealApplied ? "   DEAL" : "";
                    double per = sim.peekSupplierCost(w, 1);

                    lbl.setText(w.getName()
                            + " | 1x " + money2(per)
                            + " | base sell " + money2(w.getBasePrice())
                            + tag);
                }
            }

            // 2) qty buttons
            for (Component c : row.getComponents()) {
                if (!(c instanceof JButton b)) continue;
                Wine w = (Wine) b.getClientProperty("wine");
                Integer qObj = (Integer) b.getClientProperty("qty");
                if (w == null || qObj == null) continue;

                int q = qObj;
                double total = sim.peekSupplierCost(w, q);
                b.setText("x" + q + " " + money0(total));

                boolean okSlots = freeSlots >= q;
                b.setEnabled(canBuy && okSlots);
            }
        }
    }

    // -----------------------
    // Kitchen Supplier Window
    // -----------------------

    private void openKitchenSupplierWindow() {
        if (!state.kitchenUnlocked) {
            log.neg("Kitchen not unlocked.");
            return;
        }
        if (state.nightOpen && state.staffCountOfType(Staff.Type.HEAD_CHEF) < 1) {
            log.neg("Emergency kitchen orders require a Head Chef on staff.");
            return;
        }

        if (kitchenSupplierDialog == null) {
            kitchenSupplierDialog = new JDialog(frame, "Food Supplier (Bulk Buy)", false);
            kitchenSupplierDialog.setLayout(new BorderLayout(10, 10));

            JLabel top = new JLabel("Kitchen supplier deals: bulk discounts only.");
            top.setBorder(new EmptyBorder(8, 10, 0, 10));
            kitchenSupplierDialog.add(top, BorderLayout.NORTH);

            kitchenSupplierListPanel = new JPanel();
            kitchenSupplierListPanel.setLayout(new BoxLayout(kitchenSupplierListPanel, BoxLayout.Y_AXIS));

            int[] qs = new int[]{1, 5, 10, 25};
            for (Food f : state.foodSupplier) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

                JLabel lbl = new JLabel();
                lbl.putClientProperty("food", f);
                row.add(lbl);

                for (int q : qs) {
                    JButton qb = new JButton();
                    qb.putClientProperty("food", f);
                    qb.putClientProperty("qty", q);
                    qb.addActionListener(e -> {
                        sim.buyFoodFromSupplier(f, q);
                        refreshAll();
                        refreshAllMenus();
                    });
                    row.add(qb);
                }

                kitchenSupplierListPanel.add(row);
                kitchenSupplierListPanel.add(Box.createVerticalStrut(6));
            }

            kitchenSupplierDialog.add(new JScrollPane(kitchenSupplierListPanel), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> kitchenSupplierDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            kitchenSupplierDialog.add(bottom, BorderLayout.SOUTH);

            kitchenSupplierDialog.setSize(760, 460);
            kitchenSupplierDialog.setLocationRelativeTo(frame);
        }

        refreshKitchenSupplierButtons();
        kitchenSupplierDialog.setVisible(true);
    }

    private void refreshKitchenSupplierButtons() {
        if (kitchenSupplierDialog == null || kitchenSupplierListPanel == null) return;

        int freeSlots = state.foodRack.getCapacity() - state.foodRack.count();

        for (Component rowC : kitchenSupplierListPanel.getComponents()) {
            if (!(rowC instanceof JPanel row)) continue;

            Food rowFood = null;

            for (Component c : row.getComponents()) {
                if (c instanceof JLabel lbl) {
                    Food f = (Food) lbl.getClientProperty("food");
                    if (f == null) continue;
                    rowFood = f;
                    lbl.setText(f.getName()
                            + " | 1x " + money2(f.getBaseCost())
                            + " | sell " + money2(f.getBasePrice())
                            + " | tier " + f.getQualityTier());
                }
            }

            for (Component c : row.getComponents()) {
                if (!(c instanceof JButton b)) continue;
                Food f = (Food) b.getClientProperty("food");
                Integer qObj = (Integer) b.getClientProperty("qty");
                if (f == null || qObj == null) continue;

                int q = qObj;
                double total = sim.peekFoodCost(f, q);
                b.setText("x" + q + " " + money0(total));

                boolean okSlots = freeSlots >= q;
                boolean emergencyAllowed = state.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1;
                b.setEnabled((!state.nightOpen || emergencyAllowed) && okSlots);
            }
        }
    }

    // -----------------------
    // Staff Window (Hire + Fire)
    // -----------------------

    private void openStaffWindow() {
        if (staffDialog == null) {
            staffDialog = new JDialog(frame, "Staff (Hire & Fire)", false);
            staffDialog.setLayout(new BorderLayout(10, 10));

            JLabel top = new JLabel("Hire types (click). Fire pays accrued wages immediately.");
            top.setBorder(new EmptyBorder(8, 10, 0, 10));
            staffDialog.add(top, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
            center.setBorder(new EmptyBorder(6, 10, 10, 10));

            // LEFT: hire options
            staffHirePanel = new JPanel();
            staffHirePanel.setLayout(new BoxLayout(staffHirePanel, BoxLayout.Y_AXIS));
            staffHirePanel.setBorder(BorderFactory.createTitledBorder("Hire"));

            addStaffHireButton(Staff.Type.TRAINEE);
            addStaffHireButton(Staff.Type.EXPERIENCED);
            addStaffHireButton(Staff.Type.SPEED);
            addStaffHireButton(Staff.Type.CHARISMA);
            addStaffHireButton(Staff.Type.SECURITY);
            addStaffHireButton(Staff.Type.KITCHEN_PORTER);
            addStaffHireButton(Staff.Type.KITCHEN_ASSISTANT);
            addStaffHireButton(Staff.Type.CHEF_DE_PARTIE);
            addStaffHireButton(Staff.Type.SOUS_CHEF);
            addStaffHireButton(Staff.Type.HEAD_CHEF);
            addStaffHireButton(Staff.Type.ASSISTANT_MANAGER);
            addStaffHireButton(Staff.Type.MANAGER);

            // RIGHT: roster with fire buttons
            staffRosterPanel = new JPanel();
            staffRosterPanel.setLayout(new BoxLayout(staffRosterPanel, BoxLayout.Y_AXIS));
            staffRosterPanel.setBorder(BorderFactory.createTitledBorder("Roster"));

            center.add(new JScrollPane(staffHirePanel));
            center.add(new JScrollPane(staffRosterPanel));

            staffDialog.add(center, BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> staffDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            staffDialog.add(bottom, BorderLayout.SOUTH);

            staffDialog.setSize(980, 560);
            staffDialog.setLocationRelativeTo(frame);
        }

        refreshStaffButtons();
        staffDialog.setVisible(true);
    }

    private void addStaffHireButton(Staff.Type type) {
        JButton b = new JButton(Staff.rangeLabel(type));
        b.putClientProperty("staffType", type);
        b.setHorizontalAlignment(SwingConstants.LEFT);

        b.addActionListener(e -> {
            sim.hireStaff(type);
            refreshAll();
            refreshAllMenus();
        });

        staffHirePanel.add(b);
        staffHirePanel.add(Box.createVerticalStrut(6));
    }

    private void refreshStaffButtons() {
        if (staffDialog == null || staffHirePanel == null || staffRosterPanel == null) return;

        // Enable/disable hire options
        for (Component c : staffHirePanel.getComponents()) {
            if (!(c instanceof JButton b)) continue;

            Object tObj = b.getClientProperty("staffType");
            if (!(tObj instanceof Staff.Type t)) continue;

            boolean enabled;

            // General manager pool
            if (t == Staff.Type.MANAGER) {
                enabled = state.generalManagers.size() < state.managerCap;

                // Kitchen roles (BOH)
            } else if (t == Staff.Type.HEAD_CHEF
                    || t == Staff.Type.SOUS_CHEF
                    || t == Staff.Type.CHEF_DE_PARTIE
                    || t == Staff.Type.KITCHEN_ASSISTANT
                    || t == Staff.Type.KITCHEN_PORTER
                    || t == Staff.Type.CHEF) {
                enabled = state.kitchenUnlocked && (state.bohStaff.size() < state.kitchenChefCap);
                if (t == Staff.Type.HEAD_CHEF && state.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1) {
                    enabled = false;
                }

                // FOH roles (includes assistant manager + bar staff)
            } else {
                enabled = state.fohStaff.size() < state.fohStaffCap;
            }

            // hires only between nights (except bouncer)
            if (state.nightOpen) enabled = false;

            b.setEnabled(enabled);
        }

        // Rebuild roster (fire buttons)
        staffRosterPanel.removeAll();

        // General manager rows
        if (state.generalManagers.isEmpty()) {
            staffRosterPanel.add(new JLabel("General Managers: (none)"));
            staffRosterPanel.add(Box.createVerticalStrut(8));
        } else {
            staffRosterPanel.add(new JLabel("General Managers (" + state.generalManagers.size() + "/" + state.managerCap + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int i = 0; i < state.generalManagers.size(); i++) {
                final int idx = i;
                Staff st = state.generalManagers.get(i);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireManagerAt(idx);
                    refreshAll();
                    refreshAllMenus();
                }));
                staffRosterPanel.add(Box.createVerticalStrut(6));
            }
        }

        if (state.fohStaff.isEmpty()) {
            staffRosterPanel.add(new JLabel("FOH Staff: (none)"));
        } else {
            staffRosterPanel.add(new JLabel("FOH Staff (" + state.fohStaff.size() + "/" + state.fohStaffCap + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int i = 0; i < state.fohStaff.size(); i++) {
                final int idx = i;
                Staff st = state.fohStaff.get(i);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireStaffAt(idx);
                    refreshAll();
                    refreshAllMenus();
                }));
                staffRosterPanel.add(Box.createVerticalStrut(6));
            }
        }

        if (state.bohStaff.isEmpty()) {
            staffRosterPanel.add(new JLabel("BOH Staff: (none)"));
        } else {
            staffRosterPanel.add(new JLabel("BOH Staff (" + state.bohStaff.size() + "/" + state.kitchenChefCap + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int i = 0; i < state.bohStaff.size(); i++) {
                final int idx = i;
                Staff st = state.bohStaff.get(i);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireBohStaffAt(idx);
                    refreshAll();
                    refreshAllMenus();
                }));
                staffRosterPanel.add(Box.createVerticalStrut(6));
            }
        }

        staffRosterPanel.revalidate();
        staffRosterPanel.repaint();
    }

    // -----------------------
    // Security Window
    // -----------------------

    private void openSecurityWindow() {
        if (securityDialog == null) {
            securityDialog = new JDialog(frame, "Security", false);
            securityDialog.setLayout(new BorderLayout(10, 10));

            JLabel top = new JLabel("Manage base security and bouncers.");
            top.setBorder(new EmptyBorder(8, 10, 0, 10));
            securityDialog.add(top, BorderLayout.NORTH);

            JPanel securityListPanel = new JPanel();
            securityListPanel.setLayout(new BoxLayout(securityListPanel, BoxLayout.Y_AXIS));

            securityUpgradeBtn = new JButton();
            securityUpgradeBtn.addActionListener(e -> {
                if (state.nightOpen) {
                    log.neg("Security upgrades are installed between nights.");
                    return;
                }
                sim.upgradeSecurity();
                refreshAll();
                refreshAllMenus();
            });

            bouncerBtn = new JButton();
            bouncerBtn.addActionListener(e -> {
                if (!state.nightOpen) {
                    log.neg("Bouncers can only be hired while the pub is OPEN.");
                    return;
                }
                sim.hireBouncerTonight();
                refreshAll();
                refreshAllMenus();
            });

            securityListPanel.add(securityUpgradeBtn);
            securityListPanel.add(Box.createVerticalStrut(8));
            securityListPanel.add(bouncerBtn);

            securityDialog.add(securityListPanel, BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> securityDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            securityDialog.add(bottom, BorderLayout.SOUTH);

            securityDialog.setSize(420, 220);
            securityDialog.setLocationRelativeTo(frame);
        }

        refreshSecurityButtons();
        securityDialog.setVisible(true);
    }

    private void refreshSecurityButtons() {
        if (securityDialog == null || securityUpgradeBtn == null || bouncerBtn == null) return;
        securityUpgradeBtn.setText("Base Security +1 (level " + state.baseSecurityLevel
                + ", cost " + money0(sim.peekSecurityUpgradeCost()) + ")");
        securityUpgradeBtn.setEnabled(!state.nightOpen);

        bouncerBtn.setText("Hire Bouncer Tonight " + state.bouncersHiredTonight + "/" + state.bouncerCap);
        bouncerBtn.setEnabled(state.nightOpen && state.bouncersHiredTonight < state.bouncerCap);
    }


    private JPanel makeStaffRow(String text, Runnable onFire) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel lbl = new JLabel(text);

        JButton fire = new JButton("Fire");
        fire.addActionListener(e -> onFire.run());

        // firing between nights only (keeps it sane)
        fire.setEnabled(!state.nightOpen);

        row.add(lbl, BorderLayout.CENTER);
        row.add(fire, BorderLayout.EAST);
        return row;
    }

    // -----------------------
    // Upgrades Window (supplier-style)
    // -----------------------

    private void openUpgradesWindow() {
        if (upgradesDialog == null) {
            upgradesDialog = new JDialog(frame, "Upgrades (One-time)", false);
            upgradesDialog.setLayout(new BorderLayout(10, 10));

            JLabel top = new JLabel("One-time purchases. Installed between nights.");
            top.setBorder(new EmptyBorder(8, 10, 0, 10));
            upgradesDialog.add(top, BorderLayout.NORTH);

            upgradesListPanel = new JPanel();
            upgradesListPanel.setLayout(new BoxLayout(upgradesListPanel, BoxLayout.Y_AXIS));

            for (PubUpgrade up : PubUpgrade.values()) {
                JButton b = new JButton();
                b.putClientProperty("upgrade", up);
                b.setHorizontalAlignment(SwingConstants.LEFT);
                b.addActionListener(e -> {
                    sim.buyUpgrade(up);
                    refreshAll();
                    refreshAllMenus();
                });

                upgradesListPanel.add(b);
                upgradesListPanel.add(Box.createVerticalStrut(6));
            }

            upgradesDialog.add(new JScrollPane(upgradesListPanel), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> upgradesDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            upgradesDialog.add(bottom, BorderLayout.SOUTH);

            upgradesDialog.setSize(780, 520);
            upgradesDialog.setLocationRelativeTo(frame);
        }

        refreshUpgradesButtons();
        upgradesDialog.setVisible(true);
    }

    private void refreshUpgradesButtons() {
        if (upgradesDialog == null || upgradesListPanel == null) return;

        Color defaultBg = UIManager.getColor("Button.background");
        Color defaultFg = UIManager.getColor("Button.foreground");

        for (Component c : upgradesListPanel.getComponents()) {
            if (!(c instanceof JButton b)) continue;

            PubUpgrade up = (PubUpgrade) b.getClientProperty("upgrade");
            if (up == null) continue;

            boolean owned = state.ownedUpgrades.contains(up);
            boolean unlocked = sim.canBuyUpgrade(up);
            PendingUpgradeInstall installing = findUpgradeInstall(up);
            String prefix;
            if (owned) {
                prefix = " OWNED  ";
            } else if (installing != null) {
                prefix = " INSTALLING (" + installing.nightsRemaining() + " nights)  ";
            } else {
                prefix = unlocked ? "" : " LOCKED  ";
            }
            b.setText(prefix + up.toString());
            boolean enabled = !state.nightOpen && !owned && unlocked && installing == null;
            b.setEnabled(enabled);
            if (installing != null) {
                b.setOpaque(true);
                b.setBackground(INSTALLING_BG);
                b.setForeground(defaultFg);
            } else {
                b.setOpaque(true);
                b.setBackground(defaultBg);
                b.setForeground(defaultFg);
            }
        }
    }

    private PendingUpgradeInstall findUpgradeInstall(PubUpgrade up) {
        for (PendingUpgradeInstall install : state.pendingUpgradeInstalls) {
            if (install.upgrade() == up) return install;
        }
        return null;
    }

    // -----------------------
    // Activities Window
    // -----------------------

    private void openActivitiesWindow() {
        if (activitiesDialog == null) {
            activitiesDialog = new JDialog(frame, "Activities (Scheduled)", false);
            activitiesDialog.setLayout(new BorderLayout(10, 10));

            JLabel top = new JLabel("Schedule one activity between nights (starts in 1-3 days).");
            top.setBorder(new EmptyBorder(8, 10, 0, 10));
            activitiesDialog.add(top, BorderLayout.NORTH);

            activitiesListPanel = new JPanel();
            activitiesListPanel.setLayout(new BoxLayout(activitiesListPanel, BoxLayout.Y_AXIS));

            for (PubActivity a : PubActivity.values()) {
                JButton b = new JButton();
                b.putClientProperty("activity", a);
                b.setHorizontalAlignment(SwingConstants.LEFT);
                b.addActionListener(e -> {
                    sim.startActivity(a);
                    refreshAll();
                    refreshAllMenus();
                });

                activitiesListPanel.add(b);
                activitiesListPanel.add(Box.createVerticalStrut(6));
            }

            activitiesDialog.add(new JScrollPane(activitiesListPanel), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> activitiesDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            activitiesDialog.add(bottom, BorderLayout.SOUTH);

            activitiesDialog.setSize(780, 520);
            activitiesDialog.setLocationRelativeTo(frame);
        }

        refreshActivitiesButtons();
        activitiesDialog.setVisible(true);
    }

    private void refreshActivitiesButtons() {
        if (activitiesDialog == null || activitiesListPanel == null) return;

        for (Component c : activitiesListPanel.getComponents()) {
            if (!(c instanceof JButton b)) continue;

            PubActivity a = (PubActivity) b.getClientProperty("activity");
            if (a == null) continue;

            boolean unlocked = state.unlockedActivities.contains(a);
            boolean enabled = unlocked && !state.nightOpen && state.scheduledActivity == null;
            b.setEnabled(enabled);

            String txt = a.toString();
            if (!unlocked) txt = " LOCKED  " + txt;
            if (state.activityTonight == a) txt = " RUNNING  " + txt;
            if (state.scheduledActivity != null) {
                int daysLeft = Math.max(0, state.scheduledActivity.startAbsDayIndex() - state.absDayIndex());
                txt = " SCHEDULED (" + daysLeft + "d)  " + state.scheduledActivity.activity().toString();
                b.setEnabled(false);
            }
            b.setText(txt);
        }
    }

    // -----------------------
    // Pay Debt Dialog
    // -----------------------

    private void openPayDebtDialog() {
        if (state.debt <= 0) {
            log.info("No debt to pay.");
            return;
        }

        Object[] options = {"25", "50", "100", "250", "500", "Max"};
        int pick = JOptionPane.showOptionDialog(
                frame,
                "Debt: " + String.format("%.2f", state.debt) + "\nCash: " + String.format("%.2f", state.cash) + "\n\nHow much to pay?",
                "Pay Debt",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]
        );

        if (pick < 0) return;

        double amt = switch (pick) {
            case 0 -> 25;
            case 1 -> 50;
            case 2 -> 100;
            case 3 -> 250;
            case 4 -> 500;
            default -> Double.POSITIVE_INFINITY;
        };

        if (Double.isInfinite(amt)) amt = Math.min(state.cash, state.debt);
        sim.payDebt(amt);

        refreshAll();
        refreshAllMenus();
    }

    // -----------------------
    // Loan Shark Dialog
    // -----------------------

    private void openLoanDialog() {
        if (loanDialog == null) {
            loanDialog = new JDialog(frame, "Loan Shark", false);
            loanDialog.setLayout(new BorderLayout(10, 10));

            loanTextArea = new JTextArea(12, 44);
            loanTextArea.setEditable(false);
            loanTextArea.setFont(UIManager.getFont("TextArea.font"));

            loanDialog.add(new JScrollPane(loanTextArea), BorderLayout.CENTER);

            loanButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JButton b100 = new JButton("Borrow 100");
            JButton b250 = new JButton("Borrow 250");
            JButton b500 = new JButton("Borrow 500");
            JButton b1000 = new JButton("Borrow 1000");
            JButton repayFull = new JButton("Repay In Full");

            b100.addActionListener(e -> { sim.borrowFromLoanShark(100); refreshAll(); refreshAllMenus(); });
            b250.addActionListener(e -> { sim.borrowFromLoanShark(250); refreshAll(); refreshAllMenus(); });
            b500.addActionListener(e -> { sim.borrowFromLoanShark(500); refreshAll(); refreshAllMenus(); });
            b1000.addActionListener(e -> { sim.borrowFromLoanShark(1000); refreshAll(); refreshAllMenus(); });
            repayFull.addActionListener(e -> { sim.repayLoanSharkInFull(); refreshAll(); refreshAllMenus(); });

            loanButtonsPanel.add(b100);
            loanButtonsPanel.add(b250);
            loanButtonsPanel.add(b500);
            loanButtonsPanel.add(b1000);
            loanButtonsPanel.add(repayFull);

            loanDialog.add(loanButtonsPanel, BorderLayout.SOUTH);

            loanDialog.setSize(700, 390);
            loanDialog.setLocationRelativeTo(frame);
        }

        refreshLoanDialog();
        loanDialog.setVisible(true);
    }

    private void refreshLoanDialog() {
        if (loanDialog == null || loanTextArea == null || loanButtonsPanel == null) return;

        loanTextArea.setText(
                state.loanShark.buildLoanText(
                        state.absWeekIndex(),
                        state.reportIndex,
                        state.weeksIntoReport,
                        state.reputation
                )
        );

        boolean active = state.loanShark.hasActiveLoan();

        for (Component c : loanButtonsPanel.getComponents()) {
            if (!(c instanceof JButton b)) continue;

            String txt = b.getText().toLowerCase();
            if (txt.startsWith("borrow")) {
                b.setEnabled(!active);
            } else if (txt.contains("repay")) {
                b.setEnabled(active);
            }
        }
    }

    private void refreshReportsDialog() {
        if (reportsDialog == null) return;
        if (reportsDialogArea != null) {
            reportsDialogArea.setText(ReportSystem.buildReportText(state));
        }
        if (reportsDialogLoansArea != null) {
            reportsDialogLoansArea.setText(
                    state.loanShark.buildLoanText(
                            state.absWeekIndex(),
                            state.reportIndex,
                            state.weeksIntoReport,
                            state.reputation
                    )
            );
        }
    }

    private void refreshMissionControl() {
        if (missionControlDialog == null) return;
        MetricsSnapshot snapshot = lastSnapshot != null ? lastSnapshot : sim.buildMetricsSnapshot();
        if (snapshot == null) return;
        if (missionOverviewArea != null) {
            missionOverviewArea.setText(String.join("\n", snapshot.overviewLines));
        }
        if (missionEconomyArea != null) missionEconomyArea.setText(snapshot.economy);
        if (missionOperationsArea != null) missionOperationsArea.setText(snapshot.operations);
        if (missionStaffArea != null) missionStaffArea.setText(snapshot.staff);
        if (missionRiskArea != null) missionRiskArea.setText(snapshot.risk);
        if (missionReputationArea != null) missionReputationArea.setText(snapshot.reputationIdentity);
        if (missionRumorsArea != null) missionRumorsArea.setText(snapshot.rumors);
        if (missionTrafficArea != null) missionTrafficArea.setText(snapshot.trafficPunters);
        if (missionInventoryArea != null) missionInventoryArea.setText(snapshot.inventory);
        if (missionLoansArea != null) missionLoansArea.setText(snapshot.loans);
        if (missionLogArea != null && (missionLogArea.getText() == null || missionLogArea.getText().isBlank())) {
            missionLogArea.setText(snapshot.logEvents + "\n");
        }
    }

    private void openEventFeedDialog() {
        if (eventFeedDialog == null) {
            eventFeedDialog = new EventFeedDialog(frame);
            log.setEventSink(this::appendEventToFeeds);
        }
        eventFeedDialog.showDialog();
    }

    private void appendEventToFeeds(String event) {
        if (eventFeedDialog != null) {
            eventFeedDialog.appendEvent(event);
        }
        if (missionLogArea != null) {
            missionLogArea.append(event + "\n");
            missionLogArea.setCaretPosition(missionLogArea.getDocument().getLength());
        }
    }

    private void checkReportPopups() {
        if (state.weeklyReportReady) {
            showWeeklyReportDialog();
            state.weeklyReportReady = false;
            return;
        }
        if (state.fourWeekReportReady) {
            showFourWeekReportDialog();
            state.fourWeekReportReady = false;
            return;
        }
        if (!state.milestonePopups.isEmpty()) {
            String msg = state.milestonePopups.removeFirst();
            UIPopup.showPopup(frame, UIPopup.PopupStyle.POSITIVE, "Milestone Achieved",
                    msg.replace("\n", "<br/>"), "");
        }
    }

    private void showWeeklyReportDialog() {
        if (weeklyReportDialog == null) {
            weeklyReportDialog = new JDialog(frame, "Weekly Report", false);
            weeklyReportDialog.setLayout(new BorderLayout(8, 8));

            weeklyReportArea = new JTextArea(24, 60);
            weeklyReportArea.setEditable(false);
            weeklyReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            weeklyReportDialog.add(new JScrollPane(weeklyReportArea), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> weeklyReportDialog.setVisible(false));
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            weeklyReportDialog.add(bottom, BorderLayout.SOUTH);

            weeklyReportDialog.setSize(780, 560);
            weeklyReportDialog.setLocationRelativeTo(frame);
        }

        weeklyReportArea.setText(state.weeklyReportText);
        weeklyReportDialog.setVisible(true);
    }

    private void showFourWeekReportDialog() {
        if (fourWeekReportDialog == null) {
            fourWeekReportDialog = new JDialog(frame, "4-Week Summary", false);
            fourWeekReportDialog.setLayout(new BorderLayout(8, 8));

            fourWeekReportArea = new JTextArea(24, 60);
            fourWeekReportArea.setEditable(false);
            fourWeekReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            fourWeekReportDialog.add(new JScrollPane(fourWeekReportArea), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> fourWeekReportDialog.setVisible(false));
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            fourWeekReportDialog.add(bottom, BorderLayout.SOUTH);

            fourWeekReportDialog.setSize(780, 560);
            fourWeekReportDialog.setLocationRelativeTo(frame);
        }

        fourWeekReportArea.setText(state.fourWeekReportText);
        fourWeekReportDialog.setVisible(true);
    }

    // -----------------------
    // Refresh UI
    // -----------------------

    private void refreshAll() {
        lastSnapshot = sim.buildMetricsSnapshot();
        updateHud(lastSnapshot);
        updateInventory();
        updateReportsPanel(lastSnapshot);
        refreshMissionControl();
        updateMoodLighting();
        checkReportPopups();
    }

    private void updateHud(MetricsSnapshot snapshot) {
        if (snapshot == null) return;
        boolean cashIncreased = state.cash > lastCash;
        boolean debtIncreased = state.debt > lastDebt;

        cashLabel.setText("Cash: " + money2(state.cash));
        debtLabel.setText("Debt: " + money2(state.debt));
        pubNameLabel.setText(" " + state.pubName + " (Lv " + state.pubLevel + ")");
        invoiceDueLabel.setText("Invoice Due: " + money2(sim.invoiceDueNow()));

        String mood =
                (state.reputation >= 60) ? " Loved" :
                        (state.reputation >= 20) ? " Solid" :
                                (state.reputation >= -20) ? " Shaky" :
                                        (state.reputation >= -60) ? " Bad" :
                                                " Toxic";

        repLabel.setText("Reputation: " + state.reputation + " (" + mood + ")");
        calendarLabel.setText("Week " + state.weekCount + "  " + state.dayName() + " | Night " + state.nightCount);

        int cap = sim.peekServeCapacity();

        roundLabel.setText(state.nightOpen
                ? ("Night OPEN  Round " + state.roundInNight + "/" + state.closingRound
                + " | Bar " + state.nightPunters.size() + "/" + state.maxBarOccupancy)
                : ("Night CLOSED  Ready"));

        int sec = state.baseSecurityLevel
                + (state.bouncersHiredTonight * 2)
                + (state.hasSkilledManager() ? 1 : 0)
                + state.staffSecurityBonus();
        sec = Math.max(0, sec);
        securityLabel.setText("Security: " + sec);

        staffLabel.setText("Staff: " + state.staff().summaryLine() + " | Serve cap " + cap);
        reportLabel.setText("Report: " + state.reports().summaryLine());
        String forecastLine = state.trafficForecastLine != null ? state.trafficForecastLine : "Forecast: 0–0 tonight";

        // OBS box = traffic only (2 lines max)
        observationLabel.setText("<html>🚶 In: " + state.lastTrafficIn + " | Out: " + state.lastTrafficOut
                + "<br>📈 " + forecastLine + "</html>");

        // Middle grey box = quips only (no serve cap here)
        String quipLine = (state.observationLine != null && !state.observationLine.isBlank())
                ? state.observationLine
                : "";
        serveCapLabel.setText(quipLine.isEmpty() ? " " : "<html>" + quipLine + "</html>");


        payDebtBtn.setEnabled(state.debt > 0 && state.cash > 0);
        boolean emergencySupplierAllowed = state.canEmergencyRestock();
        supplierBtn.setEnabled(!state.nightOpen || emergencySupplierAllowed);
        boolean emergencyFoodAllowed = state.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1;
        kitchenSupplierBtn.setEnabled(state.kitchenUnlocked && (!state.nightOpen || emergencyFoodAllowed));
        happyHourBtn.setEnabled(state.nightOpen);

        lastCash = state.cash;
        lastDebt = state.debt;

        if (cashIncreased) {
            flashBadge(cashBadge, FLASH_GREEN, CASH_BG, true);
        }
        if (debtIncreased) {
            flashBadge(debtBadge, FLASH_RED, DEBT_BG, false);
        }

        updateNightPulse();

    }

    private void updateNightPulse() {
        if (nightIndicator == null) return;
        if (state.nightOpen) {
            nightIndicator.setVisible(true);
            if (nightPulseTimer == null) {
                nightPulseTimer = new Timer(520, e -> {
                    nightPulseOn = !nightPulseOn;
                    nightIndicator.setForeground(nightPulseOn ? new Color(255, 220, 140) : new Color(160, 160, 160));
                });
            }
            if (!nightPulseTimer.isRunning()) {
                nightPulseTimer.start();
            }
        } else {
            if (nightPulseTimer != null) {
                nightPulseTimer.stop();
            }
            nightIndicator.setVisible(false);
            nightPulseOn = false;
        }
    }

    private void flashBadge(JPanel badge, Color flash, Color base, boolean cashFlash) {
        if (badge == null) return;
        badge.setBackground(flash);
        Timer timer = new Timer(450, e -> badge.setBackground(base));
        timer.setRepeats(false);
        if (cashFlash) {
            if (cashFlashTimer != null) cashFlashTimer.stop();
            cashFlashTimer = timer;
        } else {
            if (debtFlashTimer != null) debtFlashTimer.stop();
            debtFlashTimer = timer;
        }
        timer.start();
    }

    private void updateInventory() {
        invModel.clear();

        Map<String, Integer> counts = state.rack.inventoryCounts();
        invModel.addElement("=== Wine ===");
        if (counts.isEmpty()) {
            invModel.addElement("(empty)");
        } else {
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                invModel.addElement(e.getKey() + " x" + e.getValue());
            }
        }
        List<WineRack.SpoilageLine> spoilage = state.rack.spoilageForecast(state.absDayIndex());
        if (!spoilage.isEmpty()) {
            invModel.addElement(" ");
            invModel.addElement("=== Spoilage forecast ===");
            for (WineRack.SpoilageLine line : spoilage) {
                invModel.addElement(line.wineName() + " - " + line.count() + " bottle(s) spoil " + formatSpoilageDays(line.daysRemaining()));
            }
        }
        invModel.addElement("Total: " + state.rack.count() + "/" + state.rack.getCapacity());

        if (state.kitchenUnlocked) {
            invModel.addElement(" ");
            invModel.addElement("=== Food ===");
            Map<String, Integer> foodCounts = state.foodRack.inventoryCounts();
            if (foodCounts.isEmpty()) {
                invModel.addElement("(empty)");
            } else {
                for (Map.Entry<String, Integer> e : foodCounts.entrySet()) {
                    invModel.addElement(e.getKey() + " x" + e.getValue());
                }
            }
            invModel.addElement("Total: " + state.foodRack.count() + "/" + state.foodRack.getCapacity());
        }
    }

    private String formatSpoilageDays(int daysRemaining) {
        if (daysRemaining <= 0) return "today";
        if (daysRemaining == 1) return "tomorrow";
        return "in " + daysRemaining + " days";
    }

    private void updateReportsPanel(MetricsSnapshot snapshot) {
        if (reportArea == null) return;
        reportArea.setText(ReportSystem.buildReportText(state));
        updateReportSummary(snapshot);
        refreshReportsDialog();
    }

    private void updateReportSummary(MetricsSnapshot snapshot) {
        if (reportSummaryCash == null || snapshot == null) return;
        reportSummaryCash.setText(snapshot.hudCash);
        reportSummaryDebt.setText(snapshot.hudDebt);
        reportSummaryRep.setText(snapshot.hudRep);
        reportSummarySecurity.setText(snapshot.hudSecurity);
        reportSummaryStaff.setText("Staff: FOH " + state.fohStaff.size()
                + " | BOH " + state.bohStaff.size()
                + " | GM " + state.generalManagers.size());
        reportSummaryServe.setText(snapshot.hudServeCap);
        reportSummaryRefunds.setText("Refunds (week): " + money2(state.weekRefundTotal));
        reportSummaryInvoice.setText(snapshot.hudInvoice);
    }

    private void updateMoodLighting() {
        Color bg =
                (state.reputation >= 60) ? new Color(20, 45, 25) :
                        (state.reputation >= 20) ? new Color(25, 35, 45) :
                                (state.reputation >= -20) ? new Color(45, 45, 25) :
                                        (state.reputation >= -60) ? new Color(55, 30, 30) :
                                                new Color(45, 20, 25);

        root.setBackground(bg);
        controls.setBackground(bg);
    }
}
