MCME-Events
===========

[![Build Status](https://travis-ci.org/DonoA/MCME-Events.svg)](https://travis-ci.org/DonoA/MCME-Events)

Winter and Summer events for MCME

TODO (in order):
 * auto updating from github (done)
 * add lobby tools (done)
 * load and save maps (done)
 * track player stats (almost done)
 * player prefixes (done but needs more chat handler work)
 * command block commands that allow games to start, end, and use plugin features (basics done)
 * create basic pvp stats and controls (need to add listener hooks)
 * add servlet for said stats (done but not pretty)
 * free for all (hook ready and tested)
 * capture the point (not going to be used, but ready)
 * infected (hook ready and tested)
 * team conquest (started)
 * other stuff
 * get sleep



Quick guide to setup:
The basis of how this plugin interacts with redstone is through Points Of Interest called RedBlock(s) you need to set a RedBlock location and that is how the game will start. First you need to declare a location as a map. To do this you will need to do /pvp map <map name> spawn. That will create a map called you map name and declare a spawn point (where the players of your game will be moved on join). Second you need to set a RedBlock location. To do this use `/pvp map <map name> poi RedBlock`. The block will be below your feet and the location sent to you. Next you need to set a gamemode, do this with `/pvp map <map name> setGamemode <Freeforall | Infected | TeamDeathmatch>` which will set the gamemode, and if you did the RedBlock command correctly this wont complain either. If the RedBlock command did not work you will get a message saying: `WARNING: there is not yet a RedBlock location for this map!` make sure the caps a right. Last you need to set the max number of player once this number is reached the game will start (you can always start it manually though) use `/pvp map <map name> setMax <max players>`. Now will need to make the lobby. The command `/pvp lobby` should give you a sign for every map. That is how player will join the game. It will also show all the game information. Last you will need to end the game with the command block command `/pvp cmdBlock endGame <map name>`. Good luck and pm me if you have questions :P


Needed commands for wrapper: 

Shell: mv plugins\update\MCME-Events-0.1.jar plugins\MCME-Events-0.1.jar

MSDOS: MOVE /y plugins\update\MCME-Events-0.1.jar plugins\MCME-Events-0.1.jar


