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

import com.mcmiddleearth.mcme.events.Util.EventLocation;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Free1For1All;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Infected;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.King1of1the1Hill;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.RingBearer;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Siege;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Team1Conquest;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Team1Deathmatch;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class MapEditor implements CommandExecutor, Listener{
    
    private static HashMap<String, String> poiHelp = new HashMap<String, String>() {{
        put("RedBlock", "Redstone Block location for commandblock gamemodes");
        put("SpawnPoint", "Sets a Spawnpoint");
    }};

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if(cs instanceof Player){
            Player p = (Player) cs;
            if(args[0].equalsIgnoreCase("pvp") && args.length > 1){
                if(args[1].equalsIgnoreCase("lobby")){
                    p.sendMessage("Sending Signs");
                    for(Map m : Map.maps.values()){
                        ItemStack sign = new ItemStack(Material.SIGN);
                        ItemMeta im = sign.getItemMeta();
                        im.setDisplayName(m.getName());
                        im.setLore(Arrays.asList(new String[] {m.getName(), 
                            m.getGm().getClass().getName().replace("1", " "),
                            String.valueOf(m.getMax())}));
                        sign.setItemMeta(im);
                        p.getInventory().addItem(sign);
                    }
                }else if(args[1].equalsIgnoreCase("map")){
                    if(args.length > 3){
                        if(Map.maps.containsKey(args[2])){
                            Map m = Map.maps.get(args[2]);
                            if(args[3].equalsIgnoreCase("spawn")){
                                m.setSpawn(new EventLocation(p.getLocation()));
                                p.sendMessage("Map spawn set");
                            }else if(args[3].equalsIgnoreCase("poi") && args.length > 4){
                                if(args[4].equalsIgnoreCase("help")){
                                    for(Entry<String, String> e: poiHelp.entrySet()){
                                        p.sendMessage(e.getKey() + " - " + e.getValue());
                                    }
                                }else if(args[4].equalsIgnoreCase("SpawnPoint")){
                                    m.getSpawnPoints().add(new EventLocation(p.getLocation()));
                                }else{
                                    m.getImportantPoints().put(args[4], new EventLocation(p.getLocation()));
                                }
                            }else if(args[3].equalsIgnoreCase("setMax") && args.length > 4){
                                m.setMax(Integer.parseInt(args[4]));
                            }else if(args[3].equalsIgnoreCase("setName") && args.length > 4){
                                m.setName(args[4]);
                            }else if(args[3].equalsIgnoreCase("setGamemode") && args.length > 4){
                                if(args[4].equalsIgnoreCase("Freeforall")){
                                    m.setGm(new Free1For1All());
                                    if(!m.getImportantPoints().containsKey("RedBlock")){
                                        p.sendMessage("WARNING: there is not yet a redblock location for this map!");
                                    }
                                }else if(args[4].equalsIgnoreCase("Infected")){
                                    m.setGm(new Infected());
                                    if(!m.getImportantPoints().containsKey("RedBlock")){
                                        p.sendMessage("WARNING: there is not yet a redblock location for this map!");
                                    }
                                }else if(args[4].equalsIgnoreCase("Ringbearer")){
                                    m.setGm(new RingBearer());
                                }else if(args[4].equalsIgnoreCase("Team1Deathmatch")){
                                    m.setGm(new Team1Deathmatch());
                                    if(!m.getImportantPoints().containsKey("RedBlock")){
                                        p.sendMessage("WARNING: there is not yet a redblock location for this map!");
                                    }
                                }else if(args[4].equalsIgnoreCase("Siege")){
                                    m.setGm(new Siege());
                                }
                            }
                        }else{
                            if(args[3].equalsIgnoreCase("spawn")){
                                p.sendMessage("Creating new map");
                                Map.maps.put(args[2], new Map(p.getLocation()));
                            }else{
                                p.sendMessage("No such map!");
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
}
