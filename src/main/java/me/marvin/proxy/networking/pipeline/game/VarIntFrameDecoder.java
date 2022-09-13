package me.marvin.proxy.networking.pipeline.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

import static me.marvin.proxy.utils.ByteBufUtils.readVarInt;

public class VarIntFrameDecoder extends ByteToMessageDecoder {
    private static final CorruptedFrameException WIDER_FRAME = new CorruptedFrameException("Length wider than 21-bit");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!ctx.channel().isActive()) {
            in.skipBytes(in.readableBytes());
            return;
        }

        in.markReaderIndex();
        byte[] buffer = new byte[3];

        for (int i = 0; i < buffer.length; ++i) {
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;
            }

            if ((buffer[i] = in.readByte()) >= 0) {
                ByteBuf buf = Unpooled.wrappedBuffer(buffer);

                try {
                    int len = readVarInt(buf);

                    if (in.readableBytes() >= len) {
                        out.add(in.readBytes(len));
                        return;
                    }

                    in.resetReaderIndex();
                } finally {
                    buf.release();
                }

                return;
            }
        }

        throw WIDER_FRAME;
    }
}
