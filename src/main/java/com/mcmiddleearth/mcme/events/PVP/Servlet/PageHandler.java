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
package com.mcmiddleearth.mcme.events.PVP.Servlet;

import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.PVP.PVPCore;
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import com.mcmiddleearth.mcme.events.PVP.PlayerStat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bukkit.Bukkit;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class PageHandler extends AbstractHandler{

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        String[] targets = target.split("/");
        if(targets.length == 0){
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println("SummerEvent Servlet enabled and running!");
            try {
                Scanner s = new Scanner(new File("logs" + System.getProperty("file.separator") + "latest.log"));
                response.getWriter().println("----- Log for Server: -----");
                String line = "";
                while(s.hasNextLine()){
                    line = s.nextLine();
                    response.getWriter().println(line);
                }
            } catch (FileNotFoundException ex) {
                response.sendError(404, ex.toString());
            }
        }else{
            PlayerStat ps = null;
            if(PlayerStat.getPlayerStats().containsKey(targets[1])){
                ps = PlayerStat.getPlayerStats().get(targets[1]);
            }else{
                File loc = new File(PVPCore.getSaveLoc() + Main.getFileSep() + "stats" + Main.getFileSep() + Bukkit.getOfflinePlayer(targets[1]).getUniqueId());
                if(!loc.exists()){
                    response.getWriter().println("Player " + targets[1] + " not found");
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                    return;
                }else{
                    ps = (PlayerStat) DBmanager.loadObj(PlayerStat.class, loc);
                }
            }
            if(targets.length > 2){
                if(targets[2].equalsIgnoreCase("raw")){
                    response.getWriter().println(DBmanager.getJSonParser().writeValueAsString(ps));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                    return;
                }
            }
            String resp = "Viewing stats for " + targets[1] + ":<br><br>" +
                    "Deaths: " + ps.getDeaths() + "<br><ul>" + 
                    "<li>Suicides: " + ps.getSuicides() + "</li></ul>" +
                    "Kills: " + ps.getKills().size() + "<br><ul>";
            for(String s : ps.getKills()){
                resp += "<li>" + s + "</li>";
            }
            resp += "</ul>"; 
            response.getWriter().println(resp);
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
    
}
