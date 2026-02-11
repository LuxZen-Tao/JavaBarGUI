public class AudioSettings {
    private float musicVolume = 0.7f;
    private float chatterVolume = 0.6f;

    public float musicVolume() {
        return musicVolume;
    }

    public float chatterVolume() {
        return chatterVolume;
    }

    public void setMusicVolume(float value) {
        musicVolume = clamp(value);
    }

    public void setChatterVolume(float value) {
        chatterVolume = clamp(value);
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
