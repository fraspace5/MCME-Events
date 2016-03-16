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
 * @author Donovan <dallen@dallen.xyz>
 */
public class TeamSlayer extends BasePluginGamemode{
    
    @Getter
    private Team redTeam = new Team();
    
    @Getter
    private Team blueTeam = new Team();
    
    @Getter
    private Team spectatingTeam = new Team();
    
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
        "SpectatorSpawn",
    }));
    
    @Getter
    private boolean Running = false;
    
    Map map;
    
    private int count = 10;
    private Scoreboard Score;
    
    @Getter
    private Objective Points;
    
    private GameEvents events;
    
    public void Start(Map m, int parameter){
        count = 10;
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
            p.sendMessage(ChatColor.GREEN + "selecting teams");
            if(Team.getRedPlayers().size() <= Team.getBluePlayers().size()){
                Team.addToTeam(p, Teams.RED);
                p.setWalkSpeed(0);
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
                p.setWalkSpeed(0);
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
                player.teleport(m.getImportantPoints().get("SpectatorSpawn").toBukkitLoc().add(0, 2, 0));
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
                                for(Player p : Team.getRedPlayers()){
                                    rd.addPlayer(p);
                                }
                                for(Player p : Team.getBluePlayers()){
                                    blu.addPlayer(p);
                                }
                            }
                        }
                        Points = Score.registerNewObjective("Score", "dummy");
                        Points.setDisplayName("Score");
                        Points.getScore(ChatColor.WHITE + "Goal:").setScore(target);
                        Points.getScore(ChatColor.BLUE + "Blue:").setScore(0);
                        Points.getScore(ChatColor.RED + "Red:").setScore(0);
                        Points.setDisplaySlot(DisplaySlot.SIDEBAR);
                        for(Player p : Team.getRedPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.setWalkSpeed(0.2F);
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
                        for(Player p : Team.getBluePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.setWalkSpeed(0.2F);
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
    
    public void End(Map m){
        Running = false;
        super.End(m);
        
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
        Score.clearSlot(DisplaySlot.SIDEBAR);
        m.playerLeaveAll();
        blueTeam = new Team();
        redTeam = new Team();
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
    
    @Override
    public void playerLeave(Player p){
        
    }
    
    public boolean midgamePlayerJoin(Player p){
        
        if(Points.getScore(ChatColor.RED + "Red:").getScore() > Points.getScore(ChatColor.BLUE + "Blue:").getScore()){
            Team.addToTeam(p, Teams.BLUE);
            p.teleport(map.getImportantPoints().get("BlueSpawn1").toBukkitLoc().add(0, 2, 0));
        }
        else if(Points.getScore(ChatColor.RED + "Blue:").getScore() > Points.getScore(ChatColor.BLUE + "Red:").getScore()){
            Team.addToTeam(p, Teams.RED);
            p.teleport(map.getImportantPoints().get("RedSpawn1").toBukkitLoc().add(0, 2, 0));
        }
        return true;
    }
    
    public class GameEvents implements Listener{
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent e){
            if(Running){
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
                                
                for(Player player : players){
                    player.sendMessage(ChatColor.RED + "Game over!");
                    player.sendMessage(ChatColor.RED + "Red Team Wins!");
                }
                End(map);
                
            }
            else if(Points.getScore(ChatColor.BLUE + "Blue:").getScore() >= target){
                
                for(Player player : players){
                    player.sendMessage(ChatColor.BLUE + "Game over!");
                    player.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                }
                End(map);
                
            }
        }
    }
    
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){
            Random random = new Random();
            int spawn = random.nextInt(2) + 1;
        
            if(Running){
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
