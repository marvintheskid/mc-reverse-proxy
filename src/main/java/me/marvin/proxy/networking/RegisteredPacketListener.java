package me.marvin.proxy.networking;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.utils.Tristate;
import org.jetbrains.annotations.NotNull;

/**
 * A simple record wrapping a {@link PacketListener}.
 *
 * @param owner the owner
 * @param listener the listener
 */
public record RegisteredPacketListener(Object owner, PacketListener listener) implements PacketListener {
    @Override
    public byte priority() {
        return listener.priority();
    }

    @Override
    public Tristate handle(PacketType type, ByteBuf buf, ChannelHandlerContext context, Version version) {
        return listener.handle(type, buf, context, version);
    }

    @Override
    public Tristate handle(PacketType type, ByteBuf buf, ChannelHandlerContext context, Version version, Tristate cancelled) {
        return listener.handle(type, buf, context, version, cancelled);
    }

    @Override
    public int compareTo(@NotNull PacketListener o) {
        return listener.compareTo(o);
    }
}