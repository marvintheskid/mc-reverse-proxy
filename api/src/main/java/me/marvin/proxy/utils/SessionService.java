package me.marvin.proxy.utils;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A simple interface representing a session service.
 */
public interface SessionService {
    /**
     * Default Mojang session service.
     */
    SessionService DEFAULT = new SessionService() {
        private static final URI JOIN_URL = URI.create("https://sessionserver.mojang.com/session/minecraft/join");
        private static final HttpClient CLIENT = HttpClient.newBuilder().build();

        @Override
        public void joinServer(@NotNull GameProfile profile, @NotNull String authenticationToken, @NotNull String serverId) throws Exception {
            JsonObject request = new JsonObject();
            request.addProperty("accessToken", authenticationToken);
            request.addProperty("selectedProfile", profile.uuid().toString());
            request.addProperty("serverId", serverId);

            HttpResponse<String> response = CLIENT.send(HttpRequest.newBuilder()
                .uri(JOIN_URL)
                .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                .header("Content-Type", "application/json")
                .build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204) {
                throw new Exception("An error happened while authenticating %s".formatted(profile.uuid()), new Exception(response.body()));
            }
        }
    };

    /**
     * Attempts to join the specified Minecraft server.
     * <p />
     * The {@link GameProfile} used to join with may be partial, but the exact requirements will vary on
     * authentication service. If this method returns without throwing an exception, the server should accept the player.
     *
     * @param profile partial {@link GameProfile} to join as
     * @param authenticationToken the authenticated token of the user
     * @param serverId the random ID of the server to join
     * @throws Exception if we could not authenticate the user
     */
    void joinServer(@NotNull GameProfile profile, @NotNull String authenticationToken, @NotNull String serverId) throws Exception;
}
