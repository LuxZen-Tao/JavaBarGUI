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

@SuppressWarnings({"unused", "FieldCanBeLocal", "DuplicatedCode"})
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
    private JLabel supplierCreditLabel;
    private SupplierInvoiceControls wineInvoiceControls;
    private JDialog paydayDialog;
    private JPanel paydayListPanel;
    private JLabel paydaySummaryLabel;
    private JDialog kitchenSupplierDialog;
    private JPanel kitchenSupplierListPanel;
    private JLabel kitchenSupplierNoticeLabel;
    private JLabel kitchenSupplierCreditLabel;
    private SupplierInvoiceControls foodInvoiceControls;

    // Staff window (hire + fire)
    private JDialog staffDialog;
    private JPanel staffHirePanel;
    private JPanel staffRosterPanel;
    private JLabel kitchenLockLabel;

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
    private JDialog actionsDialog;
    private LandlordActionsPanel actionsPanel;
    private JDialog innDialog;
    private JDialog prestigeDialog;
    private JTextArea prestigeDialogArea;
    private JButton prestigeConfirmButton;
    private JLabel innRoomsLabel;
    private JLabel innBookedLabel;
    private JLabel innPriceLabel;
    private JSlider innPriceSlider;
    private JProgressBar innRepBar;
    private JProgressBar innCleanBar;
    private JLabel innReceptionLabel;
    private JLabel innHousekeepingLabel;
    private JLabel innDutyManagerLabel;
    private JLabel innMaintenanceLabel;
    private JLabel innSummaryLabel;

    // Reports panel (right side)
    private JTextArea reportArea;

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
    private final JLabel timePhaseLabel = new JLabel();
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
    private final java.util.List<PaydayBillRow> paydayRows = new java.util.ArrayList<>();


    // Buttons
    private final JButton openBtn = new JButton("Open Pub");
    private final JButton nextRoundBtn = new JButton("Next Round");
    private final JButton closeBtn = new JButton("Close Night");

    private final JButton supplierBtn = new JButton("Supplier");
    private final JButton kitchenSupplierBtn = new JButton("Food Supplier");
    private final JButton upgradesBtn = new JButton("Upgrades");
    private final JButton activitiesBtn = new JButton("Activities");
    private final JButton actionsBtn = new JButton("Actions");
    private final JButton staffBtn = new JButton("Staff");
    private final JButton innBtn = new JButton("Inn");

    private final JButton securityBtn = new JButton("Security");
    private final JButton loanSharkBtn = new JButton("Finance");
    private final JComboBox<MusicProfileType> musicProfileBox = new JComboBox<>(MusicProfileType.values());

    private final JToggleButton autoBtn = new JToggleButton("Auto: OFF");
    private Timer autoTimer;
    private Timer nightPulseTimer;
    private Timer cashFlashTimer;
    private Timer debtFlashTimer;
    private double lastCash;
    private double lastDebt;
    private boolean nightPulseOn;
    private MetricsSnapshot lastSnapshot;
    private int lastPrestigePreviewStar = -1;

    private JDialog reportsDialog;
    private JTextArea reportsDialogArea;
    private JTextArea reportsDialogLoansArea;
    private JDialog missionControlDialog;
    private JTextArea missionOverviewArea;
    private JTextArea missionEconomyArea;
    private JTextArea missionFinanceArea;
    private JTextArea missionPaydayArea;
    private JTextArea missionSuppliersArea;
    private JTextArea missionProgressionArea;
    private JTextArea missionOperationsArea;
    private JTextArea missionStaffArea;
    private JTextArea missionStaffDetailArea;
    private JTextArea missionRiskArea;
    private JTextArea missionSecurityArea;
    private JTextArea missionReputationArea;
    private JTextArea missionRumorsArea;
    private JTextArea missionTrafficArea;
    private JTextArea missionInventoryArea;
    private JTextArea missionLoansArea;
    private JTextArea missionLogArea;
    private JTextArea missionInnArea;
    private JTextArea missionPrestigeArea;
    private JTextArea missionMusicArea;
    private JButton prestigePreviewButton;
    private JRadioButton policyFriendlyBtn;
    private JRadioButton policyBalancedBtn;
    private JRadioButton policyStrictBtn;
    private JComboBox<SecurityPolicy> securityPolicyBox;
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
    private JButton marshallBtn;
    private JPanel securityTasksPanel;
    private JPanel securityTasksListPanel;
    private boolean updatingSecurityPolicyBox = false;

    public WineBarGUI(GameState state) {
        this.state = state;

        this.log = new UILogger(logPane);
        this.sim = new Simulation(state, log);
        this.state.creditLineSelector = this::selectCreditLineForPayment;

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
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

        hud.setBorder(new EmptyBorder(2, 4, 2, 4));
        hud.setOpaque(false);
        controls.setOpaque(false);
        cashBadge = createBadge(CASH_BG, cashLabel);
        debtBadge = createBadge(DEBT_BG, debtLabel);
        JPanel repBadge = createBadge(REP_BG, repLabel);
        repLabel.setVerticalAlignment(SwingConstants.TOP);
        pubNameLabel.setFont(pubNameLabel.getFont().deriveFont(Font.BOLD));
        pubNameBadge = createBadge(new Color(70, 110, 160), pubNameLabel);
        JPanel invoiceBadge = createBadge(REPORT_BG, invoiceDueLabel);
        securityLabel.setVerticalAlignment(SwingConstants.TOP);
        securityBadge = createBadge(SECURITY_BG, securityLabel);
        calendarBadge = createBadge(CAL_BG, calendarLabel);
        reportBadge = createBadge(REPORT_BG, reportLabel);
        staffLabel.setVerticalAlignment(SwingConstants.TOP);
        staffBadge = createBadge(STAFF_BG, staffLabel);
        serveBadge = createBadge(SERVE_BG, serveCapLabel);
        observationLabel.setVerticalAlignment(SwingConstants.TOP);
        observationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        Font obsFont = observationLabel.getFont();
        float obsSize = Math.max(11f, obsFont.getSize() + 1f);
        observationLabel.setFont(obsFont.deriveFont(Font.BOLD, obsSize));
        observationLabel.setPreferredSize(new Dimension(220, 54));
        observationLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        observationBadge = createBadge(OBS_BG, observationLabel);
        serveCapLabel.setVerticalAlignment(SwingConstants.TOP);
        Font quipFont = serveCapLabel.getFont();
        float quipSize = Math.max(12f, quipFont.getSize() + 1f);
        serveCapLabel.setFont(quipFont.deriveFont(quipSize));
        serveCapLabel.setPreferredSize(new Dimension(240, 44));
        serveCapLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
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
        priceSlider.setPreferredSize(new Dimension(110, 24));
        priceSlider.addChangeListener(e -> {
            double m = priceSlider.getValue() / 100.0;
            sim.setPriceMultiplier(m);
            priceLabel.setText("Price x" + String.format("%.2f", state.priceMultiplier));
            refreshAll();
        });

        JPanel nightControls = createControlGroup("Night", openBtn, nextRoundBtn, closeBtn, happyHourBtn);
        musicProfileBox.setToolTipText(sim.currentMusicTooltip());
        musicProfileBox.addActionListener(e -> {
            Object selected = musicProfileBox.getSelectedItem();
            if (selected instanceof MusicProfileType profile) {
                sim.setMusicProfile(profile);
                musicProfileBox.setToolTipText(sim.currentMusicTooltip());
                refreshAll();
            }
        });

        JPanel economyControls = createControlGroup("Economy", priceLabel, priceSlider, musicProfileBox, supplierBtn, kitchenSupplierBtn, loanSharkBtn);
        JPanel managementControls = createControlGroup("Management", staffBtn, innBtn, upgradesBtn);
        JPanel riskControls = createControlGroup("Risk", securityBtn);
        JPanel activityControls = createControlGroup("Activities", activitiesBtn, actionsBtn);
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
        right.setPreferredSize(new Dimension(520, 1));
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
        JScrollPane controlsScroll = new JScrollPane(controls, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        controlsScroll.setBorder(BorderFactory.createEmptyBorder());
        controlsScroll.getViewport().setOpaque(false);
        controlsScroll.setOpaque(false);
        root.add(controlsScroll, BorderLayout.SOUTH);
        root.add(right, BorderLayout.EAST);

        frame.setContentPane(root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1320, 760);
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
        wrapper.add(new JLabel("  "));
        wrapper.add(timePhaseLabel);
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
        actionsBtn.setIcon(createGlyphIcon("L", new Color(160, 190, 120)));
        loanSharkBtn.setIcon(createGlyphIcon("F", new Color(140, 190, 220)));
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

        reportArea = new JTextArea(14, 36);
        reportArea.setEditable(false);
        reportArea.setFont(UIManager.getFont("TextArea.font"));
        reportArea.setLineWrap(false);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setPreferredSize(new Dimension(0, 240));

        p.add(header, BorderLayout.NORTH);
        p.add(reportScroll, BorderLayout.CENTER);
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
            missionFinanceArea = createMissionTextArea();
            missionPaydayArea = createMissionTextArea();
            missionSuppliersArea = createMissionTextArea();
            missionProgressionArea = createMissionTextArea();
            missionOperationsArea = createMissionTextArea();
            missionInnArea = createMissionTextArea();
            missionStaffArea = createMissionTextArea();
            missionStaffDetailArea = createMissionTextArea();
            missionRiskArea = createMissionTextArea();
            missionSecurityArea = createMissionTextArea();
            missionReputationArea = createMissionTextArea();
            missionRumorsArea = createMissionTextArea();
            missionTrafficArea = createMissionTextArea();
            missionInventoryArea = createMissionTextArea();
            missionLoansArea = createMissionTextArea();
            missionLogArea = createMissionTextArea();
            missionPrestigeArea = createMissionTextArea();
            missionMusicArea = createMissionTextArea();
            prestigePreviewButton = new JButton("Preview Prestige");
            prestigePreviewButton.addActionListener(e -> showPrestigePreviewDialog());

            tabs.add("Overview", new JScrollPane(missionOverviewArea));
            tabs.add("Finance & Banking", new JScrollPane(missionFinanceArea));
            tabs.add("Payday", new JScrollPane(missionPaydayArea));
            tabs.add("Suppliers", new JScrollPane(missionSuppliersArea));
            tabs.add("Pub Progression", new JScrollPane(missionProgressionArea));
            JPanel securityTab = new JPanel(new BorderLayout(6, 6));
            securityTab.add(buildSecurityPolicyPanel(), BorderLayout.NORTH);
            securityTab.add(new JScrollPane(missionSecurityArea), BorderLayout.CENTER);
            tabs.add("Security", securityTab);
            tabs.add("Staff", new JScrollPane(missionStaffDetailArea));
            tabs.add("Economy", new JScrollPane(missionEconomyArea));
            tabs.add("Operations", new JScrollPane(missionOperationsArea));
            tabs.add("Inn", new JScrollPane(missionInnArea));
            tabs.add("Risk & Security", new JScrollPane(missionRiskArea));
            tabs.add("Reputation & Identity", new JScrollPane(missionReputationArea));
            tabs.add("Rumors", new JScrollPane(missionRumorsArea));
            tabs.add("Traffic & Punters", new JScrollPane(missionTrafficArea));
            tabs.add("Music", new JScrollPane(missionMusicArea));
            tabs.add("Inventory", new JScrollPane(missionInventoryArea));
            tabs.add("Loans", new JScrollPane(missionLoansArea));
            tabs.add("Log / Events", new JScrollPane(missionLogArea));
            JPanel prestigeTab = new JPanel(new BorderLayout(6, 6));
            prestigeTab.add(new JScrollPane(missionPrestigeArea), BorderLayout.CENTER);
            JPanel prestigeActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            prestigeActions.add(prestigePreviewButton);
            prestigeTab.add(prestigeActions, BorderLayout.SOUTH);
            tabs.add("Prestige / Stars", prestigeTab);

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

    private JPanel buildSecurityPolicyPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Security Policy"));
        policyFriendlyBtn = new JRadioButton("Friendly Welcome");
        policyBalancedBtn = new JRadioButton("Balanced Door");
        policyStrictBtn = new JRadioButton("Strict Door");

        ButtonGroup group = new ButtonGroup();
        group.add(policyFriendlyBtn);
        group.add(policyBalancedBtn);
        group.add(policyStrictBtn);

        policyFriendlyBtn.addActionListener(e -> setSecurityPolicy(SecurityPolicy.FRIENDLY_WELCOME));
        policyBalancedBtn.addActionListener(e -> setSecurityPolicy(SecurityPolicy.BALANCED_DOOR));
        policyStrictBtn.addActionListener(e -> setSecurityPolicy(SecurityPolicy.STRICT_DOOR));

        panel.add(policyFriendlyBtn);
        panel.add(policyBalancedBtn);
        panel.add(policyStrictBtn);
        refreshSecurityPolicyButtons();
        return panel;
    }

    private JPanel buildSecurityMenuPolicyPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Door Policy"));
        securityPolicyBox = new JComboBox<>(SecurityPolicy.values());
        securityPolicyBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value != null ? value.getLabel() : "");
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        securityPolicyBox.addActionListener(e -> {
            if (updatingSecurityPolicyBox) return;
            SecurityPolicy selected = (SecurityPolicy) securityPolicyBox.getSelectedItem();
            if (selected != null) {
                setSecurityPolicy(selected);
            }
        });
        panel.add(securityPolicyBox);
        refreshSecurityPolicyButtons();
        return panel;
    }

    private JPanel buildSecurityTasksPanel() {
        securityTasksPanel = new JPanel(new BorderLayout(6, 6));
        securityTasksPanel.setBorder(BorderFactory.createTitledBorder("Security Tasks"));
        JLabel hint = new JLabel("Choose 1 task per round. Effects apply for the current round.");
        hint.setBorder(new EmptyBorder(0, 4, 4, 4));
        securityTasksPanel.add(hint, BorderLayout.NORTH);
        securityTasksListPanel = new JPanel();
        securityTasksListPanel.setLayout(new BoxLayout(securityTasksListPanel, BoxLayout.Y_AXIS));
        securityTasksPanel.add(securityTasksListPanel, BorderLayout.CENTER);
        return securityTasksPanel;
    }

    private void setSecurityPolicy(SecurityPolicy policy) {
        sim.setSecurityPolicy(policy);
        refreshAll();
    }

    private void refreshSecurityPolicyButtons() {
        if (policyFriendlyBtn == null && securityPolicyBox == null) return;
        SecurityPolicy policy = state.securityPolicy != null ? state.securityPolicy : SecurityPolicy.BALANCED_DOOR;
        if (policyFriendlyBtn != null) {
            policyFriendlyBtn.setSelected(policy == SecurityPolicy.FRIENDLY_WELCOME);
            policyBalancedBtn.setSelected(policy == SecurityPolicy.BALANCED_DOOR);
            policyStrictBtn.setSelected(policy == SecurityPolicy.STRICT_DOOR);
        }
        if (securityPolicyBox != null) {
            updatingSecurityPolicyBox = true;
            securityPolicyBox.setSelectedItem(policy);
            updatingSecurityPolicyBox = false;
        }
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
        actionsBtn.addActionListener(e -> openActionsDialog());
        staffBtn.addActionListener(e -> openStaffWindow());
        innBtn.addActionListener(e -> openInnWindow());

        securityBtn.addActionListener(e -> openSecurityWindow());
        happyHourBtn.addActionListener(e -> {
            boolean on = happyHourBtn.isSelected();
            sim.toggleHappyHour(on);
            happyHourBtn.setText(on ? "Happy Hour: ON" : "Happy Hour: OFF");
            refreshAll();
        });

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
        refreshInnWindow();
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
            if (!state.canEmergencyRestock()) {
                log.neg("Emergency restock requires a General Manager and Assistant Manager on staff.");
                return;
            }
        }

        if (supplierDialog == null) {
            supplierDialog = new JDialog(frame, "Supplier (Bulk Buy)", false);
            supplierDialog.setLayout(new BorderLayout(10, 10));

            supplierDealLabel = new JLabel("Supplier deal: (loading...)");
            supplierDealLabel.setBorder(new EmptyBorder(8, 10, 0, 10));
            supplierCreditLabel = new JLabel(" ");
            supplierCreditLabel.setBorder(new EmptyBorder(2, 10, 0, 10));
            supplierCreditLabel.setForeground(new Color(170, 90, 90));
            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.add(supplierDealLabel);
            header.add(supplierCreditLabel);
            header.add(buildSupplierInvoicePanel(SupplierAccount.WINE));
            header.setOpaque(false);
            supplierDialog.add(header, BorderLayout.NORTH);

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
            JButton invoices = new JButton("Payday Report");
            invoices.addActionListener(e -> openPaydayDialog());

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(invoices);
            bottom.add(close);
            supplierDialog.add(bottom, BorderLayout.SOUTH);

            supplierDialog.setSize(780, 500);
            supplierDialog.setLocationRelativeTo(frame);
        }

        refreshSupplierButtons();
        supplierDialog.setVisible(true);
    }

    private void refreshSupplierButtons() {
        if (supplierDialog == null || supplierListPanel == null || supplierDealLabel == null || supplierCreditLabel == null) return;

        String dealText = (state.supplierDeal == null) ? "No supplier deal today." : state.supplierDeal.getLabel();
        supplierDealLabel.setText("Supplier deal (locked until next night ends): " + dealText);
        supplierCreditLabel.setText(buildSupplierCreditSummary(
                "Wine supplier",
                state.supplierWineCredit,
                state.supplierWineMinDue()
        ));
        refreshSupplierInvoiceControls(wineInvoiceControls, state.supplierWineCredit);

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
    // Payday Window
    // -----------------------

    private void openPaydayDialog() {
        if (paydayDialog == null) {
            paydayDialog = new JDialog(frame, "Payday Report", false);
            paydayDialog.setLayout(new BorderLayout(10, 10));

            paydaySummaryLabel = new JLabel(" ");
            paydaySummaryLabel.setBorder(new EmptyBorder(8, 10, 0, 10));
            paydayDialog.add(paydaySummaryLabel, BorderLayout.NORTH);

            paydayListPanel = new JPanel();
            paydayListPanel.setLayout(new BoxLayout(paydayListPanel, BoxLayout.Y_AXIS));
            paydayDialog.add(new JScrollPane(paydayListPanel), BorderLayout.CENTER);

            JButton apply = new JButton("Apply Payments");
            apply.addActionListener(e -> {
                sim.applyPaydayPayments();
                refreshAll();
                refreshAllMenus();
                refreshPaydayDialog();
            });

            JButton close = new JButton("Close");
            close.addActionListener(e -> paydayDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(apply);
            bottom.add(close);
            paydayDialog.add(bottom, BorderLayout.SOUTH);

            paydayDialog.setSize(900, 560);
            paydayDialog.setLocationRelativeTo(frame);
        }

        refreshPaydayDialog();
        paydayDialog.setVisible(true);
    }

    private void refreshPaydayDialog() {
        if (paydayDialog == null || paydayListPanel == null || paydaySummaryLabel == null) return;

        paydayListPanel.removeAll();
        paydayRows.clear();

        if (state.paydayBills.isEmpty()) {
            paydayListPanel.add(new JLabel("No bills due this week."));
            paydaySummaryLabel.setText("No bills due this week.");
            paydayListPanel.revalidate();
            paydayListPanel.repaint();
            return;
        }

        for (PaydayBill bill : state.paydayBills) {
            JPanel row = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(4, 6, 4, 6);
            gbc.anchor = GridBagConstraints.WEST;

            JLabel title = new JLabel(bill.getDisplayName()
                    + " | Min " + money2(bill.getMinDue())
                    + " | Full " + money2(bill.getFullDue()));
            row.add(title, gbc);

            gbc.gridx++;
            JRadioButton minBtn = new JRadioButton("Min");
            JRadioButton fullBtn = new JRadioButton("Full");
            JRadioButton customBtn = new JRadioButton("Custom");
            ButtonGroup group = new ButtonGroup();
            group.add(minBtn);
            group.add(fullBtn);
            group.add(customBtn);
            minBtn.setSelected(true);
            row.add(minBtn, gbc);

            gbc.gridx++;
            row.add(fullBtn, gbc);
            gbc.gridx++;
            row.add(customBtn, gbc);

            gbc.gridx++;
            SpinnerNumberModel model = new SpinnerNumberModel(bill.getMinDue(), 0.0, bill.getFullDue(), 1.0);
            JSpinner amountSpinner = new JSpinner(model);
            amountSpinner.setEnabled(false);
            amountSpinner.setPreferredSize(new Dimension(110, amountSpinner.getPreferredSize().height));
            row.add(amountSpinner, gbc);

            gbc.gridx++;
            JComboBox<PaymentSourceOption> sourceBox = new JComboBox<>();
            for (PaymentSourceOption option : buildPaymentSourceOptions(bill)) {
                sourceBox.addItem(option);
            }
            row.add(sourceBox, gbc);

            gbc.gridx++;
            JLabel status = new JLabel(" ");
            status.setForeground(FLASH_RED);
            row.add(status, gbc);

            PaydayBillRow rowState = new PaydayBillRow(bill, minBtn, fullBtn, customBtn, amountSpinner, sourceBox, status);
            paydayRows.add(rowState);

            minBtn.addActionListener(e -> {
                bill.setSelectedAmount(bill.getMinDue());
                amountSpinner.setValue(bill.getMinDue());
                amountSpinner.setEnabled(false);
                updatePaydaySummary();
            });
            fullBtn.addActionListener(e -> {
                bill.setSelectedAmount(bill.getFullDue());
                amountSpinner.setValue(bill.getFullDue());
                amountSpinner.setEnabled(false);
                updatePaydaySummary();
            });
            customBtn.addActionListener(e -> {
                amountSpinner.setEnabled(true);
                double val = ((Number) amountSpinner.getValue()).doubleValue();
                bill.setSelectedAmount(val);
                updatePaydaySummary();
            });
            amountSpinner.addChangeListener(e -> {
                if (!customBtn.isSelected()) return;
                double val = ((Number) amountSpinner.getValue()).doubleValue();
                bill.setSelectedAmount(val);
                updatePaydaySummary();
            });
            sourceBox.addActionListener(e -> {
                PaymentSourceOption option = (PaymentSourceOption) sourceBox.getSelectedItem();
                bill.setSelectedSourceId(option == null ? "CASH" : option.id());
                updatePaydaySummary();
            });

            paydayListPanel.add(row);
        }

        updatePaydaySummary();
        paydayListPanel.revalidate();
        paydayListPanel.repaint();
    }

    private List<PaymentSourceOption> buildPaymentSourceOptions(PaydayBill bill) {
        java.util.List<PaymentSourceOption> options = new java.util.ArrayList<>();
        options.add(new PaymentSourceOption("CASH", "Cash (GBP " + money2(state.cash) + ")"));
        for (CreditLine line : state.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            if (bill.getType() == PaydayBill.Type.CREDIT_LINE && line.getId().equals(bill.getReferenceId())) {
                continue;
            }
            options.add(new PaymentSourceOption(
                    line.getId(),
                    line.getLenderName() + " (avail " + money2(line.availableCredit()) + ")"
            ));
        }
        return options;
    }

    private void updatePaydaySummary() {
        double totalMin = 0.0;
        double totalFull = 0.0;
        double totalSelected = 0.0;
        double cash = state.cash;
        java.util.Map<String, Double> creditAvailable = new java.util.HashMap<>();
        for (CreditLine line : state.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            creditAvailable.put(line.getId(), line.availableCredit());
        }

        for (PaydayBillRow row : paydayRows) {
            PaydayBill bill = row.bill();
            totalMin += bill.getMinDue();
            totalFull += bill.getFullDue();

            double requested = bill.getSelectedAmount();
            double available = "CASH".equals(bill.getSelectedSourceId())
                    ? cash
                    : creditAvailable.getOrDefault(bill.getSelectedSourceId(), 0.0);
            double applied = Math.min(requested, available);
            if (applied + 0.01 < requested) {
                row.statusLabel().setText("Clamped to available");
                bill.setSelectedAmount(applied);
                if (row.customBtn().isSelected()) {
                    row.amountSpinner().setValue(applied);
                }
            } else {
                row.statusLabel().setText(" ");
            }
            if ("CASH".equals(bill.getSelectedSourceId())) {
                cash -= applied;
            } else {
                creditAvailable.put(bill.getSelectedSourceId(), available - applied);
            }
            totalSelected += applied;
        }

        double creditUsed = 0.0;
        for (CreditLine line : state.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            double original = line.availableCredit();
            double remaining = creditAvailable.getOrDefault(line.getId(), original);
            creditUsed += Math.max(0.0, original - remaining);
        }

        paydaySummaryLabel.setText("Min due: " + money2(totalMin)
                + " | Full due: " + money2(totalFull)
                + " | Selected: " + money2(totalSelected)
                + " | Projected cash: " + money2(cash)
                + " | Credit usage: " + money2(creditUsed));
    }

    private String buildSupplierCreditSummary(String label, SupplierTradeCredit account, double minDue) {
        double balance = account.getBalance();
        double cap = state.supplierCreditCap();
        String trust = state.supplierTrustLabel();
        String priceMult = String.format("%.2f", state.supplierPriceMultiplier());
        return "<html>" + label + " credit: balance " + money2(balance)
                + " / cap " + money2(cap)
                + " | min due " + money2(minDue)
                + " | full due " + money2(balance)
                + "<br/>Trust: " + trust + " | Price mult x" + priceMult
                + "</html>";
    }

    private JPanel buildSupplierInvoicePanel(SupplierAccount account) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Invoice / Trade Credit"));

        JLabel summary = new JLabel(" ");
        summary.setBorder(new EmptyBorder(2, 8, 2, 8));
        panel.add(summary);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton payFull = new JButton("Pay Invoice — Full");
        JButton payCustom = new JButton("Pay Invoice — Custom");
        JLabel amountLabel = new JLabel("Amount");
        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0));
        amountSpinner.setPreferredSize(new Dimension(90, amountSpinner.getPreferredSize().height));
        JComboBox<PaymentSourceOption> sourceBox = new JComboBox<>();

        row.add(payFull);
        row.add(payCustom);
        row.add(amountLabel);
        row.add(amountSpinner);
        row.add(sourceBox);
        panel.add(row);

        JLabel status = new JLabel(" ");
        status.setForeground(FLASH_RED);
        panel.add(status);

        SupplierInvoiceControls controls = new SupplierInvoiceControls(
                account, summary, status, amountSpinner, payFull, payCustom, sourceBox
        );
        if (account == SupplierAccount.WINE) {
            wineInvoiceControls = controls;
        } else {
            foodInvoiceControls = controls;
        }

        payFull.addActionListener(e -> handleSupplierInvoicePayment(controls, true));
        payCustom.addActionListener(e -> handleSupplierInvoicePayment(controls, false));

        return panel;
    }

    private void handleSupplierInvoicePayment(SupplierInvoiceControls controls, boolean full) {
        if (controls == null) return;
        SupplierTradeCredit account = supplierAccount(controls.account());
        if (account == null) return;

        double amount = full ? account.getBalance() : ((Number) controls.amountSpinner().getValue()).doubleValue();
        PaymentSourceOption option = (PaymentSourceOption) controls.sourceBox().getSelectedItem();
        String sourceId = option == null ? "CASH" : option.id();

        Simulation.SupplierPaymentResult result = sim.paySupplierInvoice(controls.account(), amount, sourceId);
        if (result.success()) {
            controls.statusLabel().setForeground(new Color(60, 140, 90));
        } else {
            controls.statusLabel().setForeground(FLASH_RED);
        }
        controls.statusLabel().setText(result.message());
        refreshAll();
        refreshAllMenus();
        refreshSupplierButtons();
        refreshKitchenSupplierButtons();
    }

    private SupplierTradeCredit supplierAccount(SupplierAccount account) {
        if (account == SupplierAccount.FOOD) return state.supplierFoodCredit;
        return state.supplierWineCredit;
    }

    private void refreshSupplierInvoiceControls(SupplierInvoiceControls controls, SupplierTradeCredit account) {
        if (controls == null || account == null) return;
        double balance = account.getBalance();
        double cap = state.supplierCreditCap();
        double available = Math.max(0.0, cap - balance);
        String trust = state.supplierTrustLabel();
        String priceMult = String.format("%.2f", state.supplierPriceMultiplier());
        controls.summaryLabel().setText("<html>Balance " + money2(balance)
                + " | Cap " + money2(cap)
                + " | Available " + money2(available)
                + "<br/>Trust " + trust + " | Price mult x" + priceMult
                + "</html>");

        SpinnerNumberModel model = (SpinnerNumberModel) controls.amountSpinner().getModel();
        double max = Math.max(0.0, balance);
        model.setMinimum(0.0);
        model.setMaximum(max);
        double current = ((Number) model.getNumber()).doubleValue();
        if (current > max) {
            controls.amountSpinner().setValue(max);
        }

        boolean enabled = balance > 0.0;
        controls.payFullBtn().setEnabled(enabled);
        controls.payCustomBtn().setEnabled(enabled);
        controls.amountSpinner().setEnabled(enabled);

        String selectedId = null;
        PaymentSourceOption selected = (PaymentSourceOption) controls.sourceBox().getSelectedItem();
        if (selected != null) {
            selectedId = selected.id();
        }
        controls.sourceBox().removeAllItems();
        for (PaymentSourceOption option : buildSupplierPaymentSourceOptions()) {
            controls.sourceBox().addItem(option);
            if (selectedId != null && selectedId.equals(option.id())) {
                controls.sourceBox().setSelectedItem(option);
            }
        }
        if (!enabled) {
            controls.statusLabel().setForeground(FLASH_RED);
            controls.statusLabel().setText("No outstanding balance.");
        } else if (controls.statusLabel().getText().isBlank()) {
            controls.statusLabel().setText(" ");
        }
    }

    private List<PaymentSourceOption> buildSupplierPaymentSourceOptions() {
        java.util.List<PaymentSourceOption> options = new java.util.ArrayList<>();
        options.add(new PaymentSourceOption("CASH", "Cash (GBP " + money2(state.cash) + ")"));
        for (CreditLine line : state.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            options.add(new PaymentSourceOption(
                    line.getId(),
                    line.getLenderName() + " (avail " + money2(line.availableCredit()) + ")"
            ));
        }
        return options;
    }

    private CreditLine selectCreditLineForPayment(List<CreditLine> options, double shortfall, String reason) {
        if (options == null || options.isEmpty()) return null;
        if (options.size() == 1) return options.get(0);
        Object[] choices = new Object[options.size()];
        for (int i = 0; i < options.size(); i++) {
            CreditLine line = options.get(i);
            choices[i] = line.getLenderName()
                    + " | avail " + money2(line.availableCredit())
                    + " | APR " + String.format("%.2f", line.getInterestAPR() * 100) + "%"
                    + " | bal " + money2(line.getBalance()) + "/" + money2(line.getLimit());
        }
        Object pick = JOptionPane.showInputDialog(
                frame,
                "Select credit line to cover GBP " + String.format("%.2f", shortfall) + "\n" + reason,
                "Select Credit Line",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );
        if (pick == null) return null;
        String choice = pick.toString();
        for (CreditLine line : options) {
            if (choice.startsWith(line.getLenderName())) {
                return line;
            }
        }
        return null;
    }

    // -----------------------
    // Kitchen Supplier Window
    // -----------------------

    private void openKitchenSupplierWindow() {
        if (!canUseKitchen()) {
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
            kitchenSupplierNoticeLabel = new JLabel(" ");
            kitchenSupplierNoticeLabel.setBorder(new EmptyBorder(2, 10, 0, 10));
            kitchenSupplierNoticeLabel.setForeground(new Color(170, 90, 90));
            kitchenSupplierCreditLabel = new JLabel(" ");
            kitchenSupplierCreditLabel.setBorder(new EmptyBorder(2, 10, 0, 10));
            kitchenSupplierCreditLabel.setForeground(new Color(170, 90, 90));
            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.add(top);
            header.add(kitchenSupplierNoticeLabel);
            header.add(kitchenSupplierCreditLabel);
            header.add(buildSupplierInvoicePanel(SupplierAccount.FOOD));
            header.setOpaque(false);
            kitchenSupplierDialog.add(header, BorderLayout.NORTH);

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
        if (kitchenSupplierDialog == null || kitchenSupplierListPanel == null || kitchenSupplierCreditLabel == null) return;

        if (!canUseKitchen()) {
            if (kitchenSupplierNoticeLabel != null) {
                kitchenSupplierNoticeLabel.setText("Kitchen not installed (requires Kitchen upgrade).");
            }
            for (Component rowC : kitchenSupplierListPanel.getComponents()) {
                if (!(rowC instanceof JPanel row)) continue;
                for (Component c : row.getComponents()) {
                    if (c instanceof JButton b) b.setEnabled(false);
                }
            }
            return;
        }

        int freeSlots = state.foodRack.getCapacity() - state.foodRack.count();
        boolean hasHeadChef = state.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1;
        if (kitchenSupplierNoticeLabel != null) {
            kitchenSupplierNoticeLabel.setText(state.nightOpen && !hasHeadChef
                    ? "Requires Head Chef for emergency orders."
                    : " ");
        }
        kitchenSupplierCreditLabel.setText(buildSupplierCreditSummary(
                "Food supplier",
                state.supplierFoodCredit,
                state.supplierFoodMinDue()
        ));
        refreshSupplierInvoiceControls(foodInvoiceControls, state.supplierFoodCredit);

        for (Component rowC : kitchenSupplierListPanel.getComponents()) {
            if (!(rowC instanceof JPanel row)) continue;

            for (Component c : row.getComponents()) {
                if (c instanceof JLabel lbl) {
                    Food f = (Food) lbl.getClientProperty("food");
                    if (f == null) continue;
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
                b.setEnabled((!state.nightOpen || hasHeadChef) && okSlots);
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
            JLabel innLockLabel = new JLabel("Inn roles (requires Inn Wing upgrade)");
            innLockLabel.setForeground(new Color(150, 150, 150));
            staffHirePanel.add(innLockLabel);
            staffHirePanel.add(Box.createVerticalStrut(6));
            addStaffHireButton(Staff.Type.RECEPTION_TRAINEE);
            addStaffHireButton(Staff.Type.RECEPTIONIST);
            addStaffHireButton(Staff.Type.SENIOR_RECEPTIONIST);
            addStaffHireButton(Staff.Type.HOUSEKEEPING_TRAINEE);
            addStaffHireButton(Staff.Type.HOUSEKEEPER);
            addStaffHireButton(Staff.Type.HEAD_HOUSEKEEPER);
            addStaffHireButton(Staff.Type.DUTY_MANAGER);
            kitchenLockLabel = new JLabel("Kitchen not installed (requires Kitchen upgrade)");
            kitchenLockLabel.setForeground(new Color(150, 150, 150));
            staffHirePanel.add(kitchenLockLabel);
            staffHirePanel.add(Box.createVerticalStrut(6));
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
            state.lastStaffChangeDay = state.dayCounter;
            state.lastStaffChangeSummary = "Hired " + Staff.rangeLabel(type);
            refreshAll();
            refreshAllMenus();
        });

        staffHirePanel.add(b);
        staffHirePanel.add(Box.createVerticalStrut(6));
    }

    private void refreshStaffButtons() {
        if (staffDialog == null || staffHirePanel == null || staffRosterPanel == null) return;
        if (kitchenLockLabel != null) {
            kitchenLockLabel.setVisible(!canUseKitchen());
        }

        // Enable/disable hire options
        for (Component c : staffHirePanel.getComponents()) {
            if (!(c instanceof JButton b)) continue;

            Object tObj = b.getClientProperty("staffType");
            if (!(tObj instanceof Staff.Type t)) continue;

            boolean enabled;

            // General manager pool
            if (t == Staff.Type.MANAGER) {
                enabled = state.managerPoolCount() < state.managerCap;

            } else if (t == Staff.Type.DUTY_MANAGER) {
                enabled = state.innUnlocked
                        && state.innTier >= 2
                        && state.managerPoolCount() < state.managerCap
                        && state.fohStaffCount() < state.fohStaffCap;

                // Kitchen roles (BOH)
            } else if (t == Staff.Type.HEAD_CHEF
                    || t == Staff.Type.SOUS_CHEF
                    || t == Staff.Type.CHEF_DE_PARTIE
                    || t == Staff.Type.KITCHEN_ASSISTANT
                    || t == Staff.Type.KITCHEN_PORTER
                    || t == Staff.Type.CHEF) {
                enabled = canUseKitchen() && (state.bohStaff.size() < state.kitchenChefCap);
                if (t == Staff.Type.HEAD_CHEF && state.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1) {
                    enabled = false;
                }

                // FOH roles (bar staff + inn roles)
            } else {
                if (state.isHohRole(t)) {
                    enabled = state.innUnlocked && state.hohStaffCount() < state.hohStaffCap;
                } else {
                    enabled = state.fohStaffCount() < state.fohStaffCap;
                }
                if (t == Staff.Type.ASSISTANT_MANAGER) {
                    enabled = state.managerPoolCount() < state.managerCap;
                }
            }

            // hires only between nights (except bouncer)
            if (state.nightOpen) enabled = false;

            b.setEnabled(enabled);
        }

        // Rebuild roster (fire buttons)
        staffRosterPanel.removeAll();

        // Manager pool summary + general manager rows
        staffRosterPanel.add(new JLabel("Manager slots used: " + state.managerPoolCount() + "/" + state.managerCap
                + " (GM " + state.generalManagers.size() + ", AM " + state.assistantManagerCount()
                + ", DM " + state.dutyManagerCount() + ")"));
        staffRosterPanel.add(Box.createVerticalStrut(6));
        if (state.generalManagers.isEmpty()) {
            staffRosterPanel.add(new JLabel("General Managers: (none)"));
            staffRosterPanel.add(Box.createVerticalStrut(8));
        } else {
            staffRosterPanel.add(new JLabel("General Managers (" + state.generalManagers.size() + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int i = 0; i < state.generalManagers.size(); i++) {
                final int idx = i;
                Staff st = state.generalManagers.get(i);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireManagerAt(idx);
                    state.lastStaffChangeDay = state.dayCounter;
                    state.lastStaffChangeSummary = "Fired " + st.getName();
                    refreshAll();
                    refreshAllMenus();
                }));
                staffRosterPanel.add(Box.createVerticalStrut(6));
            }
        }

        java.util.List<Integer> assistantManagerIndices = new java.util.ArrayList<>();
        java.util.List<Integer> hohIndices = new java.util.ArrayList<>();
        java.util.List<Integer> fohIndices = new java.util.ArrayList<>();
        for (int i = 0; i < state.fohStaff.size(); i++) {
            Staff st = state.fohStaff.get(i);
            if (st.getType() == Staff.Type.ASSISTANT_MANAGER) {
                assistantManagerIndices.add(i);
            } else if (state.isHohRole(st.getType())) {
                hohIndices.add(i);
            } else {
                fohIndices.add(i);
            }
        }

        if (assistantManagerIndices.isEmpty()) {
            staffRosterPanel.add(new JLabel("Assistant Managers: (none)"));
        } else {
            staffRosterPanel.add(new JLabel("Assistant Managers (" + assistantManagerIndices.size() + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int index : assistantManagerIndices) {
                Staff st = state.fohStaff.get(index);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireStaffAt(index);
                    state.lastStaffChangeDay = state.dayCounter;
                    state.lastStaffChangeSummary = "Fired " + st.getName();
                    refreshAll();
                    refreshAllMenus();
                }));
                staffRosterPanel.add(Box.createVerticalStrut(6));
            }
        }
        staffRosterPanel.add(Box.createVerticalStrut(6));

        if (hohIndices.isEmpty()) {
            staffRosterPanel.add(new JLabel("HOH Staff: (none)"));
        } else {
            staffRosterPanel.add(new JLabel("HOH Staff (" + state.hohStaffCount() + "/" + state.hohStaffCap + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int index : hohIndices) {
                final int idx = index;
                Staff st = state.fohStaff.get(index);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireStaffAt(idx);
                    state.lastStaffChangeDay = state.dayCounter;
                    state.lastStaffChangeSummary = "Fired " + st.getName();
                    refreshAll();
                    refreshAllMenus();
                }));
                staffRosterPanel.add(Box.createVerticalStrut(6));
            }
        }
        staffRosterPanel.add(Box.createVerticalStrut(6));

        if (fohIndices.isEmpty()) {
            staffRosterPanel.add(new JLabel("FOH Staff: (none)"));
        } else {
            staffRosterPanel.add(new JLabel("FOH Staff (" + state.fohStaffCount() + "/" + state.fohStaffCap + "):"));
            staffRosterPanel.add(Box.createVerticalStrut(4));
            for (int index : fohIndices) {
                final int idx = index;
                Staff st = state.fohStaff.get(index);
                staffRosterPanel.add(makeStaffRow(st.toString(), () -> {
                    sim.fireStaffAt(idx);
                    state.lastStaffChangeDay = state.dayCounter;
                    state.lastStaffChangeSummary = "Fired " + st.getName();
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
                    state.lastStaffChangeDay = state.dayCounter;
                    state.lastStaffChangeSummary = "Fired " + st.getName();
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

            JLabel top = new JLabel("Manage base security, bouncers, and marshalls.");
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

            marshallBtn = new JButton();
            marshallBtn.addActionListener(e -> {
                sim.hireMarshall();
                refreshAll();
                refreshAllMenus();
            });

            securityListPanel.add(securityUpgradeBtn);
            securityListPanel.add(Box.createVerticalStrut(8));
            securityListPanel.add(bouncerBtn);
            securityListPanel.add(Box.createVerticalStrut(8));
            securityListPanel.add(marshallBtn);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(buildSecurityMenuPolicyPanel());
            content.add(Box.createVerticalStrut(8));
            content.add(securityListPanel);
            content.add(Box.createVerticalStrut(8));
            content.add(buildSecurityTasksPanel());

            securityDialog.add(new JScrollPane(content), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> securityDialog.setVisible(false));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            securityDialog.add(bottom, BorderLayout.SOUTH);

            securityDialog.setSize(520, 420);
            securityDialog.setLocationRelativeTo(frame);
        }

        refreshSecurityButtons();
        securityDialog.setVisible(true);
    }

    private void refreshSecurityButtons() {
        if (securityDialog == null || securityUpgradeBtn == null || bouncerBtn == null || marshallBtn == null) return;
        securityUpgradeBtn.setText("Base Security +1 (level " + state.baseSecurityLevel
                + ", cost " + money0(sim.peekSecurityUpgradeCost()) + ")");
        securityUpgradeBtn.setEnabled(!state.nightOpen);

        bouncerBtn.setText("Hire Bouncer Tonight " + state.bouncersHiredTonight + "/" + state.bouncerCap);
        bouncerBtn.setEnabled(state.nightOpen && state.bouncersHiredTonight < state.bouncerCap);
        marshallBtn.setText("Hire Marshall " + state.marshallCount() + "/" + state.marshallCap);
        marshallBtn.setEnabled(!state.nightOpen && state.isMarshallUnlocked() && state.marshallCount() < state.marshallCap);
        refreshSecurityPolicyButtons();
        refreshSecurityTasksPanel();
    }

    private void refreshSecurityTasksPanel() {
        if (securityTasksListPanel == null) return;
        securityTasksListPanel.removeAll();

        int baseSecurity = state.baseSecurityLevel;
        for (int tier = 1; tier <= 3; tier++) {
            int required = tier == 1 ? 5 : (tier == 2 ? 15 : 30);
            boolean unlocked = baseSecurity >= required;
            JLabel tierLabel = new JLabel("Tier " + tier + " tasks");
            tierLabel.setFont(tierLabel.getFont().deriveFont(Font.BOLD));
            securityTasksListPanel.add(tierLabel);
            if (!unlocked) {
                securityTasksListPanel.add(new JLabel("Locked: Base Security " + required + "+"));
                securityTasksListPanel.add(Box.createVerticalStrut(8));
                continue;
            }

            for (SecurityTask task : SecurityTask.tasksForTier(tier)) {
                Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
                JButton use = new JButton("Use");
                use.setEnabled(availability.canUse());
                use.addActionListener(e -> {
                    sim.resolveSecurityTask(task);
                    refreshAll();
                    refreshAllMenus();
                });

                String status = "";
                if (state.activeSecurityTask == task) {
                    status = state.isSecurityTaskActive() ? " (Active)" : " (Queued)";
                }
                int cooldown = state.securityTaskCooldownRemaining(task);
                String cooldownText = cooldown > 0 ? " | CD " + cooldown + "r" : "";
                String label = "<html><b>" + task.getLabel() + "</b> (" + task.getCategory().getLabel()
                        + ")" + status
                        + "<br/>" + task.getDescription()
                        + "<br/>" + task.effectSummary()
                        + cooldownText
                        + "</html>";
                JLabel text = new JLabel(label);

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.add(text, BorderLayout.CENTER);
                row.add(use, BorderLayout.EAST);
                securityTasksListPanel.add(row);
                securityTasksListPanel.add(Box.createVerticalStrut(6));
            }
            securityTasksListPanel.add(Box.createVerticalStrut(6));
        }

        securityTasksListPanel.revalidate();
        securityTasksListPanel.repaint();
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
                String requirement = sim.upgradeRequirementText(up);
                prefix = unlocked ? "" : (" LOCKED  " + (requirement != null ? "(" + requirement + ")  " : ""));
            }
            b.setText(prefix + up.toString());
            boolean enabled = !state.nightOpen && !owned && unlocked && installing == null;
            b.setEnabled(enabled);
            b.setToolTipText(!unlocked && !owned ? sim.upgradeRequirementText(up) : null);
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

    // -----------------------
    // Inn Window
    // -----------------------

    void openInnWindow() {
        if (innDialog == null) {
            innDialog = new JDialog(frame, "Inn", false);
            innDialog.setLayout(new BorderLayout(10, 10));

            JLabel top = new JLabel("Rooms, bookings, and nightly upkeep.");
            top.setBorder(new EmptyBorder(8, 10, 0, 10));
            innDialog.add(top, BorderLayout.NORTH);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(new EmptyBorder(8, 10, 10, 10));

            innRoomsLabel = new JLabel();
            innBookedLabel = new JLabel();
            innPriceLabel = new JLabel();
            innPriceSlider = new JSlider(20, 120, 45);
            innPriceSlider.setMajorTickSpacing(20);
            innPriceSlider.setMinorTickSpacing(5);
            innPriceSlider.setPaintTicks(true);
            innPriceSlider.addChangeListener(e -> {
                if (!state.innUnlocked) return;
                sim.setRoomPrice(innPriceSlider.getValue());
                innPriceLabel.setText("Room price: " + money0(state.roomPrice));
                refreshAll();
            });

            innRepBar = new JProgressBar(0, 100);
            innRepBar.setStringPainted(true);
            innCleanBar = new JProgressBar(0, 100);
            innCleanBar.setStringPainted(true);

            innReceptionLabel = new JLabel();
            innHousekeepingLabel = new JLabel();
            innDutyManagerLabel = new JLabel();
            innMaintenanceLabel = new JLabel();
            innSummaryLabel = new JLabel();

            content.add(innRoomsLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(innBookedLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(innPriceLabel);
            content.add(innPriceSlider);
            content.add(Box.createVerticalStrut(6));

            content.add(new JLabel("Inn reputation:"));
            content.add(innRepBar);
            content.add(Box.createVerticalStrut(4));
            content.add(new JLabel("Cleanliness:"));
            content.add(innCleanBar);
            content.add(Box.createVerticalStrut(6));

            content.add(innReceptionLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(innHousekeepingLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(innDutyManagerLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(innMaintenanceLabel);
            content.add(Box.createVerticalStrut(6));
            content.add(innSummaryLabel);

            innDialog.add(new JScrollPane(content), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> innDialog.setVisible(false));
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            innDialog.add(bottom, BorderLayout.SOUTH);

            innDialog.setSize(520, 520);
            innDialog.setLocationRelativeTo(frame);
        }

        refreshInnWindow();
        innDialog.setVisible(true);
    }

    private void refreshInnWindow() {
        if (innDialog == null) return;
        boolean unlocked = state.innUnlocked;
        if (innRoomsLabel != null) {
            innRoomsLabel.setText(unlocked
                    ? "Rooms: " + state.roomsTotal + " (Tier " + state.innTier + ")"
                    : "Rooms: Locked (purchase Inn Wing upgrade)");
        }
        if (innBookedLabel != null) {
            innBookedLabel.setText(unlocked
                    ? "Booked last night: " + state.lastNightRoomsBooked + " | Revenue " + money2(state.lastNightRoomRevenue)
                    : "Booked last night: 0");
        }
        if (innPriceLabel != null) {
            innPriceLabel.setText("Room price: " + (unlocked ? money0(state.roomPrice) : "Locked"));
        }
        if (innPriceSlider != null) {
            innPriceSlider.setEnabled(unlocked && !state.nightOpen);
            if (unlocked) {
                innPriceSlider.setValue((int)Math.round(state.roomPrice));
            }
        }
        if (innRepBar != null) {
            innRepBar.setValue((int)Math.round(state.innRep));
            innRepBar.setString(unlocked ? (int)Math.round(state.innRep) + "%" : "Locked");
        }
        if (innCleanBar != null) {
            innCleanBar.setValue((int)Math.round(state.cleanliness));
            innCleanBar.setString(unlocked ? (int)Math.round(state.cleanliness) + "%" : "Locked");
        }
        if (innReceptionLabel != null) {
            innReceptionLabel.setText("Reception coverage: " + state.lastInnReceptionCapacity
                    + " / needed " + state.roomsTotal);
        }
        if (innHousekeepingLabel != null) {
            innHousekeepingLabel.setText("Housekeeping coverage: " + state.lastInnHousekeepingCoverage
                    + " / rooms booked " + state.lastInnHousekeepingNeeded);
        }
        if (innDutyManagerLabel != null) {
            innDutyManagerLabel.setText("Duty manager: " + (state.dutyManagerCount() > 0 ? "Yes" : "No"));
        }
        if (innMaintenanceLabel != null) {
            innMaintenanceLabel.setText("Maintenance accrued (weekly): " + money2(state.innMaintenanceAccruedWeekly)
                    + " | Due at Payday");
        }
        if (innSummaryLabel != null) {
            innSummaryLabel.setText(unlocked ? ("Summary: " + state.lastNightInnSummaryLine) : "Summary: Inn locked.");
        }
    }

    private void openActionsDialog() {
        if (actionsDialog == null) {
            actionsDialog = new JDialog(frame, "Landlord Actions", true);
            actionsDialog.setLayout(new BorderLayout(8, 8));
            actionsPanel = new LandlordActionsPanel(sim, state, this::handleLandlordAction);
            actionsDialog.add(actionsPanel, BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> actionsDialog.setVisible(false));
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            actionsDialog.add(bottom, BorderLayout.SOUTH);

            actionsDialog.setSize(760, 560);
            actionsDialog.setLocationRelativeTo(frame);
        }

        actionsPanel.refresh();
        actionsDialog.setVisible(true);
    }

    private void handleLandlordAction(LandlordActionId id) {
        LandlordActionResolution result = sim.resolveLandlordAction(id);
        if (result == null) return;
        if (result.blocked()) {
            log.popup(" Action blocked", result.message(), "");
        }
        refreshAll();
        if (actionsPanel != null) {
            actionsPanel.refresh();
        }
    }

    private void refreshActivitiesButtons() {
        if (activitiesDialog == null || activitiesListPanel == null) return;

        for (Component c : activitiesListPanel.getComponents()) {
            if (!(c instanceof JButton b)) continue;

            PubActivity a = (PubActivity) b.getClientProperty("activity");
            if (a == null) continue;

            String requirement = sim.activityRequirementText(a);
            boolean unlocked = state.unlockedActivities.contains(a);
            boolean enabled = requirement == null && unlocked && !state.nightOpen && state.scheduledActivity == null;
            b.setEnabled(enabled);

            String txt = a.toString();
            if (!unlocked || requirement != null) {
                String reqText = requirement != null ? (" (" + requirement + ")") : "";
                txt = " LOCKED" + reqText + "  " + txt;
            }
            if (state.activityTonight == a) txt = " RUNNING  " + txt;
            if (state.scheduledActivity != null) {
                int daysLeft = Math.max(0, state.scheduledActivity.startAbsDayIndex() - state.absDayIndex());
                txt = " SCHEDULED (" + daysLeft + "d)  " + state.scheduledActivity.activity();
                b.setEnabled(false);
            }
            b.setText(txt);
            b.setToolTipText(requirement);
        }
    }

    // -----------------------
    // Finance Dialog
    // -----------------------

    private void openLoanDialog() {
        if (loanDialog == null) {
            loanDialog = new JDialog(frame, "Finance", false);
            loanDialog.setLayout(new BorderLayout(10, 10));

            loanTextArea = new JTextArea(12, 44);
            loanTextArea.setEditable(false);
            loanTextArea.setFont(UIManager.getFont("TextArea.font"));

            loanDialog.add(new JScrollPane(loanTextArea), BorderLayout.CENTER);

            loanButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            loanDialog.add(loanButtonsPanel, BorderLayout.SOUTH);

            loanDialog.setSize(820, 420);
            loanDialog.setLocationRelativeTo(frame);
        }

        refreshLoanDialog();
        loanDialog.setVisible(true);
    }

    private void refreshLoanDialog() {
        if (loanDialog == null || loanTextArea == null || loanButtonsPanel == null) return;

        loanTextArea.setText(buildFinanceText());

        loanButtonsPanel.removeAll();

        loanButtonsPanel.add(new JLabel("Loan Shark:"));
        JButton openShark = new JButton(state.loanShark.isOpen() ? "Loan Shark Loan Active" : "Take Loan Shark Loan");
        openShark.setEnabled(!state.loanShark.isOpen());
        openShark.setToolTipText("High APR, aggressive repayments. Taking the loan hurts credit score.");
        openShark.addActionListener(e -> { sim.openSharkLine(); refreshAll(); refreshAllMenus(); });
        loanButtonsPanel.add(openShark);

        loanButtonsPanel.add(new JLabel("Banks:"));
        for (Bank bank : Bank.values()) {
            JButton open = new JButton("Open " + bank.getName());
            boolean unlocked = bank.isUnlocked(state.creditScore);
            boolean alreadyOpen = state.creditLines.hasLine(bank.getName());
            open.setEnabled(unlocked && !alreadyOpen);
            open.setToolTipText("Limit GBP " + bank.getMinLimit() + "-" + bank.getMaxLimit()
                    + " | APR " + String.format("%.1f", bank.getMinApr() * 100) + "%-"
                    + String.format("%.1f", bank.getMaxApr() * 100) + "%"
                    + (bank.getMinScore() > 0 ? " | Score " + bank.getMinScore() + "+" : ""));
            open.addActionListener(e -> { sim.openCreditLine(bank); refreshAll(); refreshAllMenus(); });
            loanButtonsPanel.add(open);
        }

        if (!state.creditLines.getOpenLines().isEmpty()) {
            loanButtonsPanel.add(new JLabel("Repay credit lines:"));
        }
        for (CreditLine line : state.creditLines.getOpenLines()) {
            JButton repay = new JButton("Repay " + line.getLenderName());
            repay.setEnabled(line.getBalance() > 0.0 && state.cash >= line.getBalance());
            repay.addActionListener(e -> { sim.repayCreditLineInFull(line.getId()); refreshAll(); refreshAllMenus(); });
            loanButtonsPanel.add(repay);
        }

        loanButtonsPanel.revalidate();
        loanButtonsPanel.repaint();
    }

    private String buildFinanceText() {
        StringBuilder sb = new StringBuilder();
        double totalDebt = state.totalCreditBalance()
                + (state.loanShark.isOpen() ? state.loanShark.getBalance() : 0.0);
        sb.append("Cash: ").append(money2(state.cash)).append("\n");
        sb.append("Total debt: ").append(money2(totalDebt)).append(" / ")
                .append(money2(state.totalCreditLimit())).append("\n");
        sb.append("Weekly repayments due: ").append(money2(state.totalCreditWeeklyPaymentDue())).append("\n");
        sb.append("Credit score: ").append(state.creditScore).append("\n\n");

        sb.append("Open credit lines:\n");
        if (state.creditLines.getOpenLines().isEmpty()) {
            sb.append("  None\n");
        } else {
            for (CreditLine line : state.creditLines.getOpenLines()) {
                sb.append("  ").append(line.getLenderName())
                        .append(" | Limit ").append(money2(line.getLimit()))
                        .append(" | Balance ").append(money2(line.getBalance()))
                        .append(" | Weekly ").append(money2(line.getWeeklyPayment()))
                        .append(" | APR ").append(String.format("%.2f", line.getInterestAPR() * 100)).append("%")
                        .append(" | Missed ").append(line.getMissedPaymentCount())
                        .append("\n");
            }
        }

        sb.append("\nAvailable banks:\n");
        if (state.banksLocked) {
            sb.append("  Banks are refusing new credit until wages stabilize.\n");
        }
        for (Bank bank : Bank.values()) {
            sb.append("  ").append(bank.getName())
                    .append(" | Limit ").append(bank.getMinLimit()).append("-").append(bank.getMaxLimit())
                    .append(" | APR ").append(String.format("%.1f", bank.getMinApr() * 100)).append("%-")
                    .append(String.format("%.1f", bank.getMaxApr() * 100)).append("%");
            if (bank.getMinScore() > 0) sb.append(" | Score ").append(bank.getMinScore()).append("+");
            sb.append("\n");
        }

        sb.append("\nLoan Shark Loan:\n");
        if (state.loanShark.isOpen()) {
            sb.append("  Balance ").append(money2(state.loanShark.getBalance()))
                    .append(" | Weekly ").append(money2(state.loanShark.minPaymentDue()))
                    .append(" | APR ").append(String.format("%.2f", state.loanShark.getApr() * 100)).append("%\n");
            sb.append("  Threat Tier ").append(state.sharkThreatTier)
                    .append(" (").append(sim.sharkTierLabel(state.sharkThreatTier)).append(")")
                    .append(" | Trigger: ").append(state.sharkThreatTrigger).append("\n");
        } else {
            sb.append("  Not active\n");
        }

        return sb.toString();
    }

    private void refreshReportsDialog() {
        if (reportsDialog == null) return;
        if (reportsDialogArea != null) {
            reportsDialogArea.setText(ReportSystem.buildReportText(state));
        }
        if (reportsDialogLoansArea != null) {
            reportsDialogLoansArea.setText(buildFinanceText());
        }
    }

    private void refreshMissionControl() {
        if (missionControlDialog == null) return;
        MetricsSnapshot snapshot = lastSnapshot != null ? lastSnapshot : sim.buildMetricsSnapshot();
        if (snapshot == null) return;
        if (missionOverviewArea != null) {
            missionOverviewArea.setText(String.join("\n", snapshot.overviewLines));
        }
        if (missionFinanceArea != null) missionFinanceArea.setText(snapshot.financeBanking);
        if (missionPaydayArea != null) missionPaydayArea.setText(snapshot.payday);
        if (missionSuppliersArea != null) missionSuppliersArea.setText(snapshot.suppliers);
        if (missionProgressionArea != null) missionProgressionArea.setText(snapshot.progression);
        if (missionSecurityArea != null) missionSecurityArea.setText(snapshot.security);
        refreshSecurityPolicyButtons();
        if (missionStaffDetailArea != null) missionStaffDetailArea.setText(snapshot.staffDetail);
        if (missionEconomyArea != null) missionEconomyArea.setText(snapshot.economy);
        if (missionOperationsArea != null) missionOperationsArea.setText(snapshot.operations);
        if (missionInnArea != null) missionInnArea.setText(snapshot.inn);
        if (missionStaffArea != null) missionStaffArea.setText(snapshot.staff);
        if (missionRiskArea != null) missionRiskArea.setText(snapshot.risk);
        if (missionReputationArea != null) missionReputationArea.setText(snapshot.reputationIdentity);
        if (missionRumorsArea != null) missionRumorsArea.setText(snapshot.rumors);
        if (missionTrafficArea != null) missionTrafficArea.setText(snapshot.trafficPunters);
        if (missionInventoryArea != null) missionInventoryArea.setText(buildInventoryPanelText());
        if (missionLoansArea != null) missionLoansArea.setText(snapshot.loans);
        if (missionPrestigeArea != null) missionPrestigeArea.setText(snapshot.prestige);
        if (missionMusicArea != null) missionMusicArea.setText(snapshot.music);
        if (prestigePreviewButton != null) {
            prestigePreviewButton.setEnabled(sim.isPrestigeAvailable());
        }
        if (missionLogArea != null && (missionLogArea.getText() == null || missionLogArea.getText().isBlank())) {
            missionLogArea.setText(snapshot.logEvents + "\n");
        }
    }

    private void showPrestigePreviewDialog() {
        PrestigeSystem.PrestigePreview preview = sim.buildPrestigePreview();
        if (preview == null) return;
        if (prestigeDialog == null) {
            prestigeDialog = new JDialog(frame, "Prestige Preview", true);
            prestigeDialog.setLayout(new BorderLayout(8, 8));
            prestigeDialogArea = new JTextArea(20, 60);
            prestigeDialogArea.setEditable(false);
            prestigeDialogArea.setLineWrap(true);
            prestigeDialogArea.setWrapStyleWord(true);
            prestigeDialogArea.setFont(UIManager.getFont("TextArea.font"));
            prestigeDialog.add(new JScrollPane(prestigeDialogArea), BorderLayout.CENTER);

            prestigeConfirmButton = new JButton("Confirm Prestige");
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(e -> prestigeDialog.setVisible(false));
            prestigeConfirmButton.addActionListener(e -> {
                boolean applied = sim.confirmPrestige();
                if (applied) {
                    prestigeDialog.setVisible(false);
                    refreshAll();
                } else {
                    prestigeDialog.setVisible(false);
                }
            });
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(prestigeConfirmButton);
            bottom.add(cancel);
            prestigeDialog.add(bottom, BorderLayout.SOUTH);
            prestigeDialog.setSize(760, 520);
            prestigeDialog.setLocationRelativeTo(frame);
        }

        prestigeDialog.setTitle(preview.title());
        prestigeDialogArea.setText(preview.body());
        prestigeConfirmButton.setEnabled(preview.eligible() && !preview.maxed());
        prestigeDialog.setVisible(true);
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
        }
        if (state.paydayReady) {
            openPaydayDialog();
            state.paydayReady = false;
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
            weeklyReportArea.setFont(UIManager.getFont("TextArea.font"));

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
            fourWeekReportArea.setFont(UIManager.getFont("TextArea.font"));

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
        refreshInnWindow();
        updateMoodLighting();
        checkReportPopups();
        refreshActionsDialog();
        if (sim.isPrestigeAvailable() && lastPrestigePreviewStar != state.starCount) {
            showPrestigePreviewDialog();
            lastPrestigePreviewStar = state.starCount;
        }
    }

    private void refreshActionsDialog() {
        if (actionsDialog != null && actionsDialog.isVisible() && actionsPanel != null) {
            actionsPanel.refresh();
        }
    }

    private void updateHud(MetricsSnapshot snapshot) {
        if (snapshot == null) return;
        boolean cashIncreased = state.cash > lastCash;
        double currentDebt = state.totalCreditBalance()
                + (state.loanShark.isOpen() ? state.loanShark.getBalance() : 0.0);
        boolean debtIncreased = currentDebt > lastDebt;

        cashLabel.setText("Cash: " + money2(state.cash));
        debtLabel.setText("Debt: " + money2(currentDebt));
        pubNameLabel.setText(sim.pubNameBadgeHtml());
        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        invoiceDueLabel.setText("<html>Weekly Costs (Due at Payday): " + money2(due.total())
                + "<br/>Supplier " + money2(due.supplier())
                + " | Wages " + money2(due.wages())
                + " | Rent " + money2(due.rent())
                + "<br/>Security " + money2(due.security())
                + " | Inn " + money2(due.innMaintenance())
                + " | Credit " + money2(due.creditLines())
                + " | Shark " + money2(due.loanShark())
                + "</html>");

        String mood =
                (state.reputation >= 60) ? " Loved" :
                        (state.reputation >= 20) ? " Solid" :
                                (state.reputation >= -20) ? " Shaky" :
                                        (state.reputation >= -60) ? " Bad" :
                                                " Toxic";

        repLabel.setText(buildReputationBadgeText(mood));
        calendarLabel.setText("<html>Week " + state.weekCount + "  " + state.dayName()
                + " | Night " + state.nightCount
                + "<br/>Date " + state.dateString()
                + "<br/>Weather " + state.weatherLabel()
                + "</html>");

        int cap = sim.peekServeCapacity();

        String closedSuffix = state.lastEarlyCloseRepPenalty < 0
                ? (" | Last early close " + state.lastEarlyCloseRepPenalty + " rep")
                : "";
        timePhaseLabel.setText("Time: " + state.getCurrentTime() + " | Phase: " + state.getCurrentPhase() + " | Music: " + state.currentMusicProfile.getLabel());
        roundLabel.setText(state.nightOpen
                ? ("Night OPEN  Round " + state.roundInNight + "/" + state.closingRound
                + " | Bar " + state.nightPunters.size() + "/" + state.maxBarOccupancy)
                : ("Night CLOSED  Ready" + closedSuffix));

        SecuritySystem.SecurityBreakdown breakdown = sim.securityBreakdown();
        int sec = breakdown.total();
        String policyShort = state.securityPolicy != null ? state.securityPolicy.getShortLabel() : "B";
        String taskShort = state.activeSecurityTask != null ? state.activeSecurityTask.getShortLabel() : "None";
        if (state.activeSecurityTask != null && state.isSecurityTaskQueued() && !state.isSecurityTaskActive()) {
            taskShort = taskShort + "*";
        }
        String bouncerInfo = "Bouncers: " + state.bouncersHiredTonight + "/" + state.bouncerCap;
        String mitigationInfo = "Rep x" + String.format("%.2f", state.securityIncidentRepMultiplier());
        securityLabel.setText(buildSecurityBadgeText(sec, policyShort, taskShort, bouncerInfo, mitigationInfo, state.chaos));

        staffLabel.setText(buildStaffBadgeText(cap));
        reportLabel.setText("Report: " + state.reports().summaryLine());
        String forecastLine = state.trafficForecastLine != null ? state.trafficForecastLine : "Forecast: 0–0 tonight";
        String topSalesLine = state.topSalesForecastLine != null
                ? state.topSalesForecastLine
                : "Top sellers (5r): Wine None | Food None";

        // OBS box = traffic + forecast + top sellers
        observationLabel.setText("<html>🚶 In: " + state.lastTrafficIn + " | Out: " + state.lastTrafficOut
                + " (natural " + state.lastNaturalDepartures + ")"
                + "<br>📈 " + forecastLine
                + "<br>🏆 " + topSalesLine + "</html>");

        // Middle grey box = quips only (no serve cap here)
        String quipLine = (state.observationLine != null && !state.observationLine.isBlank())
                ? state.observationLine
                : "";
        serveCapLabel.setText(quipLine.isEmpty() ? " " : "<html>" + quipLine + "</html>");


        boolean emergencySupplierAllowed = state.canEmergencyRestock();
        supplierBtn.setEnabled(!state.nightOpen || emergencySupplierAllowed);
        boolean emergencyFoodAllowed = state.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1;
        boolean kitchenSupplierEnabled = canUseKitchen() && (!state.nightOpen || emergencyFoodAllowed);
        kitchenSupplierBtn.setEnabled(kitchenSupplierEnabled);
        if (!canUseKitchen()) {
            kitchenSupplierBtn.setToolTipText("Kitchen not installed (requires Kitchen upgrade)");
        } else if (state.nightOpen && !emergencyFoodAllowed) {
            kitchenSupplierBtn.setToolTipText("Requires Head Chef");
        } else {
            kitchenSupplierBtn.setToolTipText(null);
        }
        happyHourBtn.setEnabled(state.nightOpen);
        musicProfileBox.setSelectedItem(state.currentMusicProfile);
        musicProfileBox.setToolTipText(sim.currentMusicTooltip());

        lastCash = state.cash;
        lastDebt = currentDebt;

        if (cashIncreased) {
            flashBadge(cashBadge, FLASH_GREEN, CASH_BG, true);
        }
        if (debtIncreased) {
            flashBadge(debtBadge, FLASH_RED, DEBT_BG, false);
        }

        updateNightPulse();

    }

    private String buildReputationBadgeText(String mood) {
        String identity = formatIdentityLabel(state.pubIdentity);
        RumorInstance featuredRumor = findFeaturedRumor();
        String rumorLine = featuredRumor != null ? featuredRumor.type().getLabel() : "None";
        return "<html>Reputation: " + state.reputation + " (" + mood + ")"
                + "<br>Identity: " + identity
                + "<br>Rumor: " + rumorLine + "</html>";
    }

    private RumorInstance findFeaturedRumor() {
        RumorInstance featured = null;
        for (RumorInstance rumor : state.activeRumors.values()) {
            if (featured == null) {
                featured = rumor;
                continue;
            }
            // No timestamp exists, so choose the strongest active rumor by intensity (tie: longer remaining).
            if (rumor.intensity() > featured.intensity()
                    || (rumor.intensity() == featured.intensity() && rumor.daysRemaining() > featured.daysRemaining())) {
                featured = rumor;
            }
        }
        return featured;
    }

    private String formatIdentityLabel(PubIdentity identity) {
        if (identity == null) return "Unknown";
        String[] parts = identity.getDescriptor().split(" ");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (label.length() > 0) label.append(' ');
            if (part.isBlank()) continue;
            label.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return label.toString();
    }

    private String buildStaffBadgeText(int serveCap) {
        GameState.StaffSummary summary = state.staff();
        int combinedCap = state.fohStaffCap + state.hohStaffCap + state.kitchenChefCap;
        String staffLine = "Staff: " + summary.staffCount() + "/" + combinedCap
                + " | Managers: " + summary.managerPoolCount() + "/" + summary.managerCap()
                + " (GM " + summary.managerCount() + ", AM " + summary.assistantManagerCount()
                + ", DM " + summary.dutyManagerCount() + ")"
                + (summary.bouncersTonight() > 0 ? " | Bouncer: " + summary.bouncersTonight() + "/" + summary.bouncerCap() : "")
                + " | Morale: " + (int)Math.round(summary.teamMorale())
                + " | Upgrades: " + summary.upgradesOwned()
                + (summary.activityTonight() != null ? " | Activity: " + summary.activityTonight() : "")
                + " | Serve cap " + serveCap;
        String staffCounts = "FOH: " + state.fohStaffCount() + "/" + state.fohStaffCap
                + " | HOH: " + state.hohStaffCount() + "/" + state.hohStaffCap
                + " | BOH: " + state.bohStaff.size() + "/" + state.kitchenChefCap;
        return "<html>" + staffLine + "<br>" + staffCounts + "</html>";
    }

    static String buildSecurityBadgeText(int sec,
                                         String policyShort,
                                         String taskShort,
                                         String bouncerInfo,
                                         String mitigationInfo) {
        return buildSecurityBadgeText(sec, policyShort, taskShort, bouncerInfo, mitigationInfo, 0.0);
    }

    static String buildSecurityBadgeText(int sec,
                                         String policyShort,
                                         String taskShort,
                                         String bouncerInfo,
                                         String mitigationInfo,
                                         double chaos) {
        String policyLine = "Policy: " + policyShort + " | Task: " + taskShort + " | Sec " + sec + " | Chaos " + String.format("%.1f", chaos);
        return "<html>" + policyLine + "<br>" + bouncerInfo + " | " + mitigationInfo + "</html>";
    }

    private boolean canUseKitchen() {
        return state.kitchenUnlocked;
    }

    private String buildInventoryPanelText() {
        return String.join("\n", buildInventoryPanelLines());
    }

    private List<String> buildInventoryPanelLines() {
        List<String> lines = new java.util.ArrayList<>();
        int totalItems = state.rack.count() + (state.kitchenUnlocked ? state.foodRack.count() : 0);
        lines.add("Total items: " + totalItems);

        lines.add("=== Wine ===");
        Map<String, Integer> counts = state.rack.inventoryCounts();
        if (counts.isEmpty()) {
            lines.add("(empty)");
        } else {
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                lines.add(e.getKey() + " x" + e.getValue());
            }
        }
        lines.add("Total: " + state.rack.count() + "/" + state.rack.getCapacity());

        lines.add(" ");
        lines.add("=== Food ===");
        if (state.kitchenUnlocked) {
            Map<String, Integer> foodCounts = state.foodRack.inventoryCounts();
            if (foodCounts.isEmpty()) {
                lines.add("(empty)");
            } else {
                for (Map.Entry<String, Integer> e : foodCounts.entrySet()) {
                    lines.add(e.getKey() + " x" + e.getValue());
                }
            }
            lines.add("Total: " + state.foodRack.count() + "/" + state.foodRack.getCapacity());
        } else {
            lines.add("Kitchen not installed (requires Kitchen upgrade)");
        }

        lines.add(" ");
        lines.add("=== Spoilage forecast ===");
        List<String> spoilageLines = buildSpoilageForecastLines();
        if (spoilageLines.isEmpty()) {
            lines.add("No spoilage risk.");
        } else {
            lines.addAll(spoilageLines);
        }

        return lines;
    }

    private List<String> buildSpoilageForecastLines() {
        List<String> lines = new java.util.ArrayList<>();
        List<WineRack.SpoilageLine> wineSpoilage = state.rack.spoilageForecast(state.absDayIndex());
        for (WineRack.SpoilageLine line : wineSpoilage) {
            lines.add(line.wineName() + " x" + line.count() + " - spoil " + formatSpoilageDays(line.daysRemaining()));
        }
        if (state.kitchenUnlocked) {
            List<FoodRack.SpoilageLine> foodSpoilage = state.foodRack.spoilageForecast(state.absDayIndex());
            for (FoodRack.SpoilageLine line : foodSpoilage) {
                lines.add(line.foodName() + " x" + line.count() + " - spoil " + formatSpoilageDays(line.daysRemaining()));
            }
        }
        return lines;
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
        for (String line : buildInventoryPanelLines()) {
            invModel.addElement(line);
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
        refreshReportsDialog();
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

    private record SupplierInvoiceControls(
            SupplierAccount account,
            JLabel summaryLabel,
            JLabel statusLabel,
            JSpinner amountSpinner,
            JButton payFullBtn,
            JButton payCustomBtn,
            JComboBox<PaymentSourceOption> sourceBox
    ) {}

    private record PaymentSourceOption(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record PaydayBillRow(
            PaydayBill bill,
            JRadioButton minBtn,
            JRadioButton fullBtn,
            JRadioButton customBtn,
            JSpinner amountSpinner,
            JComboBox<PaymentSourceOption> sourceBox,
            JLabel statusLabel
    ) {}
}
