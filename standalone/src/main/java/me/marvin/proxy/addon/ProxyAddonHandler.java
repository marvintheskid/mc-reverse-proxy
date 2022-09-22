package me.marvin.proxy.addon;

import com.google.gson.JsonParser;
import me.marvin.proxy.Proxy;
import me.marvin.proxy.commands.impl.CommandTree;
import me.marvin.proxy.utils.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic proxy addon handler.
 */
public class ProxyAddonHandler {
    private static final Logger LOGGER = LogManager.getLogger("addons");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");
    private static final String FOLDER = "addons";
    private static final String EXTENSION = ".jar";
    private static final String ADDON_INFO = "addon-info.json";

    /**
     * The loaded addons' class loaders.
     */
    private final List<ProxyAddonLoader> loaders;

    /**
     * Creates a new addon handler, and loads all addons.
     *
     * @param proxy the proxy instance
     * @param commandTree the command tree
     * @throws IOException if {@link Files#walk(Path, FileVisitOption...)}} fails
     */
    public ProxyAddonHandler(Proxy proxy, CommandTree commandTree) throws IOException {
        try (Stream<Path> addonsFolder = Files.walk(Files.createDirectories(proxy.parentFolder().resolve(FOLDER)))) {
            this.loaders = addonsFolder
                .filter(file -> file.getFileName().toString().endsWith(EXTENSION))
                .peek(file -> LOGGER.info("Loading {}...", file.getFileName()))
                .map(file -> {
                    try {
                        ProxyAddonInfo info = readInfo(file);

                        if (info == null) {
                            throw new NullPointerException("can't find '" + ADDON_INFO + "' inside " + file.getFileName());
                        }

                        if (!NAME_PATTERN.matcher(info.name()).matches()) {
                            throw new IllegalArgumentException("addon name must match " + NAME_PATTERN + " (" + file.getFileName() + ")");
                        }

                        return Tuple.tuple(file.toUri().toURL(), info);
                    } catch (Exception ex) {
                        LOGGER.error("An error occurred while loading {}", file.getFileName(), ex);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Tuple::second))
                .map(t -> {
                    try {
                        return new ProxyAddonLoader(new URL[]{t.first()}, t.second(), proxy.parentFolder().resolve(FOLDER), proxy, commandTree);
                    } catch (Exception ex) {
                        LOGGER.error("An error occurred while loading {}", t.second().name(), ex);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        }
        this.loaders.forEach(loader -> {
            loader.setOtherLoaders(this.loaders);
            LOGGER.info("Enabling {} {}{}",  loader.info().name(), loader.info().version(), (loader.info().author() == null ? "" : " by " + loader.info().author()) + "...");
            loader.addonInstance().onEnable();
        });
    }

    /**
     * Tries to create a proxy addon info object by reading the given file.
     *
     * @param path the path to the file
     * @return a proxy addon info object, or null
     */
    @Nullable
    private static ProxyAddonInfo readInfo(Path path) {
        try (JarInputStream in = new JarInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {
            JarEntry entry;
            while ((entry = in.getNextJarEntry()) != null) {
                if (entry.getName().equals(ADDON_INFO)) {
                    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        return ProxyAddonInfo.parse(JsonParser.parseReader(reader));
                    }
                }
            }
        } catch (Exception ex) {
            return null;
        }

        return null;
    }

    /**
     * Shuts down this handler.
     */
    public void stop() {
        this.loaders.forEach(loader -> loader.addonInstance().onDisable());
    }
}
