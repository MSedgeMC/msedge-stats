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

import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsListener implements Listener {

    static final Map<UUID, Long> pendingHorseCm = new ConcurrentHashMap<>();
    static final Map<UUID, Long> pendingBoatCm  = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        if (dx == 0 && dz == 0) return;

        long cm = Math.round(Math.sqrt(dx * dx + dz * dz) * 100);
        if (cm <= 0) return;

        Player player = event.getPlayer();
        org.bukkit.entity.Entity vehicle = player.getVehicle();
        if (vehicle == null) return;

        UUID uid = player.getUniqueId();

        if (vehicle instanceof Horse) {
            Horse horse = (Horse) vehicle;
            if (horse.isTamed() && horse.getInventory().getSaddle() != null) {
                pendingHorseCm.merge(uid, cm, Long::sum);
            }
        } else if (vehicle instanceof Boat) {
            pendingBoatCm.merge(uid, cm, Long::sum);
        }
    }

    private boolean isStatsGUI(String title) {
        return title != null && title.contains(" sᴛᴀᴛs");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (isStatsGUI(event.getView().getTitle())) event.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (isStatsGUI(event.getView().getTitle())) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (isStatsGUI(event.getView().getTitle())) {
            GUIRefreshTask.stop((Player) event.getPlayer());
        }
    }
}
