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
package com.mcmiddleearth.mcme.events.summerevent.PVP;

import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import com.mcmiddleearth.mcme.events.summerevent.SummerCore;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class PVPCore {
    
    public void onEnable(){
        File loc = new File(SummerCore.getSaveLoc() + Main.getFileSep() + "Maps");
        HashMap<String, Object> maps = new HashMap<>();
        maps.putAll(DBmanager.loadAllObj(Map.class, loc));
        for(String k : maps.keySet()){
            Map.maps.put(k, (Map) maps.get(k));
        }
    }
    
    public void onDisable(){
        
    }
}
