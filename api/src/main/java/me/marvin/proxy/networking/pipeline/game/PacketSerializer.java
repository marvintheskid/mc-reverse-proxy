package me.marvin.proxy.networking.pipeline.game;

import me.marvin.proxy.networking.Keys;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static me.marvin.proxy.utils.ByteBufUtils.writeVarInt;

/**
 * Simple encoder for {@link Packet packets}.
 */
public class PacketSerializer extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
        Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
        writeVarInt(out, msg.type().id(version));
        msg.encode(out, version);
    }
}
