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
package com.mcmiddleearth.mcme.events.PVP.Gamemode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.Util.EventLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class TeamConquest implements Gamemode {//Handled by plugin, should be done needs testing
    
    
    
    @Getter @JsonIgnore
    ArrayList<Player> players = new ArrayList<>();
    
    @Getter
    private Team BlueTeam = new Team("Blue", GameMode.ADVENTURE); 
    
    @Getter
    private Team RedTeam = new Team("Red", GameMode.ADVENTURE); 
    
    @Getter
    private Team SpectatingTeam = new Team("Spectator", GameMode.SPECTATOR); 
    
    /*private int Time = 1800;//30 min game (can be changed as needed
            
    private Date StartTime;*/ //not used atm
    
    @Getter
    private static final ArrayList<String> reqPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn",
        "BlueSpawn",
        "SpectatorSpawn",
        "Point1",
        "Point2",
        "Point3",
        "Point4",
        "Point5",
        "Point6",
        "Point7"
    }));
    
    Map map;
    
    int count = 10;
    
    boolean running = false;
    
    GameEvents events;
    
    Runnable tick = new Runnable(){
            @Override
            public void run() {
                if(running){
                    RedTeam.score += RedTeam.getPoints().size();
                    BlueTeam.score += BlueTeam.getPoints().size();
                    if(RedTeam.getScore() >= 300){
                        for(Player p : players){
                            p.sendMessage(ChatColor.RED + "Game over!");
                            p.sendMessage(ChatColor.RED + "Red Team Wins!");
                        }
                        End(map);
                    }else if(BlueTeam.getScore() >= 300){
                        for(Player p : players){
                            p.sendMessage(ChatColor.BLUE + "Game over!");
                            p.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                        }
                        End(map);
                    }
                }
            }
        };
    
    public TeamConquest(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), tick, 0, 200);//every ten seconds
    }
    
    @Override
    public void Start(Map m) {
        this.map = m;
        if(!m.getImportantPoints().keySet().containsAll(reqPoints)){
            for(Player p : players){
                p.sendMessage(ChatColor.GREEN + "Game Cannot Start! Map maker f**ked up!");
            }
            End(m);
            return;
        }
        events = new GameEvents();
        for(Location l : events.points){
            l.getBlock().setType(Material.BEACON);
        }
        for(Player p : players){
            p.sendMessage("selecting teams");
            if(BlueTeam.getPlayers().size() < 16 && RedTeam.getPlayers().size() < 16){
                if(BlueTeam.getPlayers().size() > RedTeam.getPlayers().size()){
                    RedTeam.addToTeam(p);
                    p.sendMessage(ChatColor.RED + "You are on the Red Team!");
                    p.teleport(m.getImportantPoints().get("RedSpawn").toBukkitLoc());
                }else if(BlueTeam.getPlayers().size() < RedTeam.getPlayers().size()){
                    BlueTeam.addToTeam(p);
                    p.sendMessage(ChatColor.BLUE + "You are on the Blue Team!");
                    p.teleport(m.getImportantPoints().get("BlueSpawn").toBukkitLoc());
                }
            }else{
                SpectatingTeam.addToTeam(p);
                p.sendMessage(ChatColor.GRAY + "You are Spectating!");
                p.teleport(m.getImportantPoints().get("SpectatorSpawn").toBukkitLoc());
                p.setGameMode(SpectatingTeam.getGamemode());
            }
        }
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run() {
                    if(count == 0){
                        for(Player p : RedTeam.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.teleport(map.getImportantPoints().get("RedTeam").toBukkitLoc());
                            p.setGameMode(RedTeam.getGamemode());
                        }
                        for(Player p : BlueTeam.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.teleport(map.getImportantPoints().get("BlueTeam").toBukkitLoc());
                            p.setGameMode(BlueTeam.getGamemode());
                        }
                        running = true;
                    }else{
                        for(Player p : players){
                            p.sendMessage(ChatColor.GREEN + "Game begins in " + count);
                        }
                        count--;
                    }
                }

            }, 20, 11);
    }
    
    
    
    @Override
    public void End(Map m){
        running = false;
    }
    
    private class Team{
        
        @Getter
        private String name;
        
        @Getter
        private int score;
        
        @Getter
        private ArrayList<Location> points = new ArrayList<>();
        
//        @Getters
//        private HashMap<String, Integer> Classes = new HashMap<>();
        
        @Getter
        private ArrayList<Player> players = new ArrayList<>();
        
        @Getter
        private GameMode gamemode;
        
        public Team(String name, GameMode gamemode){
            this.name = name;
            this.gamemode = gamemode;
            score = 0;
        }
        
        public void addToTeam(Player p){
            players.add(p);
        }
        
    }
    
    private class GameEvents implements Listener{
        
        private ArrayList<Location> points = new ArrayList<>();
        
        HashMap<Location, Integer> capAmount = new HashMap<>();//red = +; blue = -
        
        public GameEvents(){
            for(Entry<String, EventLocation> e : map.getImportantPoints().entrySet()){
                if(e.getKey().contains("point")){
                    points.add(e.getValue().toBukkitLoc());
                    capAmount.put(e.getValue().toBukkitLoc(), 0);
                }
            }
        }
        
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(running && players.contains(e.getPlayer()) && 
                    e.getClickedBlock().getType().equals(Material.BEACON) &&
                    e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                e.setUseInteractedBlock(Event.Result.DENY);
                int cap = capAmount.get(e.getClickedBlock().getLocation());
                if(cap >= 50){
                    Player p = e.getPlayer();
                    if(RedTeam.getPlayers().contains(p)){
                        cap++;
                        p.sendMessage(ChatColor.RED + "Cap at " + (cap * 2) + "%");
                        if(cap >= 50){
                            p.sendMessage(ChatColor.RED + "Point Captured!");
                        }
                    }else if(BlueTeam.getPlayers().contains(p)){
                        cap--;
                        p.sendMessage(ChatColor.BLUE + "Cap at " + (cap * -2) + "%");
                        if(cap <= -50){
                            p.sendMessage(ChatColor.BLUE + "Point Captured!");
                        }
                    }
                    capAmount.put(e.getClickedBlock().getLocation(), cap);
                }
            }
        }
    }
}
