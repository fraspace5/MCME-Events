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
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler.SpecialGear;
import com.mcmiddleearth.mcme.events.PVP.maps.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.PVP.Team;
import com.mcmiddleearth.mcme.events.PVP.Team.GameType;
import com.mcmiddleearth.mcme.events.PVP.Team.Teams;
import com.mcmiddleearth.mcme.events.Util.EventLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
 * @author Donovan <dallen@dallen.xyz>
 */
public class Ringbearer extends BasePluginGamemode{//Handled by plugin 
    
    @Getter
    private final int target = 100;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn",
        "BlueSpawn",
    }));
    
    Map map;
    
    private int count;
    
    @Getter
    private GameState state;
    
    boolean hasTeams = false;
    
    @Getter
    private Player redBearer = null;
    
    private boolean redCanRespawn;
    private boolean redBearerHasRespawned;
    
    @Getter
    private Player blueBearer = null;
    
    private boolean blueCanRespawn;
    private boolean blueBearerHasRespawned;
    
    private GameEvents events;
    
    private boolean eventsRegistered = false;
    
    private Objective Points;
    
    public Ringbearer(){
        state = GameState.IDLE;
    }
    
    Runnable exp = new Runnable(){

            @Override
            public void run() {
                if(state == GameState.RUNNING){
                    if(redBearer != null){
                        
                        if(!redBearer.hasPotionEffect(PotionEffectType.INVISIBILITY) && redBearer.getExp() < 1f){
                            redBearer.setExp(redBearer.getExp() + .006f);
                        }
                        
                        if(redBearer.getInventory().getHelmet().getType() != Material.GLOWSTONE){
                            redBearer.getInventory().setHelmet(new ItemStack(Material.GLOWSTONE));
                        }
                        
                    }
                    if(blueBearer != null){
                        
                        if(!blueBearer.hasPotionEffect(PotionEffectType.INVISIBILITY) && blueBearer.getExp() < 1f){
                            blueBearer.setExp(blueBearer.getExp() + .006f);
                        }
                        
                        if(blueBearer.getInventory().getHelmet().getType() != Material.GLOWSTONE){
                            blueBearer.getInventory().setHelmet(new ItemStack(Material.GLOWSTONE));
                        }
                        
                    }
                }
            }
            
        };
    
    @Override
    public void Start(Map m,int parameter) {
        super.Start(m,parameter);
        count = PVPCore.getCountdownTime();
        state = GameState.COUNTDOWN;
        this.map = m;
        if(!m.getImportantPoints().keySet().containsAll(NeededPoints)){
            for(Player p : players){
                p.sendMessage(ChatColor.RED + "Game Cannot Start! Not all needed points have been added!");
            }
            End(m);
            return;
        }
        
        if(!eventsRegistered){
            events = new GameEvents();
            PluginManager pm = Main.getServerInstance().getPluginManager();
            pm.registerEvents(events, Main.getPlugin());
            eventsRegistered = true;
        }
        
        blueCanRespawn = true;
        blueBearerHasRespawned = false;
        redCanRespawn = true;
        redBearerHasRespawned = false;
        
        Points = getScoreboard().registerNewObjective("Remaining", "dummy");
        Points.setDisplayName("Remaining");
        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        for(Player p : Bukkit.getOnlinePlayers()){
            p.setScoreboard(getScoreboard());
            if(players.contains(p)){
                if(Team.getBluePlayers().size() >= Team.getRedPlayers().size()){
                    Team.addToTeam(p,Teams.RED);
                    p.teleport(m.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                }else if(Team.getBluePlayers().size() < Team.getRedPlayers().size()){
                    Team.addToTeam(p,Teams.BLUE);
                    p.teleport(m.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                }
            }else{
                Team.addToTeam(p,Teams.SPECTATORS);
                p.teleport(m.getSpawn().toBukkitLoc().add(0, 2, 0));
            }
        }
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run() {
                    if(count == 0){
                        if(state == GameState.RUNNING){
                            return;
                        }
                        
                        
                        
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            
                        }
                        
                        Random r = new Random();
                        
                        if(Team.getRedPlayers().size() == 1){
                            setRingbearer(Teams.RED, Team.getRedPlayers().get(0));
                        }else if(Team.getRedPlayers().size() > 0){
                            setRingbearer(Teams.RED, Team.getRedPlayers().get(r.nextInt(Team.getRedPlayers().size())));
                        }
                        if(Team.getBluePlayers().size() == 1){
                            setRingbearer(Teams.BLUE, Team.getBluePlayers().get(0));
                        }else if(Team.getBluePlayers().size() > 0){
                            setRingbearer(Teams.BLUE, Team.getBluePlayers().get(r.nextInt(Team.getBluePlayers().size())));
                        }
                        
                        for(Player p : Team.getRedPlayers()){
                            
                            if(!p.equals(redBearer)){
                                GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
                            }
                            
                        }
                        
                        for(Player p : Team.getBluePlayers()){

                            if(!p.equals(blueBearer)){
                                GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
                            }
                            
                        }
                        
                        Points.getScore(ChatColor.BLUE + "Blue:").setScore(Team.getBluePlayers().size());
                        Points.getScore(ChatColor.RED + "Red:").setScore(Team.getRedPlayers().size());
                        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), exp, 0, 20);
                        
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
    
    private void setRingbearer(Teams t, Player p){
        
        if(t == Teams.BLUE){
            blueBearer = p;
            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.RINGBEARER);
            
            blueBearer.sendMessage(ChatColor.BLUE + "You are Blue Team's Bearer!");
            blueBearer.sendMessage(ChatColor.BLUE + "Stay alive as long as you can!");
            
            for(Player pl : Team.getBluePlayers()){
                
                if(!pl.equals(blueBearer)){
                    pl.sendMessage(ChatColor.BLUE + blueBearer.getName() + " is your team's Ringbearer!");
                }
                
            }
            
        }
        else if(t == Teams.RED){
            redBearer = p;
            GearHandler.giveGear(p, ChatColor.RED, SpecialGear.RINGBEARER);
            
            redBearer.sendMessage(ChatColor.RED + "You are Red Team's Bearer!");
            redBearer.sendMessage(ChatColor.RED + "Stay alive as long as you can!");
            
            for(Player pl : Team.getRedPlayers()){
                
                if(!pl.equals(redBearer)){
                    pl.sendMessage(ChatColor.RED + p.getName() + " is your team's Ringbearer!");
                }
                
            }
        }
    }
    
    @Override
    public void End(Map m){
        state = GameState.IDLE;

        redBearer = null;
        blueBearer = null;
        for(Player p : Bukkit.getOnlinePlayers()){
            Team.removeFromTeam(p);
        }
        
        m.playerLeaveAll();
        
        super.End(m);
    }
        
    public boolean isMidgameJoin(){
        
        if(redCanRespawn || blueCanRespawn){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean midgamePlayerJoin(Player p){
        if(redCanRespawn && !blueCanRespawn){
            Team.addToTeam(p, Teams.RED);
            p.teleport(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
            Points.getScore(ChatColor.RED + "Red:").setScore(Points.getScore(ChatColor.RED + "Red:").getScore() + 1);
            
            GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
        }
        else if(blueCanRespawn && !redCanRespawn){
            Team.addToTeam(p, Teams.BLUE);
            p.teleport(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
            Points.getScore(ChatColor.BLUE + "Blue:").setScore(Points.getScore(ChatColor.BLUE + "Blue:").getScore() + 1);
            
            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
        }
        else if(redCanRespawn && blueCanRespawn){
            
            if(Team.getRedPlayers().size() >= Team.getBluePlayers().size()){
                Team.addToTeam(p, Teams.BLUE);
                p.teleport(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
                Points.getScore(ChatColor.BLUE + "Blue:").setScore(Points.getScore(ChatColor.BLUE + "Blue:").getScore() + 1);
            }
            else if(Team.getRedPlayers().size() < Team.getBluePlayers().size()){
                Team.addToTeam(p, Teams.RED);
                p.teleport(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                Points.getScore(ChatColor.RED + "Red:").setScore(Points.getScore(ChatColor.RED + "Red:").getScore() + 1);
            
                GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
            }
            
        }
        super.midgamePlayerJoin(p);
        return true;
    }
    
    public String requiresParameter(){
        return "none";
    }
    private class GameEvents implements Listener{
        
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent e){
            
            if(state == GameState.RUNNING && e.getEntity() instanceof Player){
                Player p = (Player) e.getEntity();
                
                if(Team.getRedPlayers().contains(p)){
                    
                    if(redBearer.equals(p) && redCanRespawn){
                        redCanRespawn = false;
                        redBearer.getInventory().clear();
                        GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
                        
                        for(Player pl : Bukkit.getOnlinePlayers()){
                            pl.sendMessage(ChatColor.RED + "Red Team's Ringbearer has been killed!");
                            pl.sendMessage(ChatColor.RED + "They can't respawn!");
                        }
                    }
                    else if(p.equals(redBearer) && redBearerHasRespawned){
                        Team.addToTeam(p, Teams.SPECTATORS);
                    }
                    
                    else if(!redCanRespawn){
                        Team.addToTeam(p, Teams.SPECTATORS);
                    }
                    
                }
                else if(Team.getBluePlayers().contains(p)){
                    
                    if(blueBearer.equals(p) && blueCanRespawn){
                        blueCanRespawn = false;
                        blueBearer.getInventory().clear();
                        GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
                        
                        for(Player pl : Bukkit.getOnlinePlayers()){
                            pl.sendMessage(ChatColor.BLUE + "Blue Team's Ringbearer has been killed!");
                            pl.sendMessage(ChatColor.BLUE + "They can't respawn!");
                        }
                    }
                    
                    else if(p.equals(blueBearer) && blueBearerHasRespawned){
                        Team.addToTeam(p, Teams.SPECTATORS);
                    }
                    
                    else if(!blueCanRespawn){
                        Team.addToTeam(p, Teams.SPECTATORS);
                    }
                    
                }
                
                Points.getScore(ChatColor.BLUE + "Blue:").setScore(Team.getBluePlayers().size());
                Points.getScore(ChatColor.RED + "Red:").setScore(Team.getRedPlayers().size());
                
                if(Team.getRedPlayers().size() <= 0){
                    
                    for(Player pl : Bukkit.getOnlinePlayers()){
                        pl.sendMessage(ChatColor.BLUE + "Game over!");
                        pl.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.BLUE);
                    PlayerStat.addGameLost(Teams.RED);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
                else if(Team.getBluePlayers().size() <= 0){
                    
                    for(Player pl : Bukkit.getOnlinePlayers()){
                        pl.sendMessage(ChatColor.RED + "Game over!");
                        pl.sendMessage(ChatColor.RED + "Red Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.RED);
                    PlayerStat.addGameLost(Teams.BLUE);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
               
            }
            
        }
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){
            
            if(state == GameState.RUNNING && players.contains(e.getPlayer())){
                if(Team.getRedPlayers().contains(e.getPlayer())){
                    e.setRespawnLocation(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                    
                    if(e.getPlayer().equals(redBearer) && !redBearerHasRespawned){
                        redBearerHasRespawned = true;
                    }
                    
                }
                else if(Team.getBluePlayers().contains(e.getPlayer())){
                    e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                    
                    if(e.getPlayer().equals(blueBearer) && !blueBearerHasRespawned){
                        blueBearerHasRespawned = true;
                    }
                }
                else{
                    e.setRespawnLocation(map.getSpawn().toBukkitLoc().add(0, 2, 0));
                }
            }
        }
        
        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent e){

            if(state == GameState.RUNNING || state == GameState.COUNTDOWN){
                Random rand = new Random();
                Team.removeFromTeam(e.getPlayer());
                
                if(e.getPlayer().equals(redBearer) && redCanRespawn){
                    int bearerNum = rand.nextInt(Team.getRedPlayers().size());
                    int loop = 0;
                    
                    for(Player p : Team.getRedPlayers()){
                        if(loop == bearerNum){
                            if(!p.equals(e.getPlayer())){
                                p.getInventory().clear();
                                setRingbearer(Teams.RED, p);
                                break;
                            }else{
                                bearerNum++;
                            }
                            
                        }
                        loop++;
                    }
                    
                }
                else if(e.getPlayer().equals(blueBearer) && blueCanRespawn){
                    int bearerNum = rand.nextInt(Team.getRedPlayers().size());
                    int loop = 0;
                    
                    for(Player p : Team.getBluePlayers()){
                        if(loop == bearerNum){
                            if(!p.equals(e.getPlayer())){
                                p.getInventory().clear();
                                setRingbearer(Teams.BLUE, p);
                                break;
                            }else{
                                bearerNum++;
                            }
                        }
                        loop++;
                    }
                }
                
                Points.getScore(ChatColor.BLUE + "Blue:").setScore(Team.getBluePlayers().size());
                Points.getScore(ChatColor.RED + "Red:").setScore(Team.getRedPlayers().size());
                
                if(Team.getBluePlayers().size() <= 0){
                    
                    for(Player pl : Bukkit.getOnlinePlayers()){
                        pl.sendMessage(ChatColor.RED + "Game over!");
                        pl.sendMessage(ChatColor.RED + "Red Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.RED);
                    PlayerStat.addGameLost(Teams.BLUE);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                    
                }
                else if(Team.getRedPlayers().size() <= 0){
                    
                    for(Player pl : Bukkit.getOnlinePlayers()){
                        pl.sendMessage(ChatColor.BLUE + "Game over!");
                        pl.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.BLUE);
                    PlayerStat.addGameLost(Teams.RED);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                    
                }
            }
        }
    }
}
