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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

public class MSedgeStats extends JavaPlugin {

    private static final String DISPLAY_NAME = "MSedge Stats";

    private static MSedgeStats instance;
    private Database database;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        rerouteDataFolder();
        printSplash();

        database = new Database(this);
        logStep("Database", "SQLite ready at " + getDataFolder().getName() + "/stats.db");

        getCommand("stats").setExecutor(new StatsCommand());
        logStep("Commands", "/stats and /msedge:stats registered");

        getServer().getPluginManager().registerEvents(new StatsListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        logStep("Listeners", "StatsListener, BlockListener hooked");

        renameForDisplay();

        long elapsed = System.currentTimeMillis() - start;
        printFooter(true, elapsed);
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender c = Bukkit.getConsoleSender();
        c.sendMessage("");
        c.sendMessage(ChatColor.DARK_GRAY + "  ╭" + line(56) + "╮");
        c.sendMessage(ChatColor.DARK_GRAY + "  │ "
                + ChatColor.RED + "✦ " + ChatColor.BOLD + DISPLAY_NAME + ChatColor.RESET
                + ChatColor.GRAY + " has been " + ChatColor.RED + "disabled"
                + ChatColor.GRAY + ". Goodbye." + pad(11)
                + ChatColor.DARK_GRAY + " │");
        c.sendMessage(ChatColor.DARK_GRAY + "  ╰" + line(56) + "╯");
        c.sendMessage("");
    }

    private void rerouteDataFolder() {
        try {
            File current = getDataFolder();
            File target = new File(current.getParentFile(), DISPLAY_NAME);
            if (!target.equals(current)) {
                Field f = JavaPlugin.class.getDeclaredField("dataFolder");
                f.setAccessible(true);
                f.set(this, target);
            }
            if (!target.exists()) {
                target.mkdirs();
            }
        } catch (Exception e) {
            getLogger().warning("Could not reroute data folder: " + e.getMessage());
        }
    }

    private void renameForDisplay() {
        try {
            PluginDescriptionFile desc = getDescription();
            Field nameField = PluginDescriptionFile.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(desc, DISPLAY_NAME);
            try {
                Field rawNameField = PluginDescriptionFile.class.getDeclaredField("rawName");
                rawNameField.setAccessible(true);
                rawNameField.set(desc, DISPLAY_NAME);
            } catch (NoSuchFieldException ignored) {}
        } catch (Exception e) {
            getLogger().warning("Could not rename plugin for display: " + e.getMessage());
        }
    }

    private void printSplash() {
        ConsoleCommandSender c = Bukkit.getConsoleSender();
        PluginDescriptionFile desc = getDescription();

        String[] logo = {
                "  ███╗   ███╗ ███████╗ ███████╗ ██████╗   ██████╗  ███████╗",
                "  ████╗ ████║ ██╔════╝ ██╔════╝ ██╔══██╗ ██╔════╝  ██╔════╝",
                "  ██╔████╔██║ ███████╗ █████╗   ██║  ██║ ██║  ███╗ █████╗  ",
                "  ██║╚██╔╝██║ ╚════██║ ██╔══╝   ██║  ██║ ██║   ██║ ██╔══╝  ",
                "  ██║ ╚═╝ ██║ ███████║ ███████╗ ██████╔╝ ╚██████╔╝ ███████╗",
                "  ╚═╝     ╚═╝ ╚══════╝ ╚══════╝ ╚═════╝   ╚═════╝  ╚══════╝"
        };

        ChatColor[] gradient = {
                ChatColor.LIGHT_PURPLE,
                ChatColor.LIGHT_PURPLE,
                ChatColor.AQUA,
                ChatColor.AQUA,
                ChatColor.BLUE,
                ChatColor.BLUE
        };

        c.sendMessage("");
        c.sendMessage(ChatColor.DARK_GRAY + "  ╭" + line(60) + "╮");
        for (int i = 0; i < logo.length; i++) {
            c.sendMessage(ChatColor.DARK_GRAY + "  │ "
                    + ChatColor.BOLD + gradient[i] + logo[i] + ChatColor.RESET
                    + ChatColor.DARK_GRAY + " │");
        }
        c.sendMessage(ChatColor.DARK_GRAY + "  │" + pad(60) + "│");

        String tagline = ChatColor.GRAY + "" + ChatColor.ITALIC
                + "Premium player statistics  ·  crafted by " + ChatColor.WHITE + "MSedge";
        c.sendMessage(ChatColor.DARK_GRAY + "  │  " + center(tagline, 56) + ChatColor.DARK_GRAY + "  │");

        c.sendMessage(ChatColor.DARK_GRAY + "  │" + pad(60) + "│");

        String pill = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " PREMIUM " + ChatColor.RESET + ChatColor.DARK_GRAY + "]";
        String version = ChatColor.GRAY + "v" + ChatColor.WHITE + desc.getVersion();
        String api = ChatColor.GRAY + "API " + ChatColor.WHITE + desc.getAPIVersion();
        String author = ChatColor.GRAY + "by " + ChatColor.WHITE + String.join(", ", desc.getAuthors());

        String meta = pill + "  " + version + ChatColor.DARK_GRAY + " · " + api + ChatColor.DARK_GRAY + " · " + author;
        c.sendMessage(ChatColor.DARK_GRAY + "  │  " + center(meta, 56) + ChatColor.DARK_GRAY + "  │");

        c.sendMessage(ChatColor.DARK_GRAY + "  ╰" + line(60) + "╯");
        c.sendMessage("");
        c.sendMessage(ChatColor.DARK_GRAY + "  " + ChatColor.LIGHT_PURPLE + "▸ " + ChatColor.GRAY + "Booting modules" + ChatColor.DARK_GRAY + "...");
    }

    private void logStep(String label, String detail) {
        ConsoleCommandSender c = Bukkit.getConsoleSender();
        c.sendMessage("    " + ChatColor.GREEN + "✔ "
                + ChatColor.WHITE + ChatColor.BOLD + label + ChatColor.RESET
                + ChatColor.DARK_GRAY + "  —  " + ChatColor.GRAY + detail);
    }

    private void printFooter(boolean ok, long elapsed) {
        ConsoleCommandSender c = Bukkit.getConsoleSender();
        c.sendMessage("");
        c.sendMessage(ChatColor.DARK_GRAY + "  ╭" + line(60) + "╮");
        String status = (ok ? ChatColor.GREEN + "● ENABLED" : ChatColor.RED + "● FAILED");
        String time = ChatColor.GRAY + "ready in " + ChatColor.WHITE + elapsed + "ms";
        String body = status + ChatColor.DARK_GRAY + "   ·   " + time;
        c.sendMessage(ChatColor.DARK_GRAY + "  │  " + center(body, 56) + ChatColor.DARK_GRAY + "  │");
        c.sendMessage(ChatColor.DARK_GRAY + "  ╰" + line(60) + "╯");
        c.sendMessage("");
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('─');
        return sb.toString();
    }

    private static String pad(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }

    private static String center(String text, int width) {
        int visible = ChatColor.stripColor(text).length();
        int total = Math.max(0, width - visible);
        int left = total / 2;
        int right = total - left;
        return pad(left) + text + ChatColor.RESET + pad(right);
    }

    public static MSedgeStats getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }
}
