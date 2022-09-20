package me.marvin.proxy.addon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents addon information.
 *
 * @param name the name of the addon
 * @param main the main class of the addon
 * @param version the version of the addon
 * @param priority the priority of the addon
 * @param author the author of the addon
 */
public record ProxyAddonInfo(String name, String main, String version, int priority, String author) implements Comparable<ProxyAddonInfo> {
    /**
     * Creates a new info object out of the given json element.
     *
     * @param element the element
     * @return a new info object
     */
    public static ProxyAddonInfo parse(JsonElement element) {
        if (element instanceof JsonObject object) {
            String name = get(object, "name", JsonElement::getAsString);
            String main = get(object, "main", JsonElement::getAsString);
            String version = get(object, "version", JsonElement::getAsString);

            int priority = getOr(object, "priority", 0, JsonElement::getAsInt);
            String author = getOr(object, "author", null, JsonElement::getAsString);

            return new ProxyAddonInfo(name, main, version, priority, author);
        } else {
            throw new IllegalArgumentException("ProxyAddonInfos are serialized as json objects");
        }
    }

    private static <T> T get(JsonObject obj, String path, Function<? super JsonElement, T> transformer) {
        T result = getOr(obj, path, null, transformer);

        if (result != null) {
            return result;
        }

        throw new NullPointerException("missing required json field: '" + path + "'");
    }

    private static <T> T getOr(JsonObject obj, String path, T defaultValue, Function<? super JsonElement, T> transformer) {
        JsonElement element = obj.get(path);

        if (element != null) {
            return transformer.apply(element);
        }

        return defaultValue;
    }

    @Override
    public int compareTo(@NotNull ProxyAddonInfo o) {
        return Integer.compare(priority(), o.priority());
    }
}
