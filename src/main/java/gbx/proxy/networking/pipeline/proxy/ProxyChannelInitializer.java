package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.utils.ServerAddress;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

public class ProxyChannelInitializer extends ChannelInitializer<Channel> {
    private final ServerAddress address;

    public ProxyChannelInitializer(ServerAddress address) {
        this.address = address;
    }

    @Override
    protected void initChannel(@NotNull Channel ch) {
        // TODO: distinguish between handshakes and logins (complex pipeline)
        System.out.println("[+] Incoming connection: " + ch.remoteAddress());
        ch.pipeline().addLast(new ClientToProxyHandler(address));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
