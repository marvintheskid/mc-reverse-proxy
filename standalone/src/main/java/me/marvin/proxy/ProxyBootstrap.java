package me.marvin.proxy;

import io.netty.channel.epoll.Epoll;
import me.marvin.proxy.addon.ProxyAddonHandler;

import java.io.IOException;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    private static ProxyAddonHandler ADDON_HANDLER;

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = Integer.getInteger("port", 25565);
        String targetAddr = System.getProperty("target", ":25566");

        if (Epoll.isAvailable()) {
            System.out.println("[!] Using epoll...");
        }

        Proxy proxy = new Proxy(port, targetAddr);
        System.out.println("[?] Resolving address... (" + targetAddr + ")");
        System.out.println("[!] Resolved server address: " + proxy.address());
        ADDON_HANDLER = new ProxyAddonHandler(proxy);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ADDON_HANDLER.stop()));
        proxy.start();
    }
}