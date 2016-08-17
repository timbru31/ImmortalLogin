package org.json;

import de.dustplanet.immortallogin.ImmortalLogin;

public class HTTPSTokener {
    private ImmortalLogin plugin;

    public HTTPSTokener(ImmortalLogin plugin) {
        this.plugin = plugin;
    }

    public int sendHTTPSToken(String data) throws HTTPTokenException {
        JSONReader jsonReader = new JSONReader(plugin);
        return jsonReader.sendPost(data);
    }
}
