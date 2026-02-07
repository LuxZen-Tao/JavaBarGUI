import java.util.List;

public class MetricsSnapshot {
    public final String hudCash;
    public final String hudDebt;
    public final String hudRep;
    public final String hudPubName;
    public final String hudInvoice;
    public final String hudCalendar;
    public final String hudRound;
    public final String hudSecurity;
    public final String hudStaff;
    public final String hudReport;
    public final String hudServeCap;
    public final List<String> overviewLines;
    public final String economy;
    public final String operations;
    public final String staff;
    public final String risk;
    public final String reputationIdentity;
    public final String rumors;
    public final String trafficPunters;
    public final String inventory;
    public final String loans;
    public final String logEvents;

    public MetricsSnapshot(
            String hudCash,
            String hudDebt,
            String hudRep,
            String hudPubName,
            String hudInvoice,
            String hudCalendar,
            String hudRound,
            String hudSecurity,
            String hudStaff,
            String hudReport,
            String hudServeCap,
            List<String> overviewLines,
            String economy,
            String operations,
            String staff,
            String risk,
            String reputationIdentity,
            String rumors,
            String trafficPunters,
            String inventory,
            String loans,
            String logEvents
    ) {
        this.hudCash = hudCash;
        this.hudDebt = hudDebt;
        this.hudRep = hudRep;
        this.hudPubName = hudPubName;
        this.hudInvoice = hudInvoice;
        this.hudCalendar = hudCalendar;
        this.hudRound = hudRound;
        this.hudSecurity = hudSecurity;
        this.hudStaff = hudStaff;
        this.hudReport = hudReport;
        this.hudServeCap = hudServeCap;
        this.overviewLines = overviewLines;
        this.economy = economy;
        this.operations = operations;
        this.staff = staff;
        this.risk = risk;
        this.reputationIdentity = reputationIdentity;
        this.rumors = rumors;
        this.trafficPunters = trafficPunters;
        this.inventory = inventory;
        this.loans = loans;
        this.logEvents = logEvents;
    }
}
