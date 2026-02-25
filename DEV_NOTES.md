# DEV_NOTES.md — JavaBar Architecture

## Module Overview

The project is split into three Gradle subprojects:

| Module | Package root | Purpose |
|--------|-------------|---------|
| `:core` | `com.luxzentao.javabar.core` | All simulation/domain logic, zero Swing/AWT dependencies |
| `:legacy-swing` | `com.luxzentao.javabar.legacy.swing` | Original Swing UI front-end (optional, for comparison) |
| `:lwjgl3` | `com.luxzentao.javabar.lwjgl3` | LibGDX desktop launcher |

---

## Key Interfaces in `core`

### `Logger` (`com.luxzentao.javabar.core.Logger`)
Used by `Simulation`, `EconomySystem`, and all other simulation systems for output.

**Methods:**
- `info(String)`, `pos(String)`, `neg(String)`, `event(String)`, `header(String)`, `spacer()` — base
- `action(String)`, `warning(String)`, `critical(String)` — default impls delegate to `info`
- `popup(String title, String body, String effects)` — default impl calls `info`
- `popup(EventCard)` — default impl calls `info`
- `popupUpgrade(String, String, String, String)` — default impl delegates to `popup`

**Implementations:**
- `UILogger` (core) — plain `System.out` logger, used for headless/CLI runs
- `BusUILogger` (core/sim) — extends `UILogger`, fires events onto `SimEventBus` for the LibGDX HUD
- `UILogger` (legacy-swing) — Swing `JTextPane`-based logger with coloured styled text

### `CreditLineSelector` (`com.luxzentao.javabar.core.CreditLineSelector`)
Injected into `GameState.creditLineSelector`. Called by `EconomySystem` when a payment shortfall
needs the player to pick a credit line.

- Swing: wired via `WineBarGUI` to show a `JOptionPane` choice dialog
- LibGDX: implement as a Scene2D dialog, call back via the selector lambda

### `SimEventBus` / `SimListener` (`com.luxzentao.javabar.core.sim`)
Pub/sub event bus used by the LibGDX HUD to react to simulation events without polling.

- `BusUILogger` publishes log strings via `SimEventBus.fireLog(String)`
- `SimAdapter` (core) reads `GameState` snapshots and pushes them to listeners
- `HudView` (core/ui) subscribes and refreshes UI components

---

## How the UI Drives the Simulation

```
Player action (button click)
  → HudView / WindowClass calls Simulation method
  → Simulation updates GameState and calls Logger methods
  → BusUILogger fires events on SimEventBus
  → SimAdapter.sync() pushes GameState snapshot to HUD panels
  → HUD panels redraw
```

The simulation is **never** advanced by the render loop. `BarGame.render()` only:
1. Calls `simAdapter.sync()` to push pending state snapshots
2. Calls `hudView.tick()` for cosmetic animations
3. Draws the Stage

---

## Where to Add New UI Panels / Buttons

1. Create a new `XxxWindow` class in `core/src/main/java/com/luxzentao/javabar/core/ui/`
   extending `com.badlogic.gdx.scenes.scene2d.ui.Window`.
2. Accept `Simulation sim`, `GameState state`, `SimEventBus bus`, `Skin skin` in the constructor.
3. Add buttons that call the appropriate `sim.xxxMethod()`.
4. Listen on `bus` via `bus.addListener(...)` if real-time log/state updates are needed.
5. Register the window in `HudView` and wire a toolbar button to toggle it.

For Swing (legacy): add a corresponding panel in `legacy-swing` module, wiring to the same
`Simulation` methods.

---

## Save / Load

`GameStatePersistence` serialises/deserialises `GameState` via Java object serialization.
The file format is **unchanged** — saves from the Swing version are compatible with the
LibGDX version as they share the same `core` serialization code.

---

## Audio

`AudioManager` (core) is a no-op stub that tracks state without playing sounds.
To add real audio:
- **Swing**: create a `SwingAudioService` in `legacy-swing` using `javax.sound.sampled.Clip`.
- **LibGDX**: create a `GdxAudioService` in `core` or `lwjgl3` using `com.badlogic.gdx.audio.Music`.

Neither implementation should be required by `core` — inject via interface if needed.
