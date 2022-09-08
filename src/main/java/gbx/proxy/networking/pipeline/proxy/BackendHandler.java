package gbx.proxy.networking.pipeline.proxy;

import gbx.proxy.ProxyBootstrap;
import gbx.proxy.networking.Keys;
import gbx.proxy.networking.ProtocolDirection;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.packet.impl.handshake.client.LoginStart;
import gbx.proxy.networking.packet.impl.handshake.client.SetProtocol;
import gbx.proxy.scripting.Scripting;
import gbx.proxy.utils.AttributeUtils;
import gbx.proxy.utils.IndexRollback;
import gbx.proxy.utils.Tristate;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static gbx.proxy.utils.ByteBufUtils.*;

/**
 * The backend handler.
 */
public class BackendHandler extends ChannelDuplexHandler {
    private final Channel frontend;

    public BackendHandler(Channel frontend) {
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
        final AtomicBoolean cancel = new AtomicBoolean(false);
        Channel backend = ctx.channel();

        if (msg instanceof ByteBuf buf) {
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
                PacketType type = PacketTypes.findThrowing(ProtocolDirection.CLIENT, phase, id, version);

                // TODO: clean this up
                ProxyBootstrap.SCRIPT_HANDLER.executeScripts(script -> {
                    if (!script.hasMember(Scripting.CLIENT_TO_SERVER)) return;

                    try (IndexRollback ___ = IndexRollback.readerManual(buf)) {
                        Tristate result = script.invokeMember(Scripting.CLIENT_TO_SERVER,
                            ctx,
                            frontend,
                            buf,
                            phase,
                            type,
                            promise
                        ).as(Tristate.class);

                        if (result != Tristate.NOT_SET) {
                            cancel.set(result.booleanValue());
                        }
                    }
                });

                if (!cancel.get()) {
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
                            cancel.set(true);
                        }
                    } else if (PacketTypes.Login.Client.LOGIN_START == type) {
                        if (!ProxyBootstrap.NAME.isBlank()) {
                            super.write(ctx, new LoginStart(ProxyBootstrap.NAME), promise);
                            cancel.set(true);
                        }
                    }
                }
            }

            if (cancel.get()) {
                buf.release();
            }
        }
        if (!cancel.get()) {
            super.write(ctx, msg, promise);
        }
    }
}
