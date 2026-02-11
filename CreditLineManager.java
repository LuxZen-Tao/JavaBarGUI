import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreditLineManager  implements java.io.Serializable {
    private static final double MIN_WEEKLY_PAYMENT = 25.0;
    private static final double WEEKLY_PAYMENT_PCT = 0.05;
    private final List<CreditLine> openLines = new ArrayList<>();
    private CreditLine lastAppliedLine;

    public List<CreditLine> getOpenLines() {
        return Collections.unmodifiableList(openLines);
    }

    public double totalBalance() {
        double total = 0.0;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            total += Math.max(0.0, line.getBalance());
        }
        return total;
    }

    public double totalLimit() {
        double total = 0.0;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            total += Math.max(0.0, line.getLimit());
        }
        return total;
    }

    public double totalWeeklyPaymentDue() {
        double total = 0.0;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            if (line.getBalance() <= 0.0) continue;
            total += Math.max(0.0, line.getWeeklyPayment());
        }
        return total;
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
        return applyCredit(amount, null);
    }

    public boolean applyCredit(double amount, String preferredLineId) {
        if (amount <= 0) return true;
        CreditLine line = selectLineForCredit(amount, preferredLineId);
        if (line == null) return false;
        line.addBalance(amount);
        updateWeeklyPayment(line);
        lastAppliedLine = line;
        return true;
    }

    private CreditLine selectLineForCredit(double amount, String preferredLineId) {
        CreditLine preferred = getLineById(preferredLineId);
        if (preferred != null && preferred.isEnabled() && preferred.availableCredit() >= amount) {
            return preferred;
        }

        CreditLine best = null;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            if (line.availableCredit() < amount) continue;
            if (best == null) {
                best = line;
                continue;
            }
            double apr = line.getInterestAPR() + line.getPenaltyAddOnApr();
            double bestApr = best.getInterestAPR() + best.getPenaltyAddOnApr();
            if (apr < bestApr) {
                best = line;
            } else if (Math.abs(apr - bestApr) < 0.000001
                    && line.availableCredit() > best.availableCredit()) {
                best = line;
            }
        }
        return best;
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

    public CreditLine getLineByName(String name) {
        if (name == null) return null;
        for (CreditLine line : openLines) {
            if (name.equals(line.getLenderName())) return line;
        }
        return null;
    }

    public void addBalanceToLine(CreditLine line, double amount) {
        if (line == null || amount <= 0.0) return;
        line.addBalance(amount);
        updateWeeklyPayment(line);
        lastAppliedLine = line;
    }

    public CreditLine consumeLastAppliedLine() {
        CreditLine line = lastAppliedLine;
        lastAppliedLine = null;
        return line;
    }

    public void applyWeeklyInterest(GameState s, UILogger log) {
        double totalLimit = 0.0;
        double totalBalance = 0.0;
        int openCount = 0;
        for (CreditLine line : openLines) {
            if (!line.isEnabled()) continue;
            openCount++;
            totalLimit += line.getLimit();
            totalBalance += line.getBalance();
            double balance = line.getBalance();
            if (balance > 0.0) {
                double interest = balance * ((line.getInterestAPR() + line.getPenaltyAddOnApr()) / 52.0);
                if (interest > 0.0) {
                    line.addBalance(interest);
                    log.info("Credit line interest: " + line.getLenderName()
                            + " +" + fmt(interest) + " (APR "
                            + fmtPct(line.getInterestAPR() + line.getPenaltyAddOnApr()) + "%)");
                }
                updateWeeklyPayment(line);
            }
            if (line.getBalance() <= 0.0) {
                line.applyPayment(line.getBalance());
                line.setWeeklyPayment(0.0);
            }
        }

        s.creditUtilization = totalLimit > 0.0 ? (totalBalance / totalLimit) : 0.0;

        if (s.creditUtilization > 0.80 && totalLimit > 0.0) {
            adjustCreditScore(s, -5, "High credit utilization");
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

    public void updateWeeklyPayment(CreditLine line) {
        if (line.getBalance() <= 0.0) {
            line.setWeeklyPayment(0.0);
            return;
        }
        double min = MIN_WEEKLY_PAYMENT;
        double pct = WEEKLY_PAYMENT_PCT;
        double target = Math.max(min, line.getBalance() * pct);
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
        int beforeScore = s.creditScore;
        String beforeTrust = s.supplierTrustLabel();

        s.creditScore = s.clampCreditScore(beforeScore + delta);
        if (s.creditScore == beforeScore) return;

        double penalty = s.supplierTrustPenalty;
        double magnitude = Math.min(4, Math.abs(delta));
        if (delta > 0) {
            penalty -= 0.005 * magnitude;
        } else {
            penalty += 0.004 * magnitude;
        }

        String afterTrust = s.supplierTrustLabel();
        if (!beforeTrust.equals(afterTrust)) {
            if ("Good".equals(afterTrust)) penalty -= 0.01;
            else if ("Very Poor".equals(afterTrust)) penalty += 0.01;
            else if ("Poor".equals(afterTrust) && "Neutral".equals(beforeTrust)) penalty += 0.005;
            else if ("Neutral".equals(afterTrust) && "Poor".equals(beforeTrust)) penalty -= 0.005;
            s.supplierTrustStatus = afterTrust;
        }

        s.supplierTrustPenalty = Math.max(0.0, Math.min(0.30, penalty));
    }

}
