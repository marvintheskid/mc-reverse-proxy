package gbx.proxy.utils;

import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Address resolver utils.
 */
public interface AddressResolver {
    /**
     * Resolves the given address using SRV records.
     *
     * @param address the address
     * @return the resolved address as a tuple
     */
    static ServerAddress getServerAddress(String address) {
        assert address != null : "address was null";
        String[] parts = address.split(":");

        if (parts.length > 2) {
            parts = new String[]{address};
        }

        String host = parts[0];
        int port = port(parts.length > 1 ? parts[1] : null);

        if (port == 25565) {
            try {
                Class.forName("com.sun.jndi.dns.DnsContextFactory");

                Hashtable<String, String> env = new Hashtable<>();
                env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                env.put("java.naming.provider.url", "dns:");
                env.put("com.sun.jndi.dns.timeout.retries", "1");

                DirContext ctx = new InitialDirContext(env);
                Attributes attr = ctx.getAttributes("_minecraft._tcp." + address, new String[]{"SRV"});
                String[] srv = attr.get("srv").get().toString().split(" ", 4);

                return new ServerAddress(srv[3], port(srv[2]));
            } catch (Throwable throwable) {
                return new ServerAddress(address, 25565);
            }
        }

        return new ServerAddress(host, port);
    }

    /**
     * Tries to parse the given port, or returns 25565.
     *
     * @param val the string
     * @return a port ranging from 0-65535
     */
    private static int port(String val) {
        try {
            int parsed = Integer.parseInt(val);
            if (parsed < 0 || parsed > 65535) {
                return 25565;
            }
            return parsed;
        } catch (Exception ex) {
            return 25565;
        }
    }
}
