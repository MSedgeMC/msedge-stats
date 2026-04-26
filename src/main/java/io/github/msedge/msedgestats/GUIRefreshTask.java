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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIRefreshTask {

    private static final Map<UUID, BukkitTask>    tasks   = new ConcurrentHashMap<>();
    private static final Map<UUID, OfflinePlayer> targets = new ConcurrentHashMap<>();

    public static void start(Player viewer, OfflinePlayer target) {
        stop(viewer);
        targets.put(viewer.getUniqueId(), target);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
            MSedgeStats.getInstance(),
            () -> {
                Database db = MSedgeStats.getInstance().getDatabase();
                StatsListener.pendingHorseCm.forEach((uid, cm) -> {
                    StatsListener.pendingHorseCm.remove(uid);
                    db.addLong(uid.toString(), "horseDistanceCm", cm);
                });
                StatsListener.pendingBoatCm.forEach((uid, cm) -> {
                    StatsListener.pendingBoatCm.remove(uid);
                    db.addLong(uid.toString(), "boatDistanceCm", cm);
                });

                Player v = Bukkit.getPlayer(viewer.getUniqueId());
                if (v == null || v.getOpenInventory() == null) { stop(viewer); return; }
                String title = v.getOpenInventory().getTitle();
                if (title == null || !title.contains(" sᴛᴀᴛs")) { stop(viewer); return; }

                OfflinePlayer tgt = targets.get(viewer.getUniqueId());
                if (tgt != null) new StatsGUI().refresh(v, tgt, v.getOpenInventory().getTopInventory());
            },
            20L, 20L
        );

        tasks.put(viewer.getUniqueId(), task);
    }

    public static void stop(Player viewer) {
        BukkitTask task = tasks.remove(viewer.getUniqueId());
        if (task != null) task.cancel();
        targets.remove(viewer.getUniqueId());
    }
}
