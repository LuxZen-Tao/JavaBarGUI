public class MusicSystem {

    private final GameState s;

    public MusicSystem(GameState s) {
        this.s = s;
    }

    public MusicEffects computeEffects(MusicProfileType profile, TimePhase phase) {
        MusicProfileType activeProfile = profile != null ? profile : MusicProfileType.ACOUSTIC_CHILL;
        MusicProfile base = baseProfile(activeProfile);

        double traffic = base.trafficMultiplier();
        double spend = base.spendMultiplier();
        double linger = base.lingerMultiplier();
        double chaos = base.chaosDelta();
        double rep = base.reputationDriftDelta();
        double morale = base.staffMoraleDelta();
        boolean lateRisk = false;

        switch (activeProfile) {
            case POP_PARTY -> {
                if (phase == TimePhase.EARLY_DAY) traffic *= 1.08;
                if (phase == TimePhase.BUILD_UP) traffic *= 1.14;
                if (phase == TimePhase.LATE) {
                    chaos += 2.4;
                    morale -= 0.4;
                    lateRisk = true;
                }
            }
            case JAZZ_LOUNGE -> {
                if (phase == TimePhase.PEAK) {
                    spend *= 1.07;
                    rep += 0.25;
                }
                if (phase == TimePhase.LATE) {
                    traffic *= 0.90;
                }
            }
            case ACOUSTIC_CHILL -> {
                if (phase == TimePhase.LATE) {
                    chaos -= 1.8;
                    morale += 0.7;
                }
            }
            case ELECTRONIC_LATE -> {
                if (phase == TimePhase.LATE) {
                    traffic *= 1.15;
                    chaos += 2.8;
                    lateRisk = true;
                } else {
                    traffic *= 0.95;
                }
            }
            case SPORTS_TV -> {
                if (phase == TimePhase.BUILD_UP) traffic *= 1.08;
                if (phase == TimePhase.PEAK) chaos += 0.8;
            }
            case CLASSIC_ROCK -> {
                if (phase == TimePhase.PEAK) traffic *= 1.06;
                if (phase == TimePhase.LATE) chaos += 1.1;
            }
            case INDIE_ALT -> {
                if (phase == TimePhase.BUILD_UP) {
                    traffic *= 1.05;
                    rep += 0.12;
                }
            }
        }

        double identityPressure = identityPressureFor(activeProfile);
        if (s.consecutiveNightsSameMusic >= 2) {
            identityPressure += 0.10 * Math.min(3, s.consecutiveNightsSameMusic);
        }
        if (s.weeklyMusicSwitches >= 3) {
            identityPressure -= 0.10 * Math.min(4, s.weeklyMusicSwitches - 2);
        }

        if (s.ownedUpgrades.contains(PubUpgrade.SOUND_SYSTEM)) {
            traffic = 1.0 + ((traffic - 1.0) * 1.10);
            spend = 1.0 + ((spend - 1.0) * 1.10);
            morale += Math.max(0.15, morale * 0.12);
            chaos *= 0.92;
        }
        if (s.ownedUpgrades.contains(PubUpgrade.CURATED_PLAYLIST)) {
            rep *= 0.75;
            chaos *= 0.93;
        }
        if (s.ownedUpgrades.contains(PubUpgrade.DJ_NIGHT)) {
            traffic *= 1.07;
            chaos += 1.2;
            lateRisk = lateRisk || phase == TimePhase.LATE || phase == TimePhase.PEAK;
        }
        if (s.ownedUpgrades.contains(PubUpgrade.LIVE_MUSIC_LICENSE)) {
            int staffCount = s.fohStaffCount() + s.generalManagers.size();
            int security = Math.max(0, s.baseSecurityLevel + s.upgradeSecurityBonus + s.bouncersHiredTonight);
            if (staffCount >= 3 && security >= 1) {
                rep += 0.35;
                spend *= 1.03;
            } else {
                rep += 0.08;
            }
        }

        String summary = "Traffic x" + fmt(traffic)
                + " | Chaos " + (chaos >= 0 ? "+" : "") + fmt(chaos)
                + " | Rep " + (rep >= 0 ? "+" : "") + fmt(rep);

        return new MusicEffects(
                clamp(traffic, 0.75, 1.40),
                clamp(spend, 0.85, 1.30),
                clamp(linger, 0.85, 1.30),
                clamp(chaos, -3.5, 4.5),
                clamp(rep, -0.8, 0.8),
                clamp(morale, -1.2, 1.2),
                clamp(identityPressure, -1.0, 1.0),
                lateRisk,
                summary
        );
    }

    private MusicProfile baseProfile(MusicProfileType profile) {
        return switch (profile) {
            case ACOUSTIC_CHILL -> new MusicProfile(0.98, 1.01, 1.03, -0.8, 0.10, 0.25);
            case INDIE_ALT -> new MusicProfile(1.02, 1.02, 1.00, 0.3, 0.12, 0.08);
            case CLASSIC_ROCK -> new MusicProfile(1.05, 1.01, 1.01, 0.8, 0.06, -0.05);
            case POP_PARTY -> new MusicProfile(1.08, 0.99, 1.03, 1.2, 0.03, -0.20);
            case JAZZ_LOUNGE -> new MusicProfile(0.96, 1.06, 1.02, -0.4, 0.18, 0.10);
            case ELECTRONIC_LATE -> new MusicProfile(1.00, 1.00, 1.04, 1.8, 0.04, -0.25);
            case SPORTS_TV -> new MusicProfile(1.04, 0.98, 1.01, 0.6, -0.02, -0.08);
        };
    }

    private double identityPressureFor(MusicProfileType profile) {
        return switch (profile) {
            case ACOUSTIC_CHILL, JAZZ_LOUNGE -> 0.25;
            case INDIE_ALT, CLASSIC_ROCK -> 0.18;
            case POP_PARTY, ELECTRONIC_LATE -> -0.05;
            case SPORTS_TV -> 0.10;
        };
    }

    private String fmt(double value) {
        return String.format("%.2f", value);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
