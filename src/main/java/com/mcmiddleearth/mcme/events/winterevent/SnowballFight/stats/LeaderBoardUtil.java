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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LeaderBoardUtil {

    private static DecimalFormat df = new DecimalFormat("0.00");

    public static void showLeaderboard(Player player) {
        HashMap<String, PlayerStats> stats = getPlayerStats();
        TreeMap<Integer, PlayerStats> thrownSort = new TreeMap<>();
        TreeMap<Integer, PlayerStats> hitSelfSort = new TreeMap<>();
        TreeMap<Integer, PlayerStats> hitOthersSort = new TreeMap<>();
        for (Map.Entry<String, PlayerStats> entry : stats.entrySet()) {
            thrownSort.put(entry.getValue().getThrown(), entry.getValue());
            hitSelfSort.put(entry.getValue().getHitSelf(), entry.getValue());
            hitOthersSort.put(entry.getValue().getHitOthers(), entry.getValue());
        }
        player.sendMessage(
                ChatColor.GRAY + "-----------------------\n"
                        + ChatColor.RED + "" + ChatColor.BOLD + "Top Hits With Snowballs\n"
                        + getTopTen(hitOthersSort)
                        + ChatColor.RED + "" + ChatColor.BOLD + "Top Hits From Snowballs\n"
                        + getTopTen(hitSelfSort)
                        + ChatColor.RED + "" + ChatColor.BOLD + "Top Thrown Snowballs\n"
                        + getTopTen(thrownSort));
    }

    private static String getTopTen(TreeMap<Integer, PlayerStats> map) {
        StringBuilder out = new StringBuilder();
        int current = 1;
        for (Map.Entry<Integer, PlayerStats> entry : map.descendingMap().entrySet()) {
            if (current + 1 > 10) {
                break;
            }
            PlayerStats playerStat = entry.getValue();
            if (playerStat.getHitOthers() == entry.getKey()) {
                out.append(ChatColor.GOLD + "" + current + ChatColor.GRAY + ". " + ChatColor.AQUA + entry.getValue().getPlayerName() + ChatColor.GRAY + ": " + ChatColor.GREEN + entry.getValue().getHitOthers() + "\n");
            } else if (playerStat.getThrown() == entry.getKey()) {
                out.append(ChatColor.GOLD + "" + current + ChatColor.GRAY + ". " + ChatColor.AQUA + entry.getValue().getPlayerName() + ChatColor.GRAY + ": " + ChatColor.GREEN + entry.getValue().getThrown() + "\n");
            } else if (playerStat.getHitSelf() == entry.getKey()) {
                out.append(ChatColor.GOLD + "" + current + ChatColor.GRAY + ". " + ChatColor.AQUA + entry.getValue().getPlayerName() + ChatColor.GRAY + ": " + ChatColor.GREEN + entry.getValue().getHitOthers() + "\n");
            }
            current += 1;
        }
        return out.toString();
    }

    public static void showLeaderboard(Player player, String name) {
        HashMap<String, PlayerStats> stats = getPlayerStats();
        PlayerStats stat = null;
        if (name.equals("me")) {
            stat = stats.get(player.getName());
        } else {
            stat = stats.get(name);
        }
        if (stat == null) {
            player.sendMessage(ChatColor.RED + "Could not find any leaderboard statistics for " + name);
            return;
        }
        player.sendMessage(ChatColor.AQUA + stat.getPlayerName() + ChatColor.GRAY + "\n"
                + "Threw " + ChatColor.AQUA + stat.getThrown() + ChatColor.GRAY + " snowballs." + "\n"
                + "Hit others " + ChatColor.AQUA + stat.getHitOthers() + ChatColor.GRAY + " times.\n"
                + "Was hit " + ChatColor.AQUA + stat.getHitSelf() + ChatColor.GRAY + " times.\n"
                + "Hit/Miss Ratio: " + ChatColor.AQUA + round((float) stat.getHitOthers() / stat.getThrown()));
    }

    private static String round(float input) {
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        return df.format(input);
    }

    private static HashMap<String, PlayerStats> getPlayerStats() {
        PlayerStatsContainer.saveAll();
        HashMap<String, PlayerStats> stats = new HashMap<>();
        for (File file : Main.getPlayerDirectory().listFiles()) {
            if (!file.isDirectory()) {
                try {
                    PlayerStats playerStats = Main.getObjectMapper().readValue(file, PlayerStats.class);

                    stats.put(playerStats.getPlayerName(), playerStats);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        Main.getServerInstance().getLogger().info("loaded " + stats.size() + " stat files for leaderboard");
        return stats;
    }
}
