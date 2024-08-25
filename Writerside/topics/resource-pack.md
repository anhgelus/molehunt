# Making a cutom resource pack

If you want to customize visual elements of the mod, you can create
your own custom resource pack.

Doing so will enable you to change or translate every text line used by 
the mod, and also change to default skin that is applied to all players
(if the [gamerule `showSkins`](gamerules.md#client-side-settings) is set to true).

> After creating your resource pack, you can either force its use by setting it as
> the server's resource pack, or make it optional (it's purely visuals anyway!)
{style=tip}


## First steps

First, create a folder with your resource pack name (it can be whatever you want!). Then,
inside of this folder, create a file named `pack.mcmeta`. This file is very important : it tells
the game that it is indeed a resource pack.

Now, edit the `pack.mcmeta` file with your favorite text editor, and write the following :
```json
{
  "pack": {
    "description": "An awesome description for an awesome resource pack",
    "pack_format": 34
  }
}
```

> Note that the `pack_format` used here (34) corresponds to minecraft version 1.21.x. If you 
> are making the resource pack for another version, you can check which pack format to use 
> [on the wiki](https://minecraft.wiki/w/Pack_format).
{style=note}

You can now close the `pack.mcmeta` file. Now, inside your resource pack's main folder, 
create a folder named `assets`, and inside it make another folder name `molehunt`. 

You file tree you look like that :
```
📁 MyAwesomeResourcePack
├── 📄 pack.mcmeta
└── 📁 assets
    └── 📁 molehunt
```

> If you want, you can also add an icon to your resource pack : just add a png file named
> `pack.png` in your resource pack's main folder.
{style=tip}


## Adding a custom skin

To add a custom skin, first you need to make one. You can either use
your own skin, or make a new one using a minecraft skin editor (there are 
a lot online).

Then grab your skin file (make sure it's a `.png` file!), name it `skin.png`
and put it inside a `textures` folder, inside the `molehunt` folder. It should 
look like that :
```
...
📁 assets
└── 📁 molehunt
    └── 📁 textures
        └── 📄 skin.png
```

Now everyone in the game will be wearing your custom skin!


## Changing the mod's text

Finally, if the mod's text doesn't suit you, or if you want to translate
it to another language, you can! 

> Note that french is already supported by default, so no need to translate
> to it.
{style=tip}

First, create a new folder in the `molehunt` folder named `lang`, then create
a `en_us.json` file.

> If you want to target another language, name the file according to your language
> and region. For example : `fr_fr.json` for French in France.
{style=note}

Now copy the content of the [default `en_us.json` file](#default-en-us-json-language-file)
in you language file, and start editing the lines you want to change!

Finally, your file structure should look like that :
```
...
📁 assets
└── 📁 molehunt
    └── 📁 lang
        └── 📄 en_us.json
        └── 📄 en_pt.json
        └── etc.
```
(You can have only one, or multiple language files, it doesn't matter)

[Minecraft formatting codes](https://minecraft.wiki/w/Formatting_codes) are 
supported in `titles` and `subtitles`.


## Final file tree, and installing your resource pack

If you followed every step of this tutorial, the final resource apck should look like this :
```
📁 MyAwesomeResourcePack
├── 📄 pack.mcmeta
├── 📄 pack.png (optional)
└── 📁 assets
    └── 📁 molehunt
        ├── 📁 textures
        │   └── 📄 skin.png
        └── 📁 lang
            └── 📄 en_us.json
            └── 📄 en_pt.json
            └── etc.
```

To install it on your client, simply put your awesome resource pack in the `resourcepacks` folder 
of [your `.minecraft` folder](https://minecraft.wiki/w/.minecraft).

If you want, you can zip it to make sharing it easier, but it is not required.


## Default `en_us.json` language file

Here's the default `en_us.json` file. You can use it as a template to
customize the mod's text lines.

> The weird `§` and the character after it corresponds to a minecraft 
> formatting code. It can change the text's color and format. You can 
> learn more [on the wiki](https://minecraft.wiki/w/Formatting_codes). 
{style=tip}

```json
{
  "commands.molehunt.stop.failed": "The Molehunt game has not been started yet.",
  "commands.molehunt.timer.show": "Showing Molehunt timer.",
  "commands.molehunt.timer.hide": "Hiding Molehunt timer.",
  "commands.molehunt.moles.list": "List of moles:",
  "commands.molehunt.moles.list.deny": "You can't see the list of moles.",
  "commands.molehunt.stop.success": "The Molehunt game has been stopped.",
  "molehunt.game.end.suspense.title": "§eAnd the winners are...",
  "molehunt.game.end.winners.moles.title": "§cThe Moles!",
  "molehunt.game.end.winners.survivors.title": "§aNot the Moles!",
  "molehunt.game.end.winners.subtitle": "§6The Moles were",
  "molehunt.game.start.suspense": "§eYou are...",
  "molehunt.game.start.mole.title": "§cThe Mole!",
  "molehunt.game.start.mole.subtitle": "§eGet the list of moles with §6/molehunt moles",
  "molehunt.game.start.survivor.title": "§aNot the Mole!",
  "molehunt.game.start.survivor.subtitle": "§eTry to survive and find out who's the mole!",
  "gamerule.molehunt:gameDuration": "Molehunt: Duration of a game",
  "gamerule.molehunt:molePercentage": "Molehunt: Percentage of Mole",
  "gamerule.molehunt:moleCount": "Molehunt: Number of Mole",
  "gamerule.molehunt:showNametags": "Molehunt: Show players' nametag",
  "gamerule.molehunt:showTab": "Molehunt: Enable the tab",
  "gamerule.molehunt:showSkins": "Molehunt: Show players' skin",
  "gamerule.molehunt:initialWorldSize": "Molehunt: Initial world size",
  "gamerule.molehunt:finalWorldSize": "Molehunt: Final world size",
  "gamerule.molehunt:borderMovingStartingTimeOffsetMinutes": "Molehunt: Time before moving the borders"
}
```
