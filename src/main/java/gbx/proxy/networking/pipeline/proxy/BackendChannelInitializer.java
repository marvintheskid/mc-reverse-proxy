package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.networking.pipeline.game.PacketSerializer;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

/**
 * The backend handler handles initial connections from the proxy towards the server.
 */
public class BackendChannelInitializer extends DefaultChannelInitializer {
    private final Channel frontend;

    public BackendChannelInitializer(Channel frontend) {
        this.frontend = frontend;
    }

    @Override
    protected void initChannel(@NotNull Channel backend) {
        super.initChannel(backend);
        backend.pipeline()
            .addLast(Pipeline.BACKEND_HANDLER, new BackendHandler(frontend));
    }
}
