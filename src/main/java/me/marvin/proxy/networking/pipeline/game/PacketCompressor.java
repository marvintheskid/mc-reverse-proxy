package me.marvin.proxy.networking.pipeline.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.zip.Deflater;

import static me.marvin.proxy.utils.ByteBufUtils.writeVarInt;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf> {
    private final int threshold;
    private final Deflater deflater;
    private final byte[] buffer = new byte[8192];

    public PacketCompressor(int threshold) {
        this.threshold = threshold;
        this.deflater = new Deflater();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        int size = in.readableBytes();

        if (size < threshold) {
            writeVarInt(out, 0);
            out.writeBytes(in);
        } else {
            byte[] input = new byte[size];
            in.readBytes(input);
            writeVarInt(out, input.length);

            deflater.setInput(input, 0, size);
            deflater.finish();

            while (!deflater.finished()) {
                int deflatedSize = deflater.deflate(buffer);
                out.writeBytes(buffer, 0, deflatedSize);
            }

            deflater.reset();
        }
    }
}