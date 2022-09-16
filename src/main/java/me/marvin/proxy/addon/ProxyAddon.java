package me.marvin.proxy.addon;

import me.marvin.proxy.Proxy;
import me.marvin.proxy.networking.PacketListener;
import me.marvin.proxy.networking.RegisteredPacketListener;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A simple class wrapping proxy addons.
 */
public abstract class ProxyAddon {
    protected ProxyAddonInfo info;
    protected Path rootFolder;
    protected Proxy proxy;

    /**
     * Initializes this proxy addon.
     *
     * @param info       the info
     * @param rootFolder the root folder where this addon is located
     */
    void initialize(@NotNull ProxyAddonInfo info, @NotNull Path rootFolder, @NotNull Proxy proxy) {
        this.info = info;
        this.rootFolder = rootFolder;
        this.proxy = proxy;
    }

    /**
     * Gets called when the addon is loaded and enabled.
     */
    public void onEnable() {
    }

    /**
     * Gets called when the proxy is shutting down.
     */
    public void onDisable() {
    }

    /**
     * Registers the given listeners with the given owner.
     *
     * @param listeners the listeners
     */
    public void registerListeners(PacketListener... listeners) {
        RegisteredPacketListener[] registeredListeners = new RegisteredPacketListener[listeners.length];
        for (int i = 0; i < listeners.length; i++) {
            registeredListeners[i] = new RegisteredPacketListener(this, listeners[i]);
        }
        proxy.registerListeners(registeredListeners);
    }

    /**
     * Unregisters the given owner or the listeners themselves.
     *
     * @param objects the objects
     */
    public void unregisterListeners(Object... objects) {
        proxy.unregisterListeners(listener -> {
            for (Object object : objects) {
                if (listener == object) return true;
                if (listener instanceof RegisteredPacketListener registered) {
                    if (object instanceof PacketListener packetListener) {
                        if (registered.listener() == packetListener) return true;
                    } else {
                        if (registered.owner() == object) return true;
                    }
                }
            }

            return false;
        });
    }
}
