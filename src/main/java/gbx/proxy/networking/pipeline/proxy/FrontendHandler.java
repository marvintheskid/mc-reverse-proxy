package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.Keys;
import gbx.proxy.networking.ProtocolDirection;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.networking.pipeline.game.PacketCompressor;
import gbx.proxy.networking.pipeline.game.PacketDecompressor;
import gbx.proxy.utils.AttributeUtils;
import gbx.proxy.utils.IndexRollback;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;

import static gbx.proxy.utils.ByteBufUtils.readVarInt;

/**
 * The frontend handler.
 */
public class FrontendHandler extends ChannelDuplexHandler {
    private final Channel backend;

    public FrontendHandler(Channel backend) {
        this.backend = backend;
    }

    /**
     * Handles client to proxy traffic.
     */
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (backend.isActive()) {
            backend.writeAndFlush(msg);
        } else {
            backend.write(msg);
        }
    }

    /**
     * Handles proxy to server traffic.
     *
     * @apiNote the {@link Channel} returned by {@code ctx} is the <b>frontend</b> connection.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Channel frontend = ctx.channel();
        if (msg instanceof ByteBuf buf) {
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
                PacketType type = PacketTypes.find(ProtocolDirection.SERVER, phase, id, version);

                if (PacketTypes.Login.Server.SET_COMPRESSION == type) {
                    int threshold = readVarInt(buf);
                    System.out.println("[+] Enabling compression for " + frontend.remoteAddress() + " (compression after: " + threshold + ")");

                    backend.pipeline()
                        .addAfter(backend.pipeline().get(Pipeline.DECRYPTER) != null ? Pipeline.DECRYPTER : Pipeline.FRAME_ENCODER, Pipeline.DECOMPRESSOR, new PacketDecompressor(threshold))
                        .addAfter(Pipeline.DECOMPRESSOR, Pipeline.COMPRESSOR, new PacketCompressor(threshold));

                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.LOGIN_SUCCESS == type) {
                    AttributeUtils.update(Keys.PHASE_KEY, ProtocolPhase.PLAY, frontend, backend);
                }
            }
            super.write(ctx, msg, promise);
        }
    }
}
