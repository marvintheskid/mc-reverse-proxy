package gbx.proxy.networking.packet.impl.login.server;

import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.Packet;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import gbx.proxy.utils.MinecraftEncryption;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;

import static gbx.proxy.utils.ByteBufUtils.*;

public class EncryptionRequest implements Packet {
    private String hashedServerId;
    private PublicKey publicKey;
    private byte[] verifyToken;

    @Override
    public void encode(@NotNull ByteBuf buf, @NotNull Version version) {
        writeString(buf, hashedServerId);
        writeByteArray(buf, publicKey.getEncoded());
        writeByteArray(buf, verifyToken);
    }

    @Override
    public void decode(@NotNull ByteBuf buf, @NotNull Version version) {
        hashedServerId = readString(buf, 20);
        publicKey = MinecraftEncryption.createRSAPublicKey(readByteArray(buf));
        verifyToken = readByteArray(buf);
    }

    @Override
    @NotNull
    public PacketType type() {
        return PacketTypes.Login.Server.ENCRYPTION_REQUEST;
    }

    public String hashedServerId() {
        return hashedServerId;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    public byte[] verifyToken() {
        return verifyToken;
    }
}
