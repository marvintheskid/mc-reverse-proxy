package gbx.proxy.utils;

import java.net.InetSocketAddress;

/**
 * A simple server address object holding the host and port.
 *
 * @param host the hostname
 * @param port the port
 */
public record ServerAddress(String host, int port) {
    public InetSocketAddress toInetAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public String toString() {
        return host + (port != 25565 ? (":" + port) : "");
    }
}