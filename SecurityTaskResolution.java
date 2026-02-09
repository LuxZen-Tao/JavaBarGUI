public record SecurityTaskResolution(SecurityTask task, boolean applied, String message) {
    public static SecurityTaskResolution blocked(SecurityTask task, String reason) {
        return new SecurityTaskResolution(task, false, reason);
    }

    public static SecurityTaskResolution applied(SecurityTask task, String message) {
        return new SecurityTaskResolution(task, true, message);
    }
}
