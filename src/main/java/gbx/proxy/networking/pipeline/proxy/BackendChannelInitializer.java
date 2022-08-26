package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.pipeline.Pipeline;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.jetbrains.annotations.NotNull;

/**
 * The backend handler handles initial connections from the proxy towards the server.
 */
public class BackendChannelInitializer extends ChannelInitializer<Channel> {
    private final Channel frontend;

    public BackendChannelInitializer(Channel frontend) {
        this.frontend = frontend;
    }

    @Override
    protected void initChannel(@NotNull Channel backend) {
        DefaultChannelInitializer.INSTANCE.init(backend);
        backend.pipeline().addLast(Pipeline.BACKEND_HANDLER, new BackendHandler(frontend));
    }
}
