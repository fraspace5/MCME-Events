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
package com.mcmiddleearth.mcme.events.winterevent.SnowManInvasion.EventHandles;

import com.mcmiddleearth.mcme.events.winterevent.SnowManInvasion.InvasionCore;
import com.mcmiddleearth.mcme.events.winterevent.SnowManInvasion.SpawnPoint;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Donovan
 */
public class SignListener implements Listener{
    
    @EventHandler
    public void onSignPlace(SignChangeEvent e){
        if(e.getPlayer().hasPermission("WinterEvent.Invasion")){
            if(e.getLine(0).equalsIgnoreCase("$spawn$")){
                try{
                    int rad = Integer.parseInt(e.getLine(1));
                    if(InvasionCore.SP.containsKey(e.getBlock().getLocation().getWorld().getName())){
                        InvasionCore.SP.get(e.getBlock().getLocation().getWorld().getName()).add(new SpawnPoint(e.getBlock().getLocation(), rad));
                    }else{
                        InvasionCore.SP.put(e.getBlock().getLocation().getWorld().getName(), new ArrayList<>(Arrays.asList(new SpawnPoint[] {new SpawnPoint(e.getBlock().getLocation(), rad)})));
                    }
                    e.setLine(0, "");
                    e.setLine(1, ChatColor.RED + "#SPAWNPOINT#");
                    e.setLine(2, "");
                    e.setLine(3, "");
                }catch(IndexOutOfBoundsException | NumberFormatException ex){
                    e.setLine(0, "#ERROR#");
                }
            }
        }
    }
    
}
