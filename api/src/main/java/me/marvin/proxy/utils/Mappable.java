package me.marvin.proxy.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a mappable data type.
 *
 * @param <T> the type of the mappable object
 */
public interface Mappable<T extends Mappable<T>> {
    /**
     * Maps this wrapper into another object.
     *
     * @param mapper the mapper function
     * @param <R>    the type of the result
     * @return a mapped result
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default <R> R map(@NotNull Function<? super T, ? extends R> mapper) {
        return mapper.apply((T) this);
    }
}
