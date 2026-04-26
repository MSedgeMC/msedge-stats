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

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.UUID;

public class StatsGUI {

    private static final char[]   NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final String[] SMALL_CAPS   = {
        "ᴀ","ʙ","ᴄ","ᴅ","ᴇ","ꜰ","ɢ","ʜ","ɪ","ᴊ","ᴋ","ʟ","ᴍ","ɴ","ᴏ","ᴘ","ǫ","ʀ","s","ᴛ","ᴜ","ᴠ","ᴡ","x","ʏ","ᴢ",
        "ᴀ","ʙ","ᴄ","ᴅ","ᴇ","ꜰ","ɢ","ʜ","ɪ","ᴊ","ᴋ","ʟ","ᴍ","ɴ","ᴏ","ᴘ","ǫ","ʀ","s","ᴛ","ᴜ","ᴠ","ᴡ","x","ʏ","ᴢ"
    };

    private static String sc(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            boolean found = false;
            for (int i = 0; i < NORMAL_CHARS.length; i++) {
                if (NORMAL_CHARS[i] == c) { sb.append(SMALL_CAPS[i]); found = true; break; }
            }
            if (!found) sb.append(c);
        }
        return sb.toString();
    }

    private static final String SILVER = ChatColor.GRAY.toString();
    private static final String RED    = ChatColor.RED.toString();
    private static final String WHITE  = ChatColor.WHITE.toString();
    private static final String GREEN  = ChatColor.GREEN.toString();
    private static final String GRAY   = ChatColor.GRAY.toString();
    private static final String ITALIC = ChatColor.ITALIC.toString();
    private static final String D_GRAY = ChatColor.DARK_GRAY.toString();

    public void open(Player viewer, OfflinePlayer target) {
        String guiTitle = ChatColor.DARK_GRAY + sc(target.getName() + " Stats");
        Inventory inv   = Bukkit.createInventory(null, 36, guiTitle);
        populate(inv, target);
        viewer.openInventory(inv);
        GUIRefreshTask.start(viewer, target);
    }

    public void refresh(Player viewer, OfflinePlayer target, Inventory inv) {
        populate(inv, target);
        viewer.updateInventory();
    }

    public void openNotFound(Player viewer, String searchedName) {
        String guiTitle = ChatColor.DARK_GRAY + sc(searchedName + " Stats");
        Inventory inv   = Bukkit.createInventory(null, 36, guiTitle);
        ItemStack notFound = new ItemStack(Material.GRAY_DYE);
        ItemMeta  nfMeta   = notFound.getItemMeta();
        if (nfMeta != null) {
            nfMeta.setDisplayName(RED + " ● " + WHITE + "Player Not Found");
            nfMeta.setLore(Collections.singletonList(D_GRAY + "ℹ " + ITALIC + SILVER + "This player has never joined the server."));
            notFound.setItemMeta(nfMeta);
        }
        inv.setItem(4, notFound);
        viewer.openInventory(inv);
    }

    private void populate(Inventory inv, OfflinePlayer target) {
        String uuid = target.getUniqueId().toString();
        Database db = MSedgeStats.getInstance().getDatabase();

        int  kills       = 0;
        int  deaths      = 0;
        long playMinutes = 0;
        int  mobKills    = 0;
        int  bPlaced     = db.get(uuid, "blocksPlaced");
        int  bBroken     = db.get(uuid, "blocksBroken");
        long distanceCm  = 0;

        if (target.hasPlayedBefore() || target.isOnline()) {
            kills       = target.getStatistic(Statistic.PLAYER_KILLS);
            deaths      = target.getStatistic(Statistic.DEATHS);
            playMinutes = target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
            mobKills    = target.getStatistic(Statistic.MOB_KILLS);
            distanceCm  = (long) target.getStatistic(Statistic.WALK_ONE_CM)
                        + (long) target.getStatistic(Statistic.SPRINT_ONE_CM)
                        + (long) target.getStatistic(Statistic.CROUCH_ONE_CM)
                        + (long) target.getStatistic(Statistic.SWIM_ONE_CM)
                        + (long) target.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM)
                        + (long) target.getStatistic(Statistic.WALK_ON_WATER_ONE_CM)
                        + (long) target.getStatistic(Statistic.CLIMB_ONE_CM)
                        + (long) target.getStatistic(Statistic.FLY_ONE_CM)
                        + (long) target.getStatistic(Statistic.AVIATE_ONE_CM)
                        + (long) target.getStatistic(Statistic.MINECART_ONE_CM)
                        + (long) target.getStatistic(Statistic.PIG_ONE_CM)
                        + (long) target.getStatistic(Statistic.STRIDER_ONE_CM)
                        + db.getLong(uuid, "horseDistanceCm")
                        + db.getLong(uuid, "boatDistanceCm");
        }

        double kd      = (deaths == 0) ? kills : (double) kills / deaths;
        String kdStr   = String.format("%.2f", kd);
        String rank    = getRank(target.getUniqueId());
        boolean online = target.isOnline();

        ItemStack rankHead  = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) rankHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(target);
            skullMeta.setDisplayName(RED + sc("Rank"));
            skullMeta.setLore(Collections.singletonList(SILVER + rank));
            rankHead.setItemMeta(skullMeta);
        }
        applySkinRestorerSkin(rankHead, target);
        inv.setItem(0, rankHead);

        inv.setItem(2, makeItem(Material.COMPASS, RED + sc("Distance Traveled"), SILVER + String.format("%,d blocks", distanceCm / 100)));

        Material statusMat = online ? Material.LIME_DYE : Material.GRAY_DYE;
        String   dotColor  = online ? GREEN : GRAY;
        String   statusTxt = online ? "Online" : "Offline";

        long firstPlayedMs = target.getFirstPlayed();
        String firstJoinedLine;
        if (firstPlayedMs > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy");
            firstJoinedLine = SILVER + "🕐 " + WHITE + sdf.format(new java.util.Date(firstPlayedMs));
        } else {
            firstJoinedLine = SILVER + "🕐 " + WHITE + "Unknown";
        }

        ItemStack statusItem = new ItemStack(statusMat);
        ItemMeta  statusMeta = statusItem.getItemMeta();
        if (statusMeta != null) {
            statusMeta.setDisplayName(dotColor + "● " + WHITE + statusTxt);
            statusMeta.setLore(Collections.singletonList(firstJoinedLine));
            statusItem.setItemMeta(statusMeta);
        }
        inv.setItem(4, statusItem);

        inv.setItem(6, makeItem(Material.CLOCK, RED + sc("Playtime"), SILVER + formatPlaytime(playMinutes)));

        int ping = online ? ((Player) target).getPing() : 0;
        inv.setItem(8, makeItem(Material.RECOVERY_COMPASS, RED + sc("Ping"), SILVER + (online ? ping + "ms" : "N/A")));

        inv.setItem(19, makeCleanSword(kills));
        inv.setItem(20, makeItem(Material.SKELETON_SKULL, RED + sc("Deaths"), SILVER + deaths));
        inv.setItem(21, makeItem(Material.FEATHER, RED + sc("K/D Ratio"), SILVER + kdStr));
        inv.setItem(22, makeItem(Material.BELL, RED + sc("Kill Streak"), SILVER + getKillStreak(target)));
        inv.setItem(23, makeItem(Material.ZOMBIE_HEAD, RED + sc("Mobs Killed"), SILVER + mobKills));
        inv.setItem(24, makeItem(Material.STONE, RED + sc("Blocks Placed"), SILVER + bPlaced));
        inv.setItem(25, makeItem(Material.COBBLESTONE, RED + sc("Blocks Broken"), SILVER + bBroken));
    }

    private ItemStack makeCleanSword(int kills) {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta  meta  = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(RED + sc("Kills"));
            meta.setLore(Collections.singletonList(SILVER + kills));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "attack_damage", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
            meta.addAttributeModifier(Attribute.ATTACK_SPEED,  new AttributeModifier(UUID.randomUUID(), "attack_speed",  0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
            sword.setItemMeta(meta);
        }
        return sword;
    }

    private ItemStack makeItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getKillStreak(OfflinePlayer target) {
        try {
            if (Bukkit.getPluginManager().getPlugin("AzKillStreak") == null) return 0;
            if (!target.isOnline()) return 0;
            Class<?> apiClass = null;
            for (String name : new String[]{"fr.azkillstreak.api.AzKillStreakAPI","dev.azzurite.killstreak.api.KillStreakAPI","com.azuriom.plugin.killstreak.api.KillStreakAPI"}) {
                try { apiClass = Class.forName(name); break; } catch (ClassNotFoundException ignored) {}
            }
            if (apiClass == null) return 0;
            Object api    = apiClass.getMethod("getInstance").invoke(null);
            Object streak = apiClass.getMethod("getKillStreak", Player.class).invoke(api, (Player) target);
            if (streak instanceof Number) return ((Number) streak).intValue();
        } catch (Exception ignored) {}
        return 0;
    }

    private void applySkinRestorerSkin(ItemStack head, OfflinePlayer target) {
        try {
            if (Bukkit.getPluginManager().getPlugin("SkinsRestorer") == null) return;
            Class<?> apiClass = Class.forName("net.skinsrestorer.api.SkinsRestorerAPI");
            Object   api      = apiClass.getMethod("getApi").invoke(null);
            String skinName   = (String) apiClass.getMethod("getSkinName", String.class).invoke(api, target.getName());
            if (skinName == null) skinName = target.getName();
            Object skinData = apiClass.getMethod("getSkinData", String.class).invoke(api, skinName);
            if (skinData == null) return;
            String value  = (String) skinData.getClass().getMethod("getValue").invoke(skinData);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) return;
            org.bukkit.profile.PlayerProfile  profile  = Bukkit.createPlayerProfile(target.getUniqueId(), target.getName());
            org.bukkit.profile.PlayerTextures textures = profile.getTextures();
            String decoded = new String(java.util.Base64.getDecoder().decode(value));
            int urlStart   = decoded.indexOf("\"url\":\"") + 7;
            int urlEnd     = decoded.indexOf("\"", urlStart);
            if (urlStart > 6 && urlEnd > urlStart) {
                java.net.URL skinUrl = new java.net.URL(decoded.substring(urlStart, urlEnd));
                textures.setSkin(skinUrl);
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
                head.setItemMeta(meta);
            }
        } catch (Exception ignored) {}
    }

    private String formatPlaytime(long totalMinutes) {
        long hours   = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    private String getRank(UUID playerUUID) {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user    = lp.getUserManager().loadUser(playerUUID).join();
            if (user != null) {
                String group = user.getPrimaryGroup();
                if (group == null || group.equalsIgnoreCase("default")) return "Player";
                return group.substring(0, 1).toUpperCase() + group.substring(1);
            }
        } catch (Exception ignored) {}
        return "Player";
    }
}
