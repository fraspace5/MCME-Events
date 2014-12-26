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

package co.mcme.lizzehface.winterevent.SnowManInvasion.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Donovan
 */
public class Invasion implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("WinterEvent.Invasion")){
            if(sender instanceof Player){
                //start in world of player
                
            }else{
                if(args.length > 0){
                    if(Bukkit.getWorld(args[0]) != null){
                        //start in world
                    }
                }else{
                    sender.sendMessage("Need world");
                    return true;
                }
            }
        }else{
            sender.sendMessage(ChatColor.RED + "No Perms");
            return true;
        }
        return false;
    }
    
}
