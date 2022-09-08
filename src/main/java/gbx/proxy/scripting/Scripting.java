package gbx.proxy.scripting;

/**
 * Scripting related constants.
 */
public interface Scripting {
    /**
     * The initializer functions name. It takes 0 parameters, and is called on proxy startup.
     */
    String INITIALIZER = "initialize";

    /**
     * The client-to-server packet handler functions name. It takes 6 parameters.
     */
    String CLIENT_TO_SERVER = "clientToServer";

    /**
     * The server-to-client packet handler functions name. It takes 6 parameters.
     */
    String SERVER_TO_CLIENT = "serverToClient";
}
