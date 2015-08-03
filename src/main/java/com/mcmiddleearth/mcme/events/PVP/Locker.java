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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class Locker implements CommandExecutor, Listener{
    
    private static volatile boolean locked = false;
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        if(cs.isOp()){
            if(args[0].equalsIgnoreCase("kickall")){
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(!p.isOp()){
                        p.kickPlayer("admin kicked all players");
                    }
                }
                cs.sendMessage("Kicked all!");
            }else if(args[0].equalsIgnoreCase("lock")){
                if(locked){
                    cs.sendMessage("Server Unlocked!");
                    locked=false;
                }else{
                    cs.sendMessage("Server Locked!");
                    locked=true;
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!p.isOp()){
                            p.kickPlayer("Server locked");
                        }
                    }
                }
            }
        }
        return true;
    }
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent e){
        System.out.println("Locked Ping!");
        if(locked){
            System.out.println("Locked Ping!");
            e.setMotd(ChatColor.BLUE + "server locked");
            e.setMaxPlayers(0);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        System.out.println("Locked Join!");
        if(locked && !e.getPlayer().isOp()){
            e.getPlayer().kickPlayer("Server is locked");
        }
    }
}
