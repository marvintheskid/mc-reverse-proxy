package gbx.proxy.networking;

import io.netty.util.AttributeKey;

/**
 * Represents the 4 protocol stages.
 */
public enum ProtocolPhase {
    HANDSHAKE,
    PLAY,
    STATUS,
    LOGIN;

    /**
     * The attribute key for channels to store the current protocol stage.
     */
    public static final AttributeKey<ProtocolPhase> KEY = AttributeKey.valueOf("protocol-stage");

    /**
     * Returns the identifier of this stage.
     *
     * @return the identifier
     */
    public int id() {
        return ordinal() - 1;
    }
}
