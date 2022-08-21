package gbx.proxy;

import gbx.proxy.pipeline.ProxyChannelInitializer;
import gbx.proxy.utils.AddressResolver;
import gbx.proxy.utils.ServerAddress;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    public static final EventLoopGroup BOSS_GROUP = new NioEventLoopGroup(1);
    public static final EventLoopGroup WORKER_GROUP = new NioEventLoopGroup();

    public static void main(String[] args) throws InterruptedException {
        int port = Integer.getInteger("port", 25565);
        String targetAddr = System.getProperty("target");

        System.out.println("[?] Resolving address... (" + targetAddr + ")");
        ServerAddress addr = AddressResolver.getServerAddress(targetAddr);
        System.out.println("[!] Resolved server address: " + addr);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
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
                    }
                });

            future.channel().closeFuture().sync();
        } finally {
            BOSS_GROUP.shutdownGracefully();
            WORKER_GROUP.shutdownGracefully();
        }
    }
}