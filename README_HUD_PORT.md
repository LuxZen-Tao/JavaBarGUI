# README_HUD_PORT

## Terminal -> UI routing
The HUD now consumes simulation output through `SimEventBus` and `BusUILogger`.

- `Simulation` is created with `new BusUILogger(simEventBus)` in `BarGame`.
- All logger calls still print to terminal (`UILogger` behavior unchanged).
- The same lines are forwarded to Scene2D via `eventBus.fireLog(...)`.
- `HudView` subscribes as `SimListener` and appends messages into `ActivityLogPanel`.

```java
simEventBus = new SimEventBus();
simulation = new Simulation(state, new BusUILogger(simEventBus));
...
@Override
public void onLog(String message) {
    activityLogPanel.append(message);
    toastManager.show(message);
}
```

## HUD component map (reusable HUD kit)
### Core reusable widgets
- `StatBarPanel` (`core/.../ui/hud/StatBarPanel.java`): colored multiline stat block.
- `ActivityLogPanel` (`core/.../ui/hud/ActivityLogPanel.java`): scroll log with Event Feed + Timestamps toggles and color rules.
- `RightDrawer` (`core/.../ui/hud/RightDrawer.java`): slide-in/out right drawer with close button.
- `ToastManager` (`core/.../ui/ToastManager.java`): transient toast notifications.
- `MissionControlModal` (`core/.../ui/hud/MissionControlModal.java`): tabbed modal overlay.
- `HudSimBridge` (`core/.../bridge/HudSimBridge.java`): thin adapter to expose display strings + command methods.

### Main composition
- `HudView` (`core/.../ui/HudView.java`) wires top stat bars, center log, bottom action groups, drawers, toasts, mission modal.

## Swing screenshot mapping -> LibGDX mapping
- Top 3-column bars => 12 `StatBarPanel`s grouped in `Table` columns.
- Activity log => `ActivityLogPanel` (`Table + ScrollPane + CheckBox`).
- Reports / Inventory side panels => two `RightDrawer` instances (only one open at once).
- Bottom grouped controls => `Table` groups with action buttons, toggles, and price slider.
- Mission Control tabs => `MissionControlModal` (tab buttons + central scroll text).

## Lifecycle
- `show/create`: instantiate `HudView` in `BarGame.create()`.
- `render`: `simAdapter.sync()`, `hudView.tick()`, `stage.act()`, `stage.draw()`.
- `resize`: call `hudView.resize(width,height)` so drawers snap correctly.
- `dispose`: call `hudView.dispose()` to unregister listener.

## Common pitfalls
- Scene2D updates must happen on render thread (this HUD only mutates from render callbacks/event bus listeners attached in game thread).
- Don’t mutate UI every frame without change filtering in sim/event layer (`SimAdapter` emits changes + `BusUILogger` emits events).
- Ensure `stage.act()` and `stage.draw()` run every frame or drawer animation/toasts/log scrolling won’t update.
- Keep `Viewport.update(..., true)` and `hudView.resize(...)` in sync.
- If `uiskin.json` is missing, fallback skin in `BarGame.loadSkin()` is used.
