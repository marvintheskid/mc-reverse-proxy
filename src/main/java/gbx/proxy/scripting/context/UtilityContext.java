package gbx.proxy.scripting.context;

import gbx.proxy.scripting.ScriptProvider;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class UtilityContext implements ScriptProvider {
    private static final UtilityContext INSTANCE = new UtilityContext();

    @Override
    public void provide(@NotNull Value globals) {
        globals.putMember("utils", instance());
    }

    public byte[] sha1(byte[] bytes) {
        return resolveDigest("SHA-1").digest(bytes);
    }

    public byte[] md5(byte[] bytes) {
        return resolveDigest("MD5").digest(bytes);
    }

    public String toHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    public byte[] toByteArray(String string) {
        return string.getBytes();
    }

    private static MessageDigest resolveDigest(String digest) {
        try {
            return MessageDigest.getInstance(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the utility context provider instance.
     *
     * @return this provider
     */
    public static UtilityContext instance() {
        return UtilityContext.INSTANCE;
    }
}
