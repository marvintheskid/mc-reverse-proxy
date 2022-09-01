package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.networking.pipeline.game.PacketSerializer;
import gbx.proxy.utils.ServerAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

/**
 * The frontend handler handles initial connections from the client towards the proxy.
 * <br>
 * It also creates the connection between the backend server and the proxy.
 */
public class FrontendChannelInitializer extends DefaultChannelInitializer {
    private final ServerAddress address;

    public FrontendChannelInitializer(ServerAddress address) {
        this.address = address;
    }

    @Override
    protected void initChannel(@NotNull Channel frontend) {
        frontend.eventLoop().execute(() -> {
            Bootstrap bootstrap = new Bootstrap()
                .group(frontend.eventLoop())
                .channel(frontend.getClass())
                .handler(new BackendChannelInitializer(frontend))
                .remoteAddress(address.toInetAddress());

            ChannelFuture future = bootstrap.connect()
                .addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        // Flushing queued up packets
                        f.channel().flush();
                    } else {
                        f.channel().close();
                        f.cause().printStackTrace();
                        // Closing parent
                        frontend.close();
                    }
                });

            super.initChannel(frontend);
            frontend.pipeline()
                .addLast(Pipeline.PACKET_SERIALIZER, new PacketSerializer())
                .addLast(Pipeline.FRONTEND_HANDLER, new FrontendHandler(future.channel()));
        });
    }
}
