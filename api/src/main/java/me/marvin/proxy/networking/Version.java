package me.marvin.proxy.networking;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Represents a protocol version.
 */
public interface Version {
    Version UNKNOWN = version(-1);
    Version V1_7_2 = version(4, "1.7.2");
    Version V1_7_6 = version(5, "1.7.6");
    Version V1_8 = version(47, "1.8.x");
    Version V1_9 = version(107, "1.9.x");
    Version V1_9_1 = version(108, "1.9.1");
    Version V1_9_2 = version(109, "1.9.2");
    Version V1_9_4 = version(110, "1.9.4");
    Version V1_10 = version(210, "1.10");
    Version V1_11 = version(315, "1.11");
    Version V1_11_1 = version(316, "1.11.1");
    Version V1_12 = version(335, "1.12");
    Version V1_12_1 = version(338, "1.12.1");
    Version V1_12_2 = version(340, "1.12.2");
    Version V1_13 = version(393, "1.13");
    Version V1_13_1 = version(401, "1.13.1");
    Version V1_13_2 = version(404, "1.13.2");
    Version V1_14 = version(477, "1.14");
    Version V1_14_1 = version(480, "1.14.1");
    Version V1_14_2 = version(485, "1.14.2");
    Version V1_14_3 = version(490, "1.14.3");
    Version V1_14_4 = version(498, "1.14.4");
    Version V1_15 = version(573, "1.15");
    Version V1_15_1 = version(575, "1.15.1");
    Version V1_15_2 = version(578, "1.15.2");
    Version V1_16 = version(735, "1.16");
    Version V1_16_1 = version(736, "1.16.1");
    Version V1_16_2 = version(751, "1.16.2");
    Version V1_16_3 = version(753, "1.16.3");
    Version V1_16_4 = version(754, "1.16.4");
    Version V1_17 = version(755, "1.17");
    Version V1_17_1 = version(756, "1.17.1");
    Version V1_18 = version(757, "1.18");
    Version V1_18_2 = version(758, "1.18.2");
    Version V1_19 = version(759, "1.19");
    Version V1_19_1 = version(760, "1.19.1/1.19.2");
    Version V1_19_3 = version(761, "1.19.3");

    /**
     * Returns the numerical representation of this protocol version.
     *
     * @return the numeric representation
     */
    int version();

    /**
     * Returns if this version is older than the given version.
     *
     * @param other the other version
     * @return true if this version is older, false otherwise
     */
    default boolean isOlderThan(@NotNull Version other) {
        return isOlderThan(other.version());
    }

    /**
     * Returns if this version is older than the given version.
     *
     * @param other the other version
     * @return true if this version is older, false otherwise
     */
    default boolean isOlderThan(int other) {
        return compare(other) < 0;
    }

    /**
     * Returns if this version is older or equals to the given version.
     *
     * @param other the other version
     * @return true if this version is older or equivalent, false otherwise
     */
    default boolean isOlderOr(@NotNull Version other) {
        return isOlderOr(other.version());
    }

    /**
     * Returns if this version is older or equals to the given version.
     *
     * @param other the other version
     * @return true if this version is older or equivalent, false otherwise
     */
    default boolean isOlderOr(int other) {
        return compare(other) <= 0;
    }

    /**
     * Returns if this version is newer than the given version.
     *
     * @param other the other version
     * @return true if this version is newer, false otherwise
     */
    default boolean isNewerThan(@NotNull Version other) {
        return isNewerThan(other.version());
    }

    /**
     * Returns if this version is newer than the given version.
     *
     * @param other the other version
     * @return true if this version is newer, false otherwise
     */
    default boolean isNewerThan(int other) {
        return compare(other) > 0;
    }

    /**
     * Returns if this version is newer or equals to the given version.
     *
     * @param other the other version
     * @return true if this version is newer or equivalent, false otherwise
     */
    default boolean isNewerOr(@NotNull Version other) {
        return isNewerOr(other.version());
    }

    /**
     * Returns if this version is newer or equals to the given version.
     *
     * @param other the other version
     * @return true if this version is newer or equivalent, false otherwise
     */
    default boolean isNewerOr(int other) {
        return compare(other) >= 0;
    }

    /**
     * Compares this version with the given version.
     *
     * @param version the other version
     * @return {@code -1} if this version is older, {@code 0} if the versions are equivalent and
     * {@code 1} if this version is newer
     */
    default int compare(Version version) {
        return compare(version.version());
    }

    /**
     * Compares this version with the given version.
     *
     * @param version the other version
     * @return {@code -1} if this version is older, {@code 0} if the versions are equivalent and
     * {@code 1} if this version is newer
     */
    default int compare(int version) {
        return Integer.compare(version(), version);
    }

    /**
     * Creates a new protocol version holder.
     *
     * @param version the numerical representation of the version
     * @return a new protocol version object
     */
    static Version version(int version) {
        return version(version, "?");
    }

    /**
     * Creates a new protocol version holder.
     *
     * @param version   the numerical representation of the version
     * @param displayed the displayed string on {@link Object#toString()}
     * @return a new protocol version object
     */
    static Version version(int version, String displayed) {
        return new Version() {
            // @formatter:off
            @Override public int version() {return version;}
            @Override public int hashCode() {return version;}
            @Override public boolean equals(Object obj) {return obj instanceof Version ver && ver.version() == version;}
            @Override public String toString() {return displayed + " (Protocol version: " + version + ")";}
            // @formatter:on
        };
    }

    /**
     * Creates a new version object for the given protocol version.
     *
     * @param version the version
     * @return the exact version, or a new version object
     */
    static Version exact(@NotNull Version version) {
        return exact(version.version());
    }

    /**
     * Creates a new version object for the given protocol version.
     *
     * @param version the version
     * @return the exact version, or a new version object
     */
    static Version exact(int version) {
        return Constants.EXACT_VERSION_CACHE.computeIfAbsent(version, Version::version);
    }

    /**
     * Returns the closest version for the given protocol version.
     *
     * @param version the version
     * @return the closest version, or -1 if not found
     */
    static Version closest(@NotNull Version version) {
        return closest(version.version());
    }

    /**
     * Returns the closest version for the given protocol version.
     *
     * @param version the version
     * @return the closest version, or -1 if not found
     */
    static Version closest(int version) {
        return Constants.CLOSEST_CACHE.computeIfAbsent(version, Constants.VERSION_MAPPER::apply);
    }

    /**
     * Returns all the known versions.
     *
     * @return all the known versions
     */
    static Collection<Version> knownVersions() {
        return Constants.VERSION_CACHE;
    }

    final class Constants {
        /**
         * Version cache.
         */
        static final Collection<Version> VERSION_CACHE = Arrays.stream(Version.class.getDeclaredFields())
            .map(field -> {
                try {
                    return field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(Version.class::isInstance)
            .map(Version.class::cast)
            .toList();

        /**
         * Exact version cache.
         */
        static final Map<Integer, Version> EXACT_VERSION_CACHE = VERSION_CACHE.stream()
            .collect(Collectors.toMap(Version::version, Function.identity()));

        /**
         * Closest version cache.
         */
        static final Map<Integer, Version> CLOSEST_CACHE = new HashMap<>(EXACT_VERSION_CACHE);

        /**
         * Version mapper.
         */
        static final IntFunction<Version> VERSION_MAPPER = ver -> CLOSEST_CACHE.values().stream()
            .sorted(Comparator.<Version>comparingInt(Version::version).reversed())
            .filter(v -> v.isOlderOr(ver))
            .findFirst()
            .orElse(Version.UNKNOWN);
    }
}
