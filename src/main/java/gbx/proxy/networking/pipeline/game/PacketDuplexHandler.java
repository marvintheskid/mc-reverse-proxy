package gbx.proxy.networking.pipeline.game;

import gbx.proxy.networking.Keys;
import gbx.proxy.networking.ProtocolDirection;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.packet.impl.handshake.client.SetProtocol;
import gbx.proxy.networking.packet.impl.login.client.EncryptionResponse;
import gbx.proxy.networking.packet.impl.login.server.EncryptionRequest;
import gbx.proxy.utils.IndexRollback;
import gbx.proxy.utils.MinecraftEncryption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;

import static gbx.proxy.utils.ByteBufUtils.readVarInt;

public class PacketDuplexHandler extends ChannelDuplexHandler {
    // c->s
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf buf) {
            System.out.println(ByteBufUtil.prettyHexDump(buf));
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                PacketType type = PacketTypes.findThrowing(ProtocolDirection.CLIENT, phase, id, Version.V1_8);

                if (PacketTypes.Handshake.Client.SET_PROTOCOL == type) {
                    SetProtocol setProtocol = new SetProtocol();
                    setProtocol.decode(buf, Version.V1_8);

                    ctx.channel().attr(Keys.PHASE_KEY).set(setProtocol.nextPhase());
                    ctx.channel().attr(Keys.VERSION_KEY).set(setProtocol.protocolVersion());
                } else if (PacketTypes.Login.Client.ENCRYPTION_RESPONSE == type) {
                    EncryptionResponse response = new EncryptionResponse();
                    response.decode(buf, Version.V1_8);
                }
            }
        }
        super.write(ctx, msg, promise);
    }

    // s -> c
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof ByteBuf buf) {
            System.out.println(ByteBufUtil.prettyHexDump(buf));
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                PacketType type = PacketTypes.findThrowing(ProtocolDirection.CLIENT, phase, id, Version.V1_8);

                if (PacketTypes.Login.Server.ENCRYPTION_REQUEST == type) {
                    EncryptionRequest request = new EncryptionRequest();
                    request.decode(buf, Version.V1_8);
                }
            }
        }
        super.channelRead(ctx, msg);
    }
}
