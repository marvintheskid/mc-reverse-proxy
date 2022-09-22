package me.marvin.proxy.addon;

import me.marvin.proxy.Proxy;
import me.marvin.proxy.commands.impl.CommandTree;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * A {@link URLClassLoader} made for proxy addons.
 */
public class ProxyAddonLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final ProxyAddonInfo info;
    private final ProxyAddon addonInstance;
    private List<ProxyAddonLoader> otherLoaders;

    @SuppressWarnings("unchecked")
    ProxyAddonLoader(URL[] urls, ProxyAddonInfo info, Path rootFolder, Proxy proxy, CommandTree commandTree) throws ReflectiveOperationException {
        super(urls, Proxy.class.getClassLoader());
        this.otherLoaders = List.of();
        this.info = info;
        Class<? extends ProxyAddon> entrypoint = (Class<? extends ProxyAddon>) Class.forName(info.main(), true, this);
        this.addonInstance = entrypoint.getDeclaredConstructor().newInstance();
        this.addonInstance.initialize(info, rootFolder, proxy, LogManager.getLogger(info.name()), commandTree);
    }

    /**
     * Sets the other loaders for lookup.
     *
     * @param otherLoaders the other loaders
     */
    void setOtherLoaders(@NotNull List<ProxyAddonLoader> otherLoaders) {
        this.otherLoaders = otherLoaders;
    }

    /**
     * {@inheritDoc}
     *
     * @param url the URL to be added to the search path of URLs
     */
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     * Returns the associated addon information.
     *
     * @return the addon information
     */
    @NotNull
    public ProxyAddonInfo info() {
        return info;
    }

    /**
     * Returns the associated addon instance.
     *
     * @return the addon instance
     */
    @NotNull
    public ProxyAddon addonInstance() {
        return addonInstance;
    }

    /**
     * {@inheritDoc}
     *
     * @param name    The <a href="#binary-name">binary name</a> of the class
     * @param resolve If {@code true} then resolve the class
     * @return The resulting {@code Class} object
     * @throws ClassNotFoundException If the class was not found
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass(name, resolve, true);
    }

    private Class<?> loadClass(String name, boolean resolve, boolean checkOthers) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (Exception ex) {
            if (checkOthers) {
                for (ProxyAddonLoader loader : otherLoaders) {
                    if (loader != this) {
                        try {
                            return loader.loadClass(name, resolve, false);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            throw ex;
        }
    }
}
