package me.marvin.proxy.networking.packet;

import me.marvin.proxy.networking.Version;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet.
 */
public interface Packet {
    /**
     * Encodes this packet using the given protocol version.
     *
     * @param buf the target buffer
     * @param version the version
     */
    void encode(@NotNull ByteBuf buf, @NotNull Version version);

    /**
     * Decodes this packet using the given protocol version.
     *
     * @param buf the source buffer
     * @param version the version
     */
    void decode(@NotNull ByteBuf buf, @NotNull Version version);

    /**
     * Returns the type of this packet.
     *
     * @return the type of this packet
     */
    @NotNull
    PacketType type();
}
