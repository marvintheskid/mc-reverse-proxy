package me.marvin.proxy.networking.pipeline.proxy;

import me.marvin.proxy.Proxy;
import me.marvin.proxy.networking.Keys;
import me.marvin.proxy.networking.ProtocolDirection;
import me.marvin.proxy.networking.ProtocolPhase;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.networking.packet.PacketTypes;
import me.marvin.proxy.networking.packet.impl.handshake.client.LoginStart;
import me.marvin.proxy.networking.packet.impl.handshake.client.SetProtocol;
import me.marvin.proxy.utils.AttributeUtils;
import me.marvin.proxy.utils.IndexRollback;
import me.marvin.proxy.utils.Tristate;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

import static me.marvin.proxy.utils.ByteBufUtils.*;

/**
 * The backend handler.
 */
public class BackendHandler extends ChannelDuplexHandler {
    private final Proxy proxy;
    private final Channel frontend;

    public BackendHandler(Proxy proxy, Channel frontend) {
        this.proxy = proxy;
        this.frontend = frontend;
    }

    /**
     * Handles server to proxy traffic.
     */
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (frontend.isActive()) {
            frontend.writeAndFlush(msg);
        } else {
            frontend.write(msg);
        }
    }

    /**
     * Handles proxy to server traffic.
     *
     * @apiNote the {@link Channel} returned by {@code ctx} is the <b>backend</b> connection.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Channel backend = ctx.channel();

        if (msg instanceof ByteBuf buf) {
            try (IndexRollback __ = IndexRollback.readerManual(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
                PacketType type = PacketTypes.findThrowing(ProtocolDirection.CLIENT, phase, id, version);
                Tristate cancelPackets = proxy.callListeners(type, buf, frontend, ctx, version);

                if (cancelPackets.booleanValue()) {
                    buf.release();
                    return;
                }

                if (PacketTypes.Handshake.Client.SET_PROTOCOL == type) {
                    SetProtocol setProtocol = new SetProtocol();
                    setProtocol.decode(buf, version);

                    AttributeUtils.update(Keys.PHASE_KEY, setProtocol.nextPhase(), frontend, backend);
                    AttributeUtils.update(Keys.VERSION_KEY, setProtocol.protocolVersion(), frontend, backend);

                    if (backend.remoteAddress() instanceof InetSocketAddress socketAddress) {
                        super.write(ctx, new SetProtocol(
                            setProtocol.protocolVersion(),
                            socketAddress.getHostName(),
                            socketAddress.getPort(),
                            setProtocol.nextPhase()
                        ), promise);
                        buf.release();
                        return;
                    }
                } else if (PacketTypes.Login.Client.LOGIN_START == type) {
                    if (!proxy.name().isBlank()) {
                        super.write(ctx, new LoginStart(proxy.name()), promise);
                        buf.release();
                        return;
                    }
                }
            }
        }
        super.write(ctx, msg, promise);
    }
}
