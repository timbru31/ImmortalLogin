package org.json;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import de.dustplanet.immortallogin.ImmortalLogin;

public class JSONReader {
    private static final int SERVER_ERROR = 500;
    private ImmortalLogin plugin;

    public JSONReader(ImmortalLogin plugin) {
        this.plugin = plugin;
    }

    // HTTP POST request
    public int sendPost(String userId) throws HTTPTokenException {
        // URL
        URL url = null;
        try {
            url = new URL("https://api.dustplanet.de/");
        } catch (MalformedURLException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return -1;
        }

        // HTTP Connection
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return -1;
        }

        // Get user id
        String rawData = "user_id=";
        String encodedData = null;
        try {
            encodedData = rawData + URLEncoder.encode(userId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return -1;
        }

        // Make POST request
        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return -1;
        }
        con.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Bukkit-Server-Port", String.valueOf(plugin.getServer().getPort()));

        // Send POST request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(encodedData.getBytes("UTF-8"));
            wr.flush();
            wr.close();
        } catch (IOException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return -1;
        }

        // Get response
        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return responseCode;
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        if (responseCode >= SERVER_ERROR) {
            return responseCode;
        } else if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
                return responseCode;
            }
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
                return responseCode;
            }
        }
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(response.toString());
        } catch (JSONException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return responseCode;
        }
        boolean blacklisted = responseJSON.getBoolean("blacklisted");
        if (blacklisted) {
            disableDueToError("You are blacklisted...");
        }
        return responseCode;
    }

    private void disableDueToError(String... messages) throws HTTPTokenException {
        for (String message : messages) {
            plugin.getLogger().severe(message);
        }
        throw new HTTPTokenException();
    }
}
