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
    private Team BlueTeam = new Team("Blue", GameMode.ADVENTURE); 
    
    @Getter
    private Team RedTeam = new Team("Red", GameMode.ADVENTURE); 
    
    @Getter
    private Team SpectatingTeam = new Team("Spectator", GameMode.SPECTATOR); 
    
    @Getter
    private int target = 100;
    
    @Getter
    private final ArrayList<String> NeededPoints = new ArrayList<String>(Arrays.asList(new String[] {
        "RedSpawn",
        "BlueSpawn",
        "SpectatorSpawn"
    }));
    
    Map map;
    
    int count;
    
    @Getter
    private boolean Running = false;
    
    boolean hasTeams = false;
    
    Runnable exp = new Runnable(){

            @Override
            public void run() {
                if(Running){
                    if(RedTeam.getBearer().getExp() < 7f){
                        RedTeam.getBearer().setExp(RedTeam.getBearer().getExp() + 0.004f);
                    }
                    if(BlueTeam.getBearer().getExp() < 7f){
                        BlueTeam.getBearer().setExp(BlueTeam.getBearer().getExp() + 0.004f);
                    }
                }
            }
            
        };
    
    public Ringbearer(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), exp, 20, 20);
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
        BlueTeam = new Team("Blue", GameMode.ADVENTURE); 
        RedTeam = new Team("Red", GameMode.ADVENTURE); 
        PluginManager pm = Main.getServerInstance().getPluginManager();
        pm.registerEvents(new GameEvents(), Main.getPlugin());
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
                        
                        for(Player p : RedTeam.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.teleport(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                            p.setGameMode(RedTeam.getGamemode());
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
                            p.setScoreboard(RedTeam.getBoard());
                        }
                        for(Player p : BlueTeam.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "Game Start!");
                            p.teleport(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                            p.setGameMode(BlueTeam.getGamemode());
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
                            p.setScoreboard(BlueTeam.getBoard());
                        }
                        Random r = new Random();
                        ItemStack ring = new ItemStack(Material.GOLD_NUGGET);
                        ItemMeta im = ring.getItemMeta();
                        im.setDisplayName("The Ring");
                        im.setLore(Arrays.asList(new String[] {"The one ring of power...", "1 of 2"}));
                        ring.setItemMeta(im);
                        if(RedTeam.getPlayers().size() == 1){
                            RedTeam.setBearer(RedTeam.getPlayers().get(0));
                        }else{
                            RedTeam.setBearer(RedTeam.getPlayers().get(r.nextInt(RedTeam.getPlayers().size()-1)));
                        }
                        if(BlueTeam.getPlayers().size() == 1){
                            BlueTeam.setBearer(BlueTeam.getPlayers().get(0));
                        }else{
                            BlueTeam.setBearer(BlueTeam.getPlayers().get(r.nextInt(BlueTeam.getPlayers().size()-1)));
                        }
                        RedTeam.getBearer().setMaxHealth(60);
                        RedTeam.getBearer().setHealth(60);
                        RedTeam.getBearer().getInventory().addItem(ring);
                        BlueTeam.getBearer().setMaxHealth(60);
                        BlueTeam.getBearer().setHealth(60);
                        BlueTeam.getBearer().getInventory().addItem(ring);
                        RedTeam.getObj().getScore(RedTeam.getBearer().getName()).setScore(0);
                        BlueTeam.getObj().getScore(BlueTeam.getBearer().getName()).setScore(0);
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
    public void playerLeave(Player p){
        if(BlueTeam.getBearer().equals(p) || RedTeam.getBearer().equals(p)){
            End(map);
        }else{
            BlueTeam.removeFromTeam(p);
            RedTeam.removeFromTeam(p);
            players.remove(p);
        }
    }
    
    @Override
    public void End(Map m){
        Running = false;
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
        private Scoreboard board;
        
        @Getter
        private Objective obj;
        
        @Getter @Setter
        private Player Bearer;
        
        @Getter @Setter
        private boolean CanRepawn;
        
        @Getter
        private ArrayList<Player> Alive = new ArrayList<>();
        
        @Getter
        private ArrayList<Player> players = new ArrayList<>();
        
        @Getter
        private GameMode gamemode;
        
        public Team(String name, GameMode gamemode){
            this.name = name;
            this.gamemode = gamemode;
            this.CanRepawn = true;
            this.board = Bukkit.getScoreboardManager().getNewScoreboard();
            this.obj = board.registerNewObjective("Bearer", "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName("Bearer");
        }
        
        public void addToTeam(Player p){
            players.add(p);
            Alive.add(p);
        }
        
        public void removeFromTeam(Player p){
            players.remove(p);
            Alive.remove(p);
        }
    }
    
    private class GameEvents implements Listener{
        
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(Running && players.contains(e.getPlayer()) && (
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
                                        if(RedTeam.getPlayers().contains(p)){
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
            if(Running && players.contains(e.getPlayer())){
                if(RedTeam.getPlayers().contains(e.getPlayer())){
                    if(RedTeam.isCanRepawn()){
                        if(RedTeam.getBearer().equals(e.getPlayer())){
                            RedTeam.setCanRepawn(false);
                            e.getPlayer().setMaxHealth(20);
                        }
                        e.setRespawnLocation(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                    }else{
                        e.getPlayer().setGameMode(GameMode.SPECTATOR);
                        e.setRespawnLocation(map.getImportantPoints().get("RedSpawn").toBukkitLoc().add(0, 2, 0));
                        RedTeam.getAlive().remove(e.getPlayer());
                        if(RedTeam.getAlive().size() == 0){
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
                }else if(BlueTeam.getPlayers().contains(e.getPlayer())){
                    if(BlueTeam.isCanRepawn()){
                        if(BlueTeam.getBearer().equals(e.getPlayer())){
                            BlueTeam.setCanRepawn(false);
                            e.getPlayer().setMaxHealth(20);
                        }
                        e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                    }else{
                        e.getPlayer().setGameMode(GameMode.SPECTATOR);
                        e.setRespawnLocation(map.getImportantPoints().get("BlueSpawn").toBukkitLoc().add(0, 2, 0));
                        BlueTeam.getAlive().remove(e.getPlayer());
                        if(BlueTeam.getAlive().size() == 0){
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
