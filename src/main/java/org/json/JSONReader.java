package org.json;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

import de.dustplanet.immortallogin.ImmortalLogin;

public class JSONReader {
    private static final int TIMEOUT = 5000;
    private static final int SERVER_ERROR = 500;
    private ImmortalLogin plugin;

    public JSONReader(ImmortalLogin plugin) {
        this.plugin = plugin;
    }

    public int sendPost(String userId) throws HTTPTokenException {
        return sendPost(userId, "https://api.dustplanet.de/", true);
    }

    public int sendPost(String userId, String apiHost, boolean useSSL) throws HTTPTokenException {
        // URL
        URL url = null;
        try {
            url = new URL(apiHost);
        } catch (MalformedURLException e) {
            disableDueToError("An error occurred, disabling ImmortalLogin (1)");
            return -1;
        }

        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();

        } catch (IOException e) {
            disableDueToError("An error occurred, disabling ImmortalLogin (2)");
            return -1;
        }

        String serverPort = String.valueOf(plugin.getServer().getPort());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", userId);
        jsonObject.put("port", serverPort);
        jsonObject.put("plugin", plugin.getDescription().getFullName());
        String data = jsonObject.toString();

        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            disableDueToError("An error occurred, disabling ImmortalLogin (3)");
            return -1;
        }
        con.setRequestProperty("Content-Length", String.valueOf(data.length()));
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Bukkit-Server-Port", serverPort);
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(data.getBytes("UTF-8"));
            wr.flush();
        } catch (UnknownHostException e) {
            return -1;
        } catch (IOException e) {
            if (useSSL) {
                return sendPost(userId, "http://api.dustplanet.de/", false);
            }
            disableDueToError("An error occurred, disabling ImmortalLogin (4)");
            return -1;
        }

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
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
                disableDueToError("An error occurred, disabling ImmortalLogin (5)");
                return responseCode;
            }
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occurred, disabling ImmortalLogin (6)");
                return responseCode;
            }
        }
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(response.toString());
        } catch (JSONException e) {
            disableDueToError("An error occurred, disabling ImmortalLogin (7)");
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
