package gbx.proxy.networking.pipeline.proxy;

import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

/**
 * A duplex handler where {@link #channelRead(ChannelHandlerContext, Object)} gets called when
 * the server sends a packet towards the proxy.
 * <br>
 * This is a "backend handler".
 */
public class ServerToProxyHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;

    public ServerToProxyHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        ctx.channel().pipeline()
            .addLast(new ServerToProxyAdapter(clientChannel));
        ctx.channel().pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
