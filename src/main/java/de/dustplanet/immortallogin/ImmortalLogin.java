package de.dustplanet.immortallogin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.nicknamer.api.NickManager;
import org.inventivetalent.nicknamer.api.NickNamerAPI;

import de.dustplanet.immortallogin.commands.ImmortalLoginCommands;
import de.dustplanet.immortallogin.listeners.ImmortalLoginListener;
import de.dustplanet.immortallogin.utils.ImmortaLoginUtilities;
import de.dustplanet.immortallogin.utils.ScalarYamlConfiguration;
import net.md_5.bungee.api.ChatColor;

public class ImmortalLogin extends JavaPlugin {
    private ArrayList<UUID> gods = new ArrayList<>();
    private HashMap<UUID, Integer> aggros = new HashMap<>();
    private HashMap<UUID, Integer> taskIDs = new HashMap<>();
    private int seconds, minutes, hits;
    private FileConfiguration localization;
    private File configFile, localizationFile;
    private ImmortaLoginUtilities utilities = new ImmortaLoginUtilities(this);
    private NickManager nickManager;

    @Override
    public void onDisable() {
        getGods().clear();
        getAggros().clear();
        for (int taskID : getTaskIDs().values()) {
            getServer().getScheduler().cancelTask(taskID);
        }
        getTaskIDs().clear();
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
        utilities.checkForUpdate(0);
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

    }

    public void setGod(final Player player) {
        player.setHealth(player.getMaxHealth());
        getGods().add(player.getUniqueId());
        addTimer(player);

        if (getNickManager() != null) {
            getNickManager().setNick(player.getUniqueId(),
                    ChatColor.valueOf(getConfig().getString("nickColor", "DARK_PURPLE")) + player.getName());
            getNickManager().setSkin(player.getUniqueId(), player.getName());
        }
        final ImmortaLoginUtilities utilz = utilities;
        final NickManager _nickManager = getNickManager();
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if (getGods().contains(player.getUniqueId())) {
                    utilz.message(player, "ungod");
                    getGods().remove(player.getUniqueId());
                    getServer().getScheduler().cancelTask(getTaskIDs().get(player.getUniqueId()));
                    getTaskIDs().remove(player.getUniqueId());
                    if (_nickManager != null) {
                        _nickManager.removeNick(player.getUniqueId());
                        _nickManager.removeSkin(player.getUniqueId());
                    }
                }
            }
        }, getSeconds() * (long) 20);
    }

    private void addTimer(final Player player) {
        final int tempSubtract = seconds / 4;
        long delay = seconds / 4 * 20L;
        final ImmortaLoginUtilities utilz = utilities;
        final int secondz = seconds;
        int taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            int i = 1;

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
        getTaskIDs().put(player.getUniqueId(), taskID);
    }

    public JavaPlugin getPlugin() {
        return this;
    }

    public void disable() {
        this.setEnabled(false);
    }

    public ArrayList<UUID> getGods() {
        return gods;
    }

    public HashMap<UUID, Integer> getTaskIDs() {
        return taskIDs;
    }

    public HashMap<UUID, Integer> getAggros() {
        return aggros;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public FileConfiguration getLocalization() {
        return localization;
    }

    public void setLocalization(FileConfiguration localization) {
        this.localization = localization;
    }

    public NickManager getNickManager() {
        return nickManager;
    }

    public void setNickManager(NickManager nickManager) {
        this.nickManager = nickManager;
    }
}
