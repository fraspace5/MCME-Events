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
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.Util.EventLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.ItemMeta.Spigot;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

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
    
    @Getter
    private Objective Points;
    
    @Getter
    private Scoreboard Score;
    
    @Getter
    private int target = 100;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn",
        "BlueSpawn",
        "SpectatorSpawn",
        "Point1",
        "Point2",
        "Point3"
    }));
    
    Map map;
    
    int count;
    
    @Getter
    private boolean Running = false;
    
    GameEvents events;
    
    boolean hasTeams = false;
    
    Runnable tick = new Runnable(){
            @Override
            public void run() {
                if(Running){
                    RedTeam.score += RedTeam.getPoints().size();
                    BlueTeam.score += BlueTeam.getPoints().size();
                    if(RedTeam.score > target){
                        RedTeam.score = target;
                    }
                    if(BlueTeam.score > target){
                        BlueTeam.score = target;
                    }
                    Points.getScore(ChatColor.BLUE + "Blue:").setScore(BlueTeam.score);
                    Points.getScore(ChatColor.RED + "Red:").setScore(RedTeam.score);
                    System.out.println(RedTeam.score + ", " + BlueTeam.score);
                    if(RedTeam.getScore() >= target){
                        for(Player p : players){
                            p.sendMessage(ChatColor.RED + "Game over!");
                            p.sendMessage(ChatColor.RED + "Red Team Wins!");
                        }
                        End(map);
                    }else if(BlueTeam.getScore() >= target){
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
        count = 10;
        this.map = m;
        if(!m.getImportantPoints().keySet().containsAll(NeededPoints)){
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
        PluginManager pm = Main.getServerInstance().getPluginManager();
        pm.registerEvents(events, Main.getPlugin());
        for(Player p : players){
            p.sendMessage("selecting teams");
            if(BlueTeam.getPlayers().size() < 16 && RedTeam.getPlayers().size() < 16){
                if(BlueTeam.getPlayers().size() >= RedTeam.getPlayers().size()){
                    RedTeam.addToTeam(p);
                    p.sendMessage(ChatColor.RED + "You are on the Red Team!");
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.RED + "Red");
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.RED + p.getName());
                    }else{
                        String newName = p.getName().substring(0, 13);
                        p.setPlayerListName(ChatColor.RED + newName);
                    }
                    p.teleport(m.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                }else if(BlueTeam.getPlayers().size() < RedTeam.getPlayers().size()){
                    BlueTeam.addToTeam(p);
                    p.sendMessage(ChatColor.BLUE + "You are on the Blue Team!");
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.BLUE + "Blue");
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.BLUE + p.getName());
                    }else{
                        String newName = p.getName().substring(0, 13);
                        p.setPlayerListName(ChatColor.BLUE + newName);
                    }
                    p.setDisplayName(ChatColor.BLUE + p.getName());
                    p.teleport(m.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                }
            }else{
                SpectatingTeam.addToTeam(p);
                p.sendMessage(ChatColor.GRAY + "You are Spectating!");
                p.teleport(m.getImportantPoints().get("SpectatorSpawn").toBukkitLoc().add(0, 2, 0));
                p.setGameMode(SpectatingTeam.getGamemode());
            }
        }
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run() {
                    if(count == 0){
                        if(Running){
                            return;
                        }
                        ScoreboardManager sbm = Bukkit.getScoreboardManager();
                        Score = sbm.getNewScoreboard();
                        if(sbm.getMainScoreboard() != null){
                            org.bukkit.scoreboard.Team blu = sbm.getMainScoreboard().getTeam("blue");
                            org.bukkit.scoreboard.Team rd =  sbm.getMainScoreboard().getTeam("red");
                            if(blu != null && rd != null){
                                for(Player p : RedTeam.getPlayers()){
                                    rd.addPlayer(p);
                                }
                                for(Player p : BlueTeam.getPlayers()){
                                    blu.addPlayer(p);
                                }
                            }
                        }
                        Points = Score.registerNewObjective("Score", "dummy");
                        Points.setDisplayName("Score");
                        Points.getScore(ChatColor.BLUE + "Blue:").setScore(0);
                        Points.getScore(ChatColor.RED + "Red:").setScore(0);
                        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
                        for(Player p : RedTeam.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.teleport(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                            p.setGameMode(RedTeam.getGamemode());
                            p.setScoreboard(Score);
                            ItemStack[] items = new ItemStack[] {new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE), 
                                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS),
                                new ItemStack(Material.IRON_SWORD), new ItemStack(Material.BOW)};
                            for(int i = 0; i <= 5; i++){
                                if(i<=3){
                                    LeatherArmorMeta lam = (LeatherArmorMeta) items[i].getItemMeta();
                                    lam.setColor(org.bukkit.Color.fromRGB(153, 51, 51));
                                    items[i].setItemMeta(lam);
                                }else{
                                    items[i].addUnsafeEnchantment(new EnchantmentWrapper(34), 10);
                                }
                                items[i].getItemMeta().spigot().setUnbreakable(true);
                            }
                            p.getInventory().clear();
                            p.getInventory().setHelmet(items[0]);
                            p.getInventory().setChestplate(items[1]);
                            p.getInventory().setLeggings(items[2]);
                            p.getInventory().setBoots(items[3]);
                            p.getInventory().addItem(items[4]);
                            p.getInventory().addItem(items[5]);
                            ItemStack Arrows = new ItemStack(Material.ARROW);
                            Arrows.setAmount(64);
                            p.getInventory().addItem(Arrows);
                            p.getInventory().addItem(Arrows);
                        }
                        for(Player p : BlueTeam.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.teleport(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                            p.setGameMode(BlueTeam.getGamemode());
                            p.setScoreboard(Score);
                            ItemStack[] items = new ItemStack[] {new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE), 
                                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS),
                                new ItemStack(Material.IRON_SWORD), new ItemStack(Material.BOW)};
                            for(int i = 0; i <= 5; i++){
                                if(i<=3){
                                    LeatherArmorMeta lam = (LeatherArmorMeta) items[i].getItemMeta();
                                    lam.setColor(org.bukkit.Color.fromRGB(51, 76, 178));
                                    items[i].setItemMeta(lam);
                                }else{
                                    items[i].addUnsafeEnchantment(new EnchantmentWrapper(34), 10);
                                }
                                items[i].getItemMeta().spigot().setUnbreakable(true);
                                
                            }
                            p.getInventory().clear();
                            p.getInventory().setHelmet(items[0]);
                            p.getInventory().setChestplate(items[1]);
                            p.getInventory().setLeggings(items[2]);
                            p.getInventory().setBoots(items[3]);
                            p.getInventory().addItem(items[4]);
                            p.getInventory().addItem(items[5]);
                            ItemStack Arrows = new ItemStack(Material.ARROW);
                            Arrows.setAmount(64);
                            p.getInventory().addItem(Arrows);
                            p.getInventory().addItem(Arrows);
                        }
                        Running = true;
                        count = -1;
                    }else if(count != -1){
                        for(Player p : players){
                            p.sendMessage(ChatColor.GREEN + "Game begins in " + count);
                        }
                        count--;
                    }
                }

            }, 40, 11);
    }
    
    
    
    @Override
    public void End(Map m){
        Running = false;
        for(Location l : events.points){
            l.getBlock().setType(Material.AIR);
            l.getBlock().getRelative(0, 1, 0).setType(Material.AIR);
        }
        for(Player p : players){
            p.teleport(PVPCore.getSpawn());
            p.setDisplayName(ChatColor.WHITE + p.getName());
            p.getInventory().clear();
            p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
        }
        Score.clearSlot(DisplaySlot.SIDEBAR);
        for(Player p : players){
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        m.playerLeaveAll();
        BlueTeam = new Team("Blue", GameMode.ADVENTURE);
        RedTeam = new Team("Red", GameMode.ADVENTURE);
        if(Bukkit.getScoreboardManager().getMainScoreboard() != null){
            org.bukkit.scoreboard.Team blu = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("blue");
            org.bukkit.scoreboard.Team rd = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("red");
            
            if(blu != null && rd != null){
                for(OfflinePlayer p : blu.getPlayers()){
                    blu.removePlayer(p);
                }
                for(OfflinePlayer p : rd.getPlayers()){
                    rd.removePlayer(p);
                }
            }
        }
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
                if(e.getKey().contains("Point")){
                    points.add(e.getValue().toBukkitLoc());
                    capAmount.put(e.getValue().toBukkitLoc(), 0);
                }
            }
        }
        
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(Running && players.contains(e.getPlayer()) && 
                    e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                if(e.getClickedBlock().getType().equals(Material.BEACON)){
                    e.setUseInteractedBlock(Event.Result.DENY);
                    int cap = capAmount.get(e.getClickedBlock().getLocation());
                    Player p = e.getPlayer();
                    if(RedTeam.getPlayers().contains(p)){
                        if(cap < 50){
                            cap++;
                            p.sendMessage(ChatColor.RED + "Cap at " + (cap * 2) + "%");
                            if(cap >= 50){
                                p.sendMessage(ChatColor.RED + "Point Captured!");
                                if(!RedTeam.points.contains(e.getClickedBlock().getLocation())){
                                    RedTeam.points.add(e.getClickedBlock().getLocation());
                                    Block b = e.getClickedBlock().getLocation().add(0, 1, 0).getBlock();
                                    b.setType(Material.STAINED_GLASS);
                                    b.setData((byte) 14);
                                    for(Player pl : players){
                                        pl.sendMessage("Red Captured ");
                                    }
                                }
                                if(BlueTeam.points.contains(e.getClickedBlock().getLocation())){
                                    BlueTeam.points.remove(e.getClickedBlock().getLocation());
                                }
                            }else{
                                capAmount.put(e.getClickedBlock().getLocation(), cap);
                            }
                        }
                    }else if(BlueTeam.getPlayers().contains(p)){
                        if(cap > -50){
                            cap--;
                            p.sendMessage(ChatColor.BLUE + "Cap at " + (cap * -2) + "%");
                            if(cap <= -50){
                                p.sendMessage(ChatColor.BLUE + "Point Captured!");
                                if(!BlueTeam.points.contains(e.getClickedBlock().getLocation())){
                                    BlueTeam.points.add(e.getClickedBlock().getLocation());
                                    Block b = e.getClickedBlock().getLocation().add(0, 1, 0).getBlock();
                                    b.setType(Material.STAINED_GLASS);
                                    b.setData((byte) 11);
                                }
                                if(RedTeam.points.contains(e.getClickedBlock().getLocation())){
                                    RedTeam.points.remove(e.getClickedBlock().getLocation());
                                }
                            }else{
                                capAmount.put(e.getClickedBlock().getLocation(), cap);
                            }
                        }
                    }
                }
            }
        }
        
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){
            if(Running && players.contains(e.getPlayer())){
                if(RedTeam.getPlayers().contains(e.getPlayer())){
                    e.setRespawnLocation(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                }else if(BlueTeam.getPlayers().contains(e.getPlayer())){
                    e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                }
            }
        }
    }
}
