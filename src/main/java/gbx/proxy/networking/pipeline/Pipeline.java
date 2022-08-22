package gbx.proxy.networking.pipeline;

/**
 * Pipeline-related constants.
 */
public interface Pipeline {
    /**
     * The identifier of the packet handler.
     */
    String PACKET_HANDLER = "packet-handler";

    /**
     * The identifier of the encrypter.
     */
    String FRAME_DECODER = "frame-decoder";

    /**
     * The identifier of the decrypter.
     */
    String FRAME_ENCODER = "frame-encoder";

    /**
     * The identifier of the compressor.
     */
    String COMPRESSOR = "compressor";

    /**
     * The identifier of the decompressor.
     */
    String DECOMPRESSOR = "decompressor";

    /**
     * The identifier of the encrypter.
     */
    String ENCRYPTER = "encrypter";

    /**
     * The identifier of the decrypter.
     */
    String DECRYPTER = "decrypter";
}
