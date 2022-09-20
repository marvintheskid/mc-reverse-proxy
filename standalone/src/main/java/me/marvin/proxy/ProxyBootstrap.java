package me.marvin.proxy;

import me.marvin.proxy.utils.Loggers;

import java.io.IOException;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    public static void main(String[] args) throws IOException {
        Loggers.setupForwarding();

        int port = Integer.getInteger("port", 25565);
        String targetAddr = System.getProperty("target", ":25566");

        InteractiveProxy instance = new InteractiveProxy(port, targetAddr);
        instance.start();
    }
}