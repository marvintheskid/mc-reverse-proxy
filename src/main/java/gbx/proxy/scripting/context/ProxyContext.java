package gbx.proxy.scripting.context;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import gbx.proxy.ProxyBootstrap;
import gbx.proxy.scripting.ScriptProvider;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

public final class ProxyContext implements ScriptProvider {
    private static final ProxyContext INSTANCE = new ProxyContext();

    @Override
    public void provide(@NotNull Value globals) {
        globals.putMember("proxy", instance());
    }

    public String getName() {
        return ProxyBootstrap.NAME;
    }

    public void setName(String name) {
        ProxyBootstrap.NAME = name;
    }

    public String getUuid() {
        return ProxyBootstrap.UUID;
    }

    public void setUuid(String uuid) {
        ProxyBootstrap.UUID = uuid;
    }

    public String getAccessToken() {
        return ProxyBootstrap.ACCESS_TOKEN;
    }

    public void setAccessToken(String accessToken) {
        ProxyBootstrap.ACCESS_TOKEN = accessToken;
    }

    public MinecraftSessionService getSessionService() {
        return ProxyBootstrap.SESSION_SERVICE;
    }

    public void setSessionService(MinecraftSessionService sessionService) {
        ProxyBootstrap.SESSION_SERVICE = sessionService;
    }

    /**
     * Returns the session context provider instance.
     *
     * @return this provider
     */
    public static ProxyContext instance() {
        return INSTANCE;
    }
}
