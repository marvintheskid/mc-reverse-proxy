package me.marvin.proxy.utils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns no
 * result. This is the three-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code TriConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <S> the type of the second argument to the operation
 * @param <U> the type of the third argument to the operation
 *
 * @see Consumer
 * @see BiConsumer
 */
@FunctionalInterface
public interface TriConsumer<T, S, U> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param s the second input argument
     * @param u the third input argument
     */
    void accept(T t, S s, U u);

    /**
     * Returns a composed {@code TriConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation. If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code TriConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default TriConsumer<T, S, U> andThen(TriConsumer<? super T, ? super S, ? super U> after) {
        Objects.requireNonNull(after);

        return (t, s, u) -> {
            accept(t, s, u);
            after.accept(t, s, u);
        };
    }
}
