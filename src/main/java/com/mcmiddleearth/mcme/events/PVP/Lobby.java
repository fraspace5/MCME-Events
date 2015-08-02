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

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
                if(e.getClickedBlock().getState() instanceof Sign){
                    Sign s = (Sign) e.getClickedBlock().getState();
                    String title = s.getLine(0).replace(ChatColor.YELLOW + "" + ChatColor.BOLD, "");
                    String gamemode = s.getLine(1).replace(ChatColor.BLUE + "" + ChatColor.BOLD, "");
                    Map m = Map.findMap(title, gamemode);
                    if(!m.getGm().getPlayers().contains(e.getPlayer()) && !PVPCore.getPlaying().containsKey(e.getPlayer().getName())){
                        if(m.playerJoin(e.getPlayer())){
                            e.getPlayer().sendMessage("Joining Map...");
                            Bukkit.broadcastMessage(e.getPlayer().getName() + " Joined");
                        }else{
                            e.getPlayer().sendMessage("Failed to Join Map");
                        }
                    }else{
                        e.getPlayer().sendMessage("You are already part of this game");
                        if(e.getPlayer().getName().equalsIgnoreCase("Despot666")){
                            e.getPlayer().kickPlayer("<3 -Dallen");
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
