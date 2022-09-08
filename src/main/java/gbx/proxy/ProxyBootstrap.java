package gbx.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.pipeline.proxy.FrontendChannelInitializer;
import gbx.proxy.scripting.ScriptHandler;
import gbx.proxy.scripting.Scripting;
import gbx.proxy.utils.AddressResolver;
import gbx.proxy.utils.ServerAddress;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    /**
     * Boss event group.
     */
    public static final EventLoopGroup BOSS_GROUP;
    /**
     * Worker event group.
     */
    public static final EventLoopGroup WORKER_GROUP;
    /**
     * Netty channel type.
     */
    public static final Class<? extends ServerChannel> CHANNEL_TYPE;
    /**
     * The folder where the program was executed. For example, it's used for loading scripts.
     */
    public static final Path PARENT_FOLDER;
    /**
     * The script handler instance.
     */
    public static final ScriptHandler SCRIPT_HANDLER;
    /**
     * The session service used for authentication.
     */
    public static MinecraftSessionService SESSION_SERVICE = new YggdrasilAuthenticationService(Proxy.NO_PROXY).createMinecraftSessionService();
    /**
     * The access token used by {@link MinecraftSessionService#joinServer(GameProfile, String, String)} during authentication.
     */
    public static String ACCESS_TOKEN = "";
    /**
     * The undashed UUID used by {@link MinecraftSessionService#joinServer(GameProfile, String, String)} during authentication.
     */
    public static String UUID = "";
    /**
     * The name used for authentication. If this is empty, then the proxy will forward the name sent by the client.
     */
    public static String NAME = "";

    static {
        if (Epoll.isAvailable()) {
            BOSS_GROUP = new EpollEventLoopGroup(1);
            WORKER_GROUP = new EpollEventLoopGroup();
            CHANNEL_TYPE = EpollServerSocketChannel.class;
        } else {
            BOSS_GROUP = new NioEventLoopGroup(1);
            WORKER_GROUP = new NioEventLoopGroup();
            CHANNEL_TYPE = NioServerSocketChannel.class;
        }

        PARENT_FOLDER = Path.of("").toAbsolutePath();

        try {
            SCRIPT_HANDLER = new ScriptHandler();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize scripts", e);
        }

        PacketTypes.load();
    }

    public static void main(String[] args) throws InterruptedException {
        // We want to execute the initializer using the persistent context, because we can't use values from dead contexts.
        // This is required, because this way we can set the session service using the script initializer.
        SCRIPT_HANDLER.forAllScripts(script -> script.executePersistent(value -> {
            if (!value.hasMember(Scripting.INITIALIZER)) return;
            value.invokeMember(Scripting.INITIALIZER);
        }));

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
                .childHandler(new FrontendChannelInitializer(addr))
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
            SCRIPT_HANDLER.close();
        }
    }
}