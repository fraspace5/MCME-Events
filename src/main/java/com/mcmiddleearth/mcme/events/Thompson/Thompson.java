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
package com.mcmiddleearth.mcme.events.Thompson;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author donoa_000
 */
public class Thompson {
    
    public static void welcome(Player p){
        sendMessage(p, "Welcome master " + p.getName().toLowerCase());
    }
    
    public static void sendMessage(Player p, String msg){
        p.sendMessage(ChatColor.DARK_BLUE + "Thompson" + ChatColor.RESET + ": " + msg);
    }
}
