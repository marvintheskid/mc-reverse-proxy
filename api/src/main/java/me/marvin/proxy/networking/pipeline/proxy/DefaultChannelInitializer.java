package me.marvin.proxy.networking.pipeline.proxy;

import me.marvin.proxy.networking.Keys;
import me.marvin.proxy.networking.ProtocolPhase;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.pipeline.Pipeline;
import me.marvin.proxy.networking.pipeline.game.PacketSerializer;
import me.marvin.proxy.networking.pipeline.game.VarIntFrameDecoder;
import me.marvin.proxy.networking.pipeline.game.VarIntFrameEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.jetbrains.annotations.NotNull;

/**
 * A shared channel initializer.
 */
public class DefaultChannelInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(@NotNull Channel ch) {
        ch.attr(Keys.PHASE_KEY).set(ProtocolPhase.HANDSHAKE);
        ch.attr(Keys.VERSION_KEY).set(Version.V1_8);

        ch.pipeline()
            .addLast(Pipeline.FRAME_DECODER, new VarIntFrameDecoder())
            .addLast(Pipeline.FRAME_ENCODER, new VarIntFrameEncoder())
            .addLast(Pipeline.PACKET_SERIALIZER, new PacketSerializer());
    }
}
