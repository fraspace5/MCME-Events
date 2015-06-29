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

import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class CommandBlockHandler implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if(cs instanceof BlockCommandSender && args.length > 1){
            if(args[0].equalsIgnoreCase("cmdBlock")){
                if(args[1].equalsIgnoreCase("prefix") && args.length >= 4){
                    if(args.length == 5){
                        if(ChatColor.valueOf(args[4]) != null){
                            ChatHandler.getPlayerPrefixes().put(args[3], ChatColor.valueOf(args[4]) + args[2]);
                        }else{
                            cs.sendMessage("not a valid bukkit chat color, " + Arrays.toString(ChatColor.values()));
                        }
                    }else{
                        ChatHandler.getPlayerPrefixes().put(args[2], args[3]);
                    }
                }else if(args[1].equalsIgnoreCase("endGame")){
                    
                }
            }
        }
        return true;
    }
}
