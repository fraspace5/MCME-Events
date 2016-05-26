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
package com.mcmiddleearth.mcme.events.PVP;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.mcmiddleearth.mcme.events.PVP.Team;

/**
 *
 * @author Eric
 */
public class TeamChat implements CommandExecutor{
    
    private Player p;
   
    private String messageString = "";
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        
       if(args.length >= 1 && cs instanceof Player) {
                      
           p = (Player) cs;
           
           if(!Team.getRedPlayers().contains(p) && !Team.getBluePlayers().contains(p)){
               p.sendMessage(ChatColor.RED + "You aren't on a team!");
           }
           
           for(String str : args) {
               
               if(messageString.equals("")) {
                   messageString += str;
               }
               
               else if(!str.equals("")) {
                   messageString += (" " + str);
               }
               
               else {
                   return false;
               }
               
           }
           if(Team.getRedPlayers().contains(p)){
               for(Player player : Team.getRedPlayers()){
                   
                   player.sendMessage(ChatColor.DARK_RED + "[TEAM] " + p.getName() + ChatColor.RED + ": " + messageString);
                   
               }    
           }
           else if(Team.getBluePlayers().contains(p)){
               for(Player player : Team.getBluePlayers()){
                   
                   player.sendMessage(ChatColor.DARK_BLUE + "[TEAM] " + p.getName() + ChatColor.BLUE + ": " + messageString);
                   
               }
           }
           
           messageString = "";
           return true;
        }else{
           return false;
       }
           
    }
}
