package me.marvin.proxy.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a generic tuple of two values.
 *
 * @param <A> the type of the first value
 * @param <B> the type of the second value
 */
public interface Tuple<A, B> {
    /**
     * Returns the first value of the tuple.
     *
     * @return the first value
     */
    A first();

    /**
     * Returns the second value of the tuple.
     *
     * @return the second value
     */
    B second();

    /**
     * Returns an immutable tuple instance.
     *
     * @param first  the first value
     * @param second the second value
     * @param <A>    the type of the first value
     * @param <B>    the type of the second value
     * @return a new immutable tuple
     */
    static <A, B> Tuple<A, B> tuple(@Nullable A first, @Nullable B second) {
        // @formatter:off
        return new Tuple<>() {
            @Override public A first() {return first;}
            @Override public B second() {return second;}
        };
        // @formatter:on
    }

    /**
     * Returns a mutable tuple instance.
     *
     * @param first  the first value
     * @param second the second value
     * @param <A>    the type of the first value
     * @param <B>    the type of the second value
     * @return a new mutable tuple
     */
    static <A, B> Mutable<A, B> mutableTuple(@Nullable A first, @Nullable B second) {
        // @formatter:off
        return new Tuple.Mutable<>() {
            A firstVal = first; B secondVal = second;
            @Override public A first() {return firstVal;}
            @Override public B second() {return secondVal;}
            @Override public Tuple.Mutable<A, B> first(A first) {firstVal = first; return this;}
            @Override public Tuple.Mutable<A, B> second(B second) {secondVal = second; return this;}
        };
        // @formatter:on
    }

    /**
     * Represents a generic mutable tuple of two values.
     *
     * @param <A> the type of the first value
     * @param <B> the type of the second value
     */
    interface Mutable<A, B> extends Tuple<A, B> {
        /**
         * Sets the first value of the tuple.
         *
         * @param first the first value
         * @return this tuple
         */
        Tuple.Mutable<A, B> first(A first);

        /**
         * Sets the second value of the tuple.
         *
         * @param second the second value
         * @return this tuple
         */
        Tuple.Mutable<A, B> second(B second);
    }
}