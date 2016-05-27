package com.weebly.openboxtechnologies.hnrlevels;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class hnrlevels extends JavaPlugin {

    private Connection connection;
    private String host, database, username, password;
    private int port;

    static Statement statement;
    static HashMap<String, int[]> playerXPArray = new HashMap<>();
    private static ArrayList<Integer> nextLevelXP = new ArrayList<>();

    @Override
    public void onEnable() {
        createConfig();

        host = getConfig().get("sql.host").toString();
        database = getConfig().get("sql.database").toString();
        username = getConfig().get("sql.username").toString();
        password = getConfig().get("sql.password").toString();
        port = (int) getConfig().get("sql.port");

        try {
            openConnection();
            statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Levels(UUID varchar(36), XP long, Level int);");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        new Listener(this);

        int highestLevel = (int) getConfig().get("levelxp.highestlevel");
        for (int i = 0; i < highestLevel; i++) {
            nextLevelXP.add(i, (int) getConfig().get("levelxp." + i));
        }
    }

    @Override
    public void onDisable() {
        for (String key : playerXPArray.keySet()) {
            try {
                hnrlevels.statement.executeUpdate("UPDATE Levels SET XP = " + playerXPArray.get(key)[0]
                        + ", LEVEL = '" + playerXPArray.get(key)[1] + "' WHERE UUID = '" + key + "';");
            } catch (SQLException exception ){
                exception.printStackTrace();
            }

        }
    }

    private String prefix = ChatColor.translateAlternateColorCodes('&', "&e&lLevel&7&l> &9");
    private String levelprefix = ChatColor.BOLD + "" + ChatColor.GOLD;

    @EventHandler
    public boolean onCommand(CommandSender e, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("level")) {
            if (args.length == 0) {
                if (e instanceof Player) {
                    int nextLevel = getLevel(((Player) e).getUniqueId()) + 1;
                    e.sendMessage(ChatColor.GREEN + "============================");
                    e.sendMessage(levelprefix + "Current Level> " + ChatColor.BLUE + getLevel(((Player) e).getUniqueId()));
                    if (getLevel(((Player) e).getUniqueId()) >= (int) getConfig().get("levelxp.highestlevel")) {
                        e.sendMessage(levelprefix + "Next Level> " + ChatColor.LIGHT_PURPLE + "Maximum Level!");
                        e.sendMessage(levelprefix + "XP> " + ChatColor.LIGHT_PURPLE + "Maximum Level!");
                    } else {
                        e.sendMessage(levelprefix + "Next Level> " + ChatColor.BLUE + nextLevel);
                        e.sendMessage(levelprefix + "XP> " + ChatColor.RED + getXP(((Player) e).getUniqueId())
                                + ChatColor.GRAY + " / " + ChatColor.BLUE + nextLevelXP.get(((Player) e).getLevel()));
                    }
                    e.sendMessage(ChatColor.GREEN + "============================");
                } else {
                    e.sendMessage(prefix + "This can only be run by a player!");
                    return true;
                }
            } else if (args.length == 1) {
                if (!e.hasPermission("level.admin")) {
                    e.sendMessage(prefix + "To use this feature, you must be rank "+ ChatColor.GRAY +
                            "(" + ChatColor.RED + "ADMIN" + ChatColor.GRAY + ")" + ChatColor.BLUE + " !");
                    return true;
                }
                e.sendMessage(prefix + "Please specify a player and amount of xp!");
                return true;
            } else if (args.length == 2) {
                if (!e.hasPermission("level.admin")) {
                    e.sendMessage(prefix + "To use this feature, you must be rank "+ ChatColor.GRAY +
                            "(" + ChatColor.RED + "ADMIN" + ChatColor.GRAY + ")" + ChatColor.BLUE + " !");
                    return true;
                }
                e.sendMessage(prefix + "Please specify amount of xp!");
                return true;
            } else if (args.length >= 3) {
                if (!e.hasPermission("level.admin")) {
                    e.sendMessage(prefix + "To use this feature, you must be rank "+ ChatColor.GRAY +
                            "(" + ChatColor.RED + "ADMIN" + ChatColor.GRAY + ")" + ChatColor.BLUE + " !");
                    return true;
                }
                if (args[0].equalsIgnoreCase("givexp")) {
                    if(getServer().getPlayer(args[1]) == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    UUID playerid = getServer().getPlayer(args[1]).getUniqueId();
                    if (playerid == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    if (!isInteger(args[2])) {
                        e.sendMessage(prefix + "Please type a number!");
                        return true;
                    }

                    setXP(playerid, Integer.parseInt(args[2]) + getXP(playerid), false);
                    if (getXP(playerid) == 0) {
                        if (Integer.parseInt(args[2]) == 0) {
                            e.sendMessage(prefix + "You gave " + ChatColor.RED + getServer().getPlayer(playerid).getDisplayName() + " " + ChatColor.AQUA
                            + args[2] + " XP " + ChatColor.GRAY + "!");
                        } else {
                            e.sendMessage(prefix + "You leveled up " + ChatColor.AQUA + args[1] + ChatColor.BLUE + " !");
                        }
                        return true;
                    }
                    e.sendMessage(prefix + "You gave " + ChatColor.RED + getServer().getPlayer(playerid).getDisplayName() + " " + ChatColor.AQUA
                            + args[2] + " XP " + ChatColor.GRAY + "!");

                } else if (args[0].equalsIgnoreCase("setxp")) {
                    if(getServer().getPlayer(args[1]) == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    UUID playerid = getServer().getPlayer(args[1]).getUniqueId();
                    if (playerid == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    if (!isInteger(args[2])) {
                        e.sendMessage(prefix + "Please type a number!");
                        return true;
                    }

                    setXP(playerid, Integer.parseInt(args[2]), false);

                    if (getXP(playerid) == 0) {
                        if (Integer.parseInt(args[2]) == 0) {
                            e.sendMessage(prefix + "You set " + ChatColor.RED + getServer().getPlayer(playerid).getDisplayName() + ChatColor.BLUE +
                                    "'s XP to " + ChatColor.AQUA + args[2] + ChatColor.GRAY + " !");
                            return true;
                        } else {
                            e.sendMessage(prefix + "You leveled up " + ChatColor.AQUA + args[1] + ChatColor.BLUE + " !");
                            return true;
                        }
                    }

                    e.sendMessage(prefix + "You set " + ChatColor.RED + getServer().getPlayer(playerid).getDisplayName() + ChatColor.BLUE +
                            "'s XP to " + ChatColor.AQUA + args[2] + ChatColor.GRAY + " !");

                } else {
                    e.sendMessage(prefix + "Invalid arguments!");
                }
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("levelup")) {
            if (e.hasPermission("level.admin")) {
                if (args.length == 0) {
                    e.sendMessage(prefix + "Please specify a player!");
                    return true;
                } else {
                    if(getServer().getPlayer(args[0]) == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    UUID playerid = getServer().getPlayer(args[0]).getUniqueId();
                    if (playerid == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }
                    if (!setLevel(playerid, getLevel(playerid) + 1, true)) {
                        e.sendMessage(prefix + "The player is at the maximum level!");
                        return true;
                    }
                    e.sendMessage(prefix + "You leveled up " + ChatColor.AQUA + args[0] + ChatColor.BLUE + " !");
                }
            } else {
                e.sendMessage(prefix + "To use this feature, you must be rank "+ ChatColor.GRAY +
                        "(" + ChatColor.RED + "ADMIN" + ChatColor.GRAY + ")" + ChatColor.BLUE + " !");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("leveldown")) {
            if (e.hasPermission("level.admin")) {
                if (args.length == 0) {
                    e.sendMessage(prefix + "Please specify a player!");
                    return true;
                } else {
                    if(getServer().getPlayer(args[0]) == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    UUID playerid = getServer().getPlayer(args[0]).getUniqueId();
                    if (playerid == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }
                    if (!setLevel(playerid, getLevel(playerid) - 1, false)) {
                        e.sendMessage(prefix + "The player is at the minimum level!");
                        return true;
                    }
                    e.sendMessage(prefix + "You leveled down " + ChatColor.AQUA + args[0] + ChatColor.BLUE + " !");
                }
            } else {
                e.sendMessage(prefix + "To use this feature, you must be rank "+ ChatColor.GRAY +
                        "(" + ChatColor.RED + "ADMIN" + ChatColor.GRAY + ")" + ChatColor.BLUE + " !");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("setlevel")) {
            if (e.hasPermission("level.admin")) {
                if (args.length == 0) {
                    e.sendMessage(prefix + "Please specify a player and amount of levels!");
                    return true;
                } else if (args.length == 1) {
                    e.sendMessage(prefix + "Please specify amount of levels!");
                    return true;
                } else {
                    if (!isInteger(args[1])) {
                        e.sendMessage(prefix + "Please type a number!");
                        return true;
                    }

                    if(getServer().getPlayer(args[0]) == null) {
                        e.sendMessage(prefix + "The player was not found!");
                        return true;
                    }

                    UUID playerid = getServer().getPlayer(args[0]).getUniqueId();
                    if (playerid == null) {
                        e.sendMessage(prefix + "The player was not found!");
                    }
                    if (!setLevel(playerid, Integer.parseInt(args[1]), false)) {
                        e.sendMessage(prefix + "That is an invalid level!");
                        return true;
                    }
                    e.sendMessage(prefix + "You set " + ChatColor.AQUA + args[0] + ChatColor.BLUE + "'s level to " +
                           ChatColor.RED + args[1] + ChatColor.BLUE +" !");
                }
            } else {
                e.sendMessage(prefix + "To use this feature, you must be rank "+ ChatColor.GRAY +
                        "(" + ChatColor.RED + "ADMIN" + ChatColor.GRAY + ")" + ChatColor.BLUE + " !");
            }
            return true;
        }
        return false;
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            String test = "jdbc:mysql://" + host+ ":" + port + "/" + database + username + password;
            getLogger().info(test);
            connection = DriverManager.getConnection("jdbc:mysql://" + host+ ":" + port + "/" + database, username, password);
        }
    }

    public static void updateXPBar(Player player) {
        double percent = (double) playerXPArray.get(player.getUniqueId().toString())[0] /
                nextLevelXP.get(playerXPArray.get(player.getUniqueId().toString())[1]);

        player.setExp((float) percent);
        player.setLevel(playerXPArray.get(player.getUniqueId().toString())[1]);

    }

    public boolean setLevel(UUID playerid, int level, boolean notifyPlayer) {
        if (level > (int) getConfig().get("levelxp.highestlevel")) {
            return false;
        }

        if (level < 0) {
            return false;
        }

        playerXPArray.put(playerid.toString(), new int[] {0, level});
        updateXPBar(getServer().getPlayer(playerid));

        if (notifyPlayer) {
            getServer().getPlayer(playerid).sendMessage(prefix + "You leveled up to " +
                    ChatColor.AQUA + level + ChatColor.BLUE + " !");
        }

        return true;
    }

    public int getLevel(UUID playerid) {
        return playerXPArray.get(playerid.toString())[1];
    }

    public boolean setXP(UUID playerid, int xp, boolean notifyPlayer) {
        if (xp < 0) {
            return false;
        }
        playerXPArray.put(playerid.toString(), new int[] {xp, getLevel(playerid)});

        if (xp > nextLevelXP.get(getLevel(playerid)) - 1) {
            setLevel(playerid, getLevel(playerid) + 1, notifyPlayer);
        } else {
            updateXPBar(getServer().getPlayer(playerid));
        }

        return true;
    }

    public int getXP(UUID playerid) {
        return playerXPArray.get(playerid.toString())[0];
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public int getAmountOfLevels() {
        return nextLevelXP.size();
    }
}
