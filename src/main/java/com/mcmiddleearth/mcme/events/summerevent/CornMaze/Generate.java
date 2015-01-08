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
package com.mcmiddleearth.mcme.events.summerevent.CornMaze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author Donovan
 */
public class Generate {
    
    public static Material MazeMaterail = Material.HAY_BLOCK;
    
    public static World w;
    
    //int randomNum = rand.nextInt((max - min) + 1) + min;
    
    public static void Generate(Location center, int size, int height){
        w = center.getWorld();
        Random r = new Random();
        Location base = center.clone();
        base.setY(base.getY() - 2);
        base.getBlock().setType(Material.DIAMOND_BLOCK);
//        for(int y = center.getBlockY(); y <= center.getBlockY() + (height - 1); y++){
            for(int x = center.getBlockX() - (size/2); x <= center.getBlockX() + (size/2); x++){
                for(int z = center.getBlockZ() - (size/2); z <= center.getBlockZ() + (size/2); z++){
                    new Location(w, x, center.getBlockY(), z).getBlock().setType(MazeMaterail);
                }
            }
//        }
        center.getBlock().setType(Material.AIR);
        ArrayList<Location> nodes = new ArrayList<>();
        Location CN = center;
        nodes.add(new Location(w, CN.getX(), CN.getY(), CN.getZ() - 1));
        nodes.add(new Location(w, CN.getX(), CN.getY(), CN.getZ() + 1));
        nodes.add(new Location(w, CN.getX() - 1, CN.getY(), CN.getZ()));
        nodes.add(new Location(w, CN.getX() + 1, CN.getY(), CN.getZ()));
        for(Location node : nodes){
            node.getBlock().setType(Material.AIR);
        }
        while(nodes.size() > 0){
            for(int node = 0; node<nodes.size(); node++){
                ArrayList<Boolean> posDirs = new ArrayList<>(Arrays.asList(new Boolean[] {false, false, false, false}));
                int dirNum = 0;
                for(int dir = 0; dir < 360; dir += 90){
                    if(dir == 0){
                        Location pos = nodes.get(node);
                        pos.add(0, 0, -1);
                        if((new Location(w, pos.getX() - 1, pos.getY(), pos.getZ()).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() - 1, pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX(), pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() + 1, pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() + 1, pos.getY(), pos.getZ()).getBlock().getType().equals(MazeMaterail))){
                            posDirs.set(0, Boolean.TRUE);
                            dirNum++;
                        }
                    }else if(dir == 90){
                        Location pos = nodes.get(node);
                        pos.add(1, 0, 0);
                        if((new Location(w, pos.getX(), pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() + 1, pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() + 1, pos.getY(), pos.getZ()).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() + 1, pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX(), pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))){
                            posDirs.set(1, Boolean.TRUE);
                            dirNum++;
                        }
                    }else if(dir == 180){
                        Location pos = nodes.get(node);
                        pos.add(0, 0, 1);
//                        new Location(w, pos.getX() + 1, pos.getY(), pos.getZ()).getBlock().setType(Material.EMERALD_BLOCK);
//                            new Location(w, pos.getX() + 1, pos.getY(), pos.getZ() + 1).getBlock().setType(Material.EMERALD_BLOCK);
//                            new Location(w, pos.getX(), pos.getY(), pos.getZ() + 1).getBlock().setType(Material.EMERALD_BLOCK);
//                            new Location(w, pos.getX() - 1, pos.getY(), pos.getZ() + 1).getBlock().setType(Material.EMERALD_BLOCK);
//                            new Location(w, pos.getX() - 1, pos.getY(), pos.getZ()).getBlock().setType(Material.EMERALD_BLOCK);
                        if((new Location(w, pos.getX() + 1, pos.getY(), pos.getZ()).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() + 1, pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX(), pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() - 1, pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() - 1, pos.getY(), pos.getZ()).getBlock().getType().equals(MazeMaterail))){
                            
                            posDirs.set(2, Boolean.TRUE);
                            dirNum++;
                        }
                    }else if(dir == 270){
                        Location pos = nodes.get(node);
                        pos.add(-1, 0, 0);
                        if((new Location(w, pos.getX(), pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() - 1, pos.getY(), pos.getZ() + 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() - 1, pos.getY(), pos.getZ()).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX() - 1, pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))&&
                                (new Location(w, pos.getX(), pos.getY(), pos.getZ() - 1).getBlock().getType().equals(MazeMaterail))){
                            posDirs.set(3, Boolean.TRUE);
                            dirNum++;
                        }
                    }
                }
                if(dirNum==0){
                    nodes.get(node).getBlock().setType(Material.DIAMOND_BLOCK);
                    nodes.remove(node);
                }else if(dirNum == 1){
                    for(int b = 0; b < posDirs.size(); b++){
                        if(posDirs.get(b)){
                            if(b == 0){
                                new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() - 1).getBlock().setType(Material.AIR);
                                nodes.set(node, new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() - 1));
                            }else if(b == 1){
                                new Location(w, nodes.get(node).getX() + 1, nodes.get(node).getY(), nodes.get(node).getZ()).getBlock().setType(Material.AIR);
                                nodes.set(node, new Location(w, nodes.get(node).getX() + 1, nodes.get(node).getY(), nodes.get(node).getZ()));
                            }else if(b == 2){
                                new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() + 1).getBlock().setType(Material.AIR);
                                nodes.set(node, new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() + 1));
                            }else if(b == 3){
                                new Location(w, nodes.get(node).getX() - 1, nodes.get(node).getY(), nodes.get(node).getZ()).getBlock().setType(Material.AIR);
                                nodes.set(node, new Location(w, nodes.get(node).getX() - 1, nodes.get(node).getY(), nodes.get(node).getZ()));
                            }
                        }
                    }
                }else if(dirNum > 1){
                    int dir = r.nextInt(3);
                    while(posDirs.get(dir) == false){
                        dir = r.nextInt(3);
                    }
                    Location Nloc = nodes.get(node);
                    if(dir == 0){
                        new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() - 1).getBlock().setType(Material.AIR);
                        nodes.set(node, new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() - 1));
                    }else if(dir == 1){
                        new Location(w, nodes.get(node).getX() + 1, nodes.get(node).getY(), nodes.get(node).getZ()).getBlock().setType(Material.AIR);
                        nodes.set(node, new Location(w, nodes.get(node).getX() + 1, nodes.get(node).getY(), nodes.get(node).getZ()));
                    }else if(dir == 2){
                        new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() + 1).getBlock().setType(Material.AIR);
                        nodes.set(node, new Location(w, nodes.get(node).getX(), nodes.get(node).getY(), nodes.get(node).getZ() + 1));
                    }else if(dir == 3){
                        new Location(w, nodes.get(node).getX() - 1, nodes.get(node).getY(), nodes.get(node).getZ()).getBlock().setType(Material.AIR);
                        nodes.set(node, new Location(w, nodes.get(node).getX() - 1, nodes.get(node).getY(), nodes.get(node).getZ()));
                    }
                    if(r.nextInt(10) == 5){
                        int dir2 = r.nextInt(3);
                        while(posDirs.get(dir2) == false && dir2 != dir){
                            dir2 = r.nextInt(3);
                        }
                        if(dir2 == 0){
                            new Location(w, Nloc.getX(), Nloc.getY(), Nloc.getZ() - 1).getBlock().setType(Material.AIR);
                            nodes.add(new Location(w, Nloc.getX(), Nloc.getY(), Nloc.getZ() - 1));
                        }else if(dir2 == 1){
                            new Location(w, Nloc.getX() + 1, Nloc.getY(), Nloc.getZ()).getBlock().setType(Material.AIR);
                            nodes.add(new Location(w, Nloc.getX() + 1, Nloc.getY(), Nloc.getZ()));
                        }else if(dir2 == 2){
                            new Location(w, Nloc.getX(), Nloc.getY(), Nloc.getZ() + 1).getBlock().setType(Material.AIR);
                            nodes.add(new Location(w, Nloc.getX(), Nloc.getY(), Nloc.getZ() + 1));
                        }else if(dir2 == 3){
                            new Location(w, Nloc.getX() - 1, Nloc.getY(), Nloc.getZ()).getBlock().setType(Material.AIR);
                            nodes.add(new Location(w, Nloc.getX() - 1, Nloc.getY(), Nloc.getZ()));
                        }
                    }
                }
            }
        }
    }
}
