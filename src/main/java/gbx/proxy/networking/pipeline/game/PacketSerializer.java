package gbx.proxy.networking.pipeline.game;

import gbx.proxy.networking.Keys;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static gbx.proxy.utils.ByteBufUtils.writeVarInt;

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
