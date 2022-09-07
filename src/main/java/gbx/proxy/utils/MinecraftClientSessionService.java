package gbx.proxy.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;

import java.net.InetAddress;
import java.util.Map;

/**
 * A helper interface for {@link MinecraftSessionService}.
 */
public interface MinecraftClientSessionService extends MinecraftSessionService {
    /**
     * {@inheritDoc}
     *
     * @param profile Partial {@link com.mojang.authlib.GameProfile} to join as
     * @param authenticationToken The {@link com.mojang.authlib.UserAuthentication#getAuthenticatedToken() authenticated token} of the user
     * @param serverId The random ID of the server to join
     * @throws com.mojang.authlib.exceptions.AuthenticationUnavailableException Thrown when the servers return a malformed response, or are otherwise unavailable
     * @throws com.mojang.authlib.exceptions.InvalidCredentialsException Thrown when the specified authenticationToken is invalid
     * @throws com.mojang.authlib.exceptions.AuthenticationException Generic exception indicating that we could not authenticate the user
     */
    void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException;

    /**
     * {@inheritDoc}
     */
    default GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) {
        throw new UnsupportedOperationException("MinecraftSessionService#hasJoinedServer(GameProfile, String, String) is not supported.");
    }

    /**
     * {@inheritDoc}
     */
    default Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        throw new UnsupportedOperationException("MinecraftSessionService#getTextures(GameProfile, boolean) is not supported.");
    }

    /**
     * {@inheritDoc}
     */
    default GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        throw new UnsupportedOperationException("MinecraftSessionService#fillProfileProperties(GameProfile, boolean) is not supported.");
    }

    /**
     * {@inheritDoc}
     */
    default String getSecurePropertyValue(Property property) {
        throw new UnsupportedOperationException("MinecraftSessionService#getSecurePropertyValue(Property) is not supported.");
    }
}
