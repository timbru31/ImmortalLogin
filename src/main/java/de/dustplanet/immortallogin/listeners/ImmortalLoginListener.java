package de.dustplanet.immortallogin.listeners;

import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;

public class ImmortalLoginListener implements Listener {
    private ImmortalLogin plugin;
    private ImmortaLoginUtilities utilities;

    public ImmortalLoginListener(ImmortalLogin instance, ImmortaLoginUtilities utilities) {
        plugin = instance;
        this.utilities = utilities;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            plugin.setGod(player);
            final ImmortaLoginUtilities utilz = utilities;
            final ImmortalLogin instance = plugin;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                    () -> utilz.message(player, "god", "", Integer.toString(instance.getMinutes())), 2 * ImmortalLogin.TICKS_PER_SECOND);
        }
    }

    @EventHandler
    public void onFoodLevelChanged(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        if (plugin.getGods().contains(player.getUniqueId())) {
            player.setFoodLevel(player.getFoodLevel());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (plugin.getGods().contains(player.getUniqueId())) {
            try {
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            } catch (NoClassDefFoundError e) {
                player.setHealth(player.getMaxHealth());
            }
            player.setRemainingAir(player.getMaximumAir());
            player.setFireTicks(-1);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (plugin.getGods().contains(damager.getUniqueId())) {
                    event.setCancelled(true);
                    if (!plugin.getAggros().containsKey(damager.getUniqueId())) {
                        utilities.message(damager, "damage", target.getName(), Integer.toString(plugin.getMinutes()));
                    }
                    if (plugin.getAggros().containsKey(damager.getUniqueId())) {
                        int hits = plugin.getAggros().get(damager.getUniqueId()) + 1;
                        if (hits >= plugin.getHits()) {
                            plugin.getGods().remove(damager.getUniqueId());
                            plugin.getAggros().remove(damager.getUniqueId());
                            plugin.getServer().getScheduler().cancelTask(plugin.getTimerTaskIDs().get(damager.getUniqueId()));
                            plugin.getTimerTaskIDs().remove(damager.getUniqueId());
                            plugin.getServer().getScheduler().cancelTask(plugin.getUngodTaskIDs().get(damager.getUniqueId()));
                            plugin.getUngodTaskIDs().remove(damager.getUniqueId());
                            utilities.message(damager, "ungod");
                            if (plugin.getNickManager() != null) {
                                plugin.getNickManager().removeNick(damager.getUniqueId());
                                plugin.getNickManager().removeSkin(damager.getUniqueId());
                            }
                            return;
                        }
                        int hitsLeft = plugin.getHits() - hits;
                        plugin.getAggros().put(damager.getUniqueId(), hits);
                        utilities.message(damager, "hitsLeft", "", "", Integer.toString(hitsLeft));
                        return;
                    }
                    plugin.getAggros().put(damager.getUniqueId(), 1);
                    int rest = plugin.getHits() - 1;
                    utilities.message(damager, "hitsLeft", "", "", Integer.toString(rest));
                } else if (plugin.getGods().contains(target.getUniqueId())) {
                    utilities.message(damager, "targetInGodMode", target.getName(), Integer.toString(plugin.getMinutes()));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (plugin.isCommandListEnabled() && plugin.getGods().contains(playerUUID)) {
            String message = event.getMessage();
            String command = message.replaceFirst("/", "");
            if (command.contains(" ")) {
                command = command.substring(0, command.indexOf(' '));
            }
            if (plugin.isCommandBlackList() && plugin.getCommandList().contains(command)
                    || !plugin.isCommandBlackList() && !plugin.getCommandList().contains(command)) {
                utilities.message(player, "commandNotAllowed");
                event.setCancelled(true);
            }
        }
    }
}
