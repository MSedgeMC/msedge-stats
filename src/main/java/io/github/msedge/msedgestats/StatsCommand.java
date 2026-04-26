/*
 * MSedge Stats — A Minecraft plugin for player statistics.
 * Copyright (C) 2025 MSedge (https://github.com/msedge)
 *
 * This project is licensed under the MIT License.
 * You are free to use, modify, and distribute this software
 * with proper attribution to the original author.
 *
 * https://github.com/msedge/msedge-stats
 */
package io.github.msedge.msedgestats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            // No argument — show own stats
            new StatsGUI().open(player, player);
            return true;
        }

        // Case-insensitive lookup: search all offline players who have joined before
        String inputName = args[0].toLowerCase();
        OfflinePlayer target = null;

        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (op.getName() != null && op.getName().toLowerCase().equals(inputName)) {
                target = op;
                break;
            }
        }

        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            // Player not found — open GUI in "not found" mode so the status dot shows red
            new StatsGUI().openNotFound(player, args[0]);
            return true;
        }

        new StatsGUI().open(player, target);
        return true;
    }
}
