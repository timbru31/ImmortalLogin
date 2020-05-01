package de.dustplanet.immortallogin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

/**
 * Based on the Updater of PatoTheBest. Thanks for sharing this class.
 *
 * @author PatoTheBest
 * @author xGhOsTkiLLeRx
 */

public class Updater {
    private JavaPlugin plugin;
    private static final String REQUEST_METHOD = "GET";
    private final String RESOURCE_ID;
    private static final String HOST = "https://api.spigotmc.org";
    private static final String PATH = "/legacy/update.php";

    @Getter
    private String version;
    private String oldVersion;

    @Getter
    private Updater.UpdateResult result = Updater.UpdateResult.DISABLED;

    private HttpURLConnection connection;

    public enum UpdateResult {
        NO_UPDATE, DISABLED, FAIL_SPIGOT, FAIL_NOVERSION, BAD_API_KEY, BAD_RESOURCEID, UPDATE_AVAILABLE, SNAPSHOT_DISABLED
    }

    public Updater(JavaPlugin plugin, Integer resourceId, boolean disabled) {
        RESOURCE_ID = Integer.toString(resourceId);
        this.plugin = plugin;
        oldVersion = this.plugin.getDescription().getVersion();

        if (disabled) {
            result = UpdateResult.DISABLED;
            return;
        }

        if (oldVersion.contains("SNAPSHOT")) {
            result = UpdateResult.SNAPSHOT_DISABLED;
            return;
        }

        try {
            final String query = String.format("?resource=%s", URLEncoder.encode(RESOURCE_ID, StandardCharsets.UTF_8.toString()));
            connection = (HttpURLConnection) new URL(HOST + PATH + query).openConnection();
        } catch (IOException e) {
            result = UpdateResult.FAIL_SPIGOT;
            plugin.getLogger().severe("Failed to open the connection to SpigotMC.");
            e.printStackTrace();
            return;
        }
        run();
    }

    private void run() {
        connection.setDoOutput(true);
        try {
            connection.setRequestMethod(REQUEST_METHOD);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to open the connection to SpigotMC.");
            e.printStackTrace();
            result = UpdateResult.FAIL_SPIGOT;
        }
        String newVersion;
        try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {
            newVersion = br.readLine();
        } catch (IOException e) {
            result = UpdateResult.FAIL_NOVERSION;
            plugin.getLogger().severe("Failed to read the version.");
            e.printStackTrace();
            return;
        }
        if (newVersion == null) {
            plugin.getLogger().severe("Failed to read the version, it's null.");
            result = UpdateResult.FAIL_NOVERSION;
            return;
        }
        // Check for "magic string"
        if ("Invalid resource".equalsIgnoreCase(newVersion)) {
            result = UpdateResult.BAD_RESOURCEID;
            return;
        } else if ("Invalid access key".equalsIgnoreCase(newVersion)) {
            result = UpdateResult.BAD_API_KEY;
            return;
        }

        this.version = newVersion.replace("[^A-Za-z]", "").replace("|", "");
        versionCheck();
    }

    private void versionCheck() {
        result = shouldUpdate(oldVersion, version) ? UpdateResult.UPDATE_AVAILABLE : UpdateResult.NO_UPDATE;
    }

    public boolean shouldUpdate(String localVersion, String remoteVersion) {
        return !localVersion.equalsIgnoreCase(remoteVersion);
    }
}
