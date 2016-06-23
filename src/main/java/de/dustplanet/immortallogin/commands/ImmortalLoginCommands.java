package de.dustplanet.immortallogin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;

public class ImmortalLoginCommands implements CommandExecutor {
    private ImmortalLogin plugin;
    private ImmortaLoginUtilities utilities;

    public ImmortalLoginCommands(ImmortalLogin instance, ImmortaLoginUtilities utilities) {
        plugin = instance;
        this.utilities = utilities;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (plugin.getGods().contains(player.getUniqueId())) {
                utilities.message(sender, "ungod");
                plugin.getGods().remove(player.getUniqueId());
                plugin.getServer().getScheduler().cancelTask(plugin.getTaskIDs().get(player.getUniqueId()));
                plugin.getTaskIDs().remove(player.getUniqueId());
            } else {
                utilities.message(sender, "notInGodMode");
            }
        } else {
            utilities.message(sender, "noConsole");
        }
        return true;
    }
}
