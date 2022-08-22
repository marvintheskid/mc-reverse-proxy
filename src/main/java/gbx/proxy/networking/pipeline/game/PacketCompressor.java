package gbx.proxy.networking.pipeline.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.zip.Deflater;

import static gbx.proxy.utils.ByteBufUtils.writeVarInt;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf> {
    private final int threshold;
    private final Deflater deflater;
    private final byte[] bytes = new byte[8192];

    public PacketCompressor(int threshold) {
        this.threshold = threshold;
        this.deflater = new Deflater();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        int readable = in.readableBytes();

        if (readable < threshold) {
            writeVarInt(out, 0);
            out.writeBytes(in);
        } else {
            byte[] arr = new byte[readable];
            in.readBytes(arr);
            writeVarInt(out, arr.length);

            deflater.setInput(arr, 0, readable);
            deflater.finish();

            while (!deflater.finished()) {
                int deflated = deflater.deflate(bytes);
                out.writeBytes(bytes, 0, deflated);
            }

            deflater.reset();
        }
    }
}