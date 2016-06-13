package de.dustplanet.immortallogin.piracy.checker;

public class BlackListedException extends Exception {
    private static final long serialVersionUID = 8557399085726848324L;

    public BlackListedException() {
        super();
    }

    public BlackListedException(String message) {
        super(message);
    }

    public BlackListedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlackListedException(Throwable cause) {
        super(cause);
    }
}
