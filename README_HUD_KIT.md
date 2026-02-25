# HUD Kit README — JavaBar LibGDX Shell

> **Purpose:** A "copy-and-paste" reference guide for wiring the pub-management game's 2D UI
> using LibGDX Scene2D. The game world (sprites / tilemap) is intentionally deferred; the HUD
> *is* the game for now.

---

## Table of Contents
1. [File Map](#file-map)
2. [Lifecycle Quick-Reference](#lifecycle-quick-reference)
3. [Copy-Paste Snippets](#copy-paste-snippets)
   - [Stage + Skin setup](#stage--skin-setup)
   - [InputMultiplexer](#inputmultiplexer)
   - [Root Table layout skeleton](#root-table-layout-skeleton)
   - [Stat bubble helper](#stat-bubble-helper)
   - [Drawer implementation](#drawer-implementation)
   - [Toast implementation](#toast-implementation)
   - [Example modal Window (Supplier)](#example-modal-window)
   - [Wiring: button → sim → event → HUD update](#wiring-button--sim--event--hud-update)
4. [Swing → LibGDX Mapping](#swing--libgdx-mapping)
5. [Common Pitfalls](#common-pitfalls)
6. [Status Summary](#status-summary)

---

## File Map

| File | Role | New / Modified |
|---|---|---|
| `core/…/BarGame.java` | LibGDX `ApplicationAdapter` entry point | **Modified** |
| `core/…/ui/HudView.java` | Master HUD: bubbles, night buttons, nav bar, drawer | **Modified** |
| `core/…/ui/ToastManager.java` | Bottom-left toast notifications | Existing |
| `core/…/ui/SupplierWindow.java` | Supplier buy screen (Scene2D `Window`) | **New** |
| `core/…/ui/UpgradesWindow.java` | Upgrades purchase screen | **New** |
| `core/…/ui/StaffWindow.java` | Staff hire / fire screen | **New** |
| `core/…/ui/BankWindow.java` | Bank / credit line screen | **New** |
| `core/…/ui/MissionWindow.java` | Mission Control stats dashboard | **New** |
| `core/…/sim/SimEventBus.java` | Pub/sub bridge: sim → HUD | **Modified** |
| `core/…/sim/SimAdapter.java` | Polls `GameState`, fires events | **Modified** |
| `core/…/sim/SimListener.java` | Interface: HUD reacts to sim changes | **Modified** |
| `lwjgl3/…/Lwjgl3Launcher.java` | Desktop launcher (`Lwjgl3Application`) | Unchanged |

---

## Lifecycle Quick-Reference

```
ApplicationAdapter lifecycle
──────────────────────────────────────────────────────
create()   → new Stage, loadSkin(), new Simulation, new SimAdapter, new HudView
render()   → simAdapter.sync()   ← polls GameState, fires events
           → stage.act(delta)
           → stage.draw()
resize()   → stage.getViewport().update(w, h, true)
           → hudView.resize(w, h)    ← repositions drawer
dispose()  → hudView.dispose()       ← removes SimListener
           → stage.dispose()
           → skin.dispose()
```

---

## Copy-Paste Snippets

### Stage + Skin setup

**Where:** `create()` in your `ApplicationAdapter` (or `show()` if using `Screen`).

```java
// In BarGame.create():
stage = new Stage(new ScreenViewport());
skin  = loadSkin();   // see BarGame.loadSkin() for fallback-safe implementation

// loadSkin() tries assets/uiskin.json first; falls back to a generated skin
// so the game still launches without external assets.
private Skin loadSkin() {
    FileHandle uiskinJson = Gdx.files.internal("uiskin.json");
    if (uiskinJson.exists()) return new Skin(uiskinJson);

    // Fallback: build a minimal skin at runtime
    Skin s = new Skin();
    Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    p.setColor(Color.WHITE); p.fill();
    s.add("white", new Texture(p));
    p.dispose();

    BitmapFont font = new BitmapFont();
    s.add("default-font", font);

    Label.LabelStyle ls = new Label.LabelStyle(font, Color.WHITE);
    s.add("default", ls);

    TextButton.TextButtonStyle bs = new TextButton.TextButtonStyle();
    bs.up   = s.newDrawable("white", new Color(0.2f, 0.24f, 0.32f, 1f));
    bs.down = s.newDrawable("white", new Color(0.15f, 0.18f, 0.26f, 1f));
    bs.over = s.newDrawable("white", new Color(0.25f, 0.30f, 0.40f, 1f));
    bs.font = font;
    s.add("default", bs);

    ScrollPane.ScrollPaneStyle ss = new ScrollPane.ScrollPaneStyle();
    ss.background = s.newDrawable("white", new Color(0.09f, 0.10f, 0.14f, 0.95f));
    s.add("default", ss);

    Window.WindowStyle ws = new Window.WindowStyle();
    ws.titleFont      = font;
    ws.titleFontColor = Color.WHITE;
    ws.background     = s.newDrawable("white", new Color(0.12f, 0.13f, 0.18f, 0.97f));
    s.add("default", ws);

    return s;
}
```

---

### InputMultiplexer

**Where:** end of `create()`.  
Allows Stage to capture clicks/keypresses before any game-world camera input processor.

```java
InputMultiplexer mux = new InputMultiplexer();
mux.addProcessor(stage);          // HUD gets input FIRST
// mux.addProcessor(gameCamera);  // add world camera adapter later
Gdx.input.setInputProcessor(mux);
```

---

### Root Table layout skeleton

**Where:** inside `HudView` constructor (or any Screen's `show()`).

```
┌─────────────────────────────────────────┐
│  [Week] [Cash] [Rep] [Staff] [Status]   │  ← top stat bubbles
│                                         │
│          (future game world here)       │  ← expand + fill
│                                         │
│  [Open Bar] [Next Round] [Close Night]  │  ← night row
│  [Supplier][Bank][Mission][Staff][Log]  │  ← nav bar
└─────────────────────────────────────────┘
       [Activity Log drawer] →  (slides in from right)
```

```java
Table root = new Table();
root.setFillParent(true);     // ← fills the full viewport
stage.addActor(root);

// Stat bubbles row
Table bubbles = new Table();
root.top().add(bubbles).left().expandX().padTop(12).padLeft(12).row();

// Centre expands to fill remaining space
root.add().expand().fill().row();

// Night control row
Table nightRow = new Table();
root.add(nightRow).fillX().row();

// Bottom nav bar
Table bottomBar = new Table();
root.bottom().add(bottomBar).fillX();
```

---

### Stat bubble helper

```java
/** Adds a "Title / Value" pill to a container and returns the value Label. */
private Label createBubble(Table parent, String title, String initial) {
    Table bubble = new Table();
    bubble.setBackground(skin.newDrawable("white", new Color(0.16f, 0.18f, 0.24f, 0.94f)));
    bubble.pad(8f);
    Label titleLbl = new Label(title, skin);
    Label valueLbl = new Label(initial, skin);
    bubble.add(titleLbl).left().row();
    bubble.add(valueLbl).left();
    parent.add(bubble).padRight(10).height(64).minWidth(100);
    return valueLbl;  // keep reference to update it live
}

// Usage (in constructor):
weekLabel = createBubble(bubbles, "Week", "W1");
cashLabel = createBubble(bubbles, "Cash", "£0.00");

// Update from SimListener:
@Override public void onWeekChanged(int week) { weekLabel.setText("W" + week); }
@Override public void onCashChanged(double c)  { cashLabel.setText(fmt(c)); }
```

---

### Drawer implementation

A `Table` that slides in/out from the right edge using `Actions.moveTo`.

```java
// 1. Create the drawer (sized to full height)
Table logDrawer = new Table();
logDrawer.setBackground(skin.newDrawable("white", new Color(0.03f, 0.03f, 0.06f, 0.95f)));
logDrawer.setSize(320f, stage.getHeight());
logDrawer.top().left().pad(10f);
stage.addActor(logDrawer);      // add DIRECTLY to stage (not root table)
logDrawer.setPosition(stage.getWidth(), 0f);   // start hidden off-screen

// 2. Toggle:
private boolean drawerOpen = false;
private void toggleDrawer() {
    float targetX = drawerOpen
        ? stage.getWidth()                         // slide out
        : stage.getWidth() - logDrawer.getWidth(); // slide in
    drawerOpen = !drawerOpen;
    logDrawer.clearActions();
    logDrawer.addAction(Actions.moveTo(targetX, 0, 0.22f));
}

// 3. On resize — MUST reposition drawer or it floats wrong:
public void resize(int w, int h) {
    stage.getViewport().update(w, h, true);
    logDrawer.setHeight(Math.max(220f, h));
    float x = drawerOpen ? w - logDrawer.getWidth() : w;
    logDrawer.setPosition(x, 0f);
    hudView.resize(w, h);
}

// 4. Append log lines:
Label line = new Label(message, skin);
line.setWrap(true);
line.setAlignment(Align.left);
logLines.add(line).left().growX().padBottom(6f).row();
Gdx.app.postRunnable(() -> {
    logScrollPane.layout();
    logScrollPane.setScrollPercentY(1f);   // auto-scroll to bottom
});
```

---

### Toast implementation

Short-lived overlay messages that fade in at the bottom-left.  
`ToastManager.show(text)` — call from anywhere with access to the Stage.

```java
// ToastManager.show(String text):
Label lbl = new Label(text, skin);
lbl.setWrap(true);

Container<Label> bubble = new Container<>(lbl);
bubble.setBackground(skin.newDrawable("white", new Color(0.08f, 0.08f, 0.12f, 0.92f)));
bubble.pad(8f);
bubble.setSize(Math.min(340f, stage.getWidth() * 0.5f), 56f);
bubble.setPosition(16f, 20f);
bubble.getColor().a = 0f;   // start transparent

stage.addActor(bubble);
bubble.addAction(Actions.sequence(
    Actions.fadeIn(0.15f),
    Actions.delay(2.2f),
    Actions.fadeOut(0.35f),
    Actions.removeActor()       // clean up from stage automatically
));
```

---

### Example modal Window

A `Scene2D Window` that floats over the HUD. Pattern used by `SupplierWindow`, `UpgradesWindow`, etc.

**Key points:**
- Extend `Window` (not `Table`) — gives you a title bar and drag-to-move for free.
- Store `skin` as `private final Skin uiSkin` (NOT named `skin`, because `Table` has its own
  private `skin` field — naming yours the same will cause a compile error).
- `show(Stage stage)` lazily adds the window to the stage on first call.
- `refresh()` rebuilds dynamic content from live `GameState`; call after every sim action.

```java
public class SupplierWindow extends Window {

    private final Skin uiSkin;  // ← name it uiSkin, NOT skin!
    private final Simulation sim;
    private final GameState state;
    private final SimEventBus bus;

    public SupplierWindow(Skin skin, Simulation sim, GameState state, SimEventBus bus) {
        super("Supplier", skin);       // ← pass constructor param to super
        this.uiSkin = skin;            // ← then store for later use
        this.sim = sim;
        this.state = state;
        this.bus = bus;

        setMovable(true);
        setModal(false);
        pad(10f);

        // ... build static widgets here using uiSkin ...

        TextButton close = new TextButton("Close", uiSkin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { setVisible(false); }
        });
        add(close).right().padTop(8f).row();

        pack();
    }

    public void refresh() {
        // Rebuild dynamic content from live GameState
        // e.g., update price labels, enable/disable buy buttons
    }

    public void show(Stage stage) {
        if (getStage() == null) stage.addActor(this);   // add once
        refresh();
        setPosition(
            (stage.getWidth()  - getWidth())  / 2f,
            (stage.getHeight() - getHeight()) / 2f);
        setVisible(true);
        toFront();
    }
}
```

---

### Wiring: button → sim → event → HUD update

```
[TextButton click]
      │
      ▼
sim.buyFromSupplier(wine, qty)    ← calls Simulation (Swing-era logic unchanged)
      │
      ▼  (Simulation calls log.action(...))
BusUILogger.action(msg)           ← BusUILogger extends UILogger
      │
      ▼
SimEventBus.fireLog(msg)          ← broadcast to all listeners
      │
      ├──► HudView.onLog(msg)     ← appends to Activity Log + shows toast
      │
      ├──► SimAdapter.sync()      ← (called every render frame)
      │         ├── state.cash changed  → fireC ash(cash)
      │         │                        → HudView.onCashChanged(cash)
      │         │                           → cashLabel.setText(fmt)
      │         └── state.nightOpen changed → fireNightStatus(open)
      │                                       → HudView.onNightStatusChanged(open)
      │                                          → enable/disable buttons
      │
      └──► supplierWindow.refresh()   ← if window is currently visible
```

**Concrete example — "Buy x1" button in SupplierWindow:**

```java
TextButton btn = new TextButton("Buy x1", uiSkin);
btn.addListener(new ChangeListener() {
    @Override public void changed(ChangeEvent event, Actor actor) {
        sim.buyFromSupplier(wine, 1);      // ← real sim call
        bus.fireLog("Bought 1x " + wine.getName());  // ← optional extra log
        refresh();                          // ← update this window
    }
});
```

**Night cycle example:**

```java
// Open Bar button
openBarBtn.addListener(new ChangeListener() {
    @Override public void changed(ChangeEvent event, Actor actor) {
        sim.openNight();           // ← Simulation.openNight() — Swing-era logic
        eventBus.fireLog("Bar opened.");
        refreshWindowsIfOpen();
    }
});

// SimAdapter.sync() runs each frame and fires onNightStatusChanged(true)
// HudView.onNightStatusChanged(true) enables nextRoundBtn / closeNightBtn
// and disables openBarBtn automatically.
```

---

## Swing → LibGDX Mapping

| Swing feature | Sim calls | LibGDX equivalent |
|---|---|---|
| `openBtn` — Open Pub | `sim.openNight()` | `HudView` night row: **Open Bar** button |
| `nextRoundBtn` — Next Round | `sim.playRound()` | `HudView` night row: **Next Round** button |
| `closeBtn` — Close Night | `sim.closeNight(reason)` | `HudView` night row: **Close Night** button |
| `supplierBtn` — Supplier dialog | `sim.buyFromSupplier(wine, qty)` | **SupplierWindow** (Scene2D Window) |
| `upgradesBtn` — Upgrades dialog | `sim.buyUpgrade(up)` | **UpgradesWindow** |
| `staffBtn` — Staff dialog | `sim.hireStaff(type)`, `sim.fireStaffAt(i)` | **StaffWindow** |
| `loanSharkBtn` — Finance dialog | `sim.openCreditLine(bank)`, `sim.repayCreditLineInFull(id)` | **BankWindow** |
| `missionControlDialog` | Read-only `GameState` | **MissionWindow** |
| Activity log (`JTextPane`) | `UILogger` → `System.out` | `SimEventBus.fireLog()` → `HudView.onLog()` → drawer + toast |
| HUD stat badges (cash, rep…) | Poll `GameState` | `SimAdapter` diffs + `SimEventBus` events → bubble labels |

### Event routing (legacy UILogger vs. new bus)

The existing `BusUILogger` already routes **all** log calls through `SimEventBus.fireLog()`.
`SimAdapter.sync()` (called in `render()`) diffs `GameState` and fires typed events:

| SimAdapter fires | SimListener callback | HUD action |
|---|---|---|
| `fireWeek(int)` | `onWeekChanged` | updates Week bubble |
| `fireCash(double)` | `onCashChanged` | updates Cash bubble |
| `fireRep(int)` | `onRepChanged` | updates Rep bubble |
| `fireStaff(int)` | `onStaffChanged` | updates Staff bubble |
| `firePunters(int)` | `onPuntersChanged` | updates Punters bubble |
| `fireNightStatus(bool)` | `onNightStatusChanged` | enables/disables night buttons + status label |
| `fireLog(String)` | `onLog` | appends drawer line + shows toast |

---

## Common Pitfalls

| Pitfall | Fix |
|---|---|
| `uiskin.json` missing from assets → blank screen or NPE | `BarGame.loadSkin()` falls back to a generated skin; copy a real skin to `assets/` for production |
| HUD invisible after resize | Always call `stage.getViewport().update(w, h, true)` in `resize()` |
| Stage not drawing | Make sure `stage.act(delta)` AND `stage.draw()` are both called in `render()` |
| Drawer stays in wrong position after resize | Call `hudView.resize(w, h)` from `ApplicationAdapter.resize()` |
| "Week: null" or "W0" showing | `SimAdapter` uses `Math.max(1, state.weekCount)` before firing; `HudView.onWeekChanged` prefixes "W" |
| `skin has private access in Table` compile error | `Table` has a `private Skin skin` field. In Window subclasses, name your skin field `uiSkin` (not `skin`) |
| Window not appearing | Call `show(stage)` which does `stage.addActor(this)` on first use; calling `setVisible(true)` alone won't work if the actor was never added |
| `stage.act()` not called | Without `act()`, Scene2D actions (drawer slide, toast fade) never progress |
| sim method called on GL thread race | All Simulation calls happen in `render()` via button listeners — already on the GL thread; no threading issues |
| `TextArea` in MissionWindow doesn't start at top | Call `statsArea.setCursorPosition(0)` after setting text |

---

## Status Summary

### What is now playable via LibGDX UI
- Open Bar / Next Round / Close Night cycle — fully wired to `sim.openNight()` / `sim.playRound()` / `sim.closeNight()`
- Supplier screen — shows wines, deal, credit, buy buttons (x1/x5/x10/x25), calls `sim.buyFromSupplier()`
- Upgrades screen — lists all `PubUpgrade` values with cost/status, calls `sim.buyUpgrade()`
- Staff screen — shows FOH/BOH/Manager roster, hire/fire buttons, calls `sim.hireStaff()` / `sim.fireStaffAt()`
- Bank screen — shows credit lines, apply for new ones, repay, calls `sim.openCreditLine()` / `sim.repayCreditLineInFull()`
- Mission Control — read-only stats dashboard (week, cash, rep, inventory, upgrades, supplier, staff)
- Activity Log — right-side sliding drawer, auto-scroll, shows every sim log event
- Toasts — bottom-left short-lived messages for every sim log event
- Live stat bubbles — Week, Cash, Rep, Staff, Punters, Bar Status — all update without refresh button

### What is stubbed but wired
- Happy Hour toggle (sim method exists: `sim.toggleHappyHour(on)`) — not yet in HUD
- Price multiplier slider — sim method `sim.setPriceMultiplier(m)` ready, no HUD widget yet
- Food supplier window — sim methods `sim.buyFoodFromSupplier()` ready, not yet wired
- Activities / Landlord Actions — sim methods exist, not yet wired to HUD windows
- Security / Loan Shark — sim methods exist, not wired to HUD windows yet

### Intentionally deferred
- Tilemap / sprites / physics — no world view exists yet (the centre panel is empty)
- Music playback — `AudioManager` exists but is not connected to LibGDX audio
- Boot sequence panel — Swing-only, not ported
- Autosave — Swing `Timer`-driven, not yet wired in LibGDX
