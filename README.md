# MSedge Stats

A lightweight, real-time player statistics plugin with a clean gui for Paper/Spigot/Purpur 1.21.x servers.

## Features

- Beautiful GUI with small-caps formatting
- Real-time stats refreshed every second
- Player head with SkinRestorer support
- LuckPerms rank display
- AzKillStreak integration
- Accurate distance tracking across all travel types
- Blocks placed & broken tracking
- K/D ratio, mob kills, playtime, ping

## Distance Tracking

Covers all movement types:

| Type | Source |
|---|---|
| Walking / Sprinting / Crouching | Vanilla stat |
| Swimming / Underwater / On Water | Vanilla stat |
| Climbing / Creative Flying | Vanilla stat |
| Elytra | Vanilla stat |
| Minecart / Pig / Strider | Vanilla stat |
| Horse / Donkey / Mule (tamed + saddled) | Plugin-tracked |
| Boat / Chest Boat | Plugin-tracked |

## Requirements

- Paper or Purpur 1.21.x
- Java 17+
- LuckPerms
- SkinsRestorer *(optional)*
- AzKillStreak *(optional)*

## Installation

1. Drop `MSedgeStats.jar` into your `/plugins` folder
2. Restart the server
3. Data is stored automatically in `plugins/MSedge Stats/stats.db`

## Commands

| Command | Description |
|---|---|
| `/stats` | Open your own stats |
| `/stats <player>` | Open another player's stats |

## Building

```bash
mvn clean package
```

The compiled jar will be in `target/`.

## License

MIT License — Copyright (c) 2025 MSedge.
See [LICENSE](LICENSE) for details.
