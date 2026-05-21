# DarkRealm

A 2D top-down dungeon crawler written in Java using the [libGDX](https://libgdx.com/) framework.

## About

DarkRealm is a roguelike game featuring procedurally generated dungeons, a quest system, character progression, and a final boss. The player fights through three zones of increasing difficulty, battling various enemy types and completing NPC quests.

## Gameplay

- **3 zones** — each with unique enemies and quests
- **Procedural dungeon generation** — a new map every run
- **Leveling system** — kill enemies to gain EXP and spend stat points
- **Quest system** — 6 quests (2 per zone) given by the Village Elder
- **Final boss Malgrath** — a three-phase boss with a unique attack set
- **Weapon system** — Sword, Gun, Staff

### Controls

| Key | Action |
|-----|--------|
| `WASD` | Move |
| `LMB` | Attack |
| `RMB` | Dodge |
| `Q` | Shadow Strike |
| `Q` | Void Shield |
| `Q` | Soul Drain |
| `Esc` | Pause |

### Enemies

| Zone | Enemies |
|------|---------|
| 1 | Shadow Wraith, Bone Archer, Plague Hound |
| 2 | Cursed Knight, Golem Sentinel, Void Sorcerer |
| 3 | Elite variants + final boss **Malgrath** |

### Quests

| ID | Name | Objective | Reward |
|----|------|-----------|--------|
| `first_shadow` | The First Shadow | Kill 5 Shadow Wraiths | 300 EXP + Iron Sword |
| `lost_dark` | Lost in the Dark | Find the missing scout | 200 EXP + Healing Potion x3 |
| `shattered` | Shattered Ramparts | Destroy 3 Cursed Barricades | 500 EXP + Steel Armor |
| `antidote` | Antidote Run | Collect 5 Moonbloom Herbs | 400 EXP + Antidote x5 |
| `void_crystals` | Voices in the Void | Destroy 4 Void Crystals | 800 EXP + Mana Orb |
| `silence` | Silence Malgrath | Defeat the final boss | Game Complete |

## Project Structure

```
final-project/
├── assets/              # Game resources (sprites, sounds)
├── core/src/main/java/com/narxoz/darkrealm/
│   ├── C.java               # Global constants
│   ├── DarkRealmGame.java   # Entry point, screen management
│   ├── entities/
│   │   ├── Player.java          # Player (HP/MP, abilities, animation)
│   │   ├── Enemy.java           # Base enemy class
│   │   ├── EnemyFactory.java    # Enemy factory by zone
│   │   ├── Zone1Enemies.java    # Zone 1 enemies
│   │   ├── Zone2Enemies.java    # Zone 2 enemies
│   │   ├── Zone3Enemies.java    # Zone 3 enemies
│   │   ├── Malgrath.java        # Final boss (3 phases)
│   │   └── VillageElder.java    # Quest-giving NPC
│   ├── interfaces/
│   │   ├── IDamageable.java
│   │   ├── IInputHandler.java
│   │   ├── IRenderable.java
│   │   └── IUpdatable.java
│   ├── screens/
│   │   ├── MainMenuScreen.java
│   │   ├── GameScreen.java      # Main game screen
│   │   ├── PauseScreen.java
│   │   ├── GameOverScreen.java
│   │   └── VictoryScreen.java
│   ├── systems/
│   │   ├── Bullet.java / BulletPool.java   # Bullet pool
│   │   ├── CollisionSystem.java             # Collision & line-of-sight
│   │   ├── HUD.java                         # UI (HP, MP, quests)
│   │   ├── KeyboardInputHandler.java
│   │   ├── ParticleSystem.java
│   │   ├── QuestManager.java                # Quest system
│   │   └── SoundManager.java
│   ├── weapons/
│   │   ├── Weapon.java
│   │   ├── SwordWeapon.java
│   │   ├── GunWeapon.java
│   │   └── StaffWeapon.java
│   └── world/
│       ├── DungeonGenerator.java   # Procedural map generation
│       └── Room.java
└── lwjgl3/                  # Desktop (LWJGL3) launcher
```

## Requirements

- **Java 8+**
- **Gradle 9.4** (wrapper included)

## Running

```bash
# Clone the repository
git clone <url>
cd final-project

# Run the desktop version
./gradlew lwjgl3:run
```

On Windows:
```cmd
gradlew.bat lwjgl3:run
```

## Building

```bash
./gradlew lwjgl3:jar
```

The executable JAR will be output to `lwjgl3/build/libs/`.

## Tech Stack

- **Java 8**
- **libGDX** — game framework (rendering, input, audio)
- **LWJGL3** — desktop backend
- **Gradle** — build system

## Author

Developed as a final project at **Narxoz University**.