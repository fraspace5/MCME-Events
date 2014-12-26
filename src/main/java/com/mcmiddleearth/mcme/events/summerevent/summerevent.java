/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mcmiddleearth.mcme.summerevent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Donovan
 */
public class summerevent extends JavaPlugin implements CommandExecutor, Listener{
    public summerevent plugin;
    private World spawn;
    private InputStream in;
    private OutputStream out;
    private ArrayList<String> noHunger = new ArrayList<>();
    public ArrayList<CapturePoint> cps = new ArrayList<>();
    public HashMap<String, Team> teams = new HashMap<>();
    public static boolean ctfStarted = false;
    public World ctfworld;
    @Override
    public void onEnable(){
        plugin = this;
        this.saveDefaultConfig();
        this.reloadConfig();
        if(this.getConfig().contains("worlds")){
            for(String s : this.getConfig().getStringList("worlds")){
                Bukkit.getServer().getWorlds().add(Bukkit.getServer().createWorld(new WorldCreator(s)));
            }
        }
        if(this.getConfig().contains("noHunger")){
            noHunger.addAll(this.getConfig().getStringList("noHunger"));
        }
        if(this.getConfig().contains("ctf")){
            ctfworld = Bukkit.getWorld(this.getConfig().getString("ctf"));
        }
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this, this);
        spawn = Bukkit.getWorlds().get(0);
        getCommand("world").setExecutor(this);
        getCommand("jump").setExecutor(this);
        getCommand("ctf").setExecutor(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    if(in != null && out != null){
                        System.gc();
                        in.close();
                        out.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(summerevent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 0, (5 * 60) * 20);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            if(cmd.getName().equalsIgnoreCase("world")){
                Player p = (Player) sender;
                if(args.length>1){
                    if(args[0].equalsIgnoreCase("save")&&p.hasPermission("summerevent.saveworld")){
                        saveWorld(p.getWorld(), args[1], p);
                    }else if(args[0].equalsIgnoreCase("load")&&p.hasPermission("summerevent.loadworld")){
                        loadWorld(p.getWorld(), args[1]);
                    }
                    return true;
                }else if(args.length>0){
                    if(args[0].equalsIgnoreCase("list")){
                        p.sendMessage("Loaded Worlds:");
                        for(World w : Bukkit.getWorlds()){
                            p.sendMessage(w.getName());
                        }
                        if(p.hasPermission("summerevent.loadworld")){
                            p.sendMessage("World Backups");
                            for(File f: this.getDataFolder().listFiles()){
                                p.sendMessage(f.getName());
                            }
                        }
                        return true;
                    }
                }
            }else if(cmd.getName().equalsIgnoreCase("jump")){
                Player p = (Player) sender;
                if(args.length>0&&p.hasPermission("summerevent.jump")){
                    if(!p.getWorld().equals(spawn)&&!args[0].equalsIgnoreCase(spawn.getName())){
                        p.sendMessage("you must be in spawn to jump");
                        p.sendMessage("use /jump "+ spawn.getName());
                        return true;
                    }
                    World w = Bukkit.getWorld(args[0]);
                    if(w == null){
                        p.sendMessage(args[0] + " is not a loaded world");
                    } else {
                        p.sendMessage("Welcome to " + ChatColor.BLUE + w.getName());
//                        if(!p.hasPermission("summerevent.admin")){
                            Inventory i = p.getInventory();
                            i.clear();
                            p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                            p.getInventory().setContents(i.getContents());
                            if(w.equals(spawn)){
                                p.setGameMode(GameMode.ADVENTURE);
                            }
//                        }
                        p.teleport(w.getSpawnLocation());
                    }
                    return true;
                }else if(cmd.getName().equalsIgnoreCase("ctf")){
                    if(args.length > 0){
                        if(args[0].equalsIgnoreCase("reset")){
                            //reset map and points/scores
                            summerevent.ctfStarted = false;
                            loadWorld(ctfworld, "ctfsv");
                        }else if(args[0].equalsIgnoreCase("start")){
                            //start the ctf game
                            if(ctfworld == null){
                                p.sendMessage("no ctf world set");
                                return true;
                            }
                            saveWorld(ctfworld, "ctfsv", p);
                            summerevent.ctfStarted = true;
                            
                            
                        }
                    }
                    if(args.length >1){
                        if(args[0].equalsIgnoreCase("new")){
                            if(args[1].equalsIgnoreCase("point")&&args.length>2){
                                cps.add(new CapturePoint(p.getLocation(), Integer.parseInt(args[2])));
                            }else if(args[1].equalsIgnoreCase("team")&&args.length>2){
                                teams.put(args[2], new Team(args[2]));
                            }
                        }else if(args[0].equalsIgnoreCase("set")){
                            if(args[1].equalsIgnoreCase("teams")){
                                //generate teams from players in current world
                                p.sendMessage("not working");
                            }else if(args[1].equalsIgnoreCase("team")&&args.length>2){
                                if(args.length>3){
                                    teams.get(args[2]).members.add(args[3]);
                                }
                                teams.get(args[2]).members.add(p.getName());
                            }
                        }
                    }
                }
            }
        }
        return false;
        
    }
    ///--CTF--///
    
    
    
    
    
    
    
