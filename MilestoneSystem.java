public class MilestoneSystem {

    public enum Milestone {
        FIRST_NIGHT,
        FIVE_NIGHTS,
        TEN_NIGHTS,
        CASH_STACK,
        REP_STAR,
        REP_PEAK_90,
        ZERO_DEBT_WEEK,
        PERFECT_WEEK,
        PROFIT_STREAK_2,
        PROFIT_STREAK_4,
        KITCHEN_LAUNCH,
        ACTIVITY_UNLOCK
    }

    private final GameState s;
    private final UILogger log;
    private boolean applyingReward = false;

    public MilestoneSystem(GameState s, UILogger log) {
        this.s = s;
        this.log = log;
    }

    public void onRepChanged() {
        if (applyingReward) return;
        if (s.reputation >= 70) {
            unlockActivity(PubActivity.BREWERY_TAKEOVER,
                    Milestone.ACTIVITY_UNLOCK,
                    "Community darling",
                    "Unlocked activity: Brewery Takeover.");
        }
        if (s.reputation >= 80) {
            grantMilestone(Milestone.REP_STAR,
                    "Reputation star",
                    "Unlock: Door Team II available.",
                    null);
        }
    }

    public void onNightEnd() {
        unlockActivity(PubActivity.OPEN_MIC,
                Milestone.FIRST_NIGHT,
                "First night survived",
                "Unlocked activity: Open Mic.");

        if (s.nightCount >= 5) {
            unlockActivity(PubActivity.KARAOKE,
                    Milestone.FIVE_NIGHTS,
                    "Five nights open",
                    "Unlocked activity: Karaoke.");
        }

        if (s.nightCount >= 10) {
            unlockActivity(PubActivity.CHARITY_NIGHT,
                    Milestone.TEN_NIGHTS,
                    "Ten nights open",
                    "Unlocked activity: Charity Night.");
        }

        if (s.totalCashEarned >= 500) {
            unlockActivity(PubActivity.COCKTAIL_PROMO,
                    Milestone.CASH_STACK,
                    "Cash flow milestone",
                    "Unlocked activity: Cocktail Promo.");
        }

        if (s.peakReputation >= 90) {
            grantMilestone(Milestone.REP_PEAK_90,
                    "Reputation peak 90",
                    "Unlock: prestige options.",
                    null);
        }
    }
    

    public void onWeekEnd() {
        if (s.profitStreakWeeks >= 2) {
            grantMilestone(Milestone.PROFIT_STREAK_2,
                    "Profitable streak (2 weeks)",
                    "Unlock: Kitchen Staffing II upgrades.",
                    null);
        }

        if (s.profitStreakWeeks >= 4) {
            grantMilestone(Milestone.PROFIT_STREAK_4,
                    "Profitable streak (4 weeks)",
                    "Unlock: high-tier upgrades.",
                    null);
        }

        if (s.debt <= 0.0) {
            grantMilestone(Milestone.ZERO_DEBT_WEEK,
                    "Zero-debt week",
                    "Unlocked: debt-free reputation perks.",
                    null);
        }

        if (s.debt <= 0.0 && s.fightsThisWeek == 0 && s.reputation > 0) {
            grantMilestone(Milestone.PERFECT_WEEK,
                    "Perfect week",
                    "Unlocked: special activity options.",
                    null);
        }

        if (s.kitchenUnlocked) {
            unlockActivity(PubActivity.FAMILY_LUNCH,
                    Milestone.KITCHEN_LAUNCH,
                    "Kitchen launch",
                    "Unlocked activity: Family Lunch.");
        }
    }

    private void unlockActivity(PubActivity activity, Milestone milestone, String title, String rewardText) {
        if (s.unlockedActivities.contains(activity)) return;
        grantMilestone(milestone, title, rewardText, () -> s.unlockedActivities.add(activity));
    }

    private void grantMilestone(Milestone milestone, String title, String rewardText, Runnable reward) {
        if (s.achievedMilestones.contains(milestone)) return;
        s.achievedMilestones.add(milestone);
        applyingReward = true;
        try {
            if (reward != null) reward.run();
        } finally {
            applyingReward = false;
        }

        String msg = " " + title + "\n" + rewardText;
        s.milestonePopups.add(msg);
        log.event(" Milestone: " + title + " - " + rewardText);
    }

    public boolean canBuyUpgrade(PubUpgrade upgrade) {
        if (upgrade.getTier() > 1 && s.pubLevel < upgrade.getTier() - 1) {
            return false;
        }
        if (upgrade.getChainKey() != null && upgrade.getTier() > 1) {
            boolean hasPrev = false;
            for (PubUpgrade owned : s.ownedUpgrades) {
                if (upgrade.getChainKey().equals(owned.getChainKey())
                        && owned.getTier() == upgrade.getTier() - 1) {
                    hasPrev = true;
                    break;
                }
            }
            if (!hasPrev) return false;
        }
        if (upgrade == PubUpgrade.DOOR_TEAM_II) {
            return s.achievedMilestones.contains(Milestone.REP_STAR);
        }
        if (upgrade == PubUpgrade.CCTV) {
            return s.achievedMilestones.contains(Milestone.CASH_STACK);
        }
        if (upgrade == PubUpgrade.STAFF_ROOM_II) {
            return s.achievedMilestones.contains(Milestone.FIVE_NIGHTS);
        }
        if (upgrade == PubUpgrade.KITCHEN_EQUIPMENT) {
            return s.achievedMilestones.contains(Milestone.KITCHEN_LAUNCH);
        }
        if (upgrade == PubUpgrade.HYGIENE_TRAINING) {
            return s.achievedMilestones.contains(Milestone.KITCHEN_LAUNCH);
        }
        if (upgrade == PubUpgrade.KITCHEN_STAFFING_II) {
            return s.achievedMilestones.contains(Milestone.PROFIT_STREAK_2);
        }
        return true;
    }

    public boolean isActivityUnlocked(PubActivity activity) {
        return !activity.requiresUnlock() || s.unlockedActivities.contains(activity);
    }
}
