# Java Bar Sim v3

**A sophisticated pub management simulation game built in Java.**

## New LibGDX + Legacy layout

This repository now contains a LibGDX multi-module project alongside preserved Swing UI code:

- `core/` - game logic + LibGDX `BarGame` app entrypoint (`com.luxzentao.javabar.core`)
- `lwjgl3/` - desktop launcher (`com.luxzentao.javabar.lwjgl3`)
- `legacy-swing/` - preserved Swing UI code (`com.luxzentao.javabar.legacy.swing`)
- `assets/` - shared LibGDX assets folder

Game logic classes were moved into the `core` module package namespace and Swing/AWT code was split into `legacy-swing`.

## Run desktop (LibGDX)

From the repo root:

```bash
export JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2
export PATH="$JAVA_HOME/bin:$PATH"
gradle :lwjgl3:run
```

If you prefer wrapper commands, once network access is available for wrapper distribution download:

```bash
./gradlew :lwjgl3:run
```

## Legacy Swing

Legacy Swing UI code is preserved under `legacy-swing/src/main/java/com/luxzentao/javabar/legacy/swing` for incremental migration.

## Documentation

All detailed documentation remains in **[UserGuide](UserGuide/)**.

## Credits

**Design & Development**: LuxZen-Tao  
**Platform**: Java (LibGDX desktop migration in progress + legacy Swing)


## Note about PR tooling and wrapper binary

Some PR tooling in this environment cannot process binary diffs (notably `gradle/wrapper/gradle-wrapper.jar`).
If needed, regenerate the wrapper locally with:

```bash
gradle wrapper
```
