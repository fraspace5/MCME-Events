/*
 * This file is part of MCME-Events.
 * 
 * MCME-Events is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MCME-Events is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MCME-Events.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.mcmiddleearth.mcme.events.PVP;

import com.mcmiddleearth.mcme.events.PVP.maps.MapEditor;
import com.mcmiddleearth.mcme.events.PVP.maps.Map;
import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode.GameState;
import com.mcmiddleearth.mcme.events.PVP.Handlers.BukkitTeamHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.CommandBlockHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler;
import java.io.File;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class PVPCommandCore implements CommandExecutor{
    
    @Getter @Setter
    private static Map queuedGame = null;
    
    @Getter @Setter
    private static Map runningGame = null;
    
    private int parameter;
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if(cs instanceof Player){
            if(args.length >= 1){
                Player p = (Player) cs;
                
                if(args[0].equalsIgnoreCase("game") && args.length >= 2){
                    if(args[1].equalsIgnoreCase("start")){
                        
                        if(queuedGame == null){
                            p.sendMessage(ChatColor.RED + "Can't start! No game is queued!");
                        }
                        
                        if(runningGame == null){
                            queuedGame.getGm().Start(queuedGame, parameter);
                            runningGame = queuedGame;
                            queuedGame = null;
                        }else{
                            p.sendMessage(ChatColor.RED + "Can't start! There's already a game running!");
                        }
                        return true;
                    }else if(args[1].equalsIgnoreCase("quickstart")){
                        
                        if(p.isOp()){
                            if(args.length >= 3){
                                if(Map.maps.containsKey(args[2])){
                                    Map m = Map.maps.get(args[2]);
                                    
                                    if(runningGame != null){
                                        p.sendMessage(ChatColor.RED + "Can't start!");
                                        p.sendMessage(ChatColor.GRAY + runningGame.getGmType() + " on " + runningGame.getTitle() + " is running!");
                                        p.sendMessage(ChatColor.GRAY + "You need to end the current game first, with " + ChatColor.GREEN + "/pvp game end" + ChatColor.GRAY + ".");
                                        return true;
                                    }
                                    else if(queuedGame != null && queuedGame != m){
                                        p.sendMessage(ChatColor.RED + "Can't queue!");
                                        p.sendMessage(ChatColor.GRAY + queuedGame.getGmType() + " on " + queuedGame.getTitle() + " is in the queue!");
                                        p.sendMessage(ChatColor.GRAY + "You need to cancel the queued game first, with " + ChatColor.GREEN + "/pvp game end" + ChatColor.GRAY + ".");
                                        return true;
                                    }
                                    if(queuedGame == m){
                                        if(Integer.parseInt(args[3]) != parameter){
                                            p.sendMessage("Parameter changed from " + ChatColor.GREEN + parameter + ChatColor.WHITE + " to " + ChatColor.GREEN + Integer.parseInt(args[3]));
                                            parameter = Integer.parseInt(args[3]);
                                        }
                                    }
                                    
                                    else if(!m.getGm().requiresParameter().equals("none")){
                                        try{
                                            parameter = Integer.parseInt(args[3]);
                                            p.sendMessage("Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                                for(Player pl : Bukkit.getOnlinePlayers()){
                                                    
                                                    pl.sendMessage(ChatColor.GRAY + p.getName() + " has started a game");
                                                    pl.sendMessage(ChatColor.GRAY + "Map: " + ChatColor.GREEN + m.getTitle() + ChatColor.GRAY + ", Gamemode: " + ChatColor.GREEN + m.getGmType());
                                                    pl.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/pvp join" + ChatColor.GRAY + " to join the game");
                                                    pl.sendMessage(ChatColor.GRAY + "There are only " + m.getMax() + " slots left");
                                                    pl.sendMessage(ChatColor.GREEN + "Do /pvp rules " + removeSpaces(m.getGmType()) + " if you don't know how this gamemode works!");
                                                    
                                                }
                                            queuedGame = m;
                                            
                                        }catch(ArrayIndexOutOfBoundsException e){
                                            p.sendMessage(ChatColor.RED + m.getGmType() + " needs you to enter " + m.getGm().requiresParameter() + "!");
                                        }catch(NumberFormatException e){
                                            p.sendMessage(ChatColor.RED + "Parameter value must be an integer!");
                                        }
                                    }else{
                                        parameter = 0;
                                        p.sendMessage("Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                            for(Player pl : Bukkit.getOnlinePlayers()){
                                                
                                                pl.sendMessage(ChatColor.GRAY + p.getName() + " has started a game");
                                                pl.sendMessage(ChatColor.GRAY + "Map: " + ChatColor.GREEN + m.getTitle() + ChatColor.GRAY + ", Gamemode: " + ChatColor.GREEN + m.getGmType());
                                                pl.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/pvp join" + ChatColor.GRAY + " to join the game");
                                                pl.sendMessage(ChatColor.GRAY + "There are only " + m.getMax() + " slots left");
                                                pl.sendMessage(ChatColor.GREEN + "Do /pvp rules " + removeSpaces(m.getGmType()) + " if you don't know how this gamemode works!");
                                                
                                            }
                                        queuedGame = m;
                                    }
                                    
                                }else{
                                    p.sendMessage("No such map!");
                                }
                            }
                        }
                    }else if(args[1].equalsIgnoreCase("end")){
                        if(p.isOp()){
                            if(runningGame != null){
                                
                                for(Player pl : Bukkit.getOnlinePlayers()){
                                    pl.sendMessage(ChatColor.GRAY + "The game was ended by a staff!");
                                }
                                runningGame.getGm().End(runningGame);
                            }
                            else if(queuedGame != null){
                                queuedGame.getGm().getPlayers().clear();
                                queuedGame = null;
                                for(Player pl : Bukkit.getOnlinePlayers()){
                                    ChatHandler.getPlayerColors().put(pl.getName(), ChatColor.WHITE);
                                    pl.setPlayerListName(ChatColor.WHITE + pl.getName());
                                    pl.setDisplayName(ChatColor.WHITE + pl.getName());
                                    BukkitTeamHandler.removeFromBukkitTeam(pl);
                                    pl.sendMessage(ChatColor.GRAY + "The queued game was canceled! You'll need to rejoin when another game is queued.");
                                }
                                ChatHandler.getPlayerPrefixes().clear();
                            }else{
                                p.sendMessage(ChatColor.GRAY + "There is no game to end!");
                            }
                        }else{
                            p.sendMessage(ChatColor.RED + "You don't have the permission to end games!");
                        }
                    }else if(args[1].equalsIgnoreCase("getgames") && p.isOp()){
                        p.sendMessage("Getting maps");
                        if(queuedGame != null || runningGame != null){
                            
                            if(queuedGame != null){
                                p.sendMessage(queuedGame.getName());
                            }
                            if(runningGame != null){
                                p.sendMessage(runningGame.getName());
                            }
                            
                        }
                        else{
                            p.sendMessage("No games are currently queued or running!");
                        }
                    }    
                }else if(args[0].equalsIgnoreCase("join")){
                   
                    Map m = null;
                    
                    if(queuedGame != null){
                        m = queuedGame;
                    }
                    else if(runningGame != null){
                        m = runningGame;
                    }else{
                        p.sendMessage(ChatColor.RED + "There is no queued or running game!");
                        return true;
                    }
                   
                    if(!m.getGm().getPlayers().contains(p) && m.getGm().getState() != GameState.COUNTDOWN){
                        if(m.playerJoin(p)){
                                
                            if(m.getGm().getState() == GameState.IDLE){
                                p.setPlayerListName(ChatColor.GREEN + p.getName());
                                p.setDisplayName(ChatColor.GREEN + p.getName());
                                ChatHandler.getPlayerColors().put(p.getName(), ChatColor.GREEN);
                                ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.GREEN + "Participant");
                                BukkitTeamHandler.addToBukkitTeam(p, ChatColor.GREEN);
                            }
                               
                        }else{
                            p.sendMessage("Failed to Join Map");
                        }
                    }else if(m.getGm().getState() == GameState.COUNTDOWN){
                        p.sendMessage(ChatColor.RED + "Do " + ChatColor.GREEN + "/pvp join" + ChatColor.RED + " again once the countdown is done!");
                    }else{
                        p.sendMessage("You are already part of a game");
                        if(p.getName().equalsIgnoreCase("Despot666")){
                            p.kickPlayer("<3 -Dallen");
                        }
                    }
                }else if(args[0].equalsIgnoreCase("pipe")){
                    GearHandler.giveCustomItem(p, GearHandler.CustomItem.PIPE);
                }else if(args[0].equalsIgnoreCase("stat") || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("statistics")){
                    
                    if(args.length == 1){
                        PlayerStat ps = PlayerStat.getPlayerStats().get(p.getName());
                    
                        p.sendMessage(ChatColor.GREEN + "Showing stats for " + p.getName());
                        p.sendMessage(ChatColor.GRAY + "Kills: " + ps.getKills());
                        p.sendMessage(ChatColor.GRAY + "Deaths: " + ps.getDeaths());
                        p.sendMessage(ChatColor.GRAY + "Games Played: " + ps.getGamesPlayed());
                        p.sendMessage(ChatColor.GRAY + "    Won: " + ps.getGamesWon());
                        p.sendMessage(ChatColor.GRAY + "    Lost: " + ps.getGamesLost());
                        p.sendMessage(ChatColor.GRAY + "Games Spectated: " + ps.getGamesSpectated());
                        
                    }else if(args[1].equalsIgnoreCase("clear") && (p.getName().equals("DSESGH") || p.getName().equals("Dallen"))){
                        
                        for(File f : new File(PVPCore.getSaveLoc() + Main.getFileSep() + "stats").listFiles()){
                            f.delete();
                        }
                        
                        for(PlayerStat ps : PlayerStat.getPlayerStats().values()){
                            
                            ps.setKills(0);
                            ps.setDeaths(0);
                            ps.setGamesLost(0);
                            ps.setGamesWon(0);
                            ps.setGamesSpectated(0);
                            ps.setGamesPlayed(0);
                            ps.getPlayersKilled().clear();
                            
                        }
                        
                    }
                     
                }else if(args[0].equalsIgnoreCase("rules")){
                    String gm;
                    try{
                        gm = args[1];
                    }catch(ArrayIndexOutOfBoundsException e){
                        p.sendMessage(ChatColor.RED + "Format: /pvp rules <gamemode>");
                        p.sendMessage(ChatColor.GRAY + "Gamemodes are: FreeForAll, Infected, OneInTheQuiver, Ringbearer, TeamConquest, TeamDeathmatch, and TeamSlayer");
                        return true;
                    }
                    giveRules(p,gm);
                }else if(args[0].equalsIgnoreCase("removegame") && (p.getName().equals("Dallen") || p.getName().equals("DSESGH"))){
                    Map.maps.remove(args[1]);
                    File f = new File(PVPCore.getSaveLoc() + Main.getFileSep() + "Maps" + Main.getFileSep() + args[1]);
                    f.delete();
                    p.sendMessage(ChatColor.RED + "Deleted " + args[1]);
                }
                 
            return new MapEditor().onCommand(cs, cmnd, label, args);
            
        }else if(args[0].equalsIgnoreCase("togglevoxel")){
            toggleVoxel(false);
        }
    }
        else if(cs instanceof BlockCommandSender){
            return new CommandBlockHandler().onCommand(cs, cmnd, label, args);
        
    }
    return false;
    }
    public static void toggleVoxel(boolean onlyDisable){
        try{
            if(Bukkit.getPluginManager().getPlugin("VoxelSniper").isEnabled()){
                Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("VoxelSniper"));
            }else if(!onlyDisable){
                Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("VoxelSniper"));
            }
        }
        catch(NullPointerException e){
            System.err.println("VoxelSniper isn't loaded! Ignoring!");
        }
    }
    
    private static void giveRules(Player sendTo, String gm){
        gm = gm.toLowerCase();
        switch(gm){
            case "freeforall":
                sendTo.sendMessage(ChatColor.GREEN + "Free For All Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Every man for himself, madly killing everyone! Highest number of kills wins.");
                break;
            case "infected":
                sendTo.sendMessage(ChatColor.GREEN + "Infected Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Everyone starts as a Survivor, except one person, who is Infected. Infected gets a Speed effect, but has less armor");
                sendTo.sendMessage(ChatColor.GRAY + "If a Survivor is killed, they become Infected. Infected players have infinite respawns");
                sendTo.sendMessage(ChatColor.GRAY + "If all Survivors are infected, Infected team wins. If the time runs out with Survivors remaining, Survivors win.");
                break;
            case "oneinthequiver":
                sendTo.sendMessage(ChatColor.GREEN + "One in the Quiver Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Everyone gets an axe, a bow, and one arrow, which kills in 1 shot if the bow is fully drawn.");
                sendTo.sendMessage(ChatColor.GRAY + "Every man is fighting for himself. If they get a kill or die, they get another arrow, up to a max of 5 arrows");
                sendTo.sendMessage(ChatColor.GRAY + "First to 21 kills wins.");
                break;
            case "ringbearer":
                sendTo.sendMessage(ChatColor.GREEN + "Ringbearer Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Two teams, each with a ringbearer, who gets The One Ring (which of course gives invisibility)");
                sendTo.sendMessage(ChatColor.GRAY + "As long as the ringbearer is alive, the team can respawn.");
                sendTo.sendMessage(ChatColor.GRAY + "Once the ringbearer dies, that team cannot respawn. The first team to run out of members loses.");
                break;
            case "teamconquest":
                sendTo.sendMessage(ChatColor.GREEN + "Team Conquest Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Two teams. There are 3 beacons, which each team can capture by repeatedly right clicking the beacon.");
                sendTo.sendMessage(ChatColor.GRAY + "Points are awarded on kills, based on the difference between each team's number of beacons.");
                sendTo.sendMessage(ChatColor.GRAY + "i.e. if Red has 3 beacons and Blue has 0, Red gets 3 point per kill. If Red has 1 and Blue has 2, Red doesn't get points for a kill.");
                sendTo.sendMessage(ChatColor.GRAY + "First team to a certain point total wins.");
                break;
            case "teamdeathmatch":
                sendTo.sendMessage(ChatColor.GREEN + "Team Deathmatch Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Two teams, and no respawns. First team to run out of players loses.");
                break;
            case "teamslayer":
                sendTo.sendMessage(ChatColor.GREEN + "Team Slayer Rules");
                sendTo.sendMessage(ChatColor.GRAY + "Two teams, and infinite respawns. 1 point per kill. First team to a certain point total wins.");
                break;
            default:
                sendTo.sendMessage(ChatColor.RED + gm + " is not a valid gamemode! Did you add spaces or hyphens?");
                sendTo.sendMessage(ChatColor.GRAY + "Gamemodes are: FreeForAll, Infected, OneInTheQuiver, Ringbearer, TeamConquest, TeamDeathmatch, and TeamSlayer");
        }
    }
    
    public static String removeSpaces(String s){
        String newString = "";
        
        char[] chars = s.toCharArray();
        
        for(char c : chars){
            if(c != ' '){
                newString += String.valueOf(c);
            }
        }
        return newString;
    }
}

                
