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

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

public class Database {

    private Connection connection;

    public Database(JavaPlugin plugin) {
        File dbFile = new File(plugin.getDataFolder(), "stats.db");
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS stats(" +
                "uuid TEXT PRIMARY KEY," +
                "blocksPlaced INTEGER DEFAULT 0," +
                "blocksBroken INTEGER DEFAULT 0," +
                "horseDistanceCm INTEGER DEFAULT 0," +
                "boatDistanceCm INTEGER DEFAULT 0" +
                ")"
            );
            try { stmt.executeUpdate("ALTER TABLE stats ADD COLUMN horseDistanceCm INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE stats ADD COLUMN boatDistanceCm INTEGER DEFAULT 0"); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int get(String uuid, String column) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT " + column + " FROM stats WHERE uuid=?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(column);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getLong(String uuid, String column) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT " + column + " FROM stats WHERE uuid=?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(column);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public void set(String uuid, String column, int value) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO stats(uuid,blocksPlaced,blocksBroken,horseDistanceCm,boatDistanceCm) VALUES(?,0,0,0,0) ON CONFLICT(uuid) DO NOTHING"
            );
            ps.setString(1, uuid);
            ps.executeUpdate();
            PreparedStatement upd = connection.prepareStatement("UPDATE stats SET " + column + "=? WHERE uuid=?");
            upd.setInt(1, value);
            upd.setString(2, uuid);
            upd.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addLong(String uuid, String column, long delta) {
        if (delta <= 0) return;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO stats(uuid,blocksPlaced,blocksBroken,horseDistanceCm,boatDistanceCm) VALUES(?,0,0,0,0) ON CONFLICT(uuid) DO NOTHING"
            );
            ps.setString(1, uuid);
            ps.executeUpdate();
            PreparedStatement upd = connection.prepareStatement("UPDATE stats SET " + column + "=" + column + "+? WHERE uuid=?");
            upd.setLong(1, delta);
            upd.setString(2, uuid);
            upd.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
