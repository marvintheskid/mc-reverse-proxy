package me.marvin.proxy.networking;

/**
 * Represents the 2 possible packet directions.
 */
public enum ProtocolDirection {
    /**
     * Serverbound direction.
     * Client -> Server
     */
    CLIENT,

    /**
     * Clientbound direction.
     * Server -> Client
     */
    SERVER
}
