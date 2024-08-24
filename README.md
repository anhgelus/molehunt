# Molehunt

Molehunt is a Minecraft mod creating the game with the same name in this cubic game.

A complete wiki is available [here](https://www.anhgelus.world/molehunt/introduction.html).

## Usage

Install the mod on the server and on all clients.

Install [Simple Voice Chat](http://modrinth.com/mod/simple-voice-chat).

Start the game with `/molehunt start` and enjoy!

## Features

Every player has the same skin.

Players' nametag are disabled.

The tab, the chat and all message commands (`/msg`, `/tell` and `/w`) are disabled.

Death and advancement messages are disabled.

Stop the game when every innocent is dead or when the timer ended (one hour and half).

The moles can see the name of other moles with `/molehunt moles`.

## Configuration

You can configure every text line and the skin with a resource pack (for reference, check out 
[the default lang file](src/client/resources/assets/molehunt/lang/en_us.json) and [the default
skin file](src/client/resources/assets/molehunt/textures/skin.png)).

Also, more server-side values can be changed in the configuration file, located in your server config directory.
These settings will be applied by default to every new world.
If you want to customize only one world, use the gamerules.

Every setting can be modified in game with these gamerules:
- `molehunt:gameDuration`
- `molehunt:molePercentage`
- `molehunt:moleCount`
- `molehunt:showNametags`
- `molehunt:showTab`
- `molehunt:showSkins`

## Technologies

- Java 21
- Fabric + Fabric API
- Minecraft with Yarn Mappings

## Credits

Creator of the skin used is unknown.

Thanks @leo-210 for the help!
