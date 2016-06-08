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
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler;
import com.mcmiddleearth.mcme.events.PVP.Handlers.GearHandler.SpecialGear;
import com.mcmiddleearth.mcme.events.PVP.Map;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import com.mcmiddleearth.mcme.events.PVP.Team;
import com.mcmiddleearth.mcme.events.PVP.Team.Teams;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
public class TeamDeathmatch extends BasePluginGamemode{
    
    private static boolean eventsRegistered = false;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn",
        "BlueSpawn",
    }));
    
    @Getter
    private GameState state = GameState.IDLE;
    
    Map map;
    
    private int count = 10;
    
    @Getter
    private Objective Points;
    
    private GameEvents events;
    
    private int startingRedNum;
    private int startingBlueNum;
    
    
    @Override
    public void Start(Map m, int parameter){
        count = 10;
        state = GameState.COUNTDOWN;
        super.Start(m, parameter);
        this.map = m;
        
        if(!map.getImportantPoints().keySet().containsAll(NeededPoints)){
            for(Player p : Bukkit.getServer().getOnlinePlayers()){
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
        System.out.println("Events registered: " + eventsRegistered);
        for(Player p : players){
            if(Team.getRedPlayers().size() <= Team.getBluePlayers().size()){
                Team.addToTeam(p, Team.Teams.RED);
                p.teleport(m.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
            }
            else if(Team.getBluePlayers().size() < Team.getRedPlayers().size()){
                Team.addToTeam(p, Team.Teams.BLUE);
                p.teleport(m.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
            }
        }
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            if(!Team.getBluePlayers().contains(player) && !Team.getRedPlayers().contains(player)){
                Team.addToTeam(player, Team.Teams.SPECTATORS);
                player.teleport(m.getImportantPoints().get("SpectatorSpawn").toBukkitLoc().add(0, 2, 0));
            }
        }
        startingRedNum = Team.getRedPlayers().size();
        startingBlueNum = Team.getBluePlayers().size();
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run() {
                    if(count == 0){
                        if(state == GameState.RUNNING){
                            return;
                        }

                        Points = getScoreboard().registerNewObjective("Remaining", "dummy");
                        Points.setDisplayName("Remaining");
                        Points.getScore(ChatColor.BLUE + "Blue:").setScore(Team.getBluePlayers().size());
                        Points.getScore(ChatColor.RED + "Red:").setScore(Team.getRedPlayers().size());
                        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
                        
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.setScoreboard(getScoreboard());
                        }
                        
                        for(Player p : Team.getBluePlayers()){
                            GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
                        }
                        for(Player p : Team.getRedPlayers()){
                            GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
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

        for(Player p : Bukkit.getOnlinePlayers()){
            Team.removeFromTeam(p);
        }
        getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        m.playerLeaveAll();
        
        super.End(m);
    }
    
    public String requiresParameter(){
        return "none";
    }
    
    public boolean isMidgameJoin(){
        if(Team.getRedPlayers().size() >= (0.75 * startingRedNum) || Team.getBluePlayers().size() >= (0.75 * startingBlueNum)){
            return true;
        }else{
            return false;
        }
    }
    
    private class GameEvents implements Listener{
        
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent e){
            
            if(e.getEntity() instanceof Player && state == GameState.RUNNING){
                Player p = e.getEntity();
                
                if(Team.getRedPlayers().contains(p)){
                    Points.getScore(ChatColor.RED + "Red:").setScore(Points.getScore(ChatColor.RED + "Red:").getScore() - 1);
                }
                else if(Team.getBluePlayers().contains(p)){
                    Points.getScore(ChatColor.BLUE + "Blue:").setScore(Points.getScore(ChatColor.BLUE + "Blue:").getScore() - 1);   
                }
                
                Team.removeFromTeam(p);
                
                if(Points.getScore(ChatColor.RED + "Red:").getScore() <= 0){
                
                for(Player player : Bukkit.getServer().getOnlinePlayers()){
                    player.sendMessage(ChatColor.BLUE + "Game over!");
                    player.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                    
                }
                PlayerStat.addGameWon(Teams.BLUE);
                PlayerStat.addGameLost(Teams.RED);
                PlayerStat.addGameSpectatedAll();
                End(map);
                }
                else if(Points.getScore(ChatColor.BLUE + "Blue:").getScore() <= 0){
                    for(Player player : Bukkit.getServer().getOnlinePlayers()){
                        player.sendMessage(ChatColor.RED + "Game over!");
                        player.sendMessage(ChatColor.RED + "Red Team Wins!");
                    
                    }
                    PlayerStat.addGameWon(Teams.RED);
                    PlayerStat.addGameLost(Teams.BLUE);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                    e.getEntity().teleport(new Location(p.getWorld(), 346, 40, 513));
                }
                
                if(state == GameState.RUNNING){
                    Team.addToTeam(p, Teams.SPECTATORS);
                }
            }
        }
        
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){
            if(state == GameState.RUNNING){
                e.setRespawnLocation(map.getImportantPoints().get("SpectatorSpawn").toBukkitLoc().add(0, 2, 0));
            
                e.getPlayer().getInventory().clear();
                e.getPlayer().getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                   new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
            }
        }
        
        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent e){
            if(state == GameState.RUNNING || state == GameState.COUNTDOWN){
                
                Points.getScore(ChatColor.BLUE + "Blue:").setScore(Team.getBluePlayers().size());
                Points.getScore(ChatColor.RED + "Red:").setScore(Team.getRedPlayers().size());
                Team.removeFromTeam(e.getPlayer());
                e.getPlayer().getInventory().clear();
                e.getPlayer().getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                   new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                
                if(Team.getRedPlayers().size() <= 0){
                
                    for(Player player : Bukkit.getServer().getOnlinePlayers()){
                        player.sendMessage(ChatColor.BLUE + "Game over!");
                        player.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                    
                    }
                    PlayerStat.addGameWon(Teams.BLUE);
                    PlayerStat.addGameLost(Teams.RED);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
                else if(Team.getBluePlayers().size() <= 0){
                    for(Player player : Bukkit.getServer().getOnlinePlayers()){
                        player.sendMessage(ChatColor.RED + "Game over!");
                        player.sendMessage(ChatColor.RED + "Red Team Wins!");
                    
                    }
                    PlayerStat.addGameWon(Teams.RED);
                    PlayerStat.addGameLost(Teams.BLUE);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
            }
            else if(state == GameState.COUNTDOWN){
                Team.removeFromTeam(e.getPlayer());
                e.getPlayer().getInventory().clear();
                e.getPlayer().getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                   new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                
                if(Team.getRedPlayers().size() == 0){
                    for(Player player : Bukkit.getServer().getOnlinePlayers()){
                        player.sendMessage(ChatColor.BLUE + "Game over!");
                        player.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.BLUE);
                    PlayerStat.addGameLost(Teams.RED);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
                else if(Team.getBluePlayers().size() == 0){
                    for(Player player : Bukkit.getServer().getOnlinePlayers()){
                        player.sendMessage(ChatColor.RED + "Game over!");
                        player.sendMessage(ChatColor.RED + "Red Team Wins!");
                    }
                    PlayerStat.addGameWon(Teams.RED);
                    PlayerStat.addGameLost(Teams.BLUE);
                    PlayerStat.addGameSpectatedAll();
                    End(map);
                }
            }
        }
    }
    
    @Override
    public boolean midgamePlayerJoin(Player p){
        if(Team.getRedPlayers().size() >= (0.75 * startingRedNum) || Team.getBluePlayers().size() >= (0.75 * startingBlueNum)){
            
            if(Team.getRedPlayers().size() >= Team.getBluePlayers().size()){
                Team.addToTeam(p, Team.Teams.BLUE);
                p.teleport(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                Points.getScore(ChatColor.BLUE + "Blue:").setScore(Points.getScore(ChatColor.BLUE + "Blue:").getScore() + 1);
                super.midgamePlayerJoin(p);
                p.setScoreboard(getScoreboard());
                
                for(Player pl : Bukkit.getServer().getOnlinePlayers()){
                    
                    if(Team.getBluePlayers().contains(pl)){
                        if(pl.getName().length() < 14){
                            pl.setPlayerListName(ChatColor.BLUE + pl.getName());
                        }else{
                            String newName = pl.getName().substring(0,13);
                            pl.setPlayerListName(ChatColor.BLUE + newName);
                        }
                        pl.setDisplayName(ChatColor.BLUE + pl.getName());
                    }
                    if(Team.getRedPlayers().contains(pl)){
                        if(pl.getName().length() < 14){
                            pl.setPlayerListName(ChatColor.RED + pl.getName());
                        }else{
                            String newName = pl.getName().substring(0,13);
                            pl.setPlayerListName(ChatColor.RED + newName);
                        }
                        pl.setDisplayName(ChatColor.RED + pl.getName());
                    }
                    if(Team.getSpectators().contains(pl)){
                        if(pl.getName().length() < 14){
                            pl.setPlayerListName(ChatColor.GRAY + pl.getName());
                        }else{
                            String newName = pl.getName().substring(0,13);
                            pl.setPlayerListName(ChatColor.GRAY + newName);
                        }
                        pl.setDisplayName(ChatColor.GRAY + pl.getName());
                    }
                    
                }
                GearHandler.giveGear(p, ChatColor.BLUE, SpecialGear.NONE);
            }
            else if(Team.getBluePlayers().size() > Team.getRedPlayers().size()){
                Team.addToTeam(p, Team.Teams.RED);
                p.teleport(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                Points.getScore(ChatColor.RED + "Red:").setScore(Points.getScore(ChatColor.RED + "Red:").getScore() + 1);
                super.midgamePlayerJoin(p);
                
                GearHandler.giveGear(p, ChatColor.RED, SpecialGear.NONE);
            }
            return true;
        }
        else{
            return false;
        }
    }
}
