package gbx.proxy.networking;

import io.netty.util.AttributeKey;

import javax.crypto.SecretKey;

/**
 * Pipeline attribute keys.
 */
public interface Keys {
    /**
     * The key for the current protocol stage.
     */
    AttributeKey<ProtocolPhase> PHASE_KEY = AttributeKey.valueOf("protocol-phase");

    /**
     * The key for the protocol version.
     */
    AttributeKey<Version> VERSION_KEY = AttributeKey.valueOf("protocol-version");
}
