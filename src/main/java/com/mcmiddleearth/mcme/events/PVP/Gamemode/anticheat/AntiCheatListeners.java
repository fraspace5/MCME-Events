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
package com.mcmiddleearth.mcme.events.PVP.Gamemode.anticheat;

import com.mcmiddleearth.mcme.events.PVP.PVPCommandCore;
import com.mcmiddleearth.mcme.events.PVP.Team;
import java.util.HashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Eric
 */
public class AntiCheatListeners implements Listener{
    
    //Prevent trolling or cheating with commands
    @EventHandler
    public static void onPlayerCommand(PlayerCommandPreprocessEvent e){
        
        String command = e.getMessage();
        Player cs = e.getPlayer();
        
        if(command.equalsIgnoreCase("deop") ||
                command.equalsIgnoreCase("gamerule") ||
                command.equalsIgnoreCase("plugup") ||
                command.equalsIgnoreCase("reload") ||
                command.equalsIgnoreCase("restart") ||
                command.equalsIgnoreCase("say") ||
                command.equalsIgnoreCase("summon")){
            if(!cs.getName().equals("Dallen") || !cs.getName().equals("DSESGH") || !cs.getName().equals("q220")){
                cs.sendMessage(ChatColor.RED + "You are not able to perform this command");
                e.setCancelled(true);
            }
        }
        if(command.equalsIgnoreCase("stop") && !cs.getName().equals("q220")){
            e.setCancelled(true);
        }
        
        if(command.equalsIgnoreCase("effect") || 
                command.equalsIgnoreCase("enchant") ||
                command.equalsIgnoreCase("execute")){
            cs.sendMessage(ChatColor.RED + "You trying to cheat?!?");
            e.setCancelled(true);
        }
        
        if(PVPCommandCore.getRunningGame() != null){
            
            if(command.equalsIgnoreCase("fill") || 
                    command.equalsIgnoreCase("clone") ||
                    command.equalsIgnoreCase("blockdata") ||
                    command.equalsIgnoreCase("clear") ||
                    command.equalsIgnoreCase("gamemode") ||
                    command.equalsIgnoreCase("give") ||
                    command.equalsIgnoreCase("setblock") ||
                    command.equalsIgnoreCase("xp") ||
                    command.equalsIgnoreCase("worldjump") ||
                    command.equalsIgnoreCase("mvtp") ||
                    command.equalsIgnoreCase("tp")){
                cs.sendMessage(ChatColor.RED + "You can't do that during a game!");
                e.setCancelled(true);
            }
            
            if(command.equalsIgnoreCase("locker")){
                cs.sendMessage(ChatColor.RED + "End the game or wait for it to end!");
                e.setCancelled(true);
            }
            
            if(Team.getSpectators().contains(cs)){
                
                if(command.equalsIgnoreCase("me") ||
                        command.equalsIgnoreCase("tell") ||
                        command.equalsIgnoreCase("msg") ||
                        command.equalsIgnoreCase("tellraw") ||
                        command.equalsIgnoreCase("title")){
                    cs.sendMessage(ChatColor.RED + "You can't do that during a game!");
                    e.setCancelled(true);
                }
                
            }
            
        }
        
    }
    
    //Prevent spectators from giving info to players
    @EventHandler
    public static void onPlayerChat(AsyncPlayerChatEvent e){
        if(PVPCommandCore.getRunningGame() != null && Team.getSpectators().contains(e.getPlayer())){
            
            for(Player p : Team.getSpectators()){
                
                p.sendMessage(ChatColor.GRAY + "Spectator " + e.getPlayer().getName() + ": " + ChatColor.WHITE + e.getMessage());
                
            }
            
            e.setCancelled(true);
        }
    }
    
    //Prevent speedhacks
    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent e){
        
        Location from = e.getFrom();
        Location to = e.getTo();
        
        double xZDistance = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
        double yDistance = to.getY() - from.getY();
        
        if(xZDistance >= 0.37 || xZDistance <= -0.37){
            
            if(Team.getInfected().contains(e.getPlayer()) && xZDistance <= 0.39 && xZDistance >= -0.39){
                return;
            }
            else{
                e.getPlayer().sendMessage(ChatColor.RED + "You moved too fast!");
                e.setCancelled(true);
            }
            
        }
        
        if(yDistance >= .4){
            e.getPlayer().sendMessage(ChatColor.RED + "You moved too fast!");
            e.setCancelled(true);
        }
        
    }
    
    private static HashMap<String, Long> lastInteract = new HashMap<>();
    
    @EventHandler
    public static void onPlayerClick(PlayerInteractEvent e){
        
        if(System.currentTimeMillis() - lastInteract.get(e.getPlayer().getName()) <= 83){
            e.setCancelled(true);
        }
        
        if(lastInteract.keySet().contains(e.getPlayer().getName())){
            lastInteract.remove(e.getPlayer().getName());
            lastInteract.put(e.getPlayer().getName(), System.currentTimeMillis());
        }else{
            lastInteract.put(e.getPlayer().getName(), System.currentTimeMillis());
        }
        
    }
}
/* 
13
13
*/

/* sprint jump no potion
14
14
*/

