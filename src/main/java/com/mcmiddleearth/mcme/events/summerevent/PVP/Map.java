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

import com.mcmiddleearth.mcme.events.Util.EventLocation;
import com.mcmiddleearth.mcme.events.summerevent.PVP.Gamemode.Gamemode;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class Map {
    @Getter @Setter
    private int Max;

    @Getter @Setter @JsonIgnore
    private int Curr;

    @Getter @Setter
    private EventLocation LobbySign;
    
    @Getter @Setter
    private Gamemode gm;
    
    @Getter @Setter
    private EventLocation Spawn;
    
    @Getter @Setter
    private String name;
    
    @Getter @Setter
    private HashMap<String, EventLocation> ImportantPoints = new HashMap<>();
    
    @Getter @Setter
    private ArrayList<EventLocation> spawnPoints = new ArrayList<>();

    public static HashMap<String, Map> maps = new HashMap<>();
    
    public Map(){}
    
    public Map(Location spawn){
        this.Spawn = new EventLocation(spawn);
    }
    
    public void bindSign(Location sign){
        this.LobbySign = new EventLocation(sign);
        Sign s = (Sign) sign.getBlock().getState();
        s.setLine(0, name);
        s.setLine(1, gm.getClass().getName().replace("1", " "));
        s.setLine(2, Curr+"/"+Max);
    }
    
    public boolean playerJoin(Player p){
        if(Max >= Curr){
            return false;
        }
        p.teleport(Spawn.toBukkitLoc());
        
        Curr++;
        Sign s = (Sign) LobbySign.toBukkitLoc().getBlock().getState();
        s.setLine(2, Curr+"/"+Max);
        if(Max == Curr){
            gm.Start(this);
        }
        return true;
    }
    
}
