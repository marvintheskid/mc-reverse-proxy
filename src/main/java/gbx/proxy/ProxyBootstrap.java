package gbx.proxy;

import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.pipeline.proxy.ProxyChannelInitializer;
import gbx.proxy.utils.AddressResolver;
import gbx.proxy.utils.ServerAddress;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    public static final EventLoopGroup BOSS_GROUP = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    public static final EventLoopGroup WORKER_GROUP = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    public static final Class<? extends ServerChannel> CHANNEL_TYPE = Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

    static {
        PacketTypes.load();
    }

    public static void main(String[] args) throws InterruptedException {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        int port = Integer.getInteger("port", 25565);
        String targetAddr = System.getProperty("target", ":25566");

        if (Epoll.isAvailable()) {
            System.out.println("[!] Using epoll...");
        }

        System.out.println("[?] Resolving address... (" + targetAddr + ")");
        ServerAddress addr = AddressResolver.getServerAddress(targetAddr);
        System.out.println("[!] Resolved server address: " + addr);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                .channel(CHANNEL_TYPE)
                .group(BOSS_GROUP, WORKER_GROUP)
                .childHandler(new ProxyChannelInitializer(addr))
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.IP_TOS, 0x18)
                .localAddress(port);

            ChannelFuture future = bootstrap.bind()
                .addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        System.out.println("[!] Listening on " + f.channel().localAddress());
                    } else {
                        System.out.println("[-] Failed to bind on " + f.channel().localAddress());
                        f.cause().printStackTrace();
                        f.channel().close();
                    }
                });

            future.channel().closeFuture().sync();
        } finally {
            BOSS_GROUP.shutdownGracefully();
            WORKER_GROUP.shutdownGracefully();
        }
    }
}