    ///--World Work---///
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        if(e.getPlayer().getWorld().equals(spawn)){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        if(e.getPlayer().getWorld().equals(spawn)){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e){
        World pw = e.getEntity().getWorld();
        if(noHunger.contains(pw.getName())){
            e.setFoodLevel(20);
        }
    }
    private void saveWorld(final World w, final String name, final Player sender){
        w.save();
        World notw = spawn;
        if(notw == null){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.kickPlayer("reseting world");
            }
        }else{
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.getWorld().equals(w)){
                    p.teleport(notw.getSpawnLocation());
//                    if(!p.hasPermission("summerevent.admin")){
//                        Inventory i = p.getInventory();
//                        i.clear();
//                        p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
//                        p.getInventory().setContents(i.getContents());
//                    }
                }
            }
        }
        final File old = w.getWorldFolder();
                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {   
                        copyWorld(old, new File(summerevent.this.getDataFolder()+ System.getProperty("file.separator") + name));
                        getServer().getScheduler().scheduleSyncDelayedTask(summerevent.this, new Runnable() {
                            @Override
                            public void run() {   
                                sender.teleport(w.getSpawnLocation());
                            }
                        }, 20L);
                    }
            }, 20L);
    }
    private boolean loadWorld(World w, final String name){
        if(!new File(summerevent.this.getDataFolder() + System.getProperty("file.separator") + name).exists()){
            return false;
        }
        final String wname = w.getName();
        World notw = spawn;
        if(notw == null){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.kickPlayer("reseting world");
            }
        }else{
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.getWorld().equals(w)){
                    p.teleport(notw.getSpawnLocation());
//                    if(!p.hasPermission("summerevent.admin")){
                        Inventory i = p.getInventory();
                        i.clear();
                        p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                        p.getInventory().setContents(i.getContents());
//                    }
                }
            }
        }
        Bukkit.getServer().unloadWorld(w, true);
        final File path = w.getWorldFolder();
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {   
                    copyWorld(new File(summerevent.this.getDataFolder() + System.getProperty("file.separator") + name), path);
                    getServer().getScheduler().scheduleSyncDelayedTask(summerevent.this, new Runnable() {
                        @Override
                        public void run() {   
                            Bukkit.getServer().getWorlds().add(Bukkit.getServer().createWorld(new WorldCreator(wname)));
                        }
                    }, 20L);
                }
            }, 20L);
        return false;
    }
    private boolean deleteWorld(File path) {
      if(path.exists()) {
//          System.out.println(path.toString());
          if (path.isDirectory()) {
            File files[] = path.listFiles();
            for (String file : path.list()) {
                deleteWorld(new File(path, file));
            }
          } else {
            path.delete();
          }
      }
      return(path.delete());
    }
    private void copyWorld(File src, File dest){
        deleteWorld(dest);
        try {
            if(src.isDirectory()){
 
    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();
//    		   System.out.println("Directory copied from " 
//                              + src + "  to " + dest);
    		}
 
    		//list all the directory contents
    		String files[] = src.list();
 
    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   copyWorld(srcFile,destFile);
    		}
 
    	}else{
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		in = new FileInputStream(src);
                out = new FileOutputStream(dest); 
 
    	        byte[] buffer = new byte[1024];
 
    	        int length;
    	        //copy the file content in bytes 
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }
 
    	        in.close();
    	        out.close();
//    	        System.out.println("File copied from " + src + " to " + dest);
    	}
        } catch (IOException e) {
            Bukkit.getLogger().info(e.getMessage());
        }
    }
}
