package com.quantumsrealm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static org.bukkit.Bukkit.*;

public class KDRStats extends JavaPlugin implements Listener {
  String neverLoggedIn = getConfig().getString("messages.neverLoggedIn");
  
  String notRecorded = getConfig().getString("messages.notRecorded");
  
  String usage = getConfig().getString("messages.usage");
  
  String noPermission = getConfig().getString("messages.noPermission");
  
  public void onEnable() {
    getLogger().info("Loading config...");
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();
    getLogger().info("Registering events...");
    Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin)this);
    if (getConfig().getInt("config-version") != 1)
      getLogger().warning("Please use config version 1 (supplied in resource page)."); 
    getLogger().info("Loaded succesfully!");
  }
  
  public float kdr(int kills, int deaths) {
    if (kills == 0) {
      if (deaths == 0)
        return 0.0F; 
      return -deaths;
    } 
    if (deaths == 0) {
      if (kills == 0)
        return 0.0F; 
      return kills;
    } 
    return (kills / deaths);
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("stats")) {
      Player p = (Player)sender;

      if (args.length == 1) {
        Player playerCheck = getPlayerExact(args[0]);
        //check to see if player is online

        if (playerCheck == null){
          //Playername used when displaying stats in p.sendMessage Ln71
          String offlinePlayer = getServer().getOfflinePlayer(args[0]).getName();
          //Get a uuid of offline player
          String uuid = getOfflinePlayer(args[0]).getUniqueId().toString();
          //if to check if user has any data in config yet, display never logged in message if not
          if (!getConfig().contains("players." + uuid)) {
            p.sendMessage(color(this.neverLoggedIn.replaceAll("%displayname%", offlinePlayer)));
            return true;
          //display /kdr <playername>'s stats if they exist
          }
          int kills = getConfig().getInt("players." + uuid + ".kills");
          int deaths = getConfig().getInt("players." + uuid + ".deaths");
          float kdr = kdr(kills, deaths);
          if (deaths > kills){kdr=0;}
          for (String stats : getConfig().getStringList("messages.stats"))
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', stats.replaceAll("%displayname%", offlinePlayer).replaceAll("%username%", offlinePlayer).replaceAll("%kills%", String.valueOf(kills)).replaceAll("%deaths%", String.valueOf(deaths)).replaceAll("%kdr%", String.valueOf(kdr))));
          return true;
          //end of offline stats
        }

        //standard online stats code pulled right from old kdrstats plugin
        Player t = p.getServer().getPlayer(args[0]);
        String uuid = t.getUniqueId().toString();

        if (!getConfig().contains("players." + uuid)) {
          p.sendMessage(color(this.neverLoggedIn));
          return true;
        }

        int kills = getConfig().getInt("players." + uuid + ".kills");
        int deaths = getConfig().getInt("players." + uuid + ".deaths");
        float kdr = kdr(kills, deaths);
        if (deaths > kills){kdr=0;}
        for (String stats : getConfig().getStringList("messages.stats"))
          p.sendMessage(ChatColor.translateAlternateColorCodes('&', stats.replaceAll("%displayname%", t.getDisplayName()).replaceAll("%username%", t.getName()).replaceAll("%kills%", String.valueOf(kills)).replaceAll("%deaths%", String.valueOf(deaths)).replaceAll("%kdr%", String.valueOf(kdr))));
        return true;
      }


      if (args.length == 0) {
        String uuid = p.getUniqueId().toString();
        if (!getConfig().contains("players." + uuid)) {
          p.sendMessage(color(this.notRecorded));
          return true;
        }
        int kills = getConfig().getInt("players." + uuid + ".kills");
        int deaths = getConfig().getInt("players." + uuid + ".deaths");
        float kdr = kdr(kills, deaths);
        for (String stats : getConfig().getStringList("messages.stats"))
          p.sendMessage(color(stats.replaceAll("%displayname%", p.getDisplayName()).replaceAll("%username%", p.getName()).replaceAll("%kills%", String.valueOf(kills)).replaceAll("%deaths%", String.valueOf(deaths)).replaceAll("%kdr%", String.valueOf(kdr))));
        return true;
      }
      p.sendMessage(color(this.usage));
      return true;


      }
    if (cmd.getName().equalsIgnoreCase("statstop")){
      List<String> playerConfigList = getConfig().getStringList("players");
//      ArrayList<String> nameList = new ArrayList<String>(playerList);
      List<ConfigPlayer> players = new ArrayList<ConfigPlayer>(playerConfigList.size());

      playerConfigList.stream().forEach(playerStr -> {
        final ConfigPlayer player = new ConfigPlayer(playerStr);
        List<String> playerConfigDetails = getConfig().getStringList(playerStr);
        player.kills = Integer.valueOf(playerConfigDetails.get(0));
        player.deaths = Integer.valueOf(playerConfigDetails.get(1));
        players.add(player);
      });

      //sender.sendMessage(color("This feature will be added in the future"));
      //return true;
    }
/*    if (cmd.getName().equalsIgnoreCase("kdrstats")) {
      if (args.length == 0) {
        sender.sendMessage(color("&8&m           &6&l KDRStats &fv" + getDescription().getVersion() + "&8 &m           &r"));
        sender.sendMessage(color(""));
        sender.sendMessage(color("&8 - &6/stats [player]&f Reveal the stats of yourself or another."));
        sender.sendMessage(color("&8 - &6/kdrstats [reload] &fReload the configuration."));
        sender.sendMessage(color(""));
        sender.sendMessage(color("&f   Developer: &6LachGameZ"));
        sender.sendMessage(color("&f   Thanks for using the plugin!"));
        sender.sendMessage(color("&8&m                                           "));
        return true;
      } */
      if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
        if (!sender.hasPermission("kdrstats.reload")) {
          sender.sendMessage(color(this.noPermission));
          return true;
        } 
        sender.sendMessage(color("&eReloading configuration..."));
        reloadConfig();
        sender.sendMessage(color("&eConfiguration reloaded!"));
        return true;
      }
      sender.sendMessage(color("&eUsage: &6/stats [reload]"));
      return true;
  }
  
  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    String uuid = p.getUniqueId().toString();
    if (!getConfig().contains("players." + uuid)) {
      getConfig().set("players." + uuid + ".kills", 0);
      getConfig().set("players." + uuid + ".deaths", 0);
      saveConfig();
    } 
  }
  
  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    if (e.getEntity().getKiller() instanceof Player && e.getEntity() instanceof Player) {
      Player p = e.getEntity();
      Player k = p.getKiller();
      String puuid = p.getUniqueId().toString();
      String kuuid = k.getUniqueId().toString();
      int kills = getConfig().getInt("players." + kuuid + ".kills");
      int deaths = getConfig().getInt("players." + puuid + ".deaths");
      getConfig().set("players." + kuuid + ".kills", kills + 1);
      getConfig().set("players." + puuid + ".deaths", deaths + 1);
      saveConfig();
    } 
  }
  
  public String color(String msg) {
    return ChatColor.translateAlternateColorCodes('&', msg);
  }
}


/* Location:              C:\Users\rhoddinott\Downloads\KDRStats v1.1.jar!\lgz\kdrstats\KDRStats.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */