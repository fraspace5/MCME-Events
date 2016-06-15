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
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler.SpecialGear;
import com.mcmiddleearth.mcme.events.PVP.maps.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.PVP.Team;
import com.mcmiddleearth.mcme.events.PVP.Team.Teams;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @author Eric
 */
public class Infected extends BasePluginGamemode{
    
    private static boolean eventsRegistered = false;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "InfectedSpawn",
        "SurvivorSpawn",
    }));
    
    @Getter
    private GameState state = GameState.IDLE;
    
    Map map;
    
    private int count;
    
    @Getter
    private Objective Points;
    
    private GameEvents events;
    
    private int time;
    
    Runnable tick = new Runnable(){
        @Override
        public void run(){
            time--;
            
            if(time % 60 == 0){
                Points.setDisplayName("Time: " + (time / 60) + "m");
            }else if(time < 60){
                Points.setDisplayName("Time: " + time + "s");
            }
            
            if(time == 30){
                
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendMessage(ChatColor.GREEN + "30 seconds remaining!");
                }
                
            }
            else if(time <= 10 && time > 1){
                
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendMessage(ChatColor.GREEN + String.valueOf(time) + " seconds remaining!");
                }
                
            }
            else if(time == 1){
                
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendMessage(ChatColor.GREEN + String.valueOf(time) + " second remaining!");
                }
                
            }
            
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
                
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendMessage(ChatColor.BLUE + "Game over!");
                    p.sendMessage(ChatColor.BLUE + "Survivors win!");
                    p.sendMessage(ChatColor.BLUE + "Remaining:" + ChatColor.AQUA + remainingPlayers);
                }
                
                PlayerStat.addGameWon(Teams.SURVIVORS);
                PlayerStat.addGameLost(Teams.INFECTED);
                PlayerStat.addGameSpectatedAll();
                End(map);
            }
        }
    };
    
    @Override
    public void Start(Map m, int parameter){
        count = PVPCore.getCountdownTime();
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
                p.teleport(m.getImportantPoints().get("InfectedSpawn").toBukkitLoc());
            }
            
            else{
                Team.addToTeam(p, Team.Teams.SURVIVORS);
                p.teleport(m.getImportantPoints().get("SurvivorSpawn").toBukkitLoc());
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
                        
                        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), tick, 0, 20);
                        
                        Points = getScoreboard().registerNewObjective("Remaining", "dummy");
                        Points.setDisplayName("Time: " + time + "m");
                        time *= 60;
                        Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Team.getSurvivors().size());
                        Points.getScore(ChatColor.DARK_RED + "Infected:").setScore(Team.getInfected().size());
                        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
                        
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                        }
                        
                        for(Player p : Team.getSurvivors()){
                            p.setScoreboard(getScoreboard());
                            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
                            }
                        for(Player p : Team.getInfected()){

                            p.setScoreboard(getScoreboard());
                            GearHandler.giveGear(p, ChatColor.DARK_RED, SpecialGear.INFECTED);
                        }
                        state = GameState.RUNNING;
                        count = -1;
                        
                        for(Player p : players){
                            p.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/unstuck" + ChatColor.GRAY + " if you're stuck in a block!");
                        }
                        
                    }
                    else if(count != -1){
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game begins in " + count);
                        }
                        count--;
                    }
                }

            }, 40, 20);
    }
    
    @Override
    public void End(Map m){
        state = GameState.IDLE;

        for(Player p : Bukkit.getOnlinePlayers()){
            Team.removeFromTeam(p);
        }
        getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        m.playerLeaveAll();
        
        
        super.End(m);
    }
    
    public String requiresParameter(){
        return "time in minutes";
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
                
                if(Team.getSurvivors().contains(p)){
                    e.setDeathMessage(ChatColor.BLUE + p.getName() + ChatColor.YELLOW + " was infected by " + ChatColor.DARK_RED + p.getKiller().getName());
                    Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() - 1);
                    Points.getScore(ChatColor.DARK_RED + "Infected:").setScore(Points.getScore(ChatColor.DARK_RED + "Infected:").getScore() + 1);
                    
                    Team.addToTeam(p, Teams.INFECTED);
                    p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                        new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                    p.getInventory().clear();
                    GearHandler.giveGear(p, ChatColor.DARK_RED, SpecialGear.INFECTED);
                }
                
                if(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() <= 0){
                
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMessage(ChatColor.DARK_RED + "Game over!");
                        player.sendMessage(ChatColor.DARK_RED + "Infected Wins!");
                    
                    }
                    PlayerStat.addGameWon(Teams.INFECTED);
                    PlayerStat.addGameLost(Teams.SURVIVORS);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
            }
        }
        
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){

            if(state == GameState.RUNNING){
                e.setRespawnLocation(map.getImportantPoints().get("InfectedSpawn").toBukkitLoc().add(0, 2, 0));
                
                e.getPlayer().removePotionEffect(PotionEffectType.SPEED);
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1));
            }
        }
        
        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent e){
            
            
            if(state == GameState.RUNNING || state == GameState.COUNTDOWN){
                
                if(Team.getInfected().contains(e.getPlayer())){
                    Points.getScore(ChatColor.DARK_RED + "Infected:").setScore(Points.getScore(ChatColor.DARK_RED + "Infected:").getScore() - 1);
                }
                else if(Team.getSurvivors().contains(e.getPlayer())){
                    Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() - 1);
                    
                }
                Team.removeFromTeam(e.getPlayer());
                e.getPlayer().getInventory().clear();
                e.getPlayer().getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                   new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                
                if(Team.getSurvivors().size() <= 0){
                
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMessage(ChatColor.DARK_RED + "Game over!");
                        player.sendMessage(ChatColor.DARK_RED + "Infected Wins!");
                    
                    }
                    PlayerStat.addGameWon(Teams.INFECTED);
                    PlayerStat.addGameLost(Teams.SURVIVORS);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
                else if(Team.getInfected().size() <= 0){
                    
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
                    
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMessage(ChatColor.BLUE + "Game over!");
                        player.sendMessage(ChatColor.BLUE + "Survivors Win!");
                        player.sendMessage(ChatColor.BLUE + "Remaining:" + ChatColor.AQUA + remainingPlayers);
                    }
                    PlayerStat.addGameWon(Teams.SURVIVORS);
                    PlayerStat.addGameLost(Teams.INFECTED);
                    PlayerStat.addGameSpectatedAll();
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
            Points.getScore(ChatColor.BLUE + "Survivors:").setScore(Points.getScore(ChatColor.BLUE + "Survivors:").getScore() + 1);
            super.midgamePlayerJoin(p);
            
            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
            
            return true;
        }else{
            return false;
        }
    }
}
