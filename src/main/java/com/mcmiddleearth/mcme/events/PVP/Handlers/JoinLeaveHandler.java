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

import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
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
        
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run(){
                    if(!p.hasPlayedBefore()){
                        p.teleport(p.getWorld().getSpawnLocation());
                    }else{
                        p.teleport(new Location(p.getWorld(), 346, 40, 513));
                    }
                }
            }, 20);
    }
    
    public void onPlayerLeave(PlayerQuitEvent e){
        if(PVPCore.getPlaying().containsKey(e.getPlayer().getName())){
            if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("players").hasPlayer(e.getPlayer())){
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam("players").removePlayer(e.getPlayer());
            }
        }
    }
}
