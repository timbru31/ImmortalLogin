package de.dustplanet.immortallogin.listeners;

import java.util.UUID;

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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The event listener to cancel various damages events while in god mode.
 *
 * @author timbru31
 */
@SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "checkstyle:MultipleStringLiterals" })
public class ImmortalLoginListener implements Listener {
    private final ImmortalLogin plugin;
    private final ImmortaLoginUtilities utilities;

    @SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidDuplicateLiterals" })
    public ImmortalLoginListener(final ImmortalLogin instance, final ImmortaLoginUtilities utilities) {
        plugin = instance;
        this.utilities = utilities;
    }

    @EventHandler
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "checkstyle:Indentation" })
    public void onPlayerJoin(final PlayerJoinEvent event) {
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
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onFoodLevelChanged(final FoodLevelChangeEvent event) {
        final Player player = (Player) event.getEntity();
        if (plugin.getGods().contains(player.getUniqueId())) {
            player.setFoodLevel(player.getFoodLevel());
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        if (plugin.getGods().contains(player.getUniqueId())) {
            utilities.setMaxHealth(player);
            player.setRemainingAir(player.getMaximumAir());
            player.setFireTicks(-1);
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "checkstyle:ReturnCount", "checkstyle:NestedIfDepth" })
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        final Player target = (Player) event.getEntity();
        final Player damager = (Player) event.getDamager();
        if (plugin.getGods().contains(damager.getUniqueId())) {
            event.setCancelled(true);
            if (plugin.getAggros().containsKey(damager.getUniqueId())) {
                final int hits = plugin.getAggros().get(damager.getUniqueId()) + 1;
                if (hits >= plugin.getHits()) {
                    plugin.getGods().remove(damager.getUniqueId());
                    plugin.getAggros().remove(damager.getUniqueId());
                    plugin.getServer().getScheduler().cancelTask(plugin.getTimerTaskIDs().get(damager.getUniqueId()));
                    plugin.getTimerTaskIDs().remove(damager.getUniqueId());
                    plugin.getServer().getScheduler().cancelTask(plugin.getUngodTaskIDs().get(damager.getUniqueId()));
                    plugin.getUngodTaskIDs().remove(damager.getUniqueId());
                    utilities.message(damager, "ungod");
                    removeNick(damager);
                    return;
                }
                final int hitsLeft = plugin.getHits() - hits;
                plugin.getAggros().put(damager.getUniqueId(), hits);
                utilities.message(damager, "hitsLeft", "", "", Integer.toString(hitsLeft));
                return;
            }
            utilities.message(damager, "damage", target.getName(), Integer.toString(plugin.getMinutes()));
            plugin.getAggros().put(damager.getUniqueId(), 1);
            final int rest = plugin.getHits() - 1;
            utilities.message(damager, "hitsLeft", "", "", Integer.toString(rest));
        } else if (plugin.getGods().contains(target.getUniqueId())) {
            utilities.message(damager, "targetInGodMode", target.getName(), Integer.toString(plugin.getMinutes()));
            event.setCancelled(true);
        }
    }

    private void removeNick(final Player damager) {
        if (plugin.getNickManager() != null) {
            plugin.getNickManager().removeNick(damager.getUniqueId());
            plugin.getNickManager().removeSkin(damager.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = event.getPlayer().getUniqueId();
        if (plugin.isCommandListEnabled() && plugin.getGods().contains(playerUUID)) {
            final String message = event.getMessage();
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
