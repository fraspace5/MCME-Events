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
package com.mcmiddleearth.mcme.events.PVP.Handlers;

import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.BasePluginGamemode.GameState;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Ringbearer;
import com.mcmiddleearth.mcme.events.PVP.PVPCommandCore;
import com.mcmiddleearth.mcme.events.PVP.Team;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Eric
 */
public class GearHandler {
    
    public enum SpecialGear{
        ONEINTHEQUIVER, INFECTED, RINGBEARER, NONE
    }
    
    public GearHandler(){
        Bukkit.getPluginManager().registerEvents(new GearEvents(), Main.getPlugin());
    }
    
    public static void giveGear(Player p, ChatColor c, SpecialGear sg){
        ItemStack[] items;
        
        if(sg == SpecialGear.ONEINTHEQUIVER){
            items = new ItemStack[] {new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE), 
                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.IRON_AXE), new ItemStack(Material.BOW)};
        }
        else{
            items = new ItemStack[] {new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE), 
                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.IRON_SWORD), new ItemStack(Material.BOW)};
        }
        
        for(int i = 0; i <= 5; i++){
            if(i<=3){
                LeatherArmorMeta lam = (LeatherArmorMeta) items[i].getItemMeta();
                switch(c){
                    
                    case AQUA:
                        lam.setColor(DyeColor.LIGHT_BLUE.getColor());
                        break;
                    case BLUE:
                        lam.setColor(DyeColor.BLUE.getColor());
                        break;
                    case DARK_AQUA:
                        lam.setColor(DyeColor.CYAN.getColor());
                        break;
                    case DARK_BLUE:
                        lam.setColor(DyeColor.BROWN.getColor());
                        break;
                    case DARK_GRAY:
                        lam.setColor(DyeColor.BLACK.getColor());
                        break;
                    case DARK_GREEN:
                        lam.setColor(DyeColor.GREEN.getColor());
                        break;
                    case DARK_PURPLE:
                        lam.setColor(DyeColor.PURPLE.getColor());
                        break;
                    case DARK_RED:
                        lam.setColor(DyeColor.ORANGE.getColor());
                        break;
                    case GOLD:
                        lam.setColor(DyeColor.SILVER.getColor());
                        break;
                    case GRAY:
                        lam.setColor(DyeColor.GRAY.getColor());
                        break;
                    case GREEN:
                        lam.setColor(DyeColor.LIME.getColor());
                        break;
                    case LIGHT_PURPLE:
                        lam.setColor(DyeColor.MAGENTA.getColor());
                        break;
                    case RED:
                        lam.setColor(DyeColor.RED.getColor());
                        break;
                    case YELLOW:
                        lam.setColor(DyeColor.YELLOW.getColor());
                        break;
                    
                }
                
                items[i].setItemMeta(lam);
            }else{
                items[i].addUnsafeEnchantment(new EnchantmentWrapper(34), 10);
            }
                items[i].getItemMeta().spigot().setUnbreakable(true);
        }
        p.getInventory().clear();
        
        if(sg == SpecialGear.RINGBEARER){
            p.getInventory().setHelmet(new ItemStack(Material.GLOWSTONE, 1));
            
        }
        
        else if(sg != SpecialGear.INFECTED){
            p.getInventory().setHelmet(items[0]);
        }
        
        
        if(sg == SpecialGear.INFECTED){
            p.getInventory().setChestplate(items[1]);
        }else{
            p.getInventory().setChestplate(items[1]);
            p.getInventory().setLeggings(items[2]);
            p.getInventory().setBoots(items[3]);
            
        }
        
        if(sg == SpecialGear.ONEINTHEQUIVER){
            items[5].addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 12);
        }
        
        p.getInventory().addItem(items[4]);
        p.getInventory().addItem(items[5]);
        
        if(sg == SpecialGear.ONEINTHEQUIVER){
            ItemStack Arrows = new ItemStack(Material.ARROW, 1);
            p.getInventory().addItem(Arrows);
        }
        else{
            ItemStack Arrows = new ItemStack(Material.ARROW, 64);
            p.getInventory().addItem(Arrows);
            p.getInventory().addItem(Arrows);
        }
        
        if(sg == SpecialGear.RINGBEARER){
            giveCustomItem(p, CustomItem.RING);
        }
        
    }
    
    public enum CustomItem{
        RING, PIPE
    }
    
    public static void giveCustomItem(Player p, CustomItem i){
        
        switch(i){
            
            case RING:
                ItemStack ring = new ItemStack(Material.GOLD_NUGGET);
                ItemMeta im = ring.getItemMeta();
                im.setDisplayName("The Ring");
                im.setLore(Arrays.asList(new String[] {"The One Ring of power...", "1 of 2"}));
                ring.setItemMeta(im);
        
                p.getInventory().addItem(ring);
                break;
            case PIPE:
                p.getInventory().addItem(new ItemStack(Material.GHAST_TEAR, 1));
        }
        
    }
    
    public static class GearEvents implements Listener{
        
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent e){
            if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                
                final Player p = e.getPlayer();
                ItemStack item = null;
                
                if(p.getItemInHand() != null){
                    item = p.getItemInHand();
                }else{
                    return;
                }
                
                if(PVPCommandCore.getRunningGame() != null){
                    if(item.getItemMeta() == null){
                        return;
                    }
                    if(item.getItemMeta().getDisplayName() == null){
                        return;
                    }
                    
                    if(item.getItemMeta().getDisplayName().equalsIgnoreCase("The Ring") && 
                            PVPCommandCore.getRunningGame().getGm().getPlayers().contains(e.getPlayer()) &&
                            PVPCommandCore.getRunningGame().getGm() instanceof Ringbearer){
                        
                        if(p.getExp() >= 1.00f){
                            p.setExp(0);
                            p.sendMessage(ChatColor.YELLOW + "You are now invisible!");
                            
                            if(Team.getRedPlayers().contains(p)){
                                for(Player pl : Team.getBluePlayers()){
                                    pl.hidePlayer(p);
                                }
                            }
                            else if(Team.getBluePlayers().contains(p)){
                                for(Player pl : Team.getRedPlayers()){
                                    pl.hidePlayer(p);
                                }
                            }
                            
                            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 600, 1, true, false));
                            p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), 
                                new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                                
                                @Override
                                public void run() {
                                    p.sendMessage(ChatColor.YELLOW + "You are no longer invisible!");
                                    p.getInventory().clear();
                                    if(Team.getBluePlayers().contains(p)){
                                        giveGear(p, ChatColor.BLUE, SpecialGear.RINGBEARER);
                                        
                                        for(Player pl : Team.getRedPlayers()){
                                            pl.showPlayer(p);
                                        }
                                    }else{
                                        giveGear(p, ChatColor.RED, SpecialGear.RINGBEARER);
                                        
                                        for(Player pl : Team.getBluePlayers()){
                                            pl.showPlayer(p);
                                        }
                                    }
                                }
                            }, 600);
                        }else{
                            p.sendMessage(ChatColor.GRAY + "You must have at least 1 xp level to use the ring");
                        }
                    }
                }
                
                if(item.getType().equals(Material.GHAST_TEAR)){
                    p.getWorld().playEffect((p.getLocation().add(0, 1, 0)), Effect.SMOKE, 4);
                }
            }
        }
        
    }
}
