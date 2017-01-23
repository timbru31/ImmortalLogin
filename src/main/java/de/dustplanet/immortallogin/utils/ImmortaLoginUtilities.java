package de.dustplanet.immortallogin.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.HTTPSTokenNormalizer;
import org.mcstats.Metrics;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.utils.Updater.UpdateResult;

public class ImmortaLoginUtilities {
    private ImmortalLogin plugin;
    private String userId = "%%__USER__%%";

    public ImmortaLoginUtilities(ImmortalLogin instance) {
        plugin = instance;
    }

    public void checkForUpdate(final int resourceID) {
        boolean updaterDisabled = plugin.getConfig().getBoolean("disableUpdater", false);
        if (!updaterDisabled) {
            final ImmortalLogin instance = plugin;
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    Updater updater = new Updater(instance, resourceID, false);
                    UpdateResult result = updater.getResult();
                    if (result == UpdateResult.NO_UPDATE) {
                        instance.getLogger().info("You are running the latest version of ImmortalLogin!");
                    } else if (result == UpdateResult.UPDATE_AVAILABLE) {
                        instance.getLogger()
                        .info("There is an update available for ImmortalLogin. Go grab it from SpigotMC!");
                        instance.getLogger()
                        .info("You are running " + instance.getPlugin().getDescription().getVersion()
                                + ", latest is " + updater.getVersion());
                    } else if (result == UpdateResult.SNAPSHOT_DISABLED) {
                        instance.getLogger().info("Update checking is disabled because you are running a dev build.");
                    } else {
                        instance.getLogger().warning("The Updater returned the following value: " + result.name());
                    }
                }
            }, 40L);
        } else {
            plugin.getLogger().info("Updater is disabled");
        }
    }

    public void trackMetrics() {
        try {
            Metrics metrics = new Metrics(plugin);
            metrics.start();
        } catch (IOException e) {
            plugin.getLogger().info("Couldn't start Metrics, please report this!");
            e.printStackTrace();
        }
    }

    public void startPiracyTask() {
        // Load piracy task runner
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new HTTPSTokenNormalizer(userId, plugin), 20L * 120);
    }

    public void loadConfig() {
        // Add defaults
        String[] commands = {"help", "rules", "motd"};
        FileConfiguration config = plugin.getConfig();
        config.addDefault("disableUpdater", false);
        config.addDefault("first-login.hits", 20);
        config.addDefault("first-login.seconds", 1200);
        config.addDefault("confirmation.enabled", false);
        config.addDefault("confirmation.delay", 30);
        config.addDefault("nickColor", "DARK_PURPLE");
        config.addDefault("commandListEnabled", true);
        config.addDefault("commandListBlacklist", true);
        config.addDefault("commandList", Arrays.asList(commands));
        config.options().copyDefaults(true);
        plugin.saveConfig();

        int seconds = config.getInt("first-login.seconds", 1200);
        float minutes = seconds / (float) 60;
        int hits = config.getInt("first-login.hits", 20);
        plugin.setSeconds(seconds);
        plugin.setHits(hits);
        plugin.setMinutes(Math.round(minutes));

        plugin.setCommandBlackList(config.getBoolean("commandListBlacklist", true));
        plugin.setCommandListEnabled(config.getBoolean("commandListEnabled", true));
        plugin.setCommandList(config.getStringList("commandList"));
        plugin.setConfirmation(config.getBoolean("confirmation.enabled"));
    }

    public void saveLocalization(FileConfiguration localization, File localizationFile) {
        try {
            localization.save(localizationFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save the localization! Please report this! (I/O)");
            e.printStackTrace();
        }
    }

    public void loadLocalization(FileConfiguration localization, File localizationFile) {
        localization.addDefault("damage",
                "&5[ImmortalLogin] &4You can not hit &e%player% &4in your first &e%time% minute(s)&4!");
        localization.addDefault("hitsLeft",
                "&5[ImmortalLogin] &4Only &e%hits% hits &4left until your god mode will be disabled!");
        localization.addDefault("god", "&5[ImmortalLogin] &2You are now &e%time% minutes(s) &2in god mode!");
        localization.addDefault("noConsole", "&4The console can not exit the god mode. Use /im list instead!");
        localization.addDefault("notInGodMode", "&5[ImmortalLogin] &4You are not in god mode anymore!");
        localization.addDefault("targetInGodMode",
                "&5[ImmortalLogin] &4You can not hit &e%player% &4in his/her first &e%time% minute(s)&4!");
        localization.addDefault("timeLeft",
                "&5[ImmortalLogin] &2Note: Your god mode will expire in &e%time% minute(s)&2!");
        localization.addDefault("ungod", "&5[ImmortalLogin] &4You are no longer in god mode!");
        localization.addDefault("unknownCommand", "&5[ImmortalLogin] &4This command is unknown.");
        localization.addDefault("noPermission", "&5[ImmortalLogin] &4You do not have the permission to use this command!");
        localization.addDefault("noActiveGods", "&5[ImmortalLogin] &4There are no active players in god mode.");
        localization.addDefault("activeGods", "&5[ImmortalLogin] &2There are &e%players% &2active players in god mode:");
        localization.addDefault("commandNotAllowed", "&5[ImmortalLogin] &4This command is not allowed in god mode!");
        localization.addDefault("confirmationPending", "&5[ImmortalLogin] &4Are you sure? &ePlease type /im again, to leave the god mode early.");
        localization.options().copyDefaults(true);
        saveLocalization(localization, localizationFile);
    }

    public void copy(String yml, File file) {
        try (OutputStream out = new FileOutputStream(file); InputStream in = plugin.getResource(yml)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to copy the default config! (I/O)");
            e.printStackTrace();
        }
    }

    public void message(CommandSender sender, String message, String... replacements) {
        String localizedMessage = plugin.getLocalization().getString(message);
        if (localizedMessage == null || localizedMessage.isEmpty()) {
            plugin.getLogger().severe("Missing i18n translation: " + message);
            return;
        }
        localizedMessage = ChatColor.translateAlternateColorCodes('\u0026', localizedMessage);
        for (int index = 0; index < replacements.length; index++) {
            switch (index) {
                case 0:
                    localizedMessage = localizedMessage.replace("%player%", replacements[index]);
                    localizedMessage = localizedMessage.replace("%players%", replacements[index]);
                    break;
                case 1:
                    localizedMessage = localizedMessage.replace("%time%", replacements[index]);
                    break;
                case 2:
                    localizedMessage = localizedMessage.replace("%hits%", replacements[index]);
                    break;
                default:
                    break;
            }
        }
        sender.sendMessage(localizedMessage);
    }
}
