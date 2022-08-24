package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.networking.Keys;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.networking.pipeline.game.*;
import gbx.proxy.utils.ServerAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

/**
 * A duplex handler where {@link #channelRead(ChannelHandlerContext, Object)} gets called when
 * the client sends a packet towards the proxy.
 * <br>
 * This is a "frontend handler".
 */
public class ClientToProxyHandler extends ChannelInboundHandlerAdapter {
    private final ServerAddress address;

    public ClientToProxyHandler(ServerAddress address) {
        this.address = address;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        Channel clientChannel = ctx.channel(); // the connection to the proxy
        Bootstrap bootstrap = new Bootstrap()
            .group(clientChannel.eventLoop())
            .channel(clientChannel.getClass())
            .handler(new ServerToProxyHandler(clientChannel))
            .remoteAddress(address.toInetAddress());

        ChannelFuture future = bootstrap.connect()
            .addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    System.out.println("[+] Successfully connected " + ctx.channel().remoteAddress() + " -> " + address);
                    // Flushing queued up packets
                    f.channel().flush();
                } else {
                    System.out.println("[-] Failed to connect to " + address);
                    f.channel().close();
                    f.cause().printStackTrace();
                    // Closing parent
                    ctx.channel().close();
                }
            }
        );

        Channel serverChannel = future.channel(); // the connection to the backend
        serverChannel.attr(Keys.PHASE_KEY).set(ProtocolPhase.HANDSHAKE);
        serverChannel.attr(Keys.VERSION_KEY).set(Version.V1_8);

        clientChannel.pipeline()
            .addLast(Pipeline.FRAME_DECODER, new VarIntFrameDecoder())
            .addLast(Pipeline.FRAME_ENCODER, new VarIntFrameEncoder())
            .addLast(new ClientToProxyAdapter(serverChannel));

        serverChannel.pipeline()
            .addLast(Pipeline.FRAME_DECODER, new VarIntFrameEncoder())
            .addLast(Pipeline.FRAME_ENCODER, new VarIntFrameDecoder())
            .addLast(Pipeline.PACKET_HANDLER, new PacketDuplexHandler(clientChannel));

        ctx.channel().pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
