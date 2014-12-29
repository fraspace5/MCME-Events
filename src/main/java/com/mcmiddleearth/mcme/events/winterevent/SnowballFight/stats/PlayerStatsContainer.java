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
package com.mcmiddleearth.mcme.events.winterevent.SnowballFight.stats;

import com.mcmiddleearth.mcme.events.Main;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatsContainer {

    private static HashMap<String, PlayerStats> stats = new HashMap<>();

    public static PlayerStats getForPlayer(Player player) {
        if (stats.containsKey(player.getName())) {
            return stats.get(player.getName());
        } else {
            File location = new File(Main.getPlayerDirectory(), player.getUniqueId().toString() + ".snowball");
            if (location.exists()) {
                Main.getServerInstance().getLogger().info("Loading stats for " + player.getName() + " from disk at " + location.getName());
                try {
                    PlayerStats playerStats = Main.getObjectMapper().readValue(location, PlayerStats.class);
                    stats.put(player.getName(), playerStats);
                    return playerStats;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Main.getServerInstance().getLogger().info("Stats for " + player.getName() + " do not exist yet, creating new.");
                PlayerStats playerStats = new PlayerStats(player);
                try {
                    playerStats.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stats.put(player.getName(), playerStats);
                return playerStats;
            }
        }
        return null;
    }

    public static PlayerStats getForPlayer(String name) {
        // Should come up with some clever lookup thing, but oh well
        if (stats.containsKey(name)) {
            return stats.get(name);
        }
        return null;
    }

    public static void saveAllAndEmpty() {
        for (Map.Entry<String, PlayerStats> entry : stats.entrySet()) {
            try {
                entry.getValue().save();
                stats.remove(entry.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveAll() {
        for (Map.Entry<String, PlayerStats> entry : stats.entrySet()) {
            try {
                entry.getValue().save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadAll() {
        saveAll();
        HashMap<String, PlayerStats> temp = new HashMap<String, PlayerStats>();
        for (File file : Main.getPlayerDirectory().listFiles()) {
            if (!file.isDirectory()) {
                try {
                    PlayerStats playerStats = Main.getObjectMapper().readValue(file, PlayerStats.class);
                    Main.getServerInstance().getLogger().info("loading " + playerStats.getPlayerUUID());
                    temp.put(playerStats.getPlayerName(), playerStats);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        stats.putAll(temp);
    }

    public static int wipeAll() {
        int files = Main.getPlayerDirectory().listFiles() .length;
        for(File file : Main.getPlayerDirectory().listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
        stats.clear();
        return files;
    }

}
