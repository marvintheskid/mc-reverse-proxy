package me.marvin.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import me.marvin.proxy.networking.PacketListener;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.networking.packet.PacketTypes;
import me.marvin.proxy.networking.pipeline.proxy.FrontendChannelInitializer;
import me.marvin.proxy.utils.ServerAddress;
import me.marvin.proxy.utils.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Represents the proxy itself.
 */
public class Proxy {
    /**
     * Event group factory.
     */
    public static final IntFunction<EventLoopGroup> GROUP_FACTORY;

    /**
     * Netty channel type.
     */
    public static final Class<? extends ServerChannel> CHANNEL_TYPE;

    static {
        if (Epoll.isAvailable()) {
            GROUP_FACTORY = EpollEventLoopGroup::new;
            CHANNEL_TYPE = EpollServerSocketChannel.class;
        } else {
            GROUP_FACTORY = NioEventLoopGroup::new;
            CHANNEL_TYPE = NioServerSocketChannel.class;
        }

        PacketTypes.load();
    }

    /**
     * Boss event group.
     */
    private final EventLoopGroup bossGroup;
    /**
     * Worker event group.
     */
    private final EventLoopGroup workerGroup;
    /**
     * The port to which the proxy binds.
     */
    private final int port;
    /**
     * The folder where the program was executed.
     */
    private final Path parentFolder;
    /**
     * The registered packet listeners.
     */
    private final List<PacketListener> listeners;
    /**
     * The proxy channel.
     */
    private Channel channel;
    /**
     * The target server's address.
     */
    private ServerAddress address;
    /**
     * The session service used for authentication.
     */
    private MinecraftSessionService sessionService;
    /**
     * The access token used by {@link MinecraftSessionService#joinServer(GameProfile, String, String)} during authentication.
     */
    private String accessToken;
    /**
     * The undashed UUID used by {@link MinecraftSessionService#joinServer(GameProfile, String, String)} during authentication.
     */
    private String uuid;
    /**
     * The name used for authentication. If this is empty, then the proxy will forward the name sent by the client.
     */
    private String name;

    public Proxy(@Range(from = 0, to = 65535) int port, @NotNull String targetAddress) {
        this(port, targetAddress, Path.of("").toAbsolutePath());
    }

    public Proxy(@Range(from = 0, to = 65535) int port, @NotNull String targetAddress, @NotNull Path parentFolder) {
        this.bossGroup = GROUP_FACTORY.apply(1);
        this.workerGroup = GROUP_FACTORY.apply(0);
        this.port = port;
        this.address = ServerAddress.parse(targetAddress);
        this.parentFolder = parentFolder;
        this.listeners = new ArrayList<>();
        this.sessionService = new YggdrasilAuthenticationService(java.net.Proxy.NO_PROXY).createMinecraftSessionService();
        this.accessToken = "";
        this.uuid = "";
        this.name = "";
    }

    /**
     * Starts this proxy.
     *
     * @return this proxy
     * @throws InterruptedException if {@link Future#sync()} throws an {@link InterruptedException}
     */
    @NotNull
    public Proxy start(ChannelFutureListener listener) throws InterruptedException {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                .channel(CHANNEL_TYPE)
                .group(bossGroup, workerGroup)
                .childHandler(new FrontendChannelInitializer(this))
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.IP_TOS, 0x18)
                .localAddress(port);
            ChannelFuture future = bootstrap.bind()
                .addListener(listener);

            channel = future.channel();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

        return this;
    }

    /**
     * Shuts down this proxy immediately.
     *
     * @return this proxy
     * @throws InterruptedException if {@link Future#await()} throws an {@link InterruptedException}
     */
    @NotNull
    public Proxy shutdown() throws InterruptedException {
        channel.close().await();
        bossGroup.shutdownGracefully().await();
        workerGroup.shutdownGracefully().await();

        return this;
    }

    /**
     * Registers the given listeners with the given owner.
     *
     * @param packetListeners the listeners
     */
    public void registerListeners(PacketListener... packetListeners) {
        listeners.addAll(Arrays.asList(packetListeners));
        listeners.sort(null);
    }

    /**
     * Unregisters the listeners matching the given predicate.
     *
     * @param predicate the predicate
     */
    public void unregisterListeners(Predicate<PacketListener> predicate) {
        listeners.removeIf(predicate);
    }

    /**
     * Unregisters the given listener.
     *
     * @param packetListener the listener
     */
    public void unregisterListener(PacketListener packetListener) {
        listeners.remove(packetListener);
    }

    /**
     * Calls all the packet listeners.
     *
     * @param type    the packet type
     * @param buf     the buffer
     * @param context the netty context
     * @param version the version
     * @return false if the server should consume the packet, otherwise true
     */
    public Tristate callListeners(PacketType type, ByteBuf buf, ChannelHandlerContext context, Version version) {
        Tristate cancelled = Tristate.NOT_SET;

        for (PacketListener listener : listeners) {
            Tristate newState = listener.handle(type, buf, context, version, cancelled);
            if (newState != Tristate.NOT_SET) {
                cancelled = newState;
            }
        }

        return cancelled;
    }

    /**
     * Returns the port used by the proxy.
     *
     * @return the port
     */
    public int port() {
        return port;
    }

    /**
     * Returns the proxy server channel.
     *
     * @return the channel
     */
    @NotNull
    public Channel channel() {
        return channel;
    }

    /**
     * Returns the remote server's address.
     *
     * @return the address
     */
    @NotNull
    public ServerAddress address() {
        return address;
    }

    /**
     * Sets the target server's address.
     *
     * @param targetAddress the target server's address
     * @return this proxy
     */
    @NotNull
    public Proxy address(@NotNull String targetAddress) {
        this.address = ServerAddress.parse(targetAddress);
        return this;
    }

    /**
     * Returns the parent folder of the proxy.
     *
     * @return the parent folder
     */
    @NotNull
    public Path parentFolder() {
        return parentFolder;
    }

    /**
     * Returns the session service used by the proxy.
     *
     * @return the session service
     */
    @NotNull
    public MinecraftSessionService sessionService() {
        return sessionService;
    }

    /**
     * Sets the session service used by the proxy.
     *
     * @param sessionService the new session service
     * @return this proxy
     */
    @NotNull
    public Proxy sessionService(@NotNull MinecraftSessionService sessionService) {
        this.sessionService = sessionService;
        return this;
    }

    /**
     * Returns the access token used by the proxy.
     *
     * @return the access token
     */
    @NotNull
    public String accessToken() {
        return accessToken;
    }

    /**
     * Sets the access token used by the proxy.
     *
     * @param accessToken the new access token
     * @return this proxy
     */
    @NotNull
    public Proxy accessToken(@NotNull String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Returns the uuid used by the proxy.
     *
     * @return the uuid
     */
    @NotNull
    public String uuid() {
        return uuid;
    }

    /**
     * Sets the uuid used by the proxy.
     *
     * @param uuid the new uuid
     * @return this proxy
     */
    @NotNull
    public Proxy uuid(@NotNull String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Returns the name used by the proxy.
     *
     * @return the name
     */
    @NotNull
    public String name() {
        return name;
    }

    /**
     * Sets the name used by the proxy.
     *
     * @param name the new name token
     * @return this proxy
     */
    @NotNull
    public Proxy name(@NotNull String name) {
        this.name = name;
        return this;
    }
}