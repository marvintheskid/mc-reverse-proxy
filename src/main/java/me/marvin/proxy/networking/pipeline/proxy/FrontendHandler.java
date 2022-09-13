package me.marvin.proxy.networking.pipeline.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import me.marvin.proxy.Proxy;
import me.marvin.proxy.ProxyBootstrap;
import me.marvin.proxy.networking.Keys;
import me.marvin.proxy.networking.ProtocolDirection;
import me.marvin.proxy.networking.ProtocolPhase;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.networking.packet.PacketTypes;
import me.marvin.proxy.networking.packet.impl.login.client.EncryptionResponse;
import me.marvin.proxy.networking.packet.impl.login.server.EncryptionRequest;
import me.marvin.proxy.networking.pipeline.Pipeline;
import me.marvin.proxy.networking.pipeline.game.CipherDecoder;
import me.marvin.proxy.networking.pipeline.game.CipherEncoder;
import me.marvin.proxy.networking.pipeline.game.PacketCompressor;
import me.marvin.proxy.networking.pipeline.game.PacketDecompressor;
import me.marvin.proxy.utils.AttributeUtils;
import me.marvin.proxy.utils.IndexRollback;
import me.marvin.proxy.utils.MinecraftEncryption;
import me.marvin.proxy.utils.Tristate;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

import static me.marvin.proxy.utils.ByteBufUtils.*;

/**
 * The frontend handler.
 */
public class FrontendHandler extends ChannelDuplexHandler {
    private final Proxy proxy;
    private final Channel backend;

    public FrontendHandler(Proxy proxy, Channel backend) {
        this.proxy = proxy;
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
                PacketType type = PacketTypes.findThrowing(ProtocolDirection.SERVER, phase, id, version);
                Tristate cancelPackets = proxy.callListeners(type, buf, ctx, version);

                if (cancelPackets.booleanValue()) {
                    buf.clear();
                    return;
                }

                if (PacketTypes.Login.Server.ENCRYPTION_REQUEST == type) {
                    System.out.println("[+] Enabling encryption for " + frontend.remoteAddress());

                    EncryptionRequest original = new EncryptionRequest();
                    original.decode(buf, version);

                    SecretKey secretKey = MinecraftEncryption.generateSecretKey();
                    PublicKey publicKey = original.publicKey();

                    String serverId = new BigInteger(MinecraftEncryption.hashServerId(original.hashedServerId(), publicKey, secretKey)).toString(16);

                    proxy.sessionService().joinServer(
                        new GameProfile(UUIDTypeAdapter.fromString(proxy.uuid()), null),
                        proxy.accessToken(),
                        serverId
                    );

                    backend.writeAndFlush(new EncryptionResponse(
                        MinecraftEncryption.encryptData(publicKey, secretKey.getEncoded()),
                        MinecraftEncryption.encryptData(publicKey, original.verifyToken())
                    )).addListener((ChannelFutureListener) f -> f.channel().pipeline()
                        .addBefore(Pipeline.FRAME_DECODER, Pipeline.DECRYPTER, new CipherDecoder(secretKey))
                        .addBefore(Pipeline.FRAME_ENCODER, Pipeline.ENCRYPTER, new CipherEncoder(secretKey))
                    );

                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.SET_COMPRESSION == type) {
                    int threshold = readVarInt(buf);
                    System.out.println("[+] Enabling compression for " + frontend.remoteAddress() + " (compression after: " + threshold + ")");

                    backend.pipeline()
                        .addAfter(Pipeline.FRAME_DECODER, Pipeline.DECOMPRESSOR, new PacketDecompressor(threshold))
                        .addAfter(Pipeline.FRAME_ENCODER, Pipeline.COMPRESSOR, new PacketCompressor(threshold));

                    buf.release();
                    return;
                } else if (PacketTypes.Login.Server.LOGIN_SUCCESS == type) {
                    System.out.println("[*] Switching protocol stage to " + ProtocolPhase.PLAY);
                    AttributeUtils.update(Keys.PHASE_KEY, ProtocolPhase.PLAY, frontend, backend);
                }
            }
            super.write(ctx, msg, promise);
        }
    }
}