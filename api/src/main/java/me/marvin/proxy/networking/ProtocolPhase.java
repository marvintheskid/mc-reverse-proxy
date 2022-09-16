package me.marvin.proxy.networking;

/**
 * Represents the 4 protocol stages.
 */
public enum ProtocolPhase {
    HANDSHAKE,
    PLAY,
    STATUS,
    LOGIN;

    /**
     * Returns the identifier of this stage.
     *
     * @return the identifier
     */
    public int id() {
        return ordinal() - 1;
    }
}
