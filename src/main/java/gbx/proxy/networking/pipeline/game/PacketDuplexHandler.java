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
import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.utils.IndexRollback;
import gbx.proxy.utils.MinecraftEncryption;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

import java.security.PublicKey;
import java.util.Map;

import static gbx.proxy.utils.ByteBufUtils.readVarInt;
import static gbx.proxy.utils.ByteBufUtils.writeVarInt;

public class PacketDuplexHandler extends ChannelDuplexHandler {
    private final Channel clientChannel;

    public PacketDuplexHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    // client to server
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf buf) {
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
                PacketType type = PacketTypes.find(ProtocolDirection.CLIENT, phase, id, version);

                if (PacketTypes.Handshake.Client.SET_PROTOCOL == type) {
                    SetProtocol setProtocol = new SetProtocol();
                    setProtocol.decode(buf, version);

                    ctx.channel().attr(Keys.PHASE_KEY).set(setProtocol.nextPhase());
                    ctx.channel().attr(Keys.VERSION_KEY).set(setProtocol.protocolVersion());
                } /*else if (PacketTypes.Login.Client.ENCRYPTION_RESPONSE == type) {
                    // TODO

                    buf.release();
                    return;
                } */
            }
        }
        super.write(ctx, msg, promise);
    }

    // server to client
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof ByteBuf buf) {
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
                PacketType type = PacketTypes.find(ProtocolDirection.SERVER, phase, id, version);

                if (PacketTypes.Login.Server.ENCRYPTION_REQUEST == type) {
                    System.out.println("[+] Enabling encryption for " + ctx.channel().remoteAddress());

                    EncryptionRequest original = new EncryptionRequest();
                    original.decode(buf, version);

                    SecretKey secretKey = MinecraftEncryption.generateSharedKey();
                    PublicKey publicKey = original.publicKey();

                    EncryptionResponse response = new EncryptionResponse(
                        MinecraftEncryption.encryptData(publicKey, secretKey.getEncoded()),
                        MinecraftEncryption.encryptData(publicKey, original.verifyToken())
                    );
                    ByteBuf responseBuf = ctx.channel().alloc().buffer();
                    responseBuf.retain();
                    writeVarInt(responseBuf, id);
                    response.encode(responseBuf, version);

                    ctx.channel().writeAndFlush(responseBuf).addListener((ChannelFutureListener) f -> f.channel().pipeline()
                        .addFirst(Pipeline.DECRYPTER, new CipherDecoder(secretKey))
                        .addAfter(Pipeline.DECRYPTER, Pipeline.ENCRYPTER, new CipherEncoder(secretKey))
                    );

                    responseBuf.release();
                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.SET_COMPRESSION == type || PacketTypes.Play.Server.SET_COMPRESSION == type) {
                    int threshold = readVarInt(buf);
                    System.out.println("[+] Enabling compression for " + ctx.channel().remoteAddress() + " (compression after: " + threshold + ")");

                    ctx.channel().pipeline()
                        .addAfter(ctx.channel().pipeline().get(Pipeline.DECRYPTER) != null ? Pipeline.DECRYPTER : Pipeline.FRAME_ENCODER, Pipeline.DECOMPRESSOR, new PacketDecompressor(threshold))
                        .addAfter(Pipeline.DECOMPRESSOR, Pipeline.COMPRESSOR, new PacketCompressor(threshold));

                    // TODO: this is a hacky solution: we dont forward the compression packet to the client, because
                    //  with the current pipeline its kinda hacky to await for the client to enable compression.
                    //  This caused the client to read compressed packets without compression enabled client-side.
                    //  Note: its not even necessary for us to enable compression for the client because of loopback
                    /*clientChannel.pipeline()
                        .addAfter(Pipeline.FRAME_ENCODER, Pipeline.DECOMPRESSOR, new PacketDecompressor(threshold))
                        .addAfter(Pipeline.DECOMPRESSOR, Pipeline.COMPRESSOR, new PacketCompressor(threshold));*/
                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.LOGIN_SUCCESS == type) {
                    ctx.channel().attr(Keys.PHASE_KEY).set(ProtocolPhase.PLAY);
                }
            }
        }
        super.channelRead(ctx, msg);
    }
}
