package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.Keys;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.networking.pipeline.game.VarIntFrameDecoder;
import gbx.proxy.networking.pipeline.game.VarIntFrameEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.jetbrains.annotations.NotNull;

/**
 * A shared channel initializer.
 */
public final class DefaultChannelInitializer extends ChannelInitializer<Channel> {
    public static final DefaultChannelInitializer INSTANCE = new DefaultChannelInitializer();

    public void init(@NotNull Channel ch) {
        initChannel(ch);
    }

    @Override
    protected void initChannel(@NotNull Channel ch) {
        ch.attr(Keys.PHASE_KEY).set(ProtocolPhase.HANDSHAKE);
        ch.attr(Keys.VERSION_KEY).set(Version.V1_8);

        ch.pipeline()
            .addLast(Pipeline.FRAME_DECODER, new VarIntFrameEncoder())
            .addLast(Pipeline.FRAME_ENCODER, new VarIntFrameDecoder());
    }
}
