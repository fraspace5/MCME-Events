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
import com.mcmiddleearth.mcme.events.PVP.Gamemode.FreeForAll;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Infected;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.RingBearer;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Siege;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.TeamDeathmatch;
import com.mcmiddleearth.mcme.events.Util.CLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            if(cmnd.getName().equalsIgnoreCase("pvp") && args.length > 0){
                if(args[0].equalsIgnoreCase("lobby")){
                    p.sendMessage("Sending Signs");
                    for(Map m : Map.maps.values()){
                        ItemStack sign = new ItemStack(Material.SIGN);
                        ItemMeta im = sign.getItemMeta();
                        im.setDisplayName(m.getName());
                        String gamemode = "none";
                        if(m.getGm() != null){
                            gamemode = m.getGmType();
                        }
                        im.setLore(Arrays.asList(new String[] {m.getName(), 
                            gamemode,
                            String.valueOf(m.getMax())}));
                        sign.setItemMeta(im);
                        p.getInventory().addItem(sign);
                    }
                }else if(args[0].equalsIgnoreCase("map")){
                    if(args.length > 2){
                        if(Map.maps.containsKey(args[1])){
                            Map m = Map.maps.get(args[1]);
                            if(args[2].equalsIgnoreCase("spawn")){
                                m.setSpawn(new EventLocation(p.getLocation()));
                                p.sendMessage("Map spawn set");
                            }else if(args[2].equalsIgnoreCase("poi") && args.length > 3){
                                if(args[3].equalsIgnoreCase("help")){
                                    for(Entry<String, String> e: poiHelp.entrySet()){
                                        p.sendMessage(e.getKey() + " - " + e.getValue());
                                    }
                                }else if(args[3].equalsIgnoreCase("SpawnPoint")){
                                    m.getSpawnPoints().add(new EventLocation(p.getLocation()));
                                }else{
                                    m.getImportantPoints().put(args[3], new EventLocation(p.getLocation()));
                                }
                            }else if(args[2].equalsIgnoreCase("setMax") && args.length > 3){
                                m.setMax(Integer.parseInt(args[3]));
                                p.sendMessage("Max players set to " + args[3]);
                            }else if(args[2].equalsIgnoreCase("setName") && args.length > 3){
                                m.setName(args[3]);
                                p.sendMessage("map name set to " + args[3]);
                            }else if(args[2].equalsIgnoreCase("setGamemode") && args.length > 3){
                                if(args[3].equalsIgnoreCase("Freeforall")){
                                    m.setGm(new FreeForAll());
                                    m.setGmType("Free For All");
                                    if(!m.getImportantPoints().containsKey("RedBlock")){
                                        p.sendMessage("WARNING: there is not yet a redblock location for this map!");
                                    }
                                }else if(args[3].equalsIgnoreCase("Infected")){
                                    m.setGm(new Infected());
                                    m.setGmType("Infected");
                                    if(!m.getImportantPoints().containsKey("RedBlock")){
                                        p.sendMessage("WARNING: there is not yet a redblock location for this map!");
                                    }
                                }else if(args[3].equalsIgnoreCase("Ringbearer")){
                                    m.setGm(new RingBearer());
                                    m.setGmType("RignBearer");
                                }else if(args[3].equalsIgnoreCase("TeamDeathmatch")){
                                    m.setGm(new TeamDeathmatch());
                                    m.setGmType("Team Deathmatch");
                                    if(!m.getImportantPoints().containsKey("RedBlock")){
                                        p.sendMessage("WARNING: there is not yet a redblock location for this map!");
                                    }
                                }else if(args[3].equalsIgnoreCase("Siege")){
                                    m.setGm(new Siege());
                                    m.setGmType("Siege");
                                }
                                p.sendMessage("Gamemode set to " + args[3]);
                            }
                        }else{
                            if(args[2].equalsIgnoreCase("spawn")){
                                p.sendMessage("Creating new map");
                                Map.maps.put(args[1], new Map(p.getLocation(), args[1]));
                                System.out.println(args[1]);
                            }else{
                                p.sendMessage("No such map!");
                            }
                        }
                    }else{
                        if(args.length > 1){
                            if(args[1].equalsIgnoreCase("list")){
                                try {
                                    p.sendMessage(Arrays.toString(Map.maps.values().toArray()));
                                } catch (Exception ex) {
                                    Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
}
