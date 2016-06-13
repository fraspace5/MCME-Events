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
 * m
 * You should have received a copy of the GNU General Public License
 * along with MCME-Events.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.mcmiddleearth.mcme.events.PVP.Gamemode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import com.mcmiddleearth.mcme.events.PVP.maps.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCommandCore;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.PVP.Team;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author donoa_000
 */
public abstract class BasePluginGamemode implements Gamemode{
    @Getter @JsonIgnore
    ArrayList<Player> players = new ArrayList<>();
    
    public enum GameState {
        IDLE, COUNTDOWN, RUNNING
    }
    
    @Getter
    private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    
    public void playerLeave(Player p){
        players.remove(p);
    }
    
    @Override
    public void Start(Map m, int parameter){
        PVPCommandCore.toggleVoxel(true);
        
        for(Player p : players){
            PlayerStat.getPlayerStats().get(p.getName()).addPlayedGame();
        }
        
    };
    
    @Override
    public void End(Map m){
        PVPCommandCore.setRunningGame(null);
        PVPCommandCore.toggleVoxel(false);
        
        Bukkit.getScheduler().cancelAllTasks();
        for(Objective o : scoreboard.getObjectives()){
            o.unregister();
        }
      
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            ChatHandler.getPlayerColors().put(p.getName(), ChatColor.WHITE);
            p.teleport(PVPCore.getSpawn());
            p.setDisplayName(ChatColor.WHITE + p.getName());
            p.setPlayerListName(ChatColor.WHITE + p.getName());
            p.getInventory().clear();
            p.setTotalExperience(0);
            p.setExp(0);
            p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
            new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
            p.setGameMode(GameMode.ADVENTURE);
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            ChatHandler.getPlayerPrefixes().remove(p.getName());
            
            if(!p.isDead()){
                p.setHealth(20);
            }
        }
    };
    
    
    public boolean midgamePlayerJoin(Player p){
        PlayerStat.getPlayerStats().get(p.getName()).addPlayedGame();
        String message = "";
        
        if(PVPCommandCore.getRunningGame() == null){
            return false;
        }
        
        if(PVPCommandCore.getRunningGame().getGm() instanceof FreeForAll || 
                PVPCommandCore.getRunningGame().getGm() instanceof OneInTheQuiver){
            
            message = ChatHandler.getPlayerColors().get(p.getName()) + p.getName() + ChatColor.GRAY + " has joined the fight!";
            
        }
        
        else if(PVPCommandCore.getRunningGame().getGm() instanceof Ringbearer || 
                PVPCommandCore.getRunningGame().getGm() instanceof TeamConquest ||
                PVPCommandCore.getRunningGame().getGm() instanceof TeamDeathmatch ||
                PVPCommandCore.getRunningGame().getGm() instanceof TeamSlayer){
            
             if(Team.getRedPlayers().contains(p)){
                 message = ChatColor.RED + p.getName() + " has joined the fight on Red Team!";
             }
             else if(Team.getBluePlayers().contains(p)){
                 message = ChatColor.BLUE + p.getName() + " has joined the fight on Blue Team!";
             }
            
        }
        
        else if(PVPCommandCore.getRunningGame().getGm() instanceof Infected){
            
            message = ChatColor.BLUE + p.getName() + " has joined the fight as a Survivor!";
            
        }
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendMessage(message);
        }
        
        return true;
    };
    
}
