package me.marvin.proxy.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Throwable-related utils.
 */
public interface Throwables {
    /**
     * Rethrows the given {@link Throwable} sneakily.
     *
     * @param t the throwable
     */
    static void sneaky(@NotNull Throwable t) {
        throw Throwables.superSneaky(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T superSneaky(Throwable t) throws T {
        throw (T) t;
    }
}
