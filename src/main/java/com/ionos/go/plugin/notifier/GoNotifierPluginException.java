package com.ionos.go.plugin.notifier;

/**
 * Application wide runtime exception.
 */
public class GoNotifierPluginException extends RuntimeException {
    public GoNotifierPluginException(String message) {
        super(message);
    }

    public GoNotifierPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoNotifierPluginException(Throwable cause) {
        super(cause);
    }
}
