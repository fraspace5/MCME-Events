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

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class Lobby {
    @Getter @Setter
    private static HashMap<String, Map> Maps = new HashMap<String, Map>();
    
    @Getter @Setter
    private static String world;
    
    public Lobby(){}
    
    public static void LoadLobby(){
        
    }
    
    public static class SignClickListener implements Listener{
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(e.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(world)){
                if(e.getClickedBlock().getState() instanceof Sign){
                    Sign s = (Sign) e.getClickedBlock().getState();
                    if(Maps.containsKey(s.getLine(0))){
                        if(Maps.get(s.getLine(0)).playerJoin(e.getPlayer())){
                            e.getPlayer().sendMessage("Joining Map...");
                        }else{
                            e.getPlayer().sendMessage("Failed to Join Map");
                        }
                    }
                }
            }
        }
    }
}
