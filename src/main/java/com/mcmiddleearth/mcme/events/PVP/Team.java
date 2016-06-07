/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.mcme.events.PVP;

import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Eric
 */
public class Team {
        
        @Getter
        private String name;
       
        @Getter @Setter
        private int score;
        
        @Getter
        private static ArrayList<Location> redCapturedPoints = new ArrayList<>();
        
        @Getter
        private static ArrayList<Location> blueCapturedPoints = new ArrayList<>();
        
        @Getter @Setter
        private Player bearer;
        
        @Getter @Setter
        private boolean canRespawn = true;
//        @Getters
//        private HashMap<String, Integer> Classes = new HashMap<>();
        
        @Getter
        private static ArrayList<Player> bluePlayers = new ArrayList<>();
        
        @Getter
        private static ArrayList<Player> redPlayers = new ArrayList<>();
        
        @Getter
        private static ArrayList<Player> spectators = new ArrayList<>();
        
        @Getter
        private static ArrayList<Player> infected = new ArrayList<>();
        
        @Getter
        private static ArrayList<Player> survivors = new ArrayList<>();
        
        @Getter
        private ArrayList<Player> Alive = new ArrayList<>();
        
        @Getter
        private GameMode gamemode;
        
        @Getter
        private Scoreboard board;
        
        public enum Teams {
            RED,BLUE,INFECTED,SURVIVORS,SPECTATORS;
        }
        
        public enum GameType {
            RINGBEARER,TEAM_CONQUEST,KING_OF_THE_HILL
        }
        
        public Team(GameType type){
            if(type == GameType.RINGBEARER){
                board = Bukkit.getScoreboardManager().getNewScoreboard();
                Objective objective = board.registerNewObjective("Bearer", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.setDisplayName("Bearer");
            }
        }
        
        public Team(){
            
        }
        
        public static void addToTeam(Player p,Teams team){
            Team.removeFromTeam(p);
            
            switch (team){
                case RED:
                    redPlayers.add(p);
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.RED + "Red");
                    ChatHandler.getPlayerColors().put(p.getName(), ChatColor.RED);
                    p.sendMessage(ChatColor.RED + "You are on the Red Team!");
                    
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.RED + p.getName());
                    }else{
                        String newName = p.getName().substring(0,13);
                        p.setPlayerListName(ChatColor.RED + newName);
                    }
                    
                    if(p.getGameMode() != GameMode.ADVENTURE){
                        p.setGameMode(GameMode.ADVENTURE);
                    }
                    p.setDisplayName(ChatColor.RED + p.getName());
                    
