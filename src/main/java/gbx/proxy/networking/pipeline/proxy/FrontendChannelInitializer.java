package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.utils.ServerAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

/**
 * The frontend handler handles initial connections from the client towards the proxy.
 * <br>
 * It also creates the connection between the backend server and the proxy.
 */
public class FrontendChannelInitializer extends ChannelInitializer<Channel> {
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
                        System.out.println("[+] Successfully connected " + frontend.remoteAddress() + " -> " + address);
                        // Flushing queued up packets
                        f.channel().flush();
                    } else {
                        System.out.println("[-] Failed to connect to " + address);
                        f.channel().close();
                        f.cause().printStackTrace();
                        // Closing parent
                        frontend.close();
                    }
                });

            DefaultChannelInitializer.INSTANCE.init(frontend);
            frontend.pipeline().addLast(Pipeline.FRONTEND_HANDLER, new FrontendHandler(future.channel()));
        });
    }
}
