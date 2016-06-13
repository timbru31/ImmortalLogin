package de.dustplanet.immortallogin.piracy.task;

import de.dustplanet.immortallogin.ImmortalLogin;
import de.dustplanet.immortallogin.piracy.checker.BlackListedException;
import de.dustplanet.immortallogin.piracy.checker.ImmortalLoginPiracyChecker;

public class ImmortalLoginPiracyTask {
    private ImmortalLogin plugin;

    public ImmortalLoginPiracyTask(ImmortalLogin plugin) {
        this.plugin = plugin;
    }

    public int checkPiracy() throws BlackListedException {
        ImmortalLoginPiracyChecker piracyChecker = new ImmortalLoginPiracyChecker(plugin);
        return piracyChecker.sendPost();
    }
}
