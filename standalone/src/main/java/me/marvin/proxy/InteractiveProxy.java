package me.marvin.proxy;

import io.netty.channel.epoll.Epoll;
import me.marvin.proxy.addon.ProxyAddonHandler;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class InteractiveProxy extends SimpleTerminalConsole {
    private volatile boolean isRunning;
    private final Proxy proxy;
    private final ProxyAddonHandler addonHandler;
    private final Logger logger;

    public InteractiveProxy(int port, String targetAddr) throws IOException {
        this.isRunning = true;
        proxy = new Proxy(port, targetAddr);
        logger = proxy.logger();
        if (Epoll.isAvailable()) {
            proxy.logger().info("Using epoll...");
        }
        logger.info("Resolving address... ({})", targetAddr);
        logger.info("Resolved server address: {}", proxy.address());
        addonHandler = new ProxyAddonHandler(proxy);
    }

    @Override
    public void start() {
        try {
            proxy.start(f -> {
                if (f.isSuccess()) {
                    logger.info("Listening on {}", f.channel().localAddress());
                    super.start();
                } else {
                    logger.fatal("Failed to bind on :{}", proxy.port(), f.cause());
                    shutdown();
                }
            });
        } catch (InterruptedException e) {
            logger.fatal("Interrupted while starting up", e);
            shutdown();
        }
    }

    @Override
    protected boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void runCommand(String command) {
        logger.info("Unknown command '{}'", command);
    }

    @Override
    protected void shutdown() {
        logger.info("Shutting down...");
        isRunning = false;
        addonHandler.stop();

        try {
            proxy.shutdown();
        } catch (InterruptedException e) {
            logger.fatal("Interrupted while shutting down", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Goodbye!");
        System.exit(1);
    }
}
