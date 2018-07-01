/*
 * This file is part of WinterEvent.
 * 
 * WinterEvent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * WinterEvent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with WinterEvent.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package com.mcmiddleearth.mcme.events.winterevent.SnowManInvasion.EventHandles;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 *
 * @author Donovan
 */
public class SnowballHandle implements Listener{
    
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){
        final Entity damager = e.getDamager();
        final Entity target = e.getEntity();
        if(damager instanceof Projectile){
            Projectile pt = (Projectile) damager;
            if(pt instanceof Snowball){
                try{
                    if(target instanceof Player){
                        ((Player) target).damage(2);
                    }else if(target instanceof Snowman){
                        ((Snowman) target).damage(2);
                        if(pt.getShooter() instanceof Player){
                            Player p = ((Player) pt.getShooter());
                            p.playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 100, 0);
                            ((Snowman) target).setTarget(p);
                        }
                    }
                }catch (Exception ex){}
            }
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e){
        if(e.getEntity().getShooter() instanceof Player){
            
        }
    }
    
}
