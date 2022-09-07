package gbx.proxy.networking.packet.impl.handshake.client;

import gbx.proxy.ProxyBootstrap;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.Packet;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static gbx.proxy.utils.ByteBufUtils.readString;
import static gbx.proxy.utils.ByteBufUtils.writeString;

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
