package gbx.proxy.networking.packet;

import gbx.proxy.networking.ProtocolDirection;
import gbx.proxy.networking.ProtocolPhase;
import gbx.proxy.networking.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a packet type.
 */
public interface PacketType {
    /**
     * Returns the version map containing per version packet ids.
     *
     * @return the version map
     */
    Map<Integer, Integer> versionMap();

    /**
     * Returns the id of this packet.
     *
     * @param version the version
     * @return the id of this packet, or -1 if it doesn't exist for the given version
     */
    default int id(@NotNull Version version) {
        return id(version.version());
    }

    /**
     * Returns the id of this packet.
     *
     * @param version the version
     * @return the id of this packet, or -1 if it doesn't exist for the given version
     */
    default int id(int version) {
        return versionMap().getOrDefault(version, -1);
    }

    /**
     * Returns the phase associated with this packet.
     *
     * @return the phase of this packet
     */
    ProtocolPhase phase();

    /**
     * Returns the direction of this packet.
     *
     * @return the direction of this packet
     */
    ProtocolDirection direction();

    /**
     * Returns if the 2 packet types are the same.
     *
     * @param type    the type
     * @param version the version
     * @return true if the packet types match, false otherwise
     */
    default boolean is(@NotNull PacketType type, @NotNull Version version) {
        return is(type.id(version), version) &&
            phase() == type.phase() &&
            direction() == type.direction();
    }

    /**
     * Returns if the 2 packet types are the same.
     * <br>
     * <b>Note: this method only compares packet ids.
     *
     * @param packetId the packet id
     * @param version  the version
     * @return true if the packet types match, false otherwise
     */
    default boolean is(int packetId, @NotNull Version version) {
        return id(version) == packetId;
    }
}
