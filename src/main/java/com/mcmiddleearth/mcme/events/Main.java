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

import com.mcmiddleearth.mcme.events.summerevent.SummerCommands;
import com.mcmiddleearth.mcme.events.winterevent.SnowManInvasion.EventHandles.SignListener;
import com.mcmiddleearth.mcme.events.winterevent.SnowManInvasion.EventHandles.SnowballHandle;
import com.mcmiddleearth.mcme.events.winterevent.WinterCommands;
import com.mcmiddleearth.mcme.events.winterevent.SnowballFight.listeners.SnowballListener;
import java.io.File;
import java.util.ArrayList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.jackson.map.ObjectMapper;

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
    
    @Getter
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    @Getter
    private static Server serverInstance;
    
    @Getter
    private static File pluginDirectory;
    
    @Getter
    private static File playerDirectory;
    
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
        this.serverInstance = getServer();
        this.pluginDirectory = getDataFolder();
        if (!pluginDirectory.exists()) {
            pluginDirectory.mkdir();
        }
        this.playerDirectory = new File(pluginDirectory + System.getProperty("file.separator") + "players");
        if (!playerDirectory.exists()) {
            playerDirectory.mkdir();
        }
        this.getCommand("WorldJump").setExecutor(new CommandCore());
        
        PluginManager pm = this.getServer().getPluginManager();
        
        boolean Winter = this.getConfig().getBoolean("WinterEvent.Enabled");
        boolean Summer = this.getConfig().getBoolean("SummerEvent.Enabled");
        if(Winter && Summer){
            this.getCommand("winter").setExecutor(new WinterCommands());
            this.getCommand("summer").setExecutor(new SummerCommands());
            registerHandles(true, pm);
            registerHandles(false, pm);
        }else{
            if(Winter){
                this.getCommand("winter").setExecutor(new WinterCommands());
                this.getCommand("event").setExecutor(new WinterCommands());
                registerHandles(false, pm);
            }else if(Summer){
                this.getCommand("summer").setExecutor(new SummerCommands());
                this.getCommand("event").setExecutor(new SummerCommands());
                registerHandles(true, pm);
            }
        }
    }
    
    @Override
    public void onDisable(){
        
    }
    
    private void registerHandles(boolean summer, PluginManager pm){
        if(summer){
            
        }else{
            pm.registerEvents(new SnowballListener(), this);
            pm.registerEvents(new SignListener(), this);
            pm.registerEvents(new SnowballHandle(), this);
        }
    }
}
