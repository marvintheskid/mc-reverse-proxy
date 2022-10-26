package me.marvin.proxy;

import io.netty.channel.epoll.Epoll;
import me.marvin.proxy.addon.ProxyAddonHandler;
import me.marvin.proxy.commands.impl.CommandTree;
import me.marvin.proxy.utils.*;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;

public class InteractiveProxy extends SimpleTerminalConsole {
    private volatile boolean isRunning;
    private final Proxy proxy;
    private final ProxyAddonHandler addonHandler;
    private final Logger logger;
    private final CommandTree commandTree;

    public InteractiveProxy(int port, String targetAddr) throws IOException {
        proxy = new Proxy(port, targetAddr);
        logger = proxy.logger();
        if (Epoll.isAvailable()) {
            proxy.logger().info("Using epoll...");
        }
        logger.info("Resolving address... ({})", targetAddr);
        logger.info("Resolved server address: {}", proxy.address());
        commandTree = new CommandTree();
        registerBuiltinCommands();
        addonHandler = new ProxyAddonHandler(proxy, commandTree);
    }

    private void registerBuiltinCommands() {
        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: setip [ip]");
                return false;
            }

            ServerAddress prev = proxy.address();
            proxy.address(args[0]);
            logger.info("Changed address: '{}' -> '{}'", prev, proxy.address());
            return true;
        }, "setip", "ip");

        commandTree.register(args -> {
            shutdown();
            return true;
        }, "shutdown", "goodbye", "stop");

        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: setname [name]");
                return false;
            }

            proxy.name(args[0]);
            logger.info("Set name to: '{}'", proxy.name());
            return true;
        }, "setname");

        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: setuuid [uuid]");
                return false;
            }

            proxy.uuid(args[0].replace("-", ""));
            logger.info("Set uuid to: '{}'", proxy.uuid());
            return true;
        }, "setuuid");

        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: settoken [access token]");
                return false;
            }

            proxy.accessToken(args[0]);
            logger.info("Set access token to: '{}'", proxy.accessToken());
            return true;
        }, "settoken");

        commandTree.register(args -> {
            logger.info("Current credentials:");
            logger.info(" Name: '{}'", proxy.name());
            logger.info(" UUID: '{}'", proxy.uuid());
            logger.info(" Token: '{}'", proxy.accessToken());
            return true;
        }, "credentials");

        commandTree.register(args -> {
            StringBuilder threadDump = new StringBuilder(System.lineSeparator());
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
                threadDump.append(threadInfo.toString());
            }
            logger.info(threadDump.toString());
            return true;
        }, "threaddump");
    }

    @Override
    public void start() {
        CountDownLatch lock = new CountDownLatch(1);

        new Thread(() -> {
            try {
                proxy.start(f -> {
                    if (f.isSuccess()) {
                        logger.info("Listening on {}", f.channel().localAddress());
                        isRunning = true;
                        lock.countDown();
                    } else {
                        logger.fatal("Failed to bind on :{}", proxy.port(), f.cause());
                        lock.countDown();
                    }
                });
            } catch (InterruptedException e) {
                logger.fatal("Interrupted while starting up the proxy", e);
                lock.countDown();
            }
        }).start();

        try {
            lock.await();
            if (isRunning()) {
                super.start();
            } else {
                shutdown();
            }
        } catch (InterruptedException e) {
            logger.fatal("Interrupted while starting up the console", e);
            shutdown();
        }
    }

    @Override
    protected boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void runCommand(String command) {
        if (commandTree.execute(command) == Tristate.NOT_SET) {
            logger.info("Unknown command '{}'", command);
        }
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
