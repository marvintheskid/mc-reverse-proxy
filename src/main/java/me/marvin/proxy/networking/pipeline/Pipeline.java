package me.marvin.proxy.networking.pipeline;

/**
 * Pipeline-related constants.
 */
public interface Pipeline {
    /**
     * The identifier of the frontend handler.
     */
    String FRONTEND_HANDLER = "frontend-handler";

    /**
     * The identifier of the backend handler.
     */
    String BACKEND_HANDLER = "backend-handler";

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

    /**
     * The identifier of the packet serializer.
     */
    String PACKET_SERIALIZER = "packet-serializer";
}
