package me.marvin.proxy;

import io.netty.channel.epoll.Epoll;
import me.marvin.proxy.addon.ProxyAddonHandler;
import net.minecrell.terminalconsole.SimpleTerminalConsole;

import java.io.IOException;

public class InteractiveProxy extends SimpleTerminalConsole {
    private boolean isRunning;
    private final Proxy proxy;
    private final ProxyAddonHandler addonHandler;

    public InteractiveProxy(int port, String targetAddr) throws IOException {
        this.isRunning = true;

        if (Epoll.isAvailable()) {
            System.out.println("[!] Using epoll...");
        }

        proxy = new Proxy(port, targetAddr);
        addonHandler = new ProxyAddonHandler(proxy);
        System.out.println("[?] Resolving address... (" + targetAddr + ")");
        System.out.println("[!] Resolved server address: " + proxy.address());

        new Thread(() -> {
            try {
                proxy.start(f -> {
                    if (f.isSuccess()) {
                        System.out.println("[!] Listening on " + f.channel().localAddress());
                    } else {
                        System.out.println("[-] Failed to bind on " + f.channel().localAddress());
                        f.cause().printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    protected boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void runCommand(String command) {
        System.out.println("Unknown command '" + command + "'.");
    }

    @Override
    protected void shutdown() {
        System.out.println("Shutting down...");
        isRunning = false;
        addonHandler.stop();

        try {
            proxy.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Goodbye!");
    }
}
