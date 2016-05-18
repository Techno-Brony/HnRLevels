package com.weebly.openboxtechnologies.hnrlevels;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by zhiyuanqi on 11/05/16.
 */

public class Listener implements org.bukkit.event.Listener {

    private JavaPlugin plugin;

    public Listener(JavaPlugin plugin) {

        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

                UUID playerid = e.getPlayer().getUniqueId();
                ResultSet result;
                boolean exists = false;
                try {
                    result = Main.statement.executeQuery("SELECT COUNT(UUID)" + " FROM Levels" +
                            " WHERE UUID ='"+ playerid.toString() +"';");
                    result.next();
                    exists = result.getInt(1) > 0;
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    return;
                }

                try {
                    if (!exists) {
                        Main.statement.executeUpdate("INSERT INTO Levels (UUID,XP,LEVEL) VALUES ('" +
                                playerid.toString() + "',0,0);");
                    }

                    result = Main.statement.executeQuery("SELECT * FROM Levels WHERE UUID ='"+ playerid.toString() +"';");
                    result.next();

                    Main.playerXPArray.put(playerid.toString(), new int[] { result.getInt("XP"), result.getInt("LEVEL")});

                } catch (SQLException exception) {
                    exception.printStackTrace();
                    return;
                }

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                Main.updateXPBar(e.getPlayer());
            }
        };
        r.runTaskLater(plugin, 2);
    }

    @EventHandler
    public void onExpDrop (PlayerExpChangeEvent e) {
        e.setAmount(0);
    }

    @EventHandler
    public void onRespawn (PlayerRespawnEvent e) {
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                Main.updateXPBar(e.getPlayer());
            }
        };
        r.runTaskLater(plugin, 2);

    }
}
