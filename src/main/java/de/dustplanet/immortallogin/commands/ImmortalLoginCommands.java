package de.dustplanet.immortallogin.commands;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Command handler of ImmortalLogin to ungod or list current gods.
 *
 * @author timbru31
 */
@SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "checkstyle:MultipleStringLiterals" })
public class ImmortalLoginCommands implements CommandExecutor {
    private final ImmortalLogin plugin;
    private final ImmortaLoginUtilities utilities;

    @SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public ImmortalLoginCommands(final ImmortalLogin instance, final ImmortaLoginUtilities utilities) {
        plugin = instance;
        this.utilities = utilities;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        switch (args.length) {
            case 0:
                handleUnGod(sender);
                break;
            case 1:
                if ("list".equalsIgnoreCase(args[0])) {
                    handleGodList(sender);
                } else {
                    utilities.message(sender, "unknownCommand");
                }
                break;
            case 2:
                if ("add".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0])) {
                    handleGodMode(sender, args);
                } else {
                    utilities.message(sender, "unknownCommand");
                }
                break;
            default:
                utilities.message(sender, "unknownCommand");
                break;
        }
        return true;
    }

    private void handleGodMode(final CommandSender sender, final String[] args) {
        if (sender.hasPermission("immortallogin.admin.gods")) {
            final String playerName = args[1];
            final Player player = plugin.getServer().getPlayer(playerName);
            if (player == null) {
                utilities.message(sender, "playerNotFound");
                return;
            }
            final String mode = args[0];
            if ("add".equalsIgnoreCase(mode)) {
                if (plugin.getGods().contains(player.getUniqueId())) {
                    utilities.message(sender, "playerAlreadyGod");
                } else {
                    plugin.setGod(player);
                    utilities.message(sender, "playerAddedGod");
                }
            } else {
                if (!plugin.getGods().contains(player.getUniqueId())) {
                    utilities.message(sender, "playerNotGod");
                } else {
                    plugin.setUnGod(player);
                    utilities.message(sender, "playerRemovedGod");
                }
            }
        } else {
            utilities.message(sender, "noPermission");
        }
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private void handleUnGod(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            utilities.message(sender, "noConsole");
            return;
        }

        final List<UUID> gods = plugin.getGods();
        final Player player = (Player) sender;
        if (!gods.contains(player.getUniqueId())) {
            utilities.message(sender, "notInGodMode");
            return;
        }

        if (plugin.isConfirmation()) {
            final UUID playerName = player.getUniqueId();
            if (!plugin.getPendingConfirmationList().contains(playerName)) {
                plugin.getPendingConfirmationList().add(playerName);
                utilities.message(sender, "confirmationPending");
                return;
            }
            plugin.getPendingConfirmationList().remove(playerName);
        }
        utilities.message(sender, "ungod");
        gods.remove(player.getUniqueId());
        plugin.getServer().getScheduler().cancelTask(plugin.getTimerTaskIDs().get(player.getUniqueId()));
        plugin.getTimerTaskIDs().remove(player.getUniqueId());
        plugin.getServer().getScheduler().cancelTask(plugin.getUngodTaskIDs().get(player.getUniqueId()));
        plugin.getUngodTaskIDs().remove(player.getUniqueId());

    }

    @SuppressWarnings("checkstyle:SeparatorWrap")
    private void handleGodList(final CommandSender sender) {
        final List<UUID> gods = plugin.getGods();
        if (sender.hasPermission("immortallogin.list.gods")) {
            if (gods.isEmpty()) {
                utilities.message(sender, "noActiveGods");
            } else {
                final String activeGods = gods.stream().map(e -> plugin.getServer().getPlayer(e)).filter(player -> player != null)
                        .map(Player::getName).collect(Collectors.joining(", "));
                utilities.message(sender, "activeGods", Integer.toString(gods.size()));
                sender.sendMessage(ChatColor.YELLOW + activeGods);
            }
        } else {
            utilities.message(sender, "noPermission");
        }
    }
}
