package me.marvin.proxy.addon;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.marvin.proxy.networking.PacketListener;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.utils.Tristate;
import org.jetbrains.annotations.NotNull;

/**
 * A simple record wrapping a {@link PacketListener}.
 *
 * @param owner the owner
 * @param listener the listener
 */
record RegisteredPacketListener(Object owner, PacketListener listener) implements PacketListener {
    @Override
    public byte priority() {
        return listener.priority();
    }

    @Override
    public Tristate handle(PacketType type, ByteBuf buf, Channel sender, ChannelHandlerContext receiver, Version version, Tristate cancelled) {
        return listener.handle(type, buf, sender, receiver, version, cancelled);
    }

    @Override
    public int compareTo(@NotNull PacketListener o) {
        return listener.compareTo(o);
    }
}