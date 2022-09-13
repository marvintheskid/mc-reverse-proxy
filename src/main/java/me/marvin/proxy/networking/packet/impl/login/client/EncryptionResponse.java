package me.marvin.proxy.networking.packet.impl.login.client;

import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.Packet;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.networking.packet.PacketTypes;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static me.marvin.proxy.utils.ByteBufUtils.readByteArray;
import static me.marvin.proxy.utils.ByteBufUtils.writeByteArray;

public class EncryptionResponse implements Packet {
    private byte[] encryptedSecretKey;
    private byte[] encryptedVerifyToken;

    public EncryptionResponse() {
        this(null, null);
    }

    public EncryptionResponse(byte[] encryptedSecretKey, byte[] encryptedVerifyToken) {
        this.encryptedSecretKey = encryptedSecretKey;
        this.encryptedVerifyToken = encryptedVerifyToken;
    }

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

    @NotNull
    public byte[] encryptedSecretKey() {
        return encryptedSecretKey;
    }

    @NotNull
    public EncryptionResponse encryptedSecretKey(byte[] encryptedSecretKey) {
        this.encryptedSecretKey = encryptedSecretKey;
        return this;
    }

    @NotNull
    public byte[] encryptedVerifyToken() {
        return encryptedVerifyToken;
    }

    @NotNull
    public EncryptionResponse encryptedVerifyToken(byte[] encryptedVerifyToken) {
        this.encryptedVerifyToken = encryptedVerifyToken;
        return this;
    }
}
