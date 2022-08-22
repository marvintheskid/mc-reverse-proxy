package gbx.proxy.networking.packet.impl.login.client;

import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.Packet;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static gbx.proxy.utils.ByteBufUtils.readByteArray;
import static gbx.proxy.utils.ByteBufUtils.writeByteArray;

public class EncryptionResponse implements Packet {
    private byte[] encryptedSecretKey;
    private byte[] encryptedVerifyToken;

    @Override
    public void encode(@NotNull ByteBuf buf, @NotNull Version version) {
        writeByteArray(buf, encryptedSecretKey);
        writeByteArray(buf, encryptedVerifyToken);
    }

    @Override
    public void decode(@NotNull ByteBuf buf, @NotNull Version version) {
        encryptedSecretKey = readByteArray(buf);
        encryptedVerifyToken = readByteArray(buf);
    }

    @Override
    @NotNull
    public PacketType type() {
        return PacketTypes.Login.Client.ENCRYPTION_RESPONSE;
    }

    public byte[] encryptedSecretKey() {
        return encryptedSecretKey;
    }

    public byte[] encryptedVerifyToken() {
        return encryptedVerifyToken;
    }
}
