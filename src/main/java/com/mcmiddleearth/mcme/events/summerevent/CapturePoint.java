/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mcmiddleearth.mcme.summerevent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingWorker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Donovan
 */
public class CapturePoint extends JavaPlugin {
    public Location center;
    public int radius;
    public CapturePoint(Location loc, int radius){
        center = loc;
        this.radius = radius;
    }
    public void caping(Team t){
        t.score+=1;
    }
    public class CapRunner extends BukkitRunnable{
        @Override
        public void run(){
            
        }
    }
    public void onEnable(){
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            HashMap<String, Date> times = new HashMap<>();
            @Override
            public void run() {
                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                    if(/*you will need the capture point class to have is caping return a bool for
                            weather the location is cloase enough to the point center*/Point.isCaping(p)){
                        if(times.containsKey(p.getName())){ //now.getTime() - cooltime.getTime() > 2*1000
                            if(new Date().getTime() - times.get(p.getName()).getTime() > 2*1000){
                                
                            }
                        }else{
                            times.put(p.getName(), new Date());
                        }
                    }
                }
            }
        }, 20, 20);
    }
}
