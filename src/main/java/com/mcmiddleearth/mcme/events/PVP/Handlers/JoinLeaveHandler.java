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
package com.mcmiddleearth.mcme.events.PVP.Handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode.GameState;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Gamemode;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.anticheat.AntiCheatListeners;
import com.mcmiddleearth.mcme.events.PVP.Lobby;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCommandCore;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.PVP.Team;
import com.mcmiddleearth.mcme.events.Util.Thompson;
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class JoinLeaveHandler implements Listener{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        final Player p = e.getPlayer();
        PlayerStat.loadStat(p);
        
        if(PVPCommandCore.getRunningGame() != null){
            e.setJoinMessage(ChatColor.GRAY + e.getPlayer().getName() + " has joined as a spectator!");
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            @Override
            public void run(){
                
                if(!p.isDead()){
                    p.setHealth(20);
                }
                
                p.setTotalExperience(0);
                p.setExp(0);
                p.setGameMode(GameMode.ADVENTURE);
                p.getInventory().clear();
                p.setPlayerListName(ChatColor.WHITE + p.getName());
                p.setDisplayName(ChatColor.WHITE + p.getName());
                p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                    new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                try {
                    System.out.println(DBmanager.getJSonParser().writeValueAsString(PlayerStat.getPlayerStats()));
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(JoinLeaveHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                for(String playerName : ChatHandler.getPlayerColors().keySet()){
                    Bukkit.getPlayer(playerName).setPlayerListName(ChatHandler.getPlayerColors().get(playerName) + playerName);
                    Bukkit.getPlayer(playerName).setDisplayName(ChatHandler.getPlayerColors().get(playerName) + playerName);
                }
                
                if(PVPCommandCore.getRunningGame() == null && PVPCommandCore.getQueuedGame() == null){
                    p.teleport(new Location(p.getWorld(), 346, 40, 513));
                    ChatHandler.getPlayerColors().put(p.getName(), org.bukkit.ChatColor.WHITE);
                }else{
                    Map m = null;
                    if(PVPCommandCore.getQueuedGame() != null){
                        m = PVPCommandCore.getQueuedGame();
                    }
                    else if(PVPCommandCore.getRunningGame() != null){
                        m = PVPCommandCore.getRunningGame();
                    }
                    else{
                        return;
                    }
                          
                    if(m.getGm().getState() != GameState.IDLE){
                        p.teleport(m.getSpawn().toBukkitLoc().add(0, 2, 0));
                        p.setScoreboard(BasePluginGamemode.getScoreboard());
                        p.sendMessage(ChatColor.GREEN + "Current Game: " + ChatColor.BLUE + m.getGmType() + ChatColor.GREEN + " on " + ChatColor.RED + m.getTitle());
                          
                        if(m.getGm().isMidgameJoin()){
                            p.sendMessage(ChatColor.YELLOW + "Use /pvp join to join the game!");
                        }else{
                            p.sendMessage(ChatColor.YELLOW + "Sorry, you can't join this game midgame");
                            p.sendMessage(ChatColor.YELLOW + "You can join the next game, though!");
                        }
                        Team.addToTeam(p, Team.Teams.SPECTATORS);
                    }
                    else{
                        p.teleport(new Location(p.getWorld(), 346, 40, 513));
                        p.sendMessage(ChatColor.GREEN + "Upcoming Game: " + ChatColor.BLUE + m.getGmType() + ChatColor.GREEN + " on " + ChatColor.RED + m.getTitle());
                        p.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/pvp join" + ChatColor.YELLOW + " to join!");
                        p.sendMessage(ChatColor.GREEN + "Do /pvp rules " + PVPCommandCore.removeSpaces(PVPCommandCore.getQueuedGame().getGmType()) + " if you don't know how this gamemode works!");
                    }
                }
            }
        }, 20);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        
        if(PVPCommandCore.getQueuedGame() != null){
            PVPCommandCore.getQueuedGame().getGm().getPlayers().remove(e.getPlayer());
        }
        else if(PVPCommandCore.getRunningGame() != null){
            PVPCommandCore.getRunningGame().getGm().getPlayers().remove(e.getPlayer());
        }
        
        PlayerStat.getPlayerStats().get(e.getPlayer().getName()).saveStat();
        PlayerStat.getPlayerStats().remove(e.getPlayer().getName());
        Thompson.getInst().farwell(e.getPlayer());
        
        if(PVPCommandCore.getRunningGame() != null){
            e.setQuitMessage(ChatHandler.getPlayerColors().get(e.getPlayer().getName()) + e.getPlayer().getName() + ChatColor.GRAY + " left the fight!");
        }
        
        AntiCheatListeners.getLastInteract().remove(e.getPlayer().getName());
        
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
            new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
        
        Team.removeFromTeam(e.getPlayer());
    }
}
