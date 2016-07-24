package org.json;

import de.dustplanet.immortallogin.ImmortalLogin;

public class HTTPSTokener {
    private ImmortalLogin plugin;

    public HTTPSTokener(ImmortalLogin plugin) {
        this.plugin = plugin;
    }

    public int checkPiracy(String userId) throws HTTPTokenException {
        JSONReader piracyChecker = new JSONReader(plugin);
        return piracyChecker.sendPost(userId);
    }
}
