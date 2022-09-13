package me.marvin.proxy;

import io.netty.channel.epoll.Epoll;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    public static void main(String[] args) throws InterruptedException {
        int port = Integer.getInteger("port", 25565);
        String targetAddr = System.getProperty("target", ":25566");

        if (Epoll.isAvailable()) {
            System.out.println("[!] Using epoll...");
        }

        Proxy proxy = new Proxy(port, targetAddr);
        System.out.println("[?] Resolving address... (" + targetAddr + ")");
        System.out.println("[!] Resolved server address: " + proxy.address());
        proxy.start();
    }
}