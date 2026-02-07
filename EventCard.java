public record EventCard(
        String title,
        String body,
        int repDelta,
        double cashDelta,
        int moraleDelta,
        String tags
) {
    public String effectsLine() {
        StringBuilder sb = new StringBuilder();
        if (repDelta != 0) {
            sb.append("Rep ").append(repDelta > 0 ? "+" : "").append(repDelta);
        }
        if (cashDelta != 0) {
            appendSep(sb);
            sb.append("Cash ").append(cashDelta > 0 ? "+" : "")
                    .append("GBP ").append(String.format("%.2f", Math.abs(cashDelta)));
        }
        if (moraleDelta != 0) {
            appendSep(sb);
            sb.append("Morale ").append(moraleDelta > 0 ? "+" : "").append(moraleDelta);
        }
        if (tags != null && !tags.isBlank()) {
            appendSep(sb);
            sb.append("Tags: ").append(tags);
        }
        return sb.toString();
    }

    private static void appendSep(StringBuilder sb) {
        if (sb.length() > 0) sb.append(" | ");
    }
}
