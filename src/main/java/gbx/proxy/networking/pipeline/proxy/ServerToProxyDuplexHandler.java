package gbx.proxy.networking.pipeline.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

public class ServerToProxyDuplexHandler extends ChannelDuplexHandler {
    private final Channel clientChannel;

    public ServerToProxyDuplexHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    // Server -> Proxy
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        clientChannel.writeAndFlush(msg, clientChannel.voidPromise());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[!] Server disconnect: " + clientChannel.remoteAddress());
        clientChannel.close();
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
