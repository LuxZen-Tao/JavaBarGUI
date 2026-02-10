public enum MusicProfileType {
    ACOUSTIC_CHILL("Acoustic Chill"),
    INDIE_ALT("Indie Alt"),
    CLASSIC_ROCK("Classic Rock"),
    POP_PARTY("Pop Party"),
    JAZZ_LOUNGE("Jazz Lounge"),
    ELECTRONIC_LATE("Electronic Late"),
    SPORTS_TV("Sports TV");

    private final String label;

    MusicProfileType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
