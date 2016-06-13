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
import static com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode.getScoreboard;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler.SpecialGear;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.PVP.Team;
import com.mcmiddleearth.mcme.events.PVP.Team.Teams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class TeamSlayer extends BasePluginGamemode{
    
    private int target;
    
    private static boolean eventsRegistered = false;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn1",
        "RedSpawn2",
        "RedSpawn3",
        "BlueSpawn1",
        "BlueSpawn2",
        "BlueSpawn3",
    }));
    
    @Getter
    private GameState state = GameState.IDLE;
    
    Map map;
    
    private int count = 10;
    
    @Getter
    private Objective Points;
    
    private GameEvents events;
    
    @Getter
    private boolean midgameJoin = true;
    
    @Override
    public void Start(Map m, int parameter){
        count = 10;
        state = GameState.COUNTDOWN;
        int lastRedSpawn = 3;
        int lastBlueSpawn = 3;
        super.Start(m, parameter);
        this.map = m;
        target = parameter;
        
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
        for(Player p : players){
            if(Team.getRedPlayers().size() <= Team.getBluePlayers().size()){
                Team.addToTeam(p, Teams.RED);
                switch(lastRedSpawn){
                    case 1:
                        p.teleport(m.getImportantPoints().get("RedSpawn2").toBukkitLoc().add(0, 2, 0));
                        lastRedSpawn = 2;
                        break;
                    case 2:
                        p.teleport(m.getImportantPoints().get("RedSpawn3").toBukkitLoc().add(0, 2, 0));
                        lastRedSpawn = 3;
                        break;
                    case 3:
                        p.teleport(m.getImportantPoints().get("RedSpawn1").toBukkitLoc().add(0, 2, 0));
                        lastRedSpawn = 1;
                        break;
                    default:
                        p.teleport(m.getImportantPoints().get("RedSpawn1").toBukkitLoc().add(0, 2, 0));
                        lastRedSpawn = 1;
                        break;
                }
            }
            else if(Team.getBluePlayers().size() < Team.getRedPlayers().size()){
                Team.addToTeam(p, Teams.BLUE);
                switch(lastBlueSpawn){
                    case 1:
                        p.teleport(m.getImportantPoints().get("BlueSpawn2").toBukkitLoc().add(0, 2, 0));
                        lastBlueSpawn = 2;
                        break;
                    case 2:
                        p.teleport(m.getImportantPoints().get("BlueSpawn3").toBukkitLoc().add(0, 2, 0));
                        lastBlueSpawn = 3;
                        break;
                    case 3:
                        p.teleport(m.getImportantPoints().get("BlueSpawn1").toBukkitLoc().add(0, 2, 0));
                        lastBlueSpawn = 1;
                        break;
                    default:
                        p.teleport(m.getImportantPoints().get("BlueSpawn1").toBukkitLoc().add(0, 2, 0));
                        lastBlueSpawn = 1;
                        break;
                }
            }
        }
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            if(!Team.getBluePlayers().contains(player) && !Team.getRedPlayers().contains(player)){
                Team.addToTeam(player, Teams.SPECTATORS);
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

                        Points = getScoreboard().registerNewObjective("Score", "dummy");
                        Points.setDisplayName("Score");
                        Points.getScore(ChatColor.WHITE + "Goal:").setScore(target);
                        Points.getScore(ChatColor.BLUE + "Blue:").setScore(0);
                        Points.getScore(ChatColor.RED + "Red:").setScore(0);
                        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
                        
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.setScoreboard(getScoreboard());
                        }
                        
                        for(Player p : Team.getRedPlayers()){
                            GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
                        }
                        for(Player p : Team.getBluePlayers()){
                            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
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
    
    public void End(Map m){
        state = GameState.IDLE;
        
        for(Player p : Bukkit.getOnlinePlayers()){
            Team.removeFromTeam(p);
        }
        getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        m.playerLeaveAll();
        
        super.End(m);
    }
    
    @Override
    public void playerLeave(Player p){
        Team.removeFromTeam(p);
    }
    
    public boolean midgamePlayerJoin(Player p){
        
        if(Points.getScore(ChatColor.RED + "Red:").getScore() > Points.getScore(ChatColor.BLUE + "Blue:").getScore()){
            Team.addToTeam(p, Teams.BLUE);
            p.teleport(map.getImportantPoints().get("BlueSpawn1").toBukkitLoc().add(0, 2, 0));
            super.midgamePlayerJoin(p);
            
            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
        }
        else if(Points.getScore(ChatColor.BLUE + "Blue:").getScore() >= Points.getScore(ChatColor.RED + "Red:").getScore()){
            Team.addToTeam(p, Teams.RED);
            p.teleport(map.getImportantPoints().get("RedSpawn1").toBukkitLoc().add(0, 2, 0));
            super.midgamePlayerJoin(p);
            
            GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
        }
        return true;
    }
    
    public String requiresParameter(){
        return "end kill number";
    }
    
    private class GameEvents implements Listener{
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent e){
            if(state == GameState.RUNNING){
                Player p = null;
                int redScore = Points.getScore(ChatColor.RED + "Red:").getScore();
                int blueScore = Points.getScore(ChatColor.BLUE + "Blue:").getScore();
                if(e.getEntity() instanceof Player){
                    p = (Player) e.getEntity();
                }
                
                if(p != null){
                    if(Team.getRedPlayers().contains(p)){
                        Points.getScore(ChatColor.BLUE + "Blue:").setScore(blueScore + 1);
                    }
                    if(Team.getBluePlayers().contains(p)){
                        Points.getScore(ChatColor.RED + "Red:").setScore(redScore + 1);
                    }
                }
            
                if(Points.getScore(ChatColor.RED + "Red:").getScore() >= target){
                                
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMessage(ChatColor.RED + "Game over!");
                        player.sendMessage(ChatColor.RED + "Red Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.RED);
                    PlayerStat.addGameLost(Teams.BLUE);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                
                }
                else if(Points.getScore(ChatColor.BLUE + "Blue:").getScore() >= target){
                
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMessage(ChatColor.BLUE + "Game over!");
                        player.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.BLUE);
                    PlayerStat.addGameLost(Teams.RED);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                
                }
            }
        }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        if(state == GameState.RUNNING || state == GameState.COUNTDOWN){
            Team.removeFromTeam(e.getPlayer());
            
            if(Team.getRedPlayers().size() <= 0){
                
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendMessage(ChatColor.BLUE + "Game over!");
                    p.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                }
                PlayerStat.addGameWon(Teams.BLUE);
                PlayerStat.addGameLost(Teams.RED);
                PlayerStat.addGameSpectatedAll();
                End(map);
            }
            if(Team.getBluePlayers().size() <= 0){
                
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendMessage(ChatColor.RED + "Game over!");
                    p.sendMessage(ChatColor.RED + "Red Team Wins!");
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
            Random random = new Random();
            int spawn = random.nextInt(2) + 1;
            if(state == GameState.RUNNING){
                if(Team.getRedPlayers().contains(e.getPlayer())){
                    switch(spawn){
                        case 1:
                            e.setRespawnLocation(map.getImportantPoints().get("RedSpawn1").toBukkitLoc().add(0, 2, 0));
                            break;
                        case 2:
                            e.setRespawnLocation(map.getImportantPoints().get("RedSpawn2").toBukkitLoc().add(0, 2, 0));
                            break;
                        case 3:
                            e.setRespawnLocation(map.getImportantPoints().get("RedSpawn3").toBukkitLoc().add(0, 2, 0));
                            break;
                    }
                }
                if(Team.getBluePlayers().contains(e.getPlayer())){
                    switch(spawn){
                        case 1:
                            e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn1").toBukkitLoc().add(0, 2, 0));
                            break;
                        case 2:
                            e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn2").toBukkitLoc().add(0, 2, 0));
                            break;
                        case 3:
                            e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn3").toBukkitLoc().add(0, 2, 0));
                            break;
                    }
                }
            }
        }
    }
}
