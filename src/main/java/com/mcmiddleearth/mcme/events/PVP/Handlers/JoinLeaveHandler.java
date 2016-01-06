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
import com.mcmiddleearth.mcme.events.PVP.Lobby;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class JoinLeaveHandler implements Listener{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        final Player p = e.getPlayer();
        PlayerStat.loadStat(p);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            @Override
            public void run(){
                p.setMaxHealth(20);
                try {
                    System.out.println(DBmanager.getJSonParser().writeValueAsString(PlayerStat.getPlayerStats()));
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(JoinLeaveHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(!p.isOp()){
                    if(!p.hasPlayedBefore()){
                        p.teleport(p.getWorld().getSpawnLocation());
                    }else{
                        p.teleport(new Location(p.getWorld(), 346, 40, 513));
                    }
                }else{
                    p.sendMessage("Welcome master " + p.getName().toLowerCase());
                }
            }
        }, 20);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(PVPCore.getPlaying().containsKey(e.getPlayer().getName())){
            Map.maps.get(PVPCore.getPlaying().get(e.getPlayer().getName())).playerLeave(e.getPlayer());
        }
        PlayerStat.getPlayerStats().get(e.getPlayer().getName()).saveStat();
        PlayerStat.getPlayerStats().remove(e.getPlayer().getName());
        e.setQuitMessage("");
    }
}
