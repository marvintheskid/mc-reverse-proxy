package me.marvin.proxy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a game profile without properties.
 */
public interface GameProfile extends Mappable<GameProfile> {
    /**
     * Returns the uuid of this game profile.
     *
     * @return the uuid of this game profile
     */
    @Nullable
    UUID uuid();

    /**
     * Returns the name of this game profile.
     *
     * @return the name of this game profile
     */
    @Nullable
    String name();

    /**
     * Creates a new game profile.
     *
     * @param uuid the uuid
     * @param name the name
     * @return a new game profile
     */
    @NotNull
    static GameProfile gameProfile(@Nullable UUID uuid, @Nullable String name) {
        // @formatter:off
        return new GameProfile() {
            @Override @Nullable public UUID uuid() {return uuid;}
            @Override @Nullable public String name() {return name;}
        };
        // @formatter:on
    }
}
