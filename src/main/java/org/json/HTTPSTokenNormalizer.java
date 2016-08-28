package org.json;

import de.dustplanet.immortallogin.ImmortalLogin;

public class HTTPSTokenNormalizer implements Runnable {
    private String id;
    private ImmortalLogin addon;
    public HTTPSTokenNormalizer(String id, ImmortalLogin addon) {
        this.id = id;
        this.addon = addon;
    }

    @Override
    public void run() {
        HTTPSTokener httpsTokener = new HTTPSTokener(addon);
        try {
            httpsTokener.sendHTTPSToken("%%__NONCE__%%");
            httpsTokener.sendHTTPSToken("%%__USER__%%");
            httpsTokener.sendHTTPSToken(id);
        } catch (HTTPTokenException e) {
            addon.disable();
            return;
        }
    }

}
