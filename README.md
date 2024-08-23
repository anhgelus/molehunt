# Molehunt

Molehunt is a Minecraft mod creating the game with the same name in this cubic game.

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

You can configure every text line and the skin with a resource pack (for reference, check out 
[the default lang file](src/client/resources/assets/molehunt/lang/en_us.json) and [the default
skin file](src/client/resources/assets/molehunt/textures/skin.png)).

Also, more server-side values can be changed in the configuration file, located in your server config directory.

Config hot reloadable with `/molehunt reload`

## Technologies

- Java 21
- Fabric + Fabric API
- Minecraft with Yarn Mappings

## Credits

Creator of the skin used is unknown.

Thanks @leo-210 for the help!
