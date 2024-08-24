# Configuration

The mod has two configurations.
The first one modifies the settings of the current world.
The second one modifies the default settings of all your worlds.

## Common concept

- Game's duration: `game_duration` (or `gameDuration`). 
Sets the game's duration in minutes (default: 90).
- Percentage of mole: `mole_percentage` (or `molePercentage`).
Sets the percentage of mole (default: 25).
- Number of mole: `mole_count` (or `moleCount`). 
Sets the number of mole (default: -1).
If you want to use the percentage of mole instead, set this value to -1.
- Enable players' nametag: `show_nametags` (or `showNametags`).
Players' nametag is visible (default: false).
- Enable players' skin: `show_skins` (or `showSkins`).
Players' skin is visible (default: false).
- Enable tab: `show_tab` (or `showTab`).
Tab can be used (default: false).

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

# Show nametags
# Default: false
show_nametags = false

# Show skins
# Default: false
show_skins = false

# Show tab
# Default: false
show_tab = false
```
