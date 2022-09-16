package me.marvin.proxy.networking.pipeline.proxy;

import me.marvin.proxy.Proxy;
import me.marvin.proxy.networking.pipeline.Pipeline;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

/**
 * The backend handler handles initial connections from the proxy towards the server.
 */
public class BackendChannelInitializer extends DefaultChannelInitializer {
    private final Proxy proxy;
    private final Channel frontend;

    public BackendChannelInitializer(Proxy proxy, Channel frontend) {
        this.proxy = proxy;
        this.frontend = frontend;
    }

    @Override
    protected void initChannel(@NotNull Channel backend) {
        super.initChannel(backend);
        backend.pipeline()
            .addLast(Pipeline.BACKEND_HANDLER, new BackendHandler(proxy, frontend));
    }
}
