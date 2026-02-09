import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreditLineManager {
    private static final double MIN_WEEKLY_PAYMENT = 25.0;
    private static final double WEEKLY_PAYMENT_PCT = 0.05;
    private final List<CreditLine> openLines = new ArrayList<>();

    public List<CreditLine> getOpenLines() {
        return Collections.unmodifiableList(openLines);
    }

    public boolean hasAvailableCredit(double amount) {
        if (amount <= 0) return true;
        for (CreditLine line : openLines) {
            if (line.isEnabled() && line.availableCredit() >= amount) {
                return true;
            }
        }
        return false;
    }

    public boolean applyCredit(double amount) {
        if (amount <= 0) return true;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            if (line.availableCredit() >= amount) {
                line.addBalance(amount);
                updateWeeklyPayment(line);
                // TODO: Enable player choice for selecting credit line in Chunk 4.
                return true;
            }
        }
        return false;
    }

    public CreditLine openLine(Bank bank, java.util.Random random) {
        if (bank == null || random == null) return null;
        if (hasLine(bank.getName())) return null;
        double limit = bank.rollLimit(random);
        double apr = bank.rollApr(random);
        CreditLine line = new CreditLine(
                java.util.UUID.randomUUID().toString(),
                bank.getName(),
                limit,
                0.0,
                apr,
                0.0,
                true
        );
        openLines.add(line);
        return line;
    }

    public boolean hasLine(String lenderName) {
        if (lenderName == null) return false;
        for (CreditLine line : openLines) {
            if (lenderName.equals(line.getLenderName())) return true;
        }
        return false;
    }

    public CreditLine getLineById(String id) {
        if (id == null) return null;
        for (CreditLine line : openLines) {
            if (id.equals(line.getId())) return line;
        }
        return null;
    }

    public void processWeekly(GameState s, UILogger log) {
        double totalLimit = 0.0;
        double totalBalance = 0.0;
        boolean missedPayment = false;
        boolean paidAllOnTime = true;
        int openCount = 0;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            openCount++;
            totalLimit += line.getLimit();
            totalBalance += line.getBalance();
            double balance = line.getBalance();
            if (balance > 0.0) {
                double interest = balance * (line.getInterestAPR() / 52.0);
                if (interest > 0.0) {
                    line.addBalance(interest);
                    log.info("Credit line interest: " + line.getLenderName()
                            + " +" + fmt(interest) + " (APR "
                            + fmtPct(line.getInterestAPR()) + "%)");
                }
                updateWeeklyPayment(line);
                double due = line.getWeeklyPayment();
                if (due > 0.0) {
                    if (s.cash >= due) {
                        s.cash -= due;
                        line.applyPayment(due);
                        log.info("Credit line payment: " + line.getLenderName()
                                + " GBP " + fmt(due)
                                + " | balance now GBP " + fmt(line.getBalance()));
                    } else {
                        line.markMissedPayment();
                        missedPayment = true;
                        paidAllOnTime = false;
                        log.neg("Missed credit line payment: " + line.getLenderName()
                                + " GBP " + fmt(due) + " due.");
                    }
                }
            }
            if (line.getBalance() <= 0.0) {
                line.applyPayment(line.getBalance());
                line.setWeeklyPayment(0.0);
            }
        }

        s.creditUtilization = totalLimit > 0.0 ? (totalBalance / totalLimit) : 0.0;

        if (missedPayment) {
            adjustCreditScore(s, -30, "Missed credit payment");
        }

        if (s.creditUtilization > 0.80 && totalLimit > 0.0) {
            adjustCreditScore(s, -5, "High credit utilization");
        }

        if (paidAllOnTime && totalBalance > 0.0) {
            adjustCreditScore(s, 4, "On-time credit repayments");
        }

        if (openCount > 0 && totalBalance <= 0.0) {
            adjustCreditScore(s, 1, "Zero balance credit lines");
        }

        if (totalBalance <= 0.0) {
            s.noDebtUsageWeeks++;
            if (s.noDebtUsageWeeks >= 2) {
                int bonus = Math.min(3, s.noDebtUsageWeeks - 1);
                adjustCreditScore(s, bonus, "No credit usage streak");
            }
        } else {
            s.noDebtUsageWeeks = 0;
        }

        String trust = s.supplierTrustLabel();
        if (!trust.equals(s.supplierTrustStatus)) {
            s.supplierTrustStatus = trust;
            if ("Good".equals(trust)) {
                log.pos("Suppliers offer better terms due to strong credit.");
            } else if ("Poor".equals(trust) || "Very Poor".equals(trust)) {
                log.neg("Suppliers tighten prices due to poor credit.");
            }
        }
    }

    public void repayInFull(GameState s, CreditLine line, UILogger log) {
        if (line == null || s == null) return;
        double due = line.getBalance();
        if (due <= 0.0) return;
        if (s.cash < due) {
            if (log != null) {
                log.neg("Not enough cash to repay " + line.getLenderName()
                        + " in full. Need GBP " + fmt(due));
            }
            return;
        }
        s.cash -= due;
        line.applyPayment(due);
        line.setWeeklyPayment(0.0);
        line.resetMissedPayments();
        if (log != null) {
            log.pos("Repaid credit line in full: " + line.getLenderName()
                    + " GBP " + fmt(due));
        }
    }

    private void updateWeeklyPayment(CreditLine line) {
        if (line.getBalance() <= 0.0) {
            line.setWeeklyPayment(0.0);
            return;
        }
        double target = Math.max(MIN_WEEKLY_PAYMENT, line.getBalance() * WEEKLY_PAYMENT_PCT);
        line.setWeeklyPayment(target);
    }

    private static String fmt(double value) {
        return String.format("%.2f", value);
    }

    private static String fmtPct(double value) {
        return String.format("%.2f", value * 100.0);
    }

    private void adjustCreditScore(GameState s, int delta, String reason) {
        if (s == null || delta == 0) return;
        int before = s.creditScore;
        s.creditScore = s.clampCreditScore(before + delta);
        if (s.creditScore != before) {
            // TODO: Expand credit score effects in Chunk 4 (suppliers, events).
        }
    }
}
