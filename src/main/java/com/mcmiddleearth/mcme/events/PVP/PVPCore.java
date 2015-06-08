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
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import com.mcmiddleearth.mcme.events.summerevent.SummerCore;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class PVPCore {
    
    @Getter
    private static ArrayList<String> Playing = new ArrayList<>();
    
    public void onEnable(){
        File loc = new File(SummerCore.getSaveLoc() + Main.getFileSep() + "Maps");
        HashMap<String, Object> maps = new HashMap<>();
        maps.putAll(DBmanager.loadAllObj(Map.class, loc));
        for(String k : maps.keySet()){
            Map.maps.put(k, (Map) maps.get(k));
        }
        PluginManager pm = Main.getServerInstance().getPluginManager();
        pm.registerEvents(new MapEditor(), Main.getPlugin());
        pm.registerEvents(new PlayerStat.StatLitener(), Main.getPlugin());
        pm.registerEvents(new Lobby.SignClickListener(), Main.getPlugin());
        
    }
    
    public void onDisable(){
        
    }
}
