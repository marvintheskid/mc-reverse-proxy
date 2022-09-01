package gbx.proxy.networking.pipeline.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import gbx.proxy.ProxyBootstrap;
import gbx.proxy.networking.Keys;
import gbx.proxy.networking.ProtocolDirection;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.networking.packet.impl.login.client.EncryptionResponse;
import gbx.proxy.networking.packet.impl.login.server.EncryptionRequest;
import gbx.proxy.networking.pipeline.Pipeline;
import gbx.proxy.networking.pipeline.game.CipherDecoder;
import gbx.proxy.networking.pipeline.game.CipherEncoder;
import gbx.proxy.networking.pipeline.game.PacketCompressor;
import gbx.proxy.networking.pipeline.game.PacketDecompressor;
import gbx.proxy.utils.AttributeUtils;
import gbx.proxy.utils.IndexRollback;
import gbx.proxy.utils.MinecraftEncryption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Map;

import static gbx.proxy.utils.ByteBufUtils.*;

/**
 * The frontend handler.
 */
public class FrontendHandler extends ChannelDuplexHandler {
    private final Channel backend;

    public FrontendHandler(Channel backend) {
        this.backend = backend;
    }

    /**
     * Handles client to proxy traffic.
     */
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (backend.isActive()) {
            backend.writeAndFlush(msg);
        } else {
            backend.write(msg);
        }
    }

    /**
     * Handles proxy to client traffic.
     *
     * @apiNote the {@link Channel} returned by {@code ctx} is the <b>frontend</b> connection.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Channel frontend = ctx.channel();
        if (msg instanceof ByteBuf buf) {
            try (IndexRollback __ = IndexRollback.reader(buf)) {
                int id = readVarInt(buf);
                ProtocolPhase phase = ctx.channel().attr(Keys.PHASE_KEY).get();
                Version version = ctx.channel().attr(Keys.VERSION_KEY).get();
                PacketType type = PacketTypes.find(ProtocolDirection.SERVER, phase, id, version);

                if (PacketTypes.Login.Server.ENCRYPTION_REQUEST == type) {
                    System.out.println("[+] Enabling encryption for " + frontend.remoteAddress());

                    EncryptionRequest original = new EncryptionRequest();
                    original.decode(buf, version);

                    SecretKey secretKey = MinecraftEncryption.generateSecretKey();
                    PublicKey publicKey = original.publicKey();

                    String serverId = new BigInteger(MinecraftEncryption.hashServerId(original.hashedServerId(), publicKey, secretKey)).toString(16);

                    ProxyBootstrap.SESSION_SERVICE.joinServer(
                        new GameProfile(UUIDTypeAdapter.fromString(ProxyBootstrap.UUID), null),
                        ProxyBootstrap.ACCESS_TOKEN,
                        serverId
                    );

                    EncryptionResponse response = new EncryptionResponse(
                        MinecraftEncryption.encryptData(publicKey, secretKey.getEncoded()),
                        MinecraftEncryption.encryptData(publicKey, original.verifyToken())
                    );

                    ByteBuf responseBuf = ctx.channel().alloc().buffer();
                    responseBuf.retain();
                    writeVarInt(responseBuf, PacketTypes.Login.Client.ENCRYPTION_RESPONSE.id(version));
                    response.encode(responseBuf, version);

                    backend.writeAndFlush(responseBuf).addListener((ChannelFutureListener) f -> f.channel().pipeline()
                        .addBefore(Pipeline.FRAME_DECODER, Pipeline.DECRYPTER, new CipherDecoder(secretKey))
                        .addBefore(Pipeline.FRAME_ENCODER, Pipeline.ENCRYPTER, new CipherEncoder(secretKey))
                    );

                    responseBuf.release();
                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.SET_COMPRESSION == type) {
                    int threshold = readVarInt(buf);
                    System.out.println("[+] Enabling compression for " + frontend.remoteAddress() + " (compression after: " + threshold + ")");

                    backend.pipeline()
                        .addAfter(Pipeline.FRAME_DECODER, Pipeline.DECOMPRESSOR, new PacketDecompressor(threshold))
                        .addAfter(Pipeline.FRAME_ENCODER, Pipeline.COMPRESSOR, new PacketCompressor(threshold));

                    int i2 = 0;
                    for (Map.Entry<String, ChannelHandler> stringChannelHandlerEntry : backend.pipeline()) {
                        System.out.println(i2++ + " " + stringChannelHandlerEntry.getKey());
                    }

                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.LOGIN_SUCCESS == type) {
                    System.out.println("[*] Switching protocol stage to " + ProtocolPhase.PLAY);
                    System.out.println(ByteBufUtil.prettyHexDump(buf));
                    AttributeUtils.update(Keys.PHASE_KEY, ProtocolPhase.PLAY, frontend, backend);
                }
            }
            super.write(ctx, msg, promise);
        }
    }
}
