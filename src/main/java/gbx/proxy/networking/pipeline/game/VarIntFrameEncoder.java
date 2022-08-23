package gbx.proxy.networking.pipeline.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static gbx.proxy.utils.ByteBufUtils.calculateVarIntSize;
import static gbx.proxy.utils.ByteBufUtils.writeVarInt;

public class VarIntFrameEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        int len = in.readableBytes();
        int headerLen = calculateVarIntSize(len);

        out.ensureWritable(headerLen + len);
        writeVarInt(out, len);
        out.writeBytes(in);
    }
}
