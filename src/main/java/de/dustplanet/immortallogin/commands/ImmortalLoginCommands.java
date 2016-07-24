package de.dustplanet.immortallogin.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;
import net.md_5.bungee.api.ChatColor;

public class ImmortalLoginCommands implements CommandExecutor {
    private ImmortalLogin plugin;
    private ImmortaLoginUtilities utilities;

    public ImmortalLoginCommands(ImmortalLogin instance, ImmortaLoginUtilities utilities) {
        plugin = instance;
        this.utilities = utilities;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ArrayList<UUID> gods = plugin.getGods();
        switch(args.length) {
        case 0:
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (gods.contains(player.getUniqueId())) {
                    utilities.message(sender, "ungod");
                    gods.remove(player.getUniqueId());
                    plugin.getServer().getScheduler().cancelTask(plugin.getTaskIDs().get(player.getUniqueId()));
                    plugin.getTaskIDs().remove(player.getUniqueId());
                } else {
                    utilities.message(sender, "notInGodMode");
                }
            } else {
                utilities.message(sender, "noConsole");
            }
            break;
        case 1:
            if (args[0].equalsIgnoreCase("list")) {
                if (sender.hasPermission("immortallogin.list.gods")) {
                    if (gods.isEmpty()) {
                        utilities.message(sender, "noActiveGods");
                    } else {
                        List<String> activeGods = gods.stream().map(e -> ChatColor.YELLOW + plugin.getServer().getPlayer(e).getName()).collect(Collectors.toList());
                        utilities.message(sender, "activeGods", Integer.toString(activeGods.size()));
                        sender.sendMessage(Strings.join(activeGods, ", "));
                    }
                } else {
                    utilities.message(sender, "noPermission");
                }
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
}
