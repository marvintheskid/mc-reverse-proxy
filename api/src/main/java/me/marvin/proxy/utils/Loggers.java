package me.marvin.proxy.utils;

import org.apache.logging.log4j.Logger;

/**
 * {@link Logger}-related utils.
 */
public interface Loggers {
    /**
     * Sets up standard output and error forwarding.
     */
    static void setupForwarding() {
        System.setOut(new RedirectStream(Logger::info, System.out));
        System.setErr(new RedirectStream(Logger::error, System.err));
    }
}
