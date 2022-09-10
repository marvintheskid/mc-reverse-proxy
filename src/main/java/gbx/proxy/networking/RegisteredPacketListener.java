package gbx.proxy.networking;

import org.jetbrains.annotations.NotNull;

/**
 * A simple record wrapping a {@link PacketListener}.
 *
 * @param owner the owner
 * @param listener the listener
 */
public record RegisteredPacketListener(Object owner, PacketListener listener) implements Comparable<RegisteredPacketListener> {
    @Override
    public int compareTo(@NotNull RegisteredPacketListener o) {
        return listener.compareTo(o.listener());
    }
}
