# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the game (desktop)
./gradlew lwjgl3:run

# Build all modules
./gradlew build

# Create runnable fat JAR
./gradlew lwjgl3:jar

# Create platform-optimized JARs (strips unused native libs)
./gradlew lwjgl3:jarWin
./gradlew lwjgl3:jarMac
./gradlew lwjgl3:jarLinux
```

There are no unit tests currently. The Gradle test task exists but has no tests defined.

## Architecture

This is a **libGDX** desktop game using a multi-module Gradle project:

- **`core/`** — Platform-agnostic game logic (`io.eclipse.arcana`). All gameplay code lives here.
- **`lwjgl3/`** — Desktop launcher only. `Lwjgl3Launcher` → `StartupHelper` (handles OS quirks for macOS, Windows non-ASCII paths, Linux NVIDIA) → creates the `Lwjgl3Application` with `Core`.
- **`assets/`** — Shared game assets referenced by both modules.

### Screen Flow

```
Core.create() → FirstScreen (title/intro with animations)
                     ↓ (any key press)
               MainScreen (currently empty placeholder)
```

`Core extends Game` and acts only as a screen host — all logic lives in `Screen` implementations. `FirstScreen` randomly selects between two visual themes (`titleA.png` bright / `titleB.png` dark) and handles fade-in, floating, and ray/vignette animations before transitioning.

`MainScreen` is the next screen to implement — it receives the `Core` reference for screen transitions.

### Dependency Highlights

The project already has these available (no additional setup needed):

| Library | Purpose |
|---|---|
| `com.badlogicgames.ashley:ashley` | Entity-Component System (primary ECS) |
| `net.onedaybeard.artemis:artemis-odb` | Alternative ECS |
| `com.badlogicgames.gdx:gdx-box2d` | 2D physics |
| `com.badlogicgames.gdx:gdx-ai` | Pathfinding / steering |
| `box2dlights` | Dynamic lighting |
| `ktx-*` (1.13.1-rc1) | Full KTX extension suite (scene2d, ashley, assets, animations, math, etc.) |
| `noise4j` | Procedural noise (Perlin/Simplex) |
| `kotlinx-coroutines-core` | Kotlin coroutines |

## Game Design Context

The game is a **two-player tarot card strategy game**. Key mechanics to know when implementing `MainScreen`:

- **Win condition:** Reduce opponent HP from 250 to 0.
- **Deck:** 36 cards — 14 Minor Arcana (one suit chosen at start: Wands/Swords/Cups/Pentacles) + 22 Major Arcana (drafted).
- **Turn structure:** Draw → Action → End (20-second timer per turn).
- **Reverse system:** Playing reversed cards is allowed for 1 turn free; subsequent reversed plays escalate through 5 debuff stages.
- **Extinction cards:** The Fool, Death, Tower, World — one-time use, permanently removed from the game after use.
- **Visual style:** Black and gold, astrology/tarot theme, fan-shaped card hand layout.

Full game rules: `게임 규칙 및 설명서.md`  
UI/system design notes: `노트.md`

## Code Style

`.editorconfig` enforces: 4-space indent for Java, 2-space for Gradle/Kotlin files, LF line endings, UTF-8.
