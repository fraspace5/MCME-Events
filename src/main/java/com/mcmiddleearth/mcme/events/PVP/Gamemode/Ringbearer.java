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
import org.bukkit.event.player.PlayerInteractEvent;
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
    private Team blueTeam = new Team(GameType.RINGBEARER); 
    
    @Getter
    private Team redTeam = new Team(GameType.RINGBEARER); 
    
    @Getter
    private Team spectatingTeam = new Team(); 
    
    @Getter
    private final int target = 100;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn",
        "BlueSpawn",
        "SpectatorSpawn"
    }));
    
    Map map;
    
    int count = 10;
    
    @Getter
    private GameState state = GameState.IDLE;
    
    boolean hasTeams = false;
    
    @Getter
    private boolean midgameJoin = false;
    
    Runnable exp = new Runnable(){

            @Override
            public void run() {
                if(state == GameState.RUNNING){
                    if(redTeam.getBearer().getExp() < 7f){
                        redTeam.getBearer().setExp(redTeam.getBearer().getExp() + 0.004f);
                    }
                    if(blueTeam.getBearer().getExp() < 7f){
                        blueTeam.getBearer().setExp(blueTeam.getBearer().getExp() + 0.004f);
                    }
                }
            }
            
        };
    
    public Ringbearer(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), exp, 20, 20);
    }
    
    @Override
    public void Start(Map m,int parameter) {
        super.Start(m,parameter);
        count = 10;
        state = GameState.COUNTDOWN;
        this.map = m;
        if(!m.getImportantPoints().keySet().containsAll(NeededPoints)){
            for(Player p : players){
                p.sendMessage(ChatColor.YELLOW + "Game Cannot Start! Map maker f**ked up!");
            }
            End(m);
            return;
        }
        blueTeam = new Team(); 
        redTeam = new Team(); 
        PluginManager pm = Main.getServerInstance().getPluginManager();
        pm.registerEvents(new GameEvents(), Main.getPlugin());
        for(Player p : players){
            if(Team.getBluePlayers().size() < 16 && Team.getRedPlayers().size() < 16){
                if(Team.getBluePlayers().size() >= Team.getRedPlayers().size()){
                    Team.addToTeam(p,Teams.RED);
                    p.teleport(m.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                }else if(Team.getBluePlayers().size() < Team.getRedPlayers().size()){
                    Team.addToTeam(p,Teams.BLUE);
                    p.teleport(m.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                }
            }else{
                Team.addToTeam(p,Teams.SPECTATORS);
                p.teleport(m.getImportantPoints().get("SpectatorSpawn").toBukkitLoc().add(0, 2, 0));
            }
        }
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable(){
                @Override
                public void run() {
                    if(count == 0){
                        if(state == GameState.RUNNING){
                            return;
                        }
                        ScoreboardManager sbm = Bukkit.getScoreboardManager();
                        
                        for(Player p : Bukkit.getServer().getOnlinePlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                        }
                        
                        for(Player p : Team.getRedPlayers()){
                            
                            p.teleport(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
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
                            p.setScoreboard(redTeam.getBoard());
                        }
                        for(Player p : Team.getBluePlayers()){

                            p.teleport(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
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
                            p.setScoreboard(blueTeam.getBoard());
                        }
                        Random r = new Random();
                        ItemStack ring = new ItemStack(Material.GOLD_NUGGET);
                        ItemMeta im = ring.getItemMeta();
                        im.setDisplayName("The Ring");
                        im.setLore(Arrays.asList(new String[] {"The one ring of power...", "1 of 2"}));
                        ring.setItemMeta(im);
                        if(Team.getRedPlayers().size() == 0 || Team.getBluePlayers().size() == 0){
                            for(Player p : players){
                                p.sendMessage(ChatColor.YELLOW + "There must be at least one player on each team!");
                            }
                            End(map);
                        }
                        if(Team.getRedPlayers().size() == 1){
                            redTeam.setBearer(Team.getRedPlayers().get(0));
                        }else{
                            redTeam.setBearer(Team.getRedPlayers().get(r.nextInt(Team.getRedPlayers().size()-1)));
                        }
                        if(Team.getBluePlayers().size() == 1){
                            blueTeam.setBearer(Team.getBluePlayers().get(0));
                        }else{
                            blueTeam.setBearer(Team.getBluePlayers().get(r.nextInt(Team.getBluePlayers().size()-1)));
                        }
                        redTeam.getBearer().setMaxHealth(60);
                        redTeam.getBearer().setHealth(60);
                        redTeam.getBearer().getInventory().addItem(ring);
                        blueTeam.getBearer().setMaxHealth(60);
                        blueTeam.getBearer().setHealth(60);
                        blueTeam.getBearer().getInventory().addItem(ring);
                        redTeam.getBoard().getObjective(DisplaySlot.SIDEBAR).getScore(redTeam.getBearer().getName()).setScore(0);
                        blueTeam.getBoard().getObjective(DisplaySlot.SIDEBAR).getScore(blueTeam.getBearer().getName()).setScore(0);
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
    public void playerLeave(Player p){
        if(blueTeam.getBearer().equals(p) || redTeam.getBearer().equals(p)){
            End(map);
        }else{
            Team.removeFromTeam(p);
            Team.removeFromTeam(p);
            players.remove(p);
        }
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
        super.End(m);
    }
    
    public boolean midgamePlayerJoin(Player p){
        return false;
    }
    
    public String requiresParameter(){
        return null;
    }
    private class GameEvents implements Listener{
        
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(state == GameState.RUNNING && players.contains(e.getPlayer()) && (
                    e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
                System.out.println("Enter");
                final Player p = e.getPlayer();
                if(p.getItemInHand() != null){
                    ItemStack ring = p.getItemInHand();
                    if(ring.getItemMeta().getDisplayName().equalsIgnoreCase("The Ring")){
                        System.out.println(p.getExp());
                        if(p.getExp() >= 1.00f){
                            p.setExp(0);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1, true, true));
                            p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), 
                                new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                                
                                @Override
                                public void run() {
                                    ItemStack[] items = new ItemStack[] {new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE), 
                                        new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS)};
                                    for(int i = 0; i <= 3; i++){
                                        LeatherArmorMeta lam = (LeatherArmorMeta) items[i].getItemMeta();
                                        if(Team.getRedPlayers().contains(p)){
                                            lam.setColor(org.bukkit.Color.fromRGB(153, 51, 51));
                                        }else{
                                            lam.setColor(org.bukkit.Color.fromRGB(51, 76, 178));
                                        }
                                        items[i].setItemMeta(lam);
                                        items[i].getItemMeta().spigot().setUnbreakable(true);

                                    }
                                    p.getInventory().setHelmet(items[0]);
                                    p.getInventory().setChestplate(items[1]);
                                    p.getInventory().setLeggings(items[2]);
                                    p.getInventory().setBoots(items[3]);
                                }
                            }, 600);
                        }
                    }
                }
            }
        }
        
        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent e){
            System.out.println("rb");
            if(state == GameState.RUNNING && players.contains(e.getPlayer())){
                if(Team.getRedPlayers().contains(e.getPlayer())){
                    if(redTeam.isCanRespawn()){
                        if(redTeam.getBearer().equals(e.getPlayer())){
                            redTeam.setCanRespawn(false);
                            e.getPlayer().setMaxHealth(20);
                        }
                        e.setRespawnLocation(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                    }else{
                        e.getPlayer().setGameMode(GameMode.SPECTATOR);
                        e.setRespawnLocation(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                        redTeam.getAlive().remove(e.getPlayer());
                        if(redTeam.getAlive().size() == 0){
                            for(Player p : players){
                                p.sendMessage(ChatColor.BLUE + "Game over!");
                                p.sendMessage(ChatColor.BLUE + "Blue Team Wins!");
                            }
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){

                                @Override
                                public void run() {
                                    End(map);
                                }
                            
                            }, 40);
                        }
                    }
                }else if(Team.getBluePlayers().contains(e.getPlayer())){
                    if(blueTeam.isCanRespawn()){
                        if(blueTeam.getBearer().equals(e.getPlayer())){
                            blueTeam.setCanRespawn(false);
                            e.getPlayer().setMaxHealth(20);
                        }
                        e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                    }else{
                        e.getPlayer().setGameMode(GameMode.SPECTATOR);
                        e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                        blueTeam.getAlive().remove(e.getPlayer());
                        if(blueTeam.getAlive().size() == 0){
                            for(Player p : players){
                                p.sendMessage(ChatColor.RED + "Game over!");
                                p.sendMessage(ChatColor.RED + "Red Team Wins!");
                            }
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                                
                                @Override
                                public void run() {
                                    End(map);
                                }
                            
                            }, 40);
                        }
                    }
                }
            }
        }
    }
}
