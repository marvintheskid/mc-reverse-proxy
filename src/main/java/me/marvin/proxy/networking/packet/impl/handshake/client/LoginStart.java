package me.marvin.proxy.networking.packet.impl.handshake.client;

import me.marvin.proxy.networking.Version;
import me.marvin.proxy.networking.packet.Packet;
import me.marvin.proxy.networking.packet.PacketType;
import me.marvin.proxy.networking.packet.PacketTypes;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static me.marvin.proxy.utils.ByteBufUtils.readString;
import static me.marvin.proxy.utils.ByteBufUtils.writeString;

public class LoginStart implements Packet {
    private String name;

    public LoginStart() {
        this(null);
    }

    public LoginStart(String name) {
        this.name = name;
    }

    @Override
    public void encode(@NotNull ByteBuf buf, @NotNull Version version) {
        writeString(buf, name);
    }

    @Override
    public void decode(@NotNull ByteBuf buf, @NotNull Version version) {
        name = readString(buf, 16);
    }

    @Override
    @NotNull
    public PacketType type() {
        return PacketTypes.Login.Client.LOGIN_START;
    }

    @NotNull
    public String name() {
        return name;
    }
}
