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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcmiddleearth.mcme.events.Main;
import com.mcmiddleearth.mcme.events.Util.DBmanager;
import com.mcmiddleearth.mcme.events.PVP.Gamemode.Gamemode;
import com.mcmiddleearth.mcme.events.PVP.Handlers.JoinLeaveHandler;
import com.mcmiddleearth.mcme.events.PVP.Team.Teams;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class PlayerStat {
    
    @Getter
    private ArrayList<String> playersKilled = new ArrayList<String>();
    
    @Getter @Setter
    private int Kills = 0;
    
    @Getter @Setter
    private int Deaths = 0;
    
    @Getter @Setter
    private int gamesPlayed = 0;
    
    @Getter @Setter
    private int gamesWon = 0;
    
    @Getter @Setter
    private int gamesLost = 0;
    
    @Getter @Setter
    private int gamesSpectated = 0;
    
    @Getter @Setter
    private static HashMap<String, PlayerStat> playerStats = new HashMap<>();
    
    @Getter @Setter @JsonIgnore    
    private UUID uuid;
    
    public PlayerStat(){}
    
    public PlayerStat(UUID uuid){this.uuid = uuid;}
    
    public static boolean loadStat(OfflinePlayer p){
        File loc = new File(PVPCore.getSaveLoc() + Main.getFileSep() + "stats" + Main.getFileSep() + p.getUniqueId());
        if(loc.exists()){
            PlayerStat ps = (PlayerStat) DBmanager.loadObj(PlayerStat.class, loc);
            ps.setUuid(p.getUniqueId());
            try {
                System.out.println("Loaded: " + DBmanager.getJSonParser().writeValueAsString(ps));
            } catch (JsonProcessingException ex) {
                Logger.getLogger(JoinLeaveHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            playerStats.put(p.getName(), ps);
            return true;
        }else{
            playerStats.put(p.getName(), new PlayerStat(p.getUniqueId()));
            
            return false;
        }
    }
        
    public void saveStat(){
        File loc = new File(PVPCore.getSaveLoc() + Main.getFileSep() + "stats");
        try {
            System.out.println("Saved: " + DBmanager.getJSonParser().writeValueAsString(this));
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JoinLeaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        DBmanager.saveObj(this, loc, uuid.toString());
    }
    
    public void addDeath(){Deaths++;}
    public void addPlayerKilled(String k){playersKilled.add(k);}
    public void addKill(){Kills++;}
    public void addPlayedGame(){gamesPlayed++;}
    public void addGameWon(){gamesWon++;}
    public void addGameLost(){gamesLost++;};
    public void addGameSpectated(){gamesSpectated++;};
    
    public static void addGameWon(Teams t){
        
        switch(t){
            case RED:
                for(Player p : Team.getRed().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameWon();
                }
                break;
                
            case BLUE:
                for(Player p : Team.getBlue().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameWon();
                }
                break;
            case INFECTED:
                for(Player p : Team.getInfected().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameWon();
                }
                break;
            case SURVIVORS:
                for(Player p : Team.getSurvivor().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameWon();
                }
                break;
        }
        
    }
    
    public static void addGameLost(Teams t){
        switch(t){
            case RED:
                for(Player p : Team.getRed().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameLost();
                }
                break;
                
            case BLUE:
                for(Player p : Team.getBlue().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameLost();
                }
                break;
            case INFECTED:
                for(Player p : Team.getInfected().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameLost();
                }
                break;
            case SURVIVORS:
                for(Player p : Team.getSurvivor().getMembers()){
                    PlayerStat.getPlayerStats().get(p.getName()).addGameLost();
                }
                break;
        }
        
    }
    public static void addGameSpectatedAll(){
        for(Player p : Team.getSpectator().getMembers()){
            PlayerStat.getPlayerStats().get(p.getName()).addGameSpectated();
        }
    }
    
    public static class StatListener implements Listener{
        
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent e){
            if(PVPCommandCore.getRunningGame() != null){
                Player d = e.getEntity();
                if(PVPCommandCore.getRunningGame().getGm().getPlayers().contains(d)){
                    PlayerStat ps = PlayerStat.getPlayerStats().get(d.getName());
                    if(d.getKiller() != null){
                        Player k = d.getKiller();
                        if(PVPCommandCore.getRunningGame().getGm().getPlayers().contains(k)){
                            if(!PlayerStat.getPlayerStats().get(k.getName()).getPlayersKilled().contains(d.getName())){
                                PlayerStat.getPlayerStats().get(k.getName()).addPlayerKilled(d.getName());
                            }
                        }
                        PlayerStat.getPlayerStats().get(k.getName()).addKill();
                    }
                    ps.setDeaths(ps.getDeaths()+1);
                }
            }
        }
    }
}
