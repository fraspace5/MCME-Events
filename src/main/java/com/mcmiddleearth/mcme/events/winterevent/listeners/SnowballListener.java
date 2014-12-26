/**
 * This file is part of winterEvent, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 Henry Slawniak <http://mcme.co/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.mcme.lizzehface.winterevent.listeners;

import co.mcme.lizzehface.winterevent.stats.PlayerStats;
import co.mcme.lizzehface.winterevent.stats.PlayerStatsContainer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class SnowballListener implements Listener{

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Snowball)) {
            return;
        }
        PlayerStats damaged = PlayerStatsContainer.getForPlayer((Player) event.getEntity());
        PlayerStats damager = PlayerStatsContainer.getForPlayer((Player) (((Snowball) event.getDamager()).getShooter()));
        damaged.incrementHitSelf();
        damager.incrementHitOthers();
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball)) {
            return;
        }
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        PlayerStats thrower = PlayerStatsContainer.getForPlayer((Player) event.getEntity().getShooter());
        thrower.incrementThrown();
    }
}
