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

import com.mcmiddleearth.mcme.events.Event;
import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.JoinLeaveHandler;
import com.mcmiddleearth.mcme.events.PVP.Servlet.PVPServer;
import com.mcmiddleearth.mcme.events.Util.CLog;
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import com.mcmiddleearth.mcme.events.summerevent.SummerCore;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class PVPCore implements Event{
    
    PVPServer server;
    
    @Getter
    private static File saveLoc = new File(Main.getPluginDirectory() + Main.getFileSep() + "PVP");
    
    @Getter @Setter
    private static HashMap<String, String> playing = new HashMap<>();
    
    
    @Getter
    private static ArrayList<String> Playing = new ArrayList<>();
    
    @Override
    public void onEnable(){
        File loc = new File(saveLoc + Main.getFileSep() + "Maps");
        HashMap<String, Object> maps = DBmanager.loadAllObj(Map.class, loc);
        if(maps == null){
            maps = new HashMap<>();
        }
        for(Entry<String, Object> e : maps.entrySet()){
            Map m = (Map) e.getValue();
            if(m.getGmType() != null){
                m.bindGamemode();
            }
            Map.maps.put(e.getKey(), m);
        }
        CLog.println(maps);
        Main.getPlugin().getCommand("pvp").setExecutor(new PVPCommandCore());
        
        PluginManager pm = Main.getServerInstance().getPluginManager();
        pm.registerEvents(new MapEditor(), Main.getPlugin());
        pm.registerEvents(new PlayerStat.StatLitener(), Main.getPlugin());
        pm.registerEvents(new Lobby.SignClickListener(), Main.getPlugin());
        pm.registerEvents(new ChatHandler(), Main.getPlugin());
        pm.registerEvents(new JoinLeaveHandler(), Main.getPlugin());
        try {
            server = new PVPServer(8080);
            server.getServ().start();
        } catch (Exception ex) {
            Logger.getLogger(SummerCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public void onDisable(){
        for(String mn : Map.maps.keySet()){
            Map m = Map.maps.get(mn);
            m.setCurr(0);
            DBmanager.saveObj(m, new File(saveLoc + Main.getFileSep() + "Maps"), mn);
        }
        try {
            server.getServ().stop();
        } catch (Exception ex) {
            Logger.getLogger(SummerCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
