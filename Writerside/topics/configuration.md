# Configuration

The mod has two configurations.
The first one modifies the settings of the current world.
The second one modifies the default settings of all your worlds.

## Common concept

### Molehunt game configuration :
- Game's duration: `game_duration` (or `gameDuration`). 
Sets the game's duration in minutes (default: 90).
- Percentage of mole: `mole_percentage` (or `molePercentage`).
Sets the percentage of mole (default: 25).
- Number of mole: `mole_count` (or `moleCount`). 
Sets the number of mole (default: -1).
If you want to use the percentage of mole instead, set this value to -1.

### Client-side settings (applies to all players) :
- Enable players' nametag: `show_nametags` (or `showNametags`).
Players' nametag is visible (default: false).
- Enable players' skin: `show_skins` (or `showSkins`).
Players' skin is visible (default: false).
- Enable tab: `show_tab` (or `showTab`).
Tab can be used (default: false).

### World-border settings :
- World border size when starting the game : `initial_world_size` (or `initialWorldSize`).
- Target border size on the end of the game : `final_world_size` (or `finalWorldSize`).
- Time before moving the borders : `border_moving_starting_time_offset` (or `borderMovingStartingTimeOffsetMinutes`).

Every clientside rules (nametag, skin and tab) are only used by the client during a game.
Before and after the game, they are not used.

## Configuration per world

All settings can be modified via gamerules.

Every gamerule related to this mod starts with the prefix `molehunt:`.

## Modifying default configuration

> These settings do not override the configuration per world! 
{style="note"}

A configuration file is available inside the `config` folder.
This is `molehunt.properties`.

### Default configuration

```ini
# Molehunt mod configuration file
# To regenerate the default configuration, delete, move or rename this file.

# Game settings

# The duration of a molehunt game, in minutes.
# Default: 90 minutes (1 hour 30 minutes).
game_duration = 90

# Mole percentage.
# For example, a mole percentage of 25% will get 1 mole every 4 players.
# Default: 25 %.
mole_percentage = 25

# Mole count (absolute).
# This setting will overwrite the mole_percentage setting.
# If set below 0, this setting is disabled.
# Default: -1.
mole_count = -1


# Client-side settings (applies to all players)

# Show nametags
# Default: false
show_nametags = false

# Show skins
# Default: false
show_skins = false

# Show tab
# Default: false
show_tab = false


# World border settings :

# Initial world size (in blocks).
# Default: 200 blocks.
initial_world_size = 200

# Final world size (in blocks).
# Default: 50 blocks.
final_world_size = 50

# Shrinking starting offset (in minutes)
# The time before starting to shrink the world borders.
# If this value is greater than the game duration, borders will never shrink.
# Default: 10 minutes.
border_shrinking_starting_time_offset = 10
```