                    addToBukkitTeam(p, ChatColor.RED);
                    break;
                case BLUE:
                    bluePlayers.add(p);
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.BLUE + "Blue");
                    ChatHandler.getPlayerColors().put(p.getName(), ChatColor.BLUE);
                    p.sendMessage(ChatColor.BLUE + "You are on the Blue Team!");
                    
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.BLUE + p.getName());
                    }else{
                        String newName = p.getName().substring(0,13);
                        p.setPlayerListName(ChatColor.BLUE + newName);
                    }
                    if(p.getGameMode() != GameMode.ADVENTURE){
                        p.setGameMode(GameMode.ADVENTURE);
                    }
                    p.setDisplayName(ChatColor.BLUE + p.getName());
                    
                    addToBukkitTeam(p, ChatColor.BLUE);
                    break;
                case SPECTATORS:
                    spectators.add(p);
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.GRAY + "Spectator");
                    ChatHandler.getPlayerColors().put(p.getName(), ChatColor.GRAY);
                    p.sendMessage(ChatColor.GRAY + "You are Spectating!");
                    p.sendMessage(ChatColor.GRAY + "As a spectator, game participants won't see your chat.");
                    
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.GRAY + p.getName());
                    }else{
                        String newName = p.getName().substring(0,13);
                        p.setPlayerListName(ChatColor.GRAY + newName);
                    }
                    if(p.getGameMode() != GameMode.SPECTATOR){
                        p.setGameMode(GameMode.SPECTATOR);
                    }
                    p.setDisplayName(ChatColor.GRAY + p.getName());
                    addToBukkitTeam(p, ChatColor.GRAY);
                    break;
                case SURVIVORS:
                    survivors.add(p);
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.BLUE + "Survivor");
                    ChatHandler.getPlayerColors().put(p.getName(), ChatColor.BLUE);
                    p.sendMessage(ChatColor.BLUE + "You are a Survivor!");
                    
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.BLUE + p.getName());
                    }else{
                        String newName = p.getName().substring(0,13);
                        p.setPlayerListName(ChatColor.BLUE + newName);
                    }
                    if(p.getGameMode() != GameMode.ADVENTURE){
                        p.setGameMode(GameMode.ADVENTURE);
                    }
                    p.setDisplayName(ChatColor.BLUE + p.getName());
                    
                    addToBukkitTeam(p, ChatColor.BLUE);
                    break;
                case INFECTED:
                    infected.add(p);
                    ChatHandler.getPlayerPrefixes().put(p.getName(), ChatColor.DARK_RED + "Infected");
                    ChatHandler.getPlayerColors().put(p.getName(), ChatColor.DARK_RED);
                    p.sendMessage(ChatColor.DARK_RED + "You are Infected!");
                    
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100000,1));
                    
                    if(p.getName().length() < 14){
                        p.setPlayerListName(ChatColor.DARK_RED + p.getName());
                    }else{
                        String newName = p.getName().substring(0,13);
                        p.setPlayerListName(ChatColor.DARK_RED + newName);
                    }
                    if(p.getGameMode() != GameMode.ADVENTURE){
                        p.setGameMode(GameMode.ADVENTURE);
                    }
                    p.setDisplayName(ChatColor.DARK_RED + p.getName());
                    addToBukkitTeam(p, ChatColor.DARK_RED);
                    break;
            }
        }
        
        public static void removeFromTeam(Player p){
            if(redPlayers.contains(p)){
                redPlayers.remove(p);
                removeFromBukkitTeam(p);
            }
            else if(bluePlayers.contains(p)){
                bluePlayers.remove(p);
                removeFromBukkitTeam(p);
            }
            else if(spectators.contains(p)){
                spectators.remove(p);
                removeFromBukkitTeam(p);
            }
            else if(survivors.contains(p)){
                survivors.remove(p);
                removeFromBukkitTeam(p);
            }
            else if(infected.contains(p)){
                infected.remove(p);
                removeFromBukkitTeam(p);
                p.removePotionEffect(PotionEffectType.SPEED);
            }
            
            if(!p.isDead()){
                p.setHealth(20);
            }
            p.setMaxHealth(20);
            p.setDisplayName(ChatColor.WHITE + p.getName());
            p.setPlayerListName(p.getName());
            ChatHandler.getPlayerPrefixes().remove(p);
        }
    public static boolean areTeamMates(Player p1, Player p2){
        
        if(Team.getRedPlayers().contains(p1) && Team.getRedPlayers().contains(p2)){
            return true;
        }
        else if(Team.getBluePlayers().contains(p1) && Team.getBluePlayers().contains(p2)){
            return true;
        }
        else if(Team.getSpectators().contains(p1) && Team.getSpectators().contains(p2)){
            return true;
        }
        else if(Team.getSurvivors().contains(p1) && Team.getSurvivors().contains(p2)){
            return true;
        }
        else if(Team.getInfected().contains(p1) && Team.getInfected().contains(p2)){
            return true;
        }
        else{
            return false;
        }
        
    }    
    private static org.bukkit.scoreboard.Team blue = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("blue");
    private static org.bukkit.scoreboard.Team darkAqua = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("darkaqua");
    private static org.bukkit.scoreboard.Team darkBlue = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("darkblue");
    private static org.bukkit.scoreboard.Team darkGray = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("darkgray");
    private static org.bukkit.scoreboard.Team darkGreen = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("darkgreen");
    private static org.bukkit.scoreboard.Team darkPurple = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("darkpurple");
    private static org.bukkit.scoreboard.Team darkRed = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("darkred");
    private static org.bukkit.scoreboard.Team gold = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("gold");
    private static org.bukkit.scoreboard.Team gray = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("gray");
    private static org.bukkit.scoreboard.Team green = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("green");
    private static org.bukkit.scoreboard.Team lightPurple = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("lightpurple");
    private static org.bukkit.scoreboard.Team red = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("red");
    private static org.bukkit.scoreboard.Team yellow = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("yellow");
    
    public static void addToBukkitTeam(Player p, ChatColor c){
        
        
        
        switch(c){
            case BLUE:
                blue.addPlayer(p);
                break;
            case DARK_AQUA:
                darkAqua.addPlayer(p);
                break;
            case DARK_BLUE:
                darkBlue.addPlayer(p);
                break;
            case DARK_GRAY:
                darkGray.addPlayer(p);
                break;
            case DARK_GREEN:
                darkGreen.addPlayer(p);
                break;
            case DARK_PURPLE:
                darkPurple.addPlayer(p);
                break;
            case DARK_RED:
                darkRed.addPlayer(p);
                break;
            case GOLD:
                gold.addPlayer(p);
                break;
            case GRAY:
                gray.addPlayer(p);
                break;
            case GREEN:
                green.addPlayer(p);
                break;
            case LIGHT_PURPLE:
                lightPurple.addPlayer(p);
                break;
            case RED:
                red.addPlayer(p);
                break;
            case YELLOW:
                yellow.addPlayer(p);
                break;
        }
    }
    
    public static void removeFromBukkitTeam(Player p){
        if(blue != null){
            if(blue.hasPlayer(p)){
                blue.removePlayer(p);
            }
        }
        else if(darkAqua != null){
            if(darkAqua.hasPlayer(p)){
                darkAqua.removePlayer(p);
            }
        }
        else if(darkBlue != null){
            if(darkBlue.hasPlayer(p)){
                darkBlue.removePlayer(p);
            }
        }
        else if(darkGray != null){
            if(darkGray.hasPlayer(p)){
                darkGray.removePlayer(p);
            }
        }
        else if(darkGreen != null){
            if(darkGreen.hasPlayer(p)){
                darkGreen.removePlayer(p);
            }
        }
        else if(darkPurple != null){
            if(darkPurple.hasPlayer(p)){
                darkPurple.removePlayer(p);
            }
        }
        else if(darkRed != null){
            if(darkRed.hasPlayer(p)){
                darkRed.removePlayer(p);
            }
        }
        else if(gold != null){
            if(gold.hasPlayer(p)){
                gold.removePlayer(p);
            }
        }
        else if(gray != null){
            if(gray.hasPlayer(p)){
                gray.removePlayer(p);
            }
        }
        else if(green != null){
            if(green.hasPlayer(p)){
                green.removePlayer(p);
            }
        }
        else if(lightPurple != null){
            if(lightPurple.hasPlayer(p)){
                lightPurple.removePlayer(p);
            }
        }
        else if(red != null){
            if(red.hasPlayer(p)){
                red.removePlayer(p);
            }
        }
        else if(yellow != null){
            if(yellow.hasPlayer(p)){
                yellow.removePlayer(p);
            }
        }
    }
    
}
