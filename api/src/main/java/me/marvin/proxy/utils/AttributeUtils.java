package me.marvin.proxy.utils;

import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link AttributeMap} related utils.
 */
public interface AttributeUtils {
    /**
     * Updates the given attribute key with the given value for all the given {@link AttributeMap attribute maps}.
     *
     * @param key the key
     * @param value the new value
     * @param maps the attribute maps
     * @param <T> the type of the value
     */
    static <T> void update(@NotNull AttributeKey<T> key, @Nullable T value, @NotNull AttributeMap... maps) {
        for (AttributeMap map : maps) {
            map.attr(key).set(value);
        }
    }
}
