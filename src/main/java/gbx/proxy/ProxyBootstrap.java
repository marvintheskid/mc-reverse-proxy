package gbx.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import gbx.proxy.networking.PacketListener;
import gbx.proxy.networking.RegisteredPacketListener;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.pipeline.proxy.FrontendChannelInitializer;
import gbx.proxy.scripting.ProxyScriptConfiguration;
import gbx.proxy.scripting.ProxyScriptDefinition;
import gbx.proxy.scripting.ProxyScriptHandler;
import gbx.proxy.utils.AddressResolver;
import gbx.proxy.utils.ServerAddress;
import gbx.proxy.utils.Tristate;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.Proxy;
import java.nio.file.Path;
import java.util.*;

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
     * The registered packet listeners.
     */
    public static final List<RegisteredPacketListener> PACKET_LISTENERS;
    /**
     * The script handler used by the proxy utilizing the kotlin script runtime.
     */
    public static final ProxyScriptHandler SCRIPT_HANDLER;
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
        PACKET_LISTENERS = new ArrayList<>();
        SCRIPT_HANDLER = new ProxyScriptHandler();
        PacketTypes.load();
    }

    public static void main(String[] args) throws InterruptedException {
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
                        System.out.println("[-] Failed to bind on " + port);
                        f.cause().printStackTrace();
                    }
                });

            future.channel().closeFuture().sync();
        } finally {
            BOSS_GROUP.shutdownGracefully();
            WORKER_GROUP.shutdownGracefully();
        }
    }

    /**
     * Registers the given listeners with the given owner.
     *
     * @param owner the owner
     * @param listeners the listeners
     */
    public static void registerListeners(Object owner, PacketListener... listeners) {
        for (PacketListener listener : listeners) {
            PACKET_LISTENERS.add(new RegisteredPacketListener(owner, listener));
        }
        PACKET_LISTENERS.sort(null);
    }

    /**
     * Unregisters the given owner or the listeners themselves.
     *
     * @param objects the objects
     */
    public static void unregisterListeners(Object... objects) {
        for (Object object : objects) {
            PACKET_LISTENERS.removeIf(listener -> {
                if (object instanceof PacketListener packetListener) {
                    return listener.listener() == packetListener;
                } else {
                    return listener.owner() == object;
                }
            });
        }
    }

    /**
     * Calls all the packet listeners.
     *
     * @param type      the packet type
     * @param buf       the buffer
     * @param context   the netty context
     * @param version   the version
     * @return false if the server should consume the packet, otherwise true
     */
    public static Tristate callListeners(PacketType type, ByteBuf buf, ChannelHandlerContext context, Version version) {
        Tristate cancelled = Tristate.NOT_SET;

        for (RegisteredPacketListener listener : PACKET_LISTENERS) {
            Tristate newState = listener.listener().handle(type, buf, context, version, cancelled);
            if (newState != Tristate.NOT_SET) {
                cancelled = newState;
            }
        }

        return cancelled;
    }
}