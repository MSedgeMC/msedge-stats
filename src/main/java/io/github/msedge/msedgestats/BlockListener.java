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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        Database db = MSedgeStats.getInstance().getDatabase();
        int current = db.get(uuid, "blocksPlaced");
        db.set(uuid, "blocksPlaced", current + 1);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        Database db = MSedgeStats.getInstance().getDatabase();
        int current = db.get(uuid, "blocksBroken");
        db.set(uuid, "blocksBroken", current + 1);
    }
}
