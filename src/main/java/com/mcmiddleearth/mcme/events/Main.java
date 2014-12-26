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
package com.mcmiddleearth.mcme.events;

import java.util.ArrayList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Donovan
 */
public class Main extends JavaPlugin{
    
    @Getter
    private static Main plugin;
    
    @Getter
    private String spawnWorld;
    
    @Getter
    private ArrayList<String> noHunger = new ArrayList<String>();
    
    @Override
    public void onEnable(){
        plugin = this;
        this.saveDefaultConfig();
        this.reloadConfig();
        if(this.getConfig().contains("worlds")){
            for(String s : this.getConfig().getStringList("worlds")){
                Bukkit.getServer().getWorlds().add(Bukkit.getServer().createWorld(new WorldCreator(s)));
            }
        }
        if(this.getConfig().contains("noHunger")){
            noHunger.addAll(this.getConfig().getStringList("noHunger"));
        }
    }
    
    @Override
    public void onDisable(){
        
    }
}
