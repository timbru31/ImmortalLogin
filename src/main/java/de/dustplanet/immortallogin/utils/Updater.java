package de.dustplanet.immortallogin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * Based on the Updater of PatoTheBest. Thanks for sharing this class.
 *
 * @author PatoTheBest
 * @author timbru31
 */

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class Updater {
    private static final String REQUEST_METHOD = "GET";
    private static final String HOST = "https://api.spigotmc.org";
    private static final String PATH = "/legacy/update.php";
    private final JavaPlugin plugin;

    @Getter
    private String version;
    private final String oldVersion;

    @Getter
    private Updater.UpdateResult result = Updater.UpdateResult.DISABLED;

    private HttpURLConnection connection;

    /**
     * The possible updater results.
     *
     * @author timbru31
     */
    public enum UpdateResult {
        NO_UPDATE, DISABLED, FAIL_SPIGOT, FAIL_NOVERSION, BAD_API_KEY, BAD_RESOURCEID, UPDATE_AVAILABLE, SNAPSHOT_DISABLED
    }

    /**
     * New Updater instance that checks Spigot's API for a newer version.
     *
     * @param plugin The plugin instanced used for a version check and logging
     * @param resourceId The Spigot assigned resource ID
     * @param disabled Whether the updater should be disabled and not run
     */
    @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
    @SuppressWarnings("checkstyle:ReturnCount")
    public Updater(final JavaPlugin plugin, final Integer intResourceId, final boolean disabled) {
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

        final String resourceId = Integer.toString(intResourceId);
        try {
            final String query = String.format("?resource=%s", URLEncoder.encode(resourceId, StandardCharsets.UTF_8.toString()));
            connection = (HttpURLConnection) new URL(HOST + PATH + query).openConnection();
        } catch (final IOException e) {
            result = UpdateResult.FAIL_SPIGOT;
            plugin.getLogger().log(Level.SEVERE, "Failed to open the connection to SpigotMC", e);
            return;
        }
        run();
    }

    @SuppressWarnings({ "checkstyle:ReturnCount", "PMD.DataflowAnomalyAnalysis" })
    private void run() {
        connection.setDoOutput(true);
        try {
            connection.setRequestMethod(REQUEST_METHOD);
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to open the connection to SpigotMC.", e);
            result = UpdateResult.FAIL_SPIGOT;
        }
        final String newVersion;
        try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(isr)) {
            newVersion = bufferedReader.readLine();
        } catch (final IOException e) {
            result = UpdateResult.FAIL_NOVERSION;
            plugin.getLogger().log(Level.SEVERE, "Failed to read the version.", e);
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

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    private void versionCheck() {
        result = shouldUpdate(oldVersion, version) ? UpdateResult.UPDATE_AVAILABLE : UpdateResult.NO_UPDATE;
    }

    @SuppressWarnings("static-method")
    private boolean shouldUpdate(final String localVersion, final String remoteVersion) {
        return !localVersion.equalsIgnoreCase(remoteVersion);
    }
}
