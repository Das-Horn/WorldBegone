package org.example;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Level;


public class DeathServer extends JavaPlugin implements Listener {
    private BukkitTask task;
    private Boolean active = false;

    private JSONObject playerStats;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Code To initialize player statistics

        try {
            File myObj = new File("playerStats.json");
            Scanner myReader = new Scanner(myObj);
            StringBuilder data = new StringBuilder();
            while (myReader.hasNextLine()) {
                data.append(myReader.nextLine());
            }
            getServer().getLogger().log(Level.INFO, data.toString());
            JSONParser parser = new JSONParser();
            playerStats = (JSONObject) parser.parse(data.toString());

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveJson();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        World w = getServer().getWorld("world");

        assert w != null;
        event.getPlayer().sendMessage(Component.text("Welcome to hell " + (this.active ? "0" : Math.floor(w.getFullTime() / 23775.0)) + " days since last incident.." + (this.active ? ":(" : ":)")));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.active) return;

        this.active = true;

        // Player Death Statistics update
        JSONArray playerList = (JSONArray) this.playerStats.get("players");

        boolean inList = true;

        for (Object i: playerList) {
            JSONObject j = (JSONObject) i;
            getServer().getLogger().log(Level.INFO, "[DEATH SERVER] Current Player @ " + event.getPlayer().getUniqueId());
            if( j.get("uuid").toString().equals(event.getPlayer().getUniqueId().toString())) {
                getServer().getLogger().log(Level.INFO, "[DEATH SERVER] Updating player Death Counter for " + event.getPlayer().getUniqueId());
                j.put("deaths", (long) j.get("deaths") + 1);
                inList = false;
            }
        }

        if(inList) {
            getServer().getLogger().log(Level.INFO, "[DEATH SERVER] Generating Config");
            JSONParser parser = new JSONParser();
            String def = "{\"uuid\" : \""+ event.getPlayer().getUniqueId() +"\", \"deaths\" : 1 }";
            try {
                JSONObject player = (JSONObject) parser.parse(def);
                playerList.add(player);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        this.playerStats.replace("worldResets", ((long) this.playerStats.get("worldResets") + 1));
        this.playerStats.replace("players", playerList);

        saveJson(); // Save State before server closes

        String playerName = event.getPlayer().getName();
        String finalMessage = playerName + " has died";
        BossBar finalMsg =  Bukkit.createBossBar(finalMessage, BarColor.PINK, BarStyle.SOLID, BarFlag.DARKEN_SKY);

        // Get all connected players and add boss bar

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();  // TODO : Check if this still works in game :)

        for (Player i:players) {
            finalMsg.addPlayer(i);
        }
        finalMsg.setVisible(true);
        final double[] progress = {1.0};


        task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            progress[0] = progress[0] - 0.01666666666;
            // Task Disabler
            if(progress[0] <= 0.0) {
                task.cancel();
                return;
            }

            finalMsg.setProgress(progress[0]);
        }, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(this, () -> {

            String str = "1";
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("./state.conf"));
                writer.write(str);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            // Process to delete world dirs
            ProcessBuilder ss = new ProcessBuilder("/home/cd/minecraft/stop.sh");
            try {
                Process t = ss.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }, 1200L);
    }

    private void saveJson() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./playerStats.json"));
            writer.write(this.playerStats.toJSONString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
