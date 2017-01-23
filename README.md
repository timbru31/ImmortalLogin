# ImmortalLogin
[![Build Status](https://ci.dustplanet.de/buildStatus/icon?job=ImmortalLogin)](https://ci.dustplanet.de/job/ImmortalLogin/)
[![Build Status](https://travis-ci.com/timbru31/ImmortalLogin.svg?token=xMwFbvUujsG645zQBus3&branch=master)](https://travis-ci.com/timbru31/ImmortalLogin)
[![CircleCI](https://img.shields.io/circleci/token/07c4ecd483c59e0d3d611a5c9f4599a8340a8777/project/github/timbru31/ImmortalLogin/master.svg)](https://circleci.com/gh/timbru31/ImmortalLogin)

## Info
This CraftBukkit/Spigot plugin adds a temporary god mode for new players.  
Per default the players are invulnerable for 20 minutes, unless the do not attack other players.  
After a certain amount of hits against other players or anytime sooner by issuing /im the god mode will be disabled.

*Third party features, all of them can be disabled*
* Metrics for usage statistics

## License

This plugin is released under closed source.
You do not have the permission to redistribute, share, sell or make this plugin in any other way available for others.
Please not that decompilation is a violation of this license.
One purchase is valid for one server, if you plan to use it on multiple servers you will need to purchase the resource again.

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
commandListBlacklist: true
commandList:
- help
- rules
- motd
```

## Permissions

| Permission              | Description                   |
|:-----------------------:|:-----------------------------:|
| immortallogin.list.gods | Lists all players in god mode |


## Commands
| Command       | Aliases      | Description                        | Permission node |
|:-------------:|:------------:|:----------------------------------:|:---------------:|
| immortallogin | immortal, im | Ends the God mode before the limit | -               |

## Support
For support visit the SpigotMC page: https://www.spigotmc.org/resources/25481/

## Usage statistics
[![MCStats](http://mcstats.org/signature/ImmortalLogin.png)](http://mcstats.org/plugin/ImmortalLogin)

## Data usage collection of Metrics

#### Disabling Metrics
The file ../plugins/Plugin Metrics/config.yml contains an option to *opt-out*

#### The following data is **read** from the server in some way or another
* File Contents of plugins/Plugin Metrics/config.yml (created if not existent)
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin
* Mineshafter status - it does not properly propagate Metrics requests however it is a very simple check and does not read the filesystem

#### The following data is **sent** to http://mcstats.org and can be seen under http://mcstats.org/plugin/ImmortalLogin
* Metrics revision of the implementing class
* Server's GUID
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin

## Donation
[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dl.dropboxusercontent.com/u/26476995/bitcoin_logo.png "Donation via BitCoins")
Address: 1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C
