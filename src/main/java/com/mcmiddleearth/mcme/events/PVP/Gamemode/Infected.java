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

import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.Team;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @author Eric
 */
public class Infected extends BasePluginGamemode{
    
    @Getter
    private Team infected = new Team();
    
    @Getter
    private Team survivors = new Team();
    
    @Getter
    private Team spectatingTeam = new Team();
    
    private static boolean eventsRegistered = false;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "InfectedSpawn",
        "SurvivorSpawn",
    }));
    
    @Getter
    private GameState state = GameState.IDLE;
    
    Map map;
    
    private int count = 10;
    
    @Getter
    private Objective Points;
    
    private GameEvents events;
    
    private int time;
    
    Runnable tick = new Runnable(){
        @Override
        public void run(){
            time = Points.getScore(ChatColor.WHITE + "Time:").getScore();
            time--;
            
            Points.getScore(ChatColor.WHITE + "Time:").setScore(time);
            
            if(time == 0){
                String remainingPlayers = "";
                int loopnum = 0;
                for(Player p : Team.getSurvivors()){
                    if(Team.getSurvivors().size() > 1 && loopnum == (Team.getSurvivors().size() - 1)){
                
                        remainingPlayers += (", and " + p.getName());
                    }
                    else if(Team.getSurvivors().size() == 1 || loopnum == 0){
                        remainingPlayers += (" " + p.getName());
                    }
                    else{
                        remainingPlayers += (", " + p.getName());
                    }
            
                    loopnum++;
                }
                
                for(Player p : players){
                    p.sendMessage(ChatColor.BLUE + "Game over!");
                    p.sendMessage(ChatColor.BLUE + "Survivors win!");
                    p.sendMessage(ChatColor.BLUE + "Remaining:" + remainingPlayers);
                }
                
                End(map);
            }
        }
    };
    
    @Override
    public void Start(Map m, int parameter){
        count = 10;
        state = GameState.COUNTDOWN;
        super.Start(m, parameter);
        this.map = m;
        time = parameter;
        
        Random rand = new Random();
        
        if(!map.getImportantPoints().keySet().containsAll(NeededPoints)){
            for(Player p : players){
                p.sendMessage(ChatColor.RED + "Game cannot start! Not all needed points have been added!");
            }
            End(m);
        }
        
        if(!eventsRegistered){
            events = new GameEvents();
            PluginManager pm = Main.getServerInstance().getPluginManager();
            pm.registerEvents(events, Main.getPlugin());
            eventsRegistered = true;
        }
        
        int c = 0;
        int infected = rand.nextInt(players.size());
        for(Player p : players){
            
            if(c == infected){
                Team.addToTeam(p, Team.Teams.INFECTED);
            }
            
            else{
                Team.addToTeam(p, Team.Teams.SURVIVORS);
            }
            
            c++;
        }
        
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            if(!Team.getInfected().contains(player) && !Team.getSurvivors().contains(player)){
                Team.addToTeam(player, Team.Teams.SPECTATORS);
                player.teleport(m.getSpawn().toBukkitLoc().add(0, 2, 0));
            }
        }
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run() {
                    if(count == 0){
                        if(state == GameState.RUNNING){
                            return;
                        }
                        if(Bukkit.getScoreboardManager().getMainScoreboard() != null){
                            org.bukkit.scoreboard.Team blu = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("blue");
                            org.bukkit.scoreboard.Team rd =  Bukkit.getScoreboardManager().getMainScoreboard().getTeam("red");
                            if(blu != null && rd != null){
                                for(Player p : Team.getRedPlayers()){
                                    rd.addPlayer(p);
                                }
                                for(Player p : Team.getBluePlayers()){
                                    blu.addPlayer(p);
                                }
                            }
                        }
                        Points = getScoreboard().registerNewObjective("Remaining", "dummy");
                        Points.setDisplayName("Remaining");
                        Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Team.getSurvivors().size());
                        Points.getScore(ChatColor.DARK_RED + "Infected:").setScore(Team.getInfected().size());
                        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
                        
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                        }
                        
                        for(Player p : Team.getSurvivors()){
                            p.setWalkSpeed(0.2F);
                            p.setScoreboard(getScoreboard());
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
                        for(Player p : Team.getInfected()){

                            p.setWalkSpeed(0.2F);
                            p.setScoreboard(getScoreboard());
                            ItemStack[] items = new ItemStack[] {new ItemStack(Material.LEATHER_CHESTPLATE), 
                                new ItemStack(Material.IRON_SWORD), new ItemStack(Material.BOW)};
                            for(int i = 0; i <= 2; i++){
                                if(i == 0){
                                    LeatherArmorMeta lam = (LeatherArmorMeta) items[i].getItemMeta();
                                    lam.setColor(org.bukkit.Color.fromRGB(153, 51, 51));
                                    items[i].setItemMeta(lam);
                                }else{
                                    items[i].addUnsafeEnchantment(new EnchantmentWrapper(34), 10);
                                }
                                items[i].getItemMeta().spigot().setUnbreakable(true);
                            }
                            p.getInventory().clear();
                            p.getInventory().setChestplate(items[0]);
                            p.getInventory().addItem(items[1]);
                            p.getInventory().addItem(items[2]);
                            ItemStack Arrows = new ItemStack(Material.ARROW);
                            Arrows.setAmount(64);
                            p.getInventory().addItem(Arrows);
                            p.getInventory().addItem(Arrows);
                        }
                        state = GameState.RUNNING;
                        count = -1;
                    }else if(count != -1){
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game begins in " + count);
                        }
                        count--;
                    }
                }

            }, 40, 11);
    }
    
    @Override
    public void End(Map m){
        state = GameState.IDLE;
        
        
        for(Player p : players){
            p.teleport(PVPCore.getSpawn());
            p.setDisplayName(ChatColor.WHITE + p.getName());
            p.getInventory().clear();
            p.setMaxHealth(20);
            p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
            p.setGameMode(GameMode.ADVENTURE);
        }
        for(Player p : players){
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            Team.removeFromTeam(p);
        }
        getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        m.playerLeaveAll();
        survivors = new Team();
        infected = new Team();
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
        super.End(m);
    }
    
    public String requiresParameter(){
        return "time in seconds";
    }
    
    public boolean isMidgameJoin(){
        if(time >= 120){
            return true;
        }
        else{
            return false;
        }
    }
    
    private class GameEvents implements Listener{
        
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent e){
            
            if(e.getEntity() instanceof Player && state == GameState.RUNNING){
                Player p = e.getEntity();

                if(Team.getInfected().contains(p) && Team.getInfected().contains(p.getKiller()) || Team.getSurvivors().contains(p) && Team.getSurvivors().contains(p.getKiller())){
                        return;
                }
                
                if(Team.getBluePlayers().contains(p)){
                    e.setDeathMessage(ChatColor.BLUE + p.getName() + ChatColor.YELLOW + " was infected by " + ChatColor.DARK_RED + p.getKiller().getName());
                    Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() - 1);
                    Team.removeFromTeam(p);
                    Team.addToTeam(p, Team.Teams.INFECTED);
                    
                    ItemStack[] items = new ItemStack[] {new ItemStack(Material.LEATHER_CHESTPLATE), 
                        new ItemStack(Material.IRON_SWORD), new ItemStack(Material.BOW)};
                    for(int i = 0; i <= 2; i++){
                        if(i == 0){
                            LeatherArmorMeta lam = (LeatherArmorMeta) items[i].getItemMeta();
                            lam.setColor(org.bukkit.Color.fromRGB(153, 51, 51));
                            items[i].setItemMeta(lam);
                        }else{
                            items[i].addUnsafeEnchantment(new EnchantmentWrapper(34), 10);
                        }
                            items[i].getItemMeta().spigot().setUnbreakable(true);
                        }
                        p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                            new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                        p.getInventory().clear();
                        p.getInventory().setChestplate(items[0]);
                        p.getInventory().addItem(items[1]);
                        p.getInventory().addItem(items[2]);
                        ItemStack Arrows = new ItemStack(Material.ARROW);
                        Arrows.setAmount(64);
                        p.getInventory().addItem(Arrows);
                        p.getInventory().addItem(Arrows);
                }
                
                if(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() <= 0){
                
                    for(Player player : players){
                        player.sendMessage(ChatColor.DARK_RED + "Game over!");
                        player.sendMessage(ChatColor.DARK_RED + "Infected Wins!");
                    
                    }
                    End(map);
                }
            }
        }
        
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){
            System.out.println("in");
            if(state == GameState.RUNNING){
                e.setRespawnLocation(map.getImportantPoints().get("InfectedSpawn").toBukkitLoc().add(0, 2, 0));
            }
        }
        
        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent e){
            if(state == GameState.RUNNING){

                if(Team.getInfected().contains(e.getPlayer())){
                    Points.getScore(ChatColor.DARK_RED + "Infected:").setScore(Points.getScore(ChatColor.DARK_RED + "Infected:").getScore() - 1);
                }
                if(Team.getBluePlayers().contains(e.getPlayer())){
                    Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() - 1);
                }
                Team.removeFromTeam(e.getPlayer());
                e.getPlayer().getInventory().clear();
                e.getPlayer().getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                   new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                
                if(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() <= 0){
                
                    for(Player player : players){
                        player.sendMessage(ChatColor.BLUE + "Game over!");
                        player.sendMessage(ChatColor.BLUE + "Infected Wins!");
                    
                    }
                    End(map);
                }
            }
        }
    }
    
    @Override
    public boolean midgamePlayerJoin(Player p){
        if(time >= 120){
            Team.addToTeam(p, Team.Teams.SURVIVORS);
            p.teleport(map.getImportantPoints().get("SurvivorSpawn").toBukkitLoc().add(0, 2, 0));
            Points.getScore(ChatColor.BLUE + "Blue:").setScore(Points.getScore(ChatColor.BLUE + "Blue:").getScore() + 1);
            super.midgamePlayerJoin(p);
            
            ItemStack[] items = new ItemStack[] {new ItemStack(Material.LEATHER_HELMET),new ItemStack(Material.LEATHER_CHESTPLATE), 
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
            
            return true;
        }else{
            return false;
        }
    }
}
