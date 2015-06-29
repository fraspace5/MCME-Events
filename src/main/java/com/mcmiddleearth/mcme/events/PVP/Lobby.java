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
import com.mcmiddleearth.mcme.events.Util.CLog;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class Lobby {
    @Getter @Setter
    private static String world;
    
    public Lobby(){}
    
    public static void LoadLobby(){
        
    }
    
    public static class SignClickListener implements Listener{
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                CLog.println("Enter1");
                if(e.getClickedBlock().getState() instanceof Sign){
                    Sign s = (Sign) e.getClickedBlock().getState();
                    if(Map.maps.containsKey(s.getLine(0))){
                        CLog.println("Enter2");
                        if(Map.maps.get(s.getLine(0)).playerJoin(e.getPlayer())){
                            e.getPlayer().sendMessage("Joining Map...");
                        }else{
                            e.getPlayer().sendMessage("Failed to Join Map");
                        }
                    }
                }
            }
        }
        
        @EventHandler
        public void onSignChange(SignChangeEvent e){
            if(e.getPlayer().getItemInHand().hasItemMeta()){
                ItemMeta im = e.getPlayer().getItemInHand().getItemMeta();
                if(im.hasLore()){
                    if(Map.maps.containsKey(im.getLore().get(0))){
                        final Map m = Map.maps.get(im.getLore().get(0));
                        m.bindSign(e);
                        world = e.getBlock().getLocation().getWorld().getName();
                    }
                }
            }
        }
    }
    
}
