package gbx.proxy.networking.packet.impl.handshake.client;

import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import gbx.proxy.networking.packet.Packet;
import gbx.proxy.networking.packet.PacketType;
import gbx.proxy.networking.packet.PacketTypes;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static gbx.proxy.utils.ByteBufUtils.*;

public class SetProtocol implements Packet {
    private Version protocolVersion;
    private String address;
    private int port;
    private ProtocolPhase nextPhase;

    public SetProtocol() {
        this(null, null, -1, null);
    }

    public SetProtocol(Version protocolVersion, String address, int port, ProtocolPhase nextPhase) {
        this.protocolVersion = protocolVersion;
        this.address = address;
        this.port = port;
        this.nextPhase = nextPhase;
    }

    @Override
    public void encode(@NotNull ByteBuf buf, @NotNull Version version) {
        writeVarInt(buf, protocolVersion.version());
        writeString(buf, address);
        buf.writeShort(port);
        writeVarInt(buf, nextPhase.id());
    }

    @Override
    public void decode(@NotNull ByteBuf buf, @NotNull Version version) {
        protocolVersion = Version.exact(readVarInt(buf));
        address = readString(buf, 255);
        port = buf.readUnsignedShort();
        nextPhase = ProtocolPhase.values()[readVarInt(buf) + 1];
    }

    @Override
    @NotNull
    public PacketType type() {
        return PacketTypes.Handshake.Client.SET_PROTOCOL;
    }

    public Version protocolVersion() {
        return protocolVersion;
    }

    @NotNull
    public String address() {
        return address;
    }

    public int port() {
        return port;
    }

    @NotNull
    public ProtocolPhase nextPhase() {
        return nextPhase;
    }
}
