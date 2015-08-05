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

import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Handlers.CommandBlockHandler;
import java.io.File;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    
    private static HashMap<String, String> StartedGames = new HashMap<>();
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if(cs instanceof Player){
            if(args.length >= 1){
                Player p = (Player) cs;
                if(args[0].equalsIgnoreCase("leave") && 
                        PVPCore.getPlaying().keySet().contains((p).getName())){
                    Map m = Map.maps.get(PVPCore.getPlaying().get(p.getName()));
                    m.playerLeave(p);
                    return true;
                }else if(args[0].equalsIgnoreCase("game") && args.length >= 2){
                    if(args[1].equalsIgnoreCase("start") && 
                            PVPCore.getPlaying().keySet().contains((p).getName())){
                        Map m = Map.maps.get(PVPCore.getPlaying().get(p.getName()));
                        m.getGm().Start(m);
                        return true;
                    }else if(args[1].equalsIgnoreCase("quickstart")){
                        if(p.isOp()){
                            if(args.length >= 4){
                                Map m = Map.findMap(args[2], args[3]);
                                if(m != null){
                                    p.sendMessage("wip");
                                }else{
                                    p.sendMessage("No such map!");
                                }
                            }else if(args.length >= 3){
                                if(Map.maps.containsKey(args[2])){
                                    Map m = Map.maps.get(args[2]);
                                    p.sendMessage("Starting game " + m.getName());
                                    p.sendMessage("Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                    for(Player pl : Bukkit.getOnlinePlayers()){
                                        if(!PVPCore.getPlaying().containsKey(pl.getName())){
                                            pl.sendMessage(ChatColor.AQUA + p.getName() + " has started a game, " + "Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                            pl.sendMessage(ChatColor.AQUA + "Use /pvp join " + p.getName() + " to join their game");
                                            pl.sendMessage(ChatColor.AQUA + "There are only " + (m.getMax() - 1) + " slots left");
                                        }
                                    }
                                    StartedGames.put(p.getName(), m.getName());
                                    m.playerJoin(p);
                                }else{
                                    p.sendMessage("No such map!");
                                }
                            }
                        }
                    }
                }else if(args[0].equalsIgnoreCase("join")){
                    if(args.length >= 2){
                        if(StartedGames.containsKey(args[1])){
                            Map m = Map.maps.get(StartedGames.get(args[1]));
                            if(!m.getGm().getPlayers().contains(p) && !PVPCore.getPlaying().containsKey(p.getName())){
                                if(m.playerJoin(p)){
                                    p.sendMessage("Joining Map...");
                                    Bukkit.broadcastMessage(p.getName() + " Joined");
                                }else{
                                    p.sendMessage("Failed to Join Map");
                                }
                            }else{
                                p.sendMessage("You are already part of a game");
                                if(p.getName().equalsIgnoreCase("Despot666")){
                                    p.kickPlayer("<3 -Dallen");
                                }
                            }
                        }else{
                            p.sendMessage("No such game");
                        }
                    }else{
                        p.sendMessage("No game name provided");
                    }
                }else if(args[0].equalsIgnoreCase("cleargames") && p.getName().equalsIgnoreCase("Dallen")){
                    for(File f : new File(PVPCore.getSaveLoc() + Main.getFileSep() + "Maps").listFiles()){
                        f.delete();
                    }
                    Map.maps.clear();
                    p.sendMessage("not done yet!");
                }
            }
            return new MapEditor().onCommand(cs, cmnd, label, args);
        }else if(cs instanceof BlockCommandSender){
            return new CommandBlockHandler().onCommand(cs, cmnd, label, args);
        }
        return false;
    }
}
