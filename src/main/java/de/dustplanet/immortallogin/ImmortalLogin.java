package de.dustplanet.immortallogin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.inventivetalent.nicknamer.api.NickManager;
import org.inventivetalent.nicknamer.api.NickNamerAPI;

import de.dustplanet.immortallogin.commands.ImmortalLoginCommands;
import de.dustplanet.immortallogin.listeners.ImmortalLoginListener;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

/**
 * The main class of the ImmortalLogin.
 *
 * @author timbru31
 */
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "PMD.TooManyFields", "checkstyle:MultipleStringLiterals", "checkstyle:MissingCtor" })
@SuppressFBWarnings({ "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "CD_CIRCULAR_DEPENDENCY" })
public class ImmortalLogin extends JavaPlugin {
    public static final long TICKS_PER_SECOND = 20L;
    private static final int RESOURCE_ID = 25_481;
    @Getter
    private final List<UUID> gods = new ArrayList<>();
    @Getter
    private final Map<UUID, Integer> aggros = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Integer> timerTaskIDs = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Integer> ungodTaskIDs = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private int seconds;
    @Getter
    @Setter
    private int minutes;
    @Getter
    @Setter
    private int hits;
    @Getter
    @Setter
    private FileConfiguration localization;
    private final ImmortaLoginUtilities utilities = new ImmortaLoginUtilities(this);
    @Getter
    @Setter
    private NickManager nickManager;
    @Getter
    @Setter
    private List<String> commandList = new ArrayList<>();
    @Getter
    @Setter
    private boolean commandBlackList = true;
    @Getter
    @Setter
    private boolean commandListEnabled = true;
    @Getter
    private final List<UUID> pendingConfirmationList = new ArrayList<>();
    @Getter
    @Setter
    private boolean confirmation;
    private BukkitTask confirmationCleanupTask;

    @Override
    public void onDisable() {
        getGods().clear();
        getAggros().clear();
        getPendingConfirmationList().clear();
        if (confirmationCleanupTask != null) {
            confirmationCleanupTask.cancel();
        }
        for (final int taskID : getTimerTaskIDs().values()) {
            getServer().getScheduler().cancelTask(taskID);
        }
        getTimerTaskIDs().clear();
        for (final int taskID : getUngodTaskIDs().values()) {
            getServer().getScheduler().cancelTask(taskID);
        }
        getUngodTaskIDs().clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @SuppressWarnings({ "PMD.UnnecessaryAnnotationValueElement", "PMD.AvoidCatchingGenericException", "checkstyle:IllegalCatch" })
    public void onEnable() {
        final File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            if (getDataFolder().exists() || getDataFolder().mkdirs()) {
                utilities.copy("config.yml", configFile);
            } else {
                getLogger().severe("The config folder could NOT be created, make sure it's writable!");
                getLogger().severe("Disabling now!");
                setEnabled(false);
                return;
            }
        }

        utilities.loadConfig();

        final File localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            utilities.copy("localization.yml", localizationFile);
        }

        setLocalization(YamlConfiguration.loadConfiguration(localizationFile));
        utilities.loadLocalization(getLocalization(), localizationFile);

        utilities.checkForUpdate(RESOURCE_ID);
        utilities.trackMetrics();

        final PluginManager pluginManager = getServer().getPluginManager();
        final ImmortalLoginListener eventListener = new ImmortalLoginListener(this, utilities);
        pluginManager.registerEvents(eventListener, this);

        final PluginCommand command = getCommand("immortallogin");
        if (command != null) {
            command.setExecutor(new ImmortalLoginCommands(this, utilities));
        }

        if (getServer().getPluginManager().getPlugin("NickNamer") != null) {
            try {
                setNickManager(NickNamerAPI.getNickManager());
            } catch (@SuppressWarnings("unused") final Exception e) {
                getLogger().severe("Unable to load NickNamer!");
            }
        }

        registerConfirmationCleanupTask();
    }

    /**
     * Enables the god mode for a player.
     *
     * @param player the player
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void setGod(final Player player) {
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        getGods().add(player.getUniqueId());
        addTimer(player);

        if (getNickManager() != null) {
            getNickManager().setNick(player.getUniqueId(),
                    ChatColor.valueOf(getConfig().getString("nickColor", "DARK_PURPLE")) + player.getName());
            getNickManager().setSkin(player.getUniqueId(), player.getName());
        }
        final ImmortaLoginUtilities utilz = utilities;
        final int ungodTaskID = getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (getGods().contains(player.getUniqueId())) {
                utilz.message(player, "ungod");
                getGods().remove(player.getUniqueId());
                getServer().getScheduler().cancelTask(getTimerTaskIDs().get(player.getUniqueId()));
                getTimerTaskIDs().remove(player.getUniqueId());
                getUngodTaskIDs().remove(player.getUniqueId());
                if (getNickManager() != null) {
                    getNickManager().removeNick(player.getUniqueId());
                    getNickManager().removeSkin(player.getUniqueId());
                }
            }
        }, getSeconds() * TICKS_PER_SECOND);
        getUngodTaskIDs().put(player.getUniqueId(), ungodTaskID);

        final ImmortalLogin instance = this;
        getServer().getScheduler().scheduleSyncDelayedTask(this,
                () -> utilz.message(player, "god", "", Integer.toString(instance.getMinutes())), 2 * ImmortalLogin.TICKS_PER_SECOND);
    }

    /**
     * Disables god mode for a player.
     *
     * @param player the player
     */
    public void setUnGod(final Player player) {
        getGods().remove(player.getUniqueId());
        getAggros().remove(player.getUniqueId());
        getServer().getScheduler().cancelTask(getTimerTaskIDs().get(player.getUniqueId()));
        getTimerTaskIDs().remove(player.getUniqueId());
        getServer().getScheduler().cancelTask(getUngodTaskIDs().get(player.getUniqueId()));
        getUngodTaskIDs().remove(player.getUniqueId());
        utilities.message(player, "ungod");
        utilities.removeNick(player);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    private void addTimer(final Player player) {
        final int tempSubtract = seconds / 4;
        final long delay = seconds / 4 * TICKS_PER_SECOND;
        final ImmortaLoginUtilities utilz = utilities;
        final int secondz = seconds;
        final int taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            private int index = 1;

            @Override
            public void run() {
                final int rest = (secondz - tempSubtract * index) / 60;
                if (rest == 0) {
                    return;
                }
                utilz.message(player, "timeLeft", "", Integer.toString(rest));
                index++;
            }
        }, delay, delay);
        getTimerTaskIDs().put(player.getUniqueId(), taskID);
    }

    @SuppressWarnings("checkstyle:Indentation")
    private void registerConfirmationCleanupTask() {
        if (isConfirmation()) {
            final long delay = getConfig().getInt("confirmation.delay") * TICKS_PER_SECOND;
            confirmationCleanupTask = getServer().getScheduler().runTaskTimerAsynchronously(this,
                    () -> getPendingConfirmationList().clear(), delay, delay);
        }
    }

    /**
     * Returns this plugin instance.
     *
     * @return this instance
     */
    public ImmortalLogin getPlugin() {
        return this;
    }

    /**
     * Disables this plugin.
     */
    public void disable() {
        this.setEnabled(false);
    }
}
