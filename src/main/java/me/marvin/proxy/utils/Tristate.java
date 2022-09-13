package me.marvin.proxy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents trinary logic.
 * <p>
 * Logical tables for AND, OR and NEG operations can be found at
 * <a href="https://en.wikipedia.org/wiki/Three-valued_logic#Logics">en.wikipedia.org</a>
 */
public enum Tristate {
    /**
     * Indicates that the value is positive.
     */
    TRUE(true, 1) {
        @Override
        public Tristate and(Tristate other) {
            return other;
        }

        @Override
        public Tristate or(Tristate other) {
            return TRUE;
        }

        @Override
        public Tristate not() {
            return FALSE;
        }
    },

    /**
     * Indicates that the value is non-existent.
     */
    NOT_SET(false, 0) {
        @Override
        public Tristate and(Tristate other) {
            return other == FALSE ? other : NOT_SET;
        }

        @Override
        public Tristate or(Tristate other) {
            return other == TRUE ? other : NOT_SET;
        }

        @Override
        public Tristate not() {
            return NOT_SET;
        }
    },

    /**
     * Indicates that the value is negative.
     */
    FALSE(false, -1) {
        @Override
        public Tristate and(Tristate other) {
            return FALSE;
        }

        @Override
        public Tristate or(Tristate other) {
            return other;
        }

        @Override
        public Tristate not() {
            return TRUE;
        }
    };

    private final boolean booleanValue;
    private final int intValue;

    Tristate(boolean booleanValue, int intValue) {
        this.booleanValue = booleanValue;
        this.intValue = intValue;
    }

    /**
     * Performs an AND operation on this {@code Tristate}.
     *
     * @param other the other state
     * @return a new tristate
     */
    public abstract Tristate and(Tristate other);

    /**
     * Performs an OR operation on this {@code Tristate}.
     *
     * @param other the other state
     * @return a new tristate
     */
    public abstract Tristate or(Tristate other);

    /**
     * Negates this tristate.
     *
     * @return the negated version of this tristate
     */
    public abstract Tristate not();

    /**
     * Returns the boolean value of this {@code Tristate}.
     *
     * @return the boolean value of this tristate
     */
    public boolean booleanValue() {
        return booleanValue;
    }

    /**
     * Returns the int value of this {@code Tristate}.
     *
     * @return the int value of this tristate
     */
    public int intValue() {
        return intValue;
    }

    /**
     * Returns a tristate from a boolean.
     *
     * @param bool the boolean
     * @return the tristate
     */
    @NotNull
    public static Tristate fromBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    /**
     * Returns a tristate from a boxed boolean. The parameter is nullable,
     * therefore returning {@code NOT_SET} when it is null.
     *
     * @param bool the boxed boolean
     * @return the tristate
     */
    @NotNull
    public static Tristate fromBoxedBoolean(@Nullable Boolean bool) {
        if (bool == null) return NOT_SET;
        return bool ? TRUE : FALSE;
    }

    /**
     * Returns a tristate from a number.
     *
     * @param number the number representing the tristate
     * @return the matching tristate, or null
     * @throws IllegalArgumentException if the number does not represent
     * any of the tristates.
     */
    public static Tristate fromNumber(int number) {
        if (number == 1) {
            return TRUE;
        } else if (number == -1) {
            return FALSE;
        } else if (number == 0) {
            return NOT_SET;
        }

        throw new IllegalArgumentException("invalid tristate: " + number);
    }
}