package me.marvin.proxy.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A simple {@link PrintStream} what delegates its calls to a {@link Logger}.
 */
public class RedirectStream extends PrintStream {
    private static final Logger LOGGER = LogManager.getLogger("forwarding");
    private static final Marker FORWARDED = MarkerManager.getMarker("FORWARDED");
    private final TriConsumer<Logger, Marker, String> consumer;

    public RedirectStream(TriConsumer<Logger, Marker, String> consumer, OutputStream out) {
        super(out);
        this.consumer = consumer;
    }

    /**
     * {@inheritDoc}
     *
     * @param string The {@code String} to be printed.
     */
    @Override
    public void println(@Nullable String string) {
        logLine(string);
    }

    /**
     * {@inheritDoc}
     *
     * @param object The {@code Object} to be printed.
     */
    @Override
    public void println(@Nullable Object object) {
        logLine(String.valueOf(object));
    }

    private void logLine(@Nullable String message) {
        consumer.accept(LOGGER, FORWARDED, message);
    }
}