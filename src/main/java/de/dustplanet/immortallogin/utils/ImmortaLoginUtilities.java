package de.dustplanet.immortallogin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.utils.Updater.UpdateResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility and helper methods for ImmortalLogin.
 *
 * @author timbru31
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ImmortaLoginUtilities {
    private static final int BUFFER_SIZE = 1024;
    private static final int BSTATS_PLUGIN_ID = 683;
    private final ImmortalLogin plugin;

    @SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public ImmortaLoginUtilities(final ImmortalLogin instance) {
        plugin = instance;
    }

    /**
     * Checks for an update of ImmortalLogin.
     *
     * @param resourceID the resourceID of ImmortalLogin assigned by Spigot
     */
    @SuppressWarnings("checkstyle:SeparatorWrap")
    public void checkForUpdate(final int resourceID) {
        final boolean updaterDisabled = plugin.getConfig().getBoolean("disableUpdater", false);
        if (updaterDisabled) {
            plugin.getLogger().info("Updater is disabled");
        } else {
            final ImmortalLogin instance = plugin;
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                final Updater updater = new Updater(instance, resourceID, false);
                final UpdateResult result = updater.getResult();
                if (result == UpdateResult.NO_UPDATE) {
                    instance.getLogger().info("You are running the latest version of ImmortalLogin!");
                } else if (result == UpdateResult.UPDATE_AVAILABLE) {
                    instance.getLogger().info("There is an update available for ImmortalLogin. Go grab it from SpigotMC!");
                    instance.getLogger()
                            .info("You are running " + instance.getPlugin().getDescription().getVersion().replaceAll("[\r\n]", "")
                                    + ", latest is " + updater.getVersion().replaceAll("[\r\n]", ""));
                } else if (result == UpdateResult.SNAPSHOT_DISABLED) {
                    instance.getLogger().info("Update checking is disabled because you are running a dev build.");
                } else {
                    instance.getLogger().warning("The Updater returned the following value: " + result.name());
                }
            }, 2 * ImmortalLogin.TICKS_PER_SECOND);
        }
    }

    @SuppressWarnings({ "unused", "checkstyle:MissingJavadocMethod" })
    @SuppressFBWarnings("SEC_SIDE_EFFECT_CONSTRUCTOR")
    public void trackMetrics() {
        new Metrics(plugin, BSTATS_PLUGIN_ID);
    }

    /**
     * Config loading.
     */
    @SuppressFBWarnings(value = "SACM_STATIC_ARRAY_CREATED_IN_METHOD", justification = "Only called once")
    @SuppressWarnings("checkstyle:MagicNumber")
    public void loadConfig() {
        final String[] commands = { "immortallogin", "immortal", "im", "help", "rules", "motd" };
        final FileConfiguration config = plugin.getConfig();
        config.addDefault("disableUpdater", Boolean.FALSE);
        config.addDefault("first-login.hits", 20);
        config.addDefault("first-login.seconds", 1200);
        config.addDefault("confirmation.enabled", Boolean.FALSE);
        config.addDefault("confirmation.delay", 30);
        config.addDefault("nickColor", "DARK_PURPLE");
        config.addDefault("commandListEnabled", Boolean.TRUE);
        config.addDefault("commandListBlacklist", Boolean.FALSE);
        config.addDefault("commandList", Arrays.asList(commands));
        config.options().copyDefaults(true);
        plugin.saveConfig();

        final int seconds = config.getInt("first-login.seconds", 1200);
        final double minutes = seconds / (double) 60;
        final int hits = config.getInt("first-login.hits", 20);
        plugin.setSeconds(seconds);
        plugin.setHits(hits);
        plugin.setMinutes((int) Math.round(minutes));

        plugin.setCommandBlackList(config.getBoolean("commandListBlacklist", true));
        plugin.setCommandListEnabled(config.getBoolean("commandListEnabled", true));
        plugin.setCommandList(config.getStringList("commandList"));
        plugin.setConfirmation(config.getBoolean("confirmation.enabled"));
    }

    /**
     * Saves the localization YML to a file.
     *
     * @param localization the YML localization object
     * @param localizationFile the file to save to
     */
    public void saveLocalization(final FileConfiguration localization, final File localizationFile) {
        try {
            localization.save(localizationFile);
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save the localization! Please report this! (I/O)", e);
        }
    }

    /**
     * Loads the localization YML and writes the default to a file.
     *
     * @param localization the YML localization object
     * @param localizationFile the file to save the defaults to
     */
    public void loadLocalization(final FileConfiguration localization, final File localizationFile) {
        localization.addDefault("damage", "&5[ImmortalLogin] &4You can not hit &e%player% &4in your first &e%time% minute(s)&4!");
        localization.addDefault("hitsLeft", "&5[ImmortalLogin] &4Only &e%hits% hits &4left until your god mode will be disabled!");
        localization.addDefault("god", "&5[ImmortalLogin] &2You are now &e%time% minutes(s) &2in god mode!");
        localization.addDefault("noConsole", "&4The console can not exit the god mode. Use /im list instead!");
        localization.addDefault("notInGodMode", "&5[ImmortalLogin] &4You are not in god mode anymore!");
        localization.addDefault("targetInGodMode",
                "&5[ImmortalLogin] &4You can not hit &e%player% &4in his/her first &e%time% minute(s)&4!");
        localization.addDefault("timeLeft", "&5[ImmortalLogin] &2Note: Your god mode will expire in &e%time% minute(s)&2!");
        localization.addDefault("ungod", "&5[ImmortalLogin] &4You are no longer in god mode!");
        localization.addDefault("unknownCommand", "&5[ImmortalLogin] &4This command is unknown.");
        localization.addDefault("noPermission", "&5[ImmortalLogin] &4You do not have the permission to use this command!");
        localization.addDefault("noActiveGods", "&5[ImmortalLogin] &4There are no active players in god mode.");
        localization.addDefault("activeGods", "&5[ImmortalLogin] &2There are &e%players% &2active players in god mode:");
        localization.addDefault("commandNotAllowed", "&5[ImmortalLogin] &4This command is not allowed in god mode!");
        localization.addDefault("confirmationPending",
                "&5[ImmortalLogin] &4Are you sure? &ePlease type /im again, to leave the god mode early.");
        localization.options().copyDefaults(true);
        saveLocalization(localization, localizationFile);
    }

    /**
     * Helper method to copy a YML string to a file.
     *
     * @param yml the YML string
     * @param file the file to save to
     */
    @SuppressWarnings({ "PMD.AssignmentInOperand", "PMD.DataflowAnomalyAnalysis" })
    @SuppressFBWarnings({ "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", "RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE",
            "NP_LOAD_OF_KNOWN_NULL_VALUE" })
    public void copy(final String yml, final File file) {
        try (OutputStream out = Files.newOutputStream(file.toPath()); InputStream inputStream = plugin.getResource(yml)) {
            if (inputStream == null) {
                return;
            }
            final byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy the default config! (I/O)", e);
        }
    }

    /**
     * Sends a message to a command sender with replacements.
     *
     * @param sender the command sender - console or place
     * @param message the message to load from the YML file
     * @param replacements the replacements that should be performed
     */
    public void message(final CommandSender sender, final String message, final String... replacements) {
        String localizedMessage = plugin.getLocalization().getString(message);
        if (localizedMessage == null || localizedMessage.isEmpty()) {
            plugin.getLogger().severe("Missing i18n translation: " + message.replaceAll("[\r\n]", ""));
            return;
        }
        localizedMessage = ChatColor.translateAlternateColorCodes('&', localizedMessage);
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

    /**
     * A multi version compatible method to set the max health of a player.
     *
     * @param player the player
     */
    @SuppressWarnings({ "deprecation", "static-method", "PMD.UnnecessaryAnnotationValueElement" })
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void setMaxHealth(final Player player) {
        if (player == null) {
            return;
        }
        try {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } catch (@SuppressWarnings("unused") final NoClassDefFoundError e) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
