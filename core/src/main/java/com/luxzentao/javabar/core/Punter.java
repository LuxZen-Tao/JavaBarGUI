package com.luxzentao.javabar.core;

import java.util.Random;

public class Punter implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public enum State { CHILL, ROWDY, MENACE }
    public enum Tier { LOWLIFE, REGULAR, DECENT, BIG_SPENDER }
    public enum DescriptorCategory { PERSONALITY, SOCIAL, PHYSICAL }
    public enum Descriptor {
        CALM(DescriptorCategory.PERSONALITY, -1, 14),
        LOUD(DescriptorCategory.PERSONALITY, 1, 12),
        SHIFTY(DescriptorCategory.PERSONALITY, 1, 10),
        DODGY(DescriptorCategory.PERSONALITY, 2, 8),
        FRIENDLY(DescriptorCategory.PERSONALITY, -1, 12),
        VOLATILE(DescriptorCategory.PERSONALITY, 2, 7),
        RESERVED(DescriptorCategory.PERSONALITY, -1, 10),
        IMPULSIVE(DescriptorCategory.PERSONALITY, 1, 9),
        FANCY(DescriptorCategory.SOCIAL, -1, 10),
        ARTSY(DescriptorCategory.SOCIAL, -1, 9),
        UNDERGROUND(DescriptorCategory.SOCIAL, 1, 8),
        CORPORATE(DescriptorCategory.SOCIAL, -1, 8),
        PARTY_HARD(DescriptorCategory.SOCIAL, 1, 10),
        LOW_KEY(DescriptorCategory.SOCIAL, -1, 10),
        WELL_DRESSED(DescriptorCategory.PHYSICAL, -1, 8),
        SCRUFFY(DescriptorCategory.PHYSICAL, 1, 9),
        INTIMIDATING(DescriptorCategory.PHYSICAL, 1, 6),
        GORGEOUS(DescriptorCategory.PHYSICAL, -1, 5),
        BABY_FACED(DescriptorCategory.PHYSICAL, 0, 6),
        DISTINCTIVE(DescriptorCategory.PHYSICAL, 0, 7);

        private final DescriptorCategory category;
        private final int chaosDelta;
        private final int weight;

        Descriptor(DescriptorCategory category, int chaosDelta, int weight) {
            this.category = category;
            this.chaosDelta = chaosDelta;
            this.weight = weight;
        }

        public DescriptorCategory getCategory() { return category; }
        public int getChaosDelta() { return chaosDelta; }
        public int getWeight() { return weight; }
    }

    private final String name;
    private final int age;
    private double wallet;
    private final int trouble; // 0..2
    private final Tier tier;
    private final int id;

    private int noBuyRounds = 0;
    private int foodCooldownRounds = 0;
    private int foodAttempts = 0;
    private boolean orderedFoodThisVisit = false;
    private boolean banned = false;
    private boolean leftBar = false;

    private State state = State.CHILL;
    private final java.util.List<Descriptor> descriptors = new java.util.ArrayList<>();
    private int chaosContribution = 0;

    public Punter(int id, String name, int age, double wallet, int trouble, Tier tier) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.wallet = wallet;
        this.trouble = trouble;
        this.tier = tier;
    }

    public static Punter randomPunter(int id, Random random, Tier tier) {
        int age = 16 + random.nextInt(35);
        double wallet = switch (tier) {
            case LOWLIFE -> 3 + random.nextDouble() * 22;
            case REGULAR -> 8 + random.nextDouble() * 50;
            case DECENT -> 18 + random.nextDouble() * 90;
            case BIG_SPENDER -> 35 + random.nextDouble() * 140;
        };

        int roll = random.nextInt(100);
        int troubleBase = switch (tier) {
            case LOWLIFE -> 55;
            case REGULAR -> 70;
            case DECENT -> 80;
            case BIG_SPENDER -> 88;
        };
        int trouble = (roll < troubleBase) ? 0 : (roll < 92 ? 1 : 2);

        String name = NameGenerator.randomName(random);
        if (name == null || name.isBlank()) {
            name = "Punter " + id;
        }
        Punter p = new Punter(id, name, age, wallet, trouble, tier);

        if (trouble == 1 && random.nextInt(100) < 30) p.state = State.ROWDY;
        if (trouble == 2 && random.nextInt(100) < 25) p.state = State.MENACE;

        return p;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public boolean isBanned() { return banned; }
    public boolean hasLeftBar() { return leftBar; }
    public boolean canDrink() { return age >= 18; }
    public double getWallet() { return wallet; }
    public int getTrouble() { return trouble; }
    public State getState() { return state; }
    public Tier getTier() { return tier; }
    public java.util.List<Descriptor> getDescriptors() { return descriptors; }
    public int getChaosContribution() { return chaosContribution; }
    public int getNoBuyStreak() { return noBuyRounds; }
    public int getFoodCooldownRounds() { return foodCooldownRounds; }
    public int getFoodAttempts() { return foodAttempts; }
    public boolean hasOrderedFoodThisVisit() { return orderedFoodThisVisit; }
    public void setState(State state) { this.state = state; }
    public void setFoodCooldownRounds(int rounds) { this.foodCooldownRounds = Math.max(0, rounds); }
    public void incrementFoodAttempts() { this.foodAttempts++; }
    public void setOrderedFoodThisVisit(boolean ordered) { this.orderedFoodThisVisit = ordered; }
    public void tickFoodCooldown() { if (foodCooldownRounds > 0) foodCooldownRounds--; }
    public void setDescriptors(java.util.List<Descriptor> assigned) {
        descriptors.clear();
        if (assigned != null) {
            descriptors.addAll(assigned);
        }
    }

    public void setChaosContribution(int chaosContribution) {
        this.chaosContribution = chaosContribution;
    }

    public void spend(double amount) {
        wallet = Math.max(0, wallet - amount);
    }

    public void leaveBar() {
        leftBar = true;
    }

    public void markKickedOut() {
        banned = true;
        leftBar = true;
    }

    public void incrementNoBuy() {
        noBuyRounds++;
        if (noBuyRounds >= 3) {
            banned = true;
            leftBar = true;
        }
    }

    public boolean escalateIfStaying() {
        return switch (state) {
            case CHILL -> { state = State.ROWDY; yield false; }
            case ROWDY -> { state = State.MENACE; yield false; }
            case MENACE -> true;
        };
    }

    @Override
    public String toString() {
        String vibe = switch (state) {
            case CHILL -> "chill";
            case ROWDY -> "rowdy";
            case MENACE -> "MENACE";
        };

        return name + " | age " + age
                + " | wallet " + String.format("%.2f", wallet)
                + " | " + tier.name().toLowerCase().replace('_', ' ')
                + " | " + vibe
                + (descriptors.isEmpty() ? "" : " | " + descriptorSummary())
                + (banned ? " (KICKED OUT)" : "")
                + (leftBar ? " (LEFT)" : "");
    }

    private String descriptorSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptors.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(descriptors.get(i).name().toLowerCase().replace('_', ' '));
        }
        return sb.toString();
    }
}
