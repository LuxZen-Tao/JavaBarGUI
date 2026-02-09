public class MilestoneSystem {

    public enum Milestone {
        FIRST_NIGHT,
        FIVE_NIGHTS,
        TEN_NIGHTS,
        CASH_STACK,
        REP_STAR,
        LOCAL_FAVOURITE,
        KNOWN_VENUE,
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
        if (s.reputation >= 50) {
            unlockActivity(PubActivity.QUIZ_NIGHT,
                    Milestone.LOCAL_FAVOURITE,
                    "Local favourite",
                    "Reputation 50+",
                    "Unlocked activity: Quiz Night.");
        }
        if (s.reputation >= 65) {
            unlockActivity(PubActivity.OPEN_MIC,
                    Milestone.KNOWN_VENUE,
                    "Known venue",
                    "Reputation 65+",
                    "Unlocked activity: Open Mic.");
        }
        if (s.reputation >= 70) {
            unlockActivity(PubActivity.BREWERY_TAKEOVER,
                    Milestone.ACTIVITY_UNLOCK,
                    "Community darling",
                    "Reputation 70+",
                    "Unlocked activity: Brewery Takeover.");
        }
        if (s.reputation >= 80) {
            grantMilestone(Milestone.REP_STAR,
                    "Reputation star",
                    "Reputation 80+",
                    "Reward: cash bonus +150.",
                    () -> grantCashBonus(150, "Reputation star"));
        }
    }

    public void onNightEnd() {
        grantMilestone(Milestone.FIRST_NIGHT,
                "First night survived",
                "Complete 1 night",
                "Reward: morale +1 for all staff.",
                () -> applyTeamMoraleBoost(1));

        if (s.nightCount >= 5) {
            unlockActivity(PubActivity.KARAOKE,
                    Milestone.FIVE_NIGHTS,
                    "Five nights open",
                    "Complete 5 nights",
                    "Unlocked activity: Karaoke.");
        }

        if (s.nightCount >= 10) {
            unlockActivity(PubActivity.CHARITY_NIGHT,
                    Milestone.TEN_NIGHTS,
                    "Ten nights open",
                    "Complete 10 nights",
                    "Unlocked activity: Charity Night.");
        }

        if (s.totalCashEarned >= 500) {
            unlockActivity(PubActivity.COCKTAIL_PROMO,
                    Milestone.CASH_STACK,
                    "Cash flow milestone",
                    "Earn GBP 500 total",
                    "Unlocked activity: Cocktail Promo.");
        }

        if (s.peakReputation >= 90) {
            grantMilestone(Milestone.REP_PEAK_90,
                    "Reputation peak 90",
                    "Reach reputation 90",
                    "Reward: chaos -10.",
                    () -> reduceChaos(10, "Reputation peak"));
        }
    }
    

    public void onWeekEnd() {
        if (s.profitStreakWeeks >= 2) {
            grantMilestone(Milestone.PROFIT_STREAK_2,
                    "Profitable streak (2 weeks)",
                    "Profit for 2 consecutive weeks",
                    "Reward: cash bonus +120.",
                    () -> grantCashBonus(120, "Profit streak"));
        }

        if (s.profitStreakWeeks >= 4) {
            grantMilestone(Milestone.PROFIT_STREAK_4,
                    "Profitable streak (4 weeks)",
                    "Profit for 4 consecutive weeks",
                    "Reward: skilled staff hire.",
                    this::grantSkilledStaffHire);
        }

        if (s.totalCreditBalance() <= 0.0) {
            grantMilestone(Milestone.ZERO_DEBT_WEEK,
                    "Zero-debt week",
                    "Finish a week with zero debt",
                    "Reward: chaos -8.",
                    () -> reduceChaos(8, "Zero-debt week"));
        }

        if (s.totalCreditBalance() <= 0.0 && s.fightsThisWeek == 0 && s.reputation > 0) {
            grantMilestone(Milestone.PERFECT_WEEK,
                    "Perfect week",
                    "Zero debt + no fights + positive rep",
                    "Reward: cash bonus +200 and morale +2.",
                    () -> {
                        grantCashBonus(200, "Perfect week");
                        applyTeamMoraleBoost(2);
                    });
        }

        if (s.kitchenUnlocked) {
            unlockActivity(PubActivity.FAMILY_LUNCH,
                    Milestone.KITCHEN_LAUNCH,
                    "Kitchen launch",
                    "Install Kitchen Base",
                    "Unlocked activity: Family Lunch.");
        }
    }

    private void unlockActivity(PubActivity activity,
                                Milestone milestone,
                                String title,
                                String requirementText,
                                String rewardText) {
        if (s.unlockedActivities.contains(activity)) return;
        grantMilestone(milestone, title, requirementText, rewardText, () -> s.unlockedActivities.add(activity));
    }

    private void grantMilestone(Milestone milestone,
                                String title,
                                String requirementText,
                                String rewardText,
                                Runnable reward) {
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
        recordMilestoneReward(title, requirementText, rewardText);
    }

    public boolean canBuyUpgrade(PubUpgrade upgrade) {
        if (upgrade.isKitchenRelated()
                && upgrade != PubUpgrade.KITCHEN_SETUP
                && !s.kitchenUnlocked) {
            return false;
        }
        if (upgrade == PubUpgrade.KITCHEN && !s.ownedUpgrades.contains(PubUpgrade.KITCHEN_SETUP)) {
            return false;
        }
        if (upgrade == PubUpgrade.NEW_KITCHEN_PLAN && !s.ownedUpgrades.contains(PubUpgrade.KITCHEN)) {
            return false;
        }
        if (upgrade == PubUpgrade.KITCHEN_EQUIPMENT && !s.ownedUpgrades.contains(PubUpgrade.NEW_KITCHEN_PLAN)) {
            return false;
        }
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
        if (upgrade == PubUpgrade.DOOR_TEAM_II || upgrade == PubUpgrade.DOOR_TEAM_III) {
            return s.achievedMilestones.contains(Milestone.REP_STAR);
        }
        if (upgrade == PubUpgrade.CCTV) {
            return s.achievedMilestones.contains(Milestone.CASH_STACK);
        }
        if (upgrade == PubUpgrade.STAFF_ROOM_II || upgrade == PubUpgrade.STAFF_ROOM_III) {
            return s.achievedMilestones.contains(Milestone.FIVE_NIGHTS);
        }
        if (upgrade == PubUpgrade.KITCHEN_EQUIPMENT) {
            return s.achievedMilestones.contains(Milestone.KITCHEN_LAUNCH);
        }
        if (upgrade == PubUpgrade.HYGIENE_TRAINING) {
            return s.achievedMilestones.contains(Milestone.KITCHEN_LAUNCH);
        }
        if (upgrade == PubUpgrade.KITCHEN_STAFFING_II || upgrade == PubUpgrade.KITCHEN_STAFFING_III) {
            return s.achievedMilestones.contains(Milestone.PROFIT_STREAK_2);
        }
        return true;
    }

    public String upgradeRequirementText(PubUpgrade upgrade) {
        if (s.ownedUpgrades.contains(upgrade)) return "Unlocked";
        if (upgrade.isKitchenRelated() && upgrade != PubUpgrade.KITCHEN_SETUP && !s.kitchenUnlocked) {
            return "Requires Kitchen Base";
        }
        if (upgrade == PubUpgrade.KITCHEN && !s.ownedUpgrades.contains(PubUpgrade.KITCHEN_SETUP)) {
            return "Requires Kitchen Base";
        }
        if (upgrade == PubUpgrade.NEW_KITCHEN_PLAN && !s.ownedUpgrades.contains(PubUpgrade.KITCHEN)) {
            return "Requires Kitchen Upgrade I";
        }
        if (upgrade == PubUpgrade.KITCHEN_EQUIPMENT && !s.ownedUpgrades.contains(PubUpgrade.NEW_KITCHEN_PLAN)) {
            return "Requires Kitchen Upgrade II";
        }
        if (upgrade.getTier() > 1 && s.pubLevel < upgrade.getTier() - 1) {
            return "Requires pub level " + (upgrade.getTier() - 1);
        }
        if (upgrade == PubUpgrade.DOOR_TEAM_II || upgrade == PubUpgrade.DOOR_TEAM_III) {
            return "Requires milestone: Reputation star";
        }
        if (upgrade == PubUpgrade.CCTV || upgrade == PubUpgrade.CCTV_PACKAGE) {
            return "Requires milestone: Cash flow";
        }
        if (upgrade == PubUpgrade.STAFF_ROOM_II || upgrade == PubUpgrade.STAFF_ROOM_III) {
            return "Requires milestone: Five nights open";
        }
        if (upgrade == PubUpgrade.KITCHEN_EQUIPMENT || upgrade == PubUpgrade.HYGIENE_TRAINING) {
            return "Requires milestone: Kitchen launch";
        }
        if (upgrade == PubUpgrade.KITCHEN_STAFFING_II || upgrade == PubUpgrade.KITCHEN_STAFFING_III) {
            return "Requires milestone: Profitable streak (2 weeks)";
        }
        if (upgrade.getChainKey() != null && upgrade.getTier() > 1) {
            return "Requires previous tier";
        }
        return null;
    }

    public String activityRequirementText(PubActivity activity) {
        if (activity.getRequiredUpgrade() != null && !s.ownedUpgrades.contains(activity.getRequiredUpgrade())) {
            return "Requires " + activity.getRequiredUpgrade().getLabel();
        }
        if (activity.requiresUnlock() && !s.unlockedActivities.contains(activity)) {
            return "Requires milestone unlock";
        }
        if (activity.getRequiredLevel() > 0 && s.pubLevel < activity.getRequiredLevel()) {
            return "Requires pub level " + activity.getRequiredLevel();
        }
        if (activity.getRequiredIdentity() != null && s.currentIdentity != activity.getRequiredIdentity()) {
            return "Requires identity " + activity.getRequiredIdentity().name();
        }
        return null;
    }

    public boolean isActivityUnlocked(PubActivity activity) {
        return !activity.requiresUnlock() || s.unlockedActivities.contains(activity);
    }

    private void recordMilestoneReward(String title, String requirement, String rewardText) {
        String entry = title + " | Req: " + requirement + " | " + rewardText;
        s.milestoneRewardLog.addFirst(entry);
        while (s.milestoneRewardLog.size() > 5) {
            s.milestoneRewardLog.removeLast();
        }
    }

    private void grantCashBonus(double amount, String reason) {
        if (amount <= 0) return;
        s.cash += amount;
        s.totalCashEarned += amount;
        log.pos(" Milestone reward: cash +" + String.format("%.0f", amount) + " (" + reason + ").");
    }

    private void reduceChaos(double amount, String reason) {
        if (amount <= 0) return;
        s.chaos = Math.max(0.0, s.chaos - amount);
        log.pos(" Milestone reward: chaos -" + String.format("%.0f", amount) + " (" + reason + ").");
    }

    private void applyTeamMoraleBoost(int delta) {
        if (delta == 0) return;
        for (Staff st : s.fohStaff) st.adjustMorale(delta);
        for (Staff st : s.bohStaff) st.adjustMorale(delta);
        for (Staff st : s.generalManagers) st.adjustMorale(delta);
        log.pos(" Milestone reward: staff morale " + (delta > 0 ? "+" : "") + delta + ".");
    }

    private void grantSkilledStaffHire() {
        Staff.Type pick = Staff.Type.EXPERIENCED;
        if (s.kitchenUnlocked && s.bohStaff.size() < s.kitchenChefCap) {
            pick = Staff.Type.CHEF_DE_PARTIE;
        } else if (s.fohStaff.size() >= s.fohStaffCap) {
            grantCashBonus(120, "Skilled hire fallback");
            return;
        }
        Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), pick, s.random);
        hire.levelUpWeekly(2);
        if (hire.isKitchenRole()) {
            s.bohStaff.add(hire);
        } else {
            s.fohStaff.add(hire);
        }
        log.pos(" Milestone reward: skilled hire " + hire + ".");
    }
}
