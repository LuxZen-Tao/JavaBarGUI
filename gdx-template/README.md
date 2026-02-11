# JavaBarSim libGDX Starter Template

This folder adds a low-risk, desktop-focused libGDX starter while keeping the legacy Swing app in the repository untouched.

## Layout

- `assets/` shared runtime assets (working directory for desktop run)
- `sim/` pure Java simulation module reused by front ends (no Swing imports)
- `core/` libGDX shared gameplay UI/screens
- `lwjgl3/` desktop launcher

## Run desktop

```bash
cd gdx-template
gradle lwjgl3:run
```

(Wrapper scripts are present, but the binary wrapper JAR is intentionally not committed to keep this template text-only in PR environments that reject binary files.)

## Notes

- `MainMenuScreen` includes New Game and a Load stub.
- `GameScreen` provides minimal HUD + controls wired to simulation ticks.
- `AudioSettings` is a front-end volume/settings placeholder to support independent music/chatter channels structurally.
- Existing Swing code remains unchanged outside this starter folder.
