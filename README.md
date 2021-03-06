# ImmortalLogin
[![Build Status](https://ci.dustplanet.de/buildStatus/icon?job=ImmortalLogin)](https://ci.dustplanet.de/job/ImmortalLogin/)
[![Build the plugin](https://github.com/timbru31/ImmortalLogin/workflows/Build%20the%20plugin/badge.svg)](https://github.com/timbru31/ImmortalLogin/actions?query=workflow%3A%22Build+the+plugin%22)
[![Known Vulnerabilities](https://snyk.io/test/github/timbru31/immortallogin/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/timbru31/immortallogin?targetFile=pom.xml)

[![SpigotMC](https://img.shields.io/badge/SpigotMC-v3.0.8-orange.svg)](https://www.spigotmc.org/resources/25481/)

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)


## Info
This CraftBukkit/Spigot plugin adds a temporary god mode for new players.  
Per default the players are invulnerable for 20 minutes, unless they attack other players.  
After a certain amount of hits against other players or anytime sooner by issuing /im the god mode will be disabled.

*Third party features, all of them can be disabled*
* bStats for usage statistics

## Standard config
```yaml
disableUpdater: false
first-login:
  hits: 20
  seconds: 1200
confirmation:
  enabled: false
  delay: 30
nickColor: 'DARK_PURPLE'
commandListEnabled: true
# Toggle between a black and a whitelist (false means whitelist)
commandListBlacklist: false
commandList:
  - immortallogin
  - immortal
  - im
  - help
  - rules
  - motd
```

## Permissions

| Permission              | Description                   |
|:------------------------|:------------------------------|
| immortallogin.list.gods | Lists all players in god mode |


## Commands
| Command       | Aliases      | Description                        | Permission node |
|:--------------|:-------------|:-----------------------------------|:----------------|
| immortallogin | immortal, im | Ends the God mode before the limit | -               |

## Support
For support visit the SpigotMC page: https://www.spigotmc.org/resources/25481/

## Usage statistics

[![Usage statistics](https://bstats.org/signatures/bukkit/ImmortalLogin.svg)](https://bstats.org/plugin/bukkit/ImmortalLogin/683)

## Data usage collection of bStats

#### Disabling bStats
The file `./plugins/bStats/config.yml` contains an option to *opt-out*.

#### The following data is **read and sent** to https://bstats.org and can be seen under https://bstats.org/plugin/bukkit/ImmortalLogin
* Your server's randomly generated UUID
* The amount of players on your server
* The online mode of your server
* The bukkit version of your server
* The java version of your system (e.g. Java 8)
* The name of your OS (e.g. Windows)
* The version of your OS
* The architecture of your OS (e.g. amd64)
* The system cores of your OS (e.g. 8)
* bStats-supported plugins
* Plugin version of bStats-supported plugins

---
Built by (c) Tim Brust and contributors. Released under the MIT license.
