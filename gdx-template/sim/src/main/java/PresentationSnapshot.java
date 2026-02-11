public record PresentationSnapshot(
        double money,
        double debt,
        int reputation,
        double chaos,
        boolean serviceOpen,
        int week,
        int day,
        int round,
        int traffic,
        int unservedLastTick,
        int refundsLastTick,
        int fightsLastTick
) {}
