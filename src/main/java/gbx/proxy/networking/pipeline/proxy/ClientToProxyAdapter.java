package gbx.proxy.networking.pipeline.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;

public class ClientToProxyAdapter extends ChannelInboundHandlerAdapter {
    private final Channel serverChannel;

    public ClientToProxyAdapter(Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    // Client -> Proxy
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (!serverChannel.isActive()) {
            serverChannel.write(msg);
        } else {
            serverChannel.writeAndFlush(msg, serverChannel.voidPromise());
        }
    }

    // write: p -> c

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        System.out.println("[!] Client disconnect: " + ctx.channel().remoteAddress());
        serverChannel.close();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
