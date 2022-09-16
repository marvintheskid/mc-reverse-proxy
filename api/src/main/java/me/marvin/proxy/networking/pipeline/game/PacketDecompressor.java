package me.marvin.proxy.networking.pipeline.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;
import java.util.zip.Inflater;

import static me.marvin.proxy.utils.ByteBufUtils.readVarInt;

public class PacketDecompressor extends ByteToMessageDecoder {
    private static final int PACKET_LIMIT = 1024 * 1024 * 2; // 2 MB

    private final Inflater inflater;
    private final int threshold;

    public PacketDecompressor(int threshold) {
        this.threshold = threshold;
        this.inflater = new Inflater();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() != 0) {
            int size = readVarInt(in);

            if (size == 0) {
                out.add(in.readBytes(in.readableBytes()));
            } else {
                if (size < threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + size + " is below server threshold of " + threshold);
                }

                if (size > PACKET_LIMIT) {
                    throw new DecoderException("Badly compressed packet - size of " + size + " is larger than protocol maximum of " + PACKET_LIMIT);
                }

                byte[] input = new byte[in.readableBytes()];
                in.readBytes(input);
                inflater.setInput(input);

                byte[] output = new byte[size];
                inflater.inflate(output);
                out.add(Unpooled.wrappedBuffer(output));
                inflater.reset();
            }
        }
    }
}
