package de.dustplanet.immortallogin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.inventivetalent.nicknamer.api.NickManager;
import org.inventivetalent.nicknamer.api.NickNamerAPI;

import de.dustplanet.immortallogin.commands.ImmortalLoginCommands;
import de.dustplanet.immortallogin.listeners.ImmortalLoginListener;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;
import de.dustplanet.immortallogin.utils.ScalarYamlConfiguration;
import lombok.Getter;
import lombok.Setter;

public class ImmortalLogin extends JavaPlugin {
    private static final int RESOURCE_ID = 25481;
    private static final long TICKS_PER_SECOND = 20L;
    @Getter
    private List<UUID> gods = new ArrayList<>();
    @Getter
    private Map<UUID, Integer> aggros = new HashMap<>();
    @Getter
    private Map<UUID, Integer> timerTaskIDs = new HashMap<>();
    @Getter
    private Map<UUID, Integer> ungodTaskIDs = new HashMap<>();
    @Getter
    @Setter
    private int seconds, minutes, hits;
    @Getter
    @Setter
    private FileConfiguration localization;
    private File configFile, localizationFile;
    private ImmortaLoginUtilities utilities = new ImmortaLoginUtilities(this);
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
    private List<UUID> pendingConfirmationList = new ArrayList<>();
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
        for (int taskID : getTimerTaskIDs().values()) {
            getServer().getScheduler().cancelTask(taskID);
        }
        getTimerTaskIDs().clear();
        for (int taskID : getUngodTaskIDs().values()) {
            getServer().getScheduler().cancelTask(taskID);
        }
        getUngodTaskIDs().clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        // Config
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            if (configFile.getParentFile().mkdirs()) {
                utilities.copy("config.yml", configFile);
            } else {
                getLogger().severe("The config folder could NOT be created, make sure it's writable!");
                getLogger().severe("Disabling now!");
                setEnabled(false);
                return;
            }
        }

        utilities.loadConfig();

        // Localization
        localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            utilities.copy("localization.yml", localizationFile);
        }

        setLocalization(ScalarYamlConfiguration.loadConfiguration(localizationFile));
        utilities.loadLocalization(getLocalization(), localizationFile);

        utilities.startPiracyTask();
        utilities.checkForUpdate(RESOURCE_ID);
        utilities.trackMetrics();

        PluginManager pluginManager = getServer().getPluginManager();
        ImmortalLoginListener eventListener = new ImmortalLoginListener(this, utilities);
        pluginManager.registerEvents(eventListener, this);

        getCommand("immortallogin").setExecutor(new ImmortalLoginCommands(this, utilities));

        if (getServer().getPluginManager().getPlugin("NickNamer") != null) {
            try {
                setNickManager(NickNamerAPI.getNickManager());
            } catch (Exception e) {
                getLogger().severe("Unable to load NickNamer!");
            }
        }

        registerConfirmationCleanupTask();
    }

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
        final NickManager _nickManager = getNickManager();
        int ungodTaskID = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if (getGods().contains(player.getUniqueId())) {
                    utilz.message(player, "ungod");
                    getGods().remove(player.getUniqueId());
                    getServer().getScheduler().cancelTask(getTimerTaskIDs().get(player.getUniqueId()));
                    getTimerTaskIDs().remove(player.getUniqueId());
                    getUngodTaskIDs().remove(player.getUniqueId());
                    if (_nickManager != null) {
                        _nickManager.removeNick(player.getUniqueId());
                        _nickManager.removeSkin(player.getUniqueId());
                    }
                }
            }
        }, getSeconds() * (long) 20);
        getUngodTaskIDs().put(player.getUniqueId(), ungodTaskID);
    }

    private void addTimer(final Player player) {
        final int tempSubtract = seconds / 4;
        long delay = seconds / 4 * 20L;
        final ImmortaLoginUtilities utilz = utilities;
        final int secondz = seconds;
        int taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            private int i = 1;
            @Override
            public void run() {
                int rest = (secondz - tempSubtract * i) / 60;
                if (rest == 0) {
                    return;
                }
                utilz.message(player, "timeLeft", "", Integer.toString(rest));
                i++;
            }
        }, delay, delay);
        getTimerTaskIDs().put(player.getUniqueId(), taskID);
    }

    private void registerConfirmationCleanupTask() {
        // Task if needed
        if (isConfirmation()) {
            confirmationCleanupTask = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    // Clear pending list
                    getPendingConfirmationList().clear();
                }
            }, getConfig().getInt("confirmation.delay") * TICKS_PER_SECOND, getConfig().getInt("confirmation.delay") * TICKS_PER_SECOND);
        }
    }

    public ImmortalLogin getPlugin() {
        return this;
    }

    public void disable() {
        this.setEnabled(false);
    }
}
