# Gamerules

To change the mods behavior, you can change your world's gamerules. Every gamerule added 
by this mod starts with the prefix `molehunt:`.

> If plan on making multiple worlds, and don't want to set the gamerules each time,
> [edit the configuration file](config-file.md).
{style=tip}

Here's a list of all the Molehunt gamerules.

## Molehunt gamerule list

### Molehunt game configuration

- `gameDurationMinutes`: sets the game's duration in minutes (default: `90 minutes`).
- `molePercentage`: sets the mole percentage among all players (default: `25 %`).
- `moleCount`: the absolute mole amount. Overwrites `molePercentage`. To disable 
  this setting, set it to `-1` (default: `-1`). 

### Client-side settings

> These gamerules affect client-side features, but are still applied to all players.
> 
> Also, they will only be effective when the game starts.
{style=note}

- `showNametags`: players' nametags are shown (default: `false`).
- `showSkins`: players' custom skin is visible. Setting this to false will
  result in everyone having the same skin. This skin can be customized by [creating
  a custom resource pack](resource-pack.md) (default: `false`).
- `showTab`: The server player list will be shown (default: `false`).

### Server-side settings

- `enablePortals`: enables all portals to other dimensions (default: `false`).

#### World-border settings

- `initialWorldSize`: the world size when starting the game (default: `600 blocks`).
- `finalWorldSize`: the target world size on the end of the game (default: `100 blocks`).
- `borderMovingStartingTimeOffsetMinutes`: the time before the world borders start to move in minutes (default: `10 minutes`).
  > Setting this to a value greater than `gameDuration` will make the borders never move.
  {style=note}
