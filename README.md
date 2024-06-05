# ImmortalLogin

[![Build Status](https://ci.dustplanet.de/buildStatus/icon?job=ImmortalLogin)](https://ci.dustplanet.de/job/ImmortalLogin/)
[![Build the plugin](https://github.com/timbru31/ImmortalLogin/workflows/Build%20the%20plugin/badge.svg)](https://github.com/timbru31/ImmortalLogin/actions?query=workflow%3A%22Build+the+plugin%22)
[![Known Vulnerabilities](https://snyk.io/test/github/timbru31/immortallogin/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/timbru31/immortallogin?targetFile=pom.xml)

[![SpigotMC](https://img.shields.io/badge/SpigotMC-v4.0.0-orange.svg)](https://www.spigotmc.org/resources/25481/)

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Info

This CraftBukkit/Spigot plugin adds a temporary god mode for new players.  
Per default the players are invulnerable for 20 minutes, unless they attack other players.  
After a certain amount of hits against other players or anytime sooner by issuing /im the god mode will be disabled.

_Third party features, all of them can be disabled_

- bStats for usage statistics

## Standard config

```yaml
disableUpdater: false
first-login:
  hits: 20
  seconds: 1200
confirmation:
  enabled: false
  delay: 30
nickColor: "DARK_PURPLE"
commandListEnabled: true
# Toggle between a deny and a allow list (false means allowlist)
commandListDenylist: false
commandList:
  - immortallogin
  - immortal
  - im
  - help
  - rules
  - motd
```

## Permissions

| Permission               | Description                                |
| :----------------------- | :----------------------------------------- |
| immortallogin.list.gods  | Lists all players in god mode              |
| immortallogin.admin.gods | Allow you to alter players in the god mode |

## Commands

| Command                       | Aliases      | Description                             | Permission node |
| :---------------------------- | :----------- | :-------------------------------------- | :-------------- |
| immortallogin                 | immortal, im | Ends the god mode before the limit      | -               |
| immortallogin list            | immortal, im | Views the list of god mode players      | -               |
| immortallogin add <player>    | immortal, im | Adds a player to the god mode list      | -               |
| immortallogin remove <player> | immortal, im | Removes a player from the god mode list | -               |

## Support

For support visit the SpigotMC page: https://www.spigotmc.org/resources/25481/  
In addition to reporting bugs here on GitHub you can join my Discord and ask your questions right away!  
[![Discord support](https://discordapp.com/api/guilds/387315912283521027/widget.png?style=banner2)](https://discord.gg/mbCRgzQRvj)

## Usage statistics

[![Usage statistics](https://bstats.org/signatures/bukkit/ImmortalLogin.svg)](https://bstats.org/plugin/bukkit/ImmortalLogin/683)

## Data usage collection of bStats

#### Disabling bStats

The file `./plugins/bStats/config.yml` contains an option to _opt-out_.

#### The following data is **read and sent** to https://bstats.org and can be seen under https://bstats.org/plugin/bukkit/ImmortalLogin

- Your server's randomly generated UUID
- The amount of players on your server
- The online mode of your server
- The bukkit version of your server
- The java version of your system (e.g. Java 8)
- The name of your OS (e.g. Windows)
- The version of your OS
- The architecture of your OS (e.g. amd64)
- The system cores of your OS (e.g. 8)
- bStats-supported plugins
- Plugin version of bStats-supported plugins

## Partnership\*

[![ScalaCube partnership](https://scalacube.com/images/banners/modpack.jpg)](https://scalacube.com/p/_hosting_server_minecraft/2986301)  
<sub><sup>\*As an affiliate partner I earn from qualified purchases</sup></sub>

---

Built by (c) Tim Brust and contributors. Released under the MIT license.
