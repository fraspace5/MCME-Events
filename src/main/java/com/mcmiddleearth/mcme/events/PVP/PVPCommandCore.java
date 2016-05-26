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
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode.GameState;
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.CommandBlockHandler;
import java.io.File;
import java.util.HashMap;
import lombok.Getter;
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
    
    @Getter
    private static HashMap<String, String> StartedGames = new HashMap<>();
    
    private int parameter;
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if(cs instanceof Player){
            if(args.length >= 1){
                Player p = (Player) cs;
                /*if(args[0].equalsIgnoreCase("leave") && 
                        PVPCore.getPlaying().keySet().contains((p).getName())){
                    Map m = Map.maps.get(PVPCore.getPlaying().get(p.getName()));
                    m.playerLeave(p);
                    return true;*/
                if(args[0].equalsIgnoreCase("game") && args.length >= 2){
                    if(args[1].equalsIgnoreCase("start") && 
                            PVPCore.getPlaying().keySet().contains((p).getName())){
                        Map m = Map.maps.get(PVPCore.getPlaying().get(p.getName()));
                        
                        m.getGm().Start(m, parameter);
                        
                        return true;
                    }else if(args[1].equalsIgnoreCase("quickstart")){
                        if(p.isOp()){
                            if(args.length >= 3){
                                if(Map.maps.containsKey(args[2])){
                                    Map m = Map.maps.get(args[2]);
                                    
                                    if(StartedGames.containsKey(p.getName()) || StartedGames.containsValue(m.getName())){
                                        if(Integer.parseInt(args[3]) != parameter){
                                            p.sendMessage("Parameter changed from " + ChatColor.GREEN + parameter + ChatColor.WHITE + " to " + ChatColor.GREEN + Integer.parseInt(args[3]));
                                            parameter = Integer.parseInt(args[3]);
                                        }else{
                                            p.sendMessage(ChatColor.RED + "There is already a game in the queue!");
                                        }
                                    }
                                    
                                    else if(!m.getGm().requiresParameter().equals("none")){
                                        try{
                                            parameter = Integer.parseInt(args[3]);
                                            p.sendMessage("Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                                for(Player pl : Bukkit.getOnlinePlayers()){
                                                    if(!PVPCore.getPlaying().containsKey(pl.getName())){
                                                        pl.sendMessage(ChatColor.AQUA + p.getName() + " has started a game");
                                                        pl.sendMessage(ChatColor.GREEN + "Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                                        pl.sendMessage(ChatColor.GREEN + "Use /pvp join to join their game");
                                                        pl.sendMessage(ChatColor.GREEN + "There are only " + (m.getMax() - 1) + " slots left");
                                                    }
                                                }
                                            StartedGames.put(p.getName(), m.getName());
                                            m.playerJoin(p);
                                            p.setPlayerListName(ChatColor.GREEN + p.getName());
                                            p.setDisplayName(ChatColor.GREEN + p.getName());
                                            ChatHandler.getPlayerColors().put(p.getName(), ChatColor.GREEN);
                                        }catch(ArrayIndexOutOfBoundsException e){
                                            p.sendMessage(ChatColor.RED + m.getGmType() + " needs you to enter " + m.getGm().requiresParameter() + "!");
                                        }
                                    }else{
                                        parameter = 0;
                                        p.sendMessage("Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                            for(Player pl : Bukkit.getOnlinePlayers()){
                                                if(!PVPCore.getPlaying().containsKey(pl.getName())){
                                                    pl.sendMessage(ChatColor.AQUA + p.getName() + " has started a game");
                                                    pl.sendMessage(ChatColor.GREEN + "Map: " + m.getTitle() + ", Gamemode: " + m.getGmType());
                                                    pl.sendMessage(ChatColor.GREEN + "Use /pvp join " + p.getName() + " to join their game");
                                                    pl.sendMessage(ChatColor.GREEN + "There are only " + (m.getMax() - 1) + " slots left");
                                                }
                                            }
                                        StartedGames.put(p.getName(), m.getName());
                                        m.playerJoin(p);
                                        p.setPlayerListName(ChatColor.GREEN + p.getName());
                                        p.setDisplayName(ChatColor.GREEN + p.getName());
                                        ChatHandler.getPlayerColors().put(p.getName(), ChatColor.GREEN);
                                    }
                                    
                                }else{
                                    p.sendMessage("No such map!");
                                }
                            }
                        }
                    }else if(args[1].equalsIgnoreCase("end") && 
                            PVPCore.getPlaying().keySet().contains((p).getName())){
                        Map m = Map.maps.get(PVPCore.getPlaying().get(p.getName()));
                        m.getGm().End(m);
                    }else if(args[1].equalsIgnoreCase("getgames")){
                        p.sendMessage("Getting maps");
                        p.sendMessage(StartedGames.toString());
                    }
                }else if(args[0].equalsIgnoreCase("join")){
                   
                    Map m = null;
                    for(Player pl : Bukkit.getServer().getOnlinePlayers()){
                        if(StartedGames.containsKey(pl.getName())){
                            m = Map.maps.get(StartedGames.get(pl.getName()));
                        }
                    }
                   
                    if(!m.getGm().getPlayers().contains(p) && !PVPCore.getPlaying().containsKey(p.getName()) && m.getGm().getState() != GameState.COUNTDOWN){
                        if(m.playerJoin(p)){
                            Bukkit.broadcastMessage(ChatColor.GREEN + p.getName() + " Joined!");
                                
                            if(m.getGm().getState() == GameState.IDLE){
                                p.setPlayerListName(ChatColor.GREEN + p.getName());
                                p.setDisplayName(ChatColor.GREEN + p.getName());
                                ChatHandler.getPlayerColors().put(p.getName(), ChatColor.GREEN);
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
                }else if(args[0].equalsIgnoreCase("cleargames") && p.getName().equalsIgnoreCase("Dallen")){
                    for(File f : new File(PVPCore.getSaveLoc() + Main.getFileSep() + "Maps").listFiles()){
                        f.delete();
                    }
                    Map.maps.clear();
                    p.sendMessage(ChatColor.RED + "Done!");
                }else if(args[0].equalsIgnoreCase("removegame") && p.getName().equalsIgnoreCase("Dallen")){
                    Map.maps.remove(args[1]);
                    File f = new File(PVPCore.getSaveLoc() + Main.getFileSep() + "Maps" + Main.getFileSep() + args[1]);
                    f.delete();
                    p.sendMessage(ChatColor.RED + "Deleted " + args[1]);
                }else if(args[0].equalsIgnoreCase("paste")){
                    int positionLow[] = new int[3];
                    int positionHigh[] = new int[3];
                    if(p.isOp()){
                        try{
                            positionLow[0] = Integer.parseInt(args[1]);
                            positionLow[1] = Integer.parseInt(args[2]);
                            positionLow[2] = Integer.parseInt(args[3]);
                            positionHigh[0] = Integer.parseInt(args[4]);
                            positionHigh[1] = Integer.parseInt(args[5]);
                            positionHigh[2] = Integer.parseInt(args[6]);
                        }
                        catch(NumberFormatException e){
                            p.sendMessage(ChatColor.RED + "All parameters must be integers!");
                        }
                        
                        if(positionLow[0] < positionHigh[0] && positionLow[1] < positionHigh[1] && positionLow[2] < positionHigh[2]){
                            
                        }
                        else{
                            p.sendMessage(ChatColor.RED + "First enter the lower northwest corner, then the higher southeast corner!");
                        }
                    }
                    else{
                        p.sendMessage(ChatColor.RED + "You don't have the necessary permissions to paste maps");
                    }
                    
                }
            }
            return new MapEditor().onCommand(cs, cmnd, label, args);
            
        }else if(args[0].equalsIgnoreCase("togglevoxel")){
            toggleVoxel(false);
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
    
}
                
