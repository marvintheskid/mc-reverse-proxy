package gbx.proxy.networking;

import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.utils.Tristate;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

/**
 * An interface what represents a low-level packet listener.
 * This listener doesn't deserialize packets by default, but rather resolves the type of the
 * packet.
 */
public interface PacketListener extends Comparable<PacketListener> {
    /**
     * Returns the priority of this listener.
     *
     * @return the priority
     */
    byte priority();

    /**
     * Handles the packet invoked on this {@link PacketListener}. It gets invoked by the
     * {@link #handle(PacketType, ByteBuf, ChannelHandlerContext, Version, Tristate)} method by default.
     * If you decide to use this method instead of the parent method, you must only override this method.
     *
     * @param type    the packet type
     * @param buf     the buffer
     * @param context the netty context
     * @param version the version
     * @return false if the server should consume the packet, otherwise true
     */
    default Tristate handle(PacketType type, ByteBuf buf, ChannelHandlerContext context, Version version) {
        return Tristate.NOT_SET;
    }

    /**
     * Handles the packet invoked on this {@link PacketListener}.
     *
     * @param type      the packet type
     * @param buf       the buffer
     * @param context   the netty context
     * @param version   the version
     * @param cancelled the current state of the invocation
     * @return false if the server should consume the packet, otherwise true
     */
    default Tristate handle(PacketType type, ByteBuf buf, ChannelHandlerContext context, Version version, Tristate cancelled) {
        return handle(type, buf, context, version);
    }

    /**
     * {@inheritDoc}
     *
     * @param o the object to be compared.
     * @return the value {@code 0} if {@code x == y};
     * a value less than {@code 0} if {@code x < y}; and
     * a value greater than {@code 0} if {@code x > y}
     */
    @Override
    default int compareTo(@NotNull PacketListener o) {
        return Byte.compare(priority(), o.priority());
    }
}