package de.dustplanet.immortallogin;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ImmortalLogin  extends JavaPlugin implements Listener {
    public ArrayList<String> gods = new ArrayList<>();
    private HashMap<String, Integer> aggro = new HashMap<>();
    private HashMap<String, Integer> taskIDs = new HashMap<>();
    private String prefix, godMsg, ungodMsg, damageMsg, leftMsg, noMsg, hitsLeft;
    private int seconds, minutes, hits;

    @Override
    public void onDisable() {
        gods.clear();
        aggro.clear();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().addDefault("messages.prefix.string", "[ImmortalLogin]");
        getConfig().addDefault("messages.prefix.color", "dark_purple");
        getConfig().addDefault("first-loign.seconds", 1200);
        getConfig().addDefault("first-login.hits", 20);
        getConfig().options().copyDefaults(true);
        saveConfig();
        // Config & Variables
        String pre = getConfig().getString("messages.prefix.string");
        if (!pre.equals("")) {
            prefix = ChatColor.valueOf(getConfig().getString("messages.prefix.color").toUpperCase()) + pre + " " + ChatColor.WHITE;
        } else {
            prefix = "";
        }
        leftMsg = "§5Hinweis: Es verbleiben noch §6%lefttime §5Minuten im Gott Modus!";
        damageMsg = "§5Du kannst §6%player §5in deinen ersten §6%time §5Minuten nicht angreifen!";
        noMsg = "§5Du kannst §6%player §5nicht in seinen ersten §6%time §5Minuten angreifen!";
        godMsg = "§5Hinweis: Du bist für §6%time §5Minuten im Gott Modus. §4Nutze diese Zeit um weit weg zu laufen!\n§9Mit §6/im §9kannst du deinen Gott Modus vorzeitig beenden!";
        ungodMsg = "§5Dein Gott Modus ist nun vorbei. §4Du bist nun verwundbar!";
        seconds = getConfig().getInt("first-login.seconds");
        float temp = seconds / 60;
        minutes = Math.round(temp);
        hits = getConfig().getInt("first-login.hits");
        hitsLeft = "§9Nur noch §6%hits §9Schläge, bis dein GodMode vorher beendet wird!";
    }

    private void setGod(final Player target, int time) {
        target.setHealth(20);
        gods.add(target.getName());
        timer(target);
        // Vielleicht wurde er von außen weggemacht
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if (gods.contains(target.getName())) {
                    target.sendMessage(prefix + ungodMsg.replaceAll("%player", target.getName()));
                    gods.remove(target.getName());
                    getServer().getScheduler().cancelTask(taskIDs.get(target.getName()));
                    taskIDs.remove(target.getName());
                }
            }
        }, seconds * 20);
    }

    private void timer(final Player player) {
        final int tempSubtract = seconds / 4;
        long delay = seconds / 4 * 20L;
        int ID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            int i = 1;
            @Override
            public void run() {
                int rest = (seconds - tempSubtract * i) / 60;
                if (rest == 0) {
                    return;
                }
                player.sendMessage(prefix + leftMsg.replaceAll("%player", player.getName()).replaceAll("%lefttime", Integer.toString(rest)));
                i++;
            }
        }, delay, delay);
        taskIDs.put(player.getName(), ID);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            setGod(player, seconds);
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(prefix + godMsg.replaceAll("%player", player.getName()).replaceAll("%time", Integer.toString(minutes)).replaceAll("%seconds", Integer.toString(seconds)));
                }
            }, 40L);
        }
    }

    @EventHandler
    public void onFoodLevelChanged(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        if (gods.contains(player.getName())) {
            player.setFoodLevel(player.getFoodLevel());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getEntity();
        if (gods.contains(player.getName())) {
            player.setHealth(20);
            player.setRemainingAir(player.getMaximumAir());
            player.setFireTicks(-1);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player target = (Player)event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player damager = (Player)event.getDamager();
                if (gods.contains(damager.getName())) {
                    event.setCancelled(true);
                    if (!aggro.containsKey(damager.getName())) {
                        damager.sendMessage(prefix + damageMsg.replaceAll("%player", target.getName()).replaceAll("%time", Integer.toString(minutes)).replaceAll("%seconds", Integer.toString(seconds)));
                    }
                    if (aggro.containsKey(damager.getName())) {
                        int tempHits = aggro.get(damager.getName()) + 1;
                        if (tempHits == hits) {
                            gods.remove(damager.getName());
                            aggro.remove(damager.getName());
                            getServer().getScheduler().cancelTask(taskIDs.get(damager.getName()));
                            taskIDs.remove(damager.getName());
                            damager.sendMessage(prefix + ungodMsg);
                            return;
                        }
                        int rest = hits - tempHits;
                        aggro.put(damager.getName(), tempHits);
                        damager.sendMessage(prefix + hitsLeft.replace("%hits", Integer.toString(rest)));
                        return;
                    }
                    aggro.put(damager.getName(), 1);
                    int rest = hits - 1;
                    damager.sendMessage(prefix + hitsLeft.replace("%hits", Integer.toString(rest)));
                }
                else if (gods.contains(target.getName())) {
                    damager.sendMessage(prefix + noMsg.replaceAll("%player", target.getName()).replaceAll("%time", Integer.toString(minutes)).replaceAll("%seconds", Integer.toString(seconds)));
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (gods.contains(sender.getName())) {
            sender.sendMessage(prefix + ungodMsg.replaceAll("%player", sender.getName()));
            gods.remove(sender.getName());
            getServer().getScheduler().cancelTask(taskIDs.get(sender.getName()));
            taskIDs.remove(sender.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Du bist nicht im Gott Modus!");
        }
        return true;
    }
}